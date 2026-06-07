package com.ricedotwho.rsm.module.impl.dungeon;

import com.ricedotwho.rsm.component.impl.Renderer3D;
import com.ricedotwho.rsm.component.impl.map.map.RoomRotation;
import com.ricedotwho.rsm.component.impl.map.map.UniqueRoom;
import com.ricedotwho.rsm.component.impl.map.utils.RoomUtils;
import com.ricedotwho.rsm.data.Colour;
import com.ricedotwho.rsm.data.Pos;
import com.ricedotwho.rsm.event.api.SubscribeEvent;
import com.ricedotwho.rsm.event.impl.game.DungeonEvent;
import com.ricedotwho.rsm.event.impl.render.Render3DEvent;
import com.ricedotwho.rsm.event.impl.world.WorldEvent;
import com.ricedotwho.rsm.module.Module;
import com.ricedotwho.rsm.module.api.Category;
import com.ricedotwho.rsm.module.api.ModuleInfo;
import com.ricedotwho.rsm.ui.clickgui.settings.impl.ColourSetting;
import com.ricedotwho.rsm.utils.RotationUtils;
import com.ricedotwho.rsm.utils.render.render3d.type.FilledOutlineBox;
import lombok.Getter;
import net.minecraft.core.BlockPos;

@Getter
@ModuleInfo(aliases = "Trap Helper", id = "TrapHelper", category = Category.DUNGEONS)
public class TrapHelper extends Module {
    private static final Pos TOP_LEFT = new Pos(11, 89, 0);
    private static final Pos BOT_LEFT = new Pos(12, 89, 0);

    private final ColourSetting fill = new ColourSetting("Fill", Colour.GREEN.alpha(100f));
    private final ColourSetting outline = new ColourSetting("Outline", Colour.GREEN.copy());

    private BlockPos pos;

    public TrapHelper() {
        this.registerProperty(fill, outline);
    }

    @SubscribeEvent
    public void onRoomChange(DungeonEvent.ChangeRoom event) {
        UniqueRoom uni = event.getUnique();
        if (uni.getName().equals("New Trap")) {
            pos = RoomUtils.getRelativePositionFixed(getPos(uni.getRotation()), uni.getMainRoom()).asBlockPos();
        } else {
            reset();
        }
    }

    @SubscribeEvent
    public void onLoad(WorldEvent.Load event) {
        reset();
    }

    @Override
    public void reset() {
        pos = null;
    }

    @SubscribeEvent
    public void onRender3D(Render3DEvent.Extract event) {
        if (pos == null) return;
        Renderer3D.addTask(new FilledOutlineBox(pos, fill.getValue(), outline.getValue(), true));
    }

    private Pos getPos(RoomRotation rotation) {
        return switch (rotation) {
            case TOPLEFT, TOPRIGHT -> TOP_LEFT;
            default -> BOT_LEFT;
        };
    }
}
