package com.overpoweredmobs.mixin;

import com.overpoweredmobs.OverpoweredMobs;
import com.overpoweredmobs.config.OverpoweredConfig;

import net.minecraft.world.entity.projectile.ShulkerBullet;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(ShulkerBullet.class)
public class ShulkerBulletMixin {

    @ModifyArg(
        method = "onHitEntity",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/effect/MobEffectInstance;<init>(Lnet/minecraft/core/Holder;I)V"
        ),
        index = 1
    )
    private int scaleLevitationDuration(int duration) {
        OverpoweredConfig config = OverpoweredMobs.getConfig();
        double mult = config.getShulkerLevitationDurationMultiplier();
        return (int) Math.ceil(duration * mult);
    }
}
