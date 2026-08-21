package com.overpoweredmobs.mixin;

import com.overpoweredmobs.OverpoweredMobs;
import com.overpoweredmobs.config.OverpoweredConfig;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public class ExperienceMultiplierMixin {
    @Inject(method = "getExperienceReward", at = @At("RETURN"), cancellable = true)
    private void multiplyExperience(ServerLevel level, Entity attacker, CallbackInfoReturnable<Integer> cir) {
        if (!(((Object) this) instanceof Mob mob)) return;
        if (mob.getType().getCategory() != MobCategory.MONSTER) return;
        if (mob.entityTags().contains(OverpoweredMobs.PINATA_TAG)) {
            cir.setReturnValue(0);
            return;
        }
        int xp = cir.getReturnValueI();
        if (xp <= 0) return;
        OverpoweredConfig config = OverpoweredMobs.getConfig();
        OverpoweredConfig.MobConfig cfg = config.getFor(mob.getType());
        cir.setReturnValue((int) Math.ceil(xp * cfg.xpMultiplier()));
    }
}
