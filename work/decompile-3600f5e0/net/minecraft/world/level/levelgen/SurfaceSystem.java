package net.minecraft.world.level.levelgen;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.Holder;
import net.minecraft.core.IRegistry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.util.MathHelper;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkCoordIntPair;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.biome.BiomeBase;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.chunk.BlockColumn;
import net.minecraft.world.level.chunk.IChunkAccess;
import net.minecraft.world.level.dimension.DimensionManager;
import net.minecraft.world.level.levelgen.carver.CarvingContext;
import net.minecraft.world.level.levelgen.synth.NoiseGeneratorNormal;

public class SurfaceSystem {

    private static final IBlockData WHITE_TERRACOTTA = Blocks.WHITE_TERRACOTTA.defaultBlockState();
    private static final IBlockData ORANGE_TERRACOTTA = Blocks.ORANGE_TERRACOTTA.defaultBlockState();
    private static final IBlockData TERRACOTTA = Blocks.TERRACOTTA.defaultBlockState();
    private static final IBlockData YELLOW_TERRACOTTA = Blocks.YELLOW_TERRACOTTA.defaultBlockState();
    private static final IBlockData BROWN_TERRACOTTA = Blocks.BROWN_TERRACOTTA.defaultBlockState();
    private static final IBlockData RED_TERRACOTTA = Blocks.RED_TERRACOTTA.defaultBlockState();
    private static final IBlockData LIGHT_GRAY_TERRACOTTA = Blocks.LIGHT_GRAY_TERRACOTTA.defaultBlockState();
    private static final IBlockData PACKED_ICE = Blocks.PACKED_ICE.defaultBlockState();
    private static final IBlockData SNOW_BLOCK = Blocks.SNOW_BLOCK.defaultBlockState();
    private final IBlockData defaultBlock;
    private final int seaLevel;
    private final IBlockData[] clayBands;
    private final NoiseGeneratorNormal clayBandsOffsetNoise;
    private final NoiseGeneratorNormal badlandsPillarNoise;
    private final NoiseGeneratorNormal badlandsPillarRoofNoise;
    private final NoiseGeneratorNormal badlandsSurfaceNoise;
    private final NoiseGeneratorNormal icebergPillarNoise;
    private final NoiseGeneratorNormal icebergPillarRoofNoise;
    private final NoiseGeneratorNormal icebergSurfaceNoise;
    private final PositionalRandomFactory noiseRandom;
    private final NoiseGeneratorNormal surfaceNoise;
    private final NoiseGeneratorNormal surfaceSecondaryNoise;

    public SurfaceSystem(RandomState randomstate, IBlockData iblockdata, int i, PositionalRandomFactory positionalrandomfactory) {
        this.defaultBlock = iblockdata;
        this.seaLevel = i;
        this.noiseRandom = positionalrandomfactory;
        this.clayBandsOffsetNoise = randomstate.getOrCreateNoise(Noises.CLAY_BANDS_OFFSET);
        this.clayBands = generateBands(positionalrandomfactory.fromHashOf(new MinecraftKey("clay_bands")));
        this.surfaceNoise = randomstate.getOrCreateNoise(Noises.SURFACE);
        this.surfaceSecondaryNoise = randomstate.getOrCreateNoise(Noises.SURFACE_SECONDARY);
        this.badlandsPillarNoise = randomstate.getOrCreateNoise(Noises.BADLANDS_PILLAR);
        this.badlandsPillarRoofNoise = randomstate.getOrCreateNoise(Noises.BADLANDS_PILLAR_ROOF);
        this.badlandsSurfaceNoise = randomstate.getOrCreateNoise(Noises.BADLANDS_SURFACE);
        this.icebergPillarNoise = randomstate.getOrCreateNoise(Noises.ICEBERG_PILLAR);
        this.icebergPillarRoofNoise = randomstate.getOrCreateNoise(Noises.ICEBERG_PILLAR_ROOF);
        this.icebergSurfaceNoise = randomstate.getOrCreateNoise(Noises.ICEBERG_SURFACE);
    }

