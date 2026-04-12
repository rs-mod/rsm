package com.ricedotwho.rsm.ui.itemmodifier;

import com.mojang.blaze3d.platform.Window;
import com.ricedotwho.rsm.RSM;
import com.ricedotwho.rsm.command.impl.itemmodifier.ItemModifierStore;
import com.ricedotwho.rsm.command.impl.itemmodifier.ItemNameOverride;
import com.ricedotwho.rsm.module.impl.render.ClickGUI;
import com.ricedotwho.rsm.ui.clickgui.RSMConfig;
import com.ricedotwho.rsm.ui.clickgui.api.FatalityColours;
import com.ricedotwho.rsm.ui.clickgui.api.Mask;
import com.ricedotwho.rsm.ui.clickgui.impl.module.settings.ValueComponent;
import com.ricedotwho.rsm.utils.Accessor;
import com.ricedotwho.rsm.utils.ChatUtils;
import com.ricedotwho.rsm.utils.ItemUtils;
import com.ricedotwho.rsm.utils.MouseUtils;
import com.ricedotwho.rsm.utils.render.render2d.NVGSpecialRenderer;
import com.ricedotwho.rsm.utils.render.render2d.NVGUtils;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.joml.Vector2d;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static org.lwjgl.nanovg.NanoVG.*;

public class ItemModifierGui extends Screen implements Accessor {
    @Getter
    @Setter
    private Vector2d position;

    @Getter
    private final List<Mask> maskList;
    private final int WIDTH = 850;
    private final int HEIGHT = 600;
    private final int RENDER_SECTION_HEIGHT = 475;

    private float scroll = 0f;
    private boolean clickHandled = false;
    private static boolean clickConsumed = false;

    private final List<ItemModifierRow> rows = new ArrayList<>();

    public ItemModifierGui() {
        super(Component.literal("Item Modifier"));
        this.maskList = new ArrayList<>();
        this.position = new Vector2d(1920 / 2.0 - WIDTH / 2.0, 1080 / 2.0 - HEIGHT / 2.0);
        reloadRows();
    }

    public static void open() {
        if (mc.screen == null) {
            mc.setScreen(new ItemModifierGui());
        }
    }

    @Override
    public void render(GuiGraphics gfx, int mouseX, int mouseY, float deltaTicks) {
        Window window = mc.getWindow();
        float standardScale = RSMConfig.getStandardGuiScale();
        this.position = new Vector2d(window.getWidth() / (2f * standardScale) - WIDTH / 2f, window.getHeight() / (2f * standardScale) - HEIGHT / 2f);

        NVGSpecialRenderer.draw(gfx, 0, 0, gfx.guiWidth(), gfx.guiHeight(), () -> {
            double scaledMouseX = MouseUtils.mouseX() / standardScale;
            double scaledMouseY = MouseUtils.mouseY() / standardScale;
            NVGUtils.scale(standardScale, standardScale);

            NVGUtils.drawRect(getPosition().x, getPosition().y, WIDTH, HEIGHT, 4, FatalityColours.BACKGROUND);

            float x = (float) getPosition().x;
            float y = (float) (getPosition().y + 50);
            float w = WIDTH;
            float h = 525f;
            NVGUtils.drawRect(x, y, w, h, FatalityColours.PANEL);

            NVGUtils.pushScissor(x, y, (int) w, (int) h);

            nvgBeginPath(NVGUtils.getVg());
            for (int i = 0; i < w + h; i += 4) {
                nvgMoveTo(NVGUtils.getVg(), x + i, y);
                nvgLineTo(NVGUtils.getVg(), x, y + i);
            }

            nvgStrokeWidth(NVGUtils.getVg(), 2f);
            NVGUtils.colour(FatalityColours.PANEL_LINES);
            nvgStrokeColor(NVGUtils.getVg(), NVGUtils.getNvgColor());
            nvgStroke(NVGUtils.getVg());

            NVGUtils.popScissor();

            NVGUtils.drawLine((float) getPosition().x, (float) (getPosition().y + 50),
                    (float) getPosition().x + WIDTH, (float) (getPosition().y + 50), 1f, FatalityColours.LINE);
            NVGUtils.drawLine((float) getPosition().x, (float) (getPosition().y + HEIGHT - 25f),
                    (float) getPosition().x + WIDTH, (float) (getPosition().y + HEIGHT - 25f), 1f, FatalityColours.LINE);

            NVGUtils.drawText(RSM.getName(), (float) (getPosition().x + 20f), (float) (getPosition().y + 20.5), 18, FatalityColours.NAME1, ClickGUI.getFont());

            float buttonX = (float) (getPosition().x + 16f);
            float buttonY = (float) (getPosition().y + 67f);
            boolean hoveringButton = NVGUtils.isHovering(scaledMouseX, scaledMouseY, buttonX, buttonY, 100f, 25f);
            NVGUtils.drawRect(buttonX, buttonY, 100f, 25f, 3f, hoveringButton ? FatalityColours.SELECTED.darker() : FatalityColours.SELECTED);
            NVGUtils.drawText("Add Held", buttonX + (100f - NVGUtils.getTextWidth("Add Held", 12, NVGUtils.JOSEFIN)) / 2f,
                    buttonY + NVGUtils.getTextHeight(12, NVGUtils.JOSEFIN) / 2f + 2f, 12, FatalityColours.TEXT, NVGUtils.JOSEFIN);

            drawRows(gfx, scaledMouseX, scaledMouseY);
        });

        super.render(gfx, mouseX, mouseY, deltaTicks);
    }

