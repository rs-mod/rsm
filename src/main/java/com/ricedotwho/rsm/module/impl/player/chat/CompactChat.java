//package com.ricedotwho.rsm.module.impl.player.chat;
//
//import com.ricedotwho.rsm.module.SubModule;
//import com.ricedotwho.rsm.module.api.SubModuleInfo;
//import com.ricedotwho.rsm.module.impl.player.Chat;
//import com.ricedotwho.rsm.ui.clickgui.settings.impl.NumberSetting;
//import lombok.AllArgsConstructor;
//import lombok.Getter;
//import net.minecraft.network.chat.Component;
//import net.minecraft.network.chat.MessageSignature;
//import net.minecraft.network.chat.MessageSignatureCache;
//import net.minecraft.network.chat.MutableComponent;
//import net.minecraft.util.FormattedCharSequence;
//
//import java.awt.*;
//import java.util.*;
//import java.util.List;
//
//@Getter
//@SubModuleInfo(name = "Chat Emotes", alwaysDisabled = false)
//public class CompactChat extends SubModule<Chat> {
//    private static CompactChat INSTANCE;
//
//    // I do not know whether storing the hash instead of the object is better
//    // From what ive read of HashMap it already stores the hash so its pointless
//    // But maybe it's bad to keep a reference to the actual object inside this map
//    // Like could that cause a memory leak?
//    private static final Map<Integer, ChatEntry> chatMessages = new HashMap<>();
//
//    private NumberSetting compactChatTime = new NumberSetting("Time to stack", 1, 300, 60, 1, "s");
//
//    public CompactChat(Chat chat) {
//        super(chat);
//        INSTANCE = this;
//        this.registerProperty(compactChatTime);
//	}
//
//    public static boolean compactChat(Component component) {
//        if (!INSTANCE.isEnabled()) return false;
//        long now = System.currentTimeMillis();
//        int hash = component.hashCode();
//
//
//        //todo: clean sk1er if else if else code
//
//        ChatEntry entry = chatMessages.computeIfAbsent(hash, _ -> new ChatEntry(1, now));
//
//        if (now - entry.time > INSTANCE.compactChatTime.getValue().longValue() * 1000) {
//            entry = new ChatEntry(1, now);
//            chatMessages.put(hash, entry);
//
//            // now we probably wanna remove all instances of this message before now
//            mc.gui.getChat().trimmedMessages.removeIf(line -> );
//        }
//
//    }
//
//
//
//    @AllArgsConstructor
//    private static class ChatEntry {
//        public int count;
//        public long time;
//    }
//}
