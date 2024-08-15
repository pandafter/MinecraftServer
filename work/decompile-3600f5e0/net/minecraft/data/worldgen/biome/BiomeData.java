package net.minecraft.data.worldgen.biome;

import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.world.level.biome.BiomeBase;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.levelgen.carver.WorldGenCarverWrapper;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

public abstract class BiomeData {

    public BiomeData() {}

    public static void bootstrap(BootstrapContext<BiomeBase> bootstrapcontext) {
        HolderGetter<PlacedFeature> holdergetter = bootstrapcontext.lookup(Registries.PLACED_FEATURE);
        HolderGetter<WorldGenCarverWrapper<?>> holdergetter1 = bootstrapcontext.lookup(Registries.CONFIGURED_CARVER);

        bootstrapcontext.register(Biomes.THE_VOID, OverworldBiomes.theVoid(holdergetter, holdergetter1));
        bootstrapcontext.register(Biomes.PLAINS, OverworldBiomes.plains(holdergetter, holdergetter1, false, false, false));
        bootstrapcontext.register(Biomes.SUNFLOWER_PLAINS, OverworldBiomes.plains(holdergetter, holdergetter1, true, false, false));
        bootstrapcontext.register(Biomes.SNOWY_PLAINS, OverworldBiomes.plains(holdergetter, holdergetter1, false, true, false));
        bootstrapcontext.register(Biomes.ICE_SPIKES, OverworldBiomes.plains(holdergetter, holdergetter1, false, true, true));
        bootstrapcontext.register(Biomes.DESERT, OverworldBiomes.desert(holdergetter, holdergetter1));
        bootstrapcontext.register(Biomes.SWAMP, OverworldBiomes.swamp(holdergetter, holdergetter1, (biomesettingsmobs_a) -> {
        }));
        bootstrapcontext.register(Biomes.MANGROVE_SWAMP, OverworldBiomes.mangroveSwamp(holdergetter, holdergetter1, (biomesettingsmobs_a) -> {
        }));
        bootstrapcontext.register(Biomes.FOREST, OverworldBiomes.forest(holdergetter, holdergetter1, false, false, false));
        bootstrapcontext.register(Biomes.FLOWER_FOREST, OverworldBiomes.forest(holdergetter, holdergetter1, false, false, true));
        bootstrapcontext.register(Biomes.BIRCH_FOREST, OverworldBiomes.forest(holdergetter, holdergetter1, true, false, false));
        bootstrapcontext.register(Biomes.DARK_FOREST, OverworldBiomes.darkForest(holdergetter, holdergetter1));
        bootstrapcontext.register(Biomes.OLD_GROWTH_BIRCH_FOREST, OverworldBiomes.forest(holdergetter, holdergetter1, true, true, false));
        bootstrapcontext.register(Biomes.OLD_GROWTH_PINE_TAIGA, OverworldBiomes.oldGrowthTaiga(holdergetter, holdergetter1, false));
        bootstrapcontext.register(Biomes.OLD_GROWTH_SPRUCE_TAIGA, OverworldBiomes.oldGrowthTaiga(holdergetter, holdergetter1, true));
        bootstrapcontext.register(Biomes.TAIGA, OverworldBiomes.taiga(holdergetter, holdergetter1, false));
        bootstrapcontext.register(Biomes.SNOWY_TAIGA, OverworldBiomes.taiga(holdergetter, holdergetter1, true));
        bootstrapcontext.register(Biomes.SAVANNA, OverworldBiomes.savanna(holdergetter, holdergetter1, false, false));
        bootstrapcontext.register(Biomes.SAVANNA_PLATEAU, OverworldBiomes.savanna(holdergetter, holdergetter1, false, true));
        bootstrapcontext.register(Biomes.WINDSWEPT_HILLS, OverworldBiomes.windsweptHills(holdergetter, holdergetter1, false));
        bootstrapcontext.register(Biomes.WINDSWEPT_GRAVELLY_HILLS, OverworldBiomes.windsweptHills(holdergetter, holdergetter1, false));
        bootstrapcontext.register(Biomes.WINDSWEPT_FOREST, OverworldBiomes.windsweptHills(holdergetter, holdergetter1, true));
        bootstrapcontext.register(Biomes.WINDSWEPT_SAVANNA, OverworldBiomes.savanna(holdergetter, holdergetter1, true, false));
        bootstrapcontext.register(Biomes.JUNGLE, OverworldBiomes.jungle(holdergetter, holdergetter1));
        bootstrapcontext.register(Biomes.SPARSE_JUNGLE, OverworldBiomes.sparseJungle(holdergetter, holdergetter1));
        bootstrapcontext.register(Biomes.BAMBOO_JUNGLE, OverworldBiomes.bambooJungle(holdergetter, holdergetter1));
        bootstrapcontext.register(Biomes.BADLANDS, OverworldBiomes.badlands(holdergetter, holdergetter1, false));
        bootstrapcontext.register(Biomes.ERODED_BADLANDS, OverworldBiomes.badlands(holdergetter, holdergetter1, false));
        bootstrapcontext.register(Biomes.WOODED_BADLANDS, OverworldBiomes.badlands(holdergetter, holdergetter1, true));
        bootstrapcontext.register(Biomes.MEADOW, OverworldBiomes.meadowOrCherryGrove(holdergetter, holdergetter1, false));
        bootstrapcontext.register(Biomes.CHERRY_GROVE, OverworldBiomes.meadowOrCherryGrove(holdergetter, holdergetter1, true));
        bootstrapcontext.register(Biomes.GROVE, OverworldBiomes.grove(holdergetter, holdergetter1));
        bootstrapcontext.register(Biomes.SNOWY_SLOPES, OverworldBiomes.snowySlopes(holdergetter, holdergetter1));
        bootstrapcontext.register(Biomes.FROZEN_PEAKS, OverworldBiomes.frozenPeaks(holdergetter, holdergetter1));
        bootstrapcontext.register(Biomes.JAGGED_PEAKS, OverworldBiomes.jaggedPeaks(holdergetter, holdergetter1));
        bootstrapcontext.register(Biomes.STONY_PEAKS, OverworldBiomes.stonyPeaks(holdergetter, holdergetter1));
        bootstrapcontext.register(Biomes.RIVER, OverworldBiomes.river(holdergetter, holdergetter1, false));
        bootstrapcontext.register(Biomes.FROZEN_RIVER, OverworldBiomes.river(holdergetter, holdergetter1, true));
        bootstrapcontext.register(Biomes.BEACH, OverworldBiomes.beach(holdergetter, holdergetter1, false, false));
        bootstrapcontext.register(Biomes.SNOWY_BEACH, OverworldBiomes.beach(holdergetter, holdergetter1, true, false));
        bootstrapcontext.register(Biomes.STONY_SHORE, OverworldBiomes.beach(holdergetter, holdergetter1, false, true));
        bootstrapcontext.register(Biomes.WARM_OCEAN, OverworldBiomes.warmOcean(holdergetter, holdergetter1));
        bootstrapcontext.register(Biomes.LUKEWARM_OCEAN, OverworldBiomes.lukeWarmOcean(holdergetter, holdergetter1, false));
        bootstrapcontext.register(Biomes.DEEP_LUKEWARM_OCEAN, OverworldBiomes.lukeWarmOcean(holdergetter, holdergetter1, true));
        bootstrapcontext.register(Biomes.OCEAN, OverworldBiomes.ocean(holdergetter, holdergetter1, false));
        bootstrapcontext.register(Biomes.DEEP_OCEAN, OverworldBiomes.ocean(holdergetter, holdergetter1, true));
        bootstrapcontext.register(Biomes.COLD_OCEAN, OverworldBiomes.coldOcean(holdergetter, holdergetter1, false));
        bootstrapcontext.register(Biomes.DEEP_COLD_OCEAN, OverworldBiomes.coldOcean(holdergetter, holdergetter1, true));
        bootstrapcontext.register(Biomes.FROZEN_OCEAN, OverworldBiomes.frozenOcean(holdergetter, holdergetter1, false));
        bootstrapcontext.register(Biomes.DEEP_FROZEN_OCEAN, OverworldBiomes.frozenOcean(holdergetter, holdergetter1, true));
        bootstrapcontext.register(Biomes.MUSHROOM_FIELDS, OverworldBiomes.mushroomFields(holdergetter, holdergetter1));
        bootstrapcontext.register(Biomes.DRIPSTONE_CAVES, OverworldBiomes.dripstoneCaves(holdergetter, holdergetter1));
        bootstrapcontext.register(Biomes.LUSH_CAVES, OverworldBiomes.lushCaves(holdergetter, holdergetter1));
        bootstrapcontext.register(Biomes.DEEP_DARK, OverworldBiomes.deepDark(holdergetter, holdergetter1));
        bootstrapcontext.register(Biomes.NETHER_WASTES, NetherBiomes.netherWastes(holdergetter, holdergetter1));
        bootstrapcontext.register(Biomes.WARPED_FOREST, NetherBiomes.warpedForest(holdergetter, holdergetter1));
        bootstrapcontext.register(Biomes.CRIMSON_FOREST, NetherBiomes.crimsonForest(holdergetter, holdergetter1));
        bootstrapcontext.register(Biomes.SOUL_SAND_VALLEY, NetherBiomes.soulSandValley(holdergetter, holdergetter1));
        bootstrapcontext.register(Biomes.BASALT_DELTAS, NetherBiomes.basaltDeltas(holdergetter, holdergetter1));
        bootstrapcontext.register(Biomes.THE_END, EndBiomes.theEnd(holdergetter, holdergetter1));
        bootstrapcontext.register(Biomes.END_HIGHLANDS, EndBiomes.endHighlands(holdergetter, holdergetter1));
        bootstrapcontext.register(Biomes.END_MIDLANDS, EndBiomes.endMidlands(holdergetter, holdergetter1));
        bootstrapcontext.register(Biomes.SMALL_END_ISLANDS, EndBiomes.smallEndIslands(holdergetter, holdergetter1));
        bootstrapcontext.register(Biomes.END_BARRENS, EndBiomes.endBarrens(holdergetter, holdergetter1));
    }
}
