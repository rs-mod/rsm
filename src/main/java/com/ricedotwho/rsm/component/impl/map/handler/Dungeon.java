package com.ricedotwho.rsm.component.impl.map.handler;

import com.ricedotwho.rsm.RSM;
import com.ricedotwho.rsm.component.api.ModComponent;
import com.ricedotwho.rsm.component.impl.location.Island;
import com.ricedotwho.rsm.component.impl.location.Location;
import com.ricedotwho.rsm.data.DungeonClass;
import com.ricedotwho.rsm.data.DungeonPlayer;
import com.ricedotwho.rsm.event.api.SubscribeEvent;
import com.ricedotwho.rsm.event.impl.client.PacketEvent;
import com.ricedotwho.rsm.event.impl.game.ChatEvent;
import com.ricedotwho.rsm.event.impl.game.DungeonEvent;
import com.ricedotwho.rsm.event.impl.world.WorldEvent;
import com.ricedotwho.rsm.module.impl.render.ClickGUI;
import com.ricedotwho.rsm.ui.clickgui.impl.Panel;
import com.ricedotwho.rsm.utils.ChatUtils;
import com.ricedotwho.rsm.utils.NumberUtils;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.ChatFormatting;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.world.entity.player.Player;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Dungeon extends ModComponent {
    @Getter
    private static boolean started = false;
    @Getter
    @Setter
    private static boolean inBoss = false;
    @Getter
    private static boolean inP3 = false;
    @Getter
    private static final Set<DungeonPlayer> players = new HashSet<>();
    @Getter
    private static boolean bloodOpen = false;
    private static final Pattern tablistPattern = Pattern.compile("^\\[(?<sbLevel>\\d+)] (?:\\[?\\w+] )*(?<name>\\w+) .*?\\((?<class>\\w+)(?: (?<classLevel>\\w+))*\\)$");

    public Dungeon() {
        super("Dungeon");
    }

    @SubscribeEvent
    public void onPacket(ChatEvent event) {
        if (mc.level == null || mc.player == null) return;
        String message = event.getMessage().getString();
        String text = ChatFormatting.stripFormatting(message);
        if (text.startsWith("[NPC] Mort: Here, I found this map when I first entered the dungeon.")) {
            started = true;
            inBoss = false;
            bloodOpen = false;
            new DungeonEvent.Start(Location.getFloor()).post();
            return;
        }
        if (text.startsWith("[BOSS]")) {
            if(!bloodOpen) {
                bloodOpen = true;
                new DungeonEvent.BloodOpened().post();
            }
            String boss = bossName();
            if (boss != null && text.contains(boss)) {
                inBoss = true;
                new DungeonEvent.EnterBoss(Location.getFloor()).post();
            }
        }
        if (message.contains("" + ChatFormatting.YELLOW + ChatFormatting.BOLD + "EXTRA STATS") && Location.getArea().is(Island.Dungeon)) {
            new DungeonEvent.End(Location.getFloor()).post();
        }
    }

    @SubscribeEvent
    public void onWorldLoad(WorldEvent.Load event) {
        reset();
    }

    private void reset() {
        RSM.getLogger().info("Dungeon#reset");
        ChatUtils.chat("Dungeon#reset");
        players.clear();
        inBoss = false;
        bloodOpen = false;
        started = false;
        inP3 = false;
    }

    @SubscribeEvent
    public void onChat(ChatEvent.Chat event) {
        if(mc.player == null || !Location.getArea().is(Island.Dungeon)) return;
        String message = ChatFormatting.stripFormatting(event.getMessage().getString()).trim();
        if(("[BOSS] Goldor: Who dares trespass into my domain?".equals(message))) {
            inP3 = true;
        }
        else if("The Core entrance is opening!".equals(message)) {
            inP3 = false;
        }
    }


    // todo: this runs every time a ClientboundPlayerInfoUpdatePacket is received while in a dungeon, maybe it should not? Regex is probably not that great to have running often
    @SubscribeEvent
    public void onTabList(PacketEvent.Receive event) {
        if(!(event.getPacket() instanceof ClientboundPlayerInfoUpdatePacket packet) || !Location.getArea().is(Island.Dungeon)) return;
        for (ClientboundPlayerInfoUpdatePacket.Entry e : packet.entries()) {
            if (e.displayName() == null) continue;
            String text = ChatFormatting.stripFormatting(e.displayName().getString().trim());

            Matcher matcher = tablistPattern.matcher(text);
            if (!matcher.find()) continue;
            String cl = matcher.group("classLevel");
            String name = matcher.group("name");
            DungeonClass clazz = DungeonClass.findClassString(matcher.group("class"));

            if (RSM.getModule(ClickGUI.class).getDevInfo().getValue()) {
                ChatUtils.chat("Player: %s, Class: %s, ClassLevel: %s", name, clazz, cl);
            }

            int level = 0;
            if(cl != null) {
                if (NumberUtils.isInteger(cl)) {
                    level = Integer.parseInt(cl);
                }
                else {
                    level = NumberUtils.convertRomanToArabic(cl);
                }
            }
            Optional<AbstractClientPlayer> optional = mc.level.players().stream().filter(p -> p.getName().getString().equals(name)).findFirst();
            if (optional.isEmpty()){
                DungeonPlayer dp = getPlayer(name);
                if (dp != null) dp.update(clazz, level);
                continue;
            }
            AbstractClientPlayer player = optional.get();

            DungeonPlayer dp = getPlayer(player);
            if (dp == null) {
                players.add(new DungeonPlayer(clazz, player, level, 0));
            } else {
                dp.update(clazz, level);
            }
        }
    }

    private String bossName() {
        return switch (Location.getFloor()) {
            case F1, M1 -> "Bonzo";
            case F2, M2 -> "Scarf";
            case F3, M3 -> "The Professor";
            case F4, M4 -> "Thorn";
            case F5, M5 -> "Livid";
            case F6, M6 -> "Sadan";
            case F7, M7 -> "Maxor";
            default -> null;
        };
    }

    /**
     * Gets the clients DungeonPlayer
     * @return {@link DungeonPlayer} or null, it no DungeonPlayer is found
     */
    public static DungeonPlayer getMyPlayer() {
        if (mc.player == null) return null;
        for (DungeonPlayer dp : players) {
            if (dp == null) continue;
            if (mc.player.getName().getString().equalsIgnoreCase(dp.getName())) return dp;
        }
        return null;
    }

    /**
     * Gets a DungeonPlayer from name
     * @param name The players name
     * @return {@link DungeonPlayer} or null, it no DungeonPlayer is found
     */
    public static DungeonPlayer getPlayer(String name) {
        for (DungeonPlayer dp : players) {
            if (dp == null) continue;
            if (dp.getName().equalsIgnoreCase(name)) return dp;
        }
        return null;
    }

    /**
     * Gets a DungeonPlayer from Player
     * @param player The player
     * @return {@link DungeonPlayer} or null, it no DungeonPlayer is found
     */
    public static DungeonPlayer getPlayer(Player player) {
        for (DungeonPlayer dp : players) {
            if (dp == null) continue;
            if (dp.getPlayer().equals(player)) return dp;
        }
        return null;
    }

    /**
     * Gets a DungeonPlayer from DungeonClass
     * @param clazz The DungeonClass
     * @return {@link DungeonPlayer} or null, it no DungeonPlayer is found
     */
    public static DungeonPlayer getClazz(DungeonClass clazz) {
        Optional<DungeonPlayer> player = players.stream().filter(dp -> dp.getDClass().equals(clazz)).findFirst();
        return player.orElse(null);
    }

    /**
     * Gets a DungeonPlayer from DungeonClass index
     * @param c The DungeonClass index
     * @return {@link DungeonPlayer} or null, it no DungeonPlayer is found
     */
    public static DungeonPlayer getClazz(int c) {
        DungeonClass clazz = DungeonClass.NONE;
        if(c < 0) return null;
        clazz = switch (c) {
            case 0 -> DungeonClass.ARCHER;
            case 1 -> DungeonClass.MAGE;
            case 2 -> DungeonClass.BERSERKER;
            case 3 -> DungeonClass.HEALER;
            case 4 -> DungeonClass.TANK;
            default -> clazz;
        };
        return getClazz(clazz);
    }

    /**
     * Checks if the client DungeonPlayer is a certain DungeonClass
     * @param clazz The DungeonClass
     * @return {@link Boolean}
     */
    public static boolean isMyClass(DungeonClass clazz) {
        DungeonPlayer player = getMyPlayer();
        if(player == null || player.getDClass() == null) return false;
        return player.getDClass().equals(clazz);
    }
}
