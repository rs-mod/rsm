package com.ricedotwho.rsm.module.impl.dungeon.boss.p3.terminal.types;

import com.ricedotwho.rsm.managers.dungeon.TerminalType;
import com.ricedotwho.rsm.module.impl.dungeon.boss.p3.terminal.TerminalSolver;
import com.ricedotwho.rsm.render.render2d.Render2D;
import com.ricedotwho.rsm.type.Color;
import lombok.Getter;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Getter
public class Melody extends Term {
    protected static final List<Integer> CLAYS = List.of(16, 25, 34);

    protected int magentaPane = -1;
    protected int limePaneRow = -1;
    protected int limePane = -1;
    protected int limeClay = -1;
    @Getter
    protected int progress = 1;
    protected boolean correct = false;

    public Melody(String title) {
        super(title);
    }

    @Override
    protected boolean canSolve(int slot) {
        return true;
    }

    @Override
    public void onSlot(int slot, ItemStack item) {
        if (slot < 0) return;
        packetItems.put(slot, item);
        solve();
    }

    @Override
    public void solve() {
        int mp = findMagentaPane();
        int lp = findLastOf(Items.STAINED_GLASS_PANE.lime());
        limeClay = findLastOf(Items.DYED_TERRACOTTA.lime());

        if (mp == -1 || lp == -1) {
            correct = false;
        } else {
            limePane = lp;
            magentaPane = mp;
            limePaneRow = (lp % 9);
            correct = limePaneRow == (mp % 9);
            progress = (lp / 9) - 1;
        }
        clicked = false;
    }

    private int findMagentaPane() {
        for (Map.Entry<Integer, ItemStack> entry : packetItems.entrySet()) {
            if (entry.getValue().is(Items.STAINED_GLASS_PANE.magenta())) return entry.getKey();
        }
        return -1;
    }

    private int findLastOf(Item item) {
        List<Map.Entry<Integer, ItemStack>> entries = new ArrayList<>(packetItems.entrySet());
        for (int i = entries.size() - 1; i >= 0; i--) {
            Map.Entry<Integer, ItemStack> entry = entries.get(i);
            if (entry.getValue().is(item)) return entry.getKey();
        }
        return -1;
    }

    @Override
    protected boolean canClick(int slot, int button) {
        if (TerminalSolver.getInstance().getBlockAll().getValue()) return false;
        return !TerminalSolver.getInstance().getMelodyBlock().getValue()
                || correct && ((TerminalSolver.getInstance().getMelodyEdges().getValue() && (limePaneRow == 0 || limePaneRow == 5)) || limeClay == slot && !clicked);
    }

    @Override
    public boolean shouldRender() {
        return TerminalSolver.getInstance().getTerminals().get("Melody");
    }

    @Override
    public void render(GuiGraphicsExtractor gfx, float x, float y, float gap, boolean noInteraction) {
        for (int i = 0; i < getSlotCount(); i++) {
            int col = i % 9;
            int row = i / 9;

            int lpRow = limePane / 9;
            int mpCol = magentaPane % 9;

            Color colour = null;
            if (row == lpRow && col > 0 && col < 6) {
                colour = i == limePane ? TerminalSolver.getInstance().getMelodyRow().getValue() : TerminalSolver.getInstance().getMelodyRowLine().getValue();
            } else if (col == mpCol && (row == 0 || row == 4)) {
                colour = TerminalSolver.getInstance().getMelodyColumn().getValue();
            } else if (CLAYS.contains(i)) {
                colour = limeClay == i ? (correct && !noInteraction ? TerminalSolver.getInstance().getCanClickColor().getValue() : TerminalSolver.getInstance().getMelodyClayCorrect().getValue()) : TerminalSolver.getInstance().getMelodyClay().getValue();
            }
            if (colour == null) continue;

            int slotX = (int) (col * gap + x);
            int slotY = (int) ((double) row * gap + y);
            Render2D.rect(gfx, slotX, slotY, 32, 32, colour);
        }
    }

    @Override
    public void clickSlot(int slot, int button) {
        if (!canClick(slot, button)) return;
        clicked = true;
        click(slot, button);
    }

    @Override
    public TerminalType getType() {
        return TerminalType.MELODY;
    }

    @Override
    public String getTitle() {
        return TerminalSolver.getInstance().getMelodyTitle().getValue();
    }
}
