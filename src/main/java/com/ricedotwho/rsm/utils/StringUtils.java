package com.ricedotwho.rsm.utils;

import lombok.experimental.UtilityClass;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@UtilityClass
public class StringUtils {
    public String replaceText(String input, String target, String replacement, boolean caseSensitive) {
        if (caseSensitive) {
            return input.replace(target, replacement);
        } else {
            Pattern pattern = Pattern.compile(Pattern.quote(target), Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(input);
            StringBuilder result = new StringBuilder();

            while (matcher.find()) {
                matcher.appendReplacement(result, Matcher.quoteReplacement(replacement));
            }
            matcher.appendTail(result);
            return result.toString();
        }
    }

    public String format(String text) {
        return text.replace("&&", "§");
    }
}
