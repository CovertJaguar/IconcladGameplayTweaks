/*
 * Copyright (c) CovertJaguar, 2011-2017
 *
 * This work (Ironclad Gameplay Tweaks) is licensed under the "MIT" License,
 * see LICENSE in root folder for details.
 */

package mods.ironclad.config;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.fml.client.IModGuiFactory;

import java.util.Set;

/**
 * Created by CovertJaguar on 10/25/2016 for Railcraft.
 *
 * @author CovertJaguar <http://www.railcraft.info>
 */
public class IroncladGuiConfigFactory implements IModGuiFactory {
    @Override
    public void initialize(Minecraft minecraftInstance) {

    }

    @Override
    public Class<? extends GuiScreen> mainConfigGuiClass() {
        return IroncladGuiConfig.class;
    }

    @Override
    public Set<RuntimeOptionCategoryElement> runtimeGuiCategories() {
        return null;
    }

    @Override
    public RuntimeOptionGuiHandler getHandlerFor(RuntimeOptionCategoryElement element) {
        return null;
    }
}
