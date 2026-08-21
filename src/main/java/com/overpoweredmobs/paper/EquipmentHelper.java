package com.overpoweredmobs.paper;

import com.overpoweredmobs.paper.config.OverpoweredConfig;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;

import java.util.Set;

public final class EquipmentHelper {
    private static final Set<EntityType> NO_EQUIP = Set.of(
        EntityType.CREEPER, EntityType.SPIDER, EntityType.CAVE_SPIDER, EntityType.SLIME,
        EntityType.MAGMA_CUBE, EntityType.ENDERMAN, EntityType.SILVERFISH, EntityType.ENDERMITE,
        EntityType.BLAZE, EntityType.GHAST, EntityType.GUARDIAN, EntityType.ELDER_GUARDIAN,
        EntityType.WITCH, EntityType.PHANTOM
    );

    private EquipmentHelper() { }

    public static boolean isEquippable(EntityType type) { return !NO_EQUIP.contains(type); }

    public static boolean isPiglin(EntityType type) {
        return type == EntityType.PIGLIN || type == EntityType.PIGLIN_BRUTE || type == EntityType.ZOMBIFIED_PIGLIN;
    }

    public static boolean isRanged(EntityType type) {
        return type == EntityType.SKELETON || type == EntityType.STRAY || type == EntityType.BOGGED ||
            "minecraft:parched".equals(type.getKey().toString());
    }

    public static void equip(Mob mob, OverpoweredConfig config) {
        if (!isEquippable(mob.getType())) return;
        EntityEquipment equipment = mob.getEquipment();
        if (equipment == null) return;

        boolean piglin = isPiglin(mob.getType());
        boolean brute = mob.getType() == EntityType.PIGLIN_BRUTE;
        if (!mob.getScoreboardTags().contains(OverpoweredMobsPlugin.PINATA_TAG)
            && (!brute || config.isTestMode() || Math.random() < config.getPiglinBruteGearChance())) {
            Material helmet = piglin ? Material.GOLDEN_HELMET : Material.NETHERITE_HELMET;
            Material chest = piglin ? Material.GOLDEN_CHESTPLATE : Material.NETHERITE_CHESTPLATE;
            Material legs = piglin ? Material.GOLDEN_LEGGINGS : Material.NETHERITE_LEGGINGS;
            Material boots = piglin ? Material.GOLDEN_BOOTS : Material.NETHERITE_BOOTS;
            equipment.setHelmet(enchanted(helmet, "protection", 10));
            equipment.setChestplate(enchanted(chest, "protection", 10));
            equipment.setLeggings(enchanted(legs, "protection", 10));
            equipment.setBoots(enchanted(boots, "protection", 10));
        }

        OverpoweredConfig.MobConfig mobConfig = config.getFor(mob.getType().getKey().toString());
            Material weapon = mobConfig.weapon() == null ? null : Material.matchMaterial(stripNamespace(mobConfig.weapon()));
        ItemStack stack;
        if (weapon != null) {
            stack = new ItemStack(weapon);
            for (var enchantment : mobConfig.weaponEnchantments().entrySet()) {
                addEnchantment(stack, enchantment.getKey(), enchantment.getValue());
            }
        } else if (isRanged(mob.getType())) {
            stack = enchanted(Material.BOW, "power", 10, "punch", 3, "flame", 1);
        } else {
            stack = enchanted(Material.NETHERITE_SWORD, "sharpness", 10, "fire_aspect", 3);
        }
        equipment.setItemInMainHand(stack);
        equipment.setHelmetDropChance(0.0f);
        equipment.setChestplateDropChance(0.0f);
        equipment.setLeggingsDropChance(0.0f);
        equipment.setBootsDropChance(0.0f);
        equipment.setItemInMainHandDropChance(0.0f);
    }

    private static ItemStack enchanted(Material material, Object... enchantments) {
        ItemStack stack = new ItemStack(material);
        for (int i = 0; i < enchantments.length; i += 2) {
            addEnchantment(stack, String.valueOf(enchantments[i]), ((Number) enchantments[i + 1]).intValue());
        }
        return stack;
    }

    private static void addEnchantment(ItemStack stack, String id, int level) {
        NamespacedKey key = id.contains(":") ? NamespacedKey.fromString(id) : NamespacedKey.minecraft(id);
        if (key == null) return;
        Enchantment enchantment = Registry.ENCHANTMENT.get(key);
        if (enchantment != null) stack.addUnsafeEnchantment(enchantment, level);
    }

    private static String stripNamespace(String id) {
        int separator = id.indexOf(':');
        return separator >= 0 ? id.substring(separator + 1) : id;
    }
}
