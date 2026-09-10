package rsm.extensions.java.lang.String;

import manifold.ext.rt.api.ComparableUsing;
import manifold.ext.rt.api.Extension;
import manifold.ext.rt.api.This;
import net.minecraft.ChatFormatting;

import java.lang.String;

@Extension
public abstract class StringExtension implements ComparableUsing<String> {
  public static String stripFormatting(@This String string) {
    return ChatFormatting.stripFormatting(string);
  }

  @Override
  public EqualityMode equalityMode() { return EqualityMode.Equals; }
}