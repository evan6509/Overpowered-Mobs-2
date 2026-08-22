package com.overpoweredmobs.mixin;

import com.overpoweredmobs.OverpoweredMobs;
import com.overpoweredmobs.config.OverpoweredConfig;
import net.minecraft.world.entity.monster.warden.Warden;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "net.minecraft.world.entity.monster.warden.Warden$VibrationUser")
public class WardenSensorMixin {
    @Shadow
    private Warden this$0;

    @Inject(method = "getListenerRadius", at = @At("RETURN"), cancellable = true)
    private void extendListenerRadius(CallbackInfoReturnable<Integer> cir) {
        OverpoweredConfig config = OverpoweredMobs.getConfig();
        if (!config.isEnableWardenSensorBoost()) return;
        if (!this$0.entityTags().contains(OverpoweredMobs.BOOSTED_TAG)) return;

        cir.setReturnValue((int) Math.ceil(config.getWardenSensorRange()));
    }
}
