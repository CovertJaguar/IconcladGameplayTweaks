/*
 * Copyright (c) CovertJaguar, 2011-2017
 *
 * This work (Ironclad Gameplay Tweaks) is licensed under the "MIT" License,
 * see LICENSE in root folder for details.
 */

package mods.ironclad.EventHandlers;

import mods.ironclad.config.IroncladConfig;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeProvider;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.client.config.DummyConfigElement;
import net.minecraftforge.fml.client.config.IConfigElement;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.apache.commons.lang3.ArrayUtils;

import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentSkipListSet;

/**
 * Created by CovertJaguar on 5/7/2017 for Railcraft.
 *
 * @author CovertJaguar <http://www.railcraft.info>
 */
public class FiniteFluidEventHandler implements IIroncladEventHandler {

    public static FiniteFluidEventHandler INSTANCE = new FiniteFluidEventHandler();
    public static String CAT_FLUID_DEF = "fluid_def";
    private boolean enableFluidReplenishmentOverride;
    private List<FluidDef> fluidDefs = new ArrayList<>();

    private Biome[] tempBiomeArray;

    private class FluidDef {

        private final Configuration config;

        private final String[] blocks;

        private int heightMinReplenish = 0;
        private int heightMaxReplenish = 64;

        private int biomeSearchDistanceBlacklisted = 1;
        private int biomeSearchDistanceNormal = 4;

        private int minPoolSize = 1000;

        private String[] whitelistedDimensions = {"0"};

        private String[] blacklistBiomesDefaults = {"minecraft:hell"};
        private String[] blacklistedBiomes = blacklistBiomesDefaults.clone();
        private String[] whitelistBiomesDefaults = {"minecraft:deep_ocean"};
        private String[] whitelistedBiomes = whitelistBiomesDefaults.clone();

        private String[] blacklistBiomeTypesDefaults = {"DRY", "NETHER", "END"};
        private String[] blacklistedBiomeTypes = blacklistBiomeTypesDefaults.clone();
        private String[] whitelistBiomeTypesDefaults = {"WATER", "OCEAN", "RIVER", "BEACH", "WET", "SWAMP"};
        private String[] whitelistedBiomeTypes = whitelistBiomeTypesDefaults.clone();

        private FluidDef(Configuration config) {
            this.config = config;

            config.setCategoryComment(CAT_FLUID_DEF, "This defines a rule set for fluid replenishment for this specific fluid.\nYou can copy this file for other fluid blocks,\nincluding things that aren't normally infinite like lava.");

            blocks = config.getStringList("blocks", CAT_FLUID_DEF, new String[]{"minecraft:water", "minecraft:flowing_water"}, "The affected fluid blocks.");

            heightMinReplenish = config.getInt("heightMinReplenish", CAT_FLUID_DEF, 40, 0, 255, "The min y-level at which the fluid will replenish.");
            heightMaxReplenish = config.getInt("heightMaxReplenish", CAT_FLUID_DEF, 64, 0, 255, "The max y-level at which the fluid will replenish. Should be set no lower than sea level.");

            biomeSearchDistanceNormal = config.getInt("biomeSearchDistanceNormal", CAT_FLUID_DEF, 4, 0, 16, "The max distance in chunks that the code should look for a valid biome for normal biomes. Zero disables.");
            biomeSearchDistanceBlacklisted = config.getInt("biomeSearchDistanceBlacklisted", CAT_FLUID_DEF, 1, 0, 16, "The max distance in chunks that the code should look for a valid biome for blacklisted biomes. Zero disables.");

            minPoolSize = config.getInt("minPoolSize", CAT_FLUID_DEF, 1000, 0, 4000, "The minimum required pool size to enable replenishment. Zero disables.");

            whitelistedDimensions = config.getStringList("whitelistedDimensions", CAT_FLUID_DEF, new String[]{"0"}, "Dimensions that should allow the fluid to be infinite. An empty list causes the dimension to be ignored.");

            blacklistedBiomes = config.getStringList("blacklistedBiomes", CAT_FLUID_DEF, blacklistBiomesDefaults, "Biomes that the fluid is not infinite in. Uses the fully qualified registry name. Takes priority over the whitelists and biome types.");
            whitelistedBiomes = config.getStringList("whitelistedBiomes", CAT_FLUID_DEF, whitelistBiomesDefaults, "Biomes that the fluid is infinite in. Uses the fully qualified registry name. Takes priority over biome types.");

            blacklistedBiomeTypes = config.getStringList("blacklistedBiomeTypes", CAT_FLUID_DEF, blacklistBiomeTypesDefaults, "Biome Types that the fluid is not infinite in. Takes priority over the whitelists.");
            whitelistedBiomeTypes = config.getStringList("whitelistedBiomeTypes", CAT_FLUID_DEF, whitelistBiomeTypesDefaults, "Biome Types that the fluid is infinite in.");
        }

