package com.ricedotwho.rsm.module.impl.dungeon.posmsg;

import com.google.common.reflect.TypeToken;
import com.ricedotwho.rsm.component.impl.Renderer3D;
import com.ricedotwho.rsm.component.impl.location.Island;
import com.ricedotwho.rsm.component.impl.location.Location;
import com.ricedotwho.rsm.component.impl.map.handler.Dungeon;
import com.ricedotwho.rsm.component.impl.map.map.Room;
import com.ricedotwho.rsm.component.impl.map.map.UniqueRoom;
import com.ricedotwho.rsm.component.impl.map.utils.RoomUtils;
import com.ricedotwho.rsm.component.impl.map.utils.ScanUtils;
import com.ricedotwho.rsm.data.Colour;
import com.ricedotwho.rsm.data.DataStore;
import com.ricedotwho.rsm.data.DungeonPlayer;
import com.ricedotwho.rsm.data.Pos;
import com.ricedotwho.rsm.event.api.SubscribeEvent;
import com.ricedotwho.rsm.event.impl.game.ClientTickEvent;
import com.ricedotwho.rsm.event.impl.game.DungeonEvent;
import com.ricedotwho.rsm.event.impl.render.Render3DEvent;
import com.ricedotwho.rsm.module.Module;
import com.ricedotwho.rsm.module.api.Category;
import com.ricedotwho.rsm.module.api.ModuleInfo;
import com.ricedotwho.rsm.module.impl.render.hud.Hud;
import com.ricedotwho.rsm.ui.clickgui.settings.impl.*;
import com.ricedotwho.rsm.utils.Formatter;
import com.ricedotwho.rsm.utils.render.render3d.type.Rectangle;
import lombok.Getter;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.*;

@Getter
@ModuleInfo(aliases = "Pos Msg", id = "PosMsg", category = Category.DUNGEONS)
public class PosMsg extends Module {

    private final BooleanSetting partyChat = new BooleanSetting("Use party chat", true);
    private final BooleanSetting noRender = new BooleanSetting("No Render", false);
    private final BooleanSetting notDungeon = new BooleanSetting("Not Dungeon", false);
    private final BooleanSetting renderText = new BooleanSetting("Render Text", false);
    private final BooleanSetting renderDepth = new BooleanSetting("Depth", true);
    private final NumberSetting renderDistance = new NumberSetting("Render Distance", 0, 150, 50, 5);
    private final NumberSetting lineWidth = new NumberSetting("Line Width", 0.25, 5, 2.5, 0.25);
    private final BooleanSetting allPlayers = new BooleanSetting("Work for all players", false);
    private final BooleanSetting noOthersChat = new BooleanSetting("Don't announces others", false);
    private final StringSetting selfFormat = new StringSetting("Self Format", "at {message}");
    private final StringSetting otherFormat = new StringSetting("Other Format", "{player} -> {message}");
    private final NumberSetting resendDelay = new NumberSetting("Resend delay", 0, 1000, 500, 100);

    private final BooleanSetting clearMsg = new BooleanSetting("Clear PosMsg", false);
    private final BooleanSetting bossMsg = new BooleanSetting("Boss PosMsg", true);

    private final ColourSetting active = new ColourSetting("Active", new Colour(0, 150, 150));
    private final ColourSetting inactive = new ColourSetting("Inactive", new Colour(0, 0, 0));

    private static final SaveSetting<Map<String, List<Msg>>> clear = new SaveSetting<>("Clear", "dungeon/posmsg/clear", "clear.json", HashMap::new, new TypeToken<Map<String, List<Msg>>>() {}.getType(), true, true, PosMsg::onClearLoad);
    private static final SaveSetting<Map<String, List<Msg>>> boss = new SaveSetting<>("Boss", "dungeon/posmsg/boss", "boss.json", HashMap::new, new TypeToken<Map<String, List<Msg>>>() {}.getType(), true, true, PosMsg::updateCurrentRenderMessageForBoss);

    private final ModeSetting soundMode = new ModeSetting("Sound Mode", "Off", List.of("Off", "Self", "Others", "All"));
    private final StringSetting sound = new StringSetting("Sound", "block.note_block.pling", false, false, () -> !soundMode.is("Off"));
    private final NumberSetting volume = new NumberSetting("Volume", 0, 10, 1, 0.1, () -> !soundMode.is("Off"));
    private final NumberSetting pitch = new NumberSetting("Pitch", 0, 2, 1, 0.1, () -> !soundMode.is("Off"));
    private final ButtonSetting playSound = new ButtonSetting("Play sound", "Play", () -> !soundMode.is("Off"), this::playSound);

