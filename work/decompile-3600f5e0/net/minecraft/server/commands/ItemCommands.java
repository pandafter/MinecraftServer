package net.minecraft.server.commands;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.exceptions.Dynamic3CommandExceptionType;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandListenerWrapper;
import net.minecraft.commands.ICompletionProvider;
import net.minecraft.commands.arguments.ArgumentEntity;
import net.minecraft.commands.arguments.ArgumentInventorySlot;
import net.minecraft.commands.arguments.ResourceOrIdArgument;
import net.minecraft.commands.arguments.coordinates.ArgumentPosition;
import net.minecraft.commands.arguments.item.ArgumentItemStack;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.server.ReloadableServerRegistries;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.server.level.WorldServer;
import net.minecraft.world.IInventory;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.TileEntity;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTableInfo;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.parameters.LootContextParameterSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParameters;

public class ItemCommands {

    static final Dynamic3CommandExceptionType ERROR_TARGET_NOT_A_CONTAINER = new Dynamic3CommandExceptionType((object, object1, object2) -> {
        return IChatBaseComponent.translatableEscape("commands.item.target.not_a_container", object, object1, object2);
    });
    static final Dynamic3CommandExceptionType ERROR_SOURCE_NOT_A_CONTAINER = new Dynamic3CommandExceptionType((object, object1, object2) -> {
        return IChatBaseComponent.translatableEscape("commands.item.source.not_a_container", object, object1, object2);
    });
    static final DynamicCommandExceptionType ERROR_TARGET_INAPPLICABLE_SLOT = new DynamicCommandExceptionType((object) -> {
        return IChatBaseComponent.translatableEscape("commands.item.target.no_such_slot", object);
    });
    private static final DynamicCommandExceptionType ERROR_SOURCE_INAPPLICABLE_SLOT = new DynamicCommandExceptionType((object) -> {
        return IChatBaseComponent.translatableEscape("commands.item.source.no_such_slot", object);
    });
    private static final DynamicCommandExceptionType ERROR_TARGET_NO_CHANGES = new DynamicCommandExceptionType((object) -> {
        return IChatBaseComponent.translatableEscape("commands.item.target.no_changes", object);
    });
    private static final Dynamic2CommandExceptionType ERROR_TARGET_NO_CHANGES_KNOWN_ITEM = new Dynamic2CommandExceptionType((object, object1) -> {
        return IChatBaseComponent.translatableEscape("commands.item.target.no_changed.known_item", object, object1);
    });
    private static final SuggestionProvider<CommandListenerWrapper> SUGGEST_MODIFIER = (commandcontext, suggestionsbuilder) -> {
        ReloadableServerRegistries.b reloadableserverregistries_b = ((CommandListenerWrapper) commandcontext.getSource()).getServer().reloadableRegistries();

        return ICompletionProvider.suggestResource((Iterable) reloadableserverregistries_b.getKeys(Registries.ITEM_MODIFIER), suggestionsbuilder);
    };

    public ItemCommands() {}

