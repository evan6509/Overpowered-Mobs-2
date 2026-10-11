package com.overpoweredmobs.mixin;

import com.overpoweredmobs.DamageScalingHelper;
import net.minecraft.world.entity.monster.cubemob.MagmaCube;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(MagmaCube.class)
public class MagmaCubeDamageMixin {
    // The attribute component is already scaled; only the vanilla +2 remains fixed.
    @ModifyConstant(method = "getAttackDamage", constant = @Constant(floatValue = 2.0F))
    private float scaleDamageBonus(float damage) {
        return DamageScalingHelper.scaleFixedAttack((MagmaCube) (Object) this, damage);
    }
}