    private final ModeSetting titleMode = new ModeSetting("Title Mode", "Off", List.of("Off", "Self", "Others", "All"));
    private final ColourSetting titleColour = new ColourSetting("Title Colour", Colour.WHITE.copy(), () -> !titleMode.is("Off"));
    private final NumberSetting duration = new NumberSetting("Duration", 0, 5000, 1000, 0.1, () -> !titleMode.is("Off"));

    private static final List<Msg> activeMsgs = new ArrayList<>();
    @Getter
    private static List<Msg> currentRenderMsgs = new ArrayList<>();

    public PosMsg() {
        this.registerProperty(
                partyChat,
                noRender,
                notDungeon,
                renderText,
                renderDepth,
                renderDistance,
                lineWidth,
                allPlayers,
                noOthersChat,
                selfFormat,
                otherFormat,
                resendDelay,
                clearMsg,
                bossMsg,
                active,
                inactive,
                clear,
                boss,
                soundMode,
                sound,
                volume,
                pitch,
                playSound,
                titleMode,
                titleColour,
                duration
        );
    }

    private void doTitleAndSound(boolean self, String content, boolean silent, boolean noTitle) {
        if (!noTitle && (titleMode.is("Self") && self || titleMode.is("Others") && !self || titleMode.is("All"))) Hud.showTitle(content, titleColour.getValue(), this.duration.getValue().longValue(), true);
        if (!silent && (soundMode.is("Self") && self || soundMode.is("Others") && !self || soundMode.is("All"))) playSound();
    }

    private void playSound() {
        if (mc.player == null) return;
        Optional<Holder.Reference<SoundEvent>> event = BuiltInRegistries.SOUND_EVENT.get(Identifier.withDefaultNamespace(this.sound.getValue()));
        if (event.isEmpty()) return;
        mc.player.playSound(event.get().value(), volume.getValue().floatValue(), pitch.getValue().floatValue());
    }

