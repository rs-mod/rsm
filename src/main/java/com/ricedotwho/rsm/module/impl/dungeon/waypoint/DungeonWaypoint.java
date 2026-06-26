package com.ricedotwho.rsm.module.impl.dungeon.waypoint;

import com.google.common.reflect.TypeToken;
import com.ricedotwho.rsm.RSM;
import com.ricedotwho.rsm.component.impl.Renderer3D;
import com.ricedotwho.rsm.component.impl.location.Island;
import com.ricedotwho.rsm.component.impl.location.Location;
import com.ricedotwho.rsm.component.impl.map.handler.Dungeon;
import com.ricedotwho.rsm.component.impl.map.map.Room;
import com.ricedotwho.rsm.component.impl.map.map.UniqueRoom;
import com.ricedotwho.rsm.component.impl.map.utils.RoomUtils;
import com.ricedotwho.rsm.data.Colour;
import com.ricedotwho.rsm.data.DataStore;
import com.ricedotwho.rsm.data.Pos;
import com.ricedotwho.rsm.event.api.SubscribeEvent;
import com.ricedotwho.rsm.event.impl.game.ClientTickEvent;
import com.ricedotwho.rsm.event.impl.game.DungeonEvent;
import com.ricedotwho.rsm.event.impl.game.SecretPickupEvent;
import com.ricedotwho.rsm.event.impl.render.Render3DEvent;
import com.ricedotwho.rsm.event.impl.world.BlockChangeEvent;
import com.ricedotwho.rsm.event.impl.world.WorldEvent;
import com.ricedotwho.rsm.module.Module;
import com.ricedotwho.rsm.module.api.Category;
import com.ricedotwho.rsm.module.api.ModuleInfo;
import com.ricedotwho.rsm.ui.clickgui.settings.impl.BooleanSetting;
import com.ricedotwho.rsm.ui.clickgui.settings.impl.ColourSetting;
import com.ricedotwho.rsm.ui.clickgui.settings.impl.SaveSetting;
import com.ricedotwho.rsm.utils.FileUtils;
import com.ricedotwho.rsm.utils.Utils;
import com.ricedotwho.rsm.utils.api.HyApi;
import com.ricedotwho.rsm.utils.render.render3d.type.OutlineBox;
import lombok.Getter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.*;

@Getter
@ModuleInfo(aliases = "Dungeon Wp", id = "DungeonWaypoint", category = Category.DUNGEONS)
public class DungeonWaypoint extends Module {
    private static final BooleanSetting useOnline = new BooleanSetting("Use Online", true);
    private final BooleanSetting showPrince = new BooleanSetting("Show Prince", false);

    private static final SaveSetting<Map<String, Set<Secret>>> waypoints = new SaveSetting<>(
            "Waypoints",
            "dungeon/waypoints",
            "default.json",
            HashMap::new,
            new TypeToken<Map<String, Set<Secret>>>() {}.getType(),
            true,
            true,
            null,
            () -> !useOnline.getValue()
    );

    private final ColourSetting chest = new ColourSetting("Chest", Colour.GREEN.copy());
    private final ColourSetting item = new ColourSetting("Item", Colour.BLUE.copy());
    private final ColourSetting lever = new ColourSetting("Lever", new Colour(0, 200, 200));
    private final ColourSetting bat = new ColourSetting("Bat", Colour.CYAN.copy());
    private final ColourSetting essence = new ColourSetting("Essence", Colour.MAGENTA.copy());
    private final ColourSetting redstoneKey = new ColourSetting("Redstone Key", Colour.RED.brighter().copy());
    private final ColourSetting redstoneBlock = new ColourSetting("Redstone Block", Colour.RED.darker().copy());
    private final ColourSetting prince = new ColourSetting("Prince", Colour.YELLOW.copy());

    private static Set<Secret> currentRenderWaypoints = new HashSet<>();

    private static final String onlineURL = "https://raw.githubusercontent.com/ricedotwho/data/refs/heads/main/default.json";
    private static Map<String, Set<Secret>> onlineWaypoints = new HashMap<>();

