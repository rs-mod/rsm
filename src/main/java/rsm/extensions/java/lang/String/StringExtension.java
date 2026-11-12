package rsm.extensions.java.lang.String;

import com.ricedotwho.rsm.utils.StringUtils;
import manifold.ext.rt.api.Extension;
import manifold.ext.rt.api.This;
import net.minecraft.ChatFormatting;

import java.util.Collection;

@Extension
public abstract class StringExtension {
  public static String stripFormatting(@This String string) {
    return ChatFormatting.stripFormatting(string);
  }

  public static String capitalise(@This String input) {
    StringBuilder sb = new StringBuilder();
    String[] words = input.split(" ");
    for (String word : words) {
      if (word.isEmpty()) continue;
      String first = word.substring(0, 1);
      String rest = word.substring(1);
      sb.append(first.toUpperCase()).append(rest).append(" ");
    }
    return sb.toString().trim();
  }

  public static int toInt(@This String thiz) {
    try {
      return Integer.parseInt(thiz.trim());
    } catch (NumberFormatException e) {
        throw new RuntimeException(e);
    }
  }

  public static boolean startsWith(@This String thiz, String ... b) {
    return StringUtils.startsWithAny(thiz, b);
  }

  public static boolean startsWith(@This String thiz, Collection<String> b) {
    return StringUtils.startsWithAny(thiz, b);
  }

  public static boolean contains(@This String thiz, String ... b) {
    return StringUtils.containsAny(thiz, b);
  }

  public static boolean contains(@This String thiz, Collection<String> b) {
    return StringUtils.containsAny(thiz, b);
  }

  public static boolean endsWith(@This String thiz, String ... b) {
    return StringUtils.endsWithAny(thiz, b);
  }

  public static boolean endsWith(@This String thiz, Collection<String> b) {
    return StringUtils.endsWithAny(thiz, b);
  }
}
