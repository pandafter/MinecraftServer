package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.EnumDirection;
import net.minecraft.core.particles.ParticleParamRedstone;
import net.minecraft.util.RandomSource;
import net.minecraft.world.EnumInteractionResult;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.item.context.BlockActionContext;
import net.minecraft.world.level.GeneratorAccess;
import net.minecraft.world.level.IWorldReader;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.state.BlockBase;
import net.minecraft.world.level.block.state.BlockStateList;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.block.state.properties.BlockProperties;
import net.minecraft.world.level.block.state.properties.BlockStateBoolean;
import net.minecraft.world.level.block.state.properties.BlockStateInteger;
import net.minecraft.world.phys.MovingObjectPositionBlock;

public class BlockRepeater extends BlockDiodeAbstract {

    public static final MapCodec<BlockRepeater> CODEC = simpleCodec(BlockRepeater::new);
    public static final BlockStateBoolean LOCKED = BlockProperties.LOCKED;
    public static final BlockStateInteger DELAY = BlockProperties.DELAY;

    @Override
    public MapCodec<BlockRepeater> codec() {
        return BlockRepeater.CODEC;
    }

    protected BlockRepeater(BlockBase.Info blockbase_info) {
        super(blockbase_info);
        this.registerDefaultState((IBlockData) ((IBlockData) ((IBlockData) ((IBlockData) ((IBlockData) this.stateDefinition.any()).setValue(BlockRepeater.FACING, EnumDirection.NORTH)).setValue(BlockRepeater.DELAY, 1)).setValue(BlockRepeater.LOCKED, false)).setValue(BlockRepeater.POWERED, false));
    }

    @Override
    protected EnumInteractionResult useWithoutItem(IBlockData iblockdata, World world, BlockPosition blockposition, EntityHuman entityhuman, MovingObjectPositionBlock movingobjectpositionblock) {
        if (!entityhuman.getAbilities().mayBuild) {
            return EnumInteractionResult.PASS;
        } else {
            world.setBlock(blockposition, (IBlockData) iblockdata.cycle(BlockRepeater.DELAY), 3);
            return EnumInteractionResult.sidedSuccess(world.isClientSide);
        }
    }

    @Override
    protected int getDelay(IBlockData iblockdata) {
        return (Integer) iblockdata.getValue(BlockRepeater.DELAY) * 2;
    }

    @Override
    public IBlockData getStateForPlacement(BlockActionContext blockactioncontext) {
        IBlockData iblockdata = super.getStateForPlacement(blockactioncontext);

        return (IBlockData) iblockdata.setValue(BlockRepeater.LOCKED, this.isLocked(blockactioncontext.getLevel(), blockactioncontext.getClickedPos(), iblockdata));
    }

    @Override
    protected IBlockData updateShape(IBlockData iblockdata, EnumDirection enumdirection, IBlockData iblockdata1, GeneratorAccess generatoraccess, BlockPosition blockposition, BlockPosition blockposition1) {
        return enumdirection == EnumDirection.DOWN && !this.canSurviveOn(generatoraccess, blockposition1, iblockdata1) ? Blocks.AIR.defaultBlockState() : (!generatoraccess.isClientSide() && enumdirection.getAxis() != ((EnumDirection) iblockdata.getValue(BlockRepeater.FACING)).getAxis() ? (IBlockData) iblockdata.setValue(BlockRepeater.LOCKED, this.isLocked(generatoraccess, blockposition, iblockdata)) : super.updateShape(iblockdata, enumdirection, iblockdata1, generatoraccess, blockposition, blockposition1));
    }

    @Override
    public boolean isLocked(IWorldReader iworldreader, BlockPosition blockposition, IBlockData iblockdata) {
        return this.getAlternateSignal(iworldreader, blockposition, iblockdata) > 0;
    }

    @Override
    protected boolean sideInputDiodesOnly() {
        return true;
    }

    @Override
    public void animateTick(IBlockData iblockdata, World world, BlockPosition blockposition, RandomSource randomsource) {
        if ((Boolean) iblockdata.getValue(BlockRepeater.POWERED)) {
            EnumDirection enumdirection = (EnumDirection) iblockdata.getValue(BlockRepeater.FACING);
            double d0 = (double) blockposition.getX() + 0.5D + (randomsource.nextDouble() - 0.5D) * 0.2D;
            double d1 = (double) blockposition.getY() + 0.4D + (randomsource.nextDouble() - 0.5D) * 0.2D;
            double d2 = (double) blockposition.getZ() + 0.5D + (randomsource.nextDouble() - 0.5D) * 0.2D;
            float f = -5.0F;

            if (randomsource.nextBoolean()) {
                f = (float) ((Integer) iblockdata.getValue(BlockRepeater.DELAY) * 2 - 1);
            }

            f /= 16.0F;
            double d3 = (double) (f * (float) enumdirection.getStepX());
            double d4 = (double) (f * (float) enumdirection.getStepZ());

            world.addParticle(ParticleParamRedstone.REDSTONE, d0 + d3, d1, d2 + d4, 0.0D, 0.0D, 0.0D);
        }
    }

    @Override
    protected void createBlockStateDefinition(BlockStateList.a<Block, IBlockData> blockstatelist_a) {
        blockstatelist_a.add(BlockRepeater.FACING, BlockRepeater.DELAY, BlockRepeater.LOCKED, BlockRepeater.POWERED);
    }
}
