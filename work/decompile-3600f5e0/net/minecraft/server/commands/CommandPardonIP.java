package net.minecraft.server.commands;

import com.google.common.net.InetAddresses;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.commands.CommandListenerWrapper;
import net.minecraft.commands.ICompletionProvider;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.server.players.IpBanList;

public class CommandPardonIP {

    private static final SimpleCommandExceptionType ERROR_INVALID = new SimpleCommandExceptionType(IChatBaseComponent.translatable("commands.pardonip.invalid"));
    private static final SimpleCommandExceptionType ERROR_NOT_BANNED = new SimpleCommandExceptionType(IChatBaseComponent.translatable("commands.pardonip.failed"));

    public CommandPardonIP() {}

    public static void register(CommandDispatcher<CommandListenerWrapper> commanddispatcher) {
        commanddispatcher.register((LiteralArgumentBuilder) ((LiteralArgumentBuilder) net.minecraft.commands.CommandDispatcher.literal("pardon-ip").requires((commandlistenerwrapper) -> {
            return commandlistenerwrapper.hasPermission(3);
        })).then(net.minecraft.commands.CommandDispatcher.argument("target", StringArgumentType.word()).suggests((commandcontext, suggestionsbuilder) -> {
            return ICompletionProvider.suggest(((CommandListenerWrapper) commandcontext.getSource()).getServer().getPlayerList().getIpBans().getUserList(), suggestionsbuilder);
        }).executes((commandcontext) -> {
            return unban((CommandListenerWrapper) commandcontext.getSource(), StringArgumentType.getString(commandcontext, "target"));
        })));
    }

    private static int unban(CommandListenerWrapper commandlistenerwrapper, String s) throws CommandSyntaxException {
        if (!InetAddresses.isInetAddress(s)) {
            throw CommandPardonIP.ERROR_INVALID.create();
        } else {
            IpBanList ipbanlist = commandlistenerwrapper.getServer().getPlayerList().getIpBans();

            if (!ipbanlist.isBanned(s)) {
                throw CommandPardonIP.ERROR_NOT_BANNED.create();
            } else {
                ipbanlist.remove((Object) s);
                commandlistenerwrapper.sendSuccess(() -> {
                    return IChatBaseComponent.translatable("commands.pardonip.success", s);
                }, true);
                return 1;
            }
        }
    }
}
