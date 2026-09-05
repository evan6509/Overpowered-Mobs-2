package com.overpoweredmobs.mixin;

import com.overpoweredmobs.OverpoweredMobs;
import com.overpoweredmobs.config.OverpoweredConfig;

import net.minecraft.world.entity.monster.Ghast;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(targets = "net.minecraft.world.entity.monster.Ghast$GhastShootFireballGoal")
public class GhastChargeMixin {

    @Shadow
    public int chargeTime;

    @Shadow
    @Final
    private Ghast ghast;

    @Unique
    private double opm_chargeFraction;

    @ModifyConstant(method = "tick", constant = @Constant(intValue = 1, ordinal = 0))
    private int scaleChargeStep(int increment) {
        if (!ghast.entityTags().contains(OverpoweredMobs.BOOSTED_TAG)) return increment;

        OverpoweredConfig config = OverpoweredMobs.getConfig();
        double speedMult = config.getRangedAttackSpeedMultiplier();
        if (speedMult <= 1.0) {
            opm_chargeFraction = 0.0;
            return increment;
        }

        double exactStep = speedMult + opm_chargeFraction;
        int step = (int) Math.floor(exactStep);
        opm_chargeFraction = exactStep - step;

        // Vanilla uses equality checks for its sound and shot, so never skip either tick.
        int nextEvent = chargeTime < 10 ? 10 : 20;
        return Math.min(step, nextEvent - chargeTime);
    }
}
