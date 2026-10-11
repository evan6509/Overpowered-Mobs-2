package com.overpoweredmobs.mixin;

import com.overpoweredmobs.DragonExperienceHelper;
import com.overpoweredmobs.OverpoweredMobs;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EnderDragon.class)
public class DragonExperienceMixin {
    @Unique
    private static final String OPM_EXPERIENCE_KEY = "opm_dragon_experience";

    @Unique
    private int opm$scaledExperience = -1;

    @Unique
    private int opm$getScaledExperience() {
        if (opm$scaledExperience >= 0) return opm$scaledExperience;

        EnderDragon dragon = (EnderDragon) (Object) this;
        if (dragon.entityTags().contains(OverpoweredMobs.PINATA_TAG)) {
            opm$scaledExperience = 0;
        } else {
            var fight = dragon.getDragonFight();
            int vanillaExperience = fight != null && !fight.hasPreviouslyKilledDragon() ? 12000 : 500;
            double multiplier = OverpoweredMobs.getConfig().getFor(dragon.getType()).xpMultiplier();
            opm$scaledExperience = DragonExperienceHelper.scaleTotal(vanillaExperience, multiplier);
        }
        return opm$scaledExperience;
    }

    // Keep vanilla's timing and doMobLoot guards; only change each award's XP amount.
    @ModifyArg(
        method = "tickDeath",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/entity/ExperienceOrb;award(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/phys/Vec3;I)V",
            ordinal = 0
        ),
        index = 2
    )
    private int scaleExperienceBurst(int vanillaExperience) {
        return DragonExperienceHelper.burstExperience(opm$getScaledExperience());
    }

    @ModifyArg(
        method = "tickDeath",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/entity/ExperienceOrb;award(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/phys/Vec3;I)V",
            ordinal = 1
        ),
        index = 2
    )
    private int scaleFinalExperience(int vanillaExperience) {
        return DragonExperienceHelper.finalExperience(opm$getScaledExperience());
    }

    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
    private void saveExperienceTotal(ValueOutput output, CallbackInfo ci) {
        if (opm$scaledExperience >= 0) output.putInt(OPM_EXPERIENCE_KEY, opm$scaledExperience);
    }

    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    private void loadExperienceTotal(ValueInput input, CallbackInfo ci) {
        opm$scaledExperience = Math.max(-1, input.getIntOr(OPM_EXPERIENCE_KEY, -1));
    }
}
