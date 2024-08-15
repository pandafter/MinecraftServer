package net.minecraft.data.worldgen.features;

import java.util.List;
import net.minecraft.core.EnumDirection;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.data.worldgen.ProcessorLists;
import net.minecraft.data.worldgen.placement.PlacementUtils;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagsBlock;
import net.minecraft.util.random.SimpleWeightedRandomList;
import net.minecraft.util.valueproviders.ClampedNormalFloat;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.util.valueproviders.UniformFloat;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.util.valueproviders.WeightedListInt;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CaveVines;
import net.minecraft.world.level.block.CaveVinesBlock;
import net.minecraft.world.level.block.MultifaceBlock;
import net.minecraft.world.level.block.SmallDripleafBlock;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.block.state.properties.BlockProperties;
import net.minecraft.world.level.levelgen.GeodeBlockSettings;
import net.minecraft.world.level.levelgen.GeodeCrackSettings;
import net.minecraft.world.level.levelgen.GeodeLayerSettings;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.level.levelgen.feature.FossilFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.WorldGenFeatureConfigured;
import net.minecraft.world.level.levelgen.feature.WorldGenerator;
import net.minecraft.world.level.levelgen.feature.configurations.BlockColumnConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.DripstoneClusterConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.GeodeConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.LargeDripstoneConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.MultifaceGrowthConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.PointedDripstoneConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.RootSystemConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.SculkPatchConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.UnderwaterMagmaConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.VegetationPatchConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.WorldGenFeatureBlockConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.WorldGenFeatureChoiceConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.WorldGenFeatureRandom2;
import net.minecraft.world.level.levelgen.feature.stateproviders.RandomizedIntStateProvider;
import net.minecraft.world.level.levelgen.feature.stateproviders.WorldGenFeatureStateProvider;
import net.minecraft.world.level.levelgen.feature.stateproviders.WorldGenFeatureStateProviderWeighted;
import net.minecraft.world.level.levelgen.placement.CaveSurface;
import net.minecraft.world.level.levelgen.placement.EnvironmentScanPlacement;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.placement.RandomOffsetPlacement;
import net.minecraft.world.level.levelgen.structure.templatesystem.ProcessorList;

public class CaveFeatures {

