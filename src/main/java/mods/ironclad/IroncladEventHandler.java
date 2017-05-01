/*
 * Copyright (c) CovertJaguar, 2011-2017
 *
 * This work (Ironclad Gameplay Tweaks) is licensed under the "MIT" License,
 * see LICENSE in root folder for details.
 */

package mods.ironclad;

import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * Created by CovertJaguar on 4/30/2017 for Railcraft.
 *
 * @author CovertJaguar <http://www.railcraft.info>
 */
public class IroncladEventHandler {
    public static IroncladEventHandler INSTANCE = new IroncladEventHandler();

    private IroncladEventHandler() {
    }

    @SubscribeEvent
    public void entitySpawn(EntityJoinWorldEvent event) {
        IroncladConfig.setArmorDropChance(event.getEntity());
        IroncladConfig.setHandDropChance(event.getEntity());
    }
}
