package net.minecraft.server.commands.data;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;
import net.minecraft.commands.CommandListenerWrapper;
import net.minecraft.commands.arguments.ArgumentNBTBase;
import net.minecraft.commands.arguments.ArgumentNBTKey;
import net.minecraft.commands.arguments.ArgumentNBTTag;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTList;
import net.minecraft.nbt.NBTNumber;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.util.MathHelper;

public class CommandData {

    private static final SimpleCommandExceptionType ERROR_MERGE_UNCHANGED = new SimpleCommandExceptionType(IChatBaseComponent.translatable("commands.data.merge.failed"));
    private static final DynamicCommandExceptionType ERROR_GET_NOT_NUMBER = new DynamicCommandExceptionType((object) -> {
        return IChatBaseComponent.translatableEscape("commands.data.get.invalid", object);
    });
    private static final DynamicCommandExceptionType ERROR_GET_NON_EXISTENT = new DynamicCommandExceptionType((object) -> {
        return IChatBaseComponent.translatableEscape("commands.data.get.unknown", object);
    });
    private static final SimpleCommandExceptionType ERROR_MULTIPLE_TAGS = new SimpleCommandExceptionType(IChatBaseComponent.translatable("commands.data.get.multiple"));
    private static final DynamicCommandExceptionType ERROR_EXPECTED_OBJECT = new DynamicCommandExceptionType((object) -> {
        return IChatBaseComponent.translatableEscape("commands.data.modify.expected_object", object);
    });
    private static final DynamicCommandExceptionType ERROR_EXPECTED_VALUE = new DynamicCommandExceptionType((object) -> {
        return IChatBaseComponent.translatableEscape("commands.data.modify.expected_value", object);
    });
    private static final Dynamic2CommandExceptionType ERROR_INVALID_SUBSTRING = new Dynamic2CommandExceptionType((object, object1) -> {
        return IChatBaseComponent.translatableEscape("commands.data.modify.invalid_substring", object, object1);
    });
    public static final List<Function<String, CommandData.c>> ALL_PROVIDERS = ImmutableList.of(CommandDataAccessorEntity.PROVIDER, CommandDataAccessorTile.PROVIDER, CommandDataStorage.PROVIDER);
    public static final List<CommandData.c> TARGET_PROVIDERS = (List) CommandData.ALL_PROVIDERS.stream().map((function) -> {
        return (CommandData.c) function.apply("target");
    }).collect(ImmutableList.toImmutableList());
    public static final List<CommandData.c> SOURCE_PROVIDERS = (List) CommandData.ALL_PROVIDERS.stream().map((function) -> {
        return (CommandData.c) function.apply("source");
    }).collect(ImmutableList.toImmutableList());

    public CommandData() {}

