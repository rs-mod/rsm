package com.ricedotwho.rsm.module.impl.render;

import com.google.gson.reflect.TypeToken;
import com.ricedotwho.rsm.event.api.SubscribeEvent;
import com.ricedotwho.rsm.event.impl.game.TickEvent;
import com.ricedotwho.rsm.event.impl.render.Render3DEvent;
import com.ricedotwho.rsm.location.Island;
import com.ricedotwho.rsm.location.Location;
import com.ricedotwho.rsm.managers.dungeon.Dungeon;
import com.ricedotwho.rsm.managers.dungeon.Phase7;
import com.ricedotwho.rsm.module.api.Category;
import com.ricedotwho.rsm.module.api.Module;
import com.ricedotwho.rsm.module.api.ModuleInfo;
import com.ricedotwho.rsm.module.api.settings.group.DefaultGroupSetting;
import com.ricedotwho.rsm.module.api.settings.impl.*;
import com.ricedotwho.rsm.type.Color;
import com.ricedotwho.rsm.utils.DungeonUtils;
import lombok.Getter;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ambient.Bat;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.monster.Giant;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@ModuleInfo(aliases = "Esp", id = "esp", category = Category.RENDER)
public class Esp extends Module {
    @Getter
    @SuppressWarnings("FieldMayBeFinal")
    private static Esp instance = new Esp();

    private final DefaultGroupSetting colours = new DefaultGroupSetting("Colours", this);
    private final DualColorSetting starred = new DualColorSetting("Starred", Color.fromHex(0xFFD600FF).withAlpha(0.35f), Color.fromHex(0xFFD600FF), true, false);
    private final DualColorSetting blood = new DualColorSetting("Blood", Color.fromHex(0xFFFF0000).withAlpha(0.35f), Color.fromHex(0xFFFF0000), true, false);
    private final DualColorSetting wither = new DualColorSetting("Wither", Color.fromHex(0xFF0066FF).withAlpha(0.35f), Color.fromHex(0xFF0066FF), true, false);
    private final DualColorSetting bat = new DualColorSetting("Bat", Color.fromRGB(173, 92, 173, 0.35f), Color.fromRGB(173, 92, 173), true, false);
    private final DualColorSetting customColour = new DualColorSetting("Custom", Color.fromRGB(255, 255, 255, 0.35f), Color.fromRGB(255, 255, 255), true, false);

    private final NumberSetting<Integer> update = new NumberSetting<>("Update Interval", 1, 20, 5, 1);
    private final EnumSetSetting<Type> type = new EnumSetSetting<>("Mode", Type.class, List.of());
    protected final EnumSetting<RenderMode> renderMode = new EnumSetting<>("Render Type", RenderMode.FILLED_OUTLINE);

    @Getter
    private final SaveSetting<Set<String>> customEsp = new SaveSetting<>("Custom Esp", "render", "custom_esp.json", HashSet::new, new TypeToken<Set<String>>(){}.getType());
    @Getter
    private final SaveSetting<Set<String>> customTracer = new SaveSetting<>("Custom Tracer", "render", "esp_tracer.json", HashSet::new, new TypeToken<Set<String>>(){}.getType());
    private static final Set<String> BLOOD_MOBS = Set.of("Revoker", "Psycho", "Reaper", "Cannibal", "Mute", "Ooze", "Putrid", "Freak", "Leech", "Tear", "Parasite", "Flamer", "Skull", "Mr. Dead", "Vader", "Frost", "Walker", "Wandering Soul", "Bonzo", "Scarf", "Livid", "Spirit Bear");
    protected final List<Tracked> tracked = new ArrayList<>();

