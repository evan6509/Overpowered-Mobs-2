package com.overpoweredmobs.mixin;

import com.overpoweredmobs.OverpoweredMobs;
import net.minecraft.world.entity.monster.skeleton.AbstractSkeleton;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(AbstractSkeleton.class)
public class SkeletonBowCooldownMixin {
    private static final int BOW_DRAW_TICKS = 20;

    @ModifyArg(
        method = "reassessWeaponGoal",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/entity/ai/goal/RangedBowAttackGoal;setMinAttackInterval(I)V"
        ),
        index = 0
    )
    private int scaleBoostedBowCooldown(int vanillaInterval) {
        AbstractSkeleton skeleton = (AbstractSkeleton) (Object) this;
        if (!skeleton.entityTags().contains(OverpoweredMobs.BOOSTED_TAG)) return vanillaInterval;

        double multiplier = OverpoweredMobs.getConfig().getRangedAttackSpeedMultiplier();
        // Vanilla spends 20 ticks drawing the bow in addition to this cooldown.
        int totalAttackTicks = BOW_DRAW_TICKS + vanillaInterval;
        return Math.max(1, (int) Math.round(totalAttackTicks / multiplier) - BOW_DRAW_TICKS);
    }
}
