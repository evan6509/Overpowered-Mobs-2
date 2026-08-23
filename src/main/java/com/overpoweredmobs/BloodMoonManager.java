package com.overpoweredmobs;

import com.overpoweredmobs.config.OverpoweredConfig;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Mob;

import java.util.Map;
import java.util.WeakHashMap;

public final class BloodMoonManager {
    private static final Map<ServerLevel, Integer> ACTIVE_TICKS = new WeakHashMap<>();
    private static final Map<ServerLevel, Long> STARTED_DAYS = new WeakHashMap<>();

    public static void onWorldTick(ServerLevel level) {
        OverpoweredConfig config = OverpoweredMobs.getConfig();
        if (!config.isEnableBloodMoon()) {
            ACTIVE_TICKS.remove(level);
            return;
        }

        Integer remaining = ACTIVE_TICKS.get(level);
        if (remaining != null) {
            if (remaining <= 1) {
                ACTIVE_TICKS.remove(level);
                OverpoweredMobsLogger.info("Blood moon ended in " + level.dimension().identifier());
            } else {
                ACTIVE_TICKS.put(level, remaining - 1);
                if (remaining % 1200 == 0) {
                    telegraph(level, false);
                }
            }
            return;
        }

        if (!level.dimensionType().hasSkyLight()) return;
        long time = level.getLevelData().getGameTime();
        long day = Math.floorDiv(time, 24000L);
        int timeOfDay = (int) Math.floorMod(time, 24000L);
        if (timeOfDay < 13000 || timeOfDay >= 23000) return;
        if (day % config.getBloodMoonIntervalNights() != 0) return;
        if (STARTED_DAYS.getOrDefault(level, Long.MIN_VALUE) == day) return;

        trigger(level);
    }

    public static void trigger(ServerLevel level) {
        OverpoweredConfig config = OverpoweredMobs.getConfig();
        if (!config.isEnableBloodMoon()) return;

        long day = Math.floorDiv(level.getLevelData().getGameTime(), 24000L);
        STARTED_DAYS.put(level, day);
        ACTIVE_TICKS.put(level, config.getBloodMoonDurationTicks());
        telegraph(level, true);
        OverpoweredMobsLogger.info("Blood moon started in " + level.dimension().identifier());
    }

    public static boolean isActive(ServerLevel level) {
        return ACTIVE_TICKS.getOrDefault(level, 0) > 0;
    }

    public static boolean shouldForceHorde(Mob mob) {
        if (!(mob.level() instanceof ServerLevel level) || !isActive(level)) return false;
        OverpoweredConfig config = OverpoweredMobs.getConfig();
        return !config.isTestMode()
            && mob.getRandom().nextDouble() < config.getBloodMoonHordeChance();
    }

    public static int getRemaining(ServerLevel level) {
        return ACTIVE_TICKS.getOrDefault(level, 0);
    }

    private static void telegraph(ServerLevel level, boolean loud) {
        level.setSkyFlashTime(loud ? 40 : 5);
        for (ServerPlayer player : level.players()) {
            level.sendParticles(ParticleTypes.CRIMSON_SPORE,
                player.getX(), player.getY() + 18.0, player.getZ(),
                loud ? 35 : 8, 8.0, 2.0, 8.0, 0.02);
            if (loud) {
                level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.WITHER_SPAWN, SoundSource.HOSTILE, 0.75f, 0.65f);
            }
        }
    }

    private BloodMoonManager() {}
}
