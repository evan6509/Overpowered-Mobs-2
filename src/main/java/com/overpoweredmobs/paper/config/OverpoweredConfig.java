package com.overpoweredmobs.paper.config;

import com.overpoweredmobs.paper.OverpoweredMobsPlugin;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class OverpoweredConfig {
    private final OverpoweredMobsPlugin plugin;
    private final Map<String, MobConfig> mobs = new LinkedHashMap<>();
    private final Map<String, Double> dimensions = new LinkedHashMap<>();
    private final List<CavalryEntry> cavalry = new ArrayList<>();
    private MobConfig defaults;

    private boolean enableGear;
    private boolean enableCavalry;
    private boolean enablePinata;
    private boolean testMode;
    private boolean enableBossBar;
    private double bossBarRange;
    private boolean enableMobNames;
    private boolean enableAlertSound;
    private double chargedCreeperChance;
    private boolean enableAggro;
    private double aggroFollowRange;
    private boolean enableDistanceSpeed;
    private double aggroFarSpeed;
    private double aggroCloseSpeed;
    private double aggroSlowRange;
    private double rangedAttackSpeedMultiplier;
    private double ghastExplosionMultiplier;
    private double piglinBruteGearChance;
    private double silverfishSpeedMultiplier;
    private double shulkerLevitationDurationMultiplier;
    private boolean enableEvilBunnies;
    private boolean enablePiglinHive;
    private double piglinHiveChance;
    private double piglinHiveRange;
    private boolean enableStrongholdMobs;
    private int strongholdMobCount;
    private boolean enableAngryWolves;
    private boolean enableWaterEndermen;
    private double spawnChance;
    private double hordeSpeedMultiplier;
    private double hordeFollowRangeMultiplier;
    private double zombiePinataChance;
    private int zombiePinataCount;

    private OverpoweredConfig(OverpoweredMobsPlugin plugin) {
        this.plugin = plugin;
        read(plugin.getConfig());
    }

    public static OverpoweredConfig load(OverpoweredMobsPlugin plugin) {
        return new OverpoweredConfig(plugin);
    }

    private void read(FileConfiguration c) {
        enableGear = c.getBoolean("enable-gear", true);
        enableCavalry = c.getBoolean("enable-cavalry", true);
        enablePinata = c.getBoolean("enable-pinata", true);
        testMode = c.getBoolean("test-mode", false);
        enableBossBar = c.getBoolean("enable-boss-bar", true);
        bossBarRange = range(c.getDouble("boss-bar-range", 32.0), 32.0);
        enableMobNames = c.getBoolean("enable-mob-names", true);
        enableAlertSound = c.getBoolean("enable-alert-sound", true);
        chargedCreeperChance = chance(c.getDouble("charged-creeper-chance", 1.0));
        enableAggro = c.getBoolean("enable-aggro", true);
        aggroFollowRange = range(c.getDouble("aggro-follow-range", 128.0), 128.0);
        enableDistanceSpeed = c.getBoolean("enable-distance-speed", true);
        aggroFarSpeed = speed(c.getDouble("aggro-far-speed", 8.4), 8.4);
        aggroCloseSpeed = speed(c.getDouble("aggro-close-speed", 5.6), 5.6);
        aggroSlowRange = range(c.getDouble("aggro-slow-range", 10.0), 10.0);
        rangedAttackSpeedMultiplier = multiplier(c.getDouble("ranged-attack-speed-multiplier", 2.0));
        ghastExplosionMultiplier = multiplier(c.getDouble("ghast-explosion-multiplier", 3.0));
        piglinBruteGearChance = chance(c.getDouble("piglin-brute-gear-chance", 0.5));
        silverfishSpeedMultiplier = multiplier(c.getDouble("silverfish-speed-multiplier", 1.0));
        shulkerLevitationDurationMultiplier = multiplier(c.getDouble("shulker-levitation-duration-multiplier", 2.0));
        enableEvilBunnies = c.getBoolean("enable-evil-bunnies", true);
        enablePiglinHive = c.getBoolean("enable-piglin-hive", true);
        piglinHiveChance = chance(c.getDouble("piglin-hive-chance", 0.1));
        piglinHiveRange = range(c.getDouble("piglin-hive-range", 32.0), 32.0);
        enableStrongholdMobs = c.getBoolean("enable-stronghold-mobs", true);
        strongholdMobCount = count(c.getInt("stronghold-mob-count", 8));
        enableAngryWolves = c.getBoolean("enable-angry-wolves", true);
        enableWaterEndermen = c.getBoolean("enable-water-endermen", true);
        spawnChance = chance(c.getDouble("spawn-chance", 0.05));
        hordeSpeedMultiplier = multiplier(c.getDouble("horde-speed-multiplier", 1.0));
        hordeFollowRangeMultiplier = multiplier(c.getDouble("horde-follow-range-multiplier", 3.0));
        zombiePinataChance = chance(c.getDouble("zombie-pinata-chance", 0.01));
        zombiePinataCount = count(c.getInt("zombie-pinata-count", 2));

        defaults = readMob(c.getConfigurationSection("defaults"));
        readMap(c.getConfigurationSection("mobs"));

        ConfigurationSection dimensionSection = c.getConfigurationSection("dimensions");
        if (dimensionSection != null) {
            for (String key : dimensionSection.getKeys(false)) {
                dimensions.put(key, multiplier(dimensionSection.getDouble(key, 1.0)));
            }
        }

        List<Map<?, ?>> entries = c.getMapList("cavalry");
        for (Map<?, ?> entry : entries) {
            Object rider = entry.get("rider");
            Object mount = entry.get("mount");
            if (rider != null && mount != null) {
                cavalry.add(new CavalryEntry(
                    String.valueOf(rider), String.valueOf(mount),
                    chance(number(entry.get("chance"), 0.0)),
                    Boolean.parseBoolean(String.valueOf(entry.containsKey("baby") ? entry.get("baby") : false))
                ));
            }
        }
    }

    private void readMap(ConfigurationSection section) {
        if (section == null) return;
        for (String key : section.getKeys(false)) {
            ConfigurationSection child = section.getConfigurationSection(key);
            if (child != null) mobs.put(key, readMob(child));
        }
    }

    private MobConfig readMob(ConfigurationSection section) {
        MobConfig mob = new MobConfig();
        if (section == null) return mob;
        mob.health = multiplier(section.getDouble("health", mob.health));
        mob.damage = multiplier(section.getDouble("damage", mob.damage));
        mob.speed = multiplier(section.getDouble("speed", mob.speed));
        mob.armor = multiplier(section.getDouble("armor", mob.armor));
        mob.followRange = multiplier(section.getDouble("follow-range", mob.followRange));
        mob.xp = multiplier(section.getDouble("xp", mob.xp));
        mob.spawnChance = section.contains("spawn-chance") ? chance(section.getDouble("spawn-chance")) : -1.0;
        mob.weapon = section.getString("weapon");
        ConfigurationSection enchantments = section.getConfigurationSection("weapon-enchantments");
        if (enchantments != null) {
            for (String key : enchantments.getKeys(false)) {
                mob.weaponEnchantments.put(key, Math.max(1, enchantments.getInt(key)));
            }
        }
        return mob;
    }

    public void save() {
        FileConfiguration c = plugin.getConfig();
        c.set("test-mode", testMode);
        writeMob(c.createSection("defaults"), defaults);
        c.set("mobs", null);
        ConfigurationSection mobsSection = c.createSection("mobs");
        for (Map.Entry<String, MobConfig> entry : mobs.entrySet()) {
            writeMob(mobsSection.createSection(entry.getKey()), entry.getValue());
        }
        plugin.saveConfig();
    }

    private void writeMob(ConfigurationSection section, MobConfig mob) {
        section.set("health", mob.health);
        section.set("damage", mob.damage);
        section.set("speed", mob.speed);
        section.set("armor", mob.armor);
        section.set("follow-range", mob.followRange);
        section.set("xp", mob.xp);
        if (mob.spawnChance >= 0) section.set("spawn-chance", mob.spawnChance);
        if (mob.weapon != null) section.set("weapon", mob.weapon);
        if (!mob.weaponEnchantments.isEmpty()) section.set("weapon-enchantments", mob.weaponEnchantments);
    }

    public boolean isEnableGear() { return enableGear; }
    public boolean isEnableCavalry() { return enableCavalry; }
    public boolean isEnablePinata() { return enablePinata; }
    public boolean isTestMode() { return testMode; }
    public void setTestMode(boolean value) { testMode = value; }
    public boolean isEnableBossBar() { return enableBossBar; }
    public double getBossBarRange() { return bossBarRange; }
    public boolean isEnableMobNames() { return enableMobNames; }
    public boolean isEnableAlertSound() { return enableAlertSound; }
    public double getChargedCreeperChance() { return chargedCreeperChance; }
    public boolean isEnableAggro() { return enableAggro; }
    public double getAggroFollowRange() { return aggroFollowRange; }
    public boolean isEnableDistanceSpeed() { return enableDistanceSpeed; }
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
    public double getDimensionMultiplier(String id) { return dimensions.getOrDefault(id, 1.0); }
    public MobConfig getDefaults() { return defaults; }
    public Map<String, MobConfig> getMobs() { return mobs; }
    public List<CavalryEntry> getCavalry() { return cavalry; }
    public double getSpawnChanceFor(String id) { return mobs.containsKey(id) && mobs.get(id).spawnChance >= 0 ? mobs.get(id).spawnChance : spawnChance; }
    public double getHordeSpeedMultiplier() { return hordeSpeedMultiplier; }
    public double getHordeFollowRangeMultiplier() { return hordeFollowRangeMultiplier; }
    public double getZombiePinataChance() { return zombiePinataChance; }
    public int getZombiePinataCount() { return zombiePinataCount; }
    public MobConfig getFor(String id) { return mobs.getOrDefault(id, defaults); }
    public void setFor(String id, MobConfig config) { mobs.put(id, config); }

    public static final class MobConfig {
        private double health = 2.0, damage = 2.0, speed = 1.0, armor = 2.0, followRange = 2.0, xp = 3.0;
        private double spawnChance = -1.0;
        private String weapon;
        private final Map<String, Integer> weaponEnchantments = new LinkedHashMap<>();

        public double get(String attr) {
            return switch (attr) {
                case "health" -> health; case "damage" -> damage; case "speed" -> speed;
                case "armor" -> armor; case "followRange" -> followRange; case "xp" -> xp;
                case "spawnchance" -> spawnChance; default -> 1.0;
            };
        }
        public void set(String attr, double value) {
            switch (attr) {
                case "health" -> health = value; case "damage" -> damage = value; case "speed" -> speed = value;
                case "armor" -> armor = value; case "followRange" -> followRange = value; case "xp" -> xp = value;
                case "spawnchance" -> spawnChance = value; default -> { }
            }
        }
        public double health() { return health; }
        public double damage() { return damage; }
        public double speed() { return speed; }
        public double armor() { return armor; }
        public double followRange() { return followRange; }
        public double xp() { return xp; }
        public String weapon() { return weapon; }
        public Map<String, Integer> weaponEnchantments() { return weaponEnchantments; }
        public MobConfig copy() {
            MobConfig result = new MobConfig(); result.health = health; result.damage = damage; result.speed = speed;
            result.armor = armor; result.followRange = followRange; result.xp = xp; result.spawnChance = spawnChance;
            result.weapon = weapon; result.weaponEnchantments.putAll(weaponEnchantments); return result;
        }
    }

    public static final class CavalryEntry {
        private final String rider;
        private final String mount;
        private final double chance;
        private final boolean baby;

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
    }

    private static double number(Object value, double fallback) { return value instanceof Number n ? n.doubleValue() : fallback; }
    private static double chance(double value) { return finite(value) ? Math.max(0.0, Math.min(1.0, value)) : 0.0; }
    private static double multiplier(double value) { return finite(value) ? Math.max(0.1, Math.min(100.0, value)) : 1.0; }
    private static double range(double value, double fallback) { return finite(value) ? Math.max(0.0, Math.min(1024.0, value)) : fallback; }
    private static double speed(double value, double fallback) { return finite(value) ? Math.max(0.0, Math.min(100.0, value)) : fallback; }
    private static int count(int value) { return Math.max(0, Math.min(100, value)); }
    private static boolean finite(double value) { return Double.isFinite(value); }
}
