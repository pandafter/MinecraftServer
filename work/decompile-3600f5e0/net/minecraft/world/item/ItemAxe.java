package net.minecraft.world.item;

import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableMap.Builder;
import java.util.Map;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.advancements.CriterionTriggers;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.EnumDirection;
import net.minecraft.core.Holder;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.sounds.SoundCategory;
import net.minecraft.sounds.SoundEffects;
import net.minecraft.tags.TagsBlock;
import net.minecraft.world.EnumInteractionResult;
import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.item.context.ItemActionContext;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BlockRotatable;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.WeatheringCopper;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.gameevent.GameEvent;

public class ItemAxe extends ItemTool {

    protected static final Map<Block, Block> STRIPPABLES = (new Builder()).put(Blocks.OAK_WOOD, Blocks.STRIPPED_OAK_WOOD).put(Blocks.OAK_LOG, Blocks.STRIPPED_OAK_LOG).put(Blocks.DARK_OAK_WOOD, Blocks.STRIPPED_DARK_OAK_WOOD).put(Blocks.DARK_OAK_LOG, Blocks.STRIPPED_DARK_OAK_LOG).put(Blocks.ACACIA_WOOD, Blocks.STRIPPED_ACACIA_WOOD).put(Blocks.ACACIA_LOG, Blocks.STRIPPED_ACACIA_LOG).put(Blocks.CHERRY_WOOD, Blocks.STRIPPED_CHERRY_WOOD).put(Blocks.CHERRY_LOG, Blocks.STRIPPED_CHERRY_LOG).put(Blocks.BIRCH_WOOD, Blocks.STRIPPED_BIRCH_WOOD).put(Blocks.BIRCH_LOG, Blocks.STRIPPED_BIRCH_LOG).put(Blocks.JUNGLE_WOOD, Blocks.STRIPPED_JUNGLE_WOOD).put(Blocks.JUNGLE_LOG, Blocks.STRIPPED_JUNGLE_LOG).put(Blocks.SPRUCE_WOOD, Blocks.STRIPPED_SPRUCE_WOOD).put(Blocks.SPRUCE_LOG, Blocks.STRIPPED_SPRUCE_LOG).put(Blocks.WARPED_STEM, Blocks.STRIPPED_WARPED_STEM).put(Blocks.WARPED_HYPHAE, Blocks.STRIPPED_WARPED_HYPHAE).put(Blocks.CRIMSON_STEM, Blocks.STRIPPED_CRIMSON_STEM).put(Blocks.CRIMSON_HYPHAE, Blocks.STRIPPED_CRIMSON_HYPHAE).put(Blocks.MANGROVE_WOOD, Blocks.STRIPPED_MANGROVE_WOOD).put(Blocks.MANGROVE_LOG, Blocks.STRIPPED_MANGROVE_LOG).put(Blocks.BAMBOO_BLOCK, Blocks.STRIPPED_BAMBOO_BLOCK).build();

    public ItemAxe(ToolMaterial toolmaterial, Item.Info item_info) {
        super(toolmaterial, TagsBlock.MINEABLE_WITH_AXE, item_info);
    }

    @Override
    public EnumInteractionResult useOn(ItemActionContext itemactioncontext) {
        World world = itemactioncontext.getLevel();
        BlockPosition blockposition = itemactioncontext.getClickedPos();
        EntityHuman entityhuman = itemactioncontext.getPlayer();
        Optional<IBlockData> optional = this.evaluateNewBlockState(world, blockposition, entityhuman, world.getBlockState(blockposition));

        if (optional.isEmpty()) {
            return EnumInteractionResult.PASS;
        } else {
            ItemStack itemstack = itemactioncontext.getItemInHand();

            if (entityhuman instanceof EntityPlayer) {
                CriterionTriggers.ITEM_USED_ON_BLOCK.trigger((EntityPlayer) entityhuman, blockposition, itemstack);
            }

            world.setBlock(blockposition, (IBlockData) optional.get(), 11);
            world.gameEvent((Holder) GameEvent.BLOCK_CHANGE, blockposition, GameEvent.a.of(entityhuman, (IBlockData) optional.get()));
            if (entityhuman != null) {
                itemstack.hurtAndBreak(1, entityhuman, EntityLiving.getSlotForHand(itemactioncontext.getHand()));
            }

            return EnumInteractionResult.sidedSuccess(world.isClientSide);
        }
    }

    private Optional<IBlockData> evaluateNewBlockState(World world, BlockPosition blockposition, @Nullable EntityHuman entityhuman, IBlockData iblockdata) {
        Optional<IBlockData> optional = this.getStripped(iblockdata);

        if (optional.isPresent()) {
            world.playSound(entityhuman, blockposition, SoundEffects.AXE_STRIP, SoundCategory.BLOCKS, 1.0F, 1.0F);
            return optional;
        } else {
            Optional<IBlockData> optional1 = WeatheringCopper.getPrevious(iblockdata);

            if (optional1.isPresent()) {
                world.playSound(entityhuman, blockposition, SoundEffects.AXE_SCRAPE, SoundCategory.BLOCKS, 1.0F, 1.0F);
                world.levelEvent(entityhuman, 3005, blockposition, 0);
                return optional1;
            } else {
                Optional<IBlockData> optional2 = Optional.ofNullable((Block) ((BiMap) HoneycombItem.WAX_OFF_BY_BLOCK.get()).get(iblockdata.getBlock())).map((block) -> {
                    return block.withPropertiesOf(iblockdata);
                });

                if (optional2.isPresent()) {
                    world.playSound(entityhuman, blockposition, SoundEffects.AXE_WAX_OFF, SoundCategory.BLOCKS, 1.0F, 1.0F);
                    world.levelEvent(entityhuman, 3004, blockposition, 0);
                    return optional2;
                } else {
                    return Optional.empty();
                }
            }
        }
    }

    private Optional<IBlockData> getStripped(IBlockData iblockdata) {
        return Optional.ofNullable((Block) ItemAxe.STRIPPABLES.get(iblockdata.getBlock())).map((block) -> {
            return (IBlockData) block.defaultBlockState().setValue(BlockRotatable.AXIS, (EnumDirection.EnumAxis) iblockdata.getValue(BlockRotatable.AXIS));
        });
    }
}
