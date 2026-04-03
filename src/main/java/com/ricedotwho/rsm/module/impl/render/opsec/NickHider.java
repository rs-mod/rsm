package com.ricedotwho.rsm.module.impl.render.opsec;

import com.ricedotwho.rsm.module.SubModule;
import com.ricedotwho.rsm.module.api.SubModuleInfo;
import com.ricedotwho.rsm.ui.clickgui.settings.impl.StringSetting;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.contents.PlainTextContents;
import net.minecraft.util.FormattedCharSequence;

import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;

@Getter
@SubModuleInfo(name = "Nick Hider", alwaysDisabled = false, isEnabled = false)
public class NickHider extends SubModule<OpSec> {
    private static NickHider INSTANCE;

    private final StringSetting fakeName = new StringSetting("Name", ":)", false, false);
//
//    private final NumberSetting fakeLevel = new NumberSetting("Level", 1, 500, 67, 1);
//
//    private static final BooleanSetting test = new BooleanSetting("test", false);

    public NickHider(OpSec opSec) {
        super(opSec);
        INSTANCE = this;
        this.registerProperty(fakeName);
    }

    private static boolean isActive() {
        return INSTANCE != null && INSTANCE.getModule().isEnabled() && INSTANCE.isEnabled();
    }

    private static String getPlayerName() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return null;
        return mc.player.getName().getString();
    }

    public static String modifyString(String text) {
        if (!isActive() || text.isBlank()) return text;
        String playerName = getPlayerName();
        if (playerName == null || playerName.isEmpty() || !text.contains(playerName)) return text;
        return text.replace(playerName, INSTANCE.fakeName.getValue());
    }

    public static Component modifyComponent(Component component) {
        if (!isActive()) return component;
        String playerName = getPlayerName();
        if (playerName == null || !component.getString().contains(playerName)) return component;
        return rebuildComponent(component, playerName);
    }

    public static FormattedCharSequence modifyCharSeq(FormattedCharSequence seq) {
        if (!isActive()) return seq;
        StringBuilder sb = new StringBuilder();
        seq.accept((index, style, codePoint) -> {
            sb.append((char) codePoint);
            return true;
        });

        String playerName = getPlayerName();
        if (playerName == null || !sb.toString().contains(playerName)) return seq;

        MutableComponent rebuilt = Component.literal("");
        AtomicReference<Style> currentStyle = new AtomicReference<>();
        StringBuilder buffer = new StringBuilder();

        seq.accept((index, style, codePoint) -> {
            if (style != currentStyle.get()) {
                if (!buffer.isEmpty()) {
                    rebuilt.append(Component.literal(buffer.toString()).withStyle(currentStyle.get()));
                    buffer.setLength(0);
                }
                currentStyle.set(style);
            }
            buffer.append((char) codePoint);
            return true;
        });

        if (!buffer.isEmpty()) {
            rebuilt.append(Component.literal(buffer.toString()).withStyle(currentStyle.get()));
        }

        return modifyComponent(rebuilt).getVisualOrderText();
    }

    private static MutableComponent rebuildComponent(Component comp, String playerName) {
        ComponentContents contents = comp.getContents();
        MutableComponent newComp;

        if (contents instanceof PlainTextContents plain) {
            String text = plain.text();
            if (text.contains(playerName)) {
                newComp = injectReplacement(text, playerName, INSTANCE.fakeName.getValue(), comp.getStyle());
            } else {
                newComp = comp.copy();
                newComp.getSiblings().clear();
            }
        } else {
            newComp = comp.copy();
            newComp.getSiblings().clear();
        }

        if (newComp.getStyle().isEmpty()) newComp.setStyle(comp.getStyle());

        for (Component sibling : comp.getSiblings()) {
            newComp.append(rebuildComponent(sibling, playerName));
        }

        return newComp;
    }

    private static MutableComponent injectReplacement(String text, String target, String replacement, Style style) {
        MutableComponent root = Component.literal("");
        String[] parts = text.split(Pattern.quote(target), 2);

        if (!parts[0].isEmpty()) root.append(Component.literal(parts[0]).withStyle(style));

        root.append(Component.literal(replacement).withStyle(style));

        if (parts.length > 1 && !parts[1].isEmpty()) {
            String remaining = parts[1];
            if (remaining.contains(target)) {
                root.append(injectReplacement(remaining, target, replacement, style));
            } else {
                root.append(Component.literal(remaining).withStyle(style));
            }
        }

        return root;
    }
}
