package rsm.extensions.java.lang.String;

import manifold.ext.rt.api.Extension;
import manifold.ext.rt.api.This;
import net.minecraft.ChatFormatting;

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
      return Integer.parseInt(thiz);
    } catch (NumberFormatException e) {
        throw new RuntimeException(e);
    }
  }
}
