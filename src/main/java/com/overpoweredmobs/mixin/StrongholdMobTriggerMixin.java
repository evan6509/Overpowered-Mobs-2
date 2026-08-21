package com.overpoweredmobs.mixin;

import com.overpoweredmobs.OverpoweredMobs;
import com.overpoweredmobs.OverpoweredMobsLogger;
import com.overpoweredmobs.config.OverpoweredConfig;

import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(PlayerAdvancements.class)
public class StrongholdMobTriggerMixin {

    @Shadow
    private ServerPlayer player;

    private static final Identifier EYE_SPY = Identifier.withDefaultNamespace("story/follow_ender_eye");

    @Inject(method = "award", at = @At("RETURN"))
    private void onAward(AdvancementHolder advancement, String criterionKey, CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValueZ()) return;
        if (!advancement.id().equals(EYE_SPY)) return;

        OverpoweredConfig config = OverpoweredMobs.getConfig();
        if (!config.isEnableStrongholdMobs()) return;

        ServerLevel level = (ServerLevel) player.level();
        BlockPos center = player.blockPosition();
        int count = config.getStrongholdMobCount();

        List<EntityType<?>> mobTypes = List.of(
            BuiltInRegistries.ENTITY_TYPE.getValue(Identifier.tryParse("minecraft:zombie")),
            BuiltInRegistries.ENTITY_TYPE.getValue(Identifier.tryParse("minecraft:skeleton")),
            BuiltInRegistries.ENTITY_TYPE.getValue(Identifier.tryParse("minecraft:spider")),
            BuiltInRegistries.ENTITY_TYPE.getValue(Identifier.tryParse("minecraft:creeper"))
        );

        for (int i = 0; i < count; i++) {
            EntityType<?> type = mobTypes.get(level.getRandom().nextInt(mobTypes.size()));
            Mob mob = (Mob) type.create(level, EntitySpawnReason.TRIGGERED);
            if (mob == null) continue;

            double ox = (level.getRandom().nextDouble() - 0.5) * 50;
            double oz = (level.getRandom().nextDouble() - 0.5) * 50;

            mob.setPos(center.getX() + 0.5 + ox, center.getY(), center.getZ() + 0.5 + oz);
            mob.finalizeSpawn(level, level.getCurrentDifficultyAt(center), EntitySpawnReason.TRIGGERED, null);
            level.addFreshEntity(mob);
        }

        OverpoweredMobsLogger.info("Spawned " + count + " stronghold mobs at " + center + " triggered by " + player.getName().getString());
    }
}
