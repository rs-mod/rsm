package com.ricedotwho.rsm.module.impl.player;

import com.google.common.reflect.TypeToken;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonSerializer;
import com.ricedotwho.rsm.component.impl.task.TaskComponent;
import com.ricedotwho.rsm.event.api.SubscribeEvent;
import com.ricedotwho.rsm.event.impl.game.ChatEvent;
import com.ricedotwho.rsm.module.Module;
import com.ricedotwho.rsm.module.api.Category;
import com.ricedotwho.rsm.module.api.ModuleInfo;
import com.ricedotwho.rsm.module.impl.player.chat.ChatEmotes;
import com.ricedotwho.rsm.module.impl.player.chat.HiddenMessage;
import com.ricedotwho.rsm.ui.chathider.ChatHiderGui;
import com.ricedotwho.rsm.ui.clickgui.settings.group.GroupSetting;
import com.ricedotwho.rsm.ui.clickgui.settings.impl.ButtonSetting;
import com.ricedotwho.rsm.ui.clickgui.settings.impl.SaveSetting;
import lombok.Getter;
import net.minecraft.ChatFormatting;

import java.util.ArrayList;
import java.util.List;

@Getter
@ModuleInfo(aliases = "Chat", id = "Chat", category = Category.PLAYER)
public class Chat extends Module {
    private final GroupSetting<ChatEmotes> chatEmotes = new GroupSetting<>("Chat Emotes", new ChatEmotes(this));

    private final ButtonSetting openChatHider = new ButtonSetting("Open Chat Hider", "Open", () -> {
        assert mc.player != null;
        mc.player.closeContainer();
        TaskComponent.onTick(0, ChatHiderGui::open);
    });

    private final ButtonSetting addDefault = new ButtonSetting("Add Default", "Add Default", () -> {
        DEFAULT_HIDDEN.forEach(s -> hiddenMessages.getValue().add(new HiddenMessage(true, s)));
        save();
    });

    private final ButtonSetting clearHiddenMessages = new ButtonSetting("Clear", "Clear", () -> {
        hiddenMessages.getValue().clear();
        save();
    });

    @Getter
    private static final SaveSetting<List<HiddenMessage>> hiddenMessages = new SaveSetting<>("Hidden Messages", "player", "hidden_messages.json", ArrayList::new,
            new TypeToken<List<HiddenMessage>>() {}.getType(),
            new GsonBuilder()
                    .registerTypeHierarchyAdapter(HiddenMessage.class, (JsonDeserializer<HiddenMessage>) (json, typeOfT, context) -> new HiddenMessage(json.getAsJsonObject()))
                    .registerTypeHierarchyAdapter(HiddenMessage.class, (JsonSerializer<HiddenMessage>) (src, typeOfT, context) -> src.serialize())
                    .setPrettyPrinting().create(),
            false, null, null);

    private static final List<String> DEFAULT_HIDDEN = List.of(
            // kc
            "^\\+[0-9]{1,2} Kill Combo",
            "^Your Kill Combo has expired! You reached a [0-9]{1,2} Kill Combo!$",

            // zzz
            "^There are blocks in the way!$",
            "^You earned \\d{1,10} GEXP$",

            // misc
            "^Your bone plating reduced the damage you took by [0-9,.]!$",
            "^[\\w ]{1,16} is ready to use! Press DROP to activate it!$",
            "^\\[CROWD] [a-zA-Z ]{0,16}: [a-zA-Z !?,.]{0,64}$",

            // stash
            "You have [0-9,]{1,6} items stashed away!",
            "You have [0-9,]{1,6} materials stashed away!",
            "This totals [0-9,]{1,6} types of materials stashed!",
            "You have [0-9,]{1,4} items stashed away!",
            ">>> CLICK HERE to pick them up! <<<",

            // blessings
            "^DUNGEON BUFF! \\w{3,16} found a Blessing of [A-Za-z]{1,6} [A-Za-z0-9sm! ()]{2,19}$",
            "^DUNGEON BUFF! A Blessing of [a-zA-Z]{1,16} [a-zA-Z]{1,3} was found![0-9sm! ()]{1,16}$",
            "^A Blessing of \\w{3,16} was picked up!$",
            "^ {5}Granted you +",
            "^ {5}Also granted you +",

            "^RIGHT CLICK on [a-zA-Z ]{1,16} to open it. This key can only be used to open 1 door!$",

            // essence
            "^ESSENCE! \\w{3,16} found x[0-9]{1,2} [a-zA-Z]{1,16} Essence!$",
            "^\\w{3,16} found a Wither Essence! Everyone gains an extra essence!$",

            // empty
            "^ *$",

            // class buff
            "^\\[\\w{3,16}] [a-zA-Z ]{1,50}[0-9%,.]{1,8} -> [0-9%,.]{1,8}$",
            "^Your \\w{1,16} stats are doubled because you are the only player using this class!$",

            // blazetek
            "^Your radio signal is strong!$",
            "^Your radio is weak. Find another enjoyer to boost it.$",

            "^◕ \\w{3,16} picked up your \\w{3,16} Orb!$",
            "^◕ You picked up a [\\w ]{3,16} Orb from \\w{3,16} healing you for",

            "^The \\w{1,16} Trap hit you for [0-9,.]{1,16} damage!$",
            "^The Flamethrower hit you for [0-9,.]{1,16} damage!$",
            "^A Crypt Wither Skull exploded, hitting you for [0-9,.]{1,16} damage.$"
    );

    public Chat() {
        this.registerProperty(
                openChatHider,
                addDefault,
                clearHiddenMessages,
                hiddenMessages,
                chatEmotes
        );
    }

    @SubscribeEvent
    public void onShowChat(ChatEvent.Show event) {
        if (!event.isOverlay() && checkMessage(ChatFormatting.stripFormatting(event.getMessage().getString()))) {
            event.setCancelled(true);
        }
    }

    private boolean checkMessage(String message) {
        return hiddenMessages.getValue().stream().anyMatch(msg -> msg.check(message));
    }

    public static void add(HiddenMessage hiddenMessage) {
        hiddenMessages.getValue().add(hiddenMessage);
        save();
    }

    public static void save() {
        hiddenMessages.save();
    }
}
