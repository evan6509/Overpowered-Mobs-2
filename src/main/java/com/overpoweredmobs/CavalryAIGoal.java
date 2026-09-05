package com.overpoweredmobs;

import com.overpoweredmobs.mixin.PhantomAccessor;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Ghast;
import net.minecraft.world.entity.monster.Phantom;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

public class CavalryAIGoal extends Goal {
    private static final int PATH_UPDATE_INTERVAL = 5;

    private final Mob rider;
    private final Mob mount;
    private final boolean diveBomb;
    private int pathUpdateCooldown;

    public CavalryAIGoal(Mob rider, Mob mount) {
        this.rider = rider;
        this.mount = mount;
        this.diveBomb = rider instanceof Creeper && mount instanceof Phantom;
        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    public boolean isFor(Mob rider) {
        return this.rider == rider;
    }

    @Override
    public boolean canUse() {
        return rider.isAlive()
            && mount.isAlive()
            && rider.getVehicle() == mount
            && rider.getTarget() != null
            && rider.getTarget().isAlive();
    }

    @Override
    public boolean canContinueToUse() {
        return canUse();
    }

    @Override
    public void start() {
        pathUpdateCooldown = 0;
    }

    @Override
    public void tick() {
        LivingEntity target = rider.getTarget();
        if (target == null) return;

        mount.setTarget(target);
        if (pathUpdateCooldown > 0) {
            pathUpdateCooldown--;
            return;
        }
        pathUpdateCooldown = PATH_UPDATE_INTERVAL - 1;

        if (mount instanceof Phantom phantom) {
            double dy = target.getY() - mount.getY();
            double hDistSq = mount.distanceToSqr(target.getX(), mount.getY(), target.getZ());
            double targetY = diveBomb && !(dy < 10.0 && hDistSq < 225.0)
                ? target.getY() + 15.0 : target.getY();
            // Phantoms ignore PathNavigation and steer toward this point directly.
            ((PhantomAccessor) phantom).setMoveTargetPoint(new Vec3(target.getX(), targetY, target.getZ()));
        } else if (mount instanceof Ghast) {
            mount.getMoveControl().setWantedPosition(target.getX(), target.getY(), target.getZ(), 2.0);
        } else {
            mount.getNavigation().setSpeedModifier(2.0);
            mount.getNavigation().moveTo(target, 2.0);
        }
    }

    @Override
    public void stop() {
        pathUpdateCooldown = 0;
        mount.getNavigation().stop();
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }
}
