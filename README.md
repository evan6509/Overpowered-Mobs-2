# Overpowered Mobs

A Fabric mod for Minecraft 26.2 that turns hostile mobs into formidable enemies with boosted stats, OP enchanted gear, charged creepers, cavalry mounts, boss bars, and more — all using only vanilla items for full client compatibility.

**Current version: 0.3.2**

## Features

### Core
- **Mob Boosting** — Multiplied health, damage, speed, armor, and follow range for hostile mobs
- **Horde Mode** — Mobs that fail the spawn roll get 3× follow range (configurable speed bonus)
- **Dynamic Drop Multiplier** — 1.2× drops without armor, 3× with armor
- **Dimension Multipliers** — Per-dimension stat scaling (e.g., nerf overworld, buff nether)

### Gear & Combat
- **OP Enchanted Gear** — Equippable mobs get full netherite armor (Protection X) + OP weapons — gear never drops
- **Custom Weapon Config** — Per-mob weapon + enchantment overrides (Drowned defaults to Trident + Impaling X)
- **Piglin Gold Armor** — Piglins get gold armor instead of netherite; brutes have 50% gear chance
- **Ranged Attack Speed** — Skeletons, strays, bogged, and parched shoot faster

### Mobs
- **Charged Creepers** — Configurable chance (default 100%) for creepers to spawn visually charged
- **Cavalry Mounts** — Hostile mobs can spawn riding mounts (zombie on chicken, creeper on phantom, wither skeleton on ghast, skeleton types on skeleton horses)
- **Skeleton Horsemen** — Skeletons, strays, bogged, and parched can spawn on skeleton horses (20%)
- **Zombie Piñata** — Zombies explode into baby zombies on player kill (1% base, 75% when densely packed)
- **Evil Bunnies** — All rabbits spawn as the killer variant
- **Giant AI** — Giants now have attack, look, wander, and random look goals
- **Silverfish Speed** — Configurable extra speed for silverfish (default 1.0×, disabled)
- **Shulker Levitation** — Doubled levitation duration from shulker bullets
- **Parched** — Desert skeleton variant gets a bow like other skeletons

### Aggression & Awareness
- **Distance Speed System** — Boosted mobs move faster the farther they are from the player (5.6 m/s close, 8.4 m/s far, configurable)
- **Alert Sound** — Wither spawn sound plays when a boosted equippable mob spawns near a player
- **Boss Bar** — Per-player boss bar tracking the nearest boosted mob's HP (color-coded)
- **Mob Name Tags** — Boosted mobs display a red `⚡ Overpowered {MobName}` name tag
- **Zombified Piglin Hivemind** — Zombified piglins periodically anger nearby piglins (10% chance per second)
- **Angry Wolves** — All wolves spawn permanently angry at the nearest player
- **Water-resistant Endermen** — Endermen no longer take damage from water or rain

### Events
- **Stronghold Mob Wave** — Entering a stronghold (follow_ender_eye advancement) spawns a wave of boosted mobs

### Commands
- **Test Mode** — Forces configured random mechanics to 100% for testing, including mob boosting, charged creepers, cavalry, piñatas, piglin brute gear, and piglin hive alerts

## Config

`config/overpoweredmobs.json` is auto-generated on first launch.

### Default multipliers

| Attribute       | Default |
|-----------------|---------|
| Health          | 2.0×    |
| Damage          | 2.0×    |
| Speed           | 1.0×    |
| Armor           | 2.0×    |
| Follow Range    | 2.0×    |
| XP              | 3.0×    |

### Full config structure

```json
{
  "enableGear": true,
  "enableCavalry": true,
  "enablePinata": true,
  "enableBossBar": true,
  "enableMobNames": true,
  "enableAlertSound": true,
  "enableAggro": true,
  "enableDistanceSpeed": true,
  "aggroCloseSpeed": 5.6,
  "aggroFarSpeed": 8.4,
  "aggroSlowRange": 10.0,
  "enableEvilBunnies": true,
  "enablePiglinHive": true,
  "enableStrongholdMobs": true,
  "enableAngryWolves": true,
  "enableWaterEndermen": true,
  "chargedCreeperChance": 1.0,
  "spawnChance": 0.05,
  "defaults": { ... },
  "mobs": {
    "minecraft:pillager": { "spawnChance": 0.15 },
    "minecraft:creeper": { "spawnChance": 0.2 },
    "minecraft:drowned": {
      "weapon": "minecraft:trident",
      "weaponEnchantments": { "minecraft:impaling": 10 }
    }
  },
  "dimensions": {
    "minecraft:the_nether": 0.5
  },
  "cavalry": [
    { "rider": "minecraft:zombie", "mount": "minecraft:chicken", "chance": 0.15, "baby": true },
    { "rider": "minecraft:creeper", "mount": "minecraft:phantom", "chance": 0.03, "baby": false },
    { "rider": "minecraft:wither_skeleton", "mount": "minecraft:ghast", "chance": 0.03, "baby": false },
    { "rider": "minecraft:skeleton", "mount": "minecraft:skeleton_horse", "chance": 0.2, "baby": false },
    { "rider": "minecraft:stray", "mount": "minecraft:skeleton_horse", "chance": 0.2, "baby": false },
    { "rider": "minecraft:bogged", "mount": "minecraft:skeleton_horse", "chance": 0.2, "baby": false },
    { "rider": "minecraft:parched", "mount": "minecraft:skeleton_horse", "chance": 0.2, "baby": false }
  ],
  "zombiePiñataChance": 0.01,
  "zombiePiñataCount": 2
}
```

## Commands

All `/opm` commands require **operator** permission.

| Command                                | Description                      |
|----------------------------------------|----------------------------------|
| `/opm status`                          | Show current multipliers         |
| `/opm set <mob> <attr> <value>`        | Set a multiplier                 |
| `/opm reload`                          | Reload config from disk          |
| `/opm reset`                           | Reset config to defaults         |
| `/opm test`                            | Toggle test mode (100% odds)     |
| `/opm cavalry <rider> <mount>`         | Spawn a rider on a mount for testing |

`<attr>` may be `health`, `damage`, `speed`, `armor`, `followRange`, `xp`, or `spawnchance`.

Drops remain dynamic: monster drops are multiplied by 1.2× without armor and 3× with armor.

## Build

```bash
./gradlew build
```

Output: `build/libs/overpoweredmobs-<version>+mc26.2-b<build_number>.jar`

The JAR includes a build number from the git commit count. Old JARs are automatically cleaned from the test server mods folder on each build.

## Requirements

- Minecraft 26.2
- Fabric Loader 0.19.3+
- Fabric API 0.154.2+
- Java 25
