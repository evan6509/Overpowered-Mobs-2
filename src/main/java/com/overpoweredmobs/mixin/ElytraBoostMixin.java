package com.overpoweredmobs.mixin;

import com.overpoweredmobs.OverpoweredMobs;
import com.overpoweredmobs.config.OverpoweredConfig;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public class ElytraBoostMixin {
    @Unique
    private int opm_fireworkCooldown;

    @Inject(method = "tick", at = @At("TAIL"))
    private void updateElytraBoost(CallbackInfo ci) {
        LivingEntity living = (LivingEntity) (Object) this;
        if (!(living instanceof Mob mob)) return;
        if (!(living.level() instanceof ServerLevel level)) return;

        OverpoweredConfig config = OverpoweredMobs.getConfig();
        if (!mob.entityTags().contains(OverpoweredMobs.ELYTRA_TAG)
            || !living.getItemBySlot(EquipmentSlot.CHEST).is(Items.ELYTRA)) {
            return;
        }
        if (!config.isEnableElytraBoost()) {
            ((LivingEntityAccessor) living).opmSetSharedFlag(7, false);
            return;
        }

        boolean wantsToFly = mob.isAlive()
            && mob.getTarget() == null
            && !living.isPassenger()
            && !living.isInWater();
        if (!wantsToFly) {
            ((LivingEntityAccessor) living).opmSetSharedFlag(7, false);
            opm_fireworkCooldown = 0;
            return;
        }

        if (living.onGround()) {
            ((LivingEntityAccessor) living).opmSetSharedFlag(7, false);
            var movement = living.getDeltaMovement();
            living.setDeltaMovement(movement.x, Math.max(movement.y, 0.6), movement.z);
            living.hurtMarked = true;
            opm_fireworkCooldown = 0;
            return;
        }

        ((LivingEntityAccessor) living).opmSetSharedFlag(7, true);
        if (opm_fireworkCooldown > 0) {
            opm_fireworkCooldown--;
            return;
        }

        ItemStack rocketItem = new ItemStack(Items.FIREWORK_ROCKET);
        FireworkRocketEntity rocket = new FireworkRocketEntity(level, rocketItem, living);
        FireworkRocketAccessor accessor = (FireworkRocketAccessor) rocket;
        if (accessor.getAttachedToEntity() != living) {
            accessor.setAttachedToEntity(living);
        }
        level.addFreshEntity(rocket);
        level.sendParticles(ParticleTypes.FIREWORK,
            living.getX(), living.getY(), living.getZ(), 5,
            0.25, 0.25, 0.25, 0.02);
        opm_fireworkCooldown = config.getFireworkBoostInterval();
    }
}
