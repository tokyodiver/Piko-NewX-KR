"""Apply a pinned upstream release as a three-way source delta, retaining KR edits."""
import argparse
import json
import os
from pathlib import Path
import re
import subprocess
import tempfile
from datetime import datetime, timezone
from urllib.error import HTTPError
from urllib.request import Request, urlopen

SOURCE = "crimera/piko-newx"
CODE = "https://github.com/crimera/piko.git"
ROOT = Path(__file__).resolve().parents[2]
# Upstream workflows, agent instructions, and KR distribution metadata stay locally owned.
SOURCE_PATHS = ["patches", "extensions", "gradle", "build.gradle.kts", "settings.gradle.kts",
                "gradlew", "gradlew.bat", "patch-twitter.sh", "patches-list.json",
                ".gitignore", ".gitattributes", "LICENSE", "NOTICE", "CHANGELOG.md"]


def git(root, *args, data=None, check=True):
    result = subprocess.run(["git", *args], cwd=root, input=data, capture_output=True)
    if check and result.returncode:
        raise RuntimeError(result.stderr.decode("utf-8", errors="replace") or
                           result.stdout.decode("utf-8", errors="replace"))
    return result


def api(path, missing=False):
    headers = {"Accept": "application/vnd.github+json", "User-Agent": "Piko-NewX-KR-sync"}
    if os.environ.get("GH_TOKEN"):
        headers["Authorization"] = "Bearer " + os.environ["GH_TOKEN"]
    try:
        with urlopen(Request("https://api.github.com/" + path, headers=headers), timeout=60) as response:
            return json.load(response)
    except HTTPError as error:
        if missing and error.code == 404:
            return None
        raise RuntimeError(f"GitHub API HTTP {error.code}: {path}") from error


def version_number(tag):
    match = re.fullmatch(r"v(\d+\.\d+\.\d+)", tag)
    if not match:
        raise ValueError("Expected a stable upstream vMAJOR.MINOR.PATCH release")
    return match[1]


def property_version(text):
    matches = re.findall(r"^version\s*=\s*(\S+)\s*$", text, re.MULTILINE)
    if len(matches) != 1:
        raise ValueError("Expected one Gradle version property")
    return matches[0]


def merge_properties(base, current, incoming, version):
    # Only the distribution version is KR-owned; other property conflicts must stop the run.
    def normalized(text):
        property_version(text)
        return re.sub(r"^version\s*=.*$", "version = KR_VERSION", text, flags=re.MULTILINE)
    with tempfile.TemporaryDirectory(prefix="piko-properties-") as directory:
        paths = [Path(directory) / name for name in ("current", "base", "incoming")]
        for path, text in zip(paths, (current, base, incoming)):
            path.write_text(normalized(text), encoding="utf-8", newline="\n")
        result = subprocess.run(["git", "merge-file", "-p", *map(str, paths)], capture_output=True)
        if result.returncode:
            raise RuntimeError("Upstream gradle.properties conflicts with KR settings")
        return result.stdout.decode("utf-8").replace("version = KR_VERSION", "version = " + version)


def apply_source_delta(root, old, new):
    patch = git(root, "diff", "--binary", "--full-index", "--no-renames", old, new, "--", *SOURCE_PATHS).stdout
    if patch:
        git(root, "apply", "--3way", "--index", data=patch)


