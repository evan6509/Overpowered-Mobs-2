package com.overpoweredmobs;

import net.minecraft.world.phys.AABB;

public final class GhastCavalryMeleeReach {
    private GhastCavalryMeleeReach() {}

    public static AABB extendDownToMount(AABB attackBox, AABB mountBox) {
        // Vanilla already includes the mount horizontally. Include its underside
        // so the skeleton above a ghast can hit targets standing next to the mount.
        return new AABB(attackBox.minX, Math.min(attackBox.minY, mountBox.minY), attackBox.minZ,
            attackBox.maxX, attackBox.maxY, attackBox.maxZ);
    }
}
