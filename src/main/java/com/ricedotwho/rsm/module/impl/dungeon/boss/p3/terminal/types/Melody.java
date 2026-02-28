package com.ricedotwho.rsm.module.impl.dungeon.boss.p3.terminal.types;

import com.ricedotwho.rsm.data.Colour;
import com.ricedotwho.rsm.data.TerminalType;
import com.ricedotwho.rsm.module.impl.dungeon.boss.p3.terminal.Terminals;
import com.ricedotwho.rsm.utils.render.render2d.NVGUtils;
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
    private boolean correct = false;

    public Melody(String title) {
        super(title);
    }

    @Override
    protected boolean canSolve(int slot) {
        return true;
    }

    @Override
    public void solve() {
        int mp = findFirstOf(Items.MAGENTA_STAINED_GLASS_PANE);
        int lp = findLastOf(Items.LIME_STAINED_GLASS_PANE);
        limeClay = findLastOf(Items.LIME_TERRACOTTA);

        if (mp == -1 || lp == -1) {
            correct = false;
        } else {
            limePane = lp;
            magentaPane = mp;
            limePaneRow = (lp % 9);
            correct = (lp % 9) == (mp % 9);
        }
        clicked = false;
    }

    private int findFirstOf(Item item) {
        for (Map.Entry<Integer, ItemStack> entry : packetItems.entrySet()) {
            if (entry.getValue().is(item)) return entry.getKey();
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
        if (Terminals.getBlockAll().getValue()) return false;
        return !Terminals.getMelodyBlock().getValue()
                || correct && ((Terminals.getMelodyEdges().getValue() && (limePaneRow == 0 || limePaneRow == 5)) || limeClay == slot && !clicked);
    }

    @Override
    public int getSlotCount() {
        return 9*6;
    }

    @Override
    public boolean shouldRender() {
        return Terminals.getMelodyEnabled().getValue();
    }

    @Override
    public void render(float x, float y, float width, float height, float gap) {
        for (int i = 0; i < getSlotCount(); i++) {
            int col = i % 9;
            int row = i / 9;

            int lpRow = limePane / 9;
            int mpCol = magentaPane % 9;

            Colour colour = null;
            if (row == lpRow && col > 0 && col < 6) {
                colour = i == limePane ? Terminals.getMelodyRow().getValue() : Terminals.getMelodyRowLine().getValue();
            } else if (col == mpCol && (row == 0 || row == 5)) {
                colour = Terminals.getMelodyColumn().getValue();
            } else if (CLAYS.contains(i)) {
                colour = limeClay == i ? (correct ? Terminals.getCanClickColour().getValue() : Terminals.getMelodyClayCorrect().getValue()) : Terminals.getMelodyClay().getValue();
            }
            if (colour == null) continue;

            float slotX = col * gap + x;
            float slotY = (float) ((double) row * gap + y);
            NVGUtils.drawRect(slotX, slotY, 32, 32, colour);
        }
    }

    @Override
    public TerminalType getType() {
        return TerminalType.MELODY;
    }

    @Override
    public String getTitle() {
        return Terminals.getMelodyTitle().getValue();
    }
}
