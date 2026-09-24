package com.ricedotwho.rsm.module.impl.player;

import com.ricedotwho.rsm.core.RSM;
import com.ricedotwho.rsm.core.UniversalSettings;
import com.ricedotwho.rsm.module.api.Category;
import com.ricedotwho.rsm.module.api.Module;
import com.ricedotwho.rsm.module.api.ModuleInfo;
import com.ricedotwho.rsm.module.api.settings.impl.EnumSetSetting;
import com.ricedotwho.rsm.module.api.settings.impl.NumberSetting;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.minecraft.network.chat.Component;

import java.util.List;

@ModuleInfo(aliases = "Action Bar", id = "action-bar", category = Category.OTHER)
public class ActionBar extends Module {
    @SuppressWarnings("unused")
    private static final ActionBar instance = new ActionBar();
    private static final String SEPARATOR = " {2,}";

    private final EnumSetSetting<Element> options = new EnumSetSetting<>("Elements", Element.class, List.of());
    private final NumberSetting<Integer> gap = new NumberSetting<>("Gap", 0, 10, 5, 1);

    public ActionBar() {
        ClientReceiveMessageEvents.MODIFY_GAME.register((message, overlay) -> {
            if (overlay) {
                return modify(message);
            }
            return message;
        });
    }

    private Component modify(Component component) {
        if (options.getValue().isEmpty() || !instance.isEnabled()) return component;
        String[] parts = component.getString().split(SEPARATOR);
        StringBuilder c = new StringBuilder();

        var first = true;
        for (var part : parts) {
            var element = Element.find(part);
            if (element == null && UniversalSettings.getDevInfo().getValue()) RSM.getLogger().info("Unknown element {}", part);
            if (element == null || !options.contains(element)) {
                if (!first) {
                    c.repeat(" ", gap.getValue());
                }
                c.append(part);
                first = false;
            }
        }

        return Component.literal(c.toString());
    }

    private enum Element {
        HEALTH("\uE010", "▅", "▃", "▂", "▁"),
        DEFENSE("\uE008", "\uE008 Defense"),
        VITALITY("\uE028"),
        MANA("\uE003", "\uE017", " Mana"),
        TRUE_DEFENSE("❈ True Defense"),
        SECRETS(" Secrets"),
        TERM_LASER("T1", "T2", "T3!"),
        DRILL_FUEL(" Drill Fuel"),
        ESSENCE_GAIN(" Essence"),
        BITS(" Bits from Cookie Buff!");

        private final String[] end;
        Element(String ... end) {
            this.end = end;
        }

        public static Element find(String part) {
            for (var element : values()) {
                for (String e : element.end) {
                    if (part.endsWith(e)) return element;
                }
            }
            return null;
        }
    }
}
