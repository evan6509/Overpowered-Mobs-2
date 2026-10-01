# Overpowered Mobs — Fabric Mod

## Identity
- Fabric is the only supported platform. The experimental Paper port is discontinued and its branch has been removed; do not develop, build, test, or maintain it.
- Mod ID `overpoweredmobs` · package `com.overpoweredmobs` · local version `0.3.2` (`mod_version` in `gradle.properties`)
- Java 25 · Fabric Loader 0.19.3+ · Fabric API 0.154.2+ · Minecraft 26.2
- Loom 1.17.14 — **no remapping** (unobfuscated MC). Use `implementation`/`compileOnly`, NEVER `modImplementation`.
- Client entrypoint: `OverpoweredMobsClient` (no-op, just logs).

### Commands
- `./gradlew build` — output: `build/libs/overpoweredmobs-<version>+mc26.2-b<build_number>.jar`
- Local deployment is explicit: `./gradlew deployToTestServer -PtestServerModsDir=<path>`
- Git commit count embedded in build number; commit before building
- **Zero tests** — no test task, no test dependencies

## MC 26.2 Quirks
- `ResourceLocation` → `Identifier` (`.tryParse()`, `.toString()`)
- `MobSpawnType` → `EntitySpawnReason`
- Attributes (`MAX_HEALTH`, etc.) are `Holder<Attribute>` — pass directly to `getAttribute(Holder<Attribute>)`
- No `Entity.getPersistentData()` → scoreboard entity tags via `addTag()` / `entityTags()`
- `Registry.get(Identifier)` returns `Optional<Reference<T>>` → use `Registry.getValue(Identifier)` for direct `T`
- Enchantments via data components: `ItemEnchantments.Mutable` + `DataComponents.ENCHANTMENTS`
- No `setPowered(boolean)` on `Creeper` → `CreeperHelper` via Mixin `@Accessor("DATA_IS_POWERED")`
- `Zombie` → `.monster.zombie.Zombie`, `Skeleton` → `.monster.skeleton.*`
- `EntityType.CREEPER` etc. removed → `BuiltInRegistries.ENTITY_TYPE.getValue(Identifier.tryParse("minecraft:creeper"))`

## Architecture
- Entry: `OverpoweredMobs.onInitialize()` → init logger, load config, register `/opm` commands, register `ServerLivingEntityEvents.AFTER_DEATH` (piñata), register `ServerTickEvents.START_LEVEL_TICK` (boss bar), register disconnect handler
- Boosting/gear/cavalry: `MobAttributesMixin` injects at `Mob.finalizeSpawn` RETURN, `MobCategory.MONSTER` only. Horde mobs (failed spawnChance roll) skip boosting + gear + cavalry, get `opm_horde` tag.
- Gear applied deferred: `level.getServer().execute(() -> EquipmentHelper.equipOPGear(...))` — prevents subclass overwriting
- Creeper charging: `CreeperHelper.setPowered()` via `CreeperAccessor` (`@Accessor("DATA_IS_POWERED")`)
- Tags: `opm_boosted`, `opm_piñata`, `opm_cavalry_mount`, `opm_horde`
- Config: `config/overpoweredmobs.json` auto-generated on first launch
- `dropsMultiplier` config field serialized but **unused** — drops are dynamic (1.2× no armor, 3× with armor via `DropMultiplierMixin`)
- EquipmentHelper reads `weapon`/`weaponEnchantments` from per-mob config before bow (ranged) / sword (melee) fallback
- Custom weapon enchantments resolved via `RegistryAccess.lookupOrThrow(Registries.ENCHANTMENT)` with `ResourceKey`
- `/opm set` attrs: `health`/`damage`/`speed`/`armor`/`followRange`/`xp`/`drops`/`spawnchance`; value clamped `[0.1, 100.0]`
- Debug log: `logs/overpoweredmobs.log` via `OverpoweredMobsLogger` (`BufferedWriter`, flush per write)

### Mixins (14 + 1 accessor) — all in `com.overpoweredmobs.mixin`
| # | File | Targets |
|---|------|---------|
| 1 | `MobAttributesMixin` | `Mob.finalizeSpawn` RETURN — boost, charge, gear, cavalry, distance speed |
| 2 | `ExperienceMultiplierMixin` | `LivingEntity.getExperienceReward` RETURN |
| 3 | `DropMultiplierMixin` | `LivingEntity.dropAllDeathLoot` RETURN — dynamic drop multiplier |
| 4 | `PinataDespawnMixin` | `Mob.tick` HEAD — remove piñata babies after 600 ticks |
| 5 | `LargeFireballMixin` | `LargeFireball` — scaled explosion power for boosted ghasts |
| 6 | `GhastChargeMixin` | `Ghast$GhastShootFireballGoal` — reduced cooldown |
| 7 | `PiglinHiveMixin` | `ZombifiedPiglin` — periodic anger propagation |
| 8 | `ShulkerBulletMixin` | Doubled levitation duration |
| 9 | `RabbitMixin` | Force killer bunny variant |
| 10 | `StrongholdMobTriggerMixin` | `follow_ender_eye` advancement — spawn mob wave |
| 11 | `GiantAIMixin` | Add attack/look/wander goals to giants |
| 12 | `WolfMixin` | Wolves spawn permanently angry |
| 13 | `EndermanMixin` | Endermen immune to water/rain damage |
| 14 | `CreeperAccessor` | `@Accessor("DATA_IS_POWERED")` |
| — | `LargeFireballAccessor` | `@Accessor("explosionPower")` |

Mixin compatibility level declared as `JAVA_21` in `overpoweredmobs.mixins.json` despite Java 25 runtime.

## Code Style
- No records — plain classes with explicit getters. No generated code, no annotation processing, no migrations.
- Vanilla items + vanilla enchantments only (full client compatibility).

## Committing
- `AGENTS.md` is gitignored — do not reference in commit messages.

## Git Workflow
- Default working branch: `codex/development`. Make routine changes and commits on this branch.
- Keep `main` as the reviewed integration branch. Do not commit or push directly to `main`.
- Push development commits when requested. When ready for review, open a PR with base `main` and head `codex/development`.
- Review the PR diff and resolve findings before merging. Merge only when the user explicitly requests it.
- Keep the development branch after merging. Prefer a merge commit for this long-lived branch; synchronize it with `origin/main` before starting the next batch of work.
- Inspect the current branch, working tree, and divergence before switching or syncing. Preserve local changes and commits; do not reset, force-push, or discard work without explicit authorization.

## Releases
- `.github/workflows/release.yml` builds development pushes and PRs; only successful pushes to `main` publish a GitHub release. Manual runs build only.
- CI selects the next patch version from GitHub release history and passes `OPM_VERSION` to Gradle for both the JAR name and `fabric.mod.json`. `mod_version` remains the local default and can request a higher release version.
- Publication verifies the built Fabric JAR, targets the exact commit, and verifies the uploaded JAR before publishing. Re-running an already published commit keeps its original release.
- Optional authored notes: `.github/release-notes/v<version>.md`. Otherwise, notes include a readable heading and commit bullets.
