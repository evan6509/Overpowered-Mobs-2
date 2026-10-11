package com.overpoweredmobs.mixin;

import com.overpoweredmobs.GhastCavalryMeleeReach;
import com.overpoweredmobs.OverpoweredMobs;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Ghast;
import net.minecraft.world.entity.monster.skeleton.WitherSkeleton;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Mob.class)
public class GhastCavalryMeleeMixin {
    @Inject(method = "getAttackBoundingBox", at = @At("RETURN"), cancellable = true)
    private void includeGhastUnderside(double horizontalExpansion, CallbackInfoReturnable<AABB> cir) {
        Mob rider = (Mob) (Object) this;
        if (!(rider.level() instanceof ServerLevel)
            || !(rider instanceof WitherSkeleton)
            || !(rider.getVehicle() instanceof Ghast mount)
            || !mount.entityTags().contains(OverpoweredMobs.CAVALRY_MOUNT_TAG)) return;

        // Keep vanilla horizontal expansion and minimum weapon range. The melee
        // goal still requires the rider's normal line of sight before attacking.
        cir.setReturnValue(GhastCavalryMeleeReach.extendDownToMount(cir.getReturnValue(), mount.getBoundingBox()));
    }
}
