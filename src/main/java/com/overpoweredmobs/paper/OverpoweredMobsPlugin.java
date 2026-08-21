package com.overpoweredmobs.paper;

import com.overpoweredmobs.paper.config.OverpoweredConfig;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Ghast;
import org.bukkit.entity.Giant;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.entity.Rabbit;
import org.bukkit.entity.Slime;
import org.bukkit.entity.Wolf;
import org.bukkit.entity.Zombie;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.ShulkerBullet;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.ExplosionPrimeEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerAdvancementDoneEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public final class OverpoweredMobsPlugin extends JavaPlugin implements Listener, CommandExecutor, TabCompleter {
    public static final String BOOSTED_TAG = "opm_boosted";
    public static final String PINATA_TAG = "opm_pinata";
    public static final String CAVALRY_MOUNT_TAG = "opm_cavalry_mount";
    public static final String HORDE_TAG = "opm_horde";

    private OverpoweredConfig config;
    private BossBarManager bossBars;
    private final Set<UUID> distanceTracked = new java.util.HashSet<>();
    private final Map<UUID, Integer> attackCooldowns = new HashMap<>();
    private boolean spawningMount;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        config = OverpoweredConfig.load(this);
        bossBars = new BossBarManager(this);
        Bukkit.getPluginManager().registerEvents(this, this);
        if (getCommand("opm") != null) {
            getCommand("opm").setExecutor(this);
            getCommand("opm").setTabCompleter(this);
        }
        Bukkit.getScheduler().runTaskTimer(this, this::tick, 1L, 5L);
        Bukkit.getScheduler().runTaskTimer(this, this::piglinHive, 20L, 20L);
        getLogger().info("Overpowered Mobs Paper port enabled for Paper 26.2.");
    }

    @Override
    public void onDisable() {
        if (bossBars != null) bossBars.clearAll();
    }

    public OverpoweredConfig getOpmConfig() { return config; }

    public void reloadOpmConfig() {
        reloadConfig();
        config = OverpoweredConfig.load(this);
    }

    public void resetOpmConfig() {
        saveResource("config.yml", true);
        reloadOpmConfig();
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        if (!(event.getEntity() instanceof Mob mob)) return;
        if (config.isEnableAngryWolves() && mob instanceof Wolf wolf) {
            wolf.setAngry(true);
            Player nearest = nearestPlayer(wolf, 64.0);
            if (nearest != null) wolf.setTarget(nearest);
        }
        if (config.isEnableEvilBunnies() && mob instanceof Rabbit rabbit) rabbit.setRabbitType(Rabbit.Type.THE_KILLER_BUNNY);
        if (!isHostile(mob)) return;
        if (spawningMount) { mob.addScoreboardTag(CAVALRY_MOUNT_TAG); return; }
        if (mob.getScoreboardTags().contains(CAVALRY_MOUNT_TAG) || mob.getScoreboardTags().contains(HORDE_TAG)) return;

        String id = key(mob.getType());
        double spawnChance = config.getSpawnChanceFor(id);
        if (!config.isTestMode() && Math.random() >= spawnChance) {
            applyHordeBuffs(mob);
            return;
        }

        if (mob instanceof Creeper creeper && (config.isTestMode() || Math.random() < config.getChargedCreeperChance())) {
            creeper.setPowered(true);
        }
        boost(mob);

        if (config.isEnableAlertSound() && EquipmentHelper.isEquippable(mob.getType()) && nearestPlayer(mob, config.getBossBarRange()) != null) {
            mob.getWorld().playSound(mob.getLocation(), Sound.ENTITY_WITHER_SPAWN, SoundCategory.HOSTILE, 1.0f, 1.0f);
        }

        Bukkit.getScheduler().runTask(this, () -> {
            if (!mob.isValid() || mob.isDead()) return;
            if (config.isEnableGear()) EquipmentHelper.equip(mob, config);
            if (config.isEnableCavalry()) spawnCavalry(mob);
        });
    }

    private void boost(Mob mob) {
        if (mob.getScoreboardTags().contains(BOOSTED_TAG)) return;
        OverpoweredConfig.MobConfig mobConfig = config.getFor(key(mob.getType()));
        double dimension = config.getDimensionMultiplier(mob.getWorld().getKey().toString());
        multiply(mob, Attribute.MAX_HEALTH, mobConfig.health() * dimension);
        multiply(mob, Attribute.ATTACK_DAMAGE, mobConfig.damage() * dimension);
        multiply(mob, Attribute.MOVEMENT_SPEED, mobConfig.speed() * dimension);
        multiply(mob, Attribute.ARMOR, mobConfig.armor() * dimension);
        multiply(mob, Attribute.FOLLOW_RANGE, mobConfig.followRange() * dimension);
        if (EquipmentHelper.isRanged(mob.getType())) multiply(mob, Attribute.ATTACK_SPEED, config.getRangedAttackSpeedMultiplier());
        if (mob.getType() == EntityType.SILVERFISH) multiply(mob, Attribute.MOVEMENT_SPEED, config.getSilverfishSpeedMultiplier());
        if (config.isEnableAggro()) setBase(mob, Attribute.FOLLOW_RANGE, config.getAggroFollowRange());
        mob.setHealth(mob.getAttribute(Attribute.MAX_HEALTH).getValue());
        mob.addScoreboardTag(BOOSTED_TAG);
        if (config.isEnableMobNames()) mob.setCustomName("§c⚡ Overpowered " + pretty(mob.getType().name()));
        mob.setCustomNameVisible(config.isEnableMobNames());
    }

    private void applyHordeBuffs(Mob mob) {
        multiply(mob, Attribute.MOVEMENT_SPEED, config.getHordeSpeedMultiplier());
        multiply(mob, Attribute.FOLLOW_RANGE, config.getHordeFollowRangeMultiplier());
        mob.addScoreboardTag(HORDE_TAG);
    }

    private void multiply(Mob mob, Attribute attribute, double multiplier) {
        if (multiplier == 1.0) return;
        AttributeInstance instance = mob.getAttribute(attribute);
        if (instance != null) instance.setBaseValue(instance.getBaseValue() * multiplier);
    }

    private void setBase(Mob mob, Attribute attribute, double value) {
        AttributeInstance instance = mob.getAttribute(attribute);
        if (instance != null) instance.setBaseValue(value);
    }

    private void spawnCavalry(Mob rider) {
        String riderId = key(rider.getType());
        for (OverpoweredConfig.CavalryEntry entry : config.getCavalry()) {
            if (!entry.rider().equals(riderId)) continue;
            if (!config.isTestMode() && Math.random() >= entry.chance()) return;
            EntityType mountType = entityType(entry.mount());
            if (mountType == null) return;
            spawningMount = true;
            Entity mountEntity;
            try { mountEntity = rider.getWorld().spawnEntity(rider.getLocation(), mountType); }
            finally { spawningMount = false; }
            if (!(mountEntity instanceof Mob mount)) return;
            mount.addScoreboardTag(CAVALRY_MOUNT_TAG);
            if (entry.baby() && rider instanceof Zombie zombie) zombie.setBaby(true);
            mount.addPassenger(rider);
            if (mount instanceof Mob) mount.setTarget(rider.getTarget());
            return;
        }
    }

    private void tick() {
        bossBars.tick();
        for (World world : Bukkit.getWorlds()) {
            for (LivingEntity entity : world.getLivingEntities()) {
                if (!(entity instanceof Mob mob)) continue;
                if (mob.getScoreboardTags().contains(PINATA_TAG) && mob.getTicksLived() > 600) mob.remove();
                if (!config.isEnableDistanceSpeed() || !mob.getScoreboardTags().contains(BOOSTED_TAG)) continue;
                Player player = nearestPlayer(mob, Math.max(config.getAggroSlowRange() + 10.0, followRange(mob)));
                if (player == null) continue;
                mob.setTarget(player);
                if (mob.getPathfinder() != null) {
                    double speed = mob.getLocation().distance(player.getLocation()) >= config.getAggroSlowRange()
                        ? config.getAggroFarSpeed() : config.getAggroCloseSpeed();
                    mob.getPathfinder().moveTo(player, speed);
                }
                if (mob instanceof Giant giant) giantCombat(giant, player);
            }
        }
    }

    private void giantCombat(Giant giant, Player player) {
        int cooldown = attackCooldowns.getOrDefault(giant.getUniqueId(), 0);
        if (cooldown > 0) { attackCooldowns.put(giant.getUniqueId(), cooldown - 1); return; }
        if (giant.getLocation().distanceSquared(player.getLocation()) <= 9.0) {
            player.damage(20.0, giant);
            attackCooldowns.put(giant.getUniqueId(), 20);
        }
    }

    private void piglinHive() {
        if (!config.isEnablePiglinHive()) return;
        for (World world : Bukkit.getWorlds()) {
            for (LivingEntity entity : world.getLivingEntities()) {
                if (entity.getType() != EntityType.ZOMBIFIED_PIGLIN) continue;
                if (!config.isTestMode() && Math.random() >= config.getPiglinHiveChance()) continue;
                Player player = nearestPlayer(entity, config.getPiglinHiveRange());
                if (player == null) continue;
                for (Entity nearby : entity.getNearbyEntities(config.getPiglinHiveRange(), config.getPiglinHiveRange(), config.getPiglinHiveRange())) {
                    if (nearby instanceof Mob mob && isPiglinType(mob.getType())) mob.setTarget(player);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onDeath(EntityDeathEvent event) {
        LivingEntity entity = event.getEntity();
        if (!(entity instanceof Mob mob) || !isHostile(mob)) return;
        if (mob.getScoreboardTags().contains(PINATA_TAG)) { event.setDroppedExp(0); return; }
        OverpoweredConfig.MobConfig mobConfig = config.getFor(key(mob.getType()));
        event.setDroppedExp((int) Math.ceil(event.getDroppedExp() * mobConfig.xp()));
        boolean armored = mob.getEquipment() != null && Arrays.stream(mob.getEquipment().getArmorContents()).anyMatch(stack -> stack != null && !stack.getType().isAir());
        double multiplier = armored ? 3.0 : 1.2;
        List<ItemStack> extras = new ArrayList<>();
        for (ItemStack drop : event.getDrops()) {
            int extra = (int) Math.floor(drop.getAmount() * (multiplier - 1.0));
            if (extra > 0) { ItemStack copy = drop.clone(); copy.setAmount(extra); extras.add(copy); }
        }
        event.getDrops().addAll(extras);

        if (config.isEnablePinata() && mob instanceof Zombie zombie && zombie.getKiller() != null &&
            !mob.getScoreboardTags().contains(PINATA_TAG)) spawnPinata(zombie);
    }

    private void spawnPinata(Zombie zombie) {
        double chance = zombie.getWorld().getNearbyEntities(zombie.getLocation(), 20, 20, 20).stream().filter(e -> e instanceof Zombie).count() >= 10 ? 0.75 : config.getZombiePinataChance();
        if (!config.isTestMode() && Math.random() >= chance) return;
        int count = config.getZombiePinataCount();
        if (Bukkit.getOnlinePlayers().stream().filter(p -> p.getWorld() == zombie.getWorld() && p.getLocation().distanceSquared(zombie.getLocation()) < 400).count() > 1) count = 3;
        for (int i = 0; i < count; i++) {
            Location location = zombie.getLocation().clone().add((Math.random() - 0.5) * 5.0, 0, (Math.random() - 0.5) * 5.0);
            Zombie baby = (Zombie) zombie.getWorld().spawnEntity(location, EntityType.ZOMBIE);
            baby.setBaby(true);
            baby.addScoreboardTag(PINATA_TAG);
        }
        World world = zombie.getWorld();
        world.playSound(zombie.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_BLAST, SoundCategory.HOSTILE, 1.0f, 1.0f);
        world.spawnParticle(Particle.EXPLOSION_EMITTER, zombie.getLocation(), 1);
        world.spawnParticle(Particle.HAPPY_VILLAGER, zombie.getLocation().add(0, 1, 0), 30, 1.5, 1.5, 1.5, 0.5);
    }

    @EventHandler(ignoreCancelled = true)
    public void onWaterDamage(EntityDamageEvent event) {
        if (config.isEnableWaterEndermen() && event.getEntity().getType() == EntityType.ENDERMAN && event.getCause() == EntityDamageEvent.DamageCause.DROWNING) event.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true)
    public void onShulkerBulletHit(ProjectileHitEvent event) {
        if (!(event.getEntity() instanceof ShulkerBullet) || !(event.getHitEntity() instanceof LivingEntity target)) return;
        Bukkit.getScheduler().runTask(this, () -> {
            PotionEffect effect = target.getPotionEffect(PotionEffectType.LEVITATION);
            if (effect == null) return;
            int duration = (int) Math.ceil(effect.getDuration() * config.getShulkerLevitationDurationMultiplier());
            target.addPotionEffect(new PotionEffect(PotionEffectType.LEVITATION, duration, effect.getAmplifier(), effect.isAmbient(), effect.hasParticles(), effect.hasIcon()), true);
        });
    }

    @EventHandler(ignoreCancelled = true)
    public void onExplosionPrime(ExplosionPrimeEvent event) {
        if (!(event.getEntity() instanceof Fireball fireball) || !(fireball.getShooter() instanceof Ghast ghast) ||
            !ghast.getScoreboardTags().contains(BOOSTED_TAG)) return;
        event.setRadius((float) (event.getRadius() * config.getGhastExplosionMultiplier()));
    }

    @EventHandler
    public void onStrongholdAdvancement(PlayerAdvancementDoneEvent event) {
        if (!config.isEnableStrongholdMobs() || !event.getAdvancement().getKey().equals(NamespacedKey.minecraft("story/follow_ender_eye"))) return;
        List<EntityType> types = List.of(EntityType.ZOMBIE, EntityType.SKELETON, EntityType.SPIDER, EntityType.CREEPER);
        Player player = event.getPlayer();
        for (int i = 0; i < config.getStrongholdMobCount(); i++) {
            EntityType type = types.get((int) (Math.random() * types.size()));
            Location location = player.getLocation().clone().add((Math.random() - 0.5) * 50, 0, (Math.random() - 0.5) * 50);
            player.getWorld().spawnEntity(location, type);
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) { bossBars.clear(event.getPlayer()); }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("overpoweredmobs.admin")) { sender.sendMessage("§cYou do not have permission."); return true; }
        if (args.length == 0 || args[0].equalsIgnoreCase("status")) { status(sender); return true; }
        switch (args[0].toLowerCase()) {
            case "reload" -> { reloadOpmConfig(); sender.sendMessage("§aConfig reloaded."); }
            case "reset" -> { resetOpmConfig(); sender.sendMessage("§aConfig reset to defaults."); }
            case "test" -> { config.setTestMode(!config.isTestMode()); config.save(); sender.sendMessage("§aTest mode " + (config.isTestMode() ? "enabled" : "disabled") + "."); }
            case "set" -> setCommand(sender, args);
            case "cavalry" -> cavalryCommand(sender, args);
            default -> sender.sendMessage("§eUsage: /opm <status|set|reload|reset|test|cavalry>");
        }
        return true;
    }

    private void setCommand(CommandSender sender, String[] args) {
        if (args.length != 4) { sender.sendMessage("§eUsage: /opm set <mob> <attribute> <value>"); return; }
        Set<String> valid = Set.of("health", "damage", "speed", "armor", "followRange", "xp", "spawnchance");
        if (!valid.contains(args[2])) { sender.sendMessage("§cUnknown attribute."); return; }
        double value;
        try { value = Double.parseDouble(args[3]); } catch (NumberFormatException e) { sender.sendMessage("§cValue must be a number."); return; }
        if (args[2].equals("spawnchance") ? value < 0 || value > 1 : value < 0.1 || value > 100) { sender.sendMessage("§cValue is outside the allowed range."); return; }
        EntityType type = entityType(args[1]);
        if (type == null) { sender.sendMessage("§cUnknown entity type: " + args[1]); return; }
        OverpoweredConfig.MobConfig mob = config.getFor(key(type)).copy(); mob.set(args[2], value); config.setFor(key(type), mob); config.save();
        sender.sendMessage("§aSet " + key(type) + " " + args[2] + " to " + value + ".");
    }

    private void cavalryCommand(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player) || args.length != 3) { sender.sendMessage("§eUsage: /opm cavalry <rider> <mount>"); return; }
        EntityType riderType = entityType(args[1]), mountType = entityType(args[2]);
        if (riderType == null || mountType == null) { sender.sendMessage("§cUnknown rider or mount."); return; }
        Mob mount = (Mob) player.getWorld().spawnEntity(player.getLocation(), mountType);
        Mob rider = (Mob) player.getWorld().spawnEntity(player.getLocation(), riderType);
        mount.addScoreboardTag(CAVALRY_MOUNT_TAG); mount.addPassenger(rider);
        sender.sendMessage("§aSpawned " + key(riderType) + " riding " + key(mountType) + ".");
    }

    private void status(CommandSender sender) {
        sender.sendMessage("§6=== Default multipliers ===");
        for (String attr : List.of("health", "damage", "speed", "armor", "followRange", "xp")) sender.sendMessage("§7  " + attr + ": " + config.getDefaults().get(attr));
        sender.sendMessage("§6=== Per-mob overrides ===");
        for (Map.Entry<String, OverpoweredConfig.MobConfig> entry : config.getMobs().entrySet()) sender.sendMessage("§7  " + entry.getKey() + ": health=" + entry.getValue().get("health") + ", damage=" + entry.getValue().get("damage") + ", speed=" + entry.getValue().get("speed"));
    }

    @Override public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) return partial(args[0], "status", "set", "reload", "reset", "test", "cavalry");
        if (args[0].equalsIgnoreCase("set") && args.length == 3) return partial(args[2], "health", "damage", "speed", "armor", "followRange", "xp", "spawnchance");
        if (args[0].equalsIgnoreCase("cavalry") && args.length >= 2) return partial(args[args.length - 1], Arrays.stream(EntityType.values()).map(OverpoweredMobsPlugin::key).toArray(String[]::new));
        return List.of();
    }

    private static List<String> partial(String value, String... candidates) { return Arrays.stream(candidates).filter(candidate -> candidate.startsWith(value.toLowerCase())).toList(); }
    private static boolean isHostile(LivingEntity entity) { return entity instanceof Monster || entity instanceof Slime; }
    private static boolean isPiglinType(EntityType type) { return type == EntityType.PIGLIN || type == EntityType.PIGLIN_BRUTE || type == EntityType.ZOMBIFIED_PIGLIN; }
    private static double followRange(Mob mob) { AttributeInstance attr = mob.getAttribute(Attribute.FOLLOW_RANGE); return attr == null ? 32.0 : attr.getValue(); }
    private static Player nearestPlayer(Entity entity, double range) {
        Player nearest = null; double distance = range * range;
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.getWorld() != entity.getWorld()) continue;
            double current = player.getLocation().distanceSquared(entity.getLocation());
            if (current < distance) { distance = current; nearest = player; }
        }
        return nearest;
    }
    private static EntityType entityType(String value) {
        if (!value.contains(":")) value = "minecraft:" + value;
        NamespacedKey key = NamespacedKey.fromString(value);
        if (key == null) return null;
        for (EntityType type : EntityType.values()) if (type.getKey().equals(key)) return type;
        return null;
    }
    private static String key(EntityType type) { return type.getKey().toString(); }
    private static String pretty(String value) { String lower = value.toLowerCase().replace('_', ' '); return Character.toUpperCase(lower.charAt(0)) + lower.substring(1); }
}
