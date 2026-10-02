package com.overpoweredmobs.mixin;

import com.overpoweredmobs.OverpoweredMobs;
import com.overpoweredmobs.EndermanAggroGoal;
import com.overpoweredmobs.EndermanTeleportStrikeGoal;
import com.overpoweredmobs.config.OverpoweredConfig;

import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EnderMan.class)
public abstract class EndermanMixin extends Monster {

    protected EndermanMixin(EntityType<? extends Monster> type, Level level) {
        super(type, level);
    }

    @Inject(method = "registerGoals", at = @At("TAIL"))
    private void addEndermanGoals(CallbackInfo ci) {
        EnderMan enderman = (EnderMan) (Object) this;
        enderman.getGoalSelector().addGoal(1, new EndermanTeleportStrikeGoal(enderman));
        // Run ahead of vanilla stare-based targeting; install for every Enderman,
        // including unboosted mobs and entities loaded from saved chunks.
        targetSelector.addGoal(0, new EndermanAggroGoal(enderman));
    }

    @Inject(method = "isSensitiveToWater", at = @At("HEAD"), cancellable = true)
    private void onIsSensitiveToWater(CallbackInfoReturnable<Boolean> cir) {
        OverpoweredConfig config = OverpoweredMobs.getConfig();
        if (config.isEnableWaterEndermen()) {
            cir.setReturnValue(false);
        }
    }
}
