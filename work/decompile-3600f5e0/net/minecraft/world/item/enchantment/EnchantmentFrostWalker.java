package net.minecraft.world.item.enchantment;

import java.util.Iterator;
import net.minecraft.core.BlockPosition;
import net.minecraft.util.MathHelper;
import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.BlockIceFrost;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.phys.shapes.VoxelShapeCollision;

public class EnchantmentFrostWalker extends Enchantment {

    public EnchantmentFrostWalker(Enchantment.b enchantment_b) {
        super(enchantment_b);
    }

    @Override
    public boolean isTreasureOnly() {
        return true;
    }

    public static void onEntityMoved(EntityLiving entityliving, World world, BlockPosition blockposition, int i) {
        if (entityliving.onGround()) {
            IBlockData iblockdata = Blocks.FROSTED_ICE.defaultBlockState();
            int j = Math.min(16, 2 + i);
            BlockPosition.MutableBlockPosition blockposition_mutableblockposition = new BlockPosition.MutableBlockPosition();
            Iterator iterator = BlockPosition.betweenClosed(blockposition.offset(-j, -1, -j), blockposition.offset(j, -1, j)).iterator();

            while (iterator.hasNext()) {
                BlockPosition blockposition1 = (BlockPosition) iterator.next();

                if (blockposition1.closerToCenterThan(entityliving.position(), (double) j)) {
                    blockposition_mutableblockposition.set(blockposition1.getX(), blockposition1.getY() + 1, blockposition1.getZ());
                    IBlockData iblockdata1 = world.getBlockState(blockposition_mutableblockposition);

                    if (iblockdata1.isAir()) {
                        IBlockData iblockdata2 = world.getBlockState(blockposition1);

                        if (iblockdata2 == BlockIceFrost.meltsInto() && iblockdata.canSurvive(world, blockposition1) && world.isUnobstructed(iblockdata, blockposition1, VoxelShapeCollision.empty())) {
                            world.setBlockAndUpdate(blockposition1, iblockdata);
                            world.scheduleTick(blockposition1, Blocks.FROSTED_ICE, MathHelper.nextInt(entityliving.getRandom(), 60, 120));
                        }
                    }
                }
            }

        }
    }

    @Override
    public boolean checkCompatibility(Enchantment enchantment) {
        return super.checkCompatibility(enchantment) && enchantment != Enchantments.DEPTH_STRIDER;
    }
}
