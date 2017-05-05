/*
 * Copyright (c) CovertJaguar, 2011-2017
 *
 * This work (Ironclad Gameplay Tweaks) is licensed under the "MIT" License,
 * see LICENSE in root folder for details.
 */

package mods.ironclad.EventHandlers;

/**
 * Created by CovertJaguar on 5/4/2017 for Railcraft.
 *
 * @author CovertJaguar <http://www.railcraft.info>
 */
public interface IIroncladEventHandler {
    default void reset() {
    }

    boolean isEnabled();

}
