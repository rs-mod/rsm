package com.ricedotwho.rsm.utils;

import com.ricedotwho.rsm.data.Pair;
import lombok.NonNull;
import lombok.experimental.UtilityClass;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.ItemLore;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@UtilityClass
public class ItemUtils {
    private final String UUID_KEY = "uuid";
    private final String ID_KEY = "id";

    private static final Pattern STRENGTH_PATTERN = Pattern.compile("^Strength: \\+(\\d+).*?");
    private static final Pattern DB_CHARGE_PATTERN = Pattern.compile("Charges: (\\d+)/(\\d+)⸕");

    public CompoundTag getCustomData(@NonNull ItemStack item) {
        return item.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
    }

    public String getID(@NonNull ItemStack item) {
        return getCustomData(item).getString(ID_KEY).orElse("");
    }

    public String getUUID(@NonNull ItemStack item) {
        return getCustomData(item).getString(UUID_KEY).orElse("");
    }

    public List<Component> getLore(@NonNull ItemStack item) {
        ItemLore lore = item.get(DataComponents.LORE);
        if (lore == null) return new ArrayList<>();
        return lore.styledLines();
    }

    public List<String> getCleanLore(@NonNull ItemStack item) {
        ItemLore lore = item.get(DataComponents.LORE);
        if (lore == null) return new ArrayList<>();
        return lore.styledLines().stream().map(Component::getString).toList();
    }

    public int getSbStrength(@NonNull ItemStack item) {
        for (String s : getCleanLore(item)) {
            Matcher m = STRENGTH_PATTERN.matcher(s);
            if(m.find()) {
                String strength = m.group(1);
                try {
                    return Integer.parseInt(strength);
                } catch (NumberFormatException e) {
                    return 0;
                }
            }
        }
        return 0;
    }

    public Pair<Integer, Integer> getDbCharges(@NonNull ItemStack item) {
        for (String s : getCleanLore(item)) {
            Matcher m = DB_CHARGE_PATTERN.matcher(s);
            if(m.find()) {
                String charges = m.group(1);
                String max = m.group(2);
                try {
                    return new Pair<>(Integer.parseInt(charges), Integer.parseInt(max));
                } catch (NumberFormatException e) {
                    return new Pair<>(0, 0);
                }
            }
        }
        return new Pair<>(0, 0);
    }

    public int getTunerDistance(ItemStack item) {
        return getCustomData(item).getIntOr("tuned_transmission", 0);
    }

    public boolean isEtherwarp(ItemStack item) {
        return getCustomData(item).getIntOr("ethermerge", 0) == 1 || "ETHERWARP_CONDUIT".equals(getID(item));
    }

    public boolean isAbilityItem(ItemStack item) {
        return getCleanLore(item).stream().anyMatch(s -> s.contains("Ability:") && s.endsWith("RIGHT CLICK"));
    }

    public List<Tag> getAbilityScrollsTagList(ItemStack item) {
        return ItemUtils.getCustomData(item).getListOrEmpty("ability_scroll").stream().toList();
    }

    public List<String> getAbilityScrollsList(ItemStack item) {
        return ItemUtils.getCustomData(item).getListOrEmpty("ability_scroll").stream().filter(t -> t.asString().isPresent()).map(t -> t.asString().get()).toList();
    }
}
