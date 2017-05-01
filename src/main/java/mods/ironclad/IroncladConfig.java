/*
 * Copyright (c) CovertJaguar, 2011-2017
 *
 * This work (Ironclad Gameplay Tweaks) is licensed under the "MIT" License,
 * see LICENSE in root folder for details.
 */

package mods.ironclad;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by CovertJaguar on 4/17/2017 for Railcraft.
 *
 * @author CovertJaguar <http://www.railcraft.info>
 */
public class IroncladConfig {
    private static Configuration config;
    private static String MOB_INV_ARMOR_DROP = "mob_inv_armor_drop_chances";
    private static String MOB_INV_HAND_DROP = "mob_inv_hand_drop_chances";
    private static Map<Class<? extends Entity>, Float> armorDropChances = new HashMap<>();
    private static Map<Class<? extends Entity>, Float> handDropChances = new HashMap<>();

    public static void load(File configFile) {
        config = new Configuration(configFile);
        config.load();

        config.setCategoryComment(MOB_INV_ARMOR_DROP, "The chance that a entity will drop the armor its wearing. -1 to disable drops entirely.");
        config.setCategoryComment(MOB_INV_HAND_DROP, "The chance that a entity will drop what is in its hands. -1 to disable drops entirely.");

        if (config.hasChanged())
            config.save();
    }

    public static void setArmorDropChance(Entity entity) {
        if (entity instanceof EntityLiving) {
            float dropChance = armorDropChances.computeIfAbsent(entity.getClass(), k -> config.getFloat(entity.getClass().getName(), MOB_INV_ARMOR_DROP, 0.085F, -1F, 1F, ""));
            if (dropChance >= 0F) {
                EntityLiving el = (EntityLiving) entity;
                float[] currentValues = ObfuscationReflectionHelper.getPrivateValue(EntityLiving.class, el, "inventoryArmorDropChances", "field_184655_bs");
                el.setDropChance(EntityEquipmentSlot.HEAD, Math.min(currentValues[0], dropChance));
                el.setDropChance(EntityEquipmentSlot.CHEST, Math.min(currentValues[1], dropChance));
                el.setDropChance(EntityEquipmentSlot.LEGS, Math.min(currentValues[2], dropChance));
                el.setDropChance(EntityEquipmentSlot.FEET, Math.min(currentValues[3], dropChance));
            }

            if (config.hasChanged())
                config.save();
        }
    }

    public static void setHandDropChance(Entity entity) {
        if (entity instanceof EntityLiving) {
            float dropChance = handDropChances.computeIfAbsent(entity.getClass(), k -> config.getFloat(entity.getClass().getName(), MOB_INV_HAND_DROP, 0.085F, -1F, 1F, ""));
            if (dropChance >= 0F) {
                EntityLiving el = (EntityLiving) entity;
                float[] currentValues = ObfuscationReflectionHelper.getPrivateValue(EntityLiving.class, el, "inventoryHandsDropChances", "field_82174_bp");
                el.setDropChance(EntityEquipmentSlot.MAINHAND, Math.min(currentValues[0], dropChance));
                el.setDropChance(EntityEquipmentSlot.OFFHAND, Math.min(currentValues[1], dropChance));
            }

            if (config.hasChanged())
                config.save();
        }
    }
}
