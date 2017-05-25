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
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
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

/**
 * Created by CovertJaguar on 5/7/2017 for Railcraft.
 *
 * @author CovertJaguar <http://www.railcraft.info>
 */
public class FiniteFluidEventHandler implements IIroncladEventHandler {

    public static FiniteFluidEventHandler INSTANCE = new FiniteFluidEventHandler();
    private static final int BIOME_SCAN_SKIP_LENGTH = 4;
    public static String CAT_FLUID_DEF = "fluid_def";
    private boolean enableFluidReplenishmentOverride;
    private List<FluidDef> fluidDefs = new ArrayList<>();

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
            int originX = pos.getX() >> 4;
            int originZ = pos.getZ() >> 4;

            int minX = originX - searchDistance;
            int maxX = originX + searchDistance;
            int minZ = originZ - searchDistance;
            int maxZ = originZ + searchDistance;

            List<ChunkPos> chunks = new ArrayList<>(81);

            BlockPos.MutableBlockPos targetPos = new BlockPos.MutableBlockPos();
            for (int chunkX = minX; chunkX <= maxX; chunkX++) {
                for (int chunkZ = minZ; chunkZ <= maxZ; chunkZ++) {
                    chunks.add(new ChunkPos(chunkX, chunkZ));
                }
            }

            chunks.sort(Comparator.comparingDouble(value -> {
                double diffX = value.chunkXPos - originX;
                double diffZ = value.chunkZPos - originZ;
                return diffX * diffX + diffZ * diffZ;
            }));

//            int checks = 0;
            for (ChunkPos chunkPos : chunks) {
                targetPos.setPos(chunkPos.chunkXPos * 16, 64, chunkPos.chunkZPos * 16);
                if (world.isBlockLoaded(targetPos)) {
                    Chunk chunk = world.getChunkFromChunkCoords(chunkPos.chunkXPos, chunkPos.chunkZPos);
                    for (int x = 0; x < 16; x += BIOME_SCAN_SKIP_LENGTH) {
                        for (int z = 0; z < 16; z += BIOME_SCAN_SKIP_LENGTH) {
                            targetPos.setPos(chunk.xPosition * 16 + x, 64, chunk.zPosition * 16 + z);
                            Biome biome = chunk.getBiome(targetPos, world.getBiomeProvider());
//                            checks++;
                            if (isValidBiome(biome)) {
//                                System.out.println("r: success c:" + checks);
                                return true;
                            }
                        }
                    }
                } else return true;
            }

//            System.out.println("r: failed c:" + chunks.size() + " b:" + checks);
            return false;
        }

        private boolean isFluidBlock(IBlockState state) {
            return ArrayUtils.contains(blocks, state.getBlock().getRegistryName().toString());
        }

        private boolean isValidPool(World world, BlockPos pos) {
            if (minPoolSize <= 0)
                return true;
            Deque<BlockPos> fluidToExpand = new ArrayDeque<>();
            Collection<BlockPos> visitedBlocks = new HashSet<>();
            fluidToExpand.add(pos);
            visitedBlocks.add(pos);
            BlockPos.MutableBlockPos targetPos = new BlockPos.MutableBlockPos();
            BlockPos nextBlock;
            while ((nextBlock = fluidToExpand.poll()) != null && visitedBlocks.size() < minPoolSize) {
                for (EnumFacing facing : EnumFacing.VALUES) {
                    targetPos.setPos(nextBlock);
                    targetPos.move(facing);
                    if (isValidYLevel(targetPos) && !visitedBlocks.contains(targetPos)) {
                        IBlockState targetState = world.getBlockState(targetPos);
                        if (isFluidBlock(targetState)) {
                            BlockPos immutable = targetPos.toImmutable();
                            fluidToExpand.addLast(immutable);
                            visitedBlocks.add(immutable);
                        }
                    }
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
