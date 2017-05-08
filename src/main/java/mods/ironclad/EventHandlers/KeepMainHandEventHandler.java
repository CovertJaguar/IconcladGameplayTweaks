/*
 * Copyright (c) CovertJaguar, 2011-2017
 *
 * This work (Ironclad Gameplay Tweaks) is licensed under the "MIT" License,
 * see LICENSE in root folder for details.
 */

package mods.ironclad.EventHandlers;

import com.google.common.collect.MapMaker;
import mods.ironclad.config.IroncladConfig;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerDropsEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.Iterator;
import java.util.Map;

/**
 * Created by CovertJaguar on 5/1/2017 for Railcraft.
 *
 * @author CovertJaguar <http://www.railcraft.info>
 */
public class KeepMainHandEventHandler implements IIroncladEventHandler{
    public static KeepMainHandEventHandler INSTANCE = new KeepMainHandEventHandler();
    private Map<EntityPlayer, ItemStack> deadPlayers = new MapMaker().weakKeys().makeMap();
    private boolean keepMainHandOnDeath;

    private KeepMainHandEventHandler() {
    }

    @Override
    public boolean isEnabled() {
        return keepMainHandOnDeath;
    }

    @Override
    public void readConfig(Configuration config) {
        keepMainHandOnDeath = config.getBoolean("keepMainHandOnDeath", IroncladConfig.CAT_PLAYER, false, "If true, the player will keep the item in his main hand through death. It is not recommended to turn on the keepInventory gamerule while this is active.");
    }

    @SubscribeEvent
    public void onLivingDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof EntityPlayerMP) {
            EntityPlayerMP player = (EntityPlayerMP) event.getEntity();
            ItemStack stack = player.getHeldItem(EnumHand.MAIN_HAND);
            if (stack != null)
                deadPlayers.put(player, stack);
        }
    }

    @SubscribeEvent
    public void onPlayerDrops(PlayerDropsEvent event) {
        EntityPlayer player = event.getEntityPlayer();
        ItemStack mainHandItemStack = deadPlayers.get(player);
        if (mainHandItemStack == null)
            return;
        Iterator<EntityItem> it = event.getDrops().iterator();
        boolean foundItem = false;
        while (it.hasNext()) {
            EntityItem ei = it.next();
            if (ei.getEntityItem() == mainHandItemStack) {
                foundItem = true;
                it.remove();
                break;
            }
        }
        if (foundItem)
            player.setHeldItem(EnumHand.MAIN_HAND, mainHandItemStack);
        deadPlayers.remove(player);
    }

    @SubscribeEvent
    public void onPlayerClone(PlayerEvent.Clone event) {
        EntityPlayer oldPlayer = event.getOriginal();
        if (!event.isWasDeath()) {
            return;
        }
        EntityPlayer newPlayer = event.getEntityPlayer();
        ItemStack mainHandItemStack = oldPlayer.getHeldItem(EnumHand.MAIN_HAND);
        if (mainHandItemStack == null)
            return;
        newPlayer.setHeldItem(EnumHand.MAIN_HAND, mainHandItemStack);
    }
}
