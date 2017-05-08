/*
 * Copyright (c) CovertJaguar, 2011-2017
 *
 * This work (Ironclad Gameplay Tweaks) is licensed under the "MIT" License,
 * see LICENSE in root folder for details.
 */
package mods.ironclad.config;

import mods.ironclad.EventHandlers.IIroncladEventHandler;
import mods.ironclad.Ironclad;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.client.config.GuiConfig;
import net.minecraftforge.fml.client.config.IConfigElement;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class IroncladGuiConfig extends GuiConfig {

    public IroncladGuiConfig(GuiScreen parent) {
        super(parent, generateConfig(), Ironclad.MOD_ID, false, false, Ironclad.MOD_ID);
    }

    private static List<IConfigElement> generateConfig() {
        List<IConfigElement> configElements = new ArrayList<>();
        configElements.addAll(getElements(IroncladConfig.config));
        for (IIroncladEventHandler eventHandler : Ironclad.eventHandlers) {
            configElements.addAll(eventHandler.getConfigs());
        }
        return configElements;
    }

    public static List<IConfigElement> getElements(Configuration config) {
        return getCategories(config).stream().map(ConfigElement::new).collect(Collectors.toList());
    }

    private static List<ConfigCategory> getCategories(Configuration config) {
        Set<String> catNames = config.getCategoryNames();
        return catNames.stream().map(config::getCategory).filter(cat -> !cat.isChild()).collect(Collectors.toList());
    }

    @Override
    public void onGuiClosed() {
        super.onGuiClosed();
        if (IroncladConfig.config.hasChanged()) {
            IroncladConfig.readConfig();
            Ironclad.refreshEventHandlers();
        }
    }
}
