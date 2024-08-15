package net.minecraft.server.commands;

import com.google.common.collect.Lists;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandListenerWrapper;
import net.minecraft.commands.ICompletionProvider;
import net.minecraft.commands.arguments.ArgumentEntity;
import net.minecraft.commands.arguments.ArgumentInventorySlot;
import net.minecraft.commands.arguments.ResourceOrIdArgument;
import net.minecraft.commands.arguments.coordinates.ArgumentPosition;
import net.minecraft.commands.arguments.coordinates.ArgumentVec3;
import net.minecraft.commands.arguments.item.ArgumentItemStack;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.ReloadableServerRegistries;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.server.level.WorldServer;
import net.minecraft.world.IInventory;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.entity.EnumItemSlot;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.item.EntityItem;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.TileEntity;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParameterSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParameters;
import net.minecraft.world.phys.Vec3D;

public class CommandLoot {

    public static final SuggestionProvider<CommandListenerWrapper> SUGGEST_LOOT_TABLE = (commandcontext, suggestionsbuilder) -> {
        ReloadableServerRegistries.b reloadableserverregistries_b = ((CommandListenerWrapper) commandcontext.getSource()).getServer().reloadableRegistries();

        return ICompletionProvider.suggestResource((Iterable) reloadableserverregistries_b.getKeys(Registries.LOOT_TABLE), suggestionsbuilder);
    };
    private static final DynamicCommandExceptionType ERROR_NO_HELD_ITEMS = new DynamicCommandExceptionType((object) -> {
        return IChatBaseComponent.translatableEscape("commands.drop.no_held_items", object);
    });
    private static final DynamicCommandExceptionType ERROR_NO_LOOT_TABLE = new DynamicCommandExceptionType((object) -> {
        return IChatBaseComponent.translatableEscape("commands.drop.no_loot_table", object);
    });

    public CommandLoot() {}

