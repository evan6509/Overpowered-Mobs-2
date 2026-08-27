package com.overpoweredmobs.mixin;

import com.overpoweredmobs.BloodMoonManager;
import com.overpoweredmobs.CavalryHelper;
import com.overpoweredmobs.CreeperHelper;
import com.overpoweredmobs.DistanceSpeedGoal;
import com.overpoweredmobs.EquipmentHelper;
import com.overpoweredmobs.OverpoweredMobs;
import com.overpoweredmobs.OverpoweredMobsLogger;
import com.overpoweredmobs.config.OverpoweredConfig;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.level.ServerLevelAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Mob.class)
public class MobAttributesMixin {

    @Inject(method = "getControllingPassenger", at = @At("HEAD"), cancellable = true)
    private void ignoreCavalryMobPassenger(CallbackInfoReturnable<LivingEntity> cir) {
        Mob mount = (Mob) (Object) this;
        if (mount.entityTags().contains(OverpoweredMobs.CAVALRY_MOUNT_TAG)
            && mount.getFirstPassenger() instanceof Mob) {
            cir.setReturnValue(null);
        }
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void syncCavalryRotation(CallbackInfo ci) {
        CavalryHelper.tick((Mob) (Object) this);
    }

    @Inject(method = "finalizeSpawn", at = @At("RETURN"))
    private void onFinalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, EntitySpawnReason reason, SpawnGroupData spawnData, CallbackInfoReturnable<SpawnGroupData> cir) {
        Mob mob = (Mob) (Object) this;
        if (mob.getType().getCategory() != MobCategory.MONSTER) return;

        if (mob.entityTags().contains(OverpoweredMobs.CAVALRY_MOUNT_TAG)
            || mob.entityTags().contains(OverpoweredMobs.HORDE_TAG)) return;

        OverpoweredMobsLogger.info("finalizeSpawn for " + mob.getType() + " at " + mob.blockPosition() + " reason=" + reason);

        OverpoweredConfig config = OverpoweredMobs.getConfig();

        double effectiveSpawnChance = config.getSpawnChanceFor(mob.getType());
        if (BloodMoonManager.shouldForceHorde(mob)
            || (!config.isTestMode() && mob.getRandom().nextDouble() >= effectiveSpawnChance)) {
            OverpoweredMobsLogger.info("  -> horde mode (spawnChance roll failed)");
            applyHordeBuffs(mob, config);
            return;
        }

        if (mob instanceof Creeper creeper) {
            if (config.isTestMode() || mob.getRandom().nextDouble() < config.getChargedCreeperChance()) {
                CreeperHelper.setPowered(creeper);
            }
        }

        OverpoweredMobs.applyBoosts(mob);
        OverpoweredMobs.tryApplyElite(mob);

        if (level instanceof ServerLevel serverLevel) {
            if (config.isEnableDistanceSpeed()
                && (EquipmentHelper.isEquippable(mob.getType()) || mob instanceof Creeper)) {
                mob.getGoalSelector().addGoal(3, new DistanceSpeedGoal(mob,
                    config.getAggroCloseSpeed(),
                    config.getAggroFarSpeed(),
                    config.getAggroSlowRange()));
            }

            if (config.isEnableAlertSound() && EquipmentHelper.isEquippable(mob.getType())) {
                double rangeSq = config.getBossBarRange() * config.getBossBarRange();
                if (OverpoweredMobs.isHostileNearby(serverLevel, mob, rangeSq)) {
                    serverLevel.playSound(null, mob.getX(), mob.getY(), mob.getZ(),
                        SoundEvents.WITHER_SPAWN, SoundSource.HOSTILE, 1.0f, 1.0f);
                }
            }

            if (config.isEnableGear()) {
                equipGear(mob, serverLevel);
            }
            if (config.isEnableCavalry()) {
                trySpawnCavalry(mob, serverLevel, difficulty, reason);
            }
        }
    }

    @Unique
    private static void applyHordeBuffs(Mob mob, OverpoweredConfig config) {
        double speedMult = config.getHordeSpeedMultiplier();
        double followMult = config.getHordeFollowRangeMultiplier();
        if (speedMult != 1.0) {
            var speed = mob.getAttribute(Attributes.MOVEMENT_SPEED);
            if (speed != null) speed.setBaseValue(speed.getBaseValue() * speedMult);
        }
        if (followMult != 1.0) {
            var follow = mob.getAttribute(Attributes.FOLLOW_RANGE);
            if (follow != null) follow.setBaseValue(follow.getBaseValue() * followMult);
        }
        mob.addTag(OverpoweredMobs.HORDE_TAG);
        OverpoweredMobsLogger.info("  -> horde speed=" + speedMult + " followRange=" + followMult);
    }

    @Unique
    private static void equipGear(Mob mob, ServerLevel level) {
        level.getServer().execute(() -> {
            if (!mob.isAlive()) return;
            OverpoweredMobsLogger.info("  -> equipping gear (deferred) for " + mob.getType());
            EquipmentHelper.equipOPGear(mob, level.registryAccess());
        });
    }

    @Unique
    private static void trySpawnCavalry(Mob rider, ServerLevel level, DifficultyInstance difficulty, EntitySpawnReason reason) {
        Identifier riderKey = BuiltInRegistries.ENTITY_TYPE.getKey(rider.getType());
        if (riderKey == null) return;

        String riderId = riderKey.toString();
        for (OverpoweredConfig.CavalryEntry entry : OverpoweredMobs.getConfig().getCavalry()) {
            if (!entry.rider().equals(riderId)) continue;
            if (!OverpoweredMobs.getConfig().isTestMode() && rider.getRandom().nextDouble() >= entry.chance()) continue;

            Identifier mountKey = Identifier.tryParse(entry.mount());
            if (mountKey == null) continue;
            EntityType<?> mountType = BuiltInRegistries.ENTITY_TYPE.getValue(mountKey);
            if (mountType == null) continue;

            var mountEntity = mountType.create(level, EntitySpawnReason.JOCKEY);
            if (!(mountEntity instanceof Mob mount)) {
                OverpoweredMobsLogger.warn("  -> invalid cavalry mount (not a mob): " + entry.mount());
                continue;
            }

            mount.setPos(rider.getX(), rider.getY(), rider.getZ());
            mount.addTag(OverpoweredMobs.CAVALRY_MOUNT_TAG);
            mount.finalizeSpawn(level, difficulty, EntitySpawnReason.JOCKEY, null);
            level.addFreshEntity(mount);

            if (entry.baby() && rider instanceof Zombie zombie) {
                zombie.setBaby(true);
            }

            if (CavalryHelper.attachRider(rider, mount)) {
                OverpoweredMobsLogger.info("  -> cavalry: " + riderId + " riding " + entry.mount());
                return;
            }

            OverpoweredMobsLogger.warn("  -> failed to attach cavalry rider to " + entry.mount());
            mount.discard();
        }
    }
}
