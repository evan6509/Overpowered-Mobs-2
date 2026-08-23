package com.overpoweredmobs.mixin;

import com.overpoweredmobs.OverpoweredMobs;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public class DropMultiplierMixin {
    @Inject(
        method = "spawnAtLocation(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/phys/Vec3;)Lnet/minecraft/world/entity/item/ItemEntity;",
        at = @At("RETURN")
    )
    private void multiplyDeathDrop(ServerLevel level, ItemStack stack, Vec3 offset,
        CallbackInfoReturnable<ItemEntity> cir) {
        Entity entity = (Entity) (Object) this;
        if (!(entity instanceof Mob mob) || !mob.isDeadOrDying()) return;
        if (mob.getType().getCategory() != MobCategory.MONSTER) return;
        if (mob.entityTags().contains(OverpoweredMobs.PINATA_TAG)) return;

        ItemEntity original = cir.getReturnValue();
        if (original == null || stack.isEmpty()) return;

        boolean hasArmor = false;
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (slot.getType() == EquipmentSlot.Type.HUMANOID_ARMOR) {
                ItemStack equipped = mob.getItemBySlot(slot);
                if (!equipped.isEmpty()) {
                    hasArmor = true;
                    break;
                }
            }
        }
        double multiplier = OverpoweredMobs.isElite(mob) ? 3.0 : (hasArmor ? 3.0 : 1.2);

        double exactExtra = stack.getCount() * (multiplier - 1.0);
        int extraCount = (int) Math.floor(exactExtra);
        if (mob.getRandom().nextDouble() < exactExtra - extraCount) {
            extraCount++;
        }

        int maxStackSize = stack.getMaxStackSize();
        while (extraCount > 0) {
            int count = Math.min(extraCount, maxStackSize);
            ItemStack extraStack = stack.copyWithCount(count);
            ItemEntity extra = new ItemEntity(level,
                original.getX(), original.getY(), original.getZ(), extraStack);
            extra.setDefaultPickUpDelay();
            extra.setDeltaMovement(original.getDeltaMovement());
            level.addFreshEntity(extra);
            extraCount -= count;
        }
    }
}