    public static void register(CommandDispatcher<CommandListenerWrapper> commanddispatcher, CommandBuildContext commandbuildcontext) {
        commanddispatcher.register((LiteralArgumentBuilder) addTargets((LiteralArgumentBuilder) net.minecraft.commands.CommandDispatcher.literal("loot").requires((commandlistenerwrapper) -> {
            return commandlistenerwrapper.hasPermission(2);
        }), (argumentbuilder, commandloot_b) -> {
            return argumentbuilder.then(net.minecraft.commands.CommandDispatcher.literal("fish").then(net.minecraft.commands.CommandDispatcher.argument("loot_table", ResourceOrIdArgument.lootTable(commandbuildcontext)).suggests(CommandLoot.SUGGEST_LOOT_TABLE).then(((RequiredArgumentBuilder) ((RequiredArgumentBuilder) ((RequiredArgumentBuilder) net.minecraft.commands.CommandDispatcher.argument("pos", ArgumentPosition.blockPos()).executes((commandcontext) -> {
                return dropFishingLoot(commandcontext, ResourceOrIdArgument.getLootTable(commandcontext, "loot_table"), ArgumentPosition.getLoadedBlockPos(commandcontext, "pos"), ItemStack.EMPTY, commandloot_b);
            })).then(net.minecraft.commands.CommandDispatcher.argument("tool", ArgumentItemStack.item(commandbuildcontext)).executes((commandcontext) -> {
                return dropFishingLoot(commandcontext, ResourceOrIdArgument.getLootTable(commandcontext, "loot_table"), ArgumentPosition.getLoadedBlockPos(commandcontext, "pos"), ArgumentItemStack.getItem(commandcontext, "tool").createItemStack(1, false), commandloot_b);
            }))).then(net.minecraft.commands.CommandDispatcher.literal("mainhand").executes((commandcontext) -> {
                return dropFishingLoot(commandcontext, ResourceOrIdArgument.getLootTable(commandcontext, "loot_table"), ArgumentPosition.getLoadedBlockPos(commandcontext, "pos"), getSourceHandItem((CommandListenerWrapper) commandcontext.getSource(), EnumItemSlot.MAINHAND), commandloot_b);
            }))).then(net.minecraft.commands.CommandDispatcher.literal("offhand").executes((commandcontext) -> {
                return dropFishingLoot(commandcontext, ResourceOrIdArgument.getLootTable(commandcontext, "loot_table"), ArgumentPosition.getLoadedBlockPos(commandcontext, "pos"), getSourceHandItem((CommandListenerWrapper) commandcontext.getSource(), EnumItemSlot.OFFHAND), commandloot_b);
            }))))).then(net.minecraft.commands.CommandDispatcher.literal("loot").then(net.minecraft.commands.CommandDispatcher.argument("loot_table", ResourceOrIdArgument.lootTable(commandbuildcontext)).suggests(CommandLoot.SUGGEST_LOOT_TABLE).executes((commandcontext) -> {
                return dropChestLoot(commandcontext, ResourceOrIdArgument.getLootTable(commandcontext, "loot_table"), commandloot_b);
            }))).then(net.minecraft.commands.CommandDispatcher.literal("kill").then(net.minecraft.commands.CommandDispatcher.argument("target", ArgumentEntity.entity()).executes((commandcontext) -> {
                return dropKillLoot(commandcontext, ArgumentEntity.getEntity(commandcontext, "target"), commandloot_b);
            }))).then(net.minecraft.commands.CommandDispatcher.literal("mine").then(((RequiredArgumentBuilder) ((RequiredArgumentBuilder) ((RequiredArgumentBuilder) net.minecraft.commands.CommandDispatcher.argument("pos", ArgumentPosition.blockPos()).executes((commandcontext) -> {
                return dropBlockLoot(commandcontext, ArgumentPosition.getLoadedBlockPos(commandcontext, "pos"), ItemStack.EMPTY, commandloot_b);
            })).then(net.minecraft.commands.CommandDispatcher.argument("tool", ArgumentItemStack.item(commandbuildcontext)).executes((commandcontext) -> {
                return dropBlockLoot(commandcontext, ArgumentPosition.getLoadedBlockPos(commandcontext, "pos"), ArgumentItemStack.getItem(commandcontext, "tool").createItemStack(1, false), commandloot_b);
            }))).then(net.minecraft.commands.CommandDispatcher.literal("mainhand").executes((commandcontext) -> {
                return dropBlockLoot(commandcontext, ArgumentPosition.getLoadedBlockPos(commandcontext, "pos"), getSourceHandItem((CommandListenerWrapper) commandcontext.getSource(), EnumItemSlot.MAINHAND), commandloot_b);
            }))).then(net.minecraft.commands.CommandDispatcher.literal("offhand").executes((commandcontext) -> {
                return dropBlockLoot(commandcontext, ArgumentPosition.getLoadedBlockPos(commandcontext, "pos"), getSourceHandItem((CommandListenerWrapper) commandcontext.getSource(), EnumItemSlot.OFFHAND), commandloot_b);
            }))));
        }));
    }

