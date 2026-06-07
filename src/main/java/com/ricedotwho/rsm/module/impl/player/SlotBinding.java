package com.ricedotwho.rsm.module.impl.player;

import com.google.common.reflect.TypeToken;
import com.mojang.blaze3d.platform.InputConstants;
import com.ricedotwho.rsm.data.Colour;
import com.ricedotwho.rsm.data.Keybind;
import com.ricedotwho.rsm.event.api.SubscribeEvent;
import com.ricedotwho.rsm.event.impl.game.GuiEvent;
import com.ricedotwho.rsm.module.Module;
import com.ricedotwho.rsm.module.api.Category;
import com.ricedotwho.rsm.module.api.ModuleInfo;
import com.ricedotwho.rsm.ui.clickgui.settings.impl.BooleanSetting;
import com.ricedotwho.rsm.ui.clickgui.settings.impl.ColourSetting;
import com.ricedotwho.rsm.ui.clickgui.settings.impl.KeybindSetting;
import com.ricedotwho.rsm.ui.clickgui.settings.impl.SaveSetting;
import com.ricedotwho.rsm.utils.GuiUtils;
import lombok.Getter;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.util.Mth;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix3x2fStack;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


@Getter
@ModuleInfo(aliases = "Slot Binding", id = "SlotBinding", category = Category.PLAYER)
public class SlotBinding extends Module {
    private static final List<Integer> HOTBAR = Arrays.asList(36, 37, 38, 39, 40, 41, 42, 43);
    private final BooleanSetting hoverRenderOnly = new BooleanSetting("Only render hovered", true);
    private final KeybindSetting bindKey = new KeybindSetting("Bind", new Keybind(InputConstants.UNKNOWN, true, this::tryBind));
    private final ColourSetting hovered = new ColourSetting("Hovered", new Colour(0, 255, 255, 100));
    private final ColourSetting notHovered = new ColourSetting("Colour", new Colour(255, 255, 255, 100));
    private final SaveSetting<Set<Binding>> bindings = new SaveSetting<>("Bindings", "player/slotbinding", "default.json", HashSet::new, new TypeToken<@NotNull Set<Binding>>() {}.getType(), true);

    private Integer createdBinding = null;

    public SlotBinding() {
        this.registerProperty(
                hoverRenderOnly,
                bindKey,
                hovered,
                notHovered,
                bindings
        );
    }

    private boolean tryBind() {
        if (mc.player == null || !(mc.screen instanceof InventoryScreen container)) return false;
        Slot slot = container.getHoveredSlot(GuiUtils.getMouseX(), GuiUtils.getMouseY());
        if (slot == null) return false;
        Binding the = bindings.getValue().stream().filter(b -> b.bind == slot.index || b.slot == slot.index).findFirst().orElse(null);
        if (the != null) {
            bindings.getValue().remove(the);
            bindings.save();
            return false;
        }
        if (createdBinding == null || slot.index == createdBinding) {
            createdBinding = slot.index;
        } else if (HOTBAR.contains(slot.index) || HOTBAR.contains(createdBinding)) {
            bindings.getValue().add(new Binding(createdBinding, slot.index));
            bindings.save();
            createdBinding = null;
        }
        return false;
    }

    @SubscribeEvent
    public void onSlotClick(GuiEvent.SlotClick event) {
        if (mc.player == null || mc.gameMode == null || !mc.hasShiftDown() || !(mc.screen instanceof InventoryScreen container) || event.getSlot() > container.getMenu().slots.size() || event.getSlot() < 0) return;
        Binding bind = get(event.getSlot());
        if (bind == null) return;
        event.setCancelled(true);
        int invSlot = HOTBAR.contains(bind.slot) ? bind.bind : bind.slot;
        int hotbar = (HOTBAR.contains(bind.slot) ? bind.slot : bind.bind) - 36;
        mc.gameMode.handleInventoryMouseClick(container.getMenu().containerId, invSlot, hotbar, ClickType.SWAP, mc.player);
    }

    @SubscribeEvent
    public void onDrawSlot(GuiEvent.PostDrawSlots event) {
        if (mc.player == null || !(mc.screen instanceof InventoryScreen container)) return;

        double mouseX = GuiUtils.getMouseX();
        double mouseY = GuiUtils.getMouseY();
        if (createdBinding != null) {
            Slot slot = container.getMenu().getSlot(createdBinding);
            event.getGfx().fill(
                    slot.x,
                    slot.y,
                    slot.x + 16,
                    slot.y + 16,
                    hovered.getValue().getRGB()
            );
            //drawLine(event.getGfx(), slot.x + 8, slot.y + 8, event.getX(), event.getY(), 1, this.hovered.getValue().getRGB());
        }

        boolean h = this.hoverRenderOnly.getValue();
        Slot slot = container.getHoveredSlot(mouseX, mouseY);

        for (Binding bind : bindings.getValue()) {
            boolean hover = slot != null && bind.isSlot(slot.index);
            if (h && (slot == null || !hover)) continue;
            Slot a = container.getMenu().getSlot(bind.bind);
            Slot b = container.getMenu().getSlot(bind.slot);
            int c = hover ? hovered.getValue().getRGB() : notHovered.getValue().getRGB();
            event.getGfx().fill(a.x, a.y, a.x + 16, a.y + 16, c);
            event.getGfx().fill(b.x, b.y, b.x + 16, b.y + 16, c);
            drawLine(event.getGfx(), a.x + 8, a.y + 8, b.x + 8, b.y + 8, 1, c);
        }
    }

    private void drawLine(GuiGraphics gfx, int x, int y, int x2, int y2, int width, int argb) {
        // is this not slow as fuck
        float dx = x2 - x;
        float dy = y2 - y;
        int dist = (int) Mth.sqrt(dx * dx + dy * dy);
        float a = (float) Mth.atan2(dy, dx);

        Matrix3x2fStack pose = gfx.pose();
        pose.translate(x, y);
        pose.rotate(a);
        gfx.fill(0, 0, dist, width, argb);
        pose.rotate(-a);
        pose.translate(-x, -y);
    }

    private Binding get(int slot) {
        return bindings.getValue().stream().filter(b -> b.bind == slot || b.slot == slot).findFirst().orElse(null);
    }

    public record Binding(int slot, int bind) {
        public boolean isSlot(int ...slot) {
            for (int s : slot) {
                if (this.slot == s || this.bind == s) return true;
            }
            return false;
        }
    }
}