    public static final ResourceKey<WorldGenFeatureConfigured<?, ?>> MONSTER_ROOM = FeatureUtils.createKey("monster_room");
    public static final ResourceKey<WorldGenFeatureConfigured<?, ?>> FOSSIL_COAL = FeatureUtils.createKey("fossil_coal");
    public static final ResourceKey<WorldGenFeatureConfigured<?, ?>> FOSSIL_DIAMONDS = FeatureUtils.createKey("fossil_diamonds");
    public static final ResourceKey<WorldGenFeatureConfigured<?, ?>> DRIPSTONE_CLUSTER = FeatureUtils.createKey("dripstone_cluster");
    public static final ResourceKey<WorldGenFeatureConfigured<?, ?>> LARGE_DRIPSTONE = FeatureUtils.createKey("large_dripstone");
    public static final ResourceKey<WorldGenFeatureConfigured<?, ?>> POINTED_DRIPSTONE = FeatureUtils.createKey("pointed_dripstone");
    public static final ResourceKey<WorldGenFeatureConfigured<?, ?>> UNDERWATER_MAGMA = FeatureUtils.createKey("underwater_magma");
    public static final ResourceKey<WorldGenFeatureConfigured<?, ?>> GLOW_LICHEN = FeatureUtils.createKey("glow_lichen");
    public static final ResourceKey<WorldGenFeatureConfigured<?, ?>> ROOTED_AZALEA_TREE = FeatureUtils.createKey("rooted_azalea_tree");
    public static final ResourceKey<WorldGenFeatureConfigured<?, ?>> CAVE_VINE = FeatureUtils.createKey("cave_vine");
    public static final ResourceKey<WorldGenFeatureConfigured<?, ?>> CAVE_VINE_IN_MOSS = FeatureUtils.createKey("cave_vine_in_moss");
    public static final ResourceKey<WorldGenFeatureConfigured<?, ?>> MOSS_VEGETATION = FeatureUtils.createKey("moss_vegetation");
    public static final ResourceKey<WorldGenFeatureConfigured<?, ?>> MOSS_PATCH = FeatureUtils.createKey("moss_patch");
    public static final ResourceKey<WorldGenFeatureConfigured<?, ?>> MOSS_PATCH_BONEMEAL = FeatureUtils.createKey("moss_patch_bonemeal");
    public static final ResourceKey<WorldGenFeatureConfigured<?, ?>> DRIPLEAF = FeatureUtils.createKey("dripleaf");
    public static final ResourceKey<WorldGenFeatureConfigured<?, ?>> CLAY_WITH_DRIPLEAVES = FeatureUtils.createKey("clay_with_dripleaves");
    public static final ResourceKey<WorldGenFeatureConfigured<?, ?>> CLAY_POOL_WITH_DRIPLEAVES = FeatureUtils.createKey("clay_pool_with_dripleaves");
    public static final ResourceKey<WorldGenFeatureConfigured<?, ?>> LUSH_CAVES_CLAY = FeatureUtils.createKey("lush_caves_clay");
    public static final ResourceKey<WorldGenFeatureConfigured<?, ?>> MOSS_PATCH_CEILING = FeatureUtils.createKey("moss_patch_ceiling");
    public static final ResourceKey<WorldGenFeatureConfigured<?, ?>> SPORE_BLOSSOM = FeatureUtils.createKey("spore_blossom");
    public static final ResourceKey<WorldGenFeatureConfigured<?, ?>> AMETHYST_GEODE = FeatureUtils.createKey("amethyst_geode");
    public static final ResourceKey<WorldGenFeatureConfigured<?, ?>> SCULK_PATCH_DEEP_DARK = FeatureUtils.createKey("sculk_patch_deep_dark");
    public static final ResourceKey<WorldGenFeatureConfigured<?, ?>> SCULK_PATCH_ANCIENT_CITY = FeatureUtils.createKey("sculk_patch_ancient_city");
    public static final ResourceKey<WorldGenFeatureConfigured<?, ?>> SCULK_VEIN = FeatureUtils.createKey("sculk_vein");

    public CaveFeatures() {}

    private static Holder<PlacedFeature> makeDripleaf(EnumDirection enumdirection) {
        return PlacementUtils.inlinePlaced(WorldGenerator.BLOCK_COLUMN, new BlockColumnConfiguration(List.of(BlockColumnConfiguration.layer(new WeightedListInt(SimpleWeightedRandomList.builder().add(UniformInt.of(0, 4), 2).add(ConstantInt.of(0), 1).build()), WorldGenFeatureStateProvider.simple((IBlockData) Blocks.BIG_DRIPLEAF_STEM.defaultBlockState().setValue(BlockProperties.HORIZONTAL_FACING, enumdirection))), BlockColumnConfiguration.layer(ConstantInt.of(1), WorldGenFeatureStateProvider.simple((IBlockData) Blocks.BIG_DRIPLEAF.defaultBlockState().setValue(BlockProperties.HORIZONTAL_FACING, enumdirection)))), EnumDirection.UP, BlockPredicate.ONLY_IN_AIR_OR_WATER_PREDICATE, true));
    }

    private static Holder<PlacedFeature> makeSmallDripleaf() {
        return PlacementUtils.inlinePlaced(WorldGenerator.SIMPLE_BLOCK, new WorldGenFeatureBlockConfiguration(new WorldGenFeatureStateProviderWeighted(SimpleWeightedRandomList.builder().add((IBlockData) Blocks.SMALL_DRIPLEAF.defaultBlockState().setValue(SmallDripleafBlock.FACING, EnumDirection.EAST), 1).add((IBlockData) Blocks.SMALL_DRIPLEAF.defaultBlockState().setValue(SmallDripleafBlock.FACING, EnumDirection.WEST), 1).add((IBlockData) Blocks.SMALL_DRIPLEAF.defaultBlockState().setValue(SmallDripleafBlock.FACING, EnumDirection.NORTH), 1).add((IBlockData) Blocks.SMALL_DRIPLEAF.defaultBlockState().setValue(SmallDripleafBlock.FACING, EnumDirection.SOUTH), 1))));
    }

