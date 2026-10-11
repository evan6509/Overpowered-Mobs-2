package com.overpoweredmobs;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Guardian;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.EvokerFangs;

public final class DamageScalingHelper {
    private DamageScalingHelper() {}

    public static float scaleSpecialAttack(DamageSource source, float damage) {
        if (!(source.getEntity() instanceof Mob owner)
            || !isSpecialAttack(source, owner)) return damage;
        return scaleFixedAttack(owner, damage);
    }

    private static boolean isSpecialAttack(DamageSource source, Mob owner) {
        if (source.getDirectEntity() instanceof Projectile
            || source.getDirectEntity() instanceof EvokerFangs
            || source.is(DamageTypes.SONIC_BOOM)) return true;

        // These paths use fixed damage rather than the boosted ATTACK_DAMAGE attribute.
        // Ordinary melee must remain excluded, especially the Guardian's second beam hit.
        if (owner instanceof EnderDragon) {
            return source.is(DamageTypes.MOB_ATTACK) && source.getDirectEntity() == owner
                || source.is(DamageTypes.INDIRECT_MAGIC) && source.getDirectEntity() instanceof AreaEffectCloud;
        }
        if (owner instanceof Guardian && source.getDirectEntity() == owner) {
            return source.is(DamageTypes.INDIRECT_MAGIC) || source.is(DamageTypes.THORNS);
        }
        return (owner instanceof Creeper || owner instanceof WitherBoss)
            && source.getDirectEntity() == owner
            && (source.is(DamageTypes.EXPLOSION) || source.is(DamageTypes.PLAYER_EXPLOSION));
    }

    public static float scaleFixedAttack(Mob owner, float damage) {
        if (OverpoweredMobs.isExcludedMob(owner.getType())) return damage;
        if (!owner.entityTags().contains(OverpoweredMobs.BOOSTED_TAG)) return damage;
        var config = OverpoweredMobs.getConfig();
        double multiplier = config.getFor(owner.getType()).damageMultiplier()
            * config.getDimensionMultiplier(owner.level().dimension().identifier().toString());
        if (OverpoweredMobs.isElite(owner)) multiplier *= config.getEliteDamageMultiplier();
        return (float) Math.min(Integer.MAX_VALUE, damage * multiplier);
    }
}