    private void drawRows(GuiGraphics gfx, double mouseX, double mouseY) {
        if (rows.size() * 40f < RENDER_SECTION_HEIGHT && scroll != 0f) {
            scroll = 0f;
        }

        float x = (float) (getPosition().x + 16f);
        float start = (float) (getPosition().y + 100f);
        float y = start - scroll;

        NVGUtils.pushScissor(x, start, WIDTH, RENDER_SECTION_HEIGHT);

        List<Runnable> expanded = new ArrayList<>();

        for (ItemModifierRow row : rows) {
            if (y < start - 40f) {
                y += 40f;
                continue;
            }

            row.render(gfx, x, y, mouseX, mouseY);

            // YES we render twice NO I don't care about your frames
            if (row.isExpanded()) {
                float finalY = y;
                expanded.add(() -> row.render(gfx, x, finalY, mouseX, mouseY));
            }

            y += 40f;
            if (y > start + RENDER_SECTION_HEIGHT + 20f) {
                break;
            }
        }

        expanded.forEach(Runnable::run);

        NVGUtils.popScissor();
    }

    @Override
    public boolean charTyped(CharacterEvent event) {
        char typedChar = event.codepointAsString().charAt(0);
        boolean handled = false;

        for (ItemModifierRow row : rows) {
            handled = row.charTyped(typedChar) || handled;
        }

        if (handled) {
            return true;
        }

        return super.charTyped(event);
    }

    @Override
    public boolean keyPressed(KeyEvent input) {
        boolean handled = false;

        for (ItemModifierRow row : rows) {
            handled = row.keyTyped(input) || handled;
        }

        if (handled) {
            return true;
        }

        return super.keyPressed(input);
    }

    public static void consumeClick() {
        clickConsumed = true;
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent click, boolean doubled) {
        clickHandled = false;
        clickConsumed = false;

        float scale = RSMConfig.getStandardGuiScale();
        double mouseX = MouseUtils.mouseX() / scale;
        double mouseY = MouseUtils.mouseY() / scale;
        int button = click.button();

        float buttonX = (float) (getPosition().x + 16f);
        float buttonY = (float) (getPosition().y + 67f);
        boolean hoveringButton = NVGUtils.isHovering(mouseX, mouseY, buttonX, buttonY, 100f, 25f);
        if (hoveringButton && button == 0) {
            addHeldItem();
            return false;
        }

        float x = (float) (getPosition().x + 16f);
        float start = (float) (getPosition().y + 100f);
        float y = start - scroll;

        for (int i = 0; i < rows.size(); i++) {
            ItemModifierRow row = rows.get(i);
            if (y < start - 40f) {
                y += 40f;
                continue;
            }

            float relY = (float) (mouseY - y);
            float relX = (float) (mouseX - x);
            if (row.click(relX, relY, button)) { // ?
                rows.remove(i);
                break;
            }
            if (clickConsumed) break;

            y += 40f;
            if (y > getPosition().y + 100f + RENDER_SECTION_HEIGHT + 20f) {
                break;
            }
        }

        if (!clickHandled && NVGUtils.isHovering(mouseX, mouseY, (int) getPosition().x, (int) getPosition().y, width, height) && button == 0) {
            for (Mask mask : maskList) {
                if (mask.contains(mouseX, mouseY)) {
                    clickHandled = true;
                    return false;
                }
            }
        }

        return super.mouseClicked(click, doubled);
    }

    @Override
    public final boolean mouseReleased(MouseButtonEvent click) {
        rows.forEach(ItemModifierRow::release);
        return super.mouseReleased(click);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double hScroll, double vScroll) {
        float amount = (float) (Math.signum(vScroll) * 20f);
        float totalHeight = rows.size() * 40f;

        if (totalHeight < RENDER_SECTION_HEIGHT && scroll != 0f) {
            return false;
        }

        float nextScroll = scroll - amount;
        if (nextScroll < 0f) {
            return false;
        }

        float bottom = totalHeight - nextScroll;
        if (bottom < RENDER_SECTION_HEIGHT) {
            return false;
        }

        scroll = nextScroll;
        return super.mouseScrolled(mouseX, mouseY, hScroll, vScroll);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void onClose() {
        rows.forEach(ItemModifierRow::commitPendingEdits);
        super.onClose();
    }

    private void reloadRows() {
        rows.clear();
        ItemModifierStore.getData().entrySet().stream()
                .sorted(Comparator.comparing(e -> e.getKey().toLowerCase()))
                .forEach(entry -> rows.add(new ItemModifierRow(entry.getKey(), entry.getValue())));
    }

    private void addHeldItem() {
        if (mc.player == null) {
            return;
        }

        ItemStack stack = mc.player.getMainHandItem();
        if (stack.isEmpty()) {
            ChatUtils.chat("Hold an item first.");
            return;
        }

        String uuid = ItemUtils.getUUID(stack);
        if (uuid.isBlank()) {
            ChatUtils.chat("Held item has no UUID in custom data.");
            return;
        }

        ItemModifierStore.getData().putIfAbsent(uuid, new ItemNameOverride(stack));
        ItemModifierStore.save();
        reloadRows();
    }
}

