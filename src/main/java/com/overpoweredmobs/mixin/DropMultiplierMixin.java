package com.overpoweredmobs.mixin;

import com.overpoweredmobs.OverpoweredMobs;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public class DropMultiplierMixin {
    @Unique
    private static final double DROP_RADIUS = 2.0;

    @Inject(method = "dropAllDeathLoot", at = @At("RETURN"))
    private void afterDropAllDeathLoot(ServerLevel level, DamageSource source, CallbackInfo ci) {
        LivingEntity entity = (LivingEntity) (Object) this;
        if (!(entity instanceof Mob mob)) return;
        if (mob.getType().getCategory() != MobCategory.MONSTER) return;
        if (mob.entityTags().contains(OverpoweredMobs.PINATA_TAG)) return;

        boolean hasArmor = false;
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (slot.getType() == EquipmentSlot.Type.HUMANOID_ARMOR) {
                ItemStack stack = mob.getItemBySlot(slot);
                if (!stack.isEmpty()) {
                    hasArmor = true;
                    break;
                }
            }
        }
        double multiplier = OverpoweredMobs.isElite(mob) ? 3.0 : (hasArmor ? 3.0 : 1.2);

        double mx = entity.getX(), my = entity.getY(), mz = entity.getZ();
        for (ItemEntity item : level.getEntitiesOfClass(ItemEntity.class, entity.getBoundingBox().inflate(DROP_RADIUS))) {
            if (item.isAlive() && item.hasPickUpDelay() && item.distanceToSqr(mx, my, mz) < DROP_RADIUS * DROP_RADIUS) {
                ItemStack stack = item.getItem().copy();
                int extraCount = (int) Math.floor(stack.getCount() * (multiplier - 1.0));
                if (extraCount > 0) {
                    stack.setCount(extraCount);
                    ItemEntity extra = new ItemEntity(level, item.getX(), item.getY(), item.getZ(), stack);
                    extra.setDeltaMovement(item.getDeltaMovement());
                    level.addFreshEntity(extra);
                }
            }
        }
    }
}
