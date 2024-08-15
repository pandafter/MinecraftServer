package net.minecraft.commands.arguments;

import com.google.gson.JsonObject;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Supplier;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandListenerWrapper;
import net.minecraft.commands.ICompletionProvider;
import net.minecraft.commands.arguments.selector.ArgumentParserSelector;
import net.minecraft.commands.arguments.selector.EntitySelector;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.network.PacketDataSerializer;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ScoreboardServer;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.server.level.WorldServer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.scores.ScoreHolder;

public class ArgumentScoreholder implements ArgumentType<ArgumentScoreholder.b> {

    public static final SuggestionProvider<CommandListenerWrapper> SUGGEST_SCORE_HOLDERS = (commandcontext, suggestionsbuilder) -> {
        StringReader stringreader = new StringReader(suggestionsbuilder.getInput());

        stringreader.setCursor(suggestionsbuilder.getStart());
        ArgumentParserSelector argumentparserselector = new ArgumentParserSelector(stringreader);

        try {
            argumentparserselector.parse();
        } catch (CommandSyntaxException commandsyntaxexception) {
            ;
        }

        return argumentparserselector.fillSuggestions(suggestionsbuilder, (suggestionsbuilder1) -> {
            ICompletionProvider.suggest((Iterable) ((CommandListenerWrapper) commandcontext.getSource()).getOnlinePlayerNames(), suggestionsbuilder1);
        });
    };
    private static final Collection<String> EXAMPLES = Arrays.asList("Player", "0123", "*", "@e");
    private static final SimpleCommandExceptionType ERROR_NO_RESULTS = new SimpleCommandExceptionType(IChatBaseComponent.translatable("argument.scoreHolder.empty"));
    final boolean multiple;

    public ArgumentScoreholder(boolean flag) {
        this.multiple = flag;
    }

    public static ScoreHolder getName(CommandContext<CommandListenerWrapper> commandcontext, String s) throws CommandSyntaxException {
        return (ScoreHolder) getNames(commandcontext, s).iterator().next();
    }

    public static Collection<ScoreHolder> getNames(CommandContext<CommandListenerWrapper> commandcontext, String s) throws CommandSyntaxException {
        return getNames(commandcontext, s, Collections::emptyList);
    }

    public static Collection<ScoreHolder> getNamesWithDefaultWildcard(CommandContext<CommandListenerWrapper> commandcontext, String s) throws CommandSyntaxException {
        ScoreboardServer scoreboardserver = ((CommandListenerWrapper) commandcontext.getSource()).getServer().getScoreboard();

        Objects.requireNonNull(scoreboardserver);
        return getNames(commandcontext, s, scoreboardserver::getTrackedPlayers);
    }

    public static Collection<ScoreHolder> getNames(CommandContext<CommandListenerWrapper> commandcontext, String s, Supplier<Collection<ScoreHolder>> supplier) throws CommandSyntaxException {
        Collection<ScoreHolder> collection = ((ArgumentScoreholder.b) commandcontext.getArgument(s, ArgumentScoreholder.b.class)).getNames((CommandListenerWrapper) commandcontext.getSource(), supplier);

        if (collection.isEmpty()) {
            throw ArgumentEntity.NO_ENTITIES_FOUND.create();
        } else {
            return collection;
        }
    }

    public static ArgumentScoreholder scoreHolder() {
        return new ArgumentScoreholder(false);
    }

    public static ArgumentScoreholder scoreHolders() {
        return new ArgumentScoreholder(true);
    }

