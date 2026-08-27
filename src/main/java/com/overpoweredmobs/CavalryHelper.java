package com.overpoweredmobs;

import net.minecraft.world.entity.Mob;

public final class CavalryHelper {
    private CavalryHelper() {}

    public static boolean attachRider(Mob rider, Mob mount) {
        if (!rider.startRiding(mount)) return false;

        syncRider(rider, mount);
        mount.positionRider(rider);
        mount.getGoalSelector().addGoal(1, new CavalryAIGoal(rider, mount));
        return true;
    }

    public static void tickRider(Mob rider) {
        if (!(rider.getVehicle() instanceof Mob mount)
            || !mount.entityTags().contains(OverpoweredMobs.CAVALRY_MOUNT_TAG)) return;

        syncRider(rider, mount);
    }

    private static void syncRider(Mob rider, Mob mount) {
        rider.getNavigation().stop();
        rider.setYRot(mount.getYRot());
        rider.setYBodyRot(mount.getYRot());
        rider.setYHeadRot(mount.getYHeadRot());
        rider.setXRot(mount.getXRot());
    }
}
