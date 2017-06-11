/*
 * Copyright (c) CovertJaguar, 2011-2017
 *
 * This work (Ironclad Gameplay Tweaks) is licensed under the "MIT" License,
 * see LICENSE in root folder for details.
 */

package mods.ironclad;

import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;

import javax.annotation.Nullable;
import java.util.Collection;

/**
 * Created by CovertJaguar on 6/11/2017 for Railcraft.
 *
 * @author CovertJaguar <http://www.railcraft.info>
 */
public class Utils {

    public static boolean isEmpty(ItemStack stack) {
        return stack == null || stack.stackSize <= 0;
    }

    public static ItemStack emptyStack() {
        return null;
    }

    public static String toString(ItemStack stack) {
        String string = stack.stackSize + " x " + stack.getItem().getRegistryName();
        if (stack.getHasSubtypes())
            string += "@" + stack.getItemDamage();
        return string;
    }

    public static boolean containsBlock(Collection<String> collection, IBlockState state) {
        String name = state.getBlock().getRegistryName().toString();
        if (collection.contains(name))
            return true;
        name += "#" + state.getBlock().getMetaFromState(state);
        return collection.contains(name);
    }

    public static boolean containsItem(Collection<String> collection, ItemStack stack) {
        String name = stack.getItem().getRegistryName().toString();
        if (collection.contains(name))
            return true;
        String metaName = name + "#" + stack.getMetadata();
        return collection.contains(metaName);
    }

    @Nullable
    public static String getEntry(Collection<String> collection, ItemStack stack) {
        String name = stack.getItem().getRegistryName().toString();
        if (collection.contains(name))
            return name;
        String metaName = name + "#" + stack.getMetadata();
        if (collection.contains(metaName))
            return metaName;
        return null;
    }
}
