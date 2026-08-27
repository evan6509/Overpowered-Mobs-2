package com.overpoweredmobs;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Phantom;

public class CavalryAIGoal extends Goal {
    private final Mob rider;
    private final Mob mount;
    private final boolean diveBomb;

    public CavalryAIGoal(Mob rider, Mob mount) {
        this.rider = rider;
        this.mount = mount;
        this.diveBomb = rider instanceof Creeper && mount instanceof Phantom;
    }

    @Override
    public boolean canUse() {
        return rider.isAlive()
            && mount.isAlive()
            && rider.getVehicle() == mount
            && rider.getTarget() != null;
    }

    @Override
    public boolean canContinueToUse() {
        return canUse();
    }

    @Override
    public void tick() {
        LivingEntity target = rider.getTarget();
        if (target == null) return;

        mount.setTarget(target);

        if (diveBomb) {
            double dy = target.getY() - mount.getY();
            double hDistSq = mount.distanceToSqr(target.getX(), mount.getY(), target.getZ());
            if (dy < 10.0 && hDistSq < 225.0) {
                mount.getNavigation().setSpeedModifier(3.0);
                mount.getNavigation().moveTo(target, 3.0);
            } else {
                mount.getNavigation().setSpeedModifier(1.5);
                mount.getNavigation().moveTo(target.getX(), target.getY() + 15, target.getZ(), 1.5);
            }
        } else {
            mount.getNavigation().setSpeedModifier(2.0);
            mount.getNavigation().moveTo(target, 2.0);
        }
    }

    @Override
    public void stop() {
        mount.getNavigation().stop();
    }
}
