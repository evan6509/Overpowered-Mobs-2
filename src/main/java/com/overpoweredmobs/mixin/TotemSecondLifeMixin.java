package com.overpoweredmobs.mixin;

import com.overpoweredmobs.OverpoweredMobs;
import com.overpoweredmobs.config.OverpoweredConfig;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.damagesource.DamageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public class TotemSecondLifeMixin {

    @Inject(method = "hurtServer", at = @At("HEAD"), cancellable = true)
    private void saveFromLethalDamage(ServerLevel level, DamageSource source, float amount,
        CallbackInfoReturnable<Boolean> cir) {
        LivingEntity living = (LivingEntity) (Object) this;
        if (!(living instanceof Mob mob)
            || mob.getType().getCategory() != MobCategory.MONSTER
            || !living.isAlive()
            || living.entityTags().contains(OverpoweredMobs.SECOND_LIFE_TAG)) {
            return;
        }

        OverpoweredConfig config = OverpoweredMobs.getConfig();
        if (!config.isEnableTotemSecondLife() || amount < living.getHealth()) return;

        EquipmentSlot slot = findTotemSlot(living);
        if (slot == null) return;

        ItemStack totem = living.getItemBySlot(slot);
        totem.shrink(1);
        living.setHealth(1.0f);
        living.addTag(OverpoweredMobs.SECOND_LIFE_TAG);
        living.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 900, 1));
        living.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 800, 0));
        living.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 100, 1));

        level.sendParticles(ParticleTypes.TOTEM_OF_UNDYING,
            living.getX(), living.getY() + living.getBbHeight() * 0.5, living.getZ(),
            30, 0.6, 0.9, 0.6, 0.1);
        level.playSound(null, living.getX(), living.getY(), living.getZ(),
            SoundEvents.TOTEM_USE, SoundSource.HOSTILE, 1.0f, 1.0f);
        cir.setReturnValue(true);
    }

    private static EquipmentSlot findTotemSlot(LivingEntity living) {
        if (living.getItemBySlot(EquipmentSlot.OFFHAND).is(Items.TOTEM_OF_UNDYING)) {
            return EquipmentSlot.OFFHAND;
        }
        if (living.getItemBySlot(EquipmentSlot.MAINHAND).is(Items.TOTEM_OF_UNDYING)) {
            return EquipmentSlot.MAINHAND;
        }
        return null;
    }
}
