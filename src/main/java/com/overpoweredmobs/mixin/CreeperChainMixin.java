package com.overpoweredmobs.mixin;

import com.overpoweredmobs.CreeperChainHelper;
import net.minecraft.world.entity.monster.Creeper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Creeper.class)
public class CreeperChainMixin {

    @Inject(method = "explodeCreeper", at = @At("HEAD"))
    private void beforeExplode(CallbackInfo ci) {
        CreeperChainHelper.triggerNearbyChargedCreepers((Creeper) (Object) this);
    }
}
