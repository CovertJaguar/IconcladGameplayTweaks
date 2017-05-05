/*
 * Copyright (c) CovertJaguar, 2011-2017
 *
 * This work (Ironclad Gameplay Tweaks) is licensed under the "MIT" License,
 * see LICENSE in root folder for details.
 */

package mods.ironclad.config;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by CovertJaguar on 4/17/2017 for Railcraft.
 *
 * @author CovertJaguar <http://www.railcraft.info>
 */
public class IroncladConfig {
    public static boolean keepMainHandOnDeath;
    public static boolean disableBonemeal;
    public static float horseSpeedModifier;
    public static float muleSpeedModifier;
    public static float undeadHorseSpeedModifier;
    public static Configuration config;
    private static String MOB_INV_ARMOR_DROP = "mob_inv_armor_drop_chances";
    private static String MOB_INV_HAND_DROP = "mob_inv_hand_drop_chances";
    private static String PLAYER_CAT = "player";
    private static String BONEMEAL_CAT = "bonemeal";
    private static String HORSE_CAT = "horse";
    private static String[] bonemealWhitelistDefaults = {
            "minecraft:rail",
            "minecraft:grass",
            "minecraft:red_mushroom",
            "minecraft:brown_mushroom",
    };
    private static Set<String> bonemealWhitelist = Collections.emptySet();

    public static void load(File configFile) {
        config = new Configuration(configFile);
        config.load();

        readConfig();
    }

    public static void readConfig() {
        config.setCategoryComment(MOB_INV_ARMOR_DROP, "The chance that a entity will drop the armor its wearing. -1 to disable drops entirely.");
        config.setCategoryComment(MOB_INV_HAND_DROP, "The chance that a entity will drop what is in its hands. -1 to disable drops entirely.");
        config.setCategoryComment(PLAYER_CAT, "Tweaks pertaining to the player.");
        config.setCategoryComment(BONEMEAL_CAT, "Tweaks pertaining to bonemeal.");
        config.setCategoryComment(HORSE_CAT, "Tweaks pertaining to horses.");

        keepMainHandOnDeath = config.getBoolean("keepMainHandOnDeath", PLAYER_CAT, false, "If true, the player will keep the item in his main hand through death. It is not recommended to turn on the keepInventory gamerule while this is active.");

        disableBonemeal = config.getBoolean("disableBonemeal", BONEMEAL_CAT, false, "If true, bonemeal won't insta-grow plants.");

        String[] bonemealWhitelistArray = config.getStringList("bonemealWhitelist", BONEMEAL_CAT, bonemealWhitelistDefaults, "Blocks that always should allow bonemeal events. Format: <resourceId/modId>:<blockName>[#<meta>]");
        bonemealWhitelist = Arrays.stream(bonemealWhitelistArray).collect(Collectors.toSet());

        horseSpeedModifier = config.getFloat("horseSpeedModifier", HORSE_CAT, 0F, -1F, 1F, "Adjusts the speed of Horses. Formula: speed = base + base * modifier");
        undeadHorseSpeedModifier = config.getFloat("undeadHorseSpeedModifier", HORSE_CAT, 0F, -1F, 1F, "Adjusts the speed of Undead Horses. Formula: speed = base + base * modifier");
        muleSpeedModifier = config.getFloat("muleSpeedModifier", HORSE_CAT, 0F, -1F, 1F, "Adjusts the speed of Mules and Donkeys. Formula: speed = base + base * modifier");

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

    public static boolean isBonemealWhitelisted(IBlockState state) {
        String name = state.getBlock().getRegistryName().toString();
        if (bonemealWhitelist.contains(name))
            return true;
        name += "#" + state.getBlock().getMetaFromState(state);
        return bonemealWhitelist.contains(name);
    }
}
