package com.overpoweredmobs;

import com.overpoweredmobs.config.OverpoweredConfig;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.skeleton.AbstractSkeleton;
import net.minecraft.world.entity.monster.zombie.Zombie;

public final class MobSpawnHelper {
    private MobSpawnHelper() {}

    // First tick reaches overrides that never call Mob.finalizeSpawn and runs after sizing.
    public static void initialize(Mob mob, ServerLevel level) {
        if (mob.getType().getCategory() != MobCategory.MONSTER
            || mob.entityTags().contains(OverpoweredMobs.CAVALRY_MOUNT_TAG)
            || mob.entityTags().contains(OverpoweredMobs.BOOSTED_TAG)
            || mob.entityTags().contains(OverpoweredMobs.HORDE_TAG)) return;
        OverpoweredConfig config = OverpoweredMobs.getConfig();
        boolean stronghold = mob.entityTags().contains(OverpoweredMobs.STRONGHOLD_BOOST_TAG);
        mob.removeTag(OverpoweredMobs.STRONGHOLD_BOOST_TAG);
        if (!stronghold && (BloodMoonManager.shouldForceHorde(mob)
            || (!config.isTestMode() && mob.getRandom().nextDouble() >= config.getSpawnChanceFor(mob.getType())))) {
            applyHorde(mob);
            return;
        }
        if (mob instanceof Creeper creeper
            && (config.isTestMode() || mob.getRandom().nextDouble() < config.getChargedCreeperChance())) {
            CreeperHelper.setPowered(creeper);
        }
        OverpoweredMobs.applyBoosts(mob);
        OverpoweredMobs.tryApplyElite(mob);
        if (config.isEnableGear()) EquipmentHelper.equipOPGear(mob, level.registryAccess());
        if (mob instanceof AbstractSkeleton skeleton) skeleton.reassessWeaponGoal();
        if (config.isEnableAlertSound() && EquipmentHelper.isEquippable(mob.getType())
            && OverpoweredMobs.isHostileNearby(level, mob, config.getBossBarRange() * config.getBossBarRange())) {
            level.playSound(null, mob.getX(), mob.getY(), mob.getZ(),
                SoundEvents.WITHER_SPAWN, SoundSource.HOSTILE, 1.0f, 1.0f);
        }
        if (config.isEnableCavalry() && !stronghold) trySpawnCavalry(mob, level);
    }

    public static void restoreConversion(Mob source, Mob converted) {
        if (converted == null || converted.getType().getCategory() != MobCategory.MONSTER
            || converted.entityTags().contains(OverpoweredMobs.CAVALRY_MOUNT_TAG)) return;
        boolean boosted = source.entityTags().contains(OverpoweredMobs.BOOSTED_TAG);
        boolean horde = source.entityTags().contains(OverpoweredMobs.HORDE_TAG);
        if (!boosted && !horde) return;
        // Conversion copies tags and equipment but creates fresh, species-specific attributes.
        float healthFraction = source.getHealth() > 0 ? source.getHealth() / source.getMaxHealth() : 1.0f;
        converted.removeTag(OverpoweredMobs.BOOSTED_TAG);
        converted.removeTag(OverpoweredMobs.HORDE_TAG);
        converted.removeTag(OverpoweredMobs.ELITE_TAG);
        if (boosted) {
            OverpoweredMobs.applyBoosts(converted);
            if (OverpoweredMobs.isElite(source)) OverpoweredMobs.applyElite(converted);
        } else {
            applyHorde(converted);
        }
        converted.setHealth(converted.getMaxHealth() * healthFraction);
        if (converted instanceof AbstractSkeleton skeleton) skeleton.reassessWeaponGoal();
    }

    private static void applyHorde(Mob mob) {
        OverpoweredConfig config = OverpoweredMobs.getConfig();
        var speed = mob.getAttribute(Attributes.MOVEMENT_SPEED);
        Identifier modifier = Identifier.fromNamespaceAndPath(OverpoweredMobs.MOD_ID, "horde_stats");
        if (speed != null) speed.addOrReplacePermanentModifier(new AttributeModifier(modifier,
            config.getHordeSpeedMultiplier() - 1.0, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
        var follow = mob.getAttribute(Attributes.FOLLOW_RANGE);
        if (follow != null) follow.addOrReplacePermanentModifier(new AttributeModifier(modifier,
            config.getHordeFollowRangeMultiplier() - 1.0, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
        mob.addTag(OverpoweredMobs.HORDE_TAG);
    }

    private static void trySpawnCavalry(Mob rider, ServerLevel level) {
        if (rider.isPassenger()) return;
        Identifier riderKey = BuiltInRegistries.ENTITY_TYPE.getKey(rider.getType());
        if (riderKey == null) return;
        OverpoweredConfig config = OverpoweredMobs.getConfig();
        for (OverpoweredConfig.CavalryEntry entry : config.getCavalry()) {
            if (!entry.rider().equals(riderKey.toString())) continue;
            if (!config.isTestMode() && rider.getRandom().nextDouble() >= entry.chance()) continue;
            Identifier mountKey = Identifier.tryParse(entry.mount());
            EntityType<?> mountType = mountKey == null ? null : BuiltInRegistries.ENTITY_TYPE.getOptional(mountKey).orElse(null);
            if (mountType == null) continue;
            var entity = mountType.create(level, EntitySpawnReason.JOCKEY);
            if (!(entity instanceof Mob mount)) continue;
            mount.setPos(rider.position());
            mount.addTag(OverpoweredMobs.CAVALRY_MOUNT_TAG);
            mount.finalizeSpawn(level, level.getCurrentDifficultyAt(mount.blockPosition()), EntitySpawnReason.JOCKEY, null);
            boolean wasBaby = rider instanceof Zombie zombie && zombie.isBaby();
            if (entry.baby() && rider instanceof Zombie zombie) zombie.setBaby(true);
            if (!SpawnSafety.canFitCavalry(level, rider, mount) || !level.addFreshEntity(mount)) {
                mount.discard();
                if (rider instanceof Zombie zombie) zombie.setBaby(wasBaby);
                continue;
            }
            if (CavalryHelper.attachRider(rider, mount)) return;
            mount.discard();
            if (rider instanceof Zombie zombie) zombie.setBaby(wasBaby);
        }
    }
}
