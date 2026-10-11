package com.overpoweredmobs.mixin;

import com.overpoweredmobs.OverpoweredMobs;
import com.overpoweredmobs.config.OverpoweredConfig;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.projectile.hurtingprojectile.LargeFireball;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LargeFireball.class)
public class LargeFireballMixin {

    @Inject(method = "<init>(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/phys/Vec3;I)V", at = @At("RETURN"))
    private void onInit(CallbackInfo ci) {
        LargeFireball fireball = (LargeFireball) (Object) this;
        Entity owner = fireball.getOwner();
        if (!(owner instanceof Mob mob)) return;
        if (!mob.entityTags().contains(OverpoweredMobs.BOOSTED_TAG)) return;

        OverpoweredConfig config = OverpoweredMobs.getConfig();
        double mult = config.getGhastExplosionMultiplier();
        if (mult == 1.0) return;

        int power = ((LargeFireballAccessor) this).getExplosionPower();
        ((LargeFireballAccessor) this).setExplosionPower((int) Math.ceil(power * mult));
    }

    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
    private void saveExplosionPower(ValueOutput output, CallbackInfo ci) {
        // Vanilla writes a signed byte even though explosionPower is an int.
        // Preserve scaled powers above 127 instead of wrapping on a chunk reload.
        output.putInt("ExplosionPower", ((LargeFireballAccessor) this).getExplosionPower());
    }

    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    private void loadExplosionPower(ValueInput input, CallbackInfo ci) {
        // getIntOr also accepts legacy byte tags. Loading must not scale again.
        ((LargeFireballAccessor) this).setExplosionPower(input.getIntOr("ExplosionPower", 1));
    }
}
