#!/usr/bin/env python3
"""Select a patch version from GitHub releases; reuse it on a commit rerun."""

import json
import os
from pathlib import Path
import re
import subprocess
import sys


VERSION = re.compile(r"(0|[1-9]\d*)\.(0|[1-9]\d*)\.(0|[1-9]\d*)")


def parse_version(value):
    match = VERSION.fullmatch(value)
    if not match:
        raise ValueError(f"Invalid mod version: {value}")
    return tuple(map(int, match.groups()))


def select_version(releases, commit, tag_commits, base_version):
    base = parse_version(base_version)
    stable = [r for r in releases if not r.get("prerelease")
              and VERSION.fullmatch(r["tag_name"].removeprefix("v"))
              and r["tag_name"].startswith("v")]
    stable.sort(key=lambda r: parse_version(r["tag_name"][1:]))
    existing = [r for r in stable if tag_commits[r["tag_name"]] == commit]
    if existing:
        return existing[-1]["tag_name"][1:]
    if not stable:
        return base_version
    latest = parse_version(stable[-1]["tag_name"][1:])
    if base > latest:
        return base_version
    major, minor, patch = latest
    return f"{major}.{minor}.{patch + 1}"


def main():
    pages = json.loads(Path(sys.argv[1]).read_text())
    releases = [release for page in pages for release in page]
    properties = {}
    for line in Path("gradle.properties").read_text().splitlines():
        if "=" in line and not line.lstrip().startswith("#"):
            key, value = line.split("=", 1)
            properties[key.strip()] = value.strip()
    tag_commits = {}
    for release in releases:
        tag = release["tag_name"]
        if release.get("prerelease") or not tag.startswith("v") or not VERSION.fullmatch(tag[1:]):
            continue
        # Includes drafts so a failed publication can resume its reserved version.
        tag_commits[tag] = subprocess.check_output(
            ["git", "rev-parse", "--verify", f"refs/tags/{tag}^{{commit}}"], text=True).strip()
    version = select_version(releases, os.environ["GITHUB_SHA"], tag_commits, properties["mod_version"])
    with open(os.environ["GITHUB_ENV"], "a") as env:
        env.write(f"OPM_VERSION={version}\n")
    with open(os.environ["GITHUB_OUTPUT"], "a") as output:
        output.write(f"version={version}\ntag=v{version}\n")
    print(f"Release version: {version}")


if __name__ == "__main__":
    main()
