/*
 * Copyright (c) CovertJaguar, 2011-2017
 *
 * This work (Ironclad Gameplay Tweaks) is licensed under the "MIT" License,
 * see LICENSE in root folder for details.
 */

package mods.ironclad.EventHandlers;

import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.client.config.IConfigElement;

import java.util.Collections;
import java.util.List;

/**
 * Created by CovertJaguar on 5/4/2017 for Railcraft.
 *
 * @author CovertJaguar <http://www.railcraft.info>
 */
public interface IIroncladEventHandler {
    default void reset() {
    }

    boolean isEnabled();

    default void readConfig(Configuration config) {
    }

    default List<IConfigElement> getConfigs() {
        return Collections.emptyList();
    }
}
