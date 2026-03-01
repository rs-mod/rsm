package com.ricedotwho.rsm.module.impl.render.visualwords;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import com.ricedotwho.rsm.RSM;
import com.ricedotwho.rsm.data.TerminalType;
import com.ricedotwho.rsm.module.Module;
import com.ricedotwho.rsm.module.api.Category;
import com.ricedotwho.rsm.module.api.ModuleInfo;
import com.ricedotwho.rsm.utils.ChatUtils;
import com.ricedotwho.rsm.utils.FileUtils;
import lombok.Getter;
import lombok.val;
import net.fabricmc.loader.api.Version;
import net.fabricmc.loader.api.VersionParsingException;
import net.fabricmc.loader.impl.util.version.VersionParser;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.*;
import net.minecraft.network.chat.contents.PlainTextContents;
import net.minecraft.resources.RegistryOps;
import net.minecraft.util.FormattedCharSequence;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/// mostly from noammaddons [...](https://github.com/Noamm9/NoammAddons-1.21.10/blob/master/src/main/kotlin/com/github/noamm9/features/impl/dev/TextReplacer.kt)
@Getter
@ModuleInfo(aliases = "Visual Words", id = "VisualWords", category = Category.RENDER)
public class VisualWords extends Module {
    public static ConcurrentHashMap<String, VisualWord> wordMap = new ConcurrentHashMap<>();
    private static final File file = FileUtils.getSaveFileInCategory("render", "visual_words.json");
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

    private static VisualWords INSTANCE;

    public VisualWords() {
        INSTANCE = this;
        load();
    }

    public static void load() {
        if (FileUtils.checkDir(file, new HashSet<>())) {
            try {
                ConcurrentHashMap<String, VisualWord> temp;
                try (InputStreamReader reader = new InputStreamReader(Files.newInputStream(file.toPath()), StandardCharsets.UTF_8)) {
                    temp = GSON.fromJson(reader, new TypeToken<ConcurrentHashMap<String, VisualWord>>(){}.getType());

                    wordMap = temp;
                }
            } catch (IOException | JsonSyntaxException | JsonIOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static void save() {
        try {
            Writer writer = new OutputStreamWriter(Files.newOutputStream(file.toPath()), StandardCharsets.UTF_8);
            GSON.toJson(wordMap, writer);
            writer.close();
        } catch (IOException e) {
            // ignored
        }
    }

    public static void addWord(String phrase, MutableComponent replacement) {
        wordMap.put(phrase, new VisualWord(replacement, true));
        save();
    }

    public static boolean removeWord(String phrase) {
        boolean ret = wordMap.remove(phrase) != null;
        save();
        return ret;
    }

    public static String modifyString(String text) {
        if (!INSTANCE.isEnabled() || text.isBlank() || wordMap.keySet().stream().noneMatch(text::contains)) return text;

        String result = text;

        for (Map.Entry<String, VisualWord> entry : wordMap.entrySet()) {
            if (!entry.getValue().enabled || !result.contains(entry.getKey())) continue;

            result = result.replace(entry.getKey(), applyColorCodes(entry.getValue().replacement.getString()));
        }
        return result;
    }

    public static Component modifyComponent(Component component) {
        if (!INSTANCE.isEnabled() || wordMap.keySet().stream().noneMatch(s -> component.getString().contains(s))) return component;
        return rebuildComponent(component);
    }

    private static MutableComponent rebuildComponent(Component comp) {
        ComponentContents contents = comp.getContents();
        MutableComponent newComp;

        if (contents instanceof PlainTextContents plain) {
            String originalText = plain.text();

            Optional<String> target = wordMap.keySet().stream().filter(originalText::contains).findFirst();

            if (target.isPresent()) {
                newComp = injectReplacement(originalText, target.get(), wordMap.get(target.get()).replacement, comp.getStyle());
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

        boolean containsAny = wordMap.keySet().stream().anyMatch(sb.toString()::contains);

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
