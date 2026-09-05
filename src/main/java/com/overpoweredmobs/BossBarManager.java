package com.overpoweredmobs;

import com.overpoweredmobs.config.OverpoweredConfig;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.BossEvent;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class BossBarManager {
    private static final int UPDATE_INTERVAL = 5;
    private static final Map<ServerPlayer, ServerBossEvent> playerBars = new HashMap<>();

    public static void onWorldTick(ServerLevel level) {
        OverpoweredConfig config = OverpoweredMobs.getConfig();
        if (!config.isEnableBossBar()) {
            hideAllBars();
            return;
        }
        if (level.getServer().getTickCount() % UPDATE_INTERVAL != 0) return;

        double range = config.getBossBarRange();
        double rangeSq = range * range;

        for (ServerPlayer player : level.players()) {
            Mob nearest = findNearestBoosted(level, player, rangeSq);
            if (nearest == null) {
                ServerBossEvent bar = playerBars.get(player);
                if (bar != null) bar.setVisible(false);
                continue;
            }
            ServerBossEvent bar = playerBars.computeIfAbsent(player, p -> {
                ServerBossEvent b = new ServerBossEvent(
                    UUID.randomUUID(),
                    Component.literal(""),
                    BossEvent.BossBarColor.RED,
                    BossEvent.BossBarOverlay.PROGRESS
                );
                b.addPlayer(p);
                return b;
            });

            String prefix = OverpoweredMobs.isElite(nearest) ? "\u00A75\u00A7l\u2620 Elite " : "\u00A7c\u26A1 Overpowered ";
            bar.setName(Component.literal(prefix).append(nearest.getType().getDescription()));
            float progress = Math.clamp(nearest.getHealth() / nearest.getMaxHealth(), 0.0f, 1.0f);
            bar.setProgress(progress);

            if (progress > 0.5f) {
                bar.setColor(BossEvent.BossBarColor.GREEN);
            } else if (progress > 0.25f) {
                bar.setColor(BossEvent.BossBarColor.YELLOW);
            } else {
                bar.setColor(BossEvent.BossBarColor.RED);
            }

            if (!bar.isVisible()) bar.setVisible(true);
        }
    }

    private static Mob findNearestBoosted(ServerLevel level, Player player, double rangeSq) {
        Mob nearest = null;
        double nearestDistSq = rangeSq;

        for (Mob mob : level.getEntitiesOfClass(Mob.class, player.getBoundingBox().inflate(Math.sqrt(rangeSq)))) {
            if (!mob.isAlive()) continue;
            if (!mob.entityTags().contains(OverpoweredMobs.BOOSTED_TAG)) continue;
            double distSq = mob.distanceToSqr(player);
            if (distSq < nearestDistSq) {
                nearestDistSq = distSq;
                nearest = mob;
            }
        }
        return nearest;
    }

    public static void onPlayerDisconnect(ServerPlayer player) {
        ServerBossEvent bar = playerBars.remove(player);
        if (bar != null) {
            bar.removePlayer(player);
        }
    }

    public static void clear() {
        for (ServerBossEvent bar : playerBars.values()) {
            bar.removeAllPlayers();
        }
        playerBars.clear();
    }

    private static void hideAllBars() {
        for (ServerBossEvent bar : playerBars.values()) {
            if (bar.isVisible()) bar.setVisible(false);
        }
    }

    private BossBarManager() {}
}
