"""Apply upstream piko-newx's NewX-only build filtering in the CI checkout."""
from pathlib import Path
import shutil

root = Path(__file__).resolve().parents[2]

def remove(relative):
    target = (root / relative).resolve()
    if not target.is_relative_to(root) or target == root:
        raise ValueError("Build cleanup escaped checkout")
    if target.is_dir():
        shutil.rmtree(target)
    elif target.exists():
        target.unlink()

for relative in (
    "patches/src/main/kotlin/app/crimera/patches/instagram",
    "patches/src/main/kotlin/app/crimera/patches/twitter",
    "patches/src/main/kotlin/app/revanced",
    "extensions/instagram", "extensions/twitter",
):
    remove(relative)

twitter = root / "patches/src/main/resources/twitter"
if twitter.exists():
    for item in twitter.iterdir():
        if item.name != "bringbacktwitter":
            remove(item.relative_to(root))
resources = root / "patches/src/main/resources/addresources"
for name in ("instagram", "twitter"):
    for item in resources.glob(f"*/{name}"):
        remove(item.relative_to(root))
print("Prepared NewX-only source; Korean NewX resources retained")
