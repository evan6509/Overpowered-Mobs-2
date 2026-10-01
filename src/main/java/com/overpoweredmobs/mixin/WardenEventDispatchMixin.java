package com.overpoweredmobs.mixin;

import com.overpoweredmobs.OverpoweredMobs;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.GameEventTags;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.GameEventDispatcher;
import net.minecraft.world.level.gameevent.vibrations.VibrationSystem;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameEventDispatcher.class)
public class WardenEventDispatchMixin {
    @Shadow @Final private ServerLevel level;

    @Inject(method = "post", at = @At("TAIL"))
    private void dispatchToExtendedWardens(Holder<GameEvent> event, Vec3 position, GameEvent.Context context, CallbackInfo ci) {
        var config = OverpoweredMobs.getConfig();
        if (!config.isEnableWardenSensorBoost() || !event.is(GameEventTags.WARDEN_CAN_LISTEN)) return;
        int range = (int) Math.ceil(config.getWardenSensorRange());
        int vanillaRadius = event.value().notificationRadius();
        if (range <= vanillaRadius) return;
        BlockPos origin = BlockPos.containing(position);
        // Query entities instead of expanding the section scan, which is cubic in the radius.
        double extent = (range + 1.0) * 2.0;
        for (Warden warden : level.getEntitiesOfClass(Warden.class, AABB.ofSize(position, extent, extent, extent))) {
            if (!warden.isAlive() || !warden.entityTags().contains(OverpoweredMobs.BOOSTED_TAG)) continue;
            var user = warden.getVibrationUser();
            if (!user.isValidVibration(event, context)) continue;
            var listenerPosition = user.getPositionSource().getPosition(level);
            if (listenerPosition.isEmpty()) continue;
            BlockPos listener = BlockPos.containing(listenerPosition.get());
            if (listener.distSqr(origin) > (double) range * range) continue;
            if (insideSections(listener.getX(), origin.getX(), vanillaRadius)
                && insideSections(listener.getY(), origin.getY(), vanillaRadius)
                && insideSections(listener.getZ(), origin.getZ(), vanillaRadius)) continue;
            // Reuse vanilla filtering, occlusion and scheduling; only missed sections are handled.
            new VibrationSystem.Listener(warden).handleGameEvent(level, event, context, position);
        }
    }

    @Unique
    private static boolean insideSections(int coordinate, int origin, int radius) {
        int section = SectionPos.blockToSectionCoord(coordinate);
        return section >= SectionPos.blockToSectionCoord(origin - radius)
            && section <= SectionPos.blockToSectionCoord(origin + radius);
    }
}
