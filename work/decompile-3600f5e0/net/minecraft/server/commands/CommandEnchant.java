package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.util.Collection;
import java.util.Iterator;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandListenerWrapper;
import net.minecraft.commands.arguments.ArgumentEntity;
import net.minecraft.commands.arguments.ResourceArgument;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentManager;

public class CommandEnchant {

    private static final DynamicCommandExceptionType ERROR_NOT_LIVING_ENTITY = new DynamicCommandExceptionType((object) -> {
        return IChatBaseComponent.translatableEscape("commands.enchant.failed.entity", object);
    });
    private static final DynamicCommandExceptionType ERROR_NO_ITEM = new DynamicCommandExceptionType((object) -> {
        return IChatBaseComponent.translatableEscape("commands.enchant.failed.itemless", object);
    });
    private static final DynamicCommandExceptionType ERROR_INCOMPATIBLE = new DynamicCommandExceptionType((object) -> {
        return IChatBaseComponent.translatableEscape("commands.enchant.failed.incompatible", object);
    });
    private static final Dynamic2CommandExceptionType ERROR_LEVEL_TOO_HIGH = new Dynamic2CommandExceptionType((object, object1) -> {
        return IChatBaseComponent.translatableEscape("commands.enchant.failed.level", object, object1);
    });
    private static final SimpleCommandExceptionType ERROR_NOTHING_HAPPENED = new SimpleCommandExceptionType(IChatBaseComponent.translatable("commands.enchant.failed"));

    public CommandEnchant() {}

    public static void register(CommandDispatcher<CommandListenerWrapper> commanddispatcher, CommandBuildContext commandbuildcontext) {
        commanddispatcher.register((LiteralArgumentBuilder) ((LiteralArgumentBuilder) net.minecraft.commands.CommandDispatcher.literal("enchant").requires((commandlistenerwrapper) -> {
            return commandlistenerwrapper.hasPermission(2);
        })).then(net.minecraft.commands.CommandDispatcher.argument("targets", ArgumentEntity.entities()).then(((RequiredArgumentBuilder) net.minecraft.commands.CommandDispatcher.argument("enchantment", ResourceArgument.resource(commandbuildcontext, Registries.ENCHANTMENT)).executes((commandcontext) -> {
            return enchant((CommandListenerWrapper) commandcontext.getSource(), ArgumentEntity.getEntities(commandcontext, "targets"), ResourceArgument.getEnchantment(commandcontext, "enchantment"), 1);
        })).then(net.minecraft.commands.CommandDispatcher.argument("level", IntegerArgumentType.integer(0)).executes((commandcontext) -> {
            return enchant((CommandListenerWrapper) commandcontext.getSource(), ArgumentEntity.getEntities(commandcontext, "targets"), ResourceArgument.getEnchantment(commandcontext, "enchantment"), IntegerArgumentType.getInteger(commandcontext, "level"));
        })))));
    }

    private static int enchant(CommandListenerWrapper commandlistenerwrapper, Collection<? extends Entity> collection, Holder<Enchantment> holder, int i) throws CommandSyntaxException {
        Enchantment enchantment = (Enchantment) holder.value();

        if (i > enchantment.getMaxLevel()) {
            throw CommandEnchant.ERROR_LEVEL_TOO_HIGH.create(i, enchantment.getMaxLevel());
        } else {
            int j = 0;
            Iterator iterator = collection.iterator();

            while (iterator.hasNext()) {
                Entity entity = (Entity) iterator.next();

                if (entity instanceof EntityLiving) {
                    EntityLiving entityliving = (EntityLiving) entity;
                    ItemStack itemstack = entityliving.getMainHandItem();

                    if (!itemstack.isEmpty()) {
                        if (enchantment.canEnchant(itemstack) && EnchantmentManager.isEnchantmentCompatible(EnchantmentManager.getEnchantmentsForCrafting(itemstack).keySet(), enchantment)) {
                            itemstack.enchant(enchantment, i);
                            ++j;
                        } else if (collection.size() == 1) {
                            throw CommandEnchant.ERROR_INCOMPATIBLE.create(itemstack.getItem().getName(itemstack).getString());
                        }
                    } else if (collection.size() == 1) {
                        throw CommandEnchant.ERROR_NO_ITEM.create(entityliving.getName().getString());
                    }
                } else if (collection.size() == 1) {
                    throw CommandEnchant.ERROR_NOT_LIVING_ENTITY.create(entity.getName().getString());
                }
            }

            if (j == 0) {
                throw CommandEnchant.ERROR_NOTHING_HAPPENED.create();
            } else {
                if (collection.size() == 1) {
                    commandlistenerwrapper.sendSuccess(() -> {
                        return IChatBaseComponent.translatable("commands.enchant.success.single", enchantment.getFullname(i), ((Entity) collection.iterator().next()).getDisplayName());
                    }, true);
                } else {
                    commandlistenerwrapper.sendSuccess(() -> {
                        return IChatBaseComponent.translatable("commands.enchant.success.multiple", enchantment.getFullname(i), collection.size());
                    }, true);
                }

                return j;
            }
        }
    }
}
