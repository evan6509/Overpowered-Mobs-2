package com.overpoweredmobs;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.equine.AbstractHorse;

public final class CavalryHelper {
    private CavalryHelper() {}

    public static boolean attachRider(Mob rider, Mob mount) {
        if (!rider.startRiding(mount)) return false;

        // Untamed horses can run their bucking goal and eject even mob passengers.
        if (mount instanceof AbstractHorse horse) horse.setTamed(true);

        syncMount(mount);
        syncRider(rider, mount);
        mount.positionRider(rider);
        ensureGoal(rider, mount);
        return true;
    }

    public static void tick(Mob mob) {
        if (isCavalryMount(mob)) {
            syncMount(mob);
        }

        if (!(mob.getVehicle() instanceof Mob mount)
            || !isCavalryMount(mount)) return;

        if (mob.level() instanceof ServerLevel) ensureGoal(mob, mount);
        syncRider(mob, mount);
    }

    public static boolean isCavalryMount(Mob mount) {
        // Passenger links are vanilla synchronized data; scoreboard tags are server-only.
        if (!(mount.level() instanceof ServerLevel)) return mount.getFirstPassenger() instanceof Mob;
        return mount.entityTags().contains(OverpoweredMobs.CAVALRY_MOUNT_TAG);
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
        float mountYaw = Mth.wrapDegrees(mount.getYRot());
        mount.setYBodyRot(mountYaw);
        mount.setYHeadRot(mountYaw);
        // Vanilla already normalized these before our post-AI rotation change.
        mount.yBodyRotO = mountYaw - Mth.wrapDegrees(mountYaw - mount.yBodyRotO);
        mount.yHeadRotO = mountYaw - Mth.wrapDegrees(mountYaw - mount.yHeadRotO);
    }

    private static void syncRider(Mob rider, Mob mount) {
        rider.getNavigation().stop();
        float mountYaw = Mth.wrapDegrees(mount.getYRot());
        rider.setYRot(mountYaw);
        rider.setYBodyRot(mountYaw);
        rider.setYHeadRot(mountYaw);
        rider.setXRot(mount.getXRot());
        // Copy the mount's previous rotation as well as its current rotation.
        // The rider's own AI/network interpolation must not introduce a second turn.
        rider.yRotO = mountYaw - Mth.wrapDegrees(mountYaw - mount.yRotO);
        rider.yBodyRotO = mountYaw - Mth.wrapDegrees(mountYaw - mount.yBodyRotO);
        rider.yHeadRotO = mountYaw - Mth.wrapDegrees(mountYaw - mount.yHeadRotO);
        rider.xRotO = mount.xRotO;
    }
}
