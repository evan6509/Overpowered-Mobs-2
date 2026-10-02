package com.overpoweredmobs.mixin;

import com.overpoweredmobs.OverpoweredMobs;
import com.overpoweredmobs.config.OverpoweredConfig;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EntityReference;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.animal.wolf.Wolf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ServerLevelAccessor;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Wolf.class)
public class WolfMixin {

    @Shadow
    private static EntityDataAccessor<Long> DATA_ANGER_END_TIME;

    @Inject(method = "finalizeSpawn", at = @At("RETURN"))
    private void onFinalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, EntitySpawnReason reason, SpawnGroupData data, CallbackInfoReturnable<SpawnGroupData> cir) {
        Wolf wolf = (Wolf) (Object) this;
        OverpoweredConfig config = OverpoweredMobs.getConfig();
        if (!config.isEnableAngryWolves()) return;
        wolf.getEntityData().set(DATA_ANGER_END_TIME, Long.MAX_VALUE);
    }

    @Inject(method = "aiStep", at = @At("TAIL"))
    private void maintainAnger(CallbackInfo ci) {
        Wolf wolf = (Wolf) (Object) this;
        if (!(wolf.level() instanceof ServerLevel level) || !wolf.isAlive() || wolf.isTame()
            || !OverpoweredMobs.getConfig().isEnableAngryWolves()) return;
        // Vanilla refreshes finite anger timers and clears invalid targets during aiStep.
        wolf.getEntityData().set(DATA_ANGER_END_TIME, Long.MAX_VALUE);
        if (wolf.getTarget() instanceof Player player && player.isAlive()
            && !player.isCreative() && !player.isSpectator()) return;
        Player nearest = null;
        double nearestDistance = 64.0 * 64.0;
        for (Player player : level.players()) {
            if (!player.isAlive() || player.isCreative() || player.isSpectator()) continue;
            double distance = wolf.distanceToSqr(player);
            if (distance < nearestDistance) {
                nearest = player;
                nearestDistance = distance;
            }
        }
        wolf.setPersistentAngerTarget(nearest == null ? null : EntityReference.of(nearest));
        if (wolf.getTarget() instanceof Player) wolf.setTarget(null);
    }
}
