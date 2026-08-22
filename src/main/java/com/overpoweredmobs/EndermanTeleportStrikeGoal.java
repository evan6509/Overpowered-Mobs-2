package com.overpoweredmobs;

import com.overpoweredmobs.config.OverpoweredConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.core.particles.ParticleTypes;

import java.util.EnumSet;
import java.util.Set;

public class EndermanTeleportStrikeGoal extends Goal {
    private final EnderMan enderman;
    private int cooldown;

    public EndermanTeleportStrikeGoal(EnderMan enderman) {
        this.enderman = enderman;
        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (cooldown > 0) {
            cooldown--;
            return false;
        }

        OverpoweredConfig config = OverpoweredMobs.getConfig();
        if (!config.isEnableEndermanTeleportStrike()) return false;
        if (!(enderman.level() instanceof ServerLevel)) return false;

        LivingEntity target = enderman.getTarget();
        if (target == null || !target.isAlive()) return false;

        double distance = enderman.distanceToSqr(target);
        double min = config.getEndermanTeleportMinRange();
        double max = config.getEndermanTeleportMaxRange();
        return distance >= min * min && distance <= max * max;
    }

    @Override
    public boolean canContinueToUse() {
        return false;
    }

    @Override
    public void start() {
        OverpoweredConfig config = OverpoweredMobs.getConfig();
        cooldown = config.getEndermanTeleportCooldown();

        if (!(enderman.level() instanceof ServerLevel level)) return;
        LivingEntity target = enderman.getTarget();
        if (target == null || !target.isAlive()) return;

        Vec3 destination = findDestination(level, target, config);
        if (destination == null) return;

        level.sendParticles(ParticleTypes.PORTAL,
            enderman.getX(), enderman.getY() + 0.8, enderman.getZ(),
            20, 0.4, 0.8, 0.4, 0.2);
        level.playSound(null, enderman.getX(), enderman.getY(), enderman.getZ(),
            SoundEvents.ENDERMAN_TELEPORT, SoundSource.HOSTILE, 1.0f, 1.0f);

        boolean teleported = enderman.teleportTo(level, destination.x, destination.y, destination.z,
            Set.of(), enderman.getYRot(), enderman.getXRot(), false);
        if (!teleported) return;

        level.sendParticles(ParticleTypes.PORTAL,
            enderman.getX(), enderman.getY() + 0.8, enderman.getZ(),
            20, 0.4, 0.8, 0.4, 0.2);
        level.playSound(null, enderman.getX(), enderman.getY(), enderman.getZ(),
            SoundEvents.ENDERMAN_TELEPORT, SoundSource.HOSTILE, 1.0f, 1.0f);
        if (enderman.distanceToSqr(target) <= 3.5 * 3.5) {
            enderman.doHurtTarget(level, target);
        }
    }

    private Vec3 findDestination(ServerLevel level, LivingEntity target, OverpoweredConfig config) {
        double min = config.getEndermanTeleportMinRange();
        double max = config.getEndermanTeleportMaxRange();
        AABB currentBox = enderman.getBoundingBox();

        for (int attempt = 0; attempt < 12; attempt++) {
            double angle = enderman.getRandom().nextDouble() * Math.PI * 2.0;
            double distance = min + enderman.getRandom().nextDouble() * (max - min);
            double x = target.getX() + Math.cos(angle) * distance;
            double y = target.getY();
            double z = target.getZ() + Math.sin(angle) * distance;
            BlockPos feet = BlockPos.containing(x, y, z);
            if (!level.getBlockState(feet).isAir()
                || !level.getBlockState(feet.above()).isAir()
                || !level.getBlockState(feet.below()).isSolid()) {
                continue;
            }

            AABB destinationBox = currentBox.move(x - enderman.getX(), y - enderman.getY(), z - enderman.getZ());
            if (level.noCollision(enderman, destinationBox)) {
                return new Vec3(x, y, z);
            }
        }
        return null;
    }
}
