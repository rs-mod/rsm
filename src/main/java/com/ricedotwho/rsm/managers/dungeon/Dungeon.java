package com.ricedotwho.rsm.managers.dungeon;

import com.ricedotwho.rsm.event.api.Register;
import com.ricedotwho.rsm.event.api.SubscribeEvent;
import com.ricedotwho.rsm.event.impl.client.PacketEvent;
import com.ricedotwho.rsm.event.impl.game.ChatEvent;
import com.ricedotwho.rsm.event.impl.game.DungeonEvent;
import com.ricedotwho.rsm.event.impl.game.SecretPickupEvent;
import com.ricedotwho.rsm.event.impl.game.TickEvent;
import com.ricedotwho.rsm.event.impl.player.PlayerInputEvent;
import com.ricedotwho.rsm.event.impl.world.WorldEvent;
import com.ricedotwho.rsm.location.Island;
import com.ricedotwho.rsm.location.Location;
import com.ricedotwho.rsm.managers.dungeon.map.DungeonScanner;
import com.ricedotwho.rsm.managers.dungeon.map.UniqueRoom;
import com.ricedotwho.rsm.module.impl.dungeon.waypoint.SecretType;
import com.ricedotwho.rsm.utils.DungeonUtils;
import com.ricedotwho.rsm.utils.NumberUtils;
import com.ricedotwho.rsm.utils.StringUtils;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.UtilityClass;
import net.minecraft.ChatFormatting;
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
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.ricedotwho.rsm.type.Accessor.mc;

@UtilityClass
@Register
public class Dungeon {
    public final Pattern TERM = Pattern.compile("^(.*?) (?:activated|completed) a (terminal|device|lever)! \\((\\d+)/(\\d+)\\)");
    private final Pattern TABLIST = Pattern.compile("^\\[(?<sbLevel>\\d+)] (?:\\[?\\w+] )*(?<name>\\w+) .*?\\((?<class>\\w+)(?: (?<classLevel>\\w+))*\\)$");
    private final Pattern SECRETS_PATTERN = Pattern.compile("(\\d{1,2})/(\\d{1,2}) Secrets");
    private final Pattern PRINCE = Pattern.compile("^A Prince falls\\. \\+1 Bonus Score$");
    private final Pattern BAT = Pattern.compile("^A Bat has been slain\\. \\+1 Bonus Score$");
    private final Pattern PARTY = Pattern.compile("Party > (?:\\[(.*?)] )?(.+?): (.+)$");
    @Getter
    @Setter
    private boolean started = false;
    @Getter
    @Setter
    private boolean inBoss = false;
    @Getter
    private boolean inP3 = false;
    @Getter
    private final Set<DungeonPlayer> players = new HashSet<>();
    @Getter
    private static final Set<DungeonPlayer> playersNoSelf = new HashSet<>();
    @Getter
    private boolean bloodOpen = false;
    @Getter
    private boolean princeKilled = false;
    @Getter
    private Set<String> bats = new HashSet<>();
    private final Map<String, DungeonPlayer> knownPlayers = new HashMap<>();

    @Getter
    private int p3SectionInt = -1;
    @Getter
    private Phase7 p3Section = Phase7.UNKNOWN;

    private final Set<String> SECRET_NAMES  = Set.of(
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
    private final String REDSTONE_KEY_ID = "fed95410-aba1-39df-9b95-1d4f361eb66e";
    private final String WITHER_ESSENCE_ID = "2865274b-3097-394e-8149-ec629c72d850";

    @SubscribeEvent
    private void onPacket(ChatEvent.Chat event) {
        if (mc.level == null || mc.player == null || !Location.getArea().is(Island.Dungeon)) return;
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
            if (!bloodOpen) {
                bloodOpen = true;
                new DungeonEvent.BloodOpened().post();
            }
            String boss = getBossName();
            if (boss != null && text.contains(boss)) {
                inBoss = true;
                new DungeonEvent.EnterBoss(Location.getFloor()).post();
            }
        }
        else if (message.contains("" + ChatFormatting.YELLOW + ChatFormatting.BOLD + "EXTRA STATS") && Location.getArea().is(Island.Dungeon)) {
            new DungeonEvent.End(Location.getFloor()).post();
            started = false;
        }
        else if (BAT.matcher(event.getString()).find()) {
            bats.add(mc.player.getName().getString());
        }
        else if (PRINCE.matcher(event.getString()).find()) {
            princeKilled = true;
        } else {
            var match = PARTY.matcher(event.getString());
            if (!match.find()) return;
            var name = match.group(2);
            switch (match.group(3).toLowerCase()) {
                case "bat killed", "bat killed!", "bat dead", "bat dead!" -> bats.add(name);
                case "prince killed", "prince slain", "prince killed!", "prince dead", "prince dead!" -> princeKilled = true;
            }
        }
    }

    @SubscribeEvent
    private void onWorldLoad(WorldEvent.Load event) {
        reset();
    }

