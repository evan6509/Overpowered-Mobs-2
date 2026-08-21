# Overpowered Mobs

Overpowered Mobs is a Paper plugin that turns hostile mobs into formidable enemies with boosted stats, enchanted gear, charged creepers, cavalry, boss bars, and special mob behaviors.

## Compatibility

- Paper 26.2, API build 112
- Java 25
- No client mod is required

## Features

- Configurable health, damage, speed, armor, follow range, XP, dimension multipliers, and spawn odds
- Horde mode for mobs that fail the boost roll
- Netherite or piglin gold armor with unsafe high-level vanilla enchantments
- Per-mob custom weapons and weapon enchantments
- Charged creepers, cavalry mounts, skeleton horsemen, and zombie pinatas
- Boss bars, red mob name tags, alert sounds, distance-based pathfinding speed, and stronghold waves
- Evil rabbits, angry wolves, giant combat behavior, piglin hive aggression, doubled shulker levitation, and water-resistant endermen
- Dynamic drops: 1.2× without armor and 3× with armor

## Installation

1. Build with `./gradlew build`.
2. Copy `build/libs/overpoweredmobs-*.jar` to the Paper server’s `plugins/` directory.
3. Start Paper once to generate `plugins/OverpoweredMobs/config.yml`.

The plugin is built against `io.papermc.paper:paper-api:26.2.build.112-stable`.

## Configuration

The generated `config.yml` contains the same settings as the former Fabric configuration, using YAML paths such as:

```yaml
defaults:
  health: 2.0
  damage: 2.0
  speed: 1.0
  armor: 2.0
  follow-range: 2.0
  xp: 3.0
mobs:
  minecraft:drowned:
    weapon: minecraft:trident
    weapon-enchantments:
      minecraft:impaling: 10
```

## Commands

All commands require the `overpoweredmobs.admin` permission, granted to operators by default.

- `/opm status`
- `/opm set <mob> <health|damage|speed|armor|followRange|xp|spawnchance> <value>`
- `/opm reload`
- `/opm reset`
- `/opm test`
- `/opm cavalry <rider> <mount>`

## Port notes

Fabric mixins were replaced with Paper events and scheduled server tasks. Paper’s public API does not expose every vanilla goal-internals hook, so ghast fire-rate acceleration is not reproduced as a direct goal patch; explosion scaling, movement/pathfinding, giant combat, and the other listed mechanics are implemented through the public plugin API.
