package com.overpoweredmobs;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;

public final class CavalryHelper {
    private CavalryHelper() {}

    public static boolean attachRider(Mob rider, Mob mount) {
        if (!rider.startRiding(mount)) return false;

        syncMount(mount);
        syncRider(rider, mount);
        mount.positionRider(rider);
        ensureGoal(rider, mount);
        return true;
    }

    public static void tick(Mob mob) {
        if (mob.entityTags().contains(OverpoweredMobs.CAVALRY_MOUNT_TAG)) {
            syncMount(mob);
        }

        if (!(mob.getVehicle() instanceof Mob mount)
            || !mount.entityTags().contains(OverpoweredMobs.CAVALRY_MOUNT_TAG)) return;

        if (mob.level() instanceof ServerLevel) ensureGoal(mob, mount);
        syncRider(mob, mount);
    }

    private static void ensureGoal(Mob rider, Mob mount) {
        var goals = mount.getGoalSelector();
        for (var wrapped : goals.getAvailableGoals()) {
            if (wrapped.getGoal() instanceof CavalryAIGoal cavalry && cavalry.isFor(rider)) return;
        }
        goals.removeAllGoals(goal -> goal instanceof CavalryAIGoal);
        goals.addGoal(1, new CavalryAIGoal(rider, mount));
    }

    private static void syncMount(Mob mount) {
        float mountYaw = mount.getYRot();
        mount.setYBodyRot(mountYaw);
        mount.setYHeadRot(mountYaw);
    }

    private static void syncRider(Mob rider, Mob mount) {
        rider.getNavigation().stop();
        float mountYaw = mount.getYRot();
        rider.setYRot(mountYaw);
        rider.setYBodyRot(mountYaw);
        rider.setYHeadRot(mountYaw);
        rider.setXRot(mount.getXRot());
    }
}
