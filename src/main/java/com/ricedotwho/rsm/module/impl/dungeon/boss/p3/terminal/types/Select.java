package com.ricedotwho.rsm.module.impl.dungeon.boss.p3.terminal.types;

import com.ricedotwho.rsm.managers.dungeon.TerminalType;
import com.ricedotwho.rsm.module.impl.dungeon.boss.p3.terminal.TermSol;
import com.ricedotwho.rsm.module.impl.dungeon.boss.p3.terminal.TerminalSolver;
import com.ricedotwho.rsm.render.render2d.NVGUtils;
import com.ricedotwho.rsm.utils.ChatUtils;
import com.ricedotwho.rsm.utils.ItemUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.item.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Select extends Term {
    private static final Pattern pattern = Pattern.compile("Select all the (.+) items!");
    private static final Map<String, String> COLOR_REPLACEMENTS = Map.of(
            "light gray", "silver",
            "wool", "white",
            "bone", "white",
            "ink", "black",
            "lapis", "blue",
            "cocoa", "brown",
            "dandelion", "yellow",
            "rose", "red",
            "cactus", "green"
    );

    private final String color;

    public Select(String title) {
        super(title);
        Matcher matcher = pattern.matcher(title);
        if (matcher.find()) {
            color = matcher.group(1).toLowerCase();
        } else {
            color = null;
            ChatUtils.chat(Component.literal("Failed to find color! (" + title + ")").withStyle(ChatFormatting.RED));
        }
    }

    @Override
    public void solve() {
        if (color == null) {
            ChatUtils.chat(Component.literal("Failed to solve Select! color is null!").withStyle(ChatFormatting.RED));
            return;
        }
        packetItems.forEach((slot, item) -> {
            if (!item.isEmpty() && !ItemUtils.isEnchanted(item)) {
                String name = fixColorItemName(ChatFormatting.stripFormatting(item.getHoverName().getString().toLowerCase()));
                if (name.startsWith(color)) {
                    solution.add(new TermSol(slot));
                }
            }
        });
    }

    private String fixColorItemName(String itemName) {
        for (Map.Entry<String, String> entry : COLOR_REPLACEMENTS.entrySet()) {
            String from = entry.getKey();
            String to = entry.getValue();

            if (itemName.startsWith(from)) {
                itemName = to + itemName.substring(from.length());
            }
        }
        return itemName;
    }

    @Override
    public boolean shouldRender() {
        return TerminalSolver.getInstance().getTerminals().get("Select");
    }

    @Override
    public void render(float x, float y, float gap, boolean noInteraction) {
        for (int i = 0; i < getSlotCount(); i++) {
            TermSol sol = getBySlot(i);
            if (sol == null) continue;

            float slotX = i % 9 * gap + x;
            float slotY = (float) (Math.floor((double) i / 9) * gap + y);

            Color color;
            if (!noInteraction && TerminalSolver.getInstance().getCanClick().getValue() && canClick(i)) {
                color = TerminalSolver.getInstance().getCanClickColor().getValue();
            } else {
                color = TerminalSolver.getInstance().getSelect().getValue();
            }

            NVGUtils.drawRect(slotX, slotY, 32, 32, color);
        }
    }

    @Override
    public TerminalType getType() {
        return TerminalType.SELECT;
    }

    @Override
    public String getTitle() {
        return TerminalSolver.getInstance().getSelectTitle().getValue();
    }

    @Override
    public int getPrediction(int slot, ContainerInput input) {
        Map<Integer, ItemStack> items = new HashMap<>(packetItems);
        ItemStack prev = items.get(slot);
        prev.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        return this.slotsHashCode(items);
    }
}
