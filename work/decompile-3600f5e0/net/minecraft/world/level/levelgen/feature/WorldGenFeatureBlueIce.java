package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.EnumDirection;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.GeneratorAccessSeed;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.levelgen.feature.configurations.WorldGenFeatureEmptyConfiguration;

public class WorldGenFeatureBlueIce extends WorldGenerator<WorldGenFeatureEmptyConfiguration> {

    public WorldGenFeatureBlueIce(Codec<WorldGenFeatureEmptyConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<WorldGenFeatureEmptyConfiguration> featureplacecontext) {
        BlockPosition blockposition = featureplacecontext.origin();
        GeneratorAccessSeed generatoraccessseed = featureplacecontext.level();
        RandomSource randomsource = featureplacecontext.random();

        if (blockposition.getY() > generatoraccessseed.getSeaLevel() - 1) {
            return false;
        } else if (!generatoraccessseed.getBlockState(blockposition).is(Blocks.WATER) && !generatoraccessseed.getBlockState(blockposition.below()).is(Blocks.WATER)) {
            return false;
        } else {
            boolean flag = false;
            EnumDirection[] aenumdirection = EnumDirection.values();
            int i = aenumdirection.length;

            int j;

            for (j = 0; j < i; ++j) {
                EnumDirection enumdirection = aenumdirection[j];

                if (enumdirection != EnumDirection.DOWN && generatoraccessseed.getBlockState(blockposition.relative(enumdirection)).is(Blocks.PACKED_ICE)) {
                    flag = true;
                    break;
                }
            }

            if (!flag) {
                return false;
            } else {
                generatoraccessseed.setBlock(blockposition, Blocks.BLUE_ICE.defaultBlockState(), 2);

                for (int k = 0; k < 200; ++k) {
                    i = randomsource.nextInt(5) - randomsource.nextInt(6);
                    j = 3;
                    if (i < 2) {
                        j += i / 2;
                    }

                    if (j >= 1) {
                        BlockPosition blockposition1 = blockposition.offset(randomsource.nextInt(j) - randomsource.nextInt(j), i, randomsource.nextInt(j) - randomsource.nextInt(j));
                        IBlockData iblockdata = generatoraccessseed.getBlockState(blockposition1);

                        if (iblockdata.isAir() || iblockdata.is(Blocks.WATER) || iblockdata.is(Blocks.PACKED_ICE) || iblockdata.is(Blocks.ICE)) {
                            EnumDirection[] aenumdirection1 = EnumDirection.values();
                            int l = aenumdirection1.length;

                            for (int i1 = 0; i1 < l; ++i1) {
                                EnumDirection enumdirection1 = aenumdirection1[i1];
                                IBlockData iblockdata1 = generatoraccessseed.getBlockState(blockposition1.relative(enumdirection1));

                                if (iblockdata1.is(Blocks.BLUE_ICE)) {
                                    generatoraccessseed.setBlock(blockposition1, Blocks.BLUE_ICE.defaultBlockState(), 2);
                                    break;
                                }
                            }
                        }
                    }
                }

                return true;
            }
        }
    }
}