    public static void register(CommandDispatcher<CommandListenerWrapper> commanddispatcher, CommandBuildContext commandbuildcontext) {
        commanddispatcher.register((LiteralArgumentBuilder) ((LiteralArgumentBuilder) ((LiteralArgumentBuilder) net.minecraft.commands.CommandDispatcher.literal("item").requires((commandlistenerwrapper) -> {
            return commandlistenerwrapper.hasPermission(2);
        })).then(((LiteralArgumentBuilder) net.minecraft.commands.CommandDispatcher.literal("replace").then(net.minecraft.commands.CommandDispatcher.literal("block").then(net.minecraft.commands.CommandDispatcher.argument("pos", ArgumentPosition.blockPos()).then(((RequiredArgumentBuilder) net.minecraft.commands.CommandDispatcher.argument("slot", ArgumentInventorySlot.slot()).then(net.minecraft.commands.CommandDispatcher.literal("with").then(((RequiredArgumentBuilder) net.minecraft.commands.CommandDispatcher.argument("item", ArgumentItemStack.item(commandbuildcontext)).executes((commandcontext) -> {
            return setBlockItem((CommandListenerWrapper) commandcontext.getSource(), ArgumentPosition.getLoadedBlockPos(commandcontext, "pos"), ArgumentInventorySlot.getSlot(commandcontext, "slot"), ArgumentItemStack.getItem(commandcontext, "item").createItemStack(1, false));
        })).then(net.minecraft.commands.CommandDispatcher.argument("count", IntegerArgumentType.integer(1, 99)).executes((commandcontext) -> {
            return setBlockItem((CommandListenerWrapper) commandcontext.getSource(), ArgumentPosition.getLoadedBlockPos(commandcontext, "pos"), ArgumentInventorySlot.getSlot(commandcontext, "slot"), ArgumentItemStack.getItem(commandcontext, "item").createItemStack(IntegerArgumentType.getInteger(commandcontext, "count"), true));
        }))))).then(((LiteralArgumentBuilder) net.minecraft.commands.CommandDispatcher.literal("from").then(net.minecraft.commands.CommandDispatcher.literal("block").then(net.minecraft.commands.CommandDispatcher.argument("source", ArgumentPosition.blockPos()).then(((RequiredArgumentBuilder) net.minecraft.commands.CommandDispatcher.argument("sourceSlot", ArgumentInventorySlot.slot()).executes((commandcontext) -> {
            return blockToBlock((CommandListenerWrapper) commandcontext.getSource(), ArgumentPosition.getLoadedBlockPos(commandcontext, "source"), ArgumentInventorySlot.getSlot(commandcontext, "sourceSlot"), ArgumentPosition.getLoadedBlockPos(commandcontext, "pos"), ArgumentInventorySlot.getSlot(commandcontext, "slot"));
        })).then(net.minecraft.commands.CommandDispatcher.argument("modifier", ResourceOrIdArgument.lootModifier(commandbuildcontext)).suggests(ItemCommands.SUGGEST_MODIFIER).executes((commandcontext) -> {
            return blockToBlock((CommandListenerWrapper) commandcontext.getSource(), ArgumentPosition.getLoadedBlockPos(commandcontext, "source"), ArgumentInventorySlot.getSlot(commandcontext, "sourceSlot"), ArgumentPosition.getLoadedBlockPos(commandcontext, "pos"), ArgumentInventorySlot.getSlot(commandcontext, "slot"), ResourceOrIdArgument.getLootModifier(commandcontext, "modifier"));
        })))))).then(net.minecraft.commands.CommandDispatcher.literal("entity").then(net.minecraft.commands.CommandDispatcher.argument("source", ArgumentEntity.entity()).then(((RequiredArgumentBuilder) net.minecraft.commands.CommandDispatcher.argument("sourceSlot", ArgumentInventorySlot.slot()).executes((commandcontext) -> {
            return entityToBlock((CommandListenerWrapper) commandcontext.getSource(), ArgumentEntity.getEntity(commandcontext, "source"), ArgumentInventorySlot.getSlot(commandcontext, "sourceSlot"), ArgumentPosition.getLoadedBlockPos(commandcontext, "pos"), ArgumentInventorySlot.getSlot(commandcontext, "slot"));
        })).then(net.minecraft.commands.CommandDispatcher.argument("modifier", ResourceOrIdArgument.lootModifier(commandbuildcontext)).suggests(ItemCommands.SUGGEST_MODIFIER).executes((commandcontext) -> {
            return entityToBlock((CommandListenerWrapper) commandcontext.getSource(), ArgumentEntity.getEntity(commandcontext, "source"), ArgumentInventorySlot.getSlot(commandcontext, "sourceSlot"), ArgumentPosition.getLoadedBlockPos(commandcontext, "pos"), ArgumentInventorySlot.getSlot(commandcontext, "slot"), ResourceOrIdArgument.getLootModifier(commandcontext, "modifier"));
        })))))))))).then(net.minecraft.commands.CommandDispatcher.literal("entity").then(net.minecraft.commands.CommandDispatcher.argument("targets", ArgumentEntity.entities()).then(((RequiredArgumentBuilder) net.minecraft.commands.CommandDispatcher.argument("slot", ArgumentInventorySlot.slot()).then(net.minecraft.commands.CommandDispatcher.literal("with").then(((RequiredArgumentBuilder) net.minecraft.commands.CommandDispatcher.argument("item", ArgumentItemStack.item(commandbuildcontext)).executes((commandcontext) -> {
            return setEntityItem((CommandListenerWrapper) commandcontext.getSource(), ArgumentEntity.getEntities(commandcontext, "targets"), ArgumentInventorySlot.getSlot(commandcontext, "slot"), ArgumentItemStack.getItem(commandcontext, "item").createItemStack(1, false));
        })).then(net.minecraft.commands.CommandDispatcher.argument("count", IntegerArgumentType.integer(1, 99)).executes((commandcontext) -> {
            return setEntityItem((CommandListenerWrapper) commandcontext.getSource(), ArgumentEntity.getEntities(commandcontext, "targets"), ArgumentInventorySlot.getSlot(commandcontext, "slot"), ArgumentItemStack.getItem(commandcontext, "item").createItemStack(IntegerArgumentType.getInteger(commandcontext, "count"), true));
        }))))).then(((LiteralArgumentBuilder) net.minecraft.commands.CommandDispatcher.literal("from").then(net.minecraft.commands.CommandDispatcher.literal("block").then(net.minecraft.commands.CommandDispatcher.argument("source", ArgumentPosition.blockPos()).then(((RequiredArgumentBuilder) net.minecraft.commands.CommandDispatcher.argument("sourceSlot", ArgumentInventorySlot.slot()).executes((commandcontext) -> {
            return blockToEntities((CommandListenerWrapper) commandcontext.getSource(), ArgumentPosition.getLoadedBlockPos(commandcontext, "source"), ArgumentInventorySlot.getSlot(commandcontext, "sourceSlot"), ArgumentEntity.getEntities(commandcontext, "targets"), ArgumentInventorySlot.getSlot(commandcontext, "slot"));
        })).then(net.minecraft.commands.CommandDispatcher.argument("modifier", ResourceOrIdArgument.lootModifier(commandbuildcontext)).suggests(ItemCommands.SUGGEST_MODIFIER).executes((commandcontext) -> {
            return blockToEntities((CommandListenerWrapper) commandcontext.getSource(), ArgumentPosition.getLoadedBlockPos(commandcontext, "source"), ArgumentInventorySlot.getSlot(commandcontext, "sourceSlot"), ArgumentEntity.getEntities(commandcontext, "targets"), ArgumentInventorySlot.getSlot(commandcontext, "slot"), ResourceOrIdArgument.getLootModifier(commandcontext, "modifier"));
        })))))).then(net.minecraft.commands.CommandDispatcher.literal("entity").then(net.minecraft.commands.CommandDispatcher.argument("source", ArgumentEntity.entity()).then(((RequiredArgumentBuilder) net.minecraft.commands.CommandDispatcher.argument("sourceSlot", ArgumentInventorySlot.slot()).executes((commandcontext) -> {
            return entityToEntities((CommandListenerWrapper) commandcontext.getSource(), ArgumentEntity.getEntity(commandcontext, "source"), ArgumentInventorySlot.getSlot(commandcontext, "sourceSlot"), ArgumentEntity.getEntities(commandcontext, "targets"), ArgumentInventorySlot.getSlot(commandcontext, "slot"));
        })).then(net.minecraft.commands.CommandDispatcher.argument("modifier", ResourceOrIdArgument.lootModifier(commandbuildcontext)).suggests(ItemCommands.SUGGEST_MODIFIER).executes((commandcontext) -> {
            return entityToEntities((CommandListenerWrapper) commandcontext.getSource(), ArgumentEntity.getEntity(commandcontext, "source"), ArgumentInventorySlot.getSlot(commandcontext, "sourceSlot"), ArgumentEntity.getEntities(commandcontext, "targets"), ArgumentInventorySlot.getSlot(commandcontext, "slot"), ResourceOrIdArgument.getLootModifier(commandcontext, "modifier"));
        }))))))))))).then(((LiteralArgumentBuilder) net.minecraft.commands.CommandDispatcher.literal("modify").then(net.minecraft.commands.CommandDispatcher.literal("block").then(net.minecraft.commands.CommandDispatcher.argument("pos", ArgumentPosition.blockPos()).then(net.minecraft.commands.CommandDispatcher.argument("slot", ArgumentInventorySlot.slot()).then(net.minecraft.commands.CommandDispatcher.argument("modifier", ResourceOrIdArgument.lootModifier(commandbuildcontext)).suggests(ItemCommands.SUGGEST_MODIFIER).executes((commandcontext) -> {
            return modifyBlockItem((CommandListenerWrapper) commandcontext.getSource(), ArgumentPosition.getLoadedBlockPos(commandcontext, "pos"), ArgumentInventorySlot.getSlot(commandcontext, "slot"), ResourceOrIdArgument.getLootModifier(commandcontext, "modifier"));
        })))))).then(net.minecraft.commands.CommandDispatcher.literal("entity").then(net.minecraft.commands.CommandDispatcher.argument("targets", ArgumentEntity.entities()).then(net.minecraft.commands.CommandDispatcher.argument("slot", ArgumentInventorySlot.slot()).then(net.minecraft.commands.CommandDispatcher.argument("modifier", ResourceOrIdArgument.lootModifier(commandbuildcontext)).suggests(ItemCommands.SUGGEST_MODIFIER).executes((commandcontext) -> {
            return modifyEntityItem((CommandListenerWrapper) commandcontext.getSource(), ArgumentEntity.getEntities(commandcontext, "targets"), ArgumentInventorySlot.getSlot(commandcontext, "slot"), ResourceOrIdArgument.getLootModifier(commandcontext, "modifier"));
        })))))));
    }