    public void buildSurface(RandomState randomstate, BiomeManager biomemanager, IRegistry<BiomeBase> iregistry, boolean flag, WorldGenerationContext worldgenerationcontext, final IChunkAccess ichunkaccess, NoiseChunk noisechunk, SurfaceRules.o surfacerules_o) {
        final BlockPosition.MutableBlockPosition blockposition_mutableblockposition = new BlockPosition.MutableBlockPosition();
        final ChunkCoordIntPair chunkcoordintpair = ichunkaccess.getPos();
        int i = chunkcoordintpair.getMinBlockX();
        int j = chunkcoordintpair.getMinBlockZ();
        BlockColumn blockcolumn = new BlockColumn(this) {
            @Override
            public IBlockData getBlock(int k) {
                return ichunkaccess.getBlockState(blockposition_mutableblockposition.setY(k));
            }

            @Override
            public void setBlock(int k, IBlockData iblockdata) {
                LevelHeightAccessor levelheightaccessor = ichunkaccess.getHeightAccessorForGeneration();

                if (k >= levelheightaccessor.getMinBuildHeight() && k < levelheightaccessor.getMaxBuildHeight()) {
                    ichunkaccess.setBlockState(blockposition_mutableblockposition.setY(k), iblockdata, false);
                    if (!iblockdata.getFluidState().isEmpty()) {
                        ichunkaccess.markPosForPostprocessing(blockposition_mutableblockposition);
                    }
                }

            }

            public String toString() {
                return "ChunkBlockColumn " + String.valueOf(chunkcoordintpair);
            }
        };

        Objects.requireNonNull(biomemanager);
        SurfaceRules.g surfacerules_g = new SurfaceRules.g(this, randomstate, ichunkaccess, noisechunk, biomemanager::getBiome, iregistry, worldgenerationcontext);
        SurfaceRules.u surfacerules_u = (SurfaceRules.u) surfacerules_o.apply(surfacerules_g);
        BlockPosition.MutableBlockPosition blockposition_mutableblockposition1 = new BlockPosition.MutableBlockPosition();

        for (int k = 0; k < 16; ++k) {
            for (int l = 0; l < 16; ++l) {
                int i1 = i + k;
                int j1 = j + l;
                int k1 = ichunkaccess.getHeight(HeightMap.Type.WORLD_SURFACE_WG, k, l) + 1;

                blockposition_mutableblockposition.setX(i1).setZ(j1);
                Holder<BiomeBase> holder = biomemanager.getBiome(blockposition_mutableblockposition1.set(i1, flag ? 0 : k1, j1));

                if (holder.is(Biomes.ERODED_BADLANDS)) {
                    this.erodedBadlandsExtension(blockcolumn, i1, j1, k1, ichunkaccess);
                }

                int l1 = ichunkaccess.getHeight(HeightMap.Type.WORLD_SURFACE_WG, k, l) + 1;

                surfacerules_g.updateXZ(i1, j1);
                int i2 = 0;
                int j2 = Integer.MIN_VALUE;
                int k2 = Integer.MAX_VALUE;
                int l2 = ichunkaccess.getMinBuildHeight();

                for (int i3 = l1; i3 >= l2; --i3) {
                    IBlockData iblockdata = blockcolumn.getBlock(i3);

                    if (iblockdata.isAir()) {
                        i2 = 0;
                        j2 = Integer.MIN_VALUE;
                    } else if (!iblockdata.getFluidState().isEmpty()) {
                        if (j2 == Integer.MIN_VALUE) {
                            j2 = i3 + 1;
                        }
                    } else {
                        IBlockData iblockdata1;
                        int j3;

                        if (k2 >= i3) {
                            k2 = DimensionManager.WAY_BELOW_MIN_Y;

                            for (j3 = i3 - 1; j3 >= l2 - 1; --j3) {
                                iblockdata1 = blockcolumn.getBlock(j3);
                                if (!this.isStone(iblockdata1)) {
                                    k2 = j3 + 1;
                                    break;
                                }
                            }
                        }

                        ++i2;
                        j3 = i3 - k2 + 1;
                        surfacerules_g.updateY(i2, j3, j2, i1, i3, j1);
                        if (iblockdata == this.defaultBlock) {
                            iblockdata1 = surfacerules_u.tryApply(i1, i3, j1);
                            if (iblockdata1 != null) {
                                blockcolumn.setBlock(i3, iblockdata1);
                            }
                        }
                    }
                }

                if (holder.is(Biomes.FROZEN_OCEAN) || holder.is(Biomes.DEEP_FROZEN_OCEAN)) {
                    this.frozenOceanExtension(surfacerules_g.getMinSurfaceLevel(), (BiomeBase) holder.value(), blockcolumn, blockposition_mutableblockposition1, i1, j1, k1);
                }
            }
        }

    }