    public ArgumentScoreholder.b parse(StringReader stringreader) throws CommandSyntaxException {
        if (stringreader.canRead() && stringreader.peek() == '@') {
            ArgumentParserSelector argumentparserselector = new ArgumentParserSelector(stringreader);
            EntitySelector entityselector = argumentparserselector.parse();

            if (!this.multiple && entityselector.getMaxResults() > 1) {
                throw ArgumentEntity.ERROR_NOT_SINGLE_ENTITY.createWithContext(stringreader);
            } else {
                return new ArgumentScoreholder.c(entityselector);
            }
        } else {
            int i = stringreader.getCursor();

            while (stringreader.canRead() && stringreader.peek() != ' ') {
                stringreader.skip();
            }

            String s = stringreader.getString().substring(i, stringreader.getCursor());

            if (s.equals("*")) {
                return (commandlistenerwrapper, supplier) -> {
                    Collection<ScoreHolder> collection = (Collection) supplier.get();

                    if (collection.isEmpty()) {
                        throw ArgumentScoreholder.ERROR_NO_RESULTS.create();
                    } else {
                        return collection;
                    }
                };
            } else {
                List<ScoreHolder> list = List.of(ScoreHolder.forNameOnly(s));

                if (s.startsWith("#")) {
                    return (commandlistenerwrapper, supplier) -> {
                        return list;
                    };
                } else {
                    try {
                        UUID uuid = UUID.fromString(s);

                        return (commandlistenerwrapper, supplier) -> {
                            MinecraftServer minecraftserver = commandlistenerwrapper.getServer();
                            Entity entity = null;
                            List<ScoreHolder> list1 = null;
                            Iterator iterator = minecraftserver.getAllLevels().iterator();

                            while (iterator.hasNext()) {
                                WorldServer worldserver = (WorldServer) iterator.next();
                                Entity entity1 = worldserver.getEntity(uuid);

                                if (entity1 != null) {
                                    if (entity == null) {
                                        entity = entity1;
                                    } else {
                                        if (list1 == null) {
                                            list1 = new ArrayList();
                                            list1.add(entity);
                                        }

                                        list1.add(entity1);
                                    }
                                }
                            }

                            if (list1 != null) {
                                return list1;
                            } else if (entity != null) {
                                return List.of(entity);
                            } else {
                                return list;
                            }
                        };
                    } catch (IllegalArgumentException illegalargumentexception) {
                        return (commandlistenerwrapper, supplier) -> {
                            MinecraftServer minecraftserver = commandlistenerwrapper.getServer();
                            EntityPlayer entityplayer = minecraftserver.getPlayerList().getPlayerByName(s);

                            return entityplayer != null ? List.of(entityplayer) : list;
                        };
                    }
                }
            }
        }
    }

    public Collection<String> getExamples() {
        return ArgumentScoreholder.EXAMPLES;
    }

    @FunctionalInterface
    public interface b {

        Collection<ScoreHolder> getNames(CommandListenerWrapper commandlistenerwrapper, Supplier<Collection<ScoreHolder>> supplier) throws CommandSyntaxException;
    }

    public static class c implements ArgumentScoreholder.b {

        private final EntitySelector selector;

        public c(EntitySelector entityselector) {
            this.selector = entityselector;
        }

        @Override
        public Collection<ScoreHolder> getNames(CommandListenerWrapper commandlistenerwrapper, Supplier<Collection<ScoreHolder>> supplier) throws CommandSyntaxException {
            List<? extends Entity> list = this.selector.findEntities(commandlistenerwrapper);

            if (list.isEmpty()) {
                throw ArgumentEntity.NO_ENTITIES_FOUND.create();
            } else {
                return List.copyOf(list);
            }
        }
    }

    public static class a implements ArgumentTypeInfo<ArgumentScoreholder, ArgumentScoreholder.a.a> {

        private static final byte FLAG_MULTIPLE = 1;

        public a() {}

        public void serializeToNetwork(ArgumentScoreholder.a.a argumentscoreholder_a_a, PacketDataSerializer packetdataserializer) {
            int i = 0;

            if (argumentscoreholder_a_a.multiple) {
                i |= 1;
            }

            packetdataserializer.writeByte(i);
        }

        @Override
        public ArgumentScoreholder.a.a deserializeFromNetwork(PacketDataSerializer packetdataserializer) {
            byte b0 = packetdataserializer.readByte();
            boolean flag = (b0 & 1) != 0;

            return new ArgumentScoreholder.a.a(flag);
        }

        public void serializeToJson(ArgumentScoreholder.a.a argumentscoreholder_a_a, JsonObject jsonobject) {
            jsonobject.addProperty("amount", argumentscoreholder_a_a.multiple ? "multiple" : "single");
        }

        public ArgumentScoreholder.a.a unpack(ArgumentScoreholder argumentscoreholder) {
            return new ArgumentScoreholder.a.a(argumentscoreholder.multiple);
        }

        public final class a implements ArgumentTypeInfo.a<ArgumentScoreholder> {

            final boolean multiple;

            a(final boolean flag) {
                this.multiple = flag;
            }

            @Override
            public ArgumentScoreholder instantiate(CommandBuildContext commandbuildcontext) {
                return new ArgumentScoreholder(this.multiple);
            }

            @Override
            public ArgumentTypeInfo<ArgumentScoreholder, ?> type() {
                return a.this;
            }
        }
    }
}
