#!/usr/bin/env python3
"""Select release versions and protect Latest when an older draft is retried."""

import argparse
import json
import os
from pathlib import Path
import re
import subprocess


VERSION = re.compile(r"(0|[1-9]\d*)\.(0|[1-9]\d*)\.(0|[1-9]\d*)")


def parse_version(value):
    match = VERSION.fullmatch(value)
    if not match:
        raise ValueError(f"Invalid mod version: {value}")
    return tuple(map(int, match.groups()))


def stable_releases(releases):
    return [release for release in releases
            if not release.get("prerelease")
            and release["tag_name"].startswith("v")
            and VERSION.fullmatch(release["tag_name"][1:])]


def should_mark_latest(releases, selected_version):
    selected = parse_version(selected_version)
    published = [parse_version(release["tag_name"][1:])
                 for release in stable_releases(releases)
                 if not release.get("draft")]
    return not published or selected >= max(published)


def select_version(releases, commit, tag_commits, base_version):
    base = parse_version(base_version)
    stable = stable_releases(releases)
    stable.sort(key=lambda r: parse_version(r["tag_name"][1:]))
    existing = [r for r in stable if tag_commits.get(r["tag_name"]) == commit]
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
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("releases_file", type=Path)
    parser.add_argument("--latest-for", metavar="VERSION",
                        help="print whether this version should be marked Latest")
    args = parser.parse_args()
    pages = json.loads(args.releases_file.read_text())
    releases = [release for page in pages for release in page]
    if args.latest_for is not None:
        print(str(should_mark_latest(releases, args.latest_for)).lower())
        return
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
        # Drafts can precede their tags, and published tags can be deleted.
        # Only an immutable commit target is safe to reuse; mutable branches
        # reserve a version but cannot establish what an earlier run built.
        resolved = subprocess.run(
            ["git", "rev-parse", "--verify", "--quiet", f"refs/tags/{tag}^{{commit}}"],
            text=True, capture_output=True, check=False)
        if resolved.returncode == 0:
            tag_commits[tag] = resolved.stdout.strip()
        elif re.fullmatch(r"[0-9a-fA-F]{40}", release.get("target_commitish", "")):
            target = subprocess.run(
                ["git", "rev-parse", "--verify", "--quiet", f'{release["target_commitish"]}^{{commit}}'],
                text=True, capture_output=True, check=False)
            if target.returncode == 0:
                tag_commits[tag] = target.stdout.strip()
    version = select_version(releases, os.environ["GITHUB_SHA"], tag_commits, properties["mod_version"])
    with open(os.environ["GITHUB_ENV"], "a") as env:
        env.write(f"OPM_VERSION={version}\n")
    with open(os.environ["GITHUB_OUTPUT"], "a") as output:
        output.write(f"version={version}\ntag=v{version}\n")
    print(f"Release version: {version}")


if __name__ == "__main__":
    main()
