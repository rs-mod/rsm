package com.ricedotwho.rsm.module.impl.dungeon.puzzle;

import com.ricedotwho.rsm.component.impl.Renderer3D;
import com.ricedotwho.rsm.component.impl.camera.CameraHandler;
import com.ricedotwho.rsm.component.impl.map.map.Room;
import com.ricedotwho.rsm.data.Colour;
import com.ricedotwho.rsm.data.Pos;
import com.ricedotwho.rsm.event.api.SubscribeEvent;
import com.ricedotwho.rsm.event.impl.client.PacketEvent;
import com.ricedotwho.rsm.event.impl.game.DungeonEvent;
import com.ricedotwho.rsm.event.impl.render.Render3DEvent;
import com.ricedotwho.rsm.event.impl.world.WorldEvent;
import com.ricedotwho.rsm.module.SubModule;
import com.ricedotwho.rsm.module.api.SubModuleInfo;
import com.ricedotwho.rsm.ui.clickgui.settings.impl.ColourSetting;
import com.ricedotwho.rsm.utils.render.render3d.type.FilledBox;
import lombok.Getter;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

import java.security.interfaces.RSAKey;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@SubModuleInfo(name = "TPMaze", alwaysDisabled = false)
public class TPMaze extends SubModule<Puzzles> {

    private final ColourSetting confirmedColor = new ColourSetting("1 Solution", new Colour(0, 255, 0, 90));
    private final ColourSetting maybeColor = new ColourSetting(">1 Solution", new Colour(255, 255, 0, 90));
    private static final double THRESHOLD = Math.cos(Math.toRadians(0.0001));

    public TPMaze(Puzzles module) {
        super(module);
        this.registerProperty(confirmedColor, maybeColor);
    }

    protected Room tpMazeRoom = null;
    protected ArrayList<TPPad> possiblePads = null;


    public record TPPad(Pos pad, Pos aimSpot) {}

    @SubscribeEvent
    public void onRoomEnter(DungeonEvent.ChangeRoom event) {
        if (event.unique == null) return;

        reset();

        if ("Teleport Maze".equals(event.unique.getName())) onTpEnter(event.room);
    }

    protected void onTpEnter(Room room) {
        tpMazeRoom = room;
        possiblePads = PAD_LOCATIONS.stream()
                .map(pad -> new TPPad(tpMazeRoom.getRealPosition(pad.pad), tpMazeRoom.getRealPosition(pad.aimSpot)))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    @Override
    public void reset() {
        tpMazeRoom = null;
        possiblePads = null;
    }

    @SubscribeEvent
    public void onLoad(WorldEvent.Load event) {
        reset();
    }

    @SubscribeEvent
    public void onTP(PacketEvent.Receive event) {
        if (tpMazeRoom == null || !(event.getPacket() instanceof ClientboundPlayerPositionPacket packet) || possiblePads.size() == 1) return;

        Vec3 packetPos = packet.change().position();

        double yawRad = Math.toRadians(packet.change().yRot());
        double facingX = -Math.sin(yawRad);
        double facingZ =  Math.cos(yawRad);
        possiblePads.removeIf(pad -> {
            if ((pad.aimSpot.x == packetPos.x && pad.aimSpot.z == packetPos.z)) {
                return true;
            }

            double dx = pad.aimSpot.x - packetPos.x;
            double dz = pad.aimSpot.z - packetPos.z;

            double length = Math.sqrt(dx * dx + dz * dz);
            if (length == 0) return true;

            double dot = (facingX * dx + facingZ * dz) / length;
            return dot < THRESHOLD;
        });
    }

    @SubscribeEvent
    public void onRender(Render3DEvent.Extract event) {
        if (tpMazeRoom == null || possiblePads.size() > 4) return;

        if (possiblePads.size() == 1) {
            Renderer3D.addTask(new FilledBox(possiblePads.getFirst().pad.asBlockPos(), confirmedColor.getValue(), false));
            return;
        }

        for (TPPad pad : possiblePads) {
            Renderer3D.addTask(new FilledBox(pad.pad.asBlockPos(), maybeColor.getValue(), false));
        }
    }

    protected final List<TPPad> PAD_LOCATIONS = List.of(
            //redstone
            new TPPad(new Pos(-4.5, 69.5, -8.5), new Pos(-5.5, 69.0, -7.5)),
            new TPPad(new Pos(-4.5, 69.5, -2.5), new Pos(-5.5, 69.0, -3.5)),
            new TPPad(new Pos(-10.5, 69.5, -2.5), new Pos(-9.5, 69.0, -3.5)),
            new TPPad(new Pos(-10.5, 69.5, -8.5), new Pos(-9.5, 69.0, -7.5)),

            //emerald
            new TPPad(new Pos(-4.5, 69.5, -0.5), new Pos(-5.5, 69.0, 0.5)),
            new TPPad(new Pos(-4.5, 69.5, 5.5), new Pos(-5.5, 69.0, 4.5)),
            new TPPad(new Pos(-10.5, 69.5, 5.5), new Pos(-9.5, 69.0, 4.5)),
            new TPPad(new Pos(-10.5, 69.5, -0.5), new Pos(-9.5, 69.0, 0.5)),

            //diamond
            new TPPad(new Pos(-4.5, 69.5, 7.5), new Pos(-5.5, 69.0, 8.5)),
            new TPPad(new Pos(-4.5, 69.5, 13.5), new Pos(-5.5, 69.0, 12.5)),
            new TPPad(new Pos(-10.5, 69.5, 13.5), new Pos(-9.5, 69.0, 12.5)),
            new TPPad(new Pos(-10.5, 69.5, 7.5), new Pos(-9.5, 69.0, 8.5)),

            //lapis
            new TPPad(new Pos(3.5, 69.5, 7.5), new Pos(2.5, 69.0, 8.5)),
            new TPPad(new Pos(3.5, 69.5, 13.5), new Pos(2.5, 69.0, 12.5)),
            new TPPad(new Pos(-2.5, 69.5, 13.5), new Pos(-1.5, 69.0, 12.5)),
            new TPPad(new Pos(-2.5, 69.5, 7.5), new Pos(-1.5, 69.0, 8.5)),

            //coal
            new TPPad(new Pos(11.5, 69.5, 7.5), new Pos(10.5, 69.0, 8.5)),
            new TPPad(new Pos(11.5, 69.5, 13.5), new Pos(10.5, 69.0, 12.5)),
            new TPPad(new Pos(5.5, 69.5, 13.5), new Pos(6.5, 69.0, 12.5)),
            new TPPad(new Pos(5.5, 69.5, 7.5), new Pos(6.5, 69.0, 8.5)),

            //iron
            new TPPad(new Pos(11.5, 69.5, -0.5), new Pos(10.5, 69.0, 0.5)),
            new TPPad(new Pos(11.5, 69.5, 5.5), new Pos(10.5, 69.0, 4.5)),
            new TPPad(new Pos(5.5, 69.5, 5.5), new Pos(6.5, 69.0, 4.5)),
            new TPPad(new Pos(5.5, 69.5, -0.5), new Pos(6.5, 69.0, 0.5)),

            //gold
            new TPPad(new Pos(11.5, 69.5, -8.5), new Pos(10.5, 69.0, -7.5)),
            new TPPad(new Pos(11.5, 69.5, -2.5), new Pos(10.5, 69.0, -3.5)),
            new TPPad(new Pos(5.5, 69.5, -2.5), new Pos(6.5, 69.0, -3.5)),
            new TPPad(new Pos(5.5, 69.5, -8.5), new Pos(6.5, 69.0, -7.5))
    );
}
