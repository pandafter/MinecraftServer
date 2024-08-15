package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import net.minecraft.commands.CommandListenerWrapper;
import net.minecraft.commands.arguments.ArgumentEntity;
import net.minecraft.commands.arguments.ArgumentMinecraftKeyRegistered;
import net.minecraft.commands.synchronization.CompletionProviders;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.world.item.crafting.RecipeHolder;

public class CommandRecipe {

    private static final SimpleCommandExceptionType ERROR_GIVE_FAILED = new SimpleCommandExceptionType(IChatBaseComponent.translatable("commands.recipe.give.failed"));
    private static final SimpleCommandExceptionType ERROR_TAKE_FAILED = new SimpleCommandExceptionType(IChatBaseComponent.translatable("commands.recipe.take.failed"));

    public CommandRecipe() {}

    public static void register(CommandDispatcher<CommandListenerWrapper> commanddispatcher) {
        commanddispatcher.register((LiteralArgumentBuilder) ((LiteralArgumentBuilder) ((LiteralArgumentBuilder) net.minecraft.commands.CommandDispatcher.literal("recipe").requires((commandlistenerwrapper) -> {
            return commandlistenerwrapper.hasPermission(2);
        })).then(net.minecraft.commands.CommandDispatcher.literal("give").then(((RequiredArgumentBuilder) net.minecraft.commands.CommandDispatcher.argument("targets", ArgumentEntity.players()).then(net.minecraft.commands.CommandDispatcher.argument("recipe", ArgumentMinecraftKeyRegistered.id()).suggests(CompletionProviders.ALL_RECIPES).executes((commandcontext) -> {
            return giveRecipes((CommandListenerWrapper) commandcontext.getSource(), ArgumentEntity.getPlayers(commandcontext, "targets"), Collections.singleton(ArgumentMinecraftKeyRegistered.getRecipe(commandcontext, "recipe")));
        }))).then(net.minecraft.commands.CommandDispatcher.literal("*").executes((commandcontext) -> {
            return giveRecipes((CommandListenerWrapper) commandcontext.getSource(), ArgumentEntity.getPlayers(commandcontext, "targets"), ((CommandListenerWrapper) commandcontext.getSource()).getServer().getRecipeManager().getRecipes());
        }))))).then(net.minecraft.commands.CommandDispatcher.literal("take").then(((RequiredArgumentBuilder) net.minecraft.commands.CommandDispatcher.argument("targets", ArgumentEntity.players()).then(net.minecraft.commands.CommandDispatcher.argument("recipe", ArgumentMinecraftKeyRegistered.id()).suggests(CompletionProviders.ALL_RECIPES).executes((commandcontext) -> {
            return takeRecipes((CommandListenerWrapper) commandcontext.getSource(), ArgumentEntity.getPlayers(commandcontext, "targets"), Collections.singleton(ArgumentMinecraftKeyRegistered.getRecipe(commandcontext, "recipe")));
        }))).then(net.minecraft.commands.CommandDispatcher.literal("*").executes((commandcontext) -> {
            return takeRecipes((CommandListenerWrapper) commandcontext.getSource(), ArgumentEntity.getPlayers(commandcontext, "targets"), ((CommandListenerWrapper) commandcontext.getSource()).getServer().getRecipeManager().getRecipes());
        })))));
    }

    private static int giveRecipes(CommandListenerWrapper commandlistenerwrapper, Collection<EntityPlayer> collection, Collection<RecipeHolder<?>> collection1) throws CommandSyntaxException {
        int i = 0;

        EntityPlayer entityplayer;

        for (Iterator iterator = collection.iterator(); iterator.hasNext(); i += entityplayer.awardRecipes(collection1)) {
            entityplayer = (EntityPlayer) iterator.next();
        }

        if (i == 0) {
            throw CommandRecipe.ERROR_GIVE_FAILED.create();
        } else {
            if (collection.size() == 1) {
                commandlistenerwrapper.sendSuccess(() -> {
                    return IChatBaseComponent.translatable("commands.recipe.give.success.single", collection1.size(), ((EntityPlayer) collection.iterator().next()).getDisplayName());
                }, true);
            } else {
                commandlistenerwrapper.sendSuccess(() -> {
                    return IChatBaseComponent.translatable("commands.recipe.give.success.multiple", collection1.size(), collection.size());
                }, true);
            }

            return i;
        }
    }

    private static int takeRecipes(CommandListenerWrapper commandlistenerwrapper, Collection<EntityPlayer> collection, Collection<RecipeHolder<?>> collection1) throws CommandSyntaxException {
        int i = 0;

        EntityPlayer entityplayer;

        for (Iterator iterator = collection.iterator(); iterator.hasNext(); i += entityplayer.resetRecipes(collection1)) {
            entityplayer = (EntityPlayer) iterator.next();
        }

        if (i == 0) {
            throw CommandRecipe.ERROR_TAKE_FAILED.create();
        } else {
            if (collection.size() == 1) {
                commandlistenerwrapper.sendSuccess(() -> {
                    return IChatBaseComponent.translatable("commands.recipe.take.success.single", collection1.size(), ((EntityPlayer) collection.iterator().next()).getDisplayName());
                }, true);
            } else {
                commandlistenerwrapper.sendSuccess(() -> {
                    return IChatBaseComponent.translatable("commands.recipe.take.success.multiple", collection1.size(), collection.size());
                }, true);
            }

            return i;
        }
    }
}
