package net.minecraft.commands.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import java.util.Arrays;
import java.util.Collection;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandListenerWrapper;
import net.minecraft.commands.ParserUtils;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.chat.IChatBaseComponent;

public class ArgumentChatComponent implements ArgumentType<IChatBaseComponent> {

    private static final Collection<String> EXAMPLES = Arrays.asList("\"hello world\"", "\"\"", "\"{\"text\":\"hello world\"}", "[\"\"]");
    public static final DynamicCommandExceptionType ERROR_INVALID_JSON = new DynamicCommandExceptionType((object) -> {
        return IChatBaseComponent.translatableEscape("argument.component.invalid", object);
    });
    private final HolderLookup.a registries;

    private ArgumentChatComponent(HolderLookup.a holderlookup_a) {
        this.registries = holderlookup_a;
    }

    public static IChatBaseComponent getComponent(CommandContext<CommandListenerWrapper> commandcontext, String s) {
        return (IChatBaseComponent) commandcontext.getArgument(s, IChatBaseComponent.class);
    }

    public static ArgumentChatComponent textComponent(CommandBuildContext commandbuildcontext) {
        return new ArgumentChatComponent(commandbuildcontext);
    }

    public IChatBaseComponent parse(StringReader stringreader) throws CommandSyntaxException {
        try {
            return (IChatBaseComponent) ParserUtils.parseJson(this.registries, stringreader, ComponentSerialization.CODEC);
        } catch (Exception exception) {
            String s = exception.getCause() != null ? exception.getCause().getMessage() : exception.getMessage();

            throw ArgumentChatComponent.ERROR_INVALID_JSON.createWithContext(stringreader, s);
        }
    }

    public Collection<String> getExamples() {
        return ArgumentChatComponent.EXAMPLES;
    }
}
