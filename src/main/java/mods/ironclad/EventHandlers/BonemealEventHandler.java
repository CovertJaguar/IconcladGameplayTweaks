/*
 * Copyright (c) CovertJaguar, 2011-2017
 *
 * This work (Ironclad Gameplay Tweaks) is licensed under the "MIT" License,
 * see LICENSE in root folder for details.
 */

package mods.ironclad.EventHandlers;

import mods.ironclad.config.IroncladConfig;
import net.minecraftforge.event.entity.player.BonemealEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * Created by CovertJaguar on 4/30/2017 for Railcraft.
 *
 * @author CovertJaguar <http://www.railcraft.info>
 */
public class BonemealEventHandler implements IIroncladEventHandler{
    public static BonemealEventHandler INSTANCE = new BonemealEventHandler();

    private BonemealEventHandler() {
    }

    @Override
    public boolean isEnabled() {
        return IroncladConfig.disableBonemeal;
    }

    @SubscribeEvent
    public void bonemeal(BonemealEvent event) {
        if (!IroncladConfig.isBonemealWhitelisted(event.getBlock()))
            event.setCanceled(true);
    }

}
