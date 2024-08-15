package net.minecraft.world.level.block;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.EnumDirection;
import net.minecraft.tags.TagsBlock;
import net.minecraft.world.EnumHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.item.HangingSignItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockActionContext;
import net.minecraft.world.level.GeneratorAccess;
import net.minecraft.world.level.IBlockAccess;
import net.minecraft.world.level.IWorldReader;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.HangingSignBlockEntity;
import net.minecraft.world.level.block.entity.TileEntity;
import net.minecraft.world.level.block.entity.TileEntitySign;
import net.minecraft.world.level.block.entity.TileEntityTypes;
import net.minecraft.world.level.block.state.BlockBase;
import net.minecraft.world.level.block.state.BlockStateList;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.block.state.properties.BlockPropertyWood;
import net.minecraft.world.level.block.state.properties.BlockStateDirection;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidTypes;
import net.minecraft.world.level.pathfinder.PathMode;
import net.minecraft.world.phys.MovingObjectPositionBlock;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.VoxelShapeCollision;
import net.minecraft.world.phys.shapes.VoxelShapes;

public class WallHangingSignBlock extends BlockSign {

    public static final MapCodec<WallHangingSignBlock> CODEC = RecordCodecBuilder.mapCodec((instance) -> {
        return instance.group(BlockPropertyWood.CODEC.fieldOf("wood_type").forGetter(BlockSign::type), propertiesCodec()).apply(instance, WallHangingSignBlock::new);
    });
    public static final BlockStateDirection FACING = BlockFacingHorizontal.FACING;
    public static final VoxelShape PLANK_NORTHSOUTH = Block.box(0.0D, 14.0D, 6.0D, 16.0D, 16.0D, 10.0D);
    public static final VoxelShape PLANK_EASTWEST = Block.box(6.0D, 14.0D, 0.0D, 10.0D, 16.0D, 16.0D);
    public static final VoxelShape SHAPE_NORTHSOUTH = VoxelShapes.or(WallHangingSignBlock.PLANK_NORTHSOUTH, Block.box(1.0D, 0.0D, 7.0D, 15.0D, 10.0D, 9.0D));
    public static final VoxelShape SHAPE_EASTWEST = VoxelShapes.or(WallHangingSignBlock.PLANK_EASTWEST, Block.box(7.0D, 0.0D, 1.0D, 9.0D, 10.0D, 15.0D));
    private static final Map<EnumDirection, VoxelShape> AABBS = Maps.newEnumMap(ImmutableMap.of(EnumDirection.NORTH, WallHangingSignBlock.SHAPE_NORTHSOUTH, EnumDirection.SOUTH, WallHangingSignBlock.SHAPE_NORTHSOUTH, EnumDirection.EAST, WallHangingSignBlock.SHAPE_EASTWEST, EnumDirection.WEST, WallHangingSignBlock.SHAPE_EASTWEST));

    @Override
    public MapCodec<WallHangingSignBlock> codec() {
        return WallHangingSignBlock.CODEC;
    }

    public WallHangingSignBlock(BlockPropertyWood blockpropertywood, BlockBase.Info blockbase_info) {
        super(blockpropertywood, blockbase_info.sound(blockpropertywood.hangingSignSoundType()));
        this.registerDefaultState((IBlockData) ((IBlockData) ((IBlockData) this.stateDefinition.any()).setValue(WallHangingSignBlock.FACING, EnumDirection.NORTH)).setValue(WallHangingSignBlock.WATERLOGGED, false));
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack itemstack, IBlockData iblockdata, World world, BlockPosition blockposition, EntityHuman entityhuman, EnumHand enumhand, MovingObjectPositionBlock movingobjectpositionblock) {
        TileEntity tileentity = world.getBlockEntity(blockposition);

        if (tileentity instanceof TileEntitySign tileentitysign) {
            if (this.shouldTryToChainAnotherHangingSign(iblockdata, entityhuman, movingobjectpositionblock, tileentitysign, itemstack)) {
                return ItemInteractionResult.SKIP_DEFAULT_BLOCK_INTERACTION;
            }
        }

        return super.useItemOn(itemstack, iblockdata, world, blockposition, entityhuman, enumhand, movingobjectpositionblock);
    }

    private boolean shouldTryToChainAnotherHangingSign(IBlockData iblockdata, EntityHuman entityhuman, MovingObjectPositionBlock movingobjectpositionblock, TileEntitySign tileentitysign, ItemStack itemstack) {
        return !tileentitysign.canExecuteClickCommands(tileentitysign.isFacingFrontText(entityhuman), entityhuman) && itemstack.getItem() instanceof HangingSignItem && !this.isHittingEditableSide(movingobjectpositionblock, iblockdata);
    }

    private boolean isHittingEditableSide(MovingObjectPositionBlock movingobjectpositionblock, IBlockData iblockdata) {
        return movingobjectpositionblock.getDirection().getAxis() == ((EnumDirection) iblockdata.getValue(WallHangingSignBlock.FACING)).getAxis();
    }

    @Override
    public String getDescriptionId() {
        return this.asItem().getDescriptionId();
    }

    @Override
    protected VoxelShape getShape(IBlockData iblockdata, IBlockAccess iblockaccess, BlockPosition blockposition, VoxelShapeCollision voxelshapecollision) {
        return (VoxelShape) WallHangingSignBlock.AABBS.get(iblockdata.getValue(WallHangingSignBlock.FACING));
    }

