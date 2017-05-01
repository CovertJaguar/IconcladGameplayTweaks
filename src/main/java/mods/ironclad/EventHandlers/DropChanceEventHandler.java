/*
 * Copyright (c) CovertJaguar, 2011-2017
 *
 * This work (Ironclad Gameplay Tweaks) is licensed under the "MIT" License,
 * see LICENSE in root folder for details.
 */

package mods.ironclad.EventHandlers;

import mods.ironclad.IroncladConfig;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * Created by CovertJaguar on 4/30/2017 for Railcraft.
 *
 * @author CovertJaguar <http://www.railcraft.info>
 */
public class DropChanceEventHandler {
    public static DropChanceEventHandler INSTANCE = new DropChanceEventHandler();

    private DropChanceEventHandler() {
    }

    @SubscribeEvent
    public void entitySpawn(EntityJoinWorldEvent event) {
        IroncladConfig.setArmorDropChance(event.getEntity());
        IroncladConfig.setHandDropChance(event.getEntity());
    }

}
