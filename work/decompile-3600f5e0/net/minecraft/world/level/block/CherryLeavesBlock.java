package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.EnumDirection;
import net.minecraft.core.particles.Particles;
import net.minecraft.util.ParticleUtils;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.state.BlockBase;
import net.minecraft.world.level.block.state.IBlockData;

public class CherryLeavesBlock extends BlockLeaves {

    public static final MapCodec<CherryLeavesBlock> CODEC = simpleCodec(CherryLeavesBlock::new);

    @Override
    public MapCodec<CherryLeavesBlock> codec() {
        return CherryLeavesBlock.CODEC;
    }

    public CherryLeavesBlock(BlockBase.Info blockbase_info) {
        super(blockbase_info);
    }

    @Override
    public void animateTick(IBlockData iblockdata, World world, BlockPosition blockposition, RandomSource randomsource) {
        super.animateTick(iblockdata, world, blockposition, randomsource);
        if (randomsource.nextInt(10) == 0) {
            BlockPosition blockposition1 = blockposition.below();
            IBlockData iblockdata1 = world.getBlockState(blockposition1);

            if (!isFaceFull(iblockdata1.getCollisionShape(world, blockposition1), EnumDirection.UP)) {
                ParticleUtils.spawnParticleBelow(world, blockposition, randomsource, Particles.CHERRY_LEAVES);
            }
        }
    }
}