    private static int modifyBlockItem(CommandListenerWrapper commandlistenerwrapper, BlockPosition blockposition, int i, Holder<LootItemFunction> holder) throws CommandSyntaxException {
        IInventory iinventory = getContainer(commandlistenerwrapper, blockposition, ItemCommands.ERROR_TARGET_NOT_A_CONTAINER);

        if (i >= 0 && i < iinventory.getContainerSize()) {
            ItemStack itemstack = applyModifier(commandlistenerwrapper, holder, iinventory.getItem(i));

            iinventory.setItem(i, itemstack);
            commandlistenerwrapper.sendSuccess(() -> {
                return IChatBaseComponent.translatable("commands.item.block.set.success", blockposition.getX(), blockposition.getY(), blockposition.getZ(), itemstack.getDisplayName());
            }, true);
            return 1;
        } else {
            throw ItemCommands.ERROR_TARGET_INAPPLICABLE_SLOT.create(i);
        }
    }

    private static int modifyEntityItem(CommandListenerWrapper commandlistenerwrapper, Collection<? extends Entity> collection, int i, Holder<LootItemFunction> holder) throws CommandSyntaxException {
        Map<Entity, ItemStack> map = Maps.newHashMapWithExpectedSize(collection.size());
        Iterator iterator = collection.iterator();

        while (iterator.hasNext()) {
            Entity entity = (Entity) iterator.next();
            SlotAccess slotaccess = entity.getSlot(i);

            if (slotaccess != SlotAccess.NULL) {
                ItemStack itemstack = applyModifier(commandlistenerwrapper, holder, slotaccess.get().copy());

                if (slotaccess.set(itemstack)) {
                    map.put(entity, itemstack);
                    if (entity instanceof EntityPlayer) {
                        ((EntityPlayer) entity).containerMenu.broadcastChanges();
                    }
                }
            }
        }

        if (map.isEmpty()) {
            throw ItemCommands.ERROR_TARGET_NO_CHANGES.create(i);
        } else {
            if (map.size() == 1) {
                Entry<Entity, ItemStack> entry = (Entry) map.entrySet().iterator().next();

                commandlistenerwrapper.sendSuccess(() -> {
                    return IChatBaseComponent.translatable("commands.item.entity.set.success.single", ((Entity) entry.getKey()).getDisplayName(), ((ItemStack) entry.getValue()).getDisplayName());
                }, true);
            } else {
                commandlistenerwrapper.sendSuccess(() -> {
                    return IChatBaseComponent.translatable("commands.item.entity.set.success.multiple", map.size());
                }, true);
            }

            return map.size();
        }
    }

