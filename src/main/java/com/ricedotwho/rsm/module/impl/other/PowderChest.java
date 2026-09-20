package com.ricedotwho.rsm.module.impl.other;

import com.ricedotwho.rsm.event.api.SubscribeEvent;
import com.ricedotwho.rsm.event.impl.client.PacketEvent;
import com.ricedotwho.rsm.event.impl.game.ChatEvent;
import com.ricedotwho.rsm.event.impl.render.Render3DEvent;
import com.ricedotwho.rsm.event.impl.world.BlockChangeEvent;
import com.ricedotwho.rsm.event.impl.world.WorldEvent;
import com.ricedotwho.rsm.location.Island;
import com.ricedotwho.rsm.location.Location;
import com.ricedotwho.rsm.managers.EventDispatcher;
import com.ricedotwho.rsm.managers.WorldRenderer;
import com.ricedotwho.rsm.module.api.Category;
import com.ricedotwho.rsm.module.api.Module;
import com.ricedotwho.rsm.module.api.ModuleInfo;
import com.ricedotwho.rsm.module.api.settings.impl.BooleanSetting;
import com.ricedotwho.rsm.module.api.settings.impl.ColorSetting;
import com.ricedotwho.rsm.type.Color;
import com.ricedotwho.rsm.utils.ChatUtils;
import lombok.Getter;
import net.minecraft.client.Camera;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ClientboundSystemChatPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.*;

@Getter
@ModuleInfo(aliases = "Powder Chest", id = "powder-chest", category = Category.OTHER)
public class PowderChest extends Module {
    @SuppressWarnings("unused")
    private static final PowderChest instance = new PowderChest();

    private final ColorSetting color = new ColorSetting("Colour", Color.GREEN.withAlpha(0.35f));
    private final BooleanSetting tracer = new BooleanSetting("Tracer", false);

    private static final AABB CHEST = new AABB(0.0625, 0, 0.0625, 0.9375, 0.875, 0.9375);
    private final LinkedHashMap<BlockPos, Entry> chests = new LinkedHashMap<>();
    private final List<Long> expecting = new ArrayList<>();

    @SubscribeEvent
    public void onLoad(WorldEvent.Load event) {
        chests.clear();
        expecting.clear();
    }

    @SubscribeEvent
    public void onChat(ChatEvent.Chat event) {
        if (!Location.getArea().is(Island.CrystalHollows)) return;
        if (event.getString().equals("You uncovered a treasure chest!")) {
            expecting.add(EventDispatcher.getServerTickTime());
        }
    }

    @SubscribeEvent
    public void onUseItemOn(PacketEvent.Send event, ServerboundUseItemOnPacket packet) {
        if (!Location.getArea().is(Island.CrystalHollows) || chests.isEmpty()) return;
        var pos = packet.getHitResult().getBlockPos();
        chests.remove(pos);
    }

    @SubscribeEvent
    public void onBlockChanged(BlockChangeEvent event) {
        if (!Location.getArea().is(Island.CrystalHollows)) return;
        if (!expecting.isEmpty() && event.getNewState().is(Blocks.CHEST) && !event.getOldState().is(Blocks.CHEST)) {
            var time = EventDispatcher.getServerTickTime();
            expecting.removeIf(it -> time - it > 3);
            var distance = mc.player.distanceToSqr(event.getVec3());
            if (!expecting.isEmpty() && distance < 256) {
                expecting.removeFirst();
                var pos = event.getBlockPos();
                chests.put(pos, new Entry(pos));
            }
        } else if (event.getOldState().is(Blocks.CHEST) && !event.getNewState().is(Blocks.CHEST)) {
            chests.remove(event.getBlockPos());
        }
    }

    @SubscribeEvent
    public void onRender(Render3DEvent.Extract event) {
        if (!Location.getArea().is(Island.CrystalHollows) || chests.isEmpty()) return;
        var camera = event.getContext().camera();
        chests.values().forEach(this::box);

        if (this.tracer.getValue()) {
            var first = chests.firstEntry().getValue();
            Vec3 start = camera.position().add(Vec3.directionFromRotation(camera.xRot(), camera.yRot()));
            WorldRenderer.line(start, first.center, color.getValue(), color.getValue(), false);
        }
    }

    private void box(Entry entry) {
        WorldRenderer.filledBox(entry.aabb, color.getValue(), false);
    }

    private record Entry(AABB aabb, Vec3 center) {
        public Entry(BlockPos pos) {
            var var1 = CHEST.move(pos);
            this(var1, var1.center);
        }
    }
}