    private void reset() {
        players.clear();
        playersNoSelf.clear();
        knownPlayers.clear();
        inBoss = false;
        bloodOpen = false;
        started = false;
        inP3 = false;
        p3SectionInt = -1;
        p3Section = Phase7.UNKNOWN;
        bats.clear();
        princeKilled = false;
    }

    @SubscribeEvent
    private void onChat(ChatEvent.Chat event) {
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
    private void onTabList(PacketEvent.MainReceivePre event, ClientboundPlayerInfoUpdatePacket packet) {
        if (!Location.getArea().is(Island.Dungeon)) return;

        for (ClientboundPlayerInfoUpdatePacket.Entry e : packet.entries()) {
            if (e.displayName() == null) continue;
            String text = ChatFormatting.stripFormatting(e.displayName().getString().trim());

            Matcher matcher = TABLIST.matcher(text);
            if (!matcher.find()) continue;
            String cl = matcher.group("classLevel");
            String name = matcher.group("name");
            String classString = matcher.group("class");
            DungeonClass clazz = DungeonClass.findClassString(classString);

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
                if (dp != null) dp.update(clazz, level, classString.contains("DEAD"));
                continue;
            }
            AbstractClientPlayer player = optional.get();

            DungeonPlayer dp = getPlayer(player);
            if (dp == null) {
                addPlayer(new DungeonPlayer(clazz, player, level, 0));
            } else {
                dp.update(clazz, level, classString.contains("DEAD"));
            }
        }
    }

    private void addPlayer(DungeonPlayer player) {
        players.add(player);
        if (!Objects.equals(player.getName(), mc.player.getName().getString())) playersNoSelf.add(player);
        knownPlayers.put(player.getName(), player);
    }

