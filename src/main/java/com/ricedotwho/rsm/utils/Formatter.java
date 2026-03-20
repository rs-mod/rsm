package com.ricedotwho.rsm.utils;

import lombok.experimental.UtilityClass;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@UtilityClass
public class Formatter {
    private final Pattern PATTERN = Pattern.compile("\\{(\\w+)}");

    public String format(String in, Map<Object, ?> values) {
        Matcher matcher = PATTERN.matcher(in);
        StringBuilder sb = new StringBuilder();
        while (matcher.find()) {
            String key = matcher.group(1);
            Object value = values.containsKey(key) ? values.get(key) : matcher.group(0);
            matcher.appendReplacement(sb, Matcher.quoteReplacement(String.valueOf(value)));
        }
        matcher.appendTail(sb);
        return sb.toString();
    }
}
