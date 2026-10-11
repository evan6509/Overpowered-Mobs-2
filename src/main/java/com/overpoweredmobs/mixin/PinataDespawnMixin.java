package com.overpoweredmobs.mixin;

import com.overpoweredmobs.OverpoweredMobs;
import com.overpoweredmobs.PinataLifetime;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ConversionParams;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Mob.class)
public class PinataDespawnMixin implements PinataLifetime {
    @Unique
    private static final int OPM_PINATA_LIFETIME_TICKS = 600;

    @Unique
    private static final String OPM_PINATA_AGE_KEY = "opm_pinata_age_ticks";

    @Unique
    private int opmPinataAgeTicks;

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void onTick(CallbackInfo ci) {
        Mob mob = (Mob) (Object) this;
        if (!(mob.level() instanceof ServerLevel)
            || !mob.entityTags().contains(OverpoweredMobs.PINATA_TAG)) return;
        // Count active ticks as before, but unloading must not reset elapsed lifetime.
        if (++opmPinataAgeTicks >= OPM_PINATA_LIFETIME_TICKS) {
            mob.remove(Entity.RemovalReason.DISCARDED);
            ci.cancel();
        }
    }

    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
    private void savePinataAge(ValueOutput output, CallbackInfo ci) {
        Mob mob = (Mob) (Object) this;
        if (mob.entityTags().contains(OverpoweredMobs.PINATA_TAG)) {
            output.putInt(OPM_PINATA_AGE_KEY, opmPinataAgeTicks);
        }
    }

    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    private void loadPinataAge(ValueInput input, CallbackInfo ci) {
        // Existing tagged babies have no saved age; initialize it once, then persist it.
        opm$setPinataAgeTicks(input.getIntOr(OPM_PINATA_AGE_KEY, 0));
    }

    @Inject(method = "convertTo(Lnet/minecraft/world/entity/EntityType;Lnet/minecraft/world/entity/ConversionParams;Lnet/minecraft/world/entity/EntitySpawnReason;Lnet/minecraft/world/entity/ConversionParams$AfterConversion;)Lnet/minecraft/world/entity/Mob;", at = @At("RETURN"))
    private void preserveConvertedPinataAge(EntityType<?> type, ConversionParams params,
        EntitySpawnReason reason, ConversionParams.AfterConversion<?> callback,
        CallbackInfoReturnable<Mob> cir) {
        Mob source = (Mob) (Object) this;
        Mob converted = cir.getReturnValue();
        if (converted == null || !source.entityTags().contains(OverpoweredMobs.PINATA_TAG)) return;
        converted.addTag(OverpoweredMobs.PINATA_TAG);
        ((PinataLifetime) converted).opm$setPinataAgeTicks(opmPinataAgeTicks);
    }

    @Override
    public int opm$getPinataAgeTicks() {
        return opmPinataAgeTicks;
    }

    @Override
    public void opm$setPinataAgeTicks(int ticks) {
        opmPinataAgeTicks = Math.clamp(ticks, 0, OPM_PINATA_LIFETIME_TICKS);
    }
}
