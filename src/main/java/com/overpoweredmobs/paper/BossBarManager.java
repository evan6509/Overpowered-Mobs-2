package com.overpoweredmobs.paper;

import com.overpoweredmobs.paper.config.OverpoweredConfig;
import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class BossBarManager {
    private final OverpoweredMobsPlugin plugin;
    private final Map<UUID, BossBar> bars = new HashMap<>();

    public BossBarManager(OverpoweredMobsPlugin plugin) { this.plugin = plugin; }

    public void tick() {
        OverpoweredConfig config = plugin.getOpmConfig();
        for (Player player : Bukkit.getOnlinePlayers()) {
            BossBar bar = bars.computeIfAbsent(player.getUniqueId(), id -> Bukkit.createBossBar("", BarColor.RED, BarStyle.SEGMENTED_10));
            if (!config.isEnableBossBar()) { bar.setVisible(false); continue; }
            LivingEntity nearest = findNearest(player, config.getBossBarRange());
            if (nearest == null) { bar.setVisible(false); continue; }
            double health = Math.max(0.0, Math.min(1.0, nearest.getHealth() / nearest.getMaxHealth()));
            bar.setTitle("§c⚡ Overpowered " + pretty(nearest.getType().name()));
            bar.setProgress(health);
            bar.setColor(health > 0.5 ? BarColor.GREEN : health > 0.25 ? BarColor.YELLOW : BarColor.RED);
            if (!bar.getPlayers().contains(player)) bar.addPlayer(player);
            bar.setVisible(true);
        }
    }

    private LivingEntity findNearest(Player player, double range) {
        LivingEntity nearest = null;
        double distance = range * range;
        for (Entity entity : player.getNearbyEntities(range, range, range)) {
            if (!(entity instanceof LivingEntity living) || !entity.isValid() ||
                !entity.getScoreboardTags().contains(OverpoweredMobsPlugin.BOOSTED_TAG)) continue;
            double current = entity.getLocation().distanceSquared(player.getLocation());
            if (current < distance) { distance = current; nearest = living; }
        }
        return nearest;
    }

    public void clear(Player player) {
        BossBar bar = bars.remove(player.getUniqueId());
        if (bar != null) bar.removePlayer(player);
    }

    public void clearAll() {
        for (BossBar bar : bars.values()) bar.removeAll();
        bars.clear();
    }

    private static String pretty(String value) {
        String lower = value.toLowerCase().replace('_', ' ');
        return Character.toUpperCase(lower.charAt(0)) + lower.substring(1);
    }
}