    // maybe this should be on S08?
    @SubscribeEvent
    private void checkInBoss(TickEvent.ClientStart event) {
        if (event.getTime() % 20 != 0 || !Location.getArea().is(Island.Dungeon) || mc.player == null) return;
        net.minecraft.world.phys.Vec3 vec3 = mc.player.position();
        if (switch (Location.getFloor()) {
            case F1, M1 -> vec3.x() > -70 && vec3.z() > -40;
            case F2, M2, F3, M3, F4, M4 -> vec3.x() > -40 && vec3.z() > -40;
            case F5, M5, F6, M6 -> vec3.x() > -40 && vec3.z() > -8;
            case F7, M7 -> vec3.x() > -8 && vec3.z() > -8;
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
    public DungeonPlayer getMyPlayer() {
        if (mc.player == null) return null;
        return knownPlayers.get(mc.player.getName().getString());
    }

    /**
     * Gets a DungeonPlayer from name
     * @param name The players name
     * @return {@link DungeonPlayer} or null, it no DungeonPlayer is found
     */
    public DungeonPlayer getPlayer(String name) {
        return knownPlayers.get(name);
    }

    /**
     * Gets a DungeonPlayer from Player
     * @param player The player
     * @return {@link DungeonPlayer} or null, it no DungeonPlayer is found
     */
    public DungeonPlayer getPlayer(Player player) {
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
    public DungeonPlayer getClazz(DungeonClass clazz) {
        Optional<DungeonPlayer> player = players.stream().filter(dp -> dp.getDClass().equals(clazz)).findFirst();
        return player.orElse(null);
    }

    /**
     * Gets a DungeonPlayer from DungeonClass index
     * @param c The DungeonClass index
     * @return {@link DungeonPlayer} or null, it no DungeonPlayer is found
     */
    public DungeonPlayer getClazz(int c) {
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
    public boolean isMyClass(DungeonClass clazz) {
        DungeonPlayer player = getMyPlayer();
        if(player == null || player.getDClass() == null) return false;
        return player.getDClass().equals(clazz);
    }

    public int getPlayersLeapt() {
        Phase7 phase = DungeonUtils.getP3Section();
        return Math.toIntExact(players.stream().filter(p -> {
            if (p.findPlayer() == null) return false;
            return DungeonUtils.getP3Section(p.getPlayer().position()) == phase;
        }).count());
    }

    @SubscribeEvent
    public void onActionBar(ChatEvent.ActionBar event) {
        if (!Location.getArea().is(Island.Dungeon) || Dungeon.isInBoss() || !Dungeon.isStarted() || mc.level == null || Dungeon.current() == null) return;
        Matcher matcher = SECRETS_PATTERN.matcher(event.getMessage().getString().stripFormatting());
        if (matcher.find()) {
            var uni = Dungeon.current();
            var found = matcher.group(1).toInt();
            var max = matcher.group(2).toInt();
            if (uni.getInfo().secrets() != max) return;
            uni.foundSecrets = found;
        }
    }

    @SubscribeEvent
    private void onSoundOrItemPacket(PacketEvent.MainReceivePre event) {
        if (!Location.getArea().is(Island.Dungeon) || Dungeon.isInBoss() || !Dungeon.isStarted() || mc.level == null) return;
        if (event.getPacket() instanceof ClientboundSoundPacket packet) {
            String name = packet.getSound().getRegisteredName();
            if (!name.startsWith("minecraft:")) return;
            switch (name.substring(10)) {
                case "entity.bat.death", "entity.bat.hurt" -> new SecretPickupEvent(new Vec3(packet.getX(), packet.getY(), packet.getZ()), SecretType.BAT).post();
                case "block.piston.contract", "block.piston.extend" -> new SecretPickupEvent(new Vec3(packet.getX(), packet.getY(), packet.getZ()), SecretType.REDSTONE_BLOCK).post();
            }
        } else if (event.getPacket() instanceof ClientboundTakeItemEntityPacket packet) {
            Entity entity = mc.level.getEntity(packet.getItemId());
            if (!(entity instanceof ItemEntity itemEntity)) return;
            String name = ChatFormatting.stripFormatting(itemEntity.getItem().getHoverName().getString());
            if (!StringUtils.containsAny(name, SECRET_NAMES)) return;
            new SecretPickupEvent(itemEntity.blockPosition().toVec3(), SecretType.ITEM).post();
        } else if (event.getPacket() instanceof ClientboundRemoveEntitiesPacket packet) {
            packet.getEntityIds().forEach(id -> {
                Entity entity = mc.level.getEntity(id);
                if (entity instanceof ItemEntity itemEntity) {
                    assert mc.player != null;
                    if (entity.distanceToSqr(mc.player) <= 64 && StringUtils.containsAny(ChatFormatting.stripFormatting(itemEntity.getItem().getHoverName().getString()), SECRET_NAMES)) {
                        new SecretPickupEvent(new Vec3(itemEntity.blockPosition()), SecretType.ITEM).post();
                    }
                }
            });
        }
    }

    @SubscribeEvent
    private void onClickBlock(PacketEvent.Send event, ServerboundUseItemOnPacket packet) {
        if (mc.level == null) return;

        BlockPos bp = packet.getHitResult().getBlockPos();
        BlockState state = mc.level.getBlockState(bp);
        Block block = state.getBlock();

        if (block == Blocks.CHEST || block == Blocks.TRAPPED_CHEST) {
            new SecretPickupEvent(new Vec3(bp), SecretType.CHEST).post();
        } else if (block == Blocks.PLAYER_HEAD) {
            if (getSkullType(bp, mc.level) == SkullType.ESSENCE) {
                new SecretPickupEvent(new Vec3(bp), SecretType.ESSENCE).post();
            }
        } else if (block == Blocks.LEVER) {
            new SecretPickupEvent(new Vec3(bp), SecretType.LEVER).post();
        }
    }

    // must run before the thing is destroyed i guess
    @SubscribeEvent
    public void preUseOn(PlayerInputEvent.Use event) {
        if (mc.player == null || !Location.getArea().is(Island.Dungeon) || !(event.getResult() instanceof BlockHitResult result)) return;
        var bp = result.getBlockPos();
        BlockState state = mc.level.getBlockState(bp);
        Block block = state.getBlock();

        if (block == Blocks.PLAYER_HEAD && getSkullType(bp, mc.level) == SkullType.KEY) {
            new SecretPickupEvent(new Vec3(bp), SecretType.REDSTONE_KEY).post();
        }
    }

    @SubscribeEvent
    public void preMineBlock(PlayerInputEvent.Attack event) {
        if (mc.player == null || !Location.getArea().is(Island.Dungeon) || !(event.getResult() instanceof BlockHitResult result)) return;
        handleAttack(result);
    }

    @SubscribeEvent
    public void preMineBlock(PlayerInputEvent.ContinueAttack event) {
        if (mc.player == null || !Location.getArea().is(Island.Dungeon) || !(event.getResult() instanceof BlockHitResult result)) return;
        handleAttack(result);
    }

    private void handleAttack(BlockHitResult result) {
        var bp = result.getBlockPos();
        BlockState state = mc.level.getBlockState(bp);
        Block block = state.getBlock();

        if (block == Blocks.PLAYER_HEAD && getSkullType(bp, mc.level) == SkullType.KEY) {
            new SecretPickupEvent(new Vec3(bp), SecretType.REDSTONE_KEY).post();
        } else if (block == Blocks.LEVER && Dungeon.isInBoss()) {
            new SecretPickupEvent(new Vec3(bp), SecretType.LEVER).post();
        }
    }

    public SkullType getSkullType(BlockPos blockPos, ClientLevel level) {
        BlockEntity entity = level.getBlockEntity(blockPos);
        if (!(entity instanceof SkullBlockEntity skullBlockEntity)) return SkullType.NONE;
        return getSkullType(skullBlockEntity.getOwnerProfile());
    }

    public SkullType getSkullType(ResolvableProfile gameProfile) {
        if (gameProfile == null) return SkullType.NONE;
        String uuid = gameProfile.partialProfile().id().toString();
        return switch (uuid) {
            case WITHER_ESSENCE_ID -> SkullType.ESSENCE;
            case REDSTONE_KEY_ID -> SkullType.KEY;
            default -> SkullType.NONE;
        };
    }

    public UniqueRoom current() {
        return DungeonScanner.currentRoom();
    }

    public enum SkullType {
        ESSENCE,
        KEY,
        NONE
    }
}