    private static final AABB FULL = new AABB(0, 0, 0, 1, 1, 1);
    private static final AABB CHEST = new AABB(0.0625, 0, 0.0625, 0.9375, 0.9375, 0.9375);
    private static final AABB SKULL = new AABB(0.25, 0, 0.25, 0.75, 0.5, 0.75);

    public DungeonWaypoint() {
        this.registerProperty(
                useOnline,
                showPrince,
                waypoints,
                chest,
                item,
                lever,
                bat,
                essence,
                redstoneKey,
                redstoneBlock,
                prince
        );
        try {
            onlineWaypoints = FileUtils.getGson().fromJson(new HyApi().simpleGet(onlineURL), new TypeToken<Map<String, Set<Secret>>>(){}.getType());
            if (onlineWaypoints == null) {
                onlineWaypoints = new HashMap<>();
            }
        } catch (Exception e) {
            RSM.getLogger().error("Failed to get online waypoints!", e);
            onlineWaypoints = new HashMap<>();
        }
    }

    @SubscribeEvent
    public void onLoad(WorldEvent.Load event) {
        currentRenderWaypoints.clear();
    }

    @SubscribeEvent
    private void onRoomScanned(DungeonEvent.RoomScanned event) {
        updateWaypoints(event.getUnique());
    }

    private static Map<String, Set<Secret>> getWaypoints() {
        if (useOnline.getValue()) {
            return onlineWaypoints;
        }
        return waypoints.getValue();
    }

    public static void updateWaypoints(UniqueRoom uni) {
        Set<Secret> data = getWaypoints().getOrDefault(uni.getName(), Collections.emptySet());
        Room room = uni.getMainRoom();
        assert mc.level != null;

        data.forEach(secret -> {
            Pos translated = RoomUtils.getRealPositionFixed(secret.getPos(), room);
            BlockPos bp = translated.asBlockPos();
            VoxelShape shape = mc.level.getBlockState(bp).getShape(mc.level, bp);
            AABB aabb = (shape.isEmpty() ? getBoundsForType(secret.getType()) : shape.bounds()).move(bp);

            secret.setFound(false);
            secret.setRenderBox(aabb);
            secret.setTranslated(translated);
        });
        uni.getData().computeIfAbsent("secret_waypoints", k -> data);
    }

    private static AABB getBoundsForType(SecretType type) {
        return switch (type) {
            case REDSTONE_KEY, BAT, ESSENCE, LEVER -> SKULL;
            case CHEST -> CHEST;
            default -> FULL;
        };
    }

    @SubscribeEvent
    private void onRoomChange(DungeonEvent.ChangeRoom event) {
        if (Dungeon.isInBoss()) return;
        updateCurrentWaypoints(event.getRoom().getUniqueRoom());
    }

    public static void updateCurrentWaypoints(UniqueRoom uni) {
        Set<Secret> temp = uni.getData().get("secret_waypoints");
        if (temp == null) {
            currentRenderWaypoints = new HashSet<>();
            return;
        }
        currentRenderWaypoints = temp;
    }

    private Colour getColour(SecretType type) {
        return switch (type) {
            case CHEST -> chest.getValue();
            case ITEM -> item.getValue();
            case LEVER -> lever.getValue();
            case BAT -> bat.getValue();
            case ESSENCE -> essence.getValue();
            case REDSTONE_KEY -> redstoneKey.getValue();
            case REDSTONE_BLOCK -> redstoneBlock.getValue();
            case PRINCE -> prince.getValue();
        };
    }

