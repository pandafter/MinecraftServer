package net.minecraft.world.level.levelgen.structure.placement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.core.BaseBlockPosition;
import net.minecraft.world.level.ChunkCoordIntPair;
import net.minecraft.world.level.chunk.ChunkGeneratorStructureState;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.SeededRandom;

public class RandomSpreadStructurePlacement extends StructurePlacement {

    public static final MapCodec<RandomSpreadStructurePlacement> CODEC = RecordCodecBuilder.mapCodec((instance) -> {
        return placementCodec(instance).and(instance.group(Codec.intRange(0, 4096).fieldOf("spacing").forGetter(RandomSpreadStructurePlacement::spacing), Codec.intRange(0, 4096).fieldOf("separation").forGetter(RandomSpreadStructurePlacement::separation), RandomSpreadType.CODEC.optionalFieldOf("spread_type", RandomSpreadType.LINEAR).forGetter(RandomSpreadStructurePlacement::spreadType))).apply(instance, RandomSpreadStructurePlacement::new);
    }).validate(RandomSpreadStructurePlacement::validate);
    private final int spacing;
    private final int separation;
    private final RandomSpreadType spreadType;

    private static DataResult<RandomSpreadStructurePlacement> validate(RandomSpreadStructurePlacement randomspreadstructureplacement) {
        return randomspreadstructureplacement.spacing <= randomspreadstructureplacement.separation ? DataResult.error(() -> {
            return "Spacing has to be larger than separation";
        }) : DataResult.success(randomspreadstructureplacement);
    }

    public RandomSpreadStructurePlacement(BaseBlockPosition baseblockposition, StructurePlacement.c structureplacement_c, float f, int i, Optional<StructurePlacement.a> optional, int j, int k, RandomSpreadType randomspreadtype) {
        super(baseblockposition, structureplacement_c, f, i, optional);
        this.spacing = j;
        this.separation = k;
        this.spreadType = randomspreadtype;
    }

    public RandomSpreadStructurePlacement(int i, int j, RandomSpreadType randomspreadtype, int k) {
        this(BaseBlockPosition.ZERO, StructurePlacement.c.DEFAULT, 1.0F, k, Optional.empty(), i, j, randomspreadtype);
    }

    public int spacing() {
        return this.spacing;
    }

    public int separation() {
        return this.separation;
    }

    public RandomSpreadType spreadType() {
        return this.spreadType;
    }

    public ChunkCoordIntPair getPotentialStructureChunk(long i, int j, int k) {
        int l = Math.floorDiv(j, this.spacing);
        int i1 = Math.floorDiv(k, this.spacing);
        SeededRandom seededrandom = new SeededRandom(new LegacyRandomSource(0L));

        seededrandom.setLargeFeatureWithSalt(i, l, i1, this.salt());
        int j1 = this.spacing - this.separation;
        int k1 = this.spreadType.evaluate(seededrandom, j1);
        int l1 = this.spreadType.evaluate(seededrandom, j1);

        return new ChunkCoordIntPair(l * this.spacing + k1, i1 * this.spacing + l1);
    }

    @Override
    protected boolean isPlacementChunk(ChunkGeneratorStructureState chunkgeneratorstructurestate, int i, int j) {
        ChunkCoordIntPair chunkcoordintpair = this.getPotentialStructureChunk(chunkgeneratorstructurestate.getLevelSeed(), i, j);

        return chunkcoordintpair.x == i && chunkcoordintpair.z == j;
    }

    @Override
    public StructurePlacementType<?> type() {
        return StructurePlacementType.RANDOM_SPREAD;
    }
}
