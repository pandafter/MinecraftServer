package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.EnumDirection;
import net.minecraft.core.particles.ParticleParamBlock;
import net.minecraft.core.particles.Particles;
import net.minecraft.server.level.WorldServer;
import net.minecraft.tags.TagsBlock;
import net.minecraft.util.ParticleUtils;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.item.EntityFallingBlock;
import net.minecraft.world.level.GeneratorAccess;
import net.minecraft.world.level.IBlockAccess;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.state.BlockBase;
import net.minecraft.world.level.block.state.IBlockData;

public abstract class BlockFalling extends Block implements Fallable {

    public BlockFalling(BlockBase.Info blockbase_info) {
        super(blockbase_info);
    }

    @Override
    protected abstract MapCodec<? extends BlockFalling> codec();

    @Override
    protected void onPlace(IBlockData iblockdata, World world, BlockPosition blockposition, IBlockData iblockdata1, boolean flag) {
        world.scheduleTick(blockposition, (Block) this, this.getDelayAfterPlace());
    }

    @Override
    protected IBlockData updateShape(IBlockData iblockdata, EnumDirection enumdirection, IBlockData iblockdata1, GeneratorAccess generatoraccess, BlockPosition blockposition, BlockPosition blockposition1) {
        generatoraccess.scheduleTick(blockposition, (Block) this, this.getDelayAfterPlace());
        return super.updateShape(iblockdata, enumdirection, iblockdata1, generatoraccess, blockposition, blockposition1);
    }

    @Override
    protected void tick(IBlockData iblockdata, WorldServer worldserver, BlockPosition blockposition, RandomSource randomsource) {
        if (isFree(worldserver.getBlockState(blockposition.below())) && blockposition.getY() >= worldserver.getMinBuildHeight()) {
            EntityFallingBlock entityfallingblock = EntityFallingBlock.fall(worldserver, blockposition, iblockdata);

            this.falling(entityfallingblock);
        }
    }

    protected void falling(EntityFallingBlock entityfallingblock) {}

    protected int getDelayAfterPlace() {
        return 2;
    }

    public static boolean isFree(IBlockData iblockdata) {
        return iblockdata.isAir() || iblockdata.is(TagsBlock.FIRE) || iblockdata.liquid() || iblockdata.canBeReplaced();
    }

    @Override
    public void animateTick(IBlockData iblockdata, World world, BlockPosition blockposition, RandomSource randomsource) {
        if (randomsource.nextInt(16) == 0) {
            BlockPosition blockposition1 = blockposition.below();

            if (isFree(world.getBlockState(blockposition1))) {
                ParticleUtils.spawnParticleBelow(world, blockposition, randomsource, new ParticleParamBlock(Particles.FALLING_DUST, iblockdata));
            }
        }

    }

    public int getDustColor(IBlockData iblockdata, IBlockAccess iblockaccess, BlockPosition blockposition) {
        return -16777216;
    }
}
