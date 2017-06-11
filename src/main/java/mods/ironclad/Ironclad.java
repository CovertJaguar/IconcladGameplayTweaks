/*
 * Copyright (c) CovertJaguar, 2011-2017
 *
 * This work (Ironclad Gameplay Tweaks) is licensed under the "MIT" License,
 * see LICENSE in root folder for details.
 */

package mods.ironclad;

import mods.ironclad.EventHandlers.*;
import mods.ironclad.config.IroncladConfig;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLModDisabledEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

@Mod(modid = Ironclad.MOD_ID,
        version = Ironclad.VERSION,
        guiFactory = "mods.ironclad.config.IroncladGuiConfigFactory",
        canBeDeactivated = true,
        acceptableRemoteVersions = "*")
public class Ironclad {
    public static final String MOD_ID = "ironclad";
    public static final String VERSION = "@VERSION@";
    public static final IIroncladEventHandler[] eventHandlers = {
            DropChanceEventHandler.INSTANCE,
            KeepMainHandEventHandler.INSTANCE,
            BonemealEventHandler.INSTANCE,
            HorseSpeedEventHandler.HORSE,
            HorseSpeedEventHandler.UNDEAD,
            HorseSpeedEventHandler.MULE,
            FiniteFluidEventHandler.INSTANCE,
            ItemEnforcerEventHandler.INSTANCE
    };

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        IroncladConfig.load(event.getModConfigurationDirectory());
    }

    @EventHandler
    public void init(FMLInitializationEvent event) {
        registerEventHandlers();
    }

    @EventHandler
    public void disable(FMLModDisabledEvent event) {
        unregisterEventHandlers();
    }

    public static void registerEventHandlers() {
        for (IIroncladEventHandler eventHandler : eventHandlers) {
            if (eventHandler.isEnabled())
                MinecraftForge.EVENT_BUS.register(eventHandler);
        }
    }

    public static void unregisterEventHandlers() {
        for (IIroncladEventHandler eventHandler : eventHandlers) {
            MinecraftForge.EVENT_BUS.unregister(eventHandler);
        }
    }

    public static void refreshEventHandlers() {
        unregisterEventHandlers();
        for (IIroncladEventHandler eventHandler : eventHandlers) {
            eventHandler.reset();
        }
        registerEventHandlers();
    }
}