    @Override
    protected VoxelShape getBlockSupportShape(IBlockData iblockdata, IBlockAccess iblockaccess, BlockPosition blockposition) {
        return this.getShape(iblockdata, iblockaccess, blockposition, VoxelShapeCollision.empty());
    }

    @Override
    protected VoxelShape getCollisionShape(IBlockData iblockdata, IBlockAccess iblockaccess, BlockPosition blockposition, VoxelShapeCollision voxelshapecollision) {
        switch ((EnumDirection) iblockdata.getValue(WallHangingSignBlock.FACING)) {
            case EAST:
            case WEST:
                return WallHangingSignBlock.PLANK_EASTWEST;
            default:
                return WallHangingSignBlock.PLANK_NORTHSOUTH;
        }
    }

    public boolean canPlace(IBlockData iblockdata, IWorldReader iworldreader, BlockPosition blockposition) {
        EnumDirection enumdirection = ((EnumDirection) iblockdata.getValue(WallHangingSignBlock.FACING)).getClockWise();
        EnumDirection enumdirection1 = ((EnumDirection) iblockdata.getValue(WallHangingSignBlock.FACING)).getCounterClockWise();

        return this.canAttachTo(iworldreader, iblockdata, blockposition.relative(enumdirection), enumdirection1) || this.canAttachTo(iworldreader, iblockdata, blockposition.relative(enumdirection1), enumdirection);
    }

    public boolean canAttachTo(IWorldReader iworldreader, IBlockData iblockdata, BlockPosition blockposition, EnumDirection enumdirection) {
        IBlockData iblockdata1 = iworldreader.getBlockState(blockposition);

        return iblockdata1.is(TagsBlock.WALL_HANGING_SIGNS) ? ((EnumDirection) iblockdata1.getValue(WallHangingSignBlock.FACING)).getAxis().test((EnumDirection) iblockdata.getValue(WallHangingSignBlock.FACING)) : iblockdata1.isFaceSturdy(iworldreader, blockposition, enumdirection, EnumBlockSupport.FULL);
    }

    @Nullable
    @Override
    public IBlockData getStateForPlacement(BlockActionContext blockactioncontext) {
        IBlockData iblockdata = this.defaultBlockState();
        Fluid fluid = blockactioncontext.getLevel().getFluidState(blockactioncontext.getClickedPos());
        World world = blockactioncontext.getLevel();
        BlockPosition blockposition = blockactioncontext.getClickedPos();
        EnumDirection[] aenumdirection = blockactioncontext.getNearestLookingDirections();
        int i = aenumdirection.length;

        for (int j = 0; j < i; ++j) {
            EnumDirection enumdirection = aenumdirection[j];

            if (enumdirection.getAxis().isHorizontal() && !enumdirection.getAxis().test(blockactioncontext.getClickedFace())) {
                EnumDirection enumdirection1 = enumdirection.getOpposite();

                iblockdata = (IBlockData) iblockdata.setValue(WallHangingSignBlock.FACING, enumdirection1);
                if (iblockdata.canSurvive(world, blockposition) && this.canPlace(iblockdata, world, blockposition)) {
                    return (IBlockData) iblockdata.setValue(WallHangingSignBlock.WATERLOGGED, fluid.getType() == FluidTypes.WATER);
                }
            }
        }

        return null;
    }

    @Override
    protected IBlockData updateShape(IBlockData iblockdata, EnumDirection enumdirection, IBlockData iblockdata1, GeneratorAccess generatoraccess, BlockPosition blockposition, BlockPosition blockposition1) {
        return enumdirection.getAxis() == ((EnumDirection) iblockdata.getValue(WallHangingSignBlock.FACING)).getClockWise().getAxis() && !iblockdata.canSurvive(generatoraccess, blockposition) ? Blocks.AIR.defaultBlockState() : super.updateShape(iblockdata, enumdirection, iblockdata1, generatoraccess, blockposition, blockposition1);
    }

    @Override
    public float getYRotationDegrees(IBlockData iblockdata) {
        return ((EnumDirection) iblockdata.getValue(WallHangingSignBlock.FACING)).toYRot();
    }

    @Override
    protected IBlockData rotate(IBlockData iblockdata, EnumBlockRotation enumblockrotation) {
        return (IBlockData) iblockdata.setValue(WallHangingSignBlock.FACING, enumblockrotation.rotate((EnumDirection) iblockdata.getValue(WallHangingSignBlock.FACING)));
    }

    @Override
    protected IBlockData mirror(IBlockData iblockdata, EnumBlockMirror enumblockmirror) {
        return iblockdata.rotate(enumblockmirror.getRotation((EnumDirection) iblockdata.getValue(WallHangingSignBlock.FACING)));
    }

    @Override
    protected void createBlockStateDefinition(BlockStateList.a<Block, IBlockData> blockstatelist_a) {
        blockstatelist_a.add(WallHangingSignBlock.FACING, WallHangingSignBlock.WATERLOGGED);
    }

    @Override
    public TileEntity newBlockEntity(BlockPosition blockposition, IBlockData iblockdata) {
        return new HangingSignBlockEntity(blockposition, iblockdata);
    }

    @Override
    protected boolean isPathfindable(IBlockData iblockdata, PathMode pathmode) {
        return false;
    }

    @Nullable
    @Override
    public <T extends TileEntity> BlockEntityTicker<T> getTicker(World world, IBlockData iblockdata, TileEntityTypes<T> tileentitytypes) {
        return createTickerHelper(tileentitytypes, TileEntityTypes.HANGING_SIGN, TileEntitySign::tick);
    }
}