    @SubscribeEvent
    public void onTick(ClientTickEvent.Start event) {
        if ((!Location.getArea().is(Island.Dungeon) && !this.notDungeon.getValue())
                || Dungeon.isInBoss() && !this.bossMsg.getValue()
                || !Dungeon.isInBoss() && !this.clearMsg.getValue()
        ) return;

        final long now = System.currentTimeMillis();

        if (Dungeon.isInBoss()) {
            List<Msg> msgs = boss.getValue().get(String.valueOf(Location.fakeFloor()));
            for (Msg msg : msgs) {
                if (allPlayers.getValue()) {
                    boolean anyPlayerInMsg = false;
                    for (DungeonPlayer player : getPlayers()) {
                        if (inside(msg, player.getPlayer())) {
                            boolean me = Objects.equals(player.getName(), mc.player.getName().getString());
                            if (!me && msg.self && !msg.others || me && msg.others && !msg.self) continue;

                            anyPlayerInMsg = true;
                            if (msg.active && msg.lastSent < now - resendDelay.getValue().longValue()) {
                                msg.active = false;
                                msg.lastSent = now;
                                String m = format(msg.message, player.getName());

                                if (me) {
                                    send(m);
                                    doTitleAndSound(false, m, msg.silent, msg.noTitle);
                                } else {
                                    if (!noOthersChat.getValue()) send(m);
                                    doTitleAndSound(false, m, msg.silent, msg.noTitle);
                                }
                            }
                            break;
                        }
                    }
                    if (!anyPlayerInMsg) {
                        msg.active = true;
                    }
                } else {
                    if (inside(msg, mc.player) && msg.self) {
                        if (msg.active) {
                            msg.active = false;
                            String m = format(msg.message, mc.player.getName().getString());
                            send(m);
                            doTitleAndSound(true, m, msg.silent, msg.noTitle);
                        }
                        break;
                    } else {
                        msg.active = true;
                    }
                }
            }
        } else {
            if (this.allPlayers.getValue()) {
                Set<DungeonPlayer> players = getPlayers();
                for (DungeonPlayer player : players) {
                    Room room = ScanUtils.getRoomFromPos((int) player.getPlayer().getX(), (int) player.getPlayer().getZ());
                    if (room == null) continue;
                    DataStore store = room.getUniqueRoom().getData().get("posmsg");
                    if (store == null) continue;
                    List<Msg> msgs = store.get(room.getData().name());
                    if (msgs == null || msgs.isEmpty()) continue;

                    for (Msg msg : msgs) {
                        if (!inside(msg, player.getPlayer())) continue;
                        boolean me = Objects.equals(player.getName(), mc.player.getName().getString());
                        if (!me && msg.self && !msg.others || me && msg.others && !msg.self) continue;

                        if (msg.active && msg.lastSent < now - resendDelay.getValue().longValue()) {
                            activeMsgs.add(msg);
                            msg.active = false;
                            msg.lastSent = now;
                            String m = format(msg.message, player.getName());

                            if (me) {
                                send(m);
                                doTitleAndSound(true, m, msg.silent, msg.noTitle);
                            } else {
                                if (!noOthersChat.getValue()) send(m);
                                doTitleAndSound(false, m, msg.silent, msg.noTitle);
                            }
                        }
                        break;
                    }
                }

                Iterator<Msg> it = activeMsgs.iterator();
                while (it.hasNext()) {
                    Msg msg = it.next();
                    boolean stillInside = false;
                    for (DungeonPlayer dungeonPlayer : players) {
                        if (inside(msg, dungeonPlayer.getPlayer())) {
                            stillInside = true;
                            break;
                        }
                    }
                    if (!stillInside) {
                        msg.active = true;
                        it.remove();
                    }
                }
            } else {
                for (Msg msg : currentRenderMsgs) {
                    if (inside(msg, mc.player) && msg.self) {
                        if (msg.active && msg.lastSent < now - resendDelay.getValue().longValue()) {
                            msg.active = false;
                            msg.lastSent = now;
                            String m = format(msg.message, mc.player.getName().getString());
                            send(m);
                            doTitleAndSound(true, m, msg.silent, msg.noTitle);
                        }
                        return;
                    } else {
                        msg.active = true;
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public void onRender(Render3DEvent.Extract event) {
        if ((!Location.getArea().is(Island.Dungeon) && !this.notDungeon.getValue()) || this.noRender.getValue() || Dungeon.isInBoss() && !this.bossMsg.getValue() || !Dungeon.isInBoss() && !this.clearMsg.getValue()) return;
        float line = lineWidth.getValue().floatValue();
        for (Msg msg : currentRenderMsgs) {
            if (mc.player.distanceToSqr((msg.tLower == null ? msg.lower : msg.tLower).asVec3()) > renderDistance.getValue().intValue() * renderDistance.getValue().intValue()) continue;
            Renderer3D.addTask(new Rectangle(msg.getTranslatedAABB(), msg.active ? active.getValue() : inactive.getValue(), line, renderDepth.getValue()));
        }
    }

    private void send(String content) {
        if (mc.getConnection() == null) return;
        if (this.partyChat.getValue()) {
            mc.getConnection().sendCommand("pc " + content);
        } else {
            mc.getConnection().sendChat(content);
        }
    }

    private String format(String message, String player) {
        return Formatter.format((player.equals(mc.player.getName().getString()) ? selfFormat : otherFormat).getValue(), Map.of("{player}", player, "{message}", message));
    }

    private static void onClearLoad() {
        Room room = com.ricedotwho.rsm.component.impl.map.Map.getCurrentRoom();
        if (room == null) return;
        String name = room.getData().name();
        currentRenderMsgs = clear.getValue().computeIfAbsent(name, k -> new ArrayList<>());
        UniqueRoom uni = room.getUniqueRoom();
        currentRenderMsgs.forEach(msg -> {
            msg.active = true;
            msg.setTranslated(
                    translateTo(msg.upper, uni.getMainRoom()),
                    translateTo(msg.lower, uni.getMainRoom())
            );
        });
        updateClearPosmsg(uni);
        updateCurrentRenderMessages(uni);
    }

    @SubscribeEvent
    public void onRoomScanned(DungeonEvent.RoomScanned event) {
        updateClearPosmsg(event.getUnique());
    }

    public static void updateClearPosmsg(UniqueRoom uni) {
        List<Msg> data = clear.getValue().getOrDefault(uni.getName(), Collections.emptyList());

        data.forEach(msg -> {
            msg.active = true;
            msg.setTranslated(
                    translateTo(msg.upper, uni.getMainRoom()),
                    translateTo(msg.lower, uni.getMainRoom())
            );
        });

        uni.getData().computeIfAbsent("posmsg", k -> new DataStore()).put(uni.getName(), data);
    }

    @SubscribeEvent
    public void onDungeonEnd(DungeonEvent.End event) {
        clear.getValue().forEach((k, v) -> v.forEach(Msg::reset));
    }

    private static Pos translateTo(Pos in, Room theRoom) {
        if (Dungeon.isInBoss() || theRoom == null) return in;
        Room room = theRoom.getUniqueRoom().getMainRoom();
        return RoomUtils.getRealPositionFixed(in, room);
    }

    private static Pos translateFrom(Pos in) {
        return translateFrom(in, com.ricedotwho.rsm.component.impl.map.Map.getCurrentRoom());
    }

    private static Pos translateFrom(Pos in, Room theRoom) {
        if (Dungeon.isInBoss() || theRoom == null) return in;
        Room room = theRoom.getUniqueRoom().getMainRoom();
        Pos pos = in.subtract(room.getX(), 0, room.getZ());
        return RoomUtils.getRelativePositionFixed(pos, room);
    }

    private Set<DungeonPlayer> getPlayers() {
        if(!Location.getArea().is(Island.Dungeon) && this.notDungeon.getValue() || Dungeon.getPlayers().isEmpty()) return Set.of(new DungeonPlayer(null, mc.player, 0, 0));
        return Dungeon.getPlayers();
    }

    @SubscribeEvent
    public void onRoomChange(DungeonEvent.ChangeRoom event) {
        if (Dungeon.isInBoss()) return; // schizophrenia
        UniqueRoom uni = event.getRoom().getUniqueRoom();
        updateCurrentRenderMessages(uni);
    }

    @SubscribeEvent
    public void onEnterBoss(DungeonEvent.EnterBoss event) {
        updateCurrentRenderMessageForBoss();
    }

    private static void updateCurrentRenderMessageForBoss() {
        String name = String.valueOf(Location.fakeFloor());
        currentRenderMsgs = boss.getValue().computeIfAbsent(name, k -> new ArrayList<>());
        currentRenderMsgs.forEach(msg -> {
            msg.active = true;
            msg.tLower = msg.lower.copy();
            msg.tUpper = msg.upper.copy();
        });
    }

    public static void updateCurrentRenderMessages(UniqueRoom uni) {
        DataStore dataStore = uni.getData().get("posmsg");
        if (dataStore == null) {
            currentRenderMsgs = new ArrayList<>();
            return;
        }
        currentRenderMsgs = new ArrayList<>(dataStore.get(uni.getName()));
    }

    public boolean inside(Msg msg, Player player) {
        return this.inside(
                player.position(),
                player.oldPosition(),
                msg
        );
    }

    public boolean inside(Vec3 curr, Vec3 prev, Msg msg) {
        AABB bb = msg.getTranslatedAABB();
        AABB feet = new AABB(curr.x - 0.2, curr.y, curr.z - 0.2, curr.x + 0.3, curr.y + 0.5, curr.z);
        boolean intercept = bb.clip(curr, prev).isPresent();
        boolean intersects = bb.intersects(feet);
        return intercept || intersects;
    }

    public static boolean add(Msg real) {
        Msg msg = new Msg(
                translateFrom(real.upper),
                translateFrom(real.lower),
                real.others,
                real.self,
                real.silent,
                real.noTitle,
                real.message
        );

        if (Dungeon.isInBoss()) {
            String name = String.valueOf(Location.fakeFloor());
            List<Msg> data = boss.getValue().computeIfAbsent(name, k -> new ArrayList<>());
            msg.tLower = msg.lower.copy();
            msg.tUpper = msg.upper.copy();
            add(msg, data);
            boss.save();
            updateCurrentRenderMessageForBoss();
        } else {
            Room room = com.ricedotwho.rsm.component.impl.map.Map.getCurrentRoom();
            if (room == null) return false;
            String name = room.getData().name();
            List<Msg> data = clear.getValue().computeIfAbsent(name, k -> new ArrayList<>());

            UniqueRoom uni = room.getUniqueRoom();
            msg.setTranslated(
                    translateTo(msg.upper, uni.getMainRoom()),
                    translateTo(msg.lower, uni.getMainRoom())
            );
            add(msg, data);
            clear.save();
            updateClearPosmsg(room.getUniqueRoom());
            updateCurrentRenderMessages(room.getUniqueRoom());
        }
        return true;
    }

    private static void add(Msg msg, List<Msg> data) {
        if (contains(msg.message, data)) {
            remove(msg.message, data);
            data.add(msg);
        } else {
            data.add(msg);
        }
    }

    private static boolean contains(String msg, List<Msg> data) {
        for (Msg s : data) {
            if(s.message.equalsIgnoreCase(msg)) {
                return true;
            }
        }
        return false;
    }

    public static boolean remove(String msg) {
        boolean ret;
        if (Dungeon.isInBoss()) {
            String name = String.valueOf(Location.fakeFloor());
            List<Msg> data = boss.getValue().computeIfAbsent(name, k -> new ArrayList<>());
            ret = remove(msg, data);
            boss.save();
            updateCurrentRenderMessageForBoss();
        } else {
            Room room = com.ricedotwho.rsm.component.impl.map.Map.getCurrentRoom();
            if (room == null) return false;
            String name = room.getData().name();
            List<Msg> data = clear.getValue().computeIfAbsent(name, k -> new ArrayList<>());
            ret = remove(msg, data);
            clear.save();
            updateClearPosmsg(room.getUniqueRoom());
            updateCurrentRenderMessages(room.getUniqueRoom());
        }
        return ret;
    }

    public static boolean remove(String msg, List<Msg> data) {
        for (Msg s : data) {
            if(s.message.equalsIgnoreCase(msg)) {
                data.remove(s);
                return true;
            }
        }
        return false;
    }

    public static void clear() {
        if (Dungeon.isInBoss()) {
            String name = String.valueOf(Location.fakeFloor());
            List<Msg> data = boss.getValue().computeIfAbsent(name, k -> new ArrayList<>());
            data.clear();
            boss.save();
            updateCurrentRenderMessageForBoss();
        } else {
            Room room = com.ricedotwho.rsm.component.impl.map.Map.getCurrentRoom();
            if (room == null) return;
            String name = room.getData().name();
            List<Msg> data = clear.getValue().computeIfAbsent(name, k -> new ArrayList<>());
            data.clear();
            clear.save();
            updateClearPosmsg(room.getUniqueRoom());
            updateCurrentRenderMessages(room.getUniqueRoom());
        }
    }

    public static List<Msg> getMsgs() {
        if (Dungeon.isInBoss()) {
            String name = String.valueOf(Location.fakeFloor());
            return boss.getValue().computeIfAbsent(name, k -> new ArrayList<>());
        } else {
            Room room = com.ricedotwho.rsm.component.impl.map.Map.getCurrentRoom();
            if (room == null) return new ArrayList<>();
            String name = room.getData().name();
            return clear.getValue().computeIfAbsent(name, k -> new ArrayList<>());
        }
    }

    public static class Msg {
        public final Pos upper;
        public final Pos lower;
        public final String message;
        public final boolean self;
        public final boolean others;
        public final boolean silent;
        public final boolean noTitle;

        public transient Pos tUpper = new Pos();
        public transient Pos tLower = new Pos();
        public transient boolean active = false;
        public transient long lastSent = 0;
        public transient int playersInside = 0;

        public Msg(Pos upper, Pos lower, boolean self, boolean others, boolean silent, boolean noTitle, String message) {
            this.upper = upper;
            this.lower = lower;
            this.self = self;
            this.others = others;
            this.silent = silent;
            this.noTitle = noTitle;
            this.message = message;
        }

        public void setTranslated(Pos up, Pos low) {
            this.tLower = new Pos(
                    Math.min(low.x(), up.x()),
                    Math.min(low.y(), up.y()),
                    Math.min(low.z(), up.z())
            );
            this.tUpper = new Pos(
                    Math.max(up.x(), low.x()),
                    Math.max(up.y(), low.z()),
                    Math.max(up.z(), low.z())
            );
        }

        public AABB getTranslatedAABB() {
            return new AABB(tLower.x(), tLower.y(), tLower.z(), tUpper.x(), tUpper.y(), tUpper.z());
        }

        public void reset() {
            lastSent = 0;
            playersInside = 0;
            active = true;
            tUpper = new Pos();
            tLower = new Pos();
        }
    }
}