    private static int setBlockItem(CommandListenerWrapper commandlistenerwrapper, BlockPosition blockposition, int i, ItemStack itemstack) throws CommandSyntaxException {
        IInventory iinventory = getContainer(commandlistenerwrapper, blockposition, ItemCommands.ERROR_TARGET_NOT_A_CONTAINER);

        if (i >= 0 && i < iinventory.getContainerSize()) {
            iinventory.setItem(i, itemstack);
            commandlistenerwrapper.sendSuccess(() -> {
                return IChatBaseComponent.translatable("commands.item.block.set.success", blockposition.getX(), blockposition.getY(), blockposition.getZ(), itemstack.getDisplayName());
            }, true);
            return 1;
        } else {
            throw ItemCommands.ERROR_TARGET_INAPPLICABLE_SLOT.create(i);
        }
    }

    static IInventory getContainer(CommandListenerWrapper commandlistenerwrapper, BlockPosition blockposition, Dynamic3CommandExceptionType dynamic3commandexceptiontype) throws CommandSyntaxException {
        TileEntity tileentity = commandlistenerwrapper.getLevel().getBlockEntity(blockposition);

        if (!(tileentity instanceof IInventory)) {
            throw dynamic3commandexceptiontype.create(blockposition.getX(), blockposition.getY(), blockposition.getZ());
        } else {
            return (IInventory) tileentity;
        }
    }

