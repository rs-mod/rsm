package rsm.extensions.java.lang.String;

import manifold.ext.rt.api.Extension;
import manifold.ext.rt.api.This;
import net.minecraft.ChatFormatting;

import java.lang.String;

@Extension
public abstract class StringExtension {
  public static String stripFormatting(@This String string) {
    return ChatFormatting.stripFormatting(string);
  }
}