    private static <T extends ArgumentBuilder<CommandListenerWrapper, T>> T addTargets(T t0, CommandLoot.c commandloot_c) {
        return t0.then(((LiteralArgumentBuilder) net.minecraft.commands.CommandDispatcher.literal("replace").then(net.minecraft.commands.CommandDispatcher.literal("entity").then(net.minecraft.commands.CommandDispatcher.argument("entities", ArgumentEntity.entities()).then(commandloot_c.construct(net.minecraft.commands.CommandDispatcher.argument("slot", ArgumentInventorySlot.slot()), (commandcontext, list, commandloot_a) -> {
            return entityReplace(ArgumentEntity.getEntities(commandcontext, "entities"), ArgumentInventorySlot.getSlot(commandcontext, "slot"), list.size(), list, commandloot_a);
        }).then(commandloot_c.construct(net.minecraft.commands.CommandDispatcher.argument("count", IntegerArgumentType.integer(0)), (commandcontext, list, commandloot_a) -> {
            return entityReplace(ArgumentEntity.getEntities(commandcontext, "entities"), ArgumentInventorySlot.getSlot(commandcontext, "slot"), IntegerArgumentType.getInteger(commandcontext, "count"), list, commandloot_a);
        })))))).then(net.minecraft.commands.CommandDispatcher.literal("block").then(net.minecraft.commands.CommandDispatcher.argument("targetPos", ArgumentPosition.blockPos()).then(commandloot_c.construct(net.minecraft.commands.CommandDispatcher.argument("slot", ArgumentInventorySlot.slot()), (commandcontext, list, commandloot_a) -> {
            return blockReplace((CommandListenerWrapper) commandcontext.getSource(), ArgumentPosition.getLoadedBlockPos(commandcontext, "targetPos"), ArgumentInventorySlot.getSlot(commandcontext, "slot"), list.size(), list, commandloot_a);
        }).then(commandloot_c.construct(net.minecraft.commands.CommandDispatcher.argument("count", IntegerArgumentType.integer(0)), (commandcontext, list, commandloot_a) -> {
            return blockReplace((CommandListenerWrapper) commandcontext.getSource(), ArgumentPosition.getLoadedBlockPos(commandcontext, "targetPos"), IntegerArgumentType.getInteger(commandcontext, "slot"), IntegerArgumentType.getInteger(commandcontext, "count"), list, commandloot_a);
        })))))).then(net.minecraft.commands.CommandDispatcher.literal("insert").then(commandloot_c.construct(net.minecraft.commands.CommandDispatcher.argument("targetPos", ArgumentPosition.blockPos()), (commandcontext, list, commandloot_a) -> {
            return blockDistribute((CommandListenerWrapper) commandcontext.getSource(), ArgumentPosition.getLoadedBlockPos(commandcontext, "targetPos"), list, commandloot_a);
        }))).then(net.minecraft.commands.CommandDispatcher.literal("give").then(commandloot_c.construct(net.minecraft.commands.CommandDispatcher.argument("players", ArgumentEntity.players()), (commandcontext, list, commandloot_a) -> {
            return playerGive(ArgumentEntity.getPlayers(commandcontext, "players"), list, commandloot_a);
        }))).then(net.minecraft.commands.CommandDispatcher.literal("spawn").then(commandloot_c.construct(net.minecraft.commands.CommandDispatcher.argument("targetPos", ArgumentVec3.vec3()), (commandcontext, list, commandloot_a) -> {
            return dropInWorld((CommandListenerWrapper) commandcontext.getSource(), ArgumentVec3.getVec3(commandcontext, "targetPos"), list, commandloot_a);
        })));
    }

    private static IInventory getContainer(CommandListenerWrapper commandlistenerwrapper, BlockPosition blockposition) throws CommandSyntaxException {
        TileEntity tileentity = commandlistenerwrapper.getLevel().getBlockEntity(blockposition);

        if (!(tileentity instanceof IInventory)) {
            throw ItemCommands.ERROR_TARGET_NOT_A_CONTAINER.create(blockposition.getX(), blockposition.getY(), blockposition.getZ());
        } else {
            return (IInventory) tileentity;
        }
    }

    private static int blockDistribute(CommandListenerWrapper commandlistenerwrapper, BlockPosition blockposition, List<ItemStack> list, CommandLoot.a commandloot_a) throws CommandSyntaxException {
        IInventory iinventory = getContainer(commandlistenerwrapper, blockposition);
        List<ItemStack> list1 = Lists.newArrayListWithCapacity(list.size());
        Iterator iterator = list.iterator();

        while (iterator.hasNext()) {
            ItemStack itemstack = (ItemStack) iterator.next();

            if (distributeToContainer(iinventory, itemstack.copy())) {
                iinventory.setChanged();
                list1.add(itemstack);
            }
        }

        commandloot_a.accept(list1);
        return list1.size();
    }

