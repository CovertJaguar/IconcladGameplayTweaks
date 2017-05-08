/*
 * Copyright (c) CovertJaguar, 2011-2017
 *
 * This work (Ironclad Gameplay Tweaks) is licensed under the "MIT" License,
 * see LICENSE in root folder for details.
 */

package mods.ironclad.EventHandlers;

import mods.ironclad.config.IroncladConfig;
import net.minecraft.block.state.IBlockState;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.event.entity.player.BonemealEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by CovertJaguar on 4/30/2017 for Railcraft.
 *
 * @author CovertJaguar <http://www.railcraft.info>
 */
public class BonemealEventHandler implements IIroncladEventHandler {
    public static BonemealEventHandler INSTANCE = new BonemealEventHandler();
    private boolean disableBonemeal;
    private String[] bonemealWhitelistDefaults = {
            "minecraft:rail",
            "minecraft:grass",
            "minecraft:red_mushroom",
            "minecraft:brown_mushroom",
    };
    private Set<String> bonemealWhitelist = Collections.emptySet();

    private BonemealEventHandler() {
    }

    @Override
    public boolean isEnabled() {
        return disableBonemeal;
    }

    @Override
    public void readConfig(Configuration config) {
        disableBonemeal = config.getBoolean("disableBonemeal", IroncladConfig.CAT_BONEMEAL, false, "If true, bonemeal won't insta-grow plants.");

        String[] bonemealWhitelistArray = config.getStringList("bonemealWhitelist", IroncladConfig.CAT_BONEMEAL, bonemealWhitelistDefaults, "Blocks that always should allow bonemeal events. Format: <resourceId/modId>:<blockName>[#<meta>]");
        bonemealWhitelist = Arrays.stream(bonemealWhitelistArray).collect(Collectors.toSet());
    }

    private boolean isBonemealWhitelisted(IBlockState state) {
        String name = state.getBlock().getRegistryName().toString();
        if (bonemealWhitelist.contains(name))
            return true;
        name += "#" + state.getBlock().getMetaFromState(state);
        return bonemealWhitelist.contains(name);
    }

    @SubscribeEvent
    public void bonemeal(BonemealEvent event) {
        if (!isBonemealWhitelisted(event.getBlock()))
            event.setCanceled(true);
    }

}
