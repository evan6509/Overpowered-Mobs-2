package com.overpoweredmobs.mixin;

import com.overpoweredmobs.CavalryHelper;
import com.overpoweredmobs.DistanceSpeedGoal;
import com.overpoweredmobs.EquipmentHelper;
import com.overpoweredmobs.MobSpawnHelper;
import com.overpoweredmobs.OverpoweredMobs;
import com.overpoweredmobs.OverpoweredMobsLogger;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.ConversionParams;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Mob.class)
public class MobAttributesMixin {
    @Unique
    private boolean opmDistanceGoalInstalled;

    @Inject(method = "dropCustomDeathLoot", at = @At("HEAD"))
    private void preventBoostedEquipmentDrops(ServerLevel level, DamageSource source,
        boolean killedByPlayer, CallbackInfo ci) {
        EquipmentHelper.suppressEquipmentDrops((Mob) (Object) this);
    }

    @Inject(method = "getControllingPassenger", at = @At("HEAD"), cancellable = true)
    private void ignoreCavalryMobPassenger(CallbackInfoReturnable<LivingEntity> cir) {
        Mob mount = (Mob) (Object) this;
        if (mount.entityTags().contains(OverpoweredMobs.CAVALRY_MOUNT_TAG)
            && mount.getFirstPassenger() instanceof Mob) cir.setReturnValue(null);
    }

    @Inject(method = "convertTo(Lnet/minecraft/world/entity/EntityType;Lnet/minecraft/world/entity/ConversionParams;Lnet/minecraft/world/entity/EntitySpawnReason;Lnet/minecraft/world/entity/ConversionParams$AfterConversion;)Lnet/minecraft/world/entity/Mob;", at = @At("RETURN"))
    private void restoreConvertedStats(EntityType<?> type, ConversionParams params, EntitySpawnReason reason,
        ConversionParams.AfterConversion<?> callback, CallbackInfoReturnable<Mob> cir) {
        MobSpawnHelper.restoreConversion((Mob) (Object) this, cir.getReturnValue());
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void tickMobSystems(CallbackInfo ci) {
        Mob mob = (Mob) (Object) this;
        if (mob.level() instanceof ServerLevel level && mob.isAlive()) {
            MobSpawnHelper.initialize(mob, level);
            if (!opmDistanceGoalInstalled && OverpoweredMobs.getConfig().isEnableDistanceSpeed()
                && mob.entityTags().contains(OverpoweredMobs.BOOSTED_TAG)
                && (EquipmentHelper.isEquippable(mob.getType()) || mob instanceof Creeper)) {
                mob.getGoalSelector().addGoal(3, new DistanceSpeedGoal(mob));
                opmDistanceGoalInstalled = true;
            }
            if (mob.entityTags().contains(OverpoweredMobs.LEGACY_ELYTRA_TAG)) {
                mob.stopFallFlying();
                mob.removeTag(OverpoweredMobs.LEGACY_ELYTRA_TAG);
                if (mob.getItemBySlot(EquipmentSlot.CHEST).is(Items.ELYTRA)) {
                    mob.setItemSlot(EquipmentSlot.CHEST, ItemStack.EMPTY);
                    if (OverpoweredMobs.getConfig().isEnableGear()) EquipmentHelper.equipOPGear(mob, level.registryAccess());
                }
                OverpoweredMobsLogger.info("Removed legacy elytra gear from " + mob.getType());
            }
        }
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void stabilizeCavalryAfterAi(CallbackInfo ci) {
        // Movement/look controls and body rotation run inside LivingEntity.tick.
        // Applying this at HEAD lets them overwrite the rider's synchronized rotation.
        CavalryHelper.tick((Mob) (Object) this);
    }
}