    private static boolean distributeToContainer(IInventory iinventory, ItemStack itemstack) {
        boolean flag = false;

        for (int i = 0; i < iinventory.getContainerSize() && !itemstack.isEmpty(); ++i) {
            ItemStack itemstack1 = iinventory.getItem(i);

            if (iinventory.canPlaceItem(i, itemstack)) {
                if (itemstack1.isEmpty()) {
                    iinventory.setItem(i, itemstack);
                    flag = true;
                    break;
                }

                if (canMergeItems(itemstack1, itemstack)) {
                    int j = itemstack.getMaxStackSize() - itemstack1.getCount();
                    int k = Math.min(itemstack.getCount(), j);

                    itemstack.shrink(k);
                    itemstack1.grow(k);
                    flag = true;
                }
            }
        }

        return flag;
    }

    private static int blockReplace(CommandListenerWrapper commandlistenerwrapper, BlockPosition blockposition, int i, int j, List<ItemStack> list, CommandLoot.a commandloot_a) throws CommandSyntaxException {
        IInventory iinventory = getContainer(commandlistenerwrapper, blockposition);
        int k = iinventory.getContainerSize();

        if (i >= 0 && i < k) {
            List<ItemStack> list1 = Lists.newArrayListWithCapacity(list.size());

            for (int l = 0; l < j; ++l) {
                int i1 = i + l;
                ItemStack itemstack = l < list.size() ? (ItemStack) list.get(l) : ItemStack.EMPTY;

                if (iinventory.canPlaceItem(i1, itemstack)) {
                    iinventory.setItem(i1, itemstack);
                    list1.add(itemstack);
                }
            }

            commandloot_a.accept(list1);
            return list1.size();
        } else {
            throw ItemCommands.ERROR_TARGET_INAPPLICABLE_SLOT.create(i);
        }
    }

    private static boolean canMergeItems(ItemStack itemstack, ItemStack itemstack1) {
        return itemstack.getCount() <= itemstack.getMaxStackSize() && ItemStack.isSameItemSameComponents(itemstack, itemstack1);
    }

    private static int playerGive(Collection<EntityPlayer> collection, List<ItemStack> list, CommandLoot.a commandloot_a) throws CommandSyntaxException {
        List<ItemStack> list1 = Lists.newArrayListWithCapacity(list.size());
        Iterator iterator = list.iterator();

        while (iterator.hasNext()) {
            ItemStack itemstack = (ItemStack) iterator.next();
            Iterator iterator1 = collection.iterator();

            while (iterator1.hasNext()) {
                EntityPlayer entityplayer = (EntityPlayer) iterator1.next();

                if (entityplayer.getInventory().add(itemstack.copy())) {
                    list1.add(itemstack);
                }
            }
        }

        commandloot_a.accept(list1);
        return list1.size();
    }

    private static void setSlots(Entity entity, List<ItemStack> list, int i, int j, List<ItemStack> list1) {
        for (int k = 0; k < j; ++k) {
            ItemStack itemstack = k < list.size() ? (ItemStack) list.get(k) : ItemStack.EMPTY;
            SlotAccess slotaccess = entity.getSlot(i + k);

            if (slotaccess != SlotAccess.NULL && slotaccess.set(itemstack.copy())) {
                list1.add(itemstack);
            }
        }

    }

    private static int entityReplace(Collection<? extends Entity> collection, int i, int j, List<ItemStack> list, CommandLoot.a commandloot_a) throws CommandSyntaxException {
        List<ItemStack> list1 = Lists.newArrayListWithCapacity(list.size());
        Iterator iterator = collection.iterator();

        while (iterator.hasNext()) {
            Entity entity = (Entity) iterator.next();

            if (entity instanceof EntityPlayer entityplayer) {
                setSlots(entity, list, i, j, list1);
                entityplayer.containerMenu.broadcastChanges();
            } else {
                setSlots(entity, list, i, j, list1);
            }
        }

        commandloot_a.accept(list1);
        return list1.size();
    }

