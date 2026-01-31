package com.ricedotwho.rsm.utils;

import lombok.experimental.UtilityClass;
import net.minecraft.ChatFormatting;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@UtilityClass
public class NumberUtils {
    private final Map<String, String[]> toSuffixes = new HashMap<>();
    private final Map<String, Double> fromSuffixes = new HashMap<>();
    private final Pattern COMPACT_PATTERN = Pattern.compile("^(\\d+(?:\\.\\d+)?)([kmb])?$", Pattern.CASE_INSENSITIVE);

    static {
        toSuffixes.put("en", new String[]{"", "K", "M", "B", "T"});
        //todo: add other locales

        fromSuffixes.put("K", 1_000.0);
        fromSuffixes.put("M", 1_000_000.0);
        fromSuffixes.put("B", 1_000_000_000.0);
        fromSuffixes.put("T", 1_000_000_000_000.0);
    }


    public boolean isFloat(String input) {
        if (input == null || input.isEmpty()) {
            return false;
        }

        try {
            Float.parseFloat(input);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public boolean isInteger(String str) {
        return str.matches("-?\\d+");
    }

    public boolean isDouble(String input) {
        if (input == null || input.isEmpty()) {
            return false;
        }

        try {
            Double.parseDouble(input);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public String millisToHHMMSS(long millis) {
        long totalSeconds = millis / 1000;
        long hours = totalSeconds / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;

        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    public String millisToSSMS(long millis) {
        long seconds = millis / 1000;
        long milliseconds = millis % 1000;

        return String.format("%02d.%03d", seconds, milliseconds);
    }

    public String millisToSMS(long millis) {
        long seconds = millis / 1000;
        long milliseconds = millis % 1000;

        return String.format("%d.%03d",  seconds, milliseconds);
    }

    public String formatCompact(long number, Locale locale) {
        String[] localeSuffixes = toSuffixes.getOrDefault(locale.getLanguage(), toSuffixes.get("en"));
        int suffixIndex = 0;
        double value = number;

        while (value >= 1000 && suffixIndex < localeSuffixes.length - 1) {
            value /= 1000;
            suffixIndex++;
        }

        DecimalFormatSymbols symbols = new DecimalFormatSymbols(locale);
        DecimalFormat decimalFormat = new DecimalFormat("#.#", symbols);
        return decimalFormat.format(value) + localeSuffixes[suffixIndex];
    }
    public String formatCompact(long number) {
        return formatCompact(number, Locale.ROOT);
    }

    public boolean isCompactNumber(String input) {
        if (input == null) {
            return false;
        }
        return COMPACT_PATTERN.matcher(input.trim()).matches();
    }

    public double parseCompact(String compactNumber) {
        Matcher matcher = COMPACT_PATTERN.matcher(compactNumber.trim());

        if (!matcher.matches()) {
            System.out.println("Invalid compact number: " + compactNumber);
            ChatUtils.chat(ChatFormatting.RED + "Invalid compact number: " + compactNumber);
            return 0;
        }

        double number = Double.parseDouble(matcher.group(1));
        String suffix = matcher.group(2);

        double factor = suffix == null
                ? 1.0
                : fromSuffixes.get(suffix.toUpperCase(Locale.ROOT));

        return number * factor;
    }

    public static int convertRomanToArabic(String roman) {
        if (roman == null) return -1;
        int number = romanCharToArabic(roman.charAt(0));

        for (int i = 1; i < roman.length(); i++) {
            int current = romanCharToArabic(roman.charAt(i));
            int previous = romanCharToArabic(roman.charAt(i - 1));
            if (current <= previous) {
                number += current;
            } else {
                number = number - previous * 2 + current;
            }
        }
        return number;
    }

    private static int romanCharToArabic(char c) {
        return switch (c) {
            case 'I' -> 1;
            case 'V' -> 5;
            case 'X' -> 10;
            case 'L' -> 50;
            case 'C' -> 100;
            case 'D' -> 500;
            case 'M' -> 1000;
            default -> -1;
        };
    }

    public static String convertArabicToRoman(int number) {
        if (number == 0) {
            return "0";
        }
        String romanOnes = arabicToRomanChars(number % 10, "I", "V", "X");
        number /= 10;

        String romanTens = arabicToRomanChars(number % 10, "X", "L", "C");
        number /= 10;

        String romanHundreds = arabicToRomanChars(number % 10, "C", "D", "M");
        number /= 10;

        String romanThousands = arabicToRomanChars(number % 10, "M", "", "");

        return romanThousands + romanHundreds + romanTens + romanOnes;
    }

    private static String arabicToRomanChars(int n, String one, String five, String ten) {
        return switch (n) {
            case 1 -> one;
            case 2 -> one + one;
            case 3 -> one + one + one;
            case 4 -> one + five;
            case 5 -> five;
            case 6 -> five + one;
            case 7 -> five + one + one;
            case 8 -> five + one + one + one;
            case 9 -> one + ten;
            default -> "";
        };
    }
}
