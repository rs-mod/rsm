package com.ricedotwho.rsm.module.impl.dungeon.boss.p3.terminal.types;

import com.ricedotwho.rsm.data.Colour;
import com.ricedotwho.rsm.data.TerminalType;
import com.ricedotwho.rsm.module.impl.dungeon.boss.p3.terminal.TermSol;
import com.ricedotwho.rsm.module.impl.dungeon.boss.p3.terminal.TerminalSolver;
import com.ricedotwho.rsm.utils.render.render2d.NVGUtils;
import lombok.Getter;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Melody extends Term {
    private static final List<Integer> CLAYS = List.of(16, 25, 34, 43);

    private int magentaPane = -1;
    private int limePaneRow = -1;
    private int limePane = -1;
    private int limeClay = -1;
    @Getter
    private int progress = 1;
    private boolean correct = false;

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

        if (canSolve(slot)) {
            solution.clear();
            rawSolution.clear();
            solve();
            rawSolution.addAll(solution.stream().map(TermSol::copy).toList());
        }
    }

    @Override
    public void solve() {
        int mp = findMagentaPane();
        int lp = findLastOf(Items.LIME_STAINED_GLASS_PANE);
        limeClay = findLastOf(Items.LIME_TERRACOTTA);

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
            if (entry.getValue().is(Items.MAGENTA_STAINED_GLASS_PANE)) return entry.getKey();
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
        if (TerminalSolver.getBlockAll().getValue()) return false;
        return !TerminalSolver.getMelodyBlock().getValue()
                || correct && ((TerminalSolver.getMelodyEdges().getValue() && (limePaneRow == 0 || limePaneRow == 5)) || limeClay == slot && !clicked);
    }

    @Override
    public int getSlotCount() {
        return 9*6;
    }

    @Override
    public boolean shouldRender() {
        return TerminalSolver.getTerminals().get("Melody");
    }

    @Override
    public void render(float x, float y, float gap, boolean noInteraction) {
        for (int i = 0; i < getSlotCount(); i++) {
            int col = i % 9;
            int row = i / 9;

            int lpRow = limePane / 9;
            int mpCol = magentaPane % 9;

            Colour colour = null;
            if (row == lpRow && col > 0 && col < 6) {
                colour = i == limePane ? TerminalSolver.getMelodyRow().getValue() : TerminalSolver.getMelodyRowLine().getValue();
            } else if (col == mpCol && (row == 0 || row == 5)) {
                colour = TerminalSolver.getMelodyColumn().getValue();
            } else if (CLAYS.contains(i)) {
                colour = limeClay == i ? (correct && !noInteraction ? TerminalSolver.getCanClickColour().getValue() : TerminalSolver.getMelodyClayCorrect().getValue()) : TerminalSolver.getMelodyClay().getValue();
            }
            if (colour == null) continue;

            float slotX = col * gap + x;
            float slotY = (float) ((double) row * gap + y);
            NVGUtils.drawRect(slotX, slotY, 32, 32, colour);
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
        return TerminalSolver.getMelodyTitle().getValue();
    }
}