    private static int dropInWorld(CommandListenerWrapper commandlistenerwrapper, Vec3D vec3d, List<ItemStack> list, CommandLoot.a commandloot_a) throws CommandSyntaxException {
        WorldServer worldserver = commandlistenerwrapper.getLevel();

        list.forEach((itemstack) -> {
            EntityItem entityitem = new EntityItem(worldserver, vec3d.x, vec3d.y, vec3d.z, itemstack.copy());

            entityitem.setDefaultPickUpDelay();
            worldserver.addFreshEntity(entityitem);
        });
        commandloot_a.accept(list);
        return list.size();
    }

    private static void callback(CommandListenerWrapper commandlistenerwrapper, List<ItemStack> list) {
        if (list.size() == 1) {
            ItemStack itemstack = (ItemStack) list.get(0);

            commandlistenerwrapper.sendSuccess(() -> {
                return IChatBaseComponent.translatable("commands.drop.success.single", itemstack.getCount(), itemstack.getDisplayName());
            }, false);
        } else {
            commandlistenerwrapper.sendSuccess(() -> {
                return IChatBaseComponent.translatable("commands.drop.success.multiple", list.size());
            }, false);
        }

    }

    private static void callback(CommandListenerWrapper commandlistenerwrapper, List<ItemStack> list, ResourceKey<LootTable> resourcekey) {
        if (list.size() == 1) {
            ItemStack itemstack = (ItemStack) list.get(0);

            commandlistenerwrapper.sendSuccess(() -> {
                return IChatBaseComponent.translatable("commands.drop.success.single_with_table", itemstack.getCount(), itemstack.getDisplayName(), IChatBaseComponent.translationArg(resourcekey.location()));
            }, false);
        } else {
            commandlistenerwrapper.sendSuccess(() -> {
                return IChatBaseComponent.translatable("commands.drop.success.multiple_with_table", list.size(), IChatBaseComponent.translationArg(resourcekey.location()));
            }, false);
        }

    }

    private static ItemStack getSourceHandItem(CommandListenerWrapper commandlistenerwrapper, EnumItemSlot enumitemslot) throws CommandSyntaxException {
        Entity entity = commandlistenerwrapper.getEntityOrException();

        if (entity instanceof EntityLiving) {
            return ((EntityLiving) entity).getItemBySlot(enumitemslot);
        } else {
            throw CommandLoot.ERROR_NO_HELD_ITEMS.create(entity.getDisplayName());
        }
    }

    private static int dropBlockLoot(CommandContext<CommandListenerWrapper> commandcontext, BlockPosition blockposition, ItemStack itemstack, CommandLoot.b commandloot_b) throws CommandSyntaxException {
        CommandListenerWrapper commandlistenerwrapper = (CommandListenerWrapper) commandcontext.getSource();
        WorldServer worldserver = commandlistenerwrapper.getLevel();
        IBlockData iblockdata = worldserver.getBlockState(blockposition);
        TileEntity tileentity = worldserver.getBlockEntity(blockposition);
        LootParams.a lootparams_a = (new LootParams.a(worldserver)).withParameter(LootContextParameters.ORIGIN, Vec3D.atCenterOf(blockposition)).withParameter(LootContextParameters.BLOCK_STATE, iblockdata).withOptionalParameter(LootContextParameters.BLOCK_ENTITY, tileentity).withOptionalParameter(LootContextParameters.THIS_ENTITY, commandlistenerwrapper.getEntity()).withParameter(LootContextParameters.TOOL, itemstack);
        List<ItemStack> list = iblockdata.getDrops(lootparams_a);

        return commandloot_b.accept(commandcontext, list, (list1) -> {
            callback(commandlistenerwrapper, list1, iblockdata.getBlock().getLootTable());
        });
    }

