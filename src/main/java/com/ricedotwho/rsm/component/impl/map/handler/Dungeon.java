package com.ricedotwho.rsm.component.impl.map.handler;

import com.ricedotwho.rsm.component.ModComponent;
import com.ricedotwho.rsm.component.impl.location.Island;
import com.ricedotwho.rsm.component.impl.location.Loc;
import com.ricedotwho.rsm.event.annotations.SubscribeEvent;
import com.ricedotwho.rsm.event.impl.client.PacketEvent;
import com.ricedotwho.rsm.event.impl.game.DungeonEvent;
import com.ricedotwho.rsm.event.impl.game.WorldEvent;
import com.ricedotwho.rsm.utils.Accessor;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Dungeon extends ModComponent {
    public static boolean dungeonStarted = false;
    public static boolean inBoss = false;
    //public static List<DungeonPlayer> dungeonPlayers = new ArrayList<>();
    private static boolean ranPlayerCheck = false;
    private static boolean startingSoon = false;
    @Getter
    private static boolean bloodOpen = false;
    static String classRegex = "^\\[([MATHB])] (\\S+) \\[Lv(\\d+)]$";
    static String tablistRegex = "^\\[(?<sbLevel>\\d+)\\] (?:\\[?\\w+\\] )*(?<name>\\w+) .*?\\((?<class>\\w+)(?: (?<classLevel>\\w+))*\\)$";
    static Pattern classPattern;
    static Pattern tablistPattern;
    static {
        classPattern = Pattern.compile(classRegex);
        tablistPattern = Pattern.compile(tablistRegex);
    }

    public Dungeon() {
        super("Dungeon");
    }

//    @SubscribeEvent
//    public void onPacket(PacketEvent.Receive event) {
//        if (mc == null || mc.theWorld == null || mc.thePlayer == null) return;
//        if (event.packet instanceof S02PacketChat) {
//            String message = ((S02PacketChat) event.packet).getChatComponent().getUnformattedText();
//            String text = EnumChatFormatting.getTextWithoutFormattingCodes(message);
//            if (text.startsWith("[NPC] Mort: Here, I found this map when I first entered the dungeon.")) {
//                dungeonStarted = true;
//                inBoss = false;
//                bloodOpen = false;
//                Utils.postAndCatch(new DungeonEvent.Start(Loc.getFloor()));
//                return;
//            }
//            if (text.startsWith("[BOSS]")) {
//                if(!bloodOpen) {
//                    bloodOpen = true;
//                    Utils.postAndCatch(new DungeonEvent.BloodOpened());
//                }
//                String boss = bossName();
//                if(boss != null && text.contains(boss)) {
//                    inBoss = true;
//                    Utils.postAndCatch(new DungeonEvent.EnterBoss(Loc.getFloor()));
//                }
//            }
//            if(message.contains("" + EnumChatFormatting.YELLOW + EnumChatFormatting.BOLD + "EXTRA STATS") && Loc.area.is(Island.Dungeon)) {
//                Utils.postAndCatch(new DungeonEvent.End(Loc.floor));
//            }
//            if(message.equals("Starting in 4 seconds.")) {
//                startingSoon = true;
//                fetchClasses();
//            }
//            //todo: stop people from being able to type ths and break mod
//            if(message.endsWith(" is no longer ready!")) {
//                if(dungeonStarted) return;
//                startingSoon = false;
//                ranPlayerCheck = false;
//                fetchClasses();
//            }
//        }
//    }

//    @SubscribeEvent
//    public void onUnload(WorldEvent.Unload event){ reset(); }

//    private void reset() {
//        dungeonPlayers.clear();
//        ranPlayerCheck = false;
//        startingSoon = false;
//        inBoss = false;
//        bloodOpen = false;
//    }
//
//    private static void fetchClasses() {
//        if(!Loc.area.is(Island.Dungeon)) return;
//        List<String> tablist = TablistUtils.readTabList();
//
//        for(String l : tablist) {
//            String line = EnumChatFormatting.getTextWithoutFormattingCodes(l);
//            Matcher matcher = tablistPattern.matcher(line);
//            if(!matcher.find()) continue;
//            String cl = matcher.group("classLevel");
//            int level = 0;
//            if(cl != null) {
//                if(Utils.isInteger(cl)) {
//                    level = Integer.parseInt(cl);
//                }
//                else {
//                    level = Utils.convertRomanToArabic(cl);
//                }
//            }
//            EntityPlayer entityPlayer = mc.theWorld.getPlayerEntityByName(matcher.group("name"));
//            if(entityPlayer != null) {
//                DungeonPlayer player = new DungeonPlayer(DungeonClass.findClassString(matcher.group("class")), entityPlayer, level, 0);
//                if(contains(dungeonPlayers, player)) {
//                    replace(dungeonPlayers, player);
//                } else {
//                    dungeonPlayers.add(player);
//                }
//            }
//        }
//
//        if(dungeonPlayers.isEmpty()) {
//            if(startingSoon) {
//                sendMessageWithPrefix(EnumChatFormatting.RED + "Failed to find players!");
//                return;
//            }
//            Utils.onTick(10, Dungeon::fetchClasses);
//            return;
//        }
//
//        // Stop if we have 5 players
//        if (dungeonPlayers.size() == 5) { ranPlayerCheck = true; return; }
//        if (startingSoon && !ranPlayerCheck) {
//            onLowPlayer();
//            ranPlayerCheck = true;
//            return;
//        }
//        if (!ranPlayerCheck) Utils.onTick(10, Dungeon::fetchClasses);
//    }
//
//    private static boolean contains(List<DungeonPlayer> list, DungeonPlayer player) {
//        for(DungeonPlayer l : list) {
//            if(!l.getName().equals(player.getName())) continue;
//            return true;
//        }
//        return false;
//    }
//
//    private static List<DungeonPlayer> replace(List<DungeonPlayer> list, DungeonPlayer player) {
//        for(DungeonPlayer l : list) {
//            if(!l.getName().equals(player.getName())) continue;
//            int i = list.indexOf(l);
//            if(i < 0) continue;
//            list.set(i, player);
//            return list;
//        }
//        return list;
//    }
//
//    @SubscribeEvent
//    public void dungeonStart(DungeonEvent.Start event) {
//        // Initialize the scheduler when the dungeon starts
//        Utils.onTick(20 * 3, Dungeon::fetchClasses);
//    }

//    @SubscribeEvent
//    public void onBoss(DungeonEvent.EnterBoss event) {
//        fetchClasses();
//    }

    private String bossName() {
        switch (Loc.floor) {
            default:
                return null;
            case F1:
            case M1:
                return "Bonzo";
            case F2:
            case M2:
                return "Scarf";
            case F3:
            case M3:
                return "The Professor";
            case F4:
            case M4:
                return "Thorn";
            case F5:
            case M5:
                return "Livid";
            case F6:
            case M6:
                return "Sadan";
            case F7:
            case M7:
                return "Maxor";
        }
    }

//    public static DungeonPlayer getMyPlayer() {
//        for(DungeonPlayer dp : dungeonPlayers) {
//            if(dp == null) continue;
//            if(mc.thePlayer.getName().equalsIgnoreCase(dp.getName())) return dp;
//        }
//        fetchClasses();
//        return null;
//    }
//
//    public static DungeonPlayer getPlayer(String name) {
//        for(DungeonPlayer dp : dungeonPlayers) {
//            if(dp == null) continue;
//            if(dp.getName().equalsIgnoreCase(name)) return dp;
//        }
//        fetchClasses();
//        return null;
//    }
//
//    public static DungeonPlayer getPlayer2(String name) {
//        for(DungeonPlayer dp : dungeonPlayers) {
//            if(dp == null) continue;
//            if(dp.getName().equalsIgnoreCase(name)) return dp;
//        }
//        return null;
//    }
//
//    public static DungeonPlayer getClazz(DungeonClass clazz) {
//        for (DungeonPlayer dp : dungeonPlayers) {
//            if(dp == null) continue;
//            if(clazz.equals(dp.getMyClass())) return dp;
//        }
//        fetchClasses();
//        return null;
//    }
//
//    public static DungeonPlayer getClazz(int c) {
//        DungeonClass clazz = DungeonClass.NONE;
//        if(c < 0) return null;
//        switch (c) {
//            case 0:
//                clazz = DungeonClass.ARCHER;
//                break;
//            case 1:
//                clazz = DungeonClass.MAGE;
//                break;
//            case 2:
//                clazz = DungeonClass.BERSERKER;
//                break;
//            case 3:
//                clazz = DungeonClass.HEALER;
//                break;
//            case 4:
//                clazz = DungeonClass.TANK;
//                break;
//        }
//        return getClazz(clazz);
//    }
//
//    public static boolean isMyPlayerClass(DungeonClass clazz) {
//        DungeonPlayer player = getMyPlayer();
//        if(player == null || player.getDClass() == null) return false;
//        return player.getDClass().equals(clazz);
//    }
//
//    public static void onLowPlayer() {
//        DungeonMap module = RS.getModule(DungeonMap.class);
//        if(module == null || !module.getLowPlayerWarn().getValue()) return;
//        sendMessageWithPrefix("Less than 5 players in dungeon!");
//        Hud.createTitle(EnumChatFormatting.RED + "<5 Players!", 1500);
//        mc.thePlayer.playSound("note.pling",1f,1f);
//        if(!module.getLowPlayerWarnMessage().getValue()) return;
//        mc.thePlayer.sendChatMessage("/pc Less than 5 players in dungeon!");
//    }
//
//    public static int playersLeapt() {
//        int i = 0;
//        Phase7 phase = DungeonUtils.getP3Section();
//        for (DungeonPlayer dPlayer : dungeonPlayers) {
//            EntityPlayer player = dPlayer.getPlayer();
//            if(player == mc.thePlayer) continue;
//            Pos pos = new Pos(player.posX, player.posY, player.posZ);
//            if(DungeonUtils.getP3Section(pos) == phase) i++;
//        }
//        return i;
//    }
}
