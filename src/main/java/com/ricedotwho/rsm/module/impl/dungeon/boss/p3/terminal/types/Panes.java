package com.ricedotwho.rsm.module.impl.dungeon.boss.p3.terminal.types;

import com.ricedotwho.rsm.data.Colour;
import com.ricedotwho.rsm.data.TerminalType;
import com.ricedotwho.rsm.module.impl.dungeon.boss.p3.terminal.TermSol;
import com.ricedotwho.rsm.module.impl.dungeon.boss.p3.terminal.TerminalSolver;
import com.ricedotwho.rsm.utils.render.render2d.NVGUtils;
import net.minecraft.world.item.Items;

public class Panes extends Term {

    public Panes(String title) {
        super(title);
    }

    @Override
    public void solve() {
        packetItems.forEach((slot, item) -> {
            if (!item.isEmpty() && item.getItem() == Items.RED_STAINED_GLASS_PANE) {
                solution.add(new TermSol(slot));
            }
        });
    }

    @Override
    public int getSlotCount() {
        return 9*5;
    }

    @Override
    public boolean shouldRender() {
        return TerminalSolver.getPanesEnabled().getValue();
    }

    @Override
    public void render(float x, float y, float width, float height, float gap) {
        for (int i = 0; i < getSlotCount(); i++) {
            TermSol sol = getBySlot(i);
            if (sol == null) continue;

            float slotX = i % 9 * gap + x;
            float slotY = (float) (Math.floor((double) i / 9) * gap + y);

            Colour colour;
            if (TerminalSolver.getCanClick().getValue() && canClick(i)) {
                colour = TerminalSolver.getCanClickColour().getValue();
            } else {
                colour = TerminalSolver.getPanesColour().getValue();
            }

            NVGUtils.drawRect(slotX, slotY, 32, 32, colour);
        }
    }

    @Override
    public TerminalType getType() {
        return TerminalType.PANES;
    }

    @Override
    public String getTitle() {
        return TerminalSolver.getPanesTitle().getValue();
    }
}
