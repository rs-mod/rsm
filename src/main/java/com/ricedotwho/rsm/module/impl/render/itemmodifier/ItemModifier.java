package com.ricedotwho.rsm.module.impl.render.itemmodifier;

import com.google.common.reflect.TypeToken;
import com.google.gson.GsonBuilder;
import com.ricedotwho.rsm.RSM;
import com.ricedotwho.rsm.component.impl.Scheduler;
import com.ricedotwho.rsm.data.Colour;
import com.ricedotwho.rsm.data.adapter.ColourAdapter;
import com.ricedotwho.rsm.event.impl.game.ClientTickEvent;
import com.ricedotwho.rsm.module.Module;
import com.ricedotwho.rsm.module.api.Category;
import com.ricedotwho.rsm.module.api.ModuleInfo;
import com.ricedotwho.rsm.ui.clickgui.settings.impl.ButtonSetting;
import com.ricedotwho.rsm.ui.clickgui.settings.impl.SaveSetting;
import com.ricedotwho.rsm.ui.itemmodifier.ItemModifierGui;
import com.ricedotwho.rsm.utils.ItemUtils;
import lombok.Getter;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Getter
@ModuleInfo(aliases = "Item Modifier", id = "ItemModifier", category = Category.RENDER)
public class ItemModifier extends Module {

    private final ButtonSetting open = new ButtonSetting("Open Editor", "Open", () -> {
        assert mc.player != null;
        mc.player.closeContainer();
        Scheduler.schedule(ClientTickEvent.Start.class, ItemModifierGui::open);
    });

    private static final SaveSetting<Map<String, ItemOverride>> data = new SaveSetting<>(
            "Data", "render", "item_modifier.json",
            ConcurrentHashMap::new, new TypeToken<Map<String, ItemOverride>>() {}.getType(),
            new GsonBuilder()
            .registerTypeHierarchyAdapter(Colour.class, new ColourAdapter())
            .setPrettyPrinting().create(), false, null, null);

    public ItemModifier() {
        this.registerProperty(
                open,
                data
        );
    }

    public static void put(String uuid, String name, Colour colour) {
        data.getValue().put(uuid, new ItemOverride(name, true, colour));
        save();
    }

    public static boolean remove(String uuid) {
        boolean removed = data.getValue().remove(uuid) != null;
        if (removed) {
            save();
        }
        return removed;
    }

    public static Component modifyName(ItemStack stack, Component original) {
        String uuid = ItemUtils.getUUID(stack);
        if (uuid.isBlank() || !RSM.getModule(ItemModifier.class).isEnabled()) {
            return null;
        }

        ItemOverride override = data.getValue().get(uuid);
        if (override == null || !override.enabled || override.name == null || override.name.isBlank()) {
            return null;
        }

        return Component.literal(override.name).withStyle(original.getStyle());
    }

    public static void save() {
        data.save();
    }

    public static void load() {
        data.load();
    }

    public static Map<String, ItemOverride> getData() {
        return data.getValue();
    }
}
