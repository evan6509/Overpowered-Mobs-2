package com.overpoweredmobs.mixin;

import com.overpoweredmobs.OverpoweredMobs;
import com.overpoweredmobs.config.OverpoweredConfig;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.piglin.PiglinAi;
import net.minecraft.world.entity.monster.zombie.ZombifiedPiglin;
import net.minecraft.world.entity.player.Player;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ZombifiedPiglin.class)
public class PiglinHiveMixin {

    @Unique
    private static final int HIVE_CHECK_INTERVAL = 20;

    @Unique
    private int opm_hiveCooldown = 0;

    @Inject(method = "customServerAiStep", at = @At("HEAD"))
    private void onCustomServerAiStep(ServerLevel level, CallbackInfo ci) {
        Mob mob = (Mob) (Object) this;
        if (level.isClientSide()) return;

        OverpoweredConfig config = OverpoweredMobs.getConfig();
        if (!config.isEnablePiglinHive()) return;

        opm_hiveCooldown++;
        if (opm_hiveCooldown < HIVE_CHECK_INTERVAL) return;
        opm_hiveCooldown = 0;

        if (!config.isTestMode() && mob.getRandom().nextDouble() >= config.getPiglinHiveChance()) return;

        Player nearest = level.getNearestPlayer(mob, config.getPiglinHiveRange());
        if (nearest == null) return;

        PiglinAi.angerNearbyPiglins(level, nearest, true);
    }
}
