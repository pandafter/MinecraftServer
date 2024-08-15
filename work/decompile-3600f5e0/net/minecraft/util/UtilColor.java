package net.minecraft.util;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Nullable;
import org.apache.commons.lang3.StringUtils;

public class UtilColor {

    private static final Pattern STRIP_COLOR_PATTERN = Pattern.compile("(?i)\\u00A7[0-9A-FK-OR]");
    private static final Pattern LINE_PATTERN = Pattern.compile("\\r\\n|\\v");
    private static final Pattern LINE_END_PATTERN = Pattern.compile("(?:\\r\\n|\\v)$");

    public UtilColor() {}

    public static String formatTickDuration(int i, float f) {
        int j = MathHelper.floor((float) i / f);
        int k = j / 60;

        j %= 60;
        int l = k / 60;

        k %= 60;
        return l > 0 ? String.format(Locale.ROOT, "%02d:%02d:%02d", l, k, j) : String.format(Locale.ROOT, "%02d:%02d", k, j);
    }

    public static String stripColor(String s) {
        return UtilColor.STRIP_COLOR_PATTERN.matcher(s).replaceAll("");
    }

    public static boolean isNullOrEmpty(@Nullable String s) {
        return StringUtils.isEmpty(s);
    }

    public static String truncateStringIfNecessary(String s, int i, boolean flag) {
        if (s.length() <= i) {
            return s;
        } else if (flag && i > 3) {
            String s1 = s.substring(0, i - 3);

            return s1 + "...";
        } else {
            return s.substring(0, i);
        }
    }

    public static int lineCount(String s) {
        if (s.isEmpty()) {
            return 0;
        } else {
            Matcher matcher = UtilColor.LINE_PATTERN.matcher(s);

            int i;

            for (i = 1; matcher.find(); ++i) {
                ;
            }

            return i;
        }
    }

    public static boolean endsWithNewLine(String s) {
        return UtilColor.LINE_END_PATTERN.matcher(s).find();
    }

    public static String trimChatMessage(String s) {
        return truncateStringIfNecessary(s, 256, false);
    }

    public static boolean isAllowedChatCharacter(char c0) {
        return c0 != 167 && c0 >= ' ' && c0 != 127;
    }

    public static boolean isValidPlayerName(String s) {
        return s.length() > 16 ? false : s.chars().filter((i) -> {
            return i <= 32 || i >= 127;
        }).findAny().isEmpty();
    }

    public static String filterText(String s) {
        return filterText(s, false);
    }

    public static String filterText(String s, boolean flag) {
        StringBuilder stringbuilder = new StringBuilder();
        char[] achar = s.toCharArray();
        int i = achar.length;

        for (int j = 0; j < i; ++j) {
            char c0 = achar[j];

            if (isAllowedChatCharacter(c0)) {
                stringbuilder.append(c0);
            } else if (flag && c0 == '\n') {
                stringbuilder.append(c0);
            }
        }

        return stringbuilder.toString();
    }

    public static boolean isWhitespace(int i) {
        return Character.isWhitespace(i) || Character.isSpaceChar(i);
    }

    public static boolean isBlank(@Nullable String s) {
        return s != null && s.length() != 0 ? s.chars().allMatch(UtilColor::isWhitespace) : true;
    }
}
