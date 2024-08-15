package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import java.util.OptionalInt;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.EnumDirection;
import net.minecraft.core.particles.Particles;
import net.minecraft.server.level.WorldServer;
import net.minecraft.tags.TagsBlock;
import net.minecraft.util.ParticleUtils;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockActionContext;
import net.minecraft.world.level.GeneratorAccess;
import net.minecraft.world.level.IBlockAccess;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.state.BlockBase;
import net.minecraft.world.level.block.state.BlockStateList;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.block.state.properties.BlockProperties;
import net.minecraft.world.level.block.state.properties.BlockStateBoolean;
import net.minecraft.world.level.block.state.properties.BlockStateInteger;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidType;
import net.minecraft.world.level.material.FluidTypes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.VoxelShapes;

public class BlockLeaves extends Block implements IBlockWaterlogged {

    public static final MapCodec<BlockLeaves> CODEC = simpleCodec(BlockLeaves::new);
    public static final int DECAY_DISTANCE = 7;
    public static final BlockStateInteger DISTANCE = BlockProperties.DISTANCE;
    public static final BlockStateBoolean PERSISTENT = BlockProperties.PERSISTENT;
    public static final BlockStateBoolean WATERLOGGED = BlockProperties.WATERLOGGED;
    private static final int TICK_DELAY = 1;

    @Override
    public MapCodec<? extends BlockLeaves> codec() {
        return BlockLeaves.CODEC;
    }

    public BlockLeaves(BlockBase.Info blockbase_info) {
        super(blockbase_info);
        this.registerDefaultState((IBlockData) ((IBlockData) ((IBlockData) ((IBlockData) this.stateDefinition.any()).setValue(BlockLeaves.DISTANCE, 7)).setValue(BlockLeaves.PERSISTENT, false)).setValue(BlockLeaves.WATERLOGGED, false));
    }

    @Override
    protected VoxelShape getBlockSupportShape(IBlockData iblockdata, IBlockAccess iblockaccess, BlockPosition blockposition) {
        return VoxelShapes.empty();
    }

    @Override
    protected boolean isRandomlyTicking(IBlockData iblockdata) {
        return (Integer) iblockdata.getValue(BlockLeaves.DISTANCE) == 7 && !(Boolean) iblockdata.getValue(BlockLeaves.PERSISTENT);
    }

    @Override
    protected void randomTick(IBlockData iblockdata, WorldServer worldserver, BlockPosition blockposition, RandomSource randomsource) {
        if (this.decaying(iblockdata)) {
            dropResources(iblockdata, worldserver, blockposition);
            worldserver.removeBlock(blockposition, false);
        }

    }

    protected boolean decaying(IBlockData iblockdata) {
        return !(Boolean) iblockdata.getValue(BlockLeaves.PERSISTENT) && (Integer) iblockdata.getValue(BlockLeaves.DISTANCE) == 7;
    }

    @Override
    protected void tick(IBlockData iblockdata, WorldServer worldserver, BlockPosition blockposition, RandomSource randomsource) {
        worldserver.setBlock(blockposition, updateDistance(iblockdata, worldserver, blockposition), 3);
    }

    @Override
    protected int getLightBlock(IBlockData iblockdata, IBlockAccess iblockaccess, BlockPosition blockposition) {
        return 1;
    }

    @Override
    protected IBlockData updateShape(IBlockData iblockdata, EnumDirection enumdirection, IBlockData iblockdata1, GeneratorAccess generatoraccess, BlockPosition blockposition, BlockPosition blockposition1) {
        if ((Boolean) iblockdata.getValue(BlockLeaves.WATERLOGGED)) {
            generatoraccess.scheduleTick(blockposition, (FluidType) FluidTypes.WATER, FluidTypes.WATER.getTickDelay(generatoraccess));
        }

        int i = getDistanceAt(iblockdata1) + 1;

        if (i != 1 || (Integer) iblockdata.getValue(BlockLeaves.DISTANCE) != i) {
            generatoraccess.scheduleTick(blockposition, (Block) this, 1);
        }

        return iblockdata;
    }

    private static IBlockData updateDistance(IBlockData iblockdata, GeneratorAccess generatoraccess, BlockPosition blockposition) {
        int i = 7;
        BlockPosition.MutableBlockPosition blockposition_mutableblockposition = new BlockPosition.MutableBlockPosition();
        EnumDirection[] aenumdirection = EnumDirection.values();
        int j = aenumdirection.length;

        for (int k = 0; k < j; ++k) {
            EnumDirection enumdirection = aenumdirection[k];

            blockposition_mutableblockposition.setWithOffset(blockposition, enumdirection);
            i = Math.min(i, getDistanceAt(generatoraccess.getBlockState(blockposition_mutableblockposition)) + 1);
            if (i == 1) {
                break;
            }
        }

        return (IBlockData) iblockdata.setValue(BlockLeaves.DISTANCE, i);
    }

    private static int getDistanceAt(IBlockData iblockdata) {
        return getOptionalDistanceAt(iblockdata).orElse(7);
    }

    public static OptionalInt getOptionalDistanceAt(IBlockData iblockdata) {
        return iblockdata.is(TagsBlock.LOGS) ? OptionalInt.of(0) : (iblockdata.hasProperty(BlockLeaves.DISTANCE) ? OptionalInt.of((Integer) iblockdata.getValue(BlockLeaves.DISTANCE)) : OptionalInt.empty());
    }

    @Override
    protected Fluid getFluidState(IBlockData iblockdata) {
        return (Boolean) iblockdata.getValue(BlockLeaves.WATERLOGGED) ? FluidTypes.WATER.getSource(false) : super.getFluidState(iblockdata);
    }

    @Override
    public void animateTick(IBlockData iblockdata, World world, BlockPosition blockposition, RandomSource randomsource) {
        if (world.isRainingAt(blockposition.above())) {
            if (randomsource.nextInt(15) == 1) {
                BlockPosition blockposition1 = blockposition.below();
                IBlockData iblockdata1 = world.getBlockState(blockposition1);

                if (!iblockdata1.canOcclude() || !iblockdata1.isFaceSturdy(world, blockposition1, EnumDirection.UP)) {
                    ParticleUtils.spawnParticleBelow(world, blockposition, randomsource, Particles.DRIPPING_WATER);
                }
            }
        }
    }

    @Override
    protected void createBlockStateDefinition(BlockStateList.a<Block, IBlockData> blockstatelist_a) {
        blockstatelist_a.add(BlockLeaves.DISTANCE, BlockLeaves.PERSISTENT, BlockLeaves.WATERLOGGED);
    }

    @Override
    public IBlockData getStateForPlacement(BlockActionContext blockactioncontext) {
        Fluid fluid = blockactioncontext.getLevel().getFluidState(blockactioncontext.getClickedPos());
        IBlockData iblockdata = (IBlockData) ((IBlockData) this.defaultBlockState().setValue(BlockLeaves.PERSISTENT, true)).setValue(BlockLeaves.WATERLOGGED, fluid.getType() == FluidTypes.WATER);

        return updateDistance(iblockdata, blockactioncontext.getLevel(), blockactioncontext.getClickedPos());
    }
}