    @SubscribeEvent
    private void onBlockChange(BlockChangeEvent event) {
        if (!Location.getArea().is(Island.Dungeon) || Dungeon.isInBoss() || currentRenderWaypoints.isEmpty() || mc.level == null) return;
        for (Secret secret : currentRenderWaypoints) {
            if (secret.getTranslated() == null || secret.getTranslated().equals(event.getPos())) continue;
            BlockPos bp = secret.getTranslated().asBlockPos();
            VoxelShape shape = mc.level.getBlockState(bp).getShape(mc.level, bp);
            AABB aabb = (shape.isEmpty() ? getBoundsForType(secret.getType()) : shape.bounds()).move(bp);
            secret.setRenderBox(aabb);
            return;
        }
    }

    @SubscribeEvent
    private void onTick(ClientTickEvent.Start event) {
        if (!Location.getArea().is(Island.Dungeon) || Dungeon.isInBoss() || currentRenderWaypoints.isEmpty() || mc.level == null || event.getTime() % 5 != 0) return;
        for (Secret secret : currentRenderWaypoints) {
            BlockPos bp = secret.getTranslated().asBlockPos();
            VoxelShape shape = mc.level.getBlockState(bp).getShape(mc.level, bp);
            AABB aabb = (shape.isEmpty() ? getBoundsForType(secret.getType()) : shape.bounds()).move(bp);
            secret.setRenderBox(aabb);
        }
    }

    @SubscribeEvent
    private void onRender(Render3DEvent.Extract event) {
        if (!Location.getArea().is(Island.Dungeon) || Dungeon.isInBoss() || currentRenderWaypoints.isEmpty()) return;
        currentRenderWaypoints.forEach(s -> {
            if (!s.isFound() && (s.getType() == SecretType.PRINCE ? this.showPrince.getValue() : true)) {
                Renderer3D.addTask(new OutlineBox(s.getRenderBox(), getColour(s.getType()), false));
            }
        });
    }

    public static boolean add(Secret secret) {
        Room room = com.ricedotwho.rsm.component.impl.map.Map.getCurrentRoom();
        if (room == null) return false;
        String name = room.getData().name();
        Set<Secret> data = waypoints.getValue().computeIfAbsent(name, k -> new HashSet<>());

        Pos translated = RoomUtils.getRealPositionFixed(secret.getPos(), room.getUniqueRoom().getMainRoom());
        BlockPos bp = translated.asBlockPos();
        VoxelShape shape = mc.level.getBlockState(bp).getShape(mc.level, bp);
        AABB aabb = (shape.isEmpty() ? getBoundsForType(secret.getType()) : shape.bounds()).move(bp);

        secret.setFound(false);
        secret.setRenderBox(aabb);
        secret.setTranslated(translated);

        data.add(secret);
        waypoints.save();
        updateWaypoints(room.getUniqueRoom());
        updateCurrentWaypoints(room.getUniqueRoom());
        return true;
    }

    public static boolean removeClosest(SecretType type) {
        Room room = com.ricedotwho.rsm.component.impl.map.Map.getCurrentRoom();
        if (room == null) return false;
        String name = room.getData().name();
        Set<Secret> data = waypoints.getValue().computeIfAbsent(name, k -> new HashSet<>());
        Pos player = RoomUtils.getRelativePositionFixed(new Pos(mc.player.position()), com.ricedotwho.rsm.component.impl.map.Map.getCurrentRoom().getUniqueRoom().getMainRoom());
        Secret secret = getClosest(player, type, data);
        if (secret == null) return false;
        boolean ret = data.remove(secret);
        waypoints.save();
        updateWaypoints(room.getUniqueRoom());
        updateCurrentWaypoints(room.getUniqueRoom());
        return ret;
    }

    public static boolean shiftClosest(SecretType type, Direction dir, double amount) {
        Room room = com.ricedotwho.rsm.component.impl.map.Map.getCurrentRoom();
        if (room == null) return false;
        String name = room.getData().name();
        Set<Secret> data = waypoints.getValue().computeIfAbsent(name, k -> new HashSet<>());
        Pos player = RoomUtils.getRelativePositionFixed(new Pos(mc.player.position()), com.ricedotwho.rsm.component.impl.map.Map.getCurrentRoom().getUniqueRoom().getMainRoom());
        Secret secret = getClosest(player, type, data);
        if (secret == null) return false;
        secret.getPos().shiftSelf(dir, amount);
        waypoints.save();
        updateWaypoints(room.getUniqueRoom());
        updateCurrentWaypoints(room.getUniqueRoom());
        return true;
    }

