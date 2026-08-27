package com.overpoweredmobs;

import net.minecraft.world.entity.Mob;

public final class CavalryHelper {
    private CavalryHelper() {}

    public static boolean attachRider(Mob rider, Mob mount) {
        if (!rider.startRiding(mount)) return false;

        syncMount(mount);
        syncRider(rider, mount);
        mount.positionRider(rider);
        mount.getGoalSelector().addGoal(1, new CavalryAIGoal(rider, mount));
        return true;
    }

    public static void tick(Mob mob) {
        if (mob.entityTags().contains(OverpoweredMobs.CAVALRY_MOUNT_TAG)) {
            syncMount(mob);
        }

        if (!(mob.getVehicle() instanceof Mob mount)
            || !mount.entityTags().contains(OverpoweredMobs.CAVALRY_MOUNT_TAG)) return;

        syncRider(mob, mount);
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
