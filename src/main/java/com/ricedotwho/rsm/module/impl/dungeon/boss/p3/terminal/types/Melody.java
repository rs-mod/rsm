package com.ricedotwho.rsm.module.impl.dungeon.boss.p3.terminal.types;

import com.ricedotwho.rsm.managers.dungeon.TerminalType;
import com.ricedotwho.rsm.module.impl.dungeon.boss.p3.terminal.TerminalSolver;
import com.ricedotwho.rsm.render.render2d.NVGUtils;
import com.ricedotwho.rsm.type.Color;
import lombok.Getter;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Melody extends Term {
    private static final List<Integer> CLAYS = List.of(16, 25, 34, 43);

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
        if (TerminalSolver.getInstance().getBlockAll().getValue()) return false;
        return !TerminalSolver.getInstance().getMelodyBlock().getValue()
                || correct && ((TerminalSolver.getInstance().getMelodyEdges().getValue() && (limePaneRow == 0 || limePaneRow == 5)) || limeClay == slot && !clicked);
    }

    @Override
    public boolean shouldRender() {
        return TerminalSolver.getInstance().getTerminals().get("Melody");
    }

    @Override
    public void render(float x, float y, float gap, boolean noInteraction) {
        for (int i = 0; i < getSlotCount(); i++) {
            int col = i % 9;
            int row = i / 9;

            int lpRow = limePane / 9;
            int mpCol = magentaPane % 9;

            Color color = null;
            if (row == lpRow && col > 0 && col < 6) {
                color = i == limePane ? TerminalSolver.getInstance().getMelodyRow().getValue() : TerminalSolver.getInstance().getMelodyRowLine().getValue();
            } else if (col == mpCol && (row == 0 || row == 5)) {
                color = TerminalSolver.getInstance().getMelodyColumn().getValue();
            } else if (CLAYS.contains(i)) {
                color = limeClay == i ? (correct && !noInteraction ? TerminalSolver.getInstance().getCanClickColor().getValue() : TerminalSolver.getInstance().getMelodyClayCorrect().getValue()) : TerminalSolver.getInstance().getMelodyClay().getValue();
            }
            if (color == null) continue;

            float slotX = col * gap + x;
            float slotY = (float) ((double) row * gap + y);
            NVGUtils.drawRect(slotX, slotY, 32, 32, color);
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

    @Override
    public int getPrediction(int slot, ContainerInput input) {
        return 0;
    }
}
