package com.overpoweredmobs.mixin.client;

import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntityRenderer.class)
public abstract class CavalryRiderRendererMixin {

    @Inject(
        method = "extractRenderState(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;F)V",
        at = @At("TAIL")
    )
    private void stabilizeMobPassengerRotation(
        LivingEntity entity,
        LivingEntityRenderState state,
        float partialTick,
        CallbackInfo ci
    ) {
        if (!(entity instanceof Mob) || !(entity.getVehicle() instanceof Mob mount)) return;

        state.bodyRot = Mth.rotLerp(partialTick, mount.yBodyRotO, mount.yBodyRot);
        state.yRot = 0.0f;
        state.xRot = mount.getXRot(partialTick);
    }
}
