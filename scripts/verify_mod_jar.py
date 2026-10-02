#!/usr/bin/env python3
"""Check the installable Fabric JAR, including its embedded release version."""

import json
import os
from pathlib import Path
import subprocess
import sys
from zipfile import ZipFile


def main():
    properties = dict(line.strip().split("=", 1) for line in Path("gradle.properties").read_text().splitlines()
                      if "=" in line and not line.lstrip().startswith("#"))
    version = os.environ.get("OPM_VERSION", properties["mod_version"])
    count = subprocess.check_output(["git", "rev-list", "--count", "HEAD"], text=True).strip()
    name = f"{properties['archives_base_name']}-{version}+mc{properties['minecraft_version']}-b{count}.jar"
    directory = Path(sys.argv[1] if len(sys.argv) > 1 else "build/libs")
    jars = [path for path in directory.glob("*.jar") if not path.name.endswith("-sources.jar")]
    if len(jars) != 1 or jars[0].name != name:
        raise ValueError(f"Expected exactly one installable JAR named {name}; found {jars}")
    with ZipFile(jars[0]) as jar:
        if jar.testzip() is not None:
            raise ValueError("JAR failed its ZIP integrity check")
        metadata = json.loads(jar.read("fabric.mod.json"))
        if metadata["id"] != "overpoweredmobs" or metadata["version"] != version:
            raise ValueError("JAR mod identity or version does not match this build")
        if metadata["depends"]["minecraft"] != properties["minecraft_dependency"]:
            raise ValueError("JAR Minecraft dependency does not match gradle.properties")
        mixins = json.loads(jar.read("overpoweredmobs.mixins.json"))
        classes = ["com/overpoweredmobs/OverpoweredMobs.class", "com/overpoweredmobs/OverpoweredMobsClient.class"]
        classes += [mixins["package"].replace(".", "/") + "/" + mixin.replace(".", "/") + ".class"
                    for mixin in mixins.get("mixins", []) + mixins.get("client", [])]
        missing = [entry for entry in classes if entry not in jar.namelist()]
        if missing:
            raise ValueError(f"Missing mod classes: {missing}")
    if "GITHUB_OUTPUT" in os.environ:
        with open(os.environ["GITHUB_OUTPUT"], "a") as output:
            output.write(f"jar={jars[0]}\n")
    print(f"Verified {jars[0]} (Fabric mod version {version})")


if __name__ == "__main__":
    main()