    private static int setEntityItem(CommandListenerWrapper commandlistenerwrapper, Collection<? extends Entity> collection, int i, ItemStack itemstack) throws CommandSyntaxException {
        List<Entity> list = Lists.newArrayListWithCapacity(collection.size());
        Iterator iterator = collection.iterator();

        while (iterator.hasNext()) {
            Entity entity = (Entity) iterator.next();
            SlotAccess slotaccess = entity.getSlot(i);

            if (slotaccess != SlotAccess.NULL && slotaccess.set(itemstack.copy())) {
                list.add(entity);
                if (entity instanceof EntityPlayer) {
                    ((EntityPlayer) entity).containerMenu.broadcastChanges();
                }
            }
        }

        if (list.isEmpty()) {
            throw ItemCommands.ERROR_TARGET_NO_CHANGES_KNOWN_ITEM.create(itemstack.getDisplayName(), i);
        } else {
            if (list.size() == 1) {
                commandlistenerwrapper.sendSuccess(() -> {
                    return IChatBaseComponent.translatable("commands.item.entity.set.success.single", ((Entity) list.iterator().next()).getDisplayName(), itemstack.getDisplayName());
                }, true);
            } else {
                commandlistenerwrapper.sendSuccess(() -> {
                    return IChatBaseComponent.translatable("commands.item.entity.set.success.multiple", list.size(), itemstack.getDisplayName());
                }, true);
            }

            return list.size();
        }
    }

    private static int blockToEntities(CommandListenerWrapper commandlistenerwrapper, BlockPosition blockposition, int i, Collection<? extends Entity> collection, int j) throws CommandSyntaxException {
        return setEntityItem(commandlistenerwrapper, collection, j, getBlockItem(commandlistenerwrapper, blockposition, i));
    }

    private static int blockToEntities(CommandListenerWrapper commandlistenerwrapper, BlockPosition blockposition, int i, Collection<? extends Entity> collection, int j, Holder<LootItemFunction> holder) throws CommandSyntaxException {
        return setEntityItem(commandlistenerwrapper, collection, j, applyModifier(commandlistenerwrapper, holder, getBlockItem(commandlistenerwrapper, blockposition, i)));
    }

