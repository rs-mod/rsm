package com.ricedotwho.rsm.module.impl.player;

import com.google.common.reflect.TypeToken;
import com.ricedotwho.rsm.event.api.SubscribeEvent;
import com.ricedotwho.rsm.event.impl.game.GuiEvent;
import com.ricedotwho.rsm.module.Module;
import com.ricedotwho.rsm.module.api.Category;
import com.ricedotwho.rsm.module.api.ModuleInfo;
import com.ricedotwho.rsm.ui.clickgui.settings.impl.BooleanSetting;
import com.ricedotwho.rsm.ui.clickgui.settings.impl.SaveSetting;
import com.ricedotwho.rsm.utils.ItemUtils;
import lombok.Getter;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.input.MouseButtonInfo;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.Slot;

import java.util.HashSet;
import java.util.Set;

@Getter
@ModuleInfo(aliases = "EQ Helper", id = "EquipmentHelper", category = Category.PLAYER)
public class EquipmentHelper extends Module {

    private final BooleanSetting autoClose = new BooleanSetting("Close", false);
    private final SaveSetting<Set<String>> autoCloseSet = new SaveSetting<>("AutoClose", "player", "autoclose.json", HashSet::new, new TypeToken<Set<String>>() {}.getType());

    public EquipmentHelper() {
        this.registerProperty(
                autoClose,
                autoCloseSet
        );
    }

    @SubscribeEvent
    private void onMouseClick(GuiEvent.Click event) {
        if (mc.player == null
                || !(mc.screen instanceof ContainerScreen container)
                || !(mc.player.containerMenu instanceof ChestMenu)
                ||  !"Your Equipment and Stats".equals(container.getTitle().getString())
                || event.getInput().button() == 2
        ) return;
        MouseButtonEvent mbe = event.getInput();
        event.setCancelled(true);
        container.mouseClicked(
                new MouseButtonEvent(mbe.x(), mbe.y(), new MouseButtonInfo(2, 0)),
                event.isDoubled()
        );
        Slot slot = container.getHoveredSlot(mbe.x(), mbe.y());
        if (slot == null || !autoClose.getValue()) return;
        String id = ItemUtils.getID(slot.getItem());
        if (!id.isBlank() && autoCloseSet.getValue().contains(id)) {
            // we could cancel the appearing screen but like that's not very legit
            mc.player.closeContainer();
        }
    }
}
