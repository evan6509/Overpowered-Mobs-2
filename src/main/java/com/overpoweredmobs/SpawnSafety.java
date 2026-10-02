package com.overpoweredmobs;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.AABB;

public final class SpawnSafety {
    private SpawnSafety() {}

    public static boolean canFitCavalry(ServerLevel level, Mob rider, Mob mount) {
        var ridingPosition = mount.getPassengerRidingPosition(rider).subtract(rider.getVehicleAttachmentPoint(mount));
        AABB riderBox = rider.getBoundingBox().move(ridingPosition.subtract(rider.position()));
        return hasSpace(level, rider, mount.getBoundingBox()) && hasSpace(level, rider, riderBox);
    }

    public static boolean findNearbyFloor(ServerLevel level, Mob mob, Mob parent) {
        for (int attempt = 0; attempt < 24; attempt++) {
            double x = parent.getX() + (parent.getRandom().nextDouble() - 0.5) * 5.0;
            double z = parent.getZ() + (parent.getRandom().nextDouble() - 0.5) * 5.0;
            for (int offset : new int[] {0, 1, -1, 2, -2}) {
                BlockPos feet = BlockPos.containing(x, parent.getY() + offset, z);
                BlockPos floor = feet.below();
                if (!level.isLoaded(floor)
                    || !level.getBlockState(floor).entityCanStandOnFace(level, floor, mob, Direction.UP)) continue;
                mob.setPos(x, feet.getY(), z);
                if (hasSpace(level, mob, mob.getBoundingBox())) return true;
            }
        }
        return false;
    }

    private static boolean hasSpace(ServerLevel level, Mob excluded, AABB box) {
        return level.isLoaded(BlockPos.containing(box.minX, box.minY, box.minZ))
            && level.isLoaded(BlockPos.containing(box.maxX, box.maxY, box.maxZ))
            && level.getWorldBorder().isWithinBounds(box)
            && level.noCollision(excluded, box)
            && !level.containsAnyLiquid(box);
    }
}
