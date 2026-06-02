package com.ricedotwho.rsm.module.impl.player.chat;

import com.ricedotwho.rsm.event.api.SubscribeEvent;
import com.ricedotwho.rsm.event.impl.player.PrePlayerChatEvent;
import com.ricedotwho.rsm.module.SubModule;
import com.ricedotwho.rsm.module.api.SubModuleInfo;
import com.ricedotwho.rsm.module.impl.player.Chat;
import com.ricedotwho.rsm.module.impl.render.ClickGUI;
import com.ricedotwho.rsm.ui.clickgui.settings.impl.MultiBoolSetting;
import com.ricedotwho.rsm.utils.ChatUtils;
import com.ricedotwho.rsm.utils.StringUtils;
import lombok.Getter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@SubModuleInfo(name = "Chat Emotes", alwaysDisabled = false)
public class ChatEmotes extends SubModule<Chat> {
    // lf font with unicode zz
    private final MultiBoolSetting emotes = new MultiBoolSetting("Emotes", REPLACEMENTS.keySet().stream().toList(), List.of());
    public static final Map<String, String> REPLACEMENTS = new HashMap<>();
    static {
        REPLACEMENTS.put(":heart:", "❤");
        REPLACEMENTS.put("o/", "( ﾟ◡ﾟ)/");
        REPLACEMENTS.put(":star:", "✮");
        REPLACEMENTS.put(":yes:", "✔");
        REPLACEMENTS.put(":no:", "✖");
        REPLACEMENTS.put(":java:", "☕");
        REPLACEMENTS.put(":arrow:", "➜");
        REPLACEMENTS.put(":shrug:", "¯\\_(ツ)_/¯");
        REPLACEMENTS.put(":tableflip:", "(╯°□°）╯︵ ┻━┻");
        REPLACEMENTS.put(":totem:", "☉_☉");
        REPLACEMENTS.put(":typing:", "✎...");
        REPLACEMENTS.put(":maths:", "√(π+x)=L");
        REPLACEMENTS.put(":snail:", "@'-'");
        REPLACEMENTS.put(":thinking:", "(0.o?)");
        REPLACEMENTS.put(":gimme:", "༼つ◕_◕༽つ");
        REPLACEMENTS.put(":wizard:", "(' - ')⊃━☆ﾟ.*･｡ﾟ");
        REPLACEMENTS.put(":pvp:", "⚔");
        REPLACEMENTS.put(":peace:", "✌");
        REPLACEMENTS.put(":puffer:", "<('O')>");
        REPLACEMENTS.put("h/", "ヽ(^◇^*)/");
        REPLACEMENTS.put(":sloth:", "(・⊝・)");
        REPLACEMENTS.put(":dog:", "(ᵔᴥᵔ)");
        REPLACEMENTS.put(":dj:", "ヽ(⌐■_■)ノ♬");
        REPLACEMENTS.put(":yey:", "ヽ (◕◡◕) ﾉ");
        REPLACEMENTS.put(":snow:", "☃");
        REPLACEMENTS.put(":dab:", "<o/");
        REPLACEMENTS.put(":cat:", "= ＾● ⋏ ●＾ =");
        REPLACEMENTS.put(":cute:", "(✿◠‿◠)");
        REPLACEMENTS.put(":skull:", "☠");
    }

    public ChatEmotes(Chat chat) {
        super(chat);
        this.registerProperty(emotes);
	}

    @SubscribeEvent
    public void onPreChatSend(PrePlayerChatEvent event) {
        String original = event.getMessage();
        if (original.startsWith(ClickGUI.getCommandPrefix().getValue())
                || event.isCommand() && !StringUtils.startsWithAny(original, "pc", "ac", "gc", "oc", "w", "msg", "t", "cc", "r")) return;
        String changed = replace(original);
        event.setMessage(changed);
    }

    private String replace(String input) {
        for (Map.Entry<String, String> entry : REPLACEMENTS.entrySet()) {
            if (!emotes.get(entry.getKey())) continue;
            input = input.replace(entry.getKey(), entry.getValue());
        }
        return input;
    }
}
