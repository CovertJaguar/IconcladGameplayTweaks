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
public class KeepHandEventHandler implements IIroncladEventHandler{
    public static KeepHandEventHandler MAIN_HAND = new KeepHandEventHandler(EnumHand.MAIN_HAND, "keepMainHandOnDeath");
    public static KeepHandEventHandler OFF_HAND = new KeepHandEventHandler(EnumHand.OFF_HAND, "keepOffHandOnDeath");
    private Map<EntityPlayer, ItemStack> deadPlayers = new MapMaker().weakKeys().makeMap();
    private boolean keep;
    private final EnumHand hand;
    private final String configTag;

    private KeepHandEventHandler(EnumHand hand, String configTag) {
        this.hand = hand;
        this.configTag = configTag;
    }

    @Override
    public boolean isEnabled() {
        return keep;
    }

    @Override
    public void readConfig(Configuration config) {
        keep = config.getBoolean(configTag, IroncladConfig.CAT_PLAYER, false, "If true, the player will keep the item in his hand through death. It is not recommended to turn on the keepInventory gamerule while this is active.");
    }

    @SubscribeEvent
    public void onLivingDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof EntityPlayerMP) {
            EntityPlayerMP player = (EntityPlayerMP) event.getEntity();
            ItemStack stack = player.getHeldItem(hand);
            if (stack != null)
                deadPlayers.put(player, stack);
        }
    }

    @SubscribeEvent
    public void onPlayerDrops(PlayerDropsEvent event) {
        EntityPlayer player = event.getEntityPlayer();
        ItemStack heldStack = deadPlayers.get(player);
        if (heldStack == null)
            return;
        Iterator<EntityItem> it = event.getDrops().iterator();
        boolean foundItem = false;
        while (it.hasNext()) {
            EntityItem ei = it.next();
            if (ei.getEntityItem() == heldStack) {
                foundItem = true;
                it.remove();
                break;
            }
        }
        if (foundItem)
            player.setHeldItem(hand, heldStack);
        deadPlayers.remove(player);
    }

    @SubscribeEvent
    public void onPlayerClone(PlayerEvent.Clone event) {
        EntityPlayer oldPlayer = event.getOriginal();
        if (!event.isWasDeath()) {
            return;
        }
        EntityPlayer newPlayer = event.getEntityPlayer();
        ItemStack heldStack = oldPlayer.getHeldItem(hand);
        if (heldStack == null)
            return;
        newPlayer.setHeldItem(hand, heldStack);
    }
}
