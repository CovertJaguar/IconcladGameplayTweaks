/*
 * Copyright (c) CovertJaguar, 2011-2017
 *
 * This work (Ironclad Gameplay Tweaks) is licensed under the "MIT" License,
 * see LICENSE in root folder for details.
 */

package mods.ironclad.EventHandlers;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import mods.ironclad.Utils;
import mods.ironclad.config.IroncladConfig;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by CovertJaguar on 4/30/2017 for Railcraft.
 *
 * @author CovertJaguar <http://www.railcraft.info>
 */
public class ItemEnforcerEventHandler implements IIroncladEventHandler {
    public static final ItemEnforcerEventHandler INSTANCE = new ItemEnforcerEventHandler();
    private Set<String> itemsToBan = Collections.emptySet();
    private Set<String> itemsToLog = Collections.emptySet();
    private File logFile;
    private Multimap<String, String> loggedItems = HashMultimap.create();

    private ItemEnforcerEventHandler() {
    }

    @Override
    public void readConfig(Configuration config) {
        File logFolder = new File(config.getConfigFile().getParentFile().getParentFile().getParentFile(), "logs");
        logFile = new File(logFolder, "ironclad.log");

        String[] bannedArray = config.getStringList("bannedItems", IroncladConfig.CAT_ITEMS, new String[]{}, "Banned items are deleted when they enter a player's inventory. Format: <resourceId/modId>:<itemName>[#<meta>]");
        itemsToBan = Arrays.stream(bannedArray).collect(Collectors.toSet());

        String[] loggedItemsArray = config.getStringList("loggedItems", IroncladConfig.CAT_ITEMS, new String[]{}, "Logged items are logged when they enter a player's inventory. Format: <resourceId/modId>:<itemName>[#<meta>]");
        itemsToLog = Arrays.stream(loggedItemsArray).collect(Collectors.toSet());
    }

    @Override
    public boolean isEnabled() {
        return !itemsToBan.isEmpty() || !itemsToLog.isEmpty();
    }

    @SubscribeEvent
    public void playerTick(TickEvent.PlayerTickEvent event) {
        EntityPlayer player = event.player;
        if (player.worldObj.isRemote)
            return;
        if ((player.worldObj.getTotalWorldTime() + player.getEntityId()) % 32 != 0)
            return;
        if (player.openContainer != player.inventoryContainer)
            return;
        List<String> toLog = new ArrayList<>();
        String playerName = player.getDisplayNameString();
        String time = new SimpleDateFormat("yyyy-MM-dd HH:mm z").format(new Date());

        for (int i = 0; i < player.inventory.getSizeInventory(); i++) {
            ItemStack stack = player.inventory.getStackInSlot(i);
            if (Utils.isEmpty(stack))
                continue;
            if (Utils.containsItem(itemsToBan, stack)) {
                toLog.add(String.format("[%s] Banned item deleted from %s: %s", time, playerName, Utils.toString(stack)));
                player.inventory.setInventorySlotContents(i, Utils.emptyStack());
            } else if (Utils.containsItem(itemsToLog, stack)) {
                String itemEntry = Utils.getEntry(itemsToLog, stack);
                if (!loggedItems.containsEntry(playerName, itemEntry)) {
                    loggedItems.put(playerName, itemEntry);
                    toLog.add(String.format("[%s] Logged Item found in %s: %s", time, playerName, Utils.toString(stack)));
                }
            }
        }

        if (!toLog.isEmpty()) {
            try (FileWriter fw = new FileWriter(logFile, true);
                 BufferedWriter bw = new BufferedWriter(fw);
                 PrintWriter out = new PrintWriter(bw)) {
                toLog.forEach(out::println);
            } catch (IOException ignored) {
            }
        }
    }
}
