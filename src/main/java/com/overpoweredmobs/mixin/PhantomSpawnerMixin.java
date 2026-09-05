package com.overpoweredmobs.mixin;

import com.overpoweredmobs.OverpoweredMobs;
import com.overpoweredmobs.config.OverpoweredConfig;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.PhantomSpawner;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(PhantomSpawner.class)
public class PhantomSpawnerMixin {

    @Redirect(
        method = "tick",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/util/RandomSource;nextInt(I)I", ordinal = 5)
    )
    private int useConfiguredPackSize(RandomSource random, int vanillaBound) {
        OverpoweredConfig config = OverpoweredMobs.getConfig();
        if (!config.isEnablePhantomPacks()) return random.nextInt(vanillaBound);

        int min = config.getPhantomPackMinSize();
        int max = config.getPhantomPackMaxSize();
        // Vanilla adds one to this random value to obtain the final pack size.
        return min - 1 + random.nextInt(max - min + 1);
    }
}
