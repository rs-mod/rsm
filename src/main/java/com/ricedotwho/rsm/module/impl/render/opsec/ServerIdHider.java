package com.ricedotwho.rsm.module.impl.render.opsec;

import com.ricedotwho.rsm.module.SubModule;
import com.ricedotwho.rsm.module.api.SubModuleInfo;
import lombok.Getter;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.contents.PlainTextContents;
import net.minecraft.util.FormattedCharSequence;

import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;

@Getter
@SubModuleInfo(name = "ServerID Hider", alwaysDisabled = false, isEnabled = false)
public class ServerIdHider extends SubModule<OpSec> {
    private static ServerIdHider INSTANCE;
    private static final Pattern SERVER_ID = Pattern.compile("[Mm]\\d{1,4}[A-Z]{1,4}");

    public ServerIdHider(OpSec opSec) {
        super(opSec);
        INSTANCE = this;
    }

    private static boolean isActive() {
        return INSTANCE != null && INSTANCE.getModule().isEnabled() && INSTANCE.isEnabled();
    }

    public static String modifyString(String text) {
        if (!isActive() || text == null || text.isBlank()) return text;
        return SERVER_ID.matcher(text).replaceAll("");
    }

    public static Component modifyComponent(Component component) {
        if (!isActive()) return component;
        if (!SERVER_ID.matcher(component.getString()).find()) return component;
        return rebuildComponent(component);
    }

    public static FormattedCharSequence modifyCharSeq(FormattedCharSequence seq) {
        if (!isActive()) return seq;
        StringBuilder sb = new StringBuilder();
        seq.accept((index, style, codePoint) -> { sb.append((char) codePoint); return true; });
        if (!SERVER_ID.matcher(sb).find()) return seq;

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

    private static MutableComponent rebuildComponent(Component comp) {
        ComponentContents contents = comp.getContents();
        MutableComponent newComp;

        if (contents instanceof PlainTextContents plain) {
            newComp = Component.literal(SERVER_ID.matcher(plain.text()).replaceAll("")).withStyle(comp.getStyle());
        } else {
            newComp = comp.copy();
            newComp.getSiblings().clear();
        }

        for (Component sibling : comp.getSiblings()) {
            newComp.append(rebuildComponent(sibling));
        }

        return newComp;
    }
}