    private Pos shift(Pos pos, Direction dir, double amount) {
        return switch (dir) {
            case UP -> pos.add(0, amount, 0);
            case DOWN -> pos.add(0, -amount, 0);
            case WEST -> pos.add(-amount, 0, 0);
            case SOUTH -> pos.add(0, 0, amount);
            case NORTH -> pos.add(0, 0, -amount);
            case EAST -> pos.add(amount, 0, 0);
            case null -> pos;
        };
    }

    private static Secret getClosest(Pos player, SecretType type, Set<Secret> set) {
        Secret closest = null;
        double maxDist = Integer.MAX_VALUE;

        for (Secret s : set) {
            if (s.getType() != type) continue;
            double d = player.squaredDistanceTo(s.getPos());
            if (d < maxDist) {
                maxDist = d;
                closest = s;
            }
        }

        return closest;
    }

    public static boolean remove(Pos pos, SecretType type) {
        boolean ret;
        Room room = com.ricedotwho.rsm.component.impl.map.Map.getCurrentRoom();
        if (room == null) return false;
        String name = room.getData().name();
        Set<Secret> data = waypoints.getValue().computeIfAbsent(name, k -> new HashSet<>());
        ret = remove(pos, type, data);
        waypoints.save();
        updateWaypoints(room.getUniqueRoom());
        updateCurrentWaypoints(room.getUniqueRoom());
        return ret;
    }

    public static boolean remove(Pos pos, SecretType type, Set<Secret> data) {
        for (Secret s : data) {
            if (s.getPos().equals(pos) &&  s.getType() == type) {
                data.remove(s);
                return true;
            }
        }
        return false;
    }

    public static void clear() {
        Room room = com.ricedotwho.rsm.component.impl.map.Map.getCurrentRoom();
        if (room == null) return;
        String name = room.getData().name();
        Set<Secret> data = waypoints.getValue().computeIfAbsent(name, k -> new HashSet<>());
        data.clear();
        waypoints.save();
        updateWaypoints(room.getUniqueRoom());
        updateCurrentWaypoints(room.getUniqueRoom());
    }

    @SubscribeEvent
    private void onSecretPickup(SecretPickupEvent event) {
        Room room = com.ricedotwho.rsm.component.impl.map.Map.getCurrentRoom();
        if (room == null) return;
        Pos pos = RoomUtils.getRelativePositionFixed(event.getPos(), room.getUniqueRoom().getMainRoom());
        switch (event.getType()) {
            case ESSENCE, LEVER, CHEST -> {
                Secret secret = getByPos(pos, event.getType());
                if (secret != null) secret.setFound(true);
            }
            case REDSTONE_KEY -> {
                for (Secret s : currentRenderWaypoints) {
                    if (!s.isFound() && s.getType() == SecretType.REDSTONE_KEY) {
                        s.setFound(true);
                    }
                }
            }
            case REDSTONE_BLOCK -> {
                for (Secret s : currentRenderWaypoints) {
                    if (!s.isFound() && Utils.equalsOneOf(SecretType.REDSTONE_KEY, SecretType.REDSTONE_BLOCK)) {
                        s.setFound(true);
                    }
                }
            }
            case ITEM, BAT -> {
                Secret secret = getClosest(pos, event.getType(), currentRenderWaypoints);
                if (secret != null && secret.getTranslated().squaredDistanceTo(mc.player.position()) < 16 * 16) secret.setFound(true);
            }
        }
    }

    private Secret getByPos(Pos pos, SecretType type) {
        for (Secret s : currentRenderWaypoints) {
            if (!s.isFound() && s.getPos().equals(pos) && s.getType() == type) {
                return s;
            }
        }
        return null;
    }
}
