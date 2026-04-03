package com.ricedotwho.rsm.module.impl.render.visualwords;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.mojang.serialization.JsonOps;
import com.ricedotwho.rsm.module.Module;
import com.ricedotwho.rsm.module.api.Category;
import com.ricedotwho.rsm.module.api.ModuleInfo;
import com.ricedotwho.rsm.ui.clickgui.settings.impl.SaveSetting;
import lombok.Getter;
import net.minecraft.network.chat.*;
import net.minecraft.network.chat.contents.PlainTextContents;
import net.minecraft.util.FormattedCharSequence;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/// mostly from noammaddons [...](https://github.com/Noamm9/NoammAddons-1.21.10/blob/master/src/main/kotlin/com/github/noamm9/features/impl/dev/TextReplacer.kt)
@Getter
@ModuleInfo(aliases = "Visual Words", id = "VisualWords", category = Category.RENDER)
public class VisualWords extends Module {
    private static final Pattern HEX_PATTERN = Pattern.compile("&#([A-Fa-f0-9]{6})");

    private static final Gson GSON = new GsonBuilder()
            .registerTypeHierarchyAdapter(Component.class, (JsonSerializer<Component>) (component, type, context) -> ComponentSerialization.CODEC
                    .encodeStart(JsonOps.INSTANCE, component)
                    .result()
                    .orElse(JsonNull.INSTANCE))
            .registerTypeHierarchyAdapter(Component.class, (JsonDeserializer<Component>) (json, type, context) -> ComponentSerialization.CODEC
                    .parse(JsonOps.INSTANCE, json)
                    .result()
                    .orElseThrow(() -> new JsonParseException("Invalid component JSON")))
            .setPrettyPrinting()
            .create();

    @Getter
    private static final SaveSetting<ConcurrentHashMap<String, VisualWord>> data = new SaveSetting<>("Word Map", "render", "visual_words.json", ConcurrentHashMap::new, new TypeToken<ConcurrentHashMap<String, VisualWord>>(){}.getType(), GSON, false, null, null);

    private static VisualWords INSTANCE;

    public VisualWords() {
        INSTANCE = this;
        registerProperty(data);
    }

    public static void addWord(String phrase, MutableComponent replacement) {
        data.getValue().put(phrase, new VisualWord(replacement, true));
        data.save();
    }

    public static boolean removeWord(String phrase) {
        boolean ret = data.getValue().remove(phrase) != null;
        data.save();
        return ret;
    }

    public static String modifyString(String text) {
        if (!INSTANCE.isEnabled() || text.isBlank() || data.getValue().keySet().stream().noneMatch(text::contains)) return text;

        String result = text;

        for (Map.Entry<String, VisualWord> entry : data.getValue().entrySet()) {
            if (!entry.getValue().enabled || !result.contains(entry.getKey())) continue;

            result = result.replace(entry.getKey(), applyColorCodes(entry.getValue().replacement.getString()));
        }
        return result;
    }

    public static Component modifyComponent(Component component) {
        if (!INSTANCE.isEnabled() || data.getValue().keySet().stream().noneMatch(s -> component.getString().contains(s))) return component;
        return rebuildComponent(component);
    }

    private static MutableComponent rebuildComponent(Component comp) {
        ComponentContents contents = comp.getContents();
        MutableComponent newComp;

        if (contents instanceof PlainTextContents plain) {
            String originalText = plain.text();

            Optional<String> target = data.getValue().keySet().stream().filter(originalText::contains).findFirst();

            if (target.isPresent()) {
                newComp = injectReplacement(originalText, target.get(), data.getValue().get(target.get()).replacement, comp.getStyle());
            } else {
                newComp = comp.copy();
                newComp.getSiblings().clear();
            }
        }
        else {
            newComp = comp.copy();
            newComp.getSiblings().clear();
        }

        if (newComp.getStyle().isEmpty()) newComp.setStyle(comp.getStyle());

        for (Component sibling : comp.getSiblings()) {
            newComp.append(rebuildComponent(sibling));
        }

        return newComp;
    }

    private static MutableComponent injectReplacement(String text, String target, MutableComponent replacement, Style parentStyle) {
        MutableComponent root = Component.literal("");

        String[] parts = text.split(target, 2);

        if (!parts[0].isEmpty()) {
            root.append(Component.literal(parts[0]).withStyle(parentStyle));
        }

        MutableComponent replacementComp = replacement.copy();

        if (replacementComp.getStyle().isEmpty()) {
            replacementComp = replacementComp.withStyle(parentStyle);
        }

        root.append(replacementComp);

        if (parts.length > 1 && !parts[1].isEmpty()) {
            String remaining = parts[1];

            if (remaining.contains(target)) {
                root.append(injectReplacement(remaining, target, replacement, parentStyle));
            } else {
                root.append(Component.literal(remaining).withStyle(parentStyle));
            }
        }

        return root;
    }

    public static FormattedCharSequence modifyCharSeq(FormattedCharSequence seq) {
        if (!INSTANCE.isEnabled()) return seq;
        StringBuilder sb = new StringBuilder();

        seq.accept((index, style, codePoint) -> {
            sb.append((char) codePoint);
            return true;
        });

        boolean containsAny = data.getValue().keySet().stream().anyMatch(sb.toString()::contains);

        if (!containsAny) {
            return seq;
        }

        MutableComponent rebuilt = Component.literal("");
        AtomicReference<Style> currentStyle = new AtomicReference<>();
        StringBuilder buffer = new StringBuilder();

        seq.accept((index, style, codePoint) -> {
            if (style != currentStyle.get()) {
                if (!buffer.isEmpty()) {
                    rebuilt.append(
                            Component.literal(buffer.toString())
                                    .withStyle(currentStyle.get())
                    );
                    buffer.setLength(0);
                }

                currentStyle.set(style);
            }

            buffer.append((char) codePoint);
            return true;
        });

        if (!buffer.isEmpty()) {
            rebuilt.append(
                    Component.literal(buffer.toString())
                            .withStyle(currentStyle.get())
            );
        }

        return modifyComponent(rebuilt).getVisualOrderText();
    }

    private static String applyColorCodes(String text) {
        Matcher m = HEX_PATTERN.matcher(text);
        StringBuilder sb = new StringBuilder();

        while (m.find()) {
            String hex = m.group(1);

            StringBuilder r = new StringBuilder("§x");
            for (char c : hex.toCharArray()) {
                r.append("§").append(c);
            }

            m.appendReplacement(sb, r.toString());
        }

        m.appendTail(sb);

        return sb.toString().replace("&", "§");
    }
}
