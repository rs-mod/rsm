package com.ricedotwho.rsm.module.impl.player;

import com.google.common.reflect.TypeToken;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonSerializer;
import com.ricedotwho.rsm.core.RSM;
import com.ricedotwho.rsm.event.api.Scheduler;
import com.ricedotwho.rsm.event.api.SubscribeEvent;
import com.ricedotwho.rsm.event.impl.game.ChatEvent;
import com.ricedotwho.rsm.event.impl.game.TickEvent;
import com.ricedotwho.rsm.managers.notification.NotificationManager;
import com.ricedotwho.rsm.managers.notification.NotificationType;
import com.ricedotwho.rsm.module.api.Category;
import com.ricedotwho.rsm.module.api.Module;
import com.ricedotwho.rsm.module.api.ModuleInfo;
import com.ricedotwho.rsm.module.api.settings.group.DefaultGroupSetting;
import com.ricedotwho.rsm.module.api.settings.group.GroupSetting;
import com.ricedotwho.rsm.module.api.settings.impl.BooleanSetting;
import com.ricedotwho.rsm.module.api.settings.impl.ButtonSetting;
import com.ricedotwho.rsm.module.api.settings.impl.SaveSetting;
import com.ricedotwho.rsm.module.impl.player.chat.ChatEmotes;
import com.ricedotwho.rsm.module.impl.player.chat.HiddenMessage;
import com.ricedotwho.rsm.ui.old.chathider.ChatHiderGui;
import lombok.Getter;
import lombok.Setter;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.multiplayer.chat.GuiMessage;

import java.awt.*;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;

@Getter
@ModuleInfo(aliases = "Chat", id = "Chat", category = Category.PLAYER)
public class Chat extends Module {
    @Getter
    private static final Chat instance = new Chat();

    @Getter
    private static final IdentityHashMap<GuiMessage.Line, GuiMessage> lineCache = new IdentityHashMap<>();

    @Getter
    private static GuiMessage lastHovered = null;

    private final GroupSetting<ChatEmotes> chatEmotes = new GroupSetting<>("Chat Emotes", new ChatEmotes(this));
    private final BooleanSetting dontClearHistory = new BooleanSetting("Don't Clear History", false);
    private final BooleanSetting stopAutoScroll = new BooleanSetting("Disable auto scroll", false);
    private final BooleanSetting copyChat = new BooleanSetting("Copy Chat", false);
    private final BooleanSetting hidePing = new BooleanSetting("Hide ping in tablist",false);

    private final ButtonSetting openChatHider = new ButtonSetting("Open Chat Hider", "Open", () -> {
        assert mc.player != null;
        mc.player.closeContainer();
        Scheduler.schedule(TickEvent.ClientStart.class, ChatHiderGui::open);
    });

    private final ButtonSetting addDefault = new ButtonSetting("Add Default", "Add Default", () -> {
        DEFAULT_HIDDEN.forEach(s -> instance.hiddenMessages.getValue().add(new HiddenMessage(true, s)));
        save();
    });

    private final ButtonSetting clearHiddenMessages = new ButtonSetting("Clear", "Clear", () -> {
        instance.hiddenMessages.getValue().clear();
        save();
    });

    @Getter
    private final SaveSetting<List<HiddenMessage>> hiddenMessages = new SaveSetting<>("Hidden Messages", "player", "hidden_messages.json", ArrayList::new,
            new TypeToken<List<HiddenMessage>>() {}.getType(),
            new GsonBuilder()
                    .registerTypeHierarchyAdapter(HiddenMessage.class, (JsonDeserializer<HiddenMessage>) (json, _, _) -> new HiddenMessage(json.getAsJsonObject()))
                    .registerTypeHierarchyAdapter(HiddenMessage.class, (JsonSerializer<HiddenMessage>) (src, _, _) -> src.serialize())
                    .setPrettyPrinting().create(),
            false, null, null);

    private static final List<String> DEFAULT_HIDDEN = List.of(
            // kc
            "^\\+[0-9]{1,2} Kill Combo",
            "^Your Kill Combo has expired! You reached a [0-9]{1,2} Kill Combo!$",

            // zzz
            "^There are blocks in the way!$",
            "^You earned \\d{1,10} GEXP$",
            "^You earned \\d{1,10} GEXP from playing \\w{1,16}!$",

            // misc
            "^.* is now available!$",
            "^Your bone plating reduced the damage you took by [0-9,.]!$",
            "^[\\w ]{1,16} is ready to use! Press DROP to activate it!$",
            "^\\[CROWD] [a-zA-Z ]{0,16}: [a-zA-Z !?,.]{0,64}$",
            "^You cannot hit the silverfish while it's moving!$",
            "^A mystical force prevents you digging in this room!$",
            "^A mystical force prevents you from digging that block!$",
            "^This chest has already been searched!$",
            "^You do not have the key for this door!$",
            "^This lever has already been used.$",
            "^A mystical force prevents you digging there!$",

            // stash
            "You have [0-9,]{1,6} items stashed away!",
            "You have [0-9,]{1,6} materials stashed away!",
            "This totals [0-9,]{1,6} types of materials stashed!",
            "You have [0-9,]{1,4} items stashed away!",
            ">>> CLICK HERE to pick them up! <<<",
            "^ {15}\\(This totals \\d* types? of materials? stashed!\\)$",

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
        ScreenEvents.BEFORE_INIT.register((_, screen, _, _) -> {
            ScreenMouseEvents.allowMouseClick(screen).register((_, event) -> {
                onChatClick(event, screen);
                return true;
            });
        });
    }

    private void onChatClick(MouseButtonEvent event, Screen screen) {
        if (!instance.getCopyChat().getValue() || event.buttonInfo.button != 1 || !(screen instanceof ChatScreen) || lastHovered == null) return;
        boolean log = mc.hasShiftDown();
        var content = log ? lastHovered.content().getString() : lastHovered.content().getString().stripFormatting();
        if (log) RSM.getLogger().info("Chat Copy: {}", lastHovered.content());
        mc.keyboardHandler.setClipboard(content);
        NotificationManager.showNotification(log ? "Logged Component" : "Copied Text", content, NotificationType.INFO, 1500);
    }

    @SubscribeEvent
    private void onShowChat(ChatEvent.Show event) {
        if (!event.isOverlay() && checkMessage(ChatFormatting.stripFormatting(event.getMessage().getString()))) {
            event.setCancelled(true);
        }
    }

    private boolean checkMessage(String message) {
        return hiddenMessages.getValue().stream().anyMatch(msg -> msg.check(message));
    }

    public static void add(HiddenMessage hiddenMessage) {
        instance.hiddenMessages.getValue().add(hiddenMessage);
        save();
    }

    public static void save() {
        instance.hiddenMessages.save();
    }

    public static void setLastHovered(GuiMessage.Line line) {
        lastHovered = lineCache.get(line);
    }

    public static void resetLastHovered() {
        lastHovered = null;
    }
}
