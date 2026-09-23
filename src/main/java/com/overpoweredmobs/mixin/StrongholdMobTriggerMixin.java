package com.overpoweredmobs.mixin;

import com.overpoweredmobs.OverpoweredMobs;
import com.overpoweredmobs.OverpoweredMobsLogger;
import com.overpoweredmobs.config.OverpoweredConfig;

import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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
    private static final int SPAWN_ATTEMPTS_PER_MOB = 64;
    private static final int VERTICAL_SEARCH_RANGE = 6;

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

        int spawned = 0;
        for (int i = 0; i < count; i++) {
            EntityType<?> type = mobTypes.get(level.getRandom().nextInt(mobTypes.size()));
            Mob mob = (Mob) type.create(level, EntitySpawnReason.TRIGGERED);
            if (mob == null) continue;

            BlockPos spawnPos = findOpenSpawnPosition(level, center, mob);
            if (spawnPos == null) {
                mob.discard();
                continue;
            }

            mob.addTag(OverpoweredMobs.STRONGHOLD_BOOST_TAG);
            mob.finalizeSpawn(level, level.getCurrentDifficultyAt(spawnPos), EntitySpawnReason.TRIGGERED, null);
            // The marker should have been consumed by MobAttributesMixin during finalizeSpawn.
            mob.removeTag(OverpoweredMobs.STRONGHOLD_BOOST_TAG);
            if (level.addFreshEntity(mob)) {
                spawned++;
            } else {
                mob.discard();
            }
        }

        OverpoweredMobsLogger.info("Spawned " + spawned + "/" + count + " stronghold mobs at " + center + " triggered by " + player.getName().getString());
    }

    private static BlockPos findOpenSpawnPosition(ServerLevel level, BlockPos center, Mob mob) {
        for (int attempt = 0; attempt < SPAWN_ATTEMPTS_PER_MOB; attempt++) {
            // Try a broad wave first, then search nearer the player's open corridor.
            int radius = attempt < SPAWN_ATTEMPTS_PER_MOB / 2 ? 24 : 8;
            int x = center.getX() + level.getRandom().nextInt(radius * 2 + 1) - radius;
            int z = center.getZ() + level.getRandom().nextInt(radius * 2 + 1) - radius;

            for (int step = 0; step <= VERTICAL_SEARCH_RANGE * 2; step++) {
                int yOffset = step == 0 ? 0 : (step + 1) / 2 * (step % 2 == 0 ? 1 : -1);
                BlockPos feet = new BlockPos(x, center.getY() + yOffset, z);
                BlockPos floor = feet.below();
                if (!level.isLoaded(feet) || !level.getWorldBorder().isWithinBounds(feet)
                    || !level.getBlockState(feet).isAir()
                    || !level.getBlockState(feet.above()).isAir()
                    || !level.getBlockState(floor).entityCanStandOnFace(level, floor, mob, Direction.UP)) {
                    continue;
                }

                mob.setPos(x + 0.5, feet.getY(), z + 0.5);
                if (level.getWorldBorder().isWithinBounds(mob.getBoundingBox())
                    && level.noCollision(mob, mob.getBoundingBox())) {
                    return feet;
                }
            }
        }
        return null;
    }
}
