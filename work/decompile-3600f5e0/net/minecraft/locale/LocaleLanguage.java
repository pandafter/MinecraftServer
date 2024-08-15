package net.minecraft.locale;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.regex.Pattern;
import net.minecraft.network.chat.ChatModifier;
import net.minecraft.network.chat.IChatFormatted;
import net.minecraft.util.ChatDeserializer;
import net.minecraft.util.FormattedString;
import net.minecraft.util.StringDecomposer;
import org.slf4j.Logger;

public abstract class LocaleLanguage {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Gson GSON = new Gson();
    private static final Pattern UNSUPPORTED_FORMAT_PATTERN = Pattern.compile("%(\\d+\\$)?[\\d.]*[df]");
    public static final String DEFAULT = "en_us";
    private static volatile LocaleLanguage instance = loadDefault();

    public LocaleLanguage() {}

    private static LocaleLanguage loadDefault() {
        Builder<String, String> builder = ImmutableMap.builder();

        Objects.requireNonNull(builder);
        BiConsumer<String, String> biconsumer = builder::put;

        parseTranslations(biconsumer, "/assets/minecraft/lang/en_us.json");
        final Map<String, String> map = builder.build();

        return new LocaleLanguage() {
            @Override
            public String getOrDefault(String s, String s1) {
                return (String) map.getOrDefault(s, s1);
            }

            @Override
            public boolean has(String s) {
                return map.containsKey(s);
            }

            @Override
            public boolean isDefaultRightToLeft() {
                return false;
            }

            @Override
            public FormattedString getVisualOrder(IChatFormatted ichatformatted) {
                return (formattedstringempty) -> {
                    return ichatformatted.visit((chatmodifier, s) -> {
                        return StringDecomposer.iterateFormatted(s, chatmodifier, formattedstringempty) ? Optional.empty() : IChatFormatted.STOP_ITERATION;
                    }, ChatModifier.EMPTY).isPresent();
                };
            }
        };
    }

    private static void parseTranslations(BiConsumer<String, String> biconsumer, String s) {
        try {
            InputStream inputstream = LocaleLanguage.class.getResourceAsStream(s);

            try {
                loadFromJson(inputstream, biconsumer);
            } catch (Throwable throwable) {
                if (inputstream != null) {
                    try {
                        inputstream.close();
                    } catch (Throwable throwable1) {
                        throwable.addSuppressed(throwable1);
                    }
                }

                throw throwable;
            }

            if (inputstream != null) {
                inputstream.close();
            }
        } catch (JsonParseException | IOException ioexception) {
            LocaleLanguage.LOGGER.error("Couldn't read strings from {}", s, ioexception);
        }

    }

    public static void loadFromJson(InputStream inputstream, BiConsumer<String, String> biconsumer) {
        JsonObject jsonobject = (JsonObject) LocaleLanguage.GSON.fromJson(new InputStreamReader(inputstream, StandardCharsets.UTF_8), JsonObject.class);
        Iterator iterator = jsonobject.entrySet().iterator();

        while (iterator.hasNext()) {
            Entry<String, JsonElement> entry = (Entry) iterator.next();
            String s = LocaleLanguage.UNSUPPORTED_FORMAT_PATTERN.matcher(ChatDeserializer.convertToString((JsonElement) entry.getValue(), (String) entry.getKey())).replaceAll("%$1s");

            biconsumer.accept((String) entry.getKey(), s);
        }

    }

    public static LocaleLanguage getInstance() {
        return LocaleLanguage.instance;
    }

    public static void inject(LocaleLanguage localelanguage) {
        LocaleLanguage.instance = localelanguage;
    }

    public String getOrDefault(String s) {
        return this.getOrDefault(s, s);
    }

    public abstract String getOrDefault(String s, String s1);

    public abstract boolean has(String s);

    public abstract boolean isDefaultRightToLeft();

    public abstract FormattedString getVisualOrder(IChatFormatted ichatformatted);

    public List<FormattedString> getVisualOrder(List<IChatFormatted> list) {
        return (List) list.stream().map(this::getVisualOrder).collect(ImmutableList.toImmutableList());
    }
}