        private boolean isValidYLevel(BlockPos pos) {
            return heightMinReplenish <= pos.getY() && pos.getY() <= heightMaxReplenish;
        }

        private boolean isBlacklistedBiome(Biome biome) {
            if (ArrayUtils.contains(blacklistedBiomes, biome.getRegistryName().toString())) {
                return true;
            }
            if (ArrayUtils.contains(whitelistedBiomes, biome.getRegistryName().toString())) {
                return false;
            }
            for (String biomeType : blacklistedBiomeTypes) {
                if (BiomeDictionary.isBiomeOfType(biome, BiomeDictionary.Type.valueOf(biomeType))) {
                    return true;
                }
            }
            return false;
        }

        private boolean isValidBiome(Biome biome) {
            if (ArrayUtils.contains(blacklistedBiomes, biome.getRegistryName().toString())) {
                return false;
            }
            if (ArrayUtils.contains(whitelistedBiomes, biome.getRegistryName().toString())) {
                return true;
            }
            for (String biomeType : blacklistedBiomeTypes) {
                if (BiomeDictionary.isBiomeOfType(biome, BiomeDictionary.Type.valueOf(biomeType))) {
                    return false;
                }
            }
            for (String biomeType : whitelistedBiomeTypes) {
                if (BiomeDictionary.isBiomeOfType(biome, BiomeDictionary.Type.valueOf(biomeType))) {
                    return true;
                }
            }
            return false;
        }

        private boolean isNearbyBiomeValid(World world, BlockPos pos, int searchDistance) {
            if (searchDistance <= 0)
                return false;
            BiomeProvider biomeProvider = world.getBiomeProvider();
            int chunkX = pos.getX() >> 4;
            int chunkZ = pos.getZ() >> 4;

            int minX = chunkX - searchDistance;
            int maxX = chunkX + searchDistance;
            int minZ = chunkZ - searchDistance;
            int maxZ = chunkZ + searchDistance;

            for (int xx = minX; xx <= maxX; xx++) {
                for (int zz = minZ; zz <= maxZ; zz++) {
                    tempBiomeArray = biomeProvider.getBiomes(tempBiomeArray, xx * 16, zz * 16, 16, 16);
                    for (Biome biome : tempBiomeArray) {
                        if (isValidBiome(biome))
                            return true;
                    }
                }
            }
            return false;
        }

        private boolean isFluidBlock(IBlockState state) {
            return ArrayUtils.contains(blocks, state.getBlock().getRegistryName().toString());
        }

