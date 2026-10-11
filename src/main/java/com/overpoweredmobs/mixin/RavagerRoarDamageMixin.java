package com.overpoweredmobs.mixin;

import com.overpoweredmobs.DamageScalingHelper;
import net.minecraft.world.entity.monster.Ravager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(Ravager.class)
public class RavagerRoarDamageMixin {
    // The roar shares MOB_ATTACK with melee, so scale its fixed damage at the source.
    @ModifyConstant(method = "roar", constant = @Constant(floatValue = 6.0F))
    private float scaleRoarDamage(float damage) {
        return DamageScalingHelper.scaleFixedAttack((Ravager) (Object) this, damage);
    }
}