    private static int dropKillLoot(CommandContext<CommandListenerWrapper> commandcontext, Entity entity, CommandLoot.b commandloot_b) throws CommandSyntaxException {
        if (!(entity instanceof EntityLiving)) {
            throw CommandLoot.ERROR_NO_LOOT_TABLE.create(entity.getDisplayName());
        } else {
            ResourceKey<LootTable> resourcekey = ((EntityLiving) entity).getLootTable();
            CommandListenerWrapper commandlistenerwrapper = (CommandListenerWrapper) commandcontext.getSource();
            LootParams.a lootparams_a = new LootParams.a(commandlistenerwrapper.getLevel());
            Entity entity1 = commandlistenerwrapper.getEntity();

            if (entity1 instanceof EntityHuman) {
                EntityHuman entityhuman = (EntityHuman) entity1;

                lootparams_a.withParameter(LootContextParameters.LAST_DAMAGE_PLAYER, entityhuman);
            }

            lootparams_a.withParameter(LootContextParameters.DAMAGE_SOURCE, entity.damageSources().magic());
            lootparams_a.withOptionalParameter(LootContextParameters.DIRECT_KILLER_ENTITY, entity1);
            lootparams_a.withOptionalParameter(LootContextParameters.KILLER_ENTITY, entity1);
            lootparams_a.withParameter(LootContextParameters.THIS_ENTITY, entity);
            lootparams_a.withParameter(LootContextParameters.ORIGIN, commandlistenerwrapper.getPosition());
            LootParams lootparams = lootparams_a.create(LootContextParameterSets.ENTITY);
            LootTable loottable = commandlistenerwrapper.getServer().reloadableRegistries().getLootTable(resourcekey);
            List<ItemStack> list = loottable.getRandomItems(lootparams);

            return commandloot_b.accept(commandcontext, list, (list1) -> {
                callback(commandlistenerwrapper, list1, resourcekey);
            });
        }
    }

    private static int dropChestLoot(CommandContext<CommandListenerWrapper> commandcontext, Holder<LootTable> holder, CommandLoot.b commandloot_b) throws CommandSyntaxException {
        CommandListenerWrapper commandlistenerwrapper = (CommandListenerWrapper) commandcontext.getSource();
        LootParams lootparams = (new LootParams.a(commandlistenerwrapper.getLevel())).withOptionalParameter(LootContextParameters.THIS_ENTITY, commandlistenerwrapper.getEntity()).withParameter(LootContextParameters.ORIGIN, commandlistenerwrapper.getPosition()).create(LootContextParameterSets.CHEST);

        return drop(commandcontext, holder, lootparams, commandloot_b);
    }

    private static int dropFishingLoot(CommandContext<CommandListenerWrapper> commandcontext, Holder<LootTable> holder, BlockPosition blockposition, ItemStack itemstack, CommandLoot.b commandloot_b) throws CommandSyntaxException {
        CommandListenerWrapper commandlistenerwrapper = (CommandListenerWrapper) commandcontext.getSource();
        LootParams lootparams = (new LootParams.a(commandlistenerwrapper.getLevel())).withParameter(LootContextParameters.ORIGIN, Vec3D.atCenterOf(blockposition)).withParameter(LootContextParameters.TOOL, itemstack).withOptionalParameter(LootContextParameters.THIS_ENTITY, commandlistenerwrapper.getEntity()).create(LootContextParameterSets.FISHING);

        return drop(commandcontext, holder, lootparams, commandloot_b);
    }

    private static int drop(CommandContext<CommandListenerWrapper> commandcontext, Holder<LootTable> holder, LootParams lootparams, CommandLoot.b commandloot_b) throws CommandSyntaxException {
        CommandListenerWrapper commandlistenerwrapper = (CommandListenerWrapper) commandcontext.getSource();
        List<ItemStack> list = ((LootTable) holder.value()).getRandomItems(lootparams);

        return commandloot_b.accept(commandcontext, list, (list1) -> {
            callback(commandlistenerwrapper, list1);
        });
    }

    @FunctionalInterface
    private interface c {

        ArgumentBuilder<CommandListenerWrapper, ?> construct(ArgumentBuilder<CommandListenerWrapper, ?> argumentbuilder, CommandLoot.b commandloot_b);
    }

    @FunctionalInterface
    private interface b {

        int accept(CommandContext<CommandListenerWrapper> commandcontext, List<ItemStack> list, CommandLoot.a commandloot_a) throws CommandSyntaxException;
    }

    @FunctionalInterface
    private interface a {

        void accept(List<ItemStack> list) throws CommandSyntaxException;
    }
}
