package com.overpoweredmobs.mixin;

import com.overpoweredmobs.DamageScalingHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(LivingEntity.class)
public class SpecialAttackDamageMixin {
    // All projectile paths, including ThrownTrident's override, converge on the victim.
    @ModifyVariable(method = "hurtServer", at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private float scaleSpecialDamage(float damage, ServerLevel level, DamageSource source, float originalDamage) {
        return DamageScalingHelper.scaleSpecialAttack(source, damage);
    }
}
