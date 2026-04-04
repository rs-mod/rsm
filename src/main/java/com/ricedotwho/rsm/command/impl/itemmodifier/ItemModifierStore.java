package com.ricedotwho.rsm.command.impl.itemmodifier;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.mojang.authlib.minecraft.client.MinecraftClient;
import com.ricedotwho.rsm.RSM;
import com.ricedotwho.rsm.data.Colour;
import com.ricedotwho.rsm.data.adapter.ColourAdapter;
import com.ricedotwho.rsm.utils.Accessor;
import com.ricedotwho.rsm.utils.FileUtils;
import com.ricedotwho.rsm.utils.ItemUtils;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class ItemModifierStore implements Accessor {
    private static final Gson GSON = new GsonBuilder()
            .registerTypeHierarchyAdapter(Colour.class, new ColourAdapter())
            .setPrettyPrinting().create();
    private static final Type TYPE = new TypeToken<ConcurrentHashMap<String, ItemNameOverride>>() {}.getType();
    private static final File FILE = FileUtils.getSaveFileInCategory("render", "item_modifiers.json");

    private static final ConcurrentHashMap<String, ItemNameOverride> DATA = new ConcurrentHashMap<>();
    private static boolean loaded = false;

    private ItemModifierStore() {
    }

    public static Map<String, ItemNameOverride> getData() {
        ensureLoaded();
        return DATA;
    }

    public static void put(String uuid, String name, Colour colour) {
        ensureLoaded();
        DATA.put(uuid, new ItemNameOverride(name, true, colour));
        save();
    }

    public static boolean remove(String uuid) {
        ensureLoaded();
        boolean removed = DATA.remove(uuid) != null;
        if (removed) {
            save();
        }
        return removed;
    }

    public static Component modifyName(ItemStack stack, Component original) {
        ensureLoaded();

        String uuid = ItemUtils.getUUID(stack);
        if (uuid.isBlank()) {
            return original;
        }

        ItemNameOverride override = DATA.get(uuid);
        if (override == null || !override.enabled || override.name == null || override.name.isBlank()) {
            return original;
        }

        return Component.literal(override.name).withStyle(original.getStyle());
    }

    public static void save() {
        ensureLoaded();
        FileUtils.writeJson(DATA, FILE, GSON);
    }

    public static void load() {
        ensureLoaded();
    }

    private static void ensureLoaded() {
        if (loaded) {
            return;
        }

        loaded = true;
        DATA.clear();

        if (!FILE.exists()) {
            FileUtils.checkDir(FILE, new ConcurrentHashMap<String, ItemNameOverride>());
            return;
        }

        try {
            String json = org.apache.commons.io.FileUtils.readFileToString(FILE, StandardCharsets.UTF_8);
            if (json == null || json.isBlank()) {
                return;
            }

            ConcurrentHashMap<String, ItemNameOverride> parsed = GSON.fromJson(json, TYPE);
            if (parsed != null) {
                DATA.putAll(parsed);
            }
        } catch (IOException e) {
            RSM.getLogger().error("Failed to load item modifier data", e);
        }
    }
}

