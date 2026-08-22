package com.overpoweredmobs.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;
import com.overpoweredmobs.OverpoweredMobs;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.core.registries.BuiltInRegistries;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OverpoweredConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("overpoweredmobs.json");

    private boolean enableGear = true;
    private boolean enableCavalry = true;
    private boolean enablePinata = true;
    private boolean testMode = false;
    private boolean enableBossBar = true;
    private double bossBarRange = 32.0;
    private boolean enableMobNames = true;
    private boolean enableAlertSound = true;
    private double chargedCreeperChance = 1.0;
    private boolean enableAggro = true;
    private double aggroFollowRange = 128.0;
    private boolean enableDistanceSpeed = true;
    private double aggroFarSpeed = 8.4;
    private double aggroCloseSpeed = 5.6;
    private double aggroSlowRange = 10.0;
    private double rangedAttackSpeedMultiplier = 2.0;
    private double ghastExplosionMultiplier = 3.0;
    private double piglinBruteGearChance = 0.5;
    private double silverfishSpeedMultiplier = 1.0;
    private double shulkerLevitationDurationMultiplier = 2.0;
    private boolean enableEvilBunnies = true;
    private boolean enablePiglinHive = true;
    private double piglinHiveChance = 0.1;
    private double piglinHiveRange = 32.0;
    private boolean enableStrongholdMobs = true;
    private int strongholdMobCount = 8;
    private boolean enableAngryWolves = true;
    private boolean enableWaterEndermen = true;
    private Map<String, Double> dimensions = new HashMap<>();
    private Map<String, MobConfig> mobs = new HashMap<>(defaultMobOverrides());
    private MobConfig defaults = new MobConfig();
    private List<CavalryEntry> cavalry = defaultCavalry();
    private double spawnChance = 0.05;
    private double hordeSpeedMultiplier = 1.0;
    private double hordeFollowRangeMultiplier = 3.0;
    private boolean enableEndermanTeleportStrike = true;
    private int endermanTeleportCooldown = 100;
    private double endermanTeleportMinRange = 4.0;
    private double endermanTeleportMaxRange = 16.0;
    private boolean enableCreeperChainDetonation = true;
    private double creeperChainRadius = 12.0;
    private boolean enablePhantomPacks = true;
    private int phantomPackMinSize = 2;
    private int phantomPackMaxSize = 4;
    private boolean enableWardenSensorBoost = true;
    private double wardenSensorRange = 32.0;
    private boolean enableElytraBoost = true;
    private double elytraChance = 0.10;
    private int fireworkBoostInterval = 80;
    private boolean enableShieldGear = true;
    private double shieldChance = 0.25;
    private boolean enableTotemSecondLife = true;
    private double totemChance = 0.03;
    private boolean enableBloodMoon = true;
    private int bloodMoonIntervalNights = 7;
    private int bloodMoonDurationTicks = 12000;
    private double bloodMoonHordeChance = 0.90;
    private boolean enableEliteMobs = true;
    private double eliteChance = 0.05;
    private double eliteHealthMultiplier = 3.0;
    private double eliteDamageMultiplier = 2.0;
    private double eliteSpeedMultiplier = 1.25;
    private double eliteArmorMultiplier = 2.0;
    private double eliteFollowRangeMultiplier = 1.5;
    @SerializedName("zombiePi\u00F1ataChance")
    private double zombiePinataChance = 0.01;

    @SerializedName("zombiePi\u00F1ataCount")
    private int zombiePinataCount = 2;

    private static Map<String, MobConfig> defaultMobOverrides() {
        Map<String, MobConfig> map = new HashMap<>();
        MobConfig drowned = new MobConfig();
        drowned.weapon = "minecraft:trident";
        drowned.weaponEnchantments = new HashMap<>(Map.of("minecraft:impaling", 10));
        map.put("minecraft:drowned", drowned);

        MobConfig pillager = new MobConfig();
        pillager.spawnChance = 0.15;
        map.put("minecraft:pillager", pillager);

        MobConfig creeper = new MobConfig();
        creeper.spawnChance = 0.2;
        map.put("minecraft:creeper", creeper);

        return map;
    }

    private static List<CavalryEntry> defaultCavalry() {
        return List.of(
            new CavalryEntry("minecraft:zombie", "minecraft:chicken", 0.15, true),
            new CavalryEntry("minecraft:creeper", "minecraft:phantom", 0.03, false),
            new CavalryEntry("minecraft:wither_skeleton", "minecraft:ghast", 0.03, false),
            new CavalryEntry("minecraft:skeleton", "minecraft:skeleton_horse", 0.2, false),
            new CavalryEntry("minecraft:stray", "minecraft:skeleton_horse", 0.2, false),
            new CavalryEntry("minecraft:bogged", "minecraft:skeleton_horse", 0.2, false),
            new CavalryEntry("minecraft:parched", "minecraft:skeleton_horse", 0.2, false)
        );
    }

    public boolean isEnableGear() { return enableGear; }
    public boolean isEnableCavalry() { return enableCavalry; }
    public boolean isEnablePinata() { return enablePinata; }
    public boolean isTestMode() { return testMode; }
    public void setTestMode(boolean testMode) { this.testMode = testMode; }
    public boolean isEnableBossBar() { return enableBossBar; }
    public double getBossBarRange() { return bossBarRange; }
    public boolean isEnableMobNames() { return enableMobNames; }
    public boolean isEnableAlertSound() { return enableAlertSound; }
    public double getChargedCreeperChance() { return chargedCreeperChance; }
    public boolean isEnableDistanceSpeed() { return enableDistanceSpeed; }
    public boolean isEnableAggro() { return enableAggro; }
    public double getAggroFollowRange() { return aggroFollowRange; }
    public double getAggroFarSpeed() { return aggroFarSpeed; }
    public double getAggroCloseSpeed() { return aggroCloseSpeed; }
    public double getAggroSlowRange() { return aggroSlowRange; }
    public double getRangedAttackSpeedMultiplier() { return rangedAttackSpeedMultiplier; }
    public double getGhastExplosionMultiplier() { return ghastExplosionMultiplier; }
    public double getPiglinBruteGearChance() { return piglinBruteGearChance; }
    public double getSilverfishSpeedMultiplier() { return silverfishSpeedMultiplier; }
    public double getShulkerLevitationDurationMultiplier() { return shulkerLevitationDurationMultiplier; }
    public boolean isEnableEvilBunnies() { return enableEvilBunnies; }
    public boolean isEnablePiglinHive() { return enablePiglinHive; }
    public double getPiglinHiveChance() { return piglinHiveChance; }
    public double getPiglinHiveRange() { return piglinHiveRange; }
    public boolean isEnableStrongholdMobs() { return enableStrongholdMobs; }
    public int getStrongholdMobCount() { return strongholdMobCount; }
    public boolean isEnableAngryWolves() { return enableAngryWolves; }
    public boolean isEnableWaterEndermen() { return enableWaterEndermen; }
    public double getDimensionMultiplier(String dimensionId) { return dimensions.getOrDefault(dimensionId, 1.0); }

    public MobConfig getFor(EntityType<?> type) {
        Identifier key = BuiltInRegistries.ENTITY_TYPE.getKey(type);
        if (key != null) {
            MobConfig specific = mobs.get(key.toString());
            if (specific != null) return specific;
        }
        return defaults;
    }

    public double getSpawnChanceFor(EntityType<?> type) {
        Identifier key = BuiltInRegistries.ENTITY_TYPE.getKey(type);
        if (key != null) {
            MobConfig specific = mobs.get(key.toString());
            if (specific != null && specific.spawnChance >= 0) return specific.spawnChance;
        }
        return spawnChance;
    }

    public MobConfig getDefaults() { return defaults; }
    public Map<String, MobConfig> getMobs() { return mobs; }
    public List<CavalryEntry> getCavalry() { return cavalry; }
    public double getSpawnChance() { return spawnChance; }
    public double getHordeSpeedMultiplier() { return hordeSpeedMultiplier; }
    public double getHordeFollowRangeMultiplier() { return hordeFollowRangeMultiplier; }
    public boolean isEnableEndermanTeleportStrike() { return enableEndermanTeleportStrike; }
    public int getEndermanTeleportCooldown() { return endermanTeleportCooldown; }
    public double getEndermanTeleportMinRange() { return endermanTeleportMinRange; }
    public double getEndermanTeleportMaxRange() { return endermanTeleportMaxRange; }
    public boolean isEnableCreeperChainDetonation() { return enableCreeperChainDetonation; }
    public double getCreeperChainRadius() { return creeperChainRadius; }
    public boolean isEnablePhantomPacks() { return enablePhantomPacks; }
    public int getPhantomPackMinSize() { return phantomPackMinSize; }
    public int getPhantomPackMaxSize() { return phantomPackMaxSize; }
    public boolean isEnableWardenSensorBoost() { return enableWardenSensorBoost; }
    public double getWardenSensorRange() { return wardenSensorRange; }
    public boolean isEnableElytraBoost() { return enableElytraBoost; }
    public double getElytraChance() { return elytraChance; }
    public int getFireworkBoostInterval() { return fireworkBoostInterval; }
    public boolean isEnableShieldGear() { return enableShieldGear; }
    public double getShieldChance() { return shieldChance; }
    public boolean isEnableTotemSecondLife() { return enableTotemSecondLife; }
    public double getTotemChance() { return totemChance; }
    public boolean isEnableBloodMoon() { return enableBloodMoon; }
    public int getBloodMoonIntervalNights() { return bloodMoonIntervalNights; }
    public int getBloodMoonDurationTicks() { return bloodMoonDurationTicks; }
    public double getBloodMoonHordeChance() { return bloodMoonHordeChance; }
    public boolean isEnableEliteMobs() { return enableEliteMobs; }
    public double getEliteChance() { return eliteChance; }
    public double getEliteHealthMultiplier() { return eliteHealthMultiplier; }
    public double getEliteDamageMultiplier() { return eliteDamageMultiplier; }
    public double getEliteSpeedMultiplier() { return eliteSpeedMultiplier; }
    public double getEliteArmorMultiplier() { return eliteArmorMultiplier; }
    public double getEliteFollowRangeMultiplier() { return eliteFollowRangeMultiplier; }
    public double getZombiePinataChance() { return zombiePinataChance; }
    public int getZombiePinataCount() { return zombiePinataCount; }

    public void setFor(EntityType<?> type, MobConfig config) {
        Identifier key = BuiltInRegistries.ENTITY_TYPE.getKey(type);
        if (key != null) {
            mobs.put(key.toString(), config);
        }
    }

    public void setDefault(String attr, double value) {
        defaults.set(attr, value);
    }

    public static void reset() {
        try {
            Files.deleteIfExists(CONFIG_PATH);
        } catch (IOException e) {
            OverpoweredMobs.LOGGER.error("Failed to delete config", e);
        }
        OverpoweredMobs.loadConfig();
    }

    public static OverpoweredConfig load() {
        if (CONFIG_PATH.toFile().exists()) {
            try (FileReader reader = new FileReader(CONFIG_PATH.toFile())) {
                Type type = new TypeToken<OverpoweredConfig>(){}.getType();
                OverpoweredConfig config = GSON.fromJson(reader, type);
                if (config != null) {
                    config.normalize();
                    return config;
                }
            } catch (IOException | JsonParseException | IllegalStateException e) {
                OverpoweredMobs.LOGGER.error("Failed to load config", e);
            }
        }
        OverpoweredConfig config = new OverpoweredConfig();
        config.normalize();
        config.save();
        return config;
    }

    public void save() {
        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            try (FileWriter writer = new FileWriter(CONFIG_PATH.toFile())) {
                GSON.toJson(this, writer);
            }
        } catch (IOException e) {
            OverpoweredMobs.LOGGER.error("Failed to save config", e);
        }
    }

    private void normalize() {
        if (dimensions == null) dimensions = new HashMap<>();
        if (mobs == null) mobs = new HashMap<>(defaultMobOverrides());
        if (defaults == null) defaults = new MobConfig();
        if (cavalry == null) cavalry = defaultCavalry();

        defaults.clamp();
        mobs.entrySet().removeIf(entry -> entry.getKey() == null || entry.getValue() == null);
        for (MobConfig mobConfig : mobs.values()) {
            mobConfig.clamp();
        }

        dimensions.entrySet().removeIf(entry -> entry.getKey() == null || entry.getValue() == null);
        dimensions.replaceAll((key, value) -> clampMultiplier(value));

        cavalry = new ArrayList<>(cavalry);
        cavalry.removeIf(entry -> entry == null || entry.rider() == null || entry.mount() == null);
        for (CavalryEntry entry : cavalry) {
            entry.clamp();
        }

        bossBarRange = clampRange(bossBarRange, 32.0);
        chargedCreeperChance = clampChance(chargedCreeperChance);
        aggroFollowRange = clampRange(aggroFollowRange, 128.0);
        aggroFarSpeed = clampSpeed(aggroFarSpeed, 8.4);
        aggroCloseSpeed = clampSpeed(aggroCloseSpeed, 5.6);
        aggroSlowRange = clampRange(aggroSlowRange, 10.0);
        rangedAttackSpeedMultiplier = clampMultiplier(rangedAttackSpeedMultiplier);
        ghastExplosionMultiplier = clampMultiplier(ghastExplosionMultiplier);
        piglinBruteGearChance = clampChance(piglinBruteGearChance);
        silverfishSpeedMultiplier = clampMultiplier(silverfishSpeedMultiplier);
        shulkerLevitationDurationMultiplier = clampMultiplier(shulkerLevitationDurationMultiplier);
        piglinHiveChance = clampChance(piglinHiveChance);
        piglinHiveRange = clampRange(piglinHiveRange, 32.0);
        strongholdMobCount = clampCount(strongholdMobCount);
        spawnChance = clampChance(spawnChance);
        hordeSpeedMultiplier = clampMultiplier(hordeSpeedMultiplier);
        hordeFollowRangeMultiplier = clampMultiplier(hordeFollowRangeMultiplier);
        endermanTeleportCooldown = clampTicks(endermanTeleportCooldown, 100, 2400);
        endermanTeleportMinRange = clampRange(endermanTeleportMinRange, 4.0);
        endermanTeleportMaxRange = clampRange(endermanTeleportMaxRange, 16.0);
        if (endermanTeleportMaxRange < endermanTeleportMinRange) {
            endermanTeleportMaxRange = endermanTeleportMinRange;
        }
        creeperChainRadius = clampRange(creeperChainRadius, 12.0);
        phantomPackMinSize = clampCount(phantomPackMinSize);
        phantomPackMaxSize = clampCount(phantomPackMaxSize);
        phantomPackMinSize = Math.max(1, phantomPackMinSize);
        phantomPackMaxSize = Math.max(phantomPackMinSize, phantomPackMaxSize);
        wardenSensorRange = clampRange(wardenSensorRange, 32.0);
        elytraChance = clampChance(elytraChance);
        fireworkBoostInterval = clampTicks(fireworkBoostInterval, 80, 2400);
        shieldChance = clampChance(shieldChance);
        totemChance = clampChance(totemChance);
        bloodMoonIntervalNights = clampNights(bloodMoonIntervalNights);
        bloodMoonDurationTicks = clampTicks(bloodMoonDurationTicks, 12000, 24000);
        bloodMoonHordeChance = clampChance(bloodMoonHordeChance);
        eliteChance = clampChance(eliteChance);
        eliteHealthMultiplier = clampMultiplier(eliteHealthMultiplier);
        eliteDamageMultiplier = clampMultiplier(eliteDamageMultiplier);
        eliteSpeedMultiplier = clampMultiplier(eliteSpeedMultiplier);
        eliteArmorMultiplier = clampMultiplier(eliteArmorMultiplier);
        eliteFollowRangeMultiplier = clampMultiplier(eliteFollowRangeMultiplier);
        zombiePinataChance = clampChance(zombiePinataChance);
        zombiePinataCount = clampCount(zombiePinataCount);
    }

    private static double clampChance(double value) {
        if (!Double.isFinite(value)) return 0.0;
        return Math.max(0.0, Math.min(1.0, value));
    }

    private static double clampMultiplier(double value) {
        if (!Double.isFinite(value)) return 1.0;
        return Math.max(0.1, Math.min(100.0, value));
    }

    private static double clampRange(double value, double fallback) {
        if (!Double.isFinite(value)) return fallback;
        return Math.max(0.0, Math.min(1024.0, value));
    }

    private static double clampSpeed(double value, double fallback) {
        if (!Double.isFinite(value)) return fallback;
        return Math.max(0.0, Math.min(100.0, value));
    }

    private static int clampCount(int value) {
        return Math.max(0, Math.min(100, value));
    }

    private static int clampTicks(int value, int fallback, int max) {
        if (value < 1) return fallback;
        return Math.min(max, value);
    }

    private static int clampNights(int value) {
        return Math.max(1, Math.min(1000, value));
    }

    public static class MobConfig {
        private double healthMultiplier = 2.0;
        private double damageMultiplier = 2.0;
        private double speedMultiplier = 1.0;
        private double armorMultiplier = 2.0;
        private double followRangeMultiplier = 2.0;
        private double xpMultiplier = 3.0;
        private double spawnChance = -1.0;
        private String weapon;
        private Map<String, Integer> weaponEnchantments;

        public double healthMultiplier() { return healthMultiplier; }
        public double damageMultiplier() { return damageMultiplier; }
        public double speedMultiplier() { return speedMultiplier; }
        public double armorMultiplier() { return armorMultiplier; }
        public double followRangeMultiplier() { return followRangeMultiplier; }
        public double xpMultiplier() { return xpMultiplier; }
        public String weapon() { return weapon; }
        public Map<String, Integer> weaponEnchantments() { return weaponEnchantments; }

        public void setHealthMultiplier(double v) { healthMultiplier = v; }
        public void setDamageMultiplier(double v) { damageMultiplier = v; }
        public void setSpeedMultiplier(double v) { speedMultiplier = v; }
        public void setArmorMultiplier(double v) { armorMultiplier = v; }
        public void setFollowRangeMultiplier(double v) { followRangeMultiplier = v; }
        public void setXpMultiplier(double v) { xpMultiplier = v; }

        public MobConfig copy() {
            MobConfig copy = new MobConfig();
            copy.healthMultiplier = healthMultiplier;
            copy.damageMultiplier = damageMultiplier;
            copy.speedMultiplier = speedMultiplier;
            copy.armorMultiplier = armorMultiplier;
            copy.followRangeMultiplier = followRangeMultiplier;
            copy.xpMultiplier = xpMultiplier;
            copy.spawnChance = spawnChance;
            copy.weapon = weapon;
            copy.weaponEnchantments = weaponEnchantments == null
                ? null
                : new HashMap<>(weaponEnchantments);
            return copy;
        }

        public void clamp() {
            healthMultiplier = clampMultiplier(healthMultiplier);
            damageMultiplier = clampMultiplier(damageMultiplier);
            speedMultiplier = clampMultiplier(speedMultiplier);
            armorMultiplier = clampMultiplier(armorMultiplier);
            followRangeMultiplier = clampMultiplier(followRangeMultiplier);
            xpMultiplier = clampMultiplier(xpMultiplier);
            if (!Double.isFinite(spawnChance)) {
                spawnChance = -1.0;
            } else if (spawnChance >= 0.0) {
                spawnChance = clampChance(spawnChance);
            }
        }

        public void set(String attr, double value) {
            switch (attr) {
                case "health" -> healthMultiplier = value;
                case "damage" -> damageMultiplier = value;
                case "speed" -> speedMultiplier = value;
                case "armor" -> armorMultiplier = value;
                case "followRange" -> followRangeMultiplier = value;
                case "xp" -> xpMultiplier = value;
                case "spawnchance" -> spawnChance = value;
            }
        }

        public double get(String attr) {
            return switch (attr) {
                case "health" -> healthMultiplier;
                case "damage" -> damageMultiplier;
                case "speed" -> speedMultiplier;
                case "armor" -> armorMultiplier;
                case "followRange" -> followRangeMultiplier;
                case "xp" -> xpMultiplier;
                case "spawnchance" -> spawnChance;
                default -> 1.0;
            };
        }
    }

    public static class CavalryEntry {
        private String rider;
        private String mount;
        private double chance;
        private boolean baby;

        public CavalryEntry() {}

        public CavalryEntry(String rider, String mount, double chance, boolean baby) {
            this.rider = rider;
            this.mount = mount;
            this.chance = chance;
            this.baby = baby;
        }

        public String rider() { return rider; }
        public String mount() { return mount; }
        public double chance() { return chance; }
        public boolean baby() { return baby; }

        private void clamp() {
            chance = clampChance(chance);
        }
    }
}