    private static int blockToBlock(CommandListenerWrapper commandlistenerwrapper, BlockPosition blockposition, int i, BlockPosition blockposition1, int j) throws CommandSyntaxException {
        return setBlockItem(commandlistenerwrapper, blockposition1, j, getBlockItem(commandlistenerwrapper, blockposition, i));
    }

    private static int blockToBlock(CommandListenerWrapper commandlistenerwrapper, BlockPosition blockposition, int i, BlockPosition blockposition1, int j, Holder<LootItemFunction> holder) throws CommandSyntaxException {
        return setBlockItem(commandlistenerwrapper, blockposition1, j, applyModifier(commandlistenerwrapper, holder, getBlockItem(commandlistenerwrapper, blockposition, i)));
    }

    private static int entityToBlock(CommandListenerWrapper commandlistenerwrapper, Entity entity, int i, BlockPosition blockposition, int j) throws CommandSyntaxException {
        return setBlockItem(commandlistenerwrapper, blockposition, j, getEntityItem(entity, i));
    }

    private static int entityToBlock(CommandListenerWrapper commandlistenerwrapper, Entity entity, int i, BlockPosition blockposition, int j, Holder<LootItemFunction> holder) throws CommandSyntaxException {
        return setBlockItem(commandlistenerwrapper, blockposition, j, applyModifier(commandlistenerwrapper, holder, getEntityItem(entity, i)));
    }

    private static int entityToEntities(CommandListenerWrapper commandlistenerwrapper, Entity entity, int i, Collection<? extends Entity> collection, int j) throws CommandSyntaxException {
        return setEntityItem(commandlistenerwrapper, collection, j, getEntityItem(entity, i));
    }

    private static int entityToEntities(CommandListenerWrapper commandlistenerwrapper, Entity entity, int i, Collection<? extends Entity> collection, int j, Holder<LootItemFunction> holder) throws CommandSyntaxException {
        return setEntityItem(commandlistenerwrapper, collection, j, applyModifier(commandlistenerwrapper, holder, getEntityItem(entity, i)));
    }

    private static ItemStack applyModifier(CommandListenerWrapper commandlistenerwrapper, Holder<LootItemFunction> holder, ItemStack itemstack) {
        WorldServer worldserver = commandlistenerwrapper.getLevel();
        LootParams lootparams = (new LootParams.a(worldserver)).withParameter(LootContextParameters.ORIGIN, commandlistenerwrapper.getPosition()).withOptionalParameter(LootContextParameters.THIS_ENTITY, commandlistenerwrapper.getEntity()).create(LootContextParameterSets.COMMAND);
        LootTableInfo loottableinfo = (new LootTableInfo.Builder(lootparams)).create(Optional.empty());

        loottableinfo.pushVisitedElement(LootTableInfo.createVisitedEntry((LootItemFunction) holder.value()));
        ItemStack itemstack1 = (ItemStack) ((LootItemFunction) holder.value()).apply(itemstack, loottableinfo);

        itemstack1.limitSize(itemstack1.getMaxStackSize());
        return itemstack1;
    }

    private static ItemStack getEntityItem(Entity entity, int i) throws CommandSyntaxException {
        SlotAccess slotaccess = entity.getSlot(i);

        if (slotaccess == SlotAccess.NULL) {
            throw ItemCommands.ERROR_SOURCE_INAPPLICABLE_SLOT.create(i);
        } else {
            return slotaccess.get().copy();
        }
    }

    private static ItemStack getBlockItem(CommandListenerWrapper commandlistenerwrapper, BlockPosition blockposition, int i) throws CommandSyntaxException {
        IInventory iinventory = getContainer(commandlistenerwrapper, blockposition, ItemCommands.ERROR_SOURCE_NOT_A_CONTAINER);

        if (i >= 0 && i < iinventory.getContainerSize()) {
            return iinventory.getItem(i).copy();
        } else {
            throw ItemCommands.ERROR_SOURCE_INAPPLICABLE_SLOT.create(i);
        }
    }
}
