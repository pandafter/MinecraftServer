package net.minecraft.data.worldgen;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.structure.pools.WorldGenFeatureDefinedStructurePoolStructure;
import net.minecraft.world.level.levelgen.structure.pools.WorldGenFeatureDefinedStructurePoolTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.ProcessorList;

public class WorldGenFeatureBastionPieces {

    public static final ResourceKey<WorldGenFeatureDefinedStructurePoolTemplate> START = WorldGenFeaturePieces.createKey("bastion/starts");

    public WorldGenFeatureBastionPieces() {}

    public static void bootstrap(BootstrapContext<WorldGenFeatureDefinedStructurePoolTemplate> bootstrapcontext) {
        HolderGetter<ProcessorList> holdergetter = bootstrapcontext.lookup(Registries.PROCESSOR_LIST);
        Holder<ProcessorList> holder = holdergetter.getOrThrow(ProcessorLists.BASTION_GENERIC_DEGRADATION);
        HolderGetter<WorldGenFeatureDefinedStructurePoolTemplate> holdergetter1 = bootstrapcontext.lookup(Registries.TEMPLATE_POOL);
        Holder<WorldGenFeatureDefinedStructurePoolTemplate> holder1 = holdergetter1.getOrThrow(WorldGenFeaturePieces.EMPTY);

        bootstrapcontext.register(WorldGenFeatureBastionPieces.START, new WorldGenFeatureDefinedStructurePoolTemplate(holder1, ImmutableList.of(Pair.of(WorldGenFeatureDefinedStructurePoolStructure.single("bastion/units/air_base", holder), 1), Pair.of(WorldGenFeatureDefinedStructurePoolStructure.single("bastion/hoglin_stable/air_base", holder), 1), Pair.of(WorldGenFeatureDefinedStructurePoolStructure.single("bastion/treasure/big_air_full", holder), 1), Pair.of(WorldGenFeatureDefinedStructurePoolStructure.single("bastion/bridge/starting_pieces/entrance_base", holder), 1)), WorldGenFeatureDefinedStructurePoolTemplate.Matching.RIGID));
        WorldGenFeatureBastionUnits.bootstrap(bootstrapcontext);
        WorldGenFeatureBastionHoglinStable.bootstrap(bootstrapcontext);
        WorldGenFeatureBastionTreasure.bootstrap(bootstrapcontext);
        WorldGenFeatureBastionBridge.bootstrap(bootstrapcontext);
        WorldGenFeatureBastionExtra.bootstrap(bootstrapcontext);
    }
}
