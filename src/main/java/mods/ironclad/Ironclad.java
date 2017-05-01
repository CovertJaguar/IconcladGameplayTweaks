/*
 * Copyright (c) CovertJaguar, 2011-2017
 *
 * This work (Ironclad Gameplay Tweaks) is licensed under the "MIT" License,
 * see LICENSE in root folder for details.
 */

package mods.ironclad;

import mods.ironclad.EventHandlers.DropChanceEventHandler;
import mods.ironclad.EventHandlers.KeepMainHandEventHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLModDisabledEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

@Mod(modid = Ironclad.MOD_ID, version = Ironclad.VERSION, canBeDeactivated = true, acceptableRemoteVersions = "*")
public class Ironclad {
    static final String MOD_ID = "ironclad";
    static final String VERSION = "@VERSION@";

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        IroncladConfig.load(event.getSuggestedConfigurationFile());
    }

    @EventHandler
    public void init(FMLInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(DropChanceEventHandler.INSTANCE);
        if (IroncladConfig.keepMainHandOnDeath)
            MinecraftForge.EVENT_BUS.register(KeepMainHandEventHandler.INSTANCE);
    }

    @EventHandler
    public void init(FMLModDisabledEvent event) {
        MinecraftForge.EVENT_BUS.unregister(DropChanceEventHandler.INSTANCE);
        MinecraftForge.EVENT_BUS.unregister(KeepMainHandEventHandler.INSTANCE);
    }
}