    public static void register(CommandDispatcher<CommandListenerWrapper> commanddispatcher) {
        LiteralArgumentBuilder<CommandListenerWrapper> literalargumentbuilder = (LiteralArgumentBuilder) net.minecraft.commands.CommandDispatcher.literal("data").requires((commandlistenerwrapper) -> {
            return commandlistenerwrapper.hasPermission(2);
        });
        Iterator iterator = CommandData.TARGET_PROVIDERS.iterator();

        while (iterator.hasNext()) {
            CommandData.c commanddata_c = (CommandData.c) iterator.next();

            ((LiteralArgumentBuilder) ((LiteralArgumentBuilder) ((LiteralArgumentBuilder) literalargumentbuilder.then(commanddata_c.wrap(net.minecraft.commands.CommandDispatcher.literal("merge"), (argumentbuilder) -> {
                return argumentbuilder.then(net.minecraft.commands.CommandDispatcher.argument("nbt", ArgumentNBTTag.compoundTag()).executes((commandcontext) -> {
                    return mergeData((CommandListenerWrapper) commandcontext.getSource(), commanddata_c.access(commandcontext), ArgumentNBTTag.getCompoundTag(commandcontext, "nbt"));
                }));
            }))).then(commanddata_c.wrap(net.minecraft.commands.CommandDispatcher.literal("get"), (argumentbuilder) -> {
                return argumentbuilder.executes((commandcontext) -> {
                    return getData((CommandListenerWrapper) commandcontext.getSource(), commanddata_c.access(commandcontext));
                }).then(((RequiredArgumentBuilder) net.minecraft.commands.CommandDispatcher.argument("path", ArgumentNBTKey.nbtPath()).executes((commandcontext) -> {
                    return getData((CommandListenerWrapper) commandcontext.getSource(), commanddata_c.access(commandcontext), ArgumentNBTKey.getPath(commandcontext, "path"));
                })).then(net.minecraft.commands.CommandDispatcher.argument("scale", DoubleArgumentType.doubleArg()).executes((commandcontext) -> {
                    return getNumeric((CommandListenerWrapper) commandcontext.getSource(), commanddata_c.access(commandcontext), ArgumentNBTKey.getPath(commandcontext, "path"), DoubleArgumentType.getDouble(commandcontext, "scale"));
                })));
            }))).then(commanddata_c.wrap(net.minecraft.commands.CommandDispatcher.literal("remove"), (argumentbuilder) -> {
                return argumentbuilder.then(net.minecraft.commands.CommandDispatcher.argument("path", ArgumentNBTKey.nbtPath()).executes((commandcontext) -> {
                    return removeData((CommandListenerWrapper) commandcontext.getSource(), commanddata_c.access(commandcontext), ArgumentNBTKey.getPath(commandcontext, "path"));
                }));
            }))).then(decorateModification((argumentbuilder, commanddata_b) -> {
                argumentbuilder.then(net.minecraft.commands.CommandDispatcher.literal("insert").then(net.minecraft.commands.CommandDispatcher.argument("index", IntegerArgumentType.integer()).then(commanddata_b.create((commandcontext, nbttagcompound, argumentnbtkey_g, list) -> {
                    return argumentnbtkey_g.insert(IntegerArgumentType.getInteger(commandcontext, "index"), nbttagcompound, list);
                })))).then(net.minecraft.commands.CommandDispatcher.literal("prepend").then(commanddata_b.create((commandcontext, nbttagcompound, argumentnbtkey_g, list) -> {
                    return argumentnbtkey_g.insert(0, nbttagcompound, list);
                }))).then(net.minecraft.commands.CommandDispatcher.literal("append").then(commanddata_b.create((commandcontext, nbttagcompound, argumentnbtkey_g, list) -> {
                    return argumentnbtkey_g.insert(-1, nbttagcompound, list);
                }))).then(net.minecraft.commands.CommandDispatcher.literal("set").then(commanddata_b.create((commandcontext, nbttagcompound, argumentnbtkey_g, list) -> {
                    return argumentnbtkey_g.set(nbttagcompound, (NBTBase) Iterables.getLast(list));
                }))).then(net.minecraft.commands.CommandDispatcher.literal("merge").then(commanddata_b.create((commandcontext, nbttagcompound, argumentnbtkey_g, list) -> {
                    NBTTagCompound nbttagcompound1 = new NBTTagCompound();
                    Iterator iterator1 = list.iterator();

                    while (iterator1.hasNext()) {
                        NBTBase nbtbase = (NBTBase) iterator1.next();

                        if (ArgumentNBTKey.g.isTooDeep(nbtbase, 0)) {
                            throw ArgumentNBTKey.ERROR_DATA_TOO_DEEP.create();
                        }

                        if (!(nbtbase instanceof NBTTagCompound)) {
                            throw CommandData.ERROR_EXPECTED_OBJECT.create(nbtbase);
                        }

                        NBTTagCompound nbttagcompound2 = (NBTTagCompound) nbtbase;

                        nbttagcompound1.merge(nbttagcompound2);
                    }

                    Collection<NBTBase> collection = argumentnbtkey_g.getOrCreate(nbttagcompound, NBTTagCompound::new);
                    int i = 0;

                    NBTTagCompound nbttagcompound3;
                    NBTTagCompound nbttagcompound4;

                    for (Iterator iterator2 = collection.iterator(); iterator2.hasNext(); i += nbttagcompound4.equals(nbttagcompound3) ? 0 : 1) {
                        NBTBase nbtbase1 = (NBTBase) iterator2.next();

                        if (!(nbtbase1 instanceof NBTTagCompound)) {
                            throw CommandData.ERROR_EXPECTED_OBJECT.create(nbtbase1);
                        }

                        nbttagcompound3 = (NBTTagCompound) nbtbase1;
                        nbttagcompound4 = nbttagcompound3.copy();
                        nbttagcompound3.merge(nbttagcompound1);
                    }

                    return i;
                })));
            }));
        }

        commanddispatcher.register(literalargumentbuilder);
    }

    private static String getAsText(NBTBase nbtbase) throws CommandSyntaxException {
        if (nbtbase.getType().isValue()) {
            return nbtbase.getAsString();
        } else {
            throw CommandData.ERROR_EXPECTED_VALUE.create(nbtbase);
        }
    }

    private static List<NBTBase> stringifyTagList(List<NBTBase> list, CommandData.d commanddata_d) throws CommandSyntaxException {
        List<NBTBase> list1 = new ArrayList(list.size());
        Iterator iterator = list.iterator();

        while (iterator.hasNext()) {
            NBTBase nbtbase = (NBTBase) iterator.next();
            String s = getAsText(nbtbase);

            list1.add(NBTTagString.valueOf(commanddata_d.process(s)));
        }

        return list1;
    }

    private static ArgumentBuilder<CommandListenerWrapper, ?> decorateModification(BiConsumer<ArgumentBuilder<CommandListenerWrapper, ?>, CommandData.b> biconsumer) {
        LiteralArgumentBuilder<CommandListenerWrapper> literalargumentbuilder = net.minecraft.commands.CommandDispatcher.literal("modify");
        Iterator iterator = CommandData.TARGET_PROVIDERS.iterator();

        while (iterator.hasNext()) {
            CommandData.c commanddata_c = (CommandData.c) iterator.next();

            commanddata_c.wrap(literalargumentbuilder, (argumentbuilder) -> {
                ArgumentBuilder<CommandListenerWrapper, ?> argumentbuilder1 = net.minecraft.commands.CommandDispatcher.argument("targetPath", ArgumentNBTKey.nbtPath());
                Iterator iterator1 = CommandData.SOURCE_PROVIDERS.iterator();

                while (iterator1.hasNext()) {
                    CommandData.c commanddata_c1 = (CommandData.c) iterator1.next();

                    biconsumer.accept(argumentbuilder1, (commanddata_a) -> {
                        return commanddata_c1.wrap(net.minecraft.commands.CommandDispatcher.literal("from"), (argumentbuilder2) -> {
                            return argumentbuilder2.executes((commandcontext) -> {
                                return manipulateData(commandcontext, commanddata_c, commanddata_a, getSingletonSource(commandcontext, commanddata_c1));
                            }).then(net.minecraft.commands.CommandDispatcher.argument("sourcePath", ArgumentNBTKey.nbtPath()).executes((commandcontext) -> {
                                return manipulateData(commandcontext, commanddata_c, commanddata_a, resolveSourcePath(commandcontext, commanddata_c1));
                            }));
                        });
                    });
                    biconsumer.accept(argumentbuilder1, (commanddata_a) -> {
                        return commanddata_c1.wrap(net.minecraft.commands.CommandDispatcher.literal("string"), (argumentbuilder2) -> {
                            return argumentbuilder2.executes((commandcontext) -> {
                                return manipulateData(commandcontext, commanddata_c, commanddata_a, stringifyTagList(getSingletonSource(commandcontext, commanddata_c1), (s) -> {
                                    return s;
                                }));
                            }).then(((RequiredArgumentBuilder) net.minecraft.commands.CommandDispatcher.argument("sourcePath", ArgumentNBTKey.nbtPath()).executes((commandcontext) -> {
                                return manipulateData(commandcontext, commanddata_c, commanddata_a, stringifyTagList(resolveSourcePath(commandcontext, commanddata_c1), (s) -> {
                                    return s;
                                }));
                            })).then(((RequiredArgumentBuilder) net.minecraft.commands.CommandDispatcher.argument("start", IntegerArgumentType.integer()).executes((commandcontext) -> {
                                return manipulateData(commandcontext, commanddata_c, commanddata_a, stringifyTagList(resolveSourcePath(commandcontext, commanddata_c1), (s) -> {
                                    return substring(s, IntegerArgumentType.getInteger(commandcontext, "start"));
                                }));
                            })).then(net.minecraft.commands.CommandDispatcher.argument("end", IntegerArgumentType.integer()).executes((commandcontext) -> {
                                return manipulateData(commandcontext, commanddata_c, commanddata_a, stringifyTagList(resolveSourcePath(commandcontext, commanddata_c1), (s) -> {
                                    return substring(s, IntegerArgumentType.getInteger(commandcontext, "start"), IntegerArgumentType.getInteger(commandcontext, "end"));
                                }));
                            }))));
                        });
                    });
                }

                biconsumer.accept(argumentbuilder1, (commanddata_a) -> {
                    return net.minecraft.commands.CommandDispatcher.literal("value").then(net.minecraft.commands.CommandDispatcher.argument("value", ArgumentNBTBase.nbtTag()).executes((commandcontext) -> {
                        List<NBTBase> list = Collections.singletonList(ArgumentNBTBase.getNbtTag(commandcontext, "value"));

                        return manipulateData(commandcontext, commanddata_c, commanddata_a, list);
                    }));
                });
                return argumentbuilder.then(argumentbuilder1);
            });
        }

        return literalargumentbuilder;
    }

    private static String validatedSubstring(String s, int i, int j) throws CommandSyntaxException {
        if (i >= 0 && j <= s.length() && i <= j) {
            return s.substring(i, j);
        } else {
            throw CommandData.ERROR_INVALID_SUBSTRING.create(i, j);
        }
    }

    private static String substring(String s, int i, int j) throws CommandSyntaxException {
        int k = s.length();
        int l = getOffset(i, k);
        int i1 = getOffset(j, k);

        return validatedSubstring(s, l, i1);
    }

    private static String substring(String s, int i) throws CommandSyntaxException {
        int j = s.length();

        return validatedSubstring(s, getOffset(i, j), j);
    }

    private static int getOffset(int i, int j) {
        return i >= 0 ? i : j + i;
    }

    private static List<NBTBase> getSingletonSource(CommandContext<CommandListenerWrapper> commandcontext, CommandData.c commanddata_c) throws CommandSyntaxException {
        CommandDataAccessor commanddataaccessor = commanddata_c.access(commandcontext);

        return Collections.singletonList(commanddataaccessor.getData());
    }

    private static List<NBTBase> resolveSourcePath(CommandContext<CommandListenerWrapper> commandcontext, CommandData.c commanddata_c) throws CommandSyntaxException {
        CommandDataAccessor commanddataaccessor = commanddata_c.access(commandcontext);
        ArgumentNBTKey.g argumentnbtkey_g = ArgumentNBTKey.getPath(commandcontext, "sourcePath");

        return argumentnbtkey_g.get(commanddataaccessor.getData());
    }

    private static int manipulateData(CommandContext<CommandListenerWrapper> commandcontext, CommandData.c commanddata_c, CommandData.a commanddata_a, List<NBTBase> list) throws CommandSyntaxException {
        CommandDataAccessor commanddataaccessor = commanddata_c.access(commandcontext);
        ArgumentNBTKey.g argumentnbtkey_g = ArgumentNBTKey.getPath(commandcontext, "targetPath");
        NBTTagCompound nbttagcompound = commanddataaccessor.getData();
        int i = commanddata_a.modify(commandcontext, nbttagcompound, argumentnbtkey_g, list);

        if (i == 0) {
            throw CommandData.ERROR_MERGE_UNCHANGED.create();
        } else {
            commanddataaccessor.setData(nbttagcompound);
            ((CommandListenerWrapper) commandcontext.getSource()).sendSuccess(() -> {
                return commanddataaccessor.getModifiedSuccess();
            }, true);
            return i;
        }
    }

    private static int removeData(CommandListenerWrapper commandlistenerwrapper, CommandDataAccessor commanddataaccessor, ArgumentNBTKey.g argumentnbtkey_g) throws CommandSyntaxException {
        NBTTagCompound nbttagcompound = commanddataaccessor.getData();
        int i = argumentnbtkey_g.remove(nbttagcompound);

        if (i == 0) {
            throw CommandData.ERROR_MERGE_UNCHANGED.create();
        } else {
            commanddataaccessor.setData(nbttagcompound);
            commandlistenerwrapper.sendSuccess(() -> {
                return commanddataaccessor.getModifiedSuccess();
            }, true);
            return i;
        }
    }

    public static NBTBase getSingleTag(ArgumentNBTKey.g argumentnbtkey_g, CommandDataAccessor commanddataaccessor) throws CommandSyntaxException {
        Collection<NBTBase> collection = argumentnbtkey_g.get(commanddataaccessor.getData());
        Iterator<NBTBase> iterator = collection.iterator();
        NBTBase nbtbase = (NBTBase) iterator.next();

        if (iterator.hasNext()) {
            throw CommandData.ERROR_MULTIPLE_TAGS.create();
        } else {
            return nbtbase;
        }
    }

    private static int getData(CommandListenerWrapper commandlistenerwrapper, CommandDataAccessor commanddataaccessor, ArgumentNBTKey.g argumentnbtkey_g) throws CommandSyntaxException {
        NBTBase nbtbase = getSingleTag(argumentnbtkey_g, commanddataaccessor);
        int i;

        if (nbtbase instanceof NBTNumber) {
            i = MathHelper.floor(((NBTNumber) nbtbase).getAsDouble());
        } else if (nbtbase instanceof NBTList) {
            i = ((NBTList) nbtbase).size();
        } else if (nbtbase instanceof NBTTagCompound) {
            i = ((NBTTagCompound) nbtbase).size();
        } else {
            if (!(nbtbase instanceof NBTTagString)) {
                throw CommandData.ERROR_GET_NON_EXISTENT.create(argumentnbtkey_g.toString());
            }

            i = nbtbase.getAsString().length();
        }

        commandlistenerwrapper.sendSuccess(() -> {
            return commanddataaccessor.getPrintSuccess(nbtbase);
        }, false);
        return i;
    }

    private static int getNumeric(CommandListenerWrapper commandlistenerwrapper, CommandDataAccessor commanddataaccessor, ArgumentNBTKey.g argumentnbtkey_g, double d0) throws CommandSyntaxException {
        NBTBase nbtbase = getSingleTag(argumentnbtkey_g, commanddataaccessor);

        if (!(nbtbase instanceof NBTNumber)) {
            throw CommandData.ERROR_GET_NOT_NUMBER.create(argumentnbtkey_g.toString());
        } else {
            int i = MathHelper.floor(((NBTNumber) nbtbase).getAsDouble() * d0);

            commandlistenerwrapper.sendSuccess(() -> {
                return commanddataaccessor.getPrintSuccess(argumentnbtkey_g, d0, i);
            }, false);
            return i;
        }
    }

    private static int getData(CommandListenerWrapper commandlistenerwrapper, CommandDataAccessor commanddataaccessor) throws CommandSyntaxException {
        NBTTagCompound nbttagcompound = commanddataaccessor.getData();

        commandlistenerwrapper.sendSuccess(() -> {
            return commanddataaccessor.getPrintSuccess(nbttagcompound);
        }, false);
        return 1;
    }

    private static int mergeData(CommandListenerWrapper commandlistenerwrapper, CommandDataAccessor commanddataaccessor, NBTTagCompound nbttagcompound) throws CommandSyntaxException {
        NBTTagCompound nbttagcompound1 = commanddataaccessor.getData();

        if (ArgumentNBTKey.g.isTooDeep(nbttagcompound, 0)) {
            throw ArgumentNBTKey.ERROR_DATA_TOO_DEEP.create();
        } else {
            NBTTagCompound nbttagcompound2 = nbttagcompound1.copy().merge(nbttagcompound);

            if (nbttagcompound1.equals(nbttagcompound2)) {
                throw CommandData.ERROR_MERGE_UNCHANGED.create();
            } else {
                commanddataaccessor.setData(nbttagcompound2);
                commandlistenerwrapper.sendSuccess(() -> {
                    return commanddataaccessor.getModifiedSuccess();
                }, true);
                return 1;
            }
        }
    }

    public interface c {

        CommandDataAccessor access(CommandContext<CommandListenerWrapper> commandcontext) throws CommandSyntaxException;

        ArgumentBuilder<CommandListenerWrapper, ?> wrap(ArgumentBuilder<CommandListenerWrapper, ?> argumentbuilder, Function<ArgumentBuilder<CommandListenerWrapper, ?>, ArgumentBuilder<CommandListenerWrapper, ?>> function);
    }

    @FunctionalInterface
    private interface d {

        String process(String s) throws CommandSyntaxException;
    }

    @FunctionalInterface
    private interface a {

        int modify(CommandContext<CommandListenerWrapper> commandcontext, NBTTagCompound nbttagcompound, ArgumentNBTKey.g argumentnbtkey_g, List<NBTBase> list) throws CommandSyntaxException;
    }

    @FunctionalInterface
    private interface b {

        ArgumentBuilder<CommandListenerWrapper, ?> create(CommandData.a commanddata_a);
    }
}
