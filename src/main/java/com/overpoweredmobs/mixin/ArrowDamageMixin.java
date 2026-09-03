package com.overpoweredmobs.mixin;

import com.overpoweredmobs.EquipmentHelper;
import com.overpoweredmobs.OverpoweredMobs;
import com.overpoweredmobs.config.OverpoweredConfig;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(AbstractArrow.class)
public class ArrowDamageMixin {

    @ModifyArg(
        method = "onHitEntity",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/entity/Entity;hurtOrSimulate(Lnet/minecraft/world/damagesource/DamageSource;F)Z"
        ),
        index = 1
    )
    private float scaleBoostedMobArrowDamage(float damage) {
        AbstractArrow arrow = (AbstractArrow) (Object) this;
        if (!(arrow.getOwner() instanceof Mob owner)
            || !owner.entityTags().contains(OverpoweredMobs.BOOSTED_TAG)
            || !EquipmentHelper.isRangedMob(owner.getType())) {
            return damage;
        }

        OverpoweredConfig config = OverpoweredMobs.getConfig();
        double multiplier = config.getFor(owner.getType()).damageMultiplier()
            * config.getDimensionMultiplier(owner.level().dimension().identifier().toString());
        if (owner.entityTags().contains(OverpoweredMobs.ELITE_TAG)) {
            multiplier *= config.getEliteDamageMultiplier();
        }

        return (float) Math.min(Integer.MAX_VALUE, damage * multiplier);
    }
}
