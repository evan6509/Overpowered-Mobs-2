package com.overpoweredmobs;

import com.overpoweredmobs.config.OverpoweredConfig;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.EntitySpawnReason;

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
            if (remaining <= 1 || !isNightOnStartedDay(level)) {
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

        long time = level.getDefaultClockTime();
        long day = Math.floorDiv(time, 24000L);
        if (!isNight(level)) return;
        if (day % config.getBloodMoonIntervalNights() != 0) return;
        if (STARTED_DAYS.getOrDefault(level, Long.MIN_VALUE) == day) return;

        trigger(level);
    }

    public static boolean trigger(ServerLevel level) {
        OverpoweredConfig config = OverpoweredMobs.getConfig();
        if (!config.isEnableBloodMoon() || !isNight(level)) return false;

        long day = Math.floorDiv(level.getDefaultClockTime(), 24000L);
        STARTED_DAYS.put(level, day);
        ACTIVE_TICKS.put(level, config.getBloodMoonDurationTicks());
        telegraph(level, true);
        OverpoweredMobsLogger.info("Blood moon started in " + level.dimension().identifier());
        return true;
    }

    public static boolean isActive(ServerLevel level) {
        return OverpoweredMobs.getConfig().isEnableBloodMoon()
            && ACTIVE_TICKS.getOrDefault(level, 0) > 0
            && isNightOnStartedDay(level);
    }

    public static boolean shouldForceHorde(Mob mob) {
        if (!(mob.level() instanceof ServerLevel level) || !isActive(level)) return false;
        OverpoweredConfig config = OverpoweredMobs.getConfig();
        return !config.isTestMode()
            && mob.getRandom().nextDouble() < config.getBloodMoonHordeChance();
    }

    public static int getRemaining(ServerLevel level) {
        return isActive(level) ? ACTIVE_TICKS.get(level) : 0;
    }

    private static boolean isNightOnStartedDay(ServerLevel level) {
        return isNight(level)
            && STARTED_DAYS.getOrDefault(level, Long.MIN_VALUE)
                == Math.floorDiv(level.getDefaultClockTime(), 24000L);
    }

    private static boolean isNight(ServerLevel level) {
        if (!level.dimensionType().hasSkyLight()) return false;
        int timeOfDay = (int) Math.floorMod(level.getDefaultClockTime(), 24000L);
        return timeOfDay >= 13000 && timeOfDay < 23000;
    }

    private static void telegraph(ServerLevel level, boolean loud) {
        for (ServerPlayer player : level.players()) {
            var flash = BuiltInRegistries.ENTITY_TYPE.getValue(Identifier.withDefaultNamespace("lightning_bolt"))
                .create(level, EntitySpawnReason.TRIGGERED);
            if (flash instanceof LightningBolt lightning) {
                lightning.setPos(player.getX(), player.getY() + 64.0, player.getZ());
                // Client-only vanilla lightning produces sky flashes without server damage,
                // fires, rod activation, copper changes or advancement triggers.
                player.connection.send(new ClientboundAddEntityPacket(lightning, 0, lightning.blockPosition()));
            }
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
