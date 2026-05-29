package com.ricedotwho.rsm.component.impl.map.handler;

import com.ricedotwho.rsm.component.api.ModComponent;
import com.ricedotwho.rsm.component.impl.location.Island;
import com.ricedotwho.rsm.component.impl.location.Location;
import com.ricedotwho.rsm.data.DungeonClass;
import com.ricedotwho.rsm.data.DungeonPlayer;
import com.ricedotwho.rsm.data.Phase7;
import com.ricedotwho.rsm.data.Pos;
import com.ricedotwho.rsm.event.api.SubscribeEvent;
import com.ricedotwho.rsm.event.impl.client.PacketEvent;
import com.ricedotwho.rsm.event.impl.client.TimeEvent;
import com.ricedotwho.rsm.event.impl.game.ChatEvent;
import com.ricedotwho.rsm.event.impl.game.DungeonEvent;
import com.ricedotwho.rsm.event.impl.game.SecretPickupEvent;
import com.ricedotwho.rsm.event.impl.world.WorldEvent;
import com.ricedotwho.rsm.module.impl.dungeon.waypoint.SecretType;
import com.ricedotwho.rsm.utils.ChatUtils;
import com.ricedotwho.rsm.utils.DungeonUtils;
import com.ricedotwho.rsm.utils.NumberUtils;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.*;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Dungeon extends ModComponent {
    public static final Pattern TERM = Pattern.compile("^(.*?) (?:activated|completed) a (terminal|device|lever)! \\((\\d+)/(\\d+)\\)");
    @Getter
    @Setter
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

    @Getter
    private static int p3SectionInt = -1;
    @Getter
    private static Phase7 p3Section = Phase7.UNKNOWN;

    private static final Set<String> SECRET_NAMES  = Set.of(
            "Health Potion VIII Splash Potion",
            "Healing Potion 8 Splash Potion",
            "Healing Potion VIII Splash Potion",
            "Healing VIII Splash Potion",
            "Healing 8 Splash Potion",
            "Decoy",
            "Inflatable Jerry",
            "Spirit Leap",
            "Trap",
            "Training Weights",
            "Defuse Kit",
            "Dungeon Chest Key",
            "Treasure Talisman",
            "Revive Stone",
            "Architect's First Draft",
            "Secret Dye",
            "Candycomb"
    );
    private static final String REDSTONE_KEY_ID = "fed95410-aba1-39df-9b95-1d4f361eb66e";
    private static final String WITHER_ESSENCE_ID = "e0f3e929-869e-3dca-9504-54c666ee6f23";

    public Dungeon() {
        super("Dungeon");
    }

    @SubscribeEvent
    public void onPacket(ChatEvent.Chat event) {
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
            String boss = getBossName();
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
        players.clear();
        inBoss = false;
        bloodOpen = false;
        started = false;
        inP3 = false;
        p3SectionInt = -1;
        p3Section = Phase7.UNKNOWN;
    }

    @SubscribeEvent
    public void onChat(ChatEvent.Chat event) {
        if(mc.player == null || !Location.getArea().is(Island.Dungeon)) return;
        String message = ChatFormatting.stripFormatting(event.getMessage().getString()).trim();
        if(("[BOSS] Goldor: Who dares trespass into my domain?".equals(message))) {
            inP3 = true;
            p3Section = Phase7.S1;
            p3SectionInt = 0;
        }
        else if("The Core entrance is opening!".equals(message)) {
            inP3 = false;
        }
        if (!inP3) return;
        Matcher matcher = TERM.matcher(message);
        if (!matcher.find()) return;
        int start = Integer.parseInt(matcher.group(3));
        int end = Integer.parseInt(matcher.group(4));
        if (start == end) {
            p3SectionInt++;
            p3Section = DungeonUtils.getSectionFromI(p3SectionInt);
        }
    }


    // todo: this runs every time a ClientboundPlayerInfoUpdatePacket is received while in a dungeon, maybe it should not? Regex is probably not that great to have running often
    @SubscribeEvent
    public void onTabList(PacketEvent.Receive event) {
        if (!(event.getPacket() instanceof ClientboundPlayerInfoUpdatePacket packet) || !Location.getArea().is(Island.Dungeon)) return;
        for (ClientboundPlayerInfoUpdatePacket.Entry e : packet.entries()) {
            if (e.displayName() == null) continue;
            String text = ChatFormatting.stripFormatting(e.displayName().getString().trim());

            Matcher matcher = tablistPattern.matcher(text);
            if (!matcher.find()) continue;
            String cl = matcher.group("classLevel");
            String name = matcher.group("name");
            DungeonClass clazz = DungeonClass.findClassString(matcher.group("class"));

            int level = 0;
            if(cl != null) {
                if (NumberUtils.isInteger(cl)) {
                    level = Integer.parseInt(cl);
                }
                else {
                    level = NumberUtils.convertRomanToArabic(cl);
                }
            }
            Optional<AbstractClientPlayer> optional = mc.level == null ? Optional.empty() : mc.level.players().stream().filter(p -> p.getName().getString().equals(name)).findFirst();
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

    // maybe this should be on S08?
    @SubscribeEvent
    public void checkInBoss(TimeEvent.Second event) {
        if (!Location.getArea().is(Island.Dungeon) || mc.player == null) return;
        Vec3 pos = mc.player.position();
        if (switch (Location.getFloor()) {
            case F1, M1 -> pos.x() > -70 && pos.z() > -40;
            case F2, M2, F3, M3, F4, M4 -> pos.x() > -40 && pos.z() > -40;
            case F5, M5, F6, M6 -> pos.x() > -40 && pos.z() > -8;
            case F7, M7 -> pos.x() > -8 && pos.z() > -8;
            case null, default -> false;
        }) inBoss = true;
        getPlayers().forEach(DungeonPlayer::findPlayer);
    }

    private String getBossName() {
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

    public static int getPlayersLeapt() {
        Phase7 phase = DungeonUtils.getP3Section();
        return Math.toIntExact(players.stream().filter(p -> {
            if (p.findPlayer() == null) return false;
            return DungeonUtils.getP3Section(p.getPlayer().position()) == phase;
        }).count());
    }

    @SubscribeEvent
    public void onSoundOrItemPacket(PacketEvent.Receive event) {
        if (!Location.getArea().is(Island.Dungeon) || Dungeon.isInBoss() || !Dungeon.isStarted() || mc.level == null) return;
        if (event.getPacket() instanceof ClientboundSoundPacket packet) {
            String name = packet.getSound().getRegisteredName();
            if (!name.startsWith("minecraft:")) return;
            switch (name.substring(10)) {
                case "entity.bat.death", "entity.bat.hurt" -> new SecretPickupEvent(new Pos(packet.getX(), packet.getY(), packet.getZ()), SecretType.BAT).post();
                case "block.piston.contract", "block.piston.extend" -> new SecretPickupEvent(new Pos(packet.getX(), packet.getY(), packet.getZ()), SecretType.REDSTONE_BLOCK);
            }
        } else if (event.getPacket() instanceof ClientboundTakeItemEntityPacket packet) {
            Entity entity = mc.level.getEntity(packet.getItemId());
            if (!(entity instanceof ItemEntity itemEntity)) return;
            String name = ChatFormatting.stripFormatting(itemEntity.getItem().getHoverName().getString());
            if (!SECRET_NAMES.contains(name)) return;
            new SecretPickupEvent(new Pos(itemEntity.position()), SecretType.ITEM).post();
        } else if (event.getPacket() instanceof ClientboundRemoveEntitiesPacket packet) {
            packet.getEntityIds().forEach(id -> {
                Entity entity = mc.level.getEntity(id);
                if (entity instanceof ItemEntity itemEntity
                        && entity.distanceToSqr(mc.player) < 64
                        && SECRET_NAMES.contains(ChatFormatting.stripFormatting(itemEntity.getItem().getHoverName().getString()))) {
                    new SecretPickupEvent(new Pos(itemEntity.position()), SecretType.ITEM).post();
                }
            });
        }
    }

    @SubscribeEvent
    public void onClickBlock(PacketEvent.Send event) {
        if (!(event.getPacket() instanceof ServerboundUseItemOnPacket packet) || mc.level == null) return;
        BlockPos bp = packet.getHitResult().getBlockPos();
        BlockState state = mc.level.getBlockState(bp);
        Block block = state.getBlock();

        if (block == Blocks.CHEST || block == Blocks.TRAPPED_CHEST) {
            new SecretPickupEvent(new Pos(bp), SecretType.CHEST).post();
        } else if (block == Blocks.PLAYER_HEAD) {
            SkullType type = getSkullType(bp, mc.level);
            switch (type) {
                case ESSENCE -> new SecretPickupEvent(new Pos(bp), SecretType.ESSENCE).post();
                case KEY -> new SecretPickupEvent(new Pos(bp), SecretType.REDSTONE_KEY).post();
            }
        } else if (block == Blocks.LEVER) {
            new SecretPickupEvent(new Pos(bp), SecretType.LEVER).post();
        }
    }

    public static SkullType getSkullType(BlockPos blockPos, ClientLevel level) {
        BlockEntity entity = level.getBlockEntity(blockPos);
        if (!(entity instanceof SkullBlockEntity skullBlockEntity)) return SkullType.NONE;
        return getSkullType(skullBlockEntity.getOwnerProfile());
    }

    public static SkullType getSkullType(ResolvableProfile gameProfile) {
        if (gameProfile == null) return SkullType.NONE;
        String uuid = gameProfile.partialProfile().id().toString();
        return switch (uuid) {
            case WITHER_ESSENCE_ID -> SkullType.ESSENCE;
            case REDSTONE_KEY_ID -> SkullType.KEY;
            default -> SkullType.NONE;
        };
    }

    public enum SkullType {
        ESSENCE,
        KEY,
        NONE
    }
}