    public static void bootstrap(BootstrapContext<WorldGenFeatureConfigured<?, ?>> bootstrapcontext) {
        HolderGetter<WorldGenFeatureConfigured<?, ?>> holdergetter = bootstrapcontext.lookup(Registries.CONFIGURED_FEATURE);
        HolderGetter<ProcessorList> holdergetter1 = bootstrapcontext.lookup(Registries.PROCESSOR_LIST);

        FeatureUtils.register(bootstrapcontext, CaveFeatures.MONSTER_ROOM, WorldGenerator.MONSTER_ROOM);
        List<MinecraftKey> list = List.of(new MinecraftKey("fossil/spine_1"), new MinecraftKey("fossil/spine_2"), new MinecraftKey("fossil/spine_3"), new MinecraftKey("fossil/spine_4"), new MinecraftKey("fossil/skull_1"), new MinecraftKey("fossil/skull_2"), new MinecraftKey("fossil/skull_3"), new MinecraftKey("fossil/skull_4"));
        List<MinecraftKey> list1 = List.of(new MinecraftKey("fossil/spine_1_coal"), new MinecraftKey("fossil/spine_2_coal"), new MinecraftKey("fossil/spine_3_coal"), new MinecraftKey("fossil/spine_4_coal"), new MinecraftKey("fossil/skull_1_coal"), new MinecraftKey("fossil/skull_2_coal"), new MinecraftKey("fossil/skull_3_coal"), new MinecraftKey("fossil/skull_4_coal"));
        Holder<ProcessorList> holder = holdergetter1.getOrThrow(ProcessorLists.FOSSIL_ROT);

        FeatureUtils.register(bootstrapcontext, CaveFeatures.FOSSIL_COAL, WorldGenerator.FOSSIL, new FossilFeatureConfiguration(list, list1, holder, holdergetter1.getOrThrow(ProcessorLists.FOSSIL_COAL), 4));
        FeatureUtils.register(bootstrapcontext, CaveFeatures.FOSSIL_DIAMONDS, WorldGenerator.FOSSIL, new FossilFeatureConfiguration(list, list1, holder, holdergetter1.getOrThrow(ProcessorLists.FOSSIL_DIAMONDS), 4));
        FeatureUtils.register(bootstrapcontext, CaveFeatures.DRIPSTONE_CLUSTER, WorldGenerator.DRIPSTONE_CLUSTER, new DripstoneClusterConfiguration(12, UniformInt.of(3, 6), UniformInt.of(2, 8), 1, 3, UniformInt.of(2, 4), UniformFloat.of(0.3F, 0.7F), ClampedNormalFloat.of(0.1F, 0.3F, 0.1F, 0.9F), 0.1F, 3, 8));
        FeatureUtils.register(bootstrapcontext, CaveFeatures.LARGE_DRIPSTONE, WorldGenerator.LARGE_DRIPSTONE, new LargeDripstoneConfiguration(30, UniformInt.of(3, 19), UniformFloat.of(0.4F, 2.0F), 0.33F, UniformFloat.of(0.3F, 0.9F), UniformFloat.of(0.4F, 1.0F), UniformFloat.of(0.0F, 0.3F), 4, 0.6F));
        FeatureUtils.register(bootstrapcontext, CaveFeatures.POINTED_DRIPSTONE, WorldGenerator.SIMPLE_RANDOM_SELECTOR, new WorldGenFeatureRandom2(HolderSet.direct(PlacementUtils.inlinePlaced(WorldGenerator.POINTED_DRIPSTONE, new PointedDripstoneConfiguration(0.2F, 0.7F, 0.5F, 0.5F), EnvironmentScanPlacement.scanningFor(EnumDirection.DOWN, BlockPredicate.solid(), BlockPredicate.ONLY_IN_AIR_OR_WATER_PREDICATE, 12), RandomOffsetPlacement.vertical(ConstantInt.of(1))), PlacementUtils.inlinePlaced(WorldGenerator.POINTED_DRIPSTONE, new PointedDripstoneConfiguration(0.2F, 0.7F, 0.5F, 0.5F), EnvironmentScanPlacement.scanningFor(EnumDirection.UP, BlockPredicate.solid(), BlockPredicate.ONLY_IN_AIR_OR_WATER_PREDICATE, 12), RandomOffsetPlacement.vertical(ConstantInt.of(-1))))));
        FeatureUtils.register(bootstrapcontext, CaveFeatures.UNDERWATER_MAGMA, WorldGenerator.UNDERWATER_MAGMA, new UnderwaterMagmaConfiguration(5, 1, 0.5F));
        MultifaceBlock multifaceblock = (MultifaceBlock)Blocks.GLOW_LICHEN;

        FeatureUtils.register(bootstrapcontext, CaveFeatures.GLOW_LICHEN, WorldGenerator.MULTIFACE_GROWTH, new MultifaceGrowthConfiguration(multifaceblock, 20, false, true, true, 0.5F, HolderSet.direct(Block::builtInRegistryHolder, (Object[])(Blocks.STONE, Blocks.ANDESITE, Blocks.DIORITE, Blocks.GRANITE, Blocks.DRIPSTONE_BLOCK, Blocks.CALCITE, Blocks.TUFF, Blocks.DEEPSLATE))));
        FeatureUtils.register(bootstrapcontext, CaveFeatures.ROOTED_AZALEA_TREE, WorldGenerator.ROOT_SYSTEM, new RootSystemConfiguration(PlacementUtils.inlinePlaced(holdergetter.getOrThrow(TreeFeatures.AZALEA_TREE)), 3, 3, TagsBlock.AZALEA_ROOT_REPLACEABLE, WorldGenFeatureStateProvider.simple(Blocks.ROOTED_DIRT), 20, 100, 3, 2, WorldGenFeatureStateProvider.simple(Blocks.HANGING_ROOTS), 20, 2, BlockPredicate.allOf(BlockPredicate.anyOf(BlockPredicate.matchesBlocks(List.of(Blocks.AIR, Blocks.CAVE_AIR, Blocks.VOID_AIR)), BlockPredicate.matchesTag(TagsBlock.REPLACEABLE_BY_TREES)), BlockPredicate.matchesTag(EnumDirection.DOWN.getNormal(), TagsBlock.AZALEA_GROWS_ON))));
        WorldGenFeatureStateProviderWeighted worldgenfeaturestateproviderweighted = new WorldGenFeatureStateProviderWeighted(SimpleWeightedRandomList.builder().add(Blocks.CAVE_VINES_PLANT.defaultBlockState(), 4).add((IBlockData)Blocks.CAVE_VINES_PLANT.defaultBlockState().setValue(CaveVines.BERRIES, true), 1));
        RandomizedIntStateProvider randomizedintstateprovider = new RandomizedIntStateProvider(new WorldGenFeatureStateProviderWeighted(SimpleWeightedRandomList.builder().add(Blocks.CAVE_VINES.defaultBlockState(), 4).add((IBlockData)Blocks.CAVE_VINES.defaultBlockState().setValue(CaveVines.BERRIES, true), 1)), CaveVinesBlock.AGE, UniformInt.of(23, 25));

        FeatureUtils.register(bootstrapcontext, CaveFeatures.CAVE_VINE, WorldGenerator.BLOCK_COLUMN, new BlockColumnConfiguration(List.of(BlockColumnConfiguration.layer(new WeightedListInt(SimpleWeightedRandomList.builder().add(UniformInt.of(0, 19), 2).add(UniformInt.of(0, 2), 3).add(UniformInt.of(0, 6), 10).build()), worldgenfeaturestateproviderweighted), BlockColumnConfiguration.layer(ConstantInt.of(1), randomizedintstateprovider)), EnumDirection.DOWN, BlockPredicate.ONLY_IN_AIR_PREDICATE, true));
        FeatureUtils.register(bootstrapcontext, CaveFeatures.CAVE_VINE_IN_MOSS, WorldGenerator.BLOCK_COLUMN, new BlockColumnConfiguration(List.of(BlockColumnConfiguration.layer(new WeightedListInt(SimpleWeightedRandomList.builder().add(UniformInt.of(0, 3), 5).add(UniformInt.of(1, 7), 1).build()), worldgenfeaturestateproviderweighted), BlockColumnConfiguration.layer(ConstantInt.of(1), randomizedintstateprovider)), EnumDirection.DOWN, BlockPredicate.ONLY_IN_AIR_PREDICATE, true));
        FeatureUtils.register(bootstrapcontext, CaveFeatures.MOSS_VEGETATION, WorldGenerator.SIMPLE_BLOCK, new WorldGenFeatureBlockConfiguration(new WorldGenFeatureStateProviderWeighted(SimpleWeightedRandomList.builder().add(Blocks.FLOWERING_AZALEA.defaultBlockState(), 4).add(Blocks.AZALEA.defaultBlockState(), 7).add(Blocks.MOSS_CARPET.defaultBlockState(), 25).add(Blocks.SHORT_GRASS.defaultBlockState(), 50).add(Blocks.TALL_GRASS.defaultBlockState(), 10))));
        FeatureUtils.register(bootstrapcontext, CaveFeatures.MOSS_PATCH, WorldGenerator.VEGETATION_PATCH, new VegetationPatchConfiguration(TagsBlock.MOSS_REPLACEABLE, WorldGenFeatureStateProvider.simple(Blocks.MOSS_BLOCK), PlacementUtils.inlinePlaced(holdergetter.getOrThrow(CaveFeatures.MOSS_VEGETATION)), CaveSurface.FLOOR, ConstantInt.of(1), 0.0F, 5, 0.8F, UniformInt.of(4, 7), 0.3F));
        FeatureUtils.register(bootstrapcontext, CaveFeatures.MOSS_PATCH_BONEMEAL, WorldGenerator.VEGETATION_PATCH, new VegetationPatchConfiguration(TagsBlock.MOSS_REPLACEABLE, WorldGenFeatureStateProvider.simple(Blocks.MOSS_BLOCK), PlacementUtils.inlinePlaced(holdergetter.getOrThrow(CaveFeatures.MOSS_VEGETATION)), CaveSurface.FLOOR, ConstantInt.of(1), 0.0F, 5, 0.6F, UniformInt.of(1, 2), 0.75F));
        FeatureUtils.register(bootstrapcontext, CaveFeatures.DRIPLEAF, WorldGenerator.SIMPLE_RANDOM_SELECTOR, new WorldGenFeatureRandom2(HolderSet.direct(makeSmallDripleaf(), makeDripleaf(EnumDirection.EAST), makeDripleaf(EnumDirection.WEST), makeDripleaf(EnumDirection.SOUTH), makeDripleaf(EnumDirection.NORTH))));
        FeatureUtils.register(bootstrapcontext, CaveFeatures.CLAY_WITH_DRIPLEAVES, WorldGenerator.VEGETATION_PATCH, new VegetationPatchConfiguration(TagsBlock.LUSH_GROUND_REPLACEABLE, WorldGenFeatureStateProvider.simple(Blocks.CLAY), PlacementUtils.inlinePlaced(holdergetter.getOrThrow(CaveFeatures.DRIPLEAF)), CaveSurface.FLOOR, ConstantInt.of(3), 0.8F, 2, 0.05F, UniformInt.of(4, 7), 0.7F));
        FeatureUtils.register(bootstrapcontext, CaveFeatures.CLAY_POOL_WITH_DRIPLEAVES, WorldGenerator.WATERLOGGED_VEGETATION_PATCH, new VegetationPatchConfiguration(TagsBlock.LUSH_GROUND_REPLACEABLE, WorldGenFeatureStateProvider.simple(Blocks.CLAY), PlacementUtils.inlinePlaced(holdergetter.getOrThrow(CaveFeatures.DRIPLEAF)), CaveSurface.FLOOR, ConstantInt.of(3), 0.8F, 5, 0.1F, UniformInt.of(4, 7), 0.7F));
        FeatureUtils.register(bootstrapcontext, CaveFeatures.LUSH_CAVES_CLAY, WorldGenerator.RANDOM_BOOLEAN_SELECTOR, new WorldGenFeatureChoiceConfiguration(PlacementUtils.inlinePlaced(holdergetter.getOrThrow(CaveFeatures.CLAY_WITH_DRIPLEAVES)), PlacementUtils.inlinePlaced(holdergetter.getOrThrow(CaveFeatures.CLAY_POOL_WITH_DRIPLEAVES))));
        FeatureUtils.register(bootstrapcontext, CaveFeatures.MOSS_PATCH_CEILING, WorldGenerator.VEGETATION_PATCH, new VegetationPatchConfiguration(TagsBlock.MOSS_REPLACEABLE, WorldGenFeatureStateProvider.simple(Blocks.MOSS_BLOCK), PlacementUtils.inlinePlaced(holdergetter.getOrThrow(CaveFeatures.CAVE_VINE_IN_MOSS)), CaveSurface.CEILING, UniformInt.of(1, 2), 0.0F, 5, 0.08F, UniformInt.of(4, 7), 0.3F));
        FeatureUtils.register(bootstrapcontext, CaveFeatures.SPORE_BLOSSOM, WorldGenerator.SIMPLE_BLOCK, new WorldGenFeatureBlockConfiguration(WorldGenFeatureStateProvider.simple(Blocks.SPORE_BLOSSOM)));
        FeatureUtils.register(bootstrapcontext, CaveFeatures.AMETHYST_GEODE, WorldGenerator.GEODE, new GeodeConfiguration(new GeodeBlockSettings(WorldGenFeatureStateProvider.simple(Blocks.AIR), WorldGenFeatureStateProvider.simple(Blocks.AMETHYST_BLOCK), WorldGenFeatureStateProvider.simple(Blocks.BUDDING_AMETHYST), WorldGenFeatureStateProvider.simple(Blocks.CALCITE), WorldGenFeatureStateProvider.simple(Blocks.SMOOTH_BASALT), List.of(Blocks.SMALL_AMETHYST_BUD.defaultBlockState(), Blocks.MEDIUM_AMETHYST_BUD.defaultBlockState(), Blocks.LARGE_AMETHYST_BUD.defaultBlockState(), Blocks.AMETHYST_CLUSTER.defaultBlockState()), TagsBlock.FEATURES_CANNOT_REPLACE, TagsBlock.GEODE_INVALID_BLOCKS), new GeodeLayerSettings(1.7D, 2.2D, 3.2D, 4.2D), new GeodeCrackSettings(0.95D, 2.0D, 2), 0.35D, 0.083D, true, UniformInt.of(4, 6), UniformInt.of(3, 4), UniformInt.of(1, 2), -16, 16, 0.05D, 1));
        FeatureUtils.register(bootstrapcontext, CaveFeatures.SCULK_PATCH_DEEP_DARK, WorldGenerator.SCULK_PATCH, new SculkPatchConfiguration(10, 32, 64, 0, 1, ConstantInt.of(0), 0.5F));
        FeatureUtils.register(bootstrapcontext, CaveFeatures.SCULK_PATCH_ANCIENT_CITY, WorldGenerator.SCULK_PATCH, new SculkPatchConfiguration(10, 32, 64, 0, 1, UniformInt.of(1, 3), 0.5F));
        MultifaceBlock multifaceblock1 = (MultifaceBlock)Blocks.SCULK_VEIN;

        FeatureUtils.register(bootstrapcontext, CaveFeatures.SCULK_VEIN, WorldGenerator.MULTIFACE_GROWTH, new MultifaceGrowthConfiguration(multifaceblock1, 20, true, true, true, 1.0F, HolderSet.direct(Block::builtInRegistryHolder, (Object[])(Blocks.STONE, Blocks.ANDESITE, Blocks.DIORITE, Blocks.GRANITE, Blocks.DRIPSTONE_BLOCK, Blocks.CALCITE, Blocks.TUFF, Blocks.DEEPSLATE))));
    }
}