    protected int getSurfaceDepth(int i, int j) {
        double d0 = this.surfaceNoise.getValue((double) i, 0.0D, (double) j);

        return (int) (d0 * 2.75D + 3.0D + this.noiseRandom.at(i, 0, j).nextDouble() * 0.25D);
    }

    protected double getSurfaceSecondary(int i, int j) {
        return this.surfaceSecondaryNoise.getValue((double) i, 0.0D, (double) j);
    }

    private boolean isStone(IBlockData iblockdata) {
        return !iblockdata.isAir() && iblockdata.getFluidState().isEmpty();
    }

    /** @deprecated */
    @Deprecated
    public Optional<IBlockData> topMaterial(SurfaceRules.o surfacerules_o, CarvingContext carvingcontext, Function<BlockPosition, Holder<BiomeBase>> function, IChunkAccess ichunkaccess, NoiseChunk noisechunk, BlockPosition blockposition, boolean flag) {
        SurfaceRules.g surfacerules_g = new SurfaceRules.g(this, carvingcontext.randomState(), ichunkaccess, noisechunk, function, carvingcontext.registryAccess().registryOrThrow(Registries.BIOME), carvingcontext);
        SurfaceRules.u surfacerules_u = (SurfaceRules.u) surfacerules_o.apply(surfacerules_g);
        int i = blockposition.getX();
        int j = blockposition.getY();
        int k = blockposition.getZ();

        surfacerules_g.updateXZ(i, k);
        surfacerules_g.updateY(1, 1, flag ? j + 1 : Integer.MIN_VALUE, i, j, k);
        IBlockData iblockdata = surfacerules_u.tryApply(i, j, k);

        return Optional.ofNullable(iblockdata);
    }

    private void erodedBadlandsExtension(BlockColumn blockcolumn, int i, int j, int k, LevelHeightAccessor levelheightaccessor) {
        double d0 = 0.2D;
        double d1 = Math.min(Math.abs(this.badlandsSurfaceNoise.getValue((double) i, 0.0D, (double) j) * 8.25D), this.badlandsPillarNoise.getValue((double) i * 0.2D, 0.0D, (double) j * 0.2D) * 15.0D);

        if (d1 > 0.0D) {
            double d2 = 0.75D;
            double d3 = 1.5D;
            double d4 = Math.abs(this.badlandsPillarRoofNoise.getValue((double) i * 0.75D, 0.0D, (double) j * 0.75D) * 1.5D);
            double d5 = 64.0D + Math.min(d1 * d1 * 2.5D, Math.ceil(d4 * 50.0D) + 24.0D);
            int l = MathHelper.floor(d5);

            if (k <= l) {
                int i1;

                for (i1 = l; i1 >= levelheightaccessor.getMinBuildHeight(); --i1) {
                    IBlockData iblockdata = blockcolumn.getBlock(i1);

                    if (iblockdata.is(this.defaultBlock.getBlock())) {
                        break;
                    }

                    if (iblockdata.is(Blocks.WATER)) {
                        return;
                    }
                }

                for (i1 = l; i1 >= levelheightaccessor.getMinBuildHeight() && blockcolumn.getBlock(i1).isAir(); --i1) {
                    blockcolumn.setBlock(i1, this.defaultBlock);
                }

            }
        }
    }

