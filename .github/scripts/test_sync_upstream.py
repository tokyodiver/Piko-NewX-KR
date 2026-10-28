"""Guards against discarding KR edits or accepting an upstream conflict."""
from pathlib import Path
import base64
import json
import tempfile
import unittest
from unittest.mock import patch

import sync_upstream
from sync_upstream import apply_source_delta, git, merge_properties, version_number


class SyncSafeguardsTest(unittest.TestCase):
    def test_latest_release_uses_version_checked_post_release_manifest(self):
        with tempfile.TemporaryDirectory(prefix="piko-sync-plan-") as directory:
            root = Path(directory)
            (root / ".github").mkdir()
            old, new = "a" * 40, "b" * 40
            (root / ".github/upstream.json").write_text(json.dumps({"release": "v3.64.1", "commit": old}))
            (root / "gradle.properties").write_text("version = 3.64.1-v5\n")
            (root / "patches-bundle.json").write_text(json.dumps({"version": "v3.64.1-v5", "piko_commit": old}))
            release = {"tag_name": "v3.65.0", "draft": False, "prerelease": False}
            manifest = {"version": "v3.65.0", "piko_commit": new, "app_version": "12.37.2-prod.01"}
            for matching in (True, False):
                manifest["version"] = "v3.65.0" if matching else "v3.64.1"
                contents = {"content": base64.b64encode(json.dumps(manifest).encode()).decode()}
                with patch.object(sync_upstream, "ROOT", root), patch.object(sync_upstream, "api", side_effect=[release, contents]) as api, patch.object(sync_upstream, "output") as output, patch("sys.argv", ["sync_upstream.py", "--check-only"]):
                    if matching:
                        sync_upstream.main()
                        self.assertEqual("check", output.call_args.args[0])
                        self.assertEqual("repos/crimera/piko-newx/contents/patches-bundle.json?ref=main", api.call_args.args[0])
                    else:
                        with self.assertRaises(ValueError):
                            sync_upstream.main()
                        output.assert_not_called()

    def test_properties_keep_kr_options_and_take_upstream_changes(self):
        middle = "".join(f"unchanged{i}=true\n" for i in range(10))
        base = "version = 3.64.1\nworkers=2\n" + middle
        current = base.replace("3.64.1", "3.64.1-v5") + "kr.option=true\n"
        incoming = base.replace("3.64.1", "3.65.0").replace("workers=2", "workers=3")
        result = merge_properties(base, current, incoming, "3.65.0-v1")
        self.assertIn("version = 3.65.0-v1", result)
        self.assertIn("workers=3", result)
        self.assertIn("kr.option=true", result)
        with self.assertRaises(RuntimeError):
            merge_properties(base, current.replace("workers=2", "workers=4"), incoming, "3.65.0-v1")

    def test_release_names_cannot_be_prereleases_or_commands(self):
        self.assertEqual("3.65.0", version_number("v3.65.0"))
        for invalid in ("v3.65.0-beta.1", "main", "v3.65.0\nunsafe", "../main"):
            with self.assertRaises(ValueError):
                version_number(invalid)

    def fixture(self, root, conflicting):
        git(root, "init", "-q")
        git(root, "config", "user.name", "Sync test")
        git(root, "config", "user.email", "sync-test@example.invalid")
        git(root, "config", "core.autocrlf", "false")
        source = root / "patches/sample.txt"
        source.parent.mkdir()
        workflow = root / ".github/workflows/build.yml"
        workflow.parent.mkdir(parents=True)
        base_text = "upstream=old\n" + "unchanged\n" * 10 + "translation=original\n"
        source.write_text(base_text, encoding="utf-8")
        workflow.write_text("original workflow\n", encoding="utf-8")
        git(root, "add", ".")
        git(root, "commit", "-qm", "base")
        base = git(root, "rev-parse", "HEAD").stdout.decode().strip()
        source.write_text(base_text.replace("upstream=old", "upstream=new"), encoding="utf-8")
        workflow.write_text("upstream workflow change\n", encoding="utf-8")
        git(root, "add", ".")
        git(root, "commit", "-qm", "upstream")
        incoming = git(root, "rev-parse", "HEAD").stdout.decode().strip()
        git(root, "checkout", "--detach", base)
        custom = base_text.replace("upstream=old", "upstream=KR") if conflicting else base_text.replace("translation=original", "translation=한국어")
        source.write_text(custom, encoding="utf-8")
        workflow.write_text("KR workflow\n", encoding="utf-8")
        git(root, "add", ".")
        git(root, "commit", "-qm", "KR")
        return base, incoming, source, workflow

    def test_three_way_update_preserves_kr_and_protects_workflows(self):
        with tempfile.TemporaryDirectory(prefix="piko-sync-test-") as directory:
            root = Path(directory)
            base, incoming, source, workflow = self.fixture(root, False)
            apply_source_delta(root, base, incoming)
            self.assertIn("upstream=new", source.read_text(encoding="utf-8"))
            self.assertIn("translation=한국어", source.read_text(encoding="utf-8"))
            self.assertEqual("KR workflow\n", workflow.read_text(encoding="utf-8"))

    def test_conflicting_source_stops_without_committing(self):
        with tempfile.TemporaryDirectory(prefix="piko-sync-test-") as directory:
            root = Path(directory)
            base, incoming, _, _ = self.fixture(root, True)
            before = git(root, "rev-parse", "HEAD").stdout
            with self.assertRaises(RuntimeError):
                apply_source_delta(root, base, incoming)
            self.assertEqual(before, git(root, "rev-parse", "HEAD").stdout)


if __name__ == "__main__":
    unittest.main()