        private boolean isValidPool(World world, BlockPos pos) {
            if (minPoolSize <= 0)
                return true;
            Collection<BlockPos> blocksToExpand = new ConcurrentSkipListSet<>();
            Collection<BlockPos> visitedBlocks = new HashSet<>();
            blocksToExpand.add(pos);
            visitedBlocks.add(pos);
            BlockPos.MutableBlockPos targetPos = new BlockPos.MutableBlockPos();
            while (!blocksToExpand.isEmpty() && visitedBlocks.size() < minPoolSize) {
                Iterator<BlockPos> it = blocksToExpand.iterator();
                while (it.hasNext() && visitedBlocks.size() < minPoolSize) {
                    BlockPos fluidBlock = it.next();
                    for (EnumFacing facing : EnumFacing.VALUES) {
                        targetPos.setPos(fluidBlock);
                        targetPos.move(facing);
                        if (isValidYLevel(targetPos) && !visitedBlocks.contains(targetPos)) {
                            IBlockState targetState = world.getBlockState(targetPos);
                            if (isFluidBlock(targetState)) {
                                BlockPos immutable = targetPos.toImmutable();
                                blocksToExpand.add(immutable);
                                visitedBlocks.add(immutable);
                            }
                        }
                    }
                    it.remove();
                }
            }
            return visitedBlocks.size() >= minPoolSize;
        }
    }

    @Override
    public boolean isEnabled() {
        return enableFluidReplenishmentOverride;
    }

    @Override
    public List<IConfigElement> getConfigs() {
        List<IConfigElement> elements = new ArrayList<>();
        for (FluidDef def : fluidDefs) {
            String filename = def.config.getConfigFile().getName();
            IConfigElement defElement = new DummyConfigElement.DummyCategoryElement(filename, filename,
                    Collections.singletonList(new ConfigElement(def.config.getCategory(CAT_FLUID_DEF))));
            elements.add(defElement);
        }

        IConfigElement fluid_replenishment_defs = new DummyConfigElement.DummyCategoryElement("fluid_replenishment", "fluid_replenishment", elements);
        return Collections.singletonList(fluid_replenishment_defs);
    }

    @Override
    public void readConfig(Configuration config) {
        fluidDefs.clear();
        enableFluidReplenishmentOverride = config.getBoolean("enableFluidReplenishmentOverride", IroncladConfig.CAT_FLUIDS, false, "If true, you can define fluid replenishment rules in the ironclad/fluid_replenishment folder.");

        File fluidConfigFolder = new File(IroncladConfig.configFolder, "fluid_replenishment");
        if (!fluidConfigFolder.exists())
            fluidConfigFolder.mkdirs();
        File[] fluidConfigs = fluidConfigFolder.listFiles((dir, name) -> name != null && name.endsWith(".cfg"));

        if (ArrayUtils.isEmpty(fluidConfigs)) {
            fluidConfigs = new File[]{new File(fluidConfigFolder, "water.cfg")};
        }

        for (File fluidConfigFile : fluidConfigs) {
            Configuration fluidConfig = new Configuration(fluidConfigFile);
            fluidConfig.load();
            FluidDef fluidDef = new FluidDef(fluidConfig);
            fluidDefs.add(fluidDef);
            if (fluidConfig.hasChanged())
                fluidConfig.save();
        }
    }

    @SubscribeEvent
    public void handleWaterReplicate(BlockEvent.CreateFluidSourceEvent event) {
        World world = event.getWorld();
        IBlockState state = event.getState();
        for (FluidDef def : fluidDefs) {
            if (def.isFluidBlock(state)) {
                event.setResult(Event.Result.DENY);
                if (!ArrayUtils.isEmpty(def.whitelistedDimensions) && !ArrayUtils.contains(def.whitelistedDimensions, Integer.toString(world.provider.getDimension())))
                    return;
                if (!def.isValidYLevel(event.getPos()))
                    return;
                if (!def.isValidPool(world, event.getPos()))
                    return;
                Biome biome = world.getBiome(event.getPos());
                if (def.isBlacklistedBiome(biome)) {
                    if (def.isNearbyBiomeValid(world, event.getPos(), def.biomeSearchDistanceBlacklisted))
                        event.setResult(Event.Result.ALLOW);
                    return;
                }
                if (def.isValidBiome(biome) || def.isNearbyBiomeValid(world, event.getPos(), def.biomeSearchDistanceNormal))
                    event.setResult(Event.Result.ALLOW);
                return;
            }
        }
    }

}
