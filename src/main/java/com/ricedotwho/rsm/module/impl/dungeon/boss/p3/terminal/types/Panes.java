package com.ricedotwho.rsm.module.impl.dungeon.boss.p3.terminal.types;

import com.ricedotwho.rsm.managers.dungeon.TerminalType;
import com.ricedotwho.rsm.module.impl.dungeon.boss.p3.terminal.TermSol;
import com.ricedotwho.rsm.module.impl.dungeon.boss.p3.terminal.TerminalSolver;
import com.ricedotwho.rsm.render.render2d.Render2D;
import com.ricedotwho.rsm.type.Color;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.item.Items;

public class Panes extends Term {

    public Panes(String title) {
        super(title);
    }

    @Override
    public void solve() {
        packetItems.forEach((slot, item) -> {
            if (!item.isEmpty() && item.getItem() == Items.STAINED_GLASS_PANE.red()) {
                solution.add(new TermSol(slot));
            }
        });
    }

    @Override
    public boolean shouldRender() {
        return TerminalSolver.getInstance().getTerminals().get("Panes");
    }

    @Override
    public void render(GuiGraphicsExtractor gfx, float x, float y, float gap, boolean noInteraction) {
        for (int i = 0; i < getSlotCount(); i++) {
            TermSol sol = getBySlot(i);
            if (sol == null) continue;

            int slotX = (int) (i % 9 * gap + x);
            int slotY = (int) (Math.floor((double) i / 9) * gap + y);

            Color color;
            if (!noInteraction && TerminalSolver.getInstance().getCanClick().getValue() && canClick(i)) {
                color = TerminalSolver.getInstance().getCanClickColor().getValue();
            } else {
                color = TerminalSolver.getInstance().getPanesColor().getValue();
            }

            Render2D.rect(gfx, slotX, slotY, 32, 32, color);
        }
    }

    @Override
    public TerminalType getType() {
        return TerminalType.PANES;
    }

    @Override
    public String getTitle() {
        return TerminalSolver.getInstance().getPanesTitle().getValue();
    }
}