    public Esp() {
        colours.add(
                starred,
                blood,
                wither,
                bat,
                customColour
        );
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientEnd event) {
        if (mc.level == null || event.getTime() % update.getValue() != 0) return;
        tracked.clear();
        var dungeon = Location.getArea().is(Island.Dungeon);
        var boss = Dungeon.isInBoss();
        boolean starred = !boss && type.contains(Type.STARRED),
                blood = !boss && type.contains(Type.BLOOD),
                wither = boss && type.contains(Type.WITHER) && !DungeonUtils.isPhase(Phase7.P5),
                bat = !boss && type.contains(Type.BAT),
                custom = type.contains(Type.CUSTOM);
        mc.level.entitiesForRendering().forEach(it -> {
            if (dungeon) {
                switch (it) {
                    case ArmorStand e when starred -> {
                        if (!e.hasCustomName()) return;
                        var name = e.getCustomName().getString().stripFormatting();
                        if (!name.contains("✯ ") || !name.endsWith("❤")) return;
                        var mob = getMobEntity(e);
                        if (mob != null && !mob.isDeadOrDying()) tracked.add(new Tracked(Type.STARRED, getMobEntity(e)));
                    }
                    case Entity e when custom && !(e instanceof ArmorStand) -> {
                        var name = it.getName().getString();
                        if (customEsp.getValue().contains(name)) {
                            tracked.add(new Tracked(Type.CUSTOM, it));
                        }
                        if (customTracer.getValue().contains(name)) {
                            tracked.add(new Tracked(Type.TRACER, it));
                        }
                    }
                    case RemotePlayer _ -> {
                        var name = it.getName().getString().trim();
                        if (starred && name.equals("Shadow Assassin")) {
                            tracked.add(new Tracked(Type.STARRED, it));
                        }

                        if (blood && BLOOD_MOBS.contains(name)) {
                            tracked.add(new Tracked(Type.BLOOD, it));
                        }
                    }
                    case Bat _ when bat && !it.isInvisible() -> tracked.add(new Tracked(Type.BAT, it));
                    case Giant _ when blood -> tracked.add(new Tracked(Type.BLOOD, it));
                    case WitherBoss e when wither && !e.isInvisible() && !e.isDeadOrDying() && e.getMaxHealth() != 300F && e.getScale() == 1F ->
                            tracked.add(new Tracked(Type.WITHER, it));
                    default -> {

                    }
                }
            } else if (custom && it instanceof Entity && !(it instanceof ArmorStand)) {
                var name = it.getName().getString();
                if (customEsp.getValue().contains(name)) {
                    tracked.add(new Tracked(Type.CUSTOM, it));
                }
                if (customTracer.getValue().contains(name)) {
                    tracked.add(new Tracked(Type.TRACER, it));
                }
            }
        });
    }

    @SubscribeEvent
    public void onRender(Render3DEvent.Extract event) {
        if (tracked.isEmpty()) return;
        var partialTicks = mc.getDeltaTracker().getGameTimeDeltaPartialTick(false);
        var camera = mc.gameRenderer.mainCamera();
        tracked.forEach(it -> {
            if (it.entity instanceof LivingEntity e && e.isDeadOrDying()) return;
            var interpolatedPos = it.entity.getPosition(partialTicks);
            var height = it.entity.getBbHeight();
            if (it.tracer) {
                var start = camera.position().add(Vec3.directionFromRotation(camera.xRot(), camera.yRot()));
                event.line(start, interpolatedPos.add(0, height / 2, 0), it.line, it.line, true);
            } else {
                float width = it.entity.getBbWidth();
                var aabb = new AABB(
                        interpolatedPos.x - width / 2, interpolatedPos.y,
                        interpolatedPos.z - width / 2, interpolatedPos.x + width / 2,
                        interpolatedPos.y + height, interpolatedPos.z + width / 2
                );

                switch (renderMode.getValue()) {
                    case RenderMode.FILLED -> event.filled(aabb, it.fill, true);
                    case RenderMode.OUTLINE -> event.outline(aabb, it.line, true);
                    case FILLED_OUTLINE -> event.filledOutline(aabb, it.fill, it.line, true);
                }
            }
        });
    }

    private LivingEntity getMobEntity(ArmorStand stand) {
        int offset = stand.getCustomName().getString().contains("Withermancer") ? 3 : 1;
        return (LivingEntity) mc.level.getEntity(stand.getId() - offset);
    }

    protected static Color c1(Type type) {
        return switch (type) {
            case STARRED -> instance.starred.getValue();
            case BLOOD -> instance.blood.getValue();
            case WITHER -> instance.wither.getValue();
            case BAT -> instance.bat.getValue();
            case CUSTOM, TRACER-> instance.customColour.getValue();
        };
    }

    protected static Color c2(Type type) {
        return switch (type) {
            case STARRED -> instance.starred.getOtherValue();
            case BLOOD -> instance.blood.getOtherValue();
            case WITHER -> instance.wither.getOtherValue();
            case BAT -> instance.bat.getOtherValue();
            case CUSTOM, TRACER -> instance.customColour.getOtherValue();
        };
    }

    protected record Tracked(boolean tracer, Color fill, Color line, Entity entity) {
        protected Tracked(Type type, Entity entity) {
            this(type == Type.TRACER, c1(type), c2(type),entity);
        }
    }

    protected enum Type {
        STARRED,
        BLOOD,
        WITHER,
        BAT,
        CUSTOM,
        TRACER
    }

    protected enum RenderMode {
        FILLED,
        OUTLINE,
        FILLED_OUTLINE
    }
}