    private void frozenOceanExtension(int i, BiomeBase biomebase, BlockColumn blockcolumn, BlockPosition.MutableBlockPosition blockposition_mutableblockposition, int j, int k, int l) {
        double d0 = 1.28D;
        double d1 = Math.min(Math.abs(this.icebergSurfaceNoise.getValue((double) j, 0.0D, (double) k) * 8.25D), this.icebergPillarNoise.getValue((double) j * 1.28D, 0.0D, (double) k * 1.28D) * 15.0D);

        if (d1 > 1.8D) {
            double d2 = 1.17D;
            double d3 = 1.5D;
            double d4 = Math.abs(this.icebergPillarRoofNoise.getValue((double) j * 1.17D, 0.0D, (double) k * 1.17D) * 1.5D);
            double d5 = Math.min(d1 * d1 * 1.2D, Math.ceil(d4 * 40.0D) + 14.0D);

            if (biomebase.shouldMeltFrozenOceanIcebergSlightly(blockposition_mutableblockposition.set(j, 63, k))) {
                d5 -= 2.0D;
            }

            double d6;

            if (d5 > 2.0D) {
                d6 = (double) this.seaLevel - d5 - 7.0D;
                d5 += (double) this.seaLevel;
            } else {
                d5 = 0.0D;
                d6 = 0.0D;
            }

            double d7 = d5;
            RandomSource randomsource = this.noiseRandom.at(j, 0, k);
            int i1 = 2 + randomsource.nextInt(4);
            int j1 = this.seaLevel + 18 + randomsource.nextInt(10);
            int k1 = 0;

            for (int l1 = Math.max(l, (int) d5 + 1); l1 >= i; --l1) {
                if (blockcolumn.getBlock(l1).isAir() && l1 < (int) d7 && randomsource.nextDouble() > 0.01D || blockcolumn.getBlock(l1).is(Blocks.WATER) && l1 > (int) d6 && l1 < this.seaLevel && d6 != 0.0D && randomsource.nextDouble() > 0.15D) {
                    if (k1 <= i1 && l1 > j1) {
                        blockcolumn.setBlock(l1, SurfaceSystem.SNOW_BLOCK);
                        ++k1;
                    } else {
                        blockcolumn.setBlock(l1, SurfaceSystem.PACKED_ICE);
                    }
                }
            }

        }
    }

    private static IBlockData[] generateBands(RandomSource randomsource) {
        IBlockData[] aiblockdata = new IBlockData[192];

        Arrays.fill(aiblockdata, SurfaceSystem.TERRACOTTA);

        int i;

        for (i = 0; i < aiblockdata.length; ++i) {
            i += randomsource.nextInt(5) + 1;
            if (i < aiblockdata.length) {
                aiblockdata[i] = SurfaceSystem.ORANGE_TERRACOTTA;
            }
        }

        makeBands(randomsource, aiblockdata, 1, SurfaceSystem.YELLOW_TERRACOTTA);
        makeBands(randomsource, aiblockdata, 2, SurfaceSystem.BROWN_TERRACOTTA);
        makeBands(randomsource, aiblockdata, 1, SurfaceSystem.RED_TERRACOTTA);
        i = randomsource.nextIntBetweenInclusive(9, 15);
        int j = 0;

        for (int k = 0; j < i && k < aiblockdata.length; k += randomsource.nextInt(16) + 4) {
            aiblockdata[k] = SurfaceSystem.WHITE_TERRACOTTA;
            if (k - 1 > 0 && randomsource.nextBoolean()) {
                aiblockdata[k - 1] = SurfaceSystem.LIGHT_GRAY_TERRACOTTA;
            }

            if (k + 1 < aiblockdata.length && randomsource.nextBoolean()) {
                aiblockdata[k + 1] = SurfaceSystem.LIGHT_GRAY_TERRACOTTA;
            }

            ++j;
        }

        return aiblockdata;
    }

    private static void makeBands(RandomSource randomsource, IBlockData[] aiblockdata, int i, IBlockData iblockdata) {
        int j = randomsource.nextIntBetweenInclusive(6, 15);

        for (int k = 0; k < j; ++k) {
            int l = i + randomsource.nextInt(3);
            int i1 = randomsource.nextInt(aiblockdata.length);

            for (int j1 = 0; i1 + j1 < aiblockdata.length && j1 < l; ++j1) {
                aiblockdata[i1 + j1] = iblockdata;
            }
        }

    }

    protected IBlockData getBand(int i, int j, int k) {
        int l = (int) Math.round(this.clayBandsOffsetNoise.getValue((double) i, 0.0D, (double) k) * 4.0D);

        return this.clayBands[(j + l + this.clayBands.length) % this.clayBands.length];
    }
}
