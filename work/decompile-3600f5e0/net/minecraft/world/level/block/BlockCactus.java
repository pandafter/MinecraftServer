package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import java.util.Iterator;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.EnumDirection;
import net.minecraft.server.level.WorldServer;
import net.minecraft.tags.TagsBlock;
import net.minecraft.tags.TagsFluid;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.GeneratorAccess;
import net.minecraft.world.level.IBlockAccess;
import net.minecraft.world.level.IWorldReader;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.state.BlockBase;
import net.minecraft.world.level.block.state.BlockStateList;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.block.state.properties.BlockProperties;
import net.minecraft.world.level.block.state.properties.BlockStateInteger;
import net.minecraft.world.level.pathfinder.PathMode;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.VoxelShapeCollision;

public class BlockCactus extends Block {

    public static final MapCodec<BlockCactus> CODEC = simpleCodec(BlockCactus::new);
    public static final BlockStateInteger AGE = BlockProperties.AGE_15;
    public static final int MAX_AGE = 15;
    protected static final int AABB_OFFSET = 1;
    protected static final VoxelShape COLLISION_SHAPE = Block.box(1.0D, 0.0D, 1.0D, 15.0D, 15.0D, 15.0D);
    protected static final VoxelShape OUTLINE_SHAPE = Block.box(1.0D, 0.0D, 1.0D, 15.0D, 16.0D, 15.0D);

    @Override
    public MapCodec<BlockCactus> codec() {
        return BlockCactus.CODEC;
    }

    protected BlockCactus(BlockBase.Info blockbase_info) {
        super(blockbase_info);
        this.registerDefaultState((IBlockData) ((IBlockData) this.stateDefinition.any()).setValue(BlockCactus.AGE, 0));
    }

    @Override
    protected void tick(IBlockData iblockdata, WorldServer worldserver, BlockPosition blockposition, RandomSource randomsource) {
        if (!iblockdata.canSurvive(worldserver, blockposition)) {
            worldserver.destroyBlock(blockposition, true);
        }

    }

    @Override
    protected void randomTick(IBlockData iblockdata, WorldServer worldserver, BlockPosition blockposition, RandomSource randomsource) {
        BlockPosition blockposition1 = blockposition.above();

        if (worldserver.isEmptyBlock(blockposition1)) {
            int i;

            for (i = 1; worldserver.getBlockState(blockposition.below(i)).is((Block) this); ++i) {
                ;
            }

            if (i < 3) {
                int j = (Integer) iblockdata.getValue(BlockCactus.AGE);

                if (j == 15) {
                    worldserver.setBlockAndUpdate(blockposition1, this.defaultBlockState());
                    IBlockData iblockdata1 = (IBlockData) iblockdata.setValue(BlockCactus.AGE, 0);

                    worldserver.setBlock(blockposition, iblockdata1, 4);
                    worldserver.neighborChanged(iblockdata1, blockposition1, this, blockposition, false);
                } else {
                    worldserver.setBlock(blockposition, (IBlockData) iblockdata.setValue(BlockCactus.AGE, j + 1), 4);
                }

            }
        }
    }

    @Override
    protected VoxelShape getCollisionShape(IBlockData iblockdata, IBlockAccess iblockaccess, BlockPosition blockposition, VoxelShapeCollision voxelshapecollision) {
        return BlockCactus.COLLISION_SHAPE;
    }

    @Override
    protected VoxelShape getShape(IBlockData iblockdata, IBlockAccess iblockaccess, BlockPosition blockposition, VoxelShapeCollision voxelshapecollision) {
        return BlockCactus.OUTLINE_SHAPE;
    }

    @Override
    protected IBlockData updateShape(IBlockData iblockdata, EnumDirection enumdirection, IBlockData iblockdata1, GeneratorAccess generatoraccess, BlockPosition blockposition, BlockPosition blockposition1) {
        if (!iblockdata.canSurvive(generatoraccess, blockposition)) {
            generatoraccess.scheduleTick(blockposition, (Block) this, 1);
        }

        return super.updateShape(iblockdata, enumdirection, iblockdata1, generatoraccess, blockposition, blockposition1);
    }

    @Override
    protected boolean canSurvive(IBlockData iblockdata, IWorldReader iworldreader, BlockPosition blockposition) {
        Iterator iterator = EnumDirection.EnumDirectionLimit.HORIZONTAL.iterator();

        EnumDirection enumdirection;
        IBlockData iblockdata1;

        do {
            if (!iterator.hasNext()) {
                IBlockData iblockdata2 = iworldreader.getBlockState(blockposition.below());

                return (iblockdata2.is(Blocks.CACTUS) || iblockdata2.is(TagsBlock.SAND)) && !iworldreader.getBlockState(blockposition.above()).liquid();
            }

            enumdirection = (EnumDirection) iterator.next();
            iblockdata1 = iworldreader.getBlockState(blockposition.relative(enumdirection));
        } while (!iblockdata1.isSolid() && !iworldreader.getFluidState(blockposition.relative(enumdirection)).is(TagsFluid.LAVA));

        return false;
    }

    @Override
    protected void entityInside(IBlockData iblockdata, World world, BlockPosition blockposition, Entity entity) {
        entity.hurt(world.damageSources().cactus(), 1.0F);
    }

    @Override
    protected void createBlockStateDefinition(BlockStateList.a<Block, IBlockData> blockstatelist_a) {
        blockstatelist_a.add(BlockCactus.AGE);
    }

    @Override
    protected boolean isPathfindable(IBlockData iblockdata, PathMode pathmode) {
        return false;
    }
}
