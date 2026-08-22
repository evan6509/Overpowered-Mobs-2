package com.overpoweredmobs;

import com.overpoweredmobs.config.OverpoweredConfig;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.phys.AABB;

public final class CreeperChainHelper {
    public static void triggerNearbyChargedCreepers(Creeper source) {
        OverpoweredConfig config = OverpoweredMobs.getConfig();
        if (!config.isEnableCreeperChainDetonation() || !source.isPowered()) return;
        if (!(source.level() instanceof ServerLevel level)) return;

        double radius = config.getCreeperChainRadius();
        AABB area = AABB.ofSize(source.position(), radius * 2.0, radius * 2.0, radius * 2.0);
        for (Creeper nearby : level.getEntitiesOfClass(Creeper.class, area)) {
            if (nearby == source || !nearby.isAlive() || !nearby.isPowered() || nearby.isIgnited()) continue;
            if (nearby.entityTags().contains(OverpoweredMobs.CHAIN_PRIMED_TAG)) continue;

            nearby.addTag(OverpoweredMobs.CHAIN_PRIMED_TAG);
            nearby.ignite();
            level.sendParticles(net.minecraft.core.particles.ParticleTypes.ELECTRIC_SPARK,
                nearby.getX(), nearby.getY() + 0.8, nearby.getZ(), 8,
                0.35, 0.5, 0.35, 0.05);
        }
    }

    private CreeperChainHelper() {}
}
