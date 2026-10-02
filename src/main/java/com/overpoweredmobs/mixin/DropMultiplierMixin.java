package com.overpoweredmobs.mixin;

import com.overpoweredmobs.OverpoweredMobs;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.ItemStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.function.Consumer;

@Mixin(LivingEntity.class)
public class DropMultiplierMixin {
    @ModifyVariable(
        method = "dropFromLootTable(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/damagesource/DamageSource;ZLnet/minecraft/resources/ResourceKey;Ljava/util/function/Consumer;)V",
        at = @At("HEAD"),
        argsOnly = true
    )
    private Consumer<ItemStack> multiplyLootTableDrops(Consumer<ItemStack> drop) {
        if (!((Object) this instanceof Mob mob) || !mob.isDeadOrDying()) return drop;
        if (mob.getType().getCategory() != MobCategory.MONSTER) return drop;
        if (mob.entityTags().contains(OverpoweredMobs.PINATA_TAG)) return drop;

        boolean hasArmor = false;
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (slot.getType() == EquipmentSlot.Type.HUMANOID_ARMOR
                && !mob.getItemBySlot(slot).isEmpty()) {
                hasArmor = true;
                break;
            }
        }
        double multiplier = OverpoweredMobs.isElite(mob) ? 3.0 : (hasArmor ? 3.0 : 1.2);

        // Only loot-table output is multiplied; equipped and picked-up items are separate.
        return stack -> {
            ItemStack extraTemplate = stack.copy();
            drop.accept(stack);
            if (extraTemplate.isEmpty()) return;

            double exactExtra = extraTemplate.getCount() * (multiplier - 1.0);
            int extraCount = (int) Math.floor(exactExtra);
            if (mob.getRandom().nextDouble() < exactExtra - extraCount) extraCount++;

            int maxStackSize = extraTemplate.getMaxStackSize();
            while (extraCount > 0) {
                int count = Math.min(extraCount, maxStackSize);
                drop.accept(extraTemplate.copyWithCount(count));
                extraCount -= count;
            }
        };
    }
}
