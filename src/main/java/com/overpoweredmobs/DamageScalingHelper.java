package com.overpoweredmobs;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.EvokerFangs;

public final class DamageScalingHelper {
    private DamageScalingHelper() {}

    public static float scaleSpecialAttack(DamageSource source, float damage) {
        if (!(source.getEntity() instanceof Mob owner)
            || !owner.entityTags().contains(OverpoweredMobs.BOOSTED_TAG)
            || !(source.getDirectEntity() instanceof Projectile
                || source.getDirectEntity() instanceof EvokerFangs
                || source.is(DamageTypes.SONIC_BOOM))) return damage;
        var config = OverpoweredMobs.getConfig();
        double multiplier = config.getFor(owner.getType()).damageMultiplier()
            * config.getDimensionMultiplier(owner.level().dimension().identifier().toString());
        if (OverpoweredMobs.isElite(owner)) multiplier *= config.getEliteDamageMultiplier();
        return (float) Math.min(Integer.MAX_VALUE, damage * multiplier);
    }
}
