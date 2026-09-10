package rsm.extensions.net.minecraft.world.item.ItemStack;

import manifold.ext.rt.api.Extension;
import manifold.ext.rt.api.This;
import net.minecraft.world.item.ItemStack;

@Extension
public class ItemStackExtension {
  public static void helloWorld(@This ItemStack thiz) {
    System.out.println("hello world!");
  }
}