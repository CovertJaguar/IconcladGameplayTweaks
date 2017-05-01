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

/**
 * Created by CovertJaguar on 4/17/2017 for Railcraft.
 *
 * @author CovertJaguar <http://www.railcraft.info>
 */
public class IroncladConfig {
    public static boolean keepMainHandOnDeath;
    private static Configuration config;
    private static String MOB_INV_ARMOR_DROP = "mob_inv_armor_drop_chances";
    private static String MOB_INV_HAND_DROP = "mob_inv_hand_drop_chances";
    private static String PLAYER_CAT = "player";

    public static void load(File configFile) {
        config = new Configuration(configFile);
        config.load();

        config.setCategoryComment(MOB_INV_ARMOR_DROP, "The chance that a entity will drop the armor its wearing. -1 to disable drops entirely.");
        config.setCategoryComment(MOB_INV_HAND_DROP, "The chance that a entity will drop what is in its hands. -1 to disable drops entirely.");
        config.setCategoryComment(PLAYER_CAT, "Tweaks pertaining to the player.");

        keepMainHandOnDeath = config.getBoolean("keepMainHandOnDeath", PLAYER_CAT, keepMainHandOnDeath, "If true, the player will keep the item in his main hand through death.");

        if (config.hasChanged())
            config.save();
    }

    public static void setArmorDropChance(Entity entity) {
        if (entity instanceof EntityLiving) {
            float dropChance = config.getFloat(entity.getClass().getName(), MOB_INV_ARMOR_DROP, 0.085F, -1F, 1F, "");
            if (dropChance >= 0F) {
                EntityLiving el = (EntityLiving) entity;
                float[] currentValues = ObfuscationReflectionHelper.getPrivateValue(EntityLiving.class, el, "inventoryArmorDropChances", "field_184655_bs");
                el.setDropChance(EntityEquipmentSlot.HEAD, currentValues[0] <= 0F ? -1F : dropChance);
                el.setDropChance(EntityEquipmentSlot.CHEST, currentValues[1] <= 0F ? -1F : dropChance);
                el.setDropChance(EntityEquipmentSlot.LEGS, currentValues[2] <= 0F ? -1F : dropChance);
                el.setDropChance(EntityEquipmentSlot.FEET, currentValues[3] <= 0F ? -1F : dropChance);
            }

            if (config.hasChanged())
                config.save();
        }
    }

    public static void setHandDropChance(Entity entity) {
        if (entity instanceof EntityLiving) {
            float dropChance = config.getFloat(entity.getClass().getName(), MOB_INV_HAND_DROP, 0.085F, -1F, 1F, "");
            if (dropChance >= 0F) {
                EntityLiving el = (EntityLiving) entity;
                float[] currentValues = ObfuscationReflectionHelper.getPrivateValue(EntityLiving.class, el, "inventoryHandsDropChances", "field_82174_bp");
                el.setDropChance(EntityEquipmentSlot.MAINHAND, currentValues[0] <= 0F ? -1F : dropChance);
                el.setDropChance(EntityEquipmentSlot.OFFHAND, currentValues[1] <= 0F ? -1F : dropChance);
            }

            if (config.hasChanged())
                config.save();
        }
    }
}
