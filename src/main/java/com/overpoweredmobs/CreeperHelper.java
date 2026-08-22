package com.overpoweredmobs;

import com.overpoweredmobs.mixin.CreeperAccessor;
import net.minecraft.world.entity.monster.Creeper;

public final class CreeperHelper {

    public static void setPowered(Creeper creeper) {
        creeper.getEntityData().set(CreeperAccessor.getDataIsPowered(), true);
    }

    private CreeperHelper() {}
}