def output(mode, tag, sha, version, app):
    values = {"mode": mode, "version": version, "upstream_tag": tag,
              "upstream_sha": sha, "app_version": app}
    if os.environ.get("GITHUB_OUTPUT"):
        with open(os.environ["GITHUB_OUTPUT"], "a", encoding="utf-8") as stream:
            stream.write("".join(f"{key}={value}\n" for key, value in values.items()))
    summary = (f"## 원본 Piko 업데이트 확인\n\n"
               f"- 원본: [{tag}](https://github.com/{SOURCE}/releases/tag/{tag}) (`{sha[:7]}`)\n"
               f"- KR: `{version}` / 대상 X: `{app}`\n"
               f"- 결과: {dict(current='최신 상태 — 변경 없음', check='새 원본 발견 — 확인만 실행', pending='소스는 최신이나 KR 배포 미완료 — 확인만 실행', update='새 원본 병합 완료 — 빌드·검증 대기', publish='미완료 배포 재시도 — 빌드·검증 대기')[mode]}\n")
    print(summary)
    if os.environ.get("GITHUB_STEP_SUMMARY"):
        with open(os.environ["GITHUB_STEP_SUMMARY"], "a", encoding="utf-8") as stream:
            stream.write(summary)


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--check-only", action="store_true")
    args = parser.parse_args()
    check_only = args.check_only or os.environ.get("CHECK_ONLY") == "true"
    state = json.loads((ROOT / ".github/upstream.json").read_text(encoding="utf-8"))
    local = json.loads((ROOT / "patches-bundle.json").read_text(encoding="utf-8"))
    properties = (ROOT / "gradle.properties").read_text(encoding="utf-8")
    current = property_version(properties)
    if local["version"] != "v" + current or local["piko_commit"] != state["commit"]:
        raise ValueError("KR version/source metadata is inconsistent")
    release = api(f"repos/{SOURCE}/releases/latest")
    tag = release["tag_name"]
    base_version = version_number(tag)
    if release["draft"] or release["prerelease"]:
        raise ValueError("Only published stable releases may be synced")
    import base64
    # Upstream updates this manifest after publishing its tag; the tag contains older metadata.
    contents = api(f"repos/{SOURCE}/contents/patches-bundle.json?ref=main")
    upstream = json.loads(base64.b64decode(contents["content"]))
    sha, app = upstream["piko_commit"], upstream["app_version"]
    if upstream["version"] != tag or not re.fullmatch(r"[0-9a-f]{40}", sha):
        raise ValueError("Upstream tag and source commit metadata do not match")
    if not re.fullmatch(r"[A-Za-z0-9._+-]+", app):
        raise ValueError("Invalid upstream app version")
    if not re.fullmatch(r"[0-9a-f]{40}", state["commit"]):
        raise ValueError("Invalid baseline source commit")
    if tuple(map(int, base_version.split('.'))) < tuple(map(int, version_number(state["release"]).split('.'))):
        raise ValueError("Refusing to downgrade the upstream source")
    if (tag, sha) == (state["release"], state["commit"]):
        published = api(f"repos/{os.environ['GITHUB_REPOSITORY']}/releases/tags/v{current}", missing=True)
        ready = (published and not published["draft"] and not published["prerelease"] and
                 any(asset["name"] == f"piko-newx-kr-{current}.mpp" for asset in published["assets"]))
        output("current" if ready else "pending" if check_only else "publish", tag, sha, current, app)
        return
    if check_only:
        output("check", tag, sha, current, app)
        return
    if git(ROOT, "status", "--porcelain", "--untracked-files=no").stdout:
        raise RuntimeError("Tracked working tree changes must be committed before syncing")
    revision = re.fullmatch(re.escape(base_version) + r"-v(\d+)", current)
    version = f"{base_version}-v{int(revision[1]) + 1 if revision else 1}"
    git(ROOT, "fetch", "--no-tags", "--depth=1", CODE, state["commit"], sha)
    base = git(ROOT, "show", state["commit"] + ":gradle.properties").stdout.decode("utf-8")
    incoming = git(ROOT, "show", sha + ":gradle.properties").stdout.decode("utf-8")
    merged = merge_properties(base, properties, incoming, version)
    apply_source_delta(ROOT, state["commit"], sha)
    (ROOT / "gradle.properties").write_text(merged, encoding="utf-8", newline="\n")
    local.update(version="v" + version, piko_commit=sha, app_version=app,
                 created_at=datetime.now(timezone.utc).strftime("%Y-%m-%dT%H:%M:%S"),
                 download_url=f"https://github.com/{os.environ['GITHUB_REPOSITORY']}/releases/download/v{version}/piko-newx-kr-{version}.mpp",
                 description=f"Piko NewX KR: synced from {SOURCE} {tag}; existing Korean and custom accent changes retained. Device UI testing pending.")
    (ROOT / "patches-bundle.json").write_text(json.dumps(local, indent=2) + "\n", encoding="utf-8")
    (ROOT / ".github/upstream.json").write_text(json.dumps({"release": tag, "commit": sha}, indent=2) + "\n", encoding="utf-8")
    readme = (ROOT / "README.md").read_text(encoding="utf-8")
    readme = re.sub(r"^원본 \[crimera/piko-newx .*?$", f"원본 [{SOURCE} {tag}](https://github.com/{SOURCE}/releases/tag/{tag})과 동일한 Piko x-lite `{sha}` 기반의 비공식 수정본입니다. 원본처럼 NewX 전용으로 빌드합니다.", readme, flags=re.MULTILINE)
    readme = re.sub(r"^원본 버전은 .*?$", f"원본 버전은 `{base_version}`로 유지하고 수정 차수를 `-v1`, `-v2`처럼 붙입니다. 현재 수정본은 `{version}`, 파일명은 `piko-newx-kr-{version}.mpp`입니다.", readme, flags=re.MULTILINE)
    readme = re.sub(r"4\. 원본 X \*\*[^*]+\*\* APKM", f"4. 원본 X **{app}** APKM", readme)
    constants = (ROOT / "patches/src/main/kotlin/app/crimera/patches/newx/utils/Constants.kt").read_text(encoding="utf-8")
    versions = re.findall(r'AppTarget\(version\s*=\s*"([^"]+)"', constants)
    if not versions or app not in versions:
        raise ValueError("Upstream app version is not declared by the merged NewX source")
    readme = re.sub(r"^현재 소스가 선언한 지원 버전: .*?$", "현재 소스가 선언한 지원 버전: " + ", ".join(versions) + ".", readme, flags=re.MULTILINE)
    (ROOT / "README.md").write_text(readme, encoding="utf-8", newline="\n")
    output("update", tag, sha, version, app)


if __name__ == "__main__":
    main()
