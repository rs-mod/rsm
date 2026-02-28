package com.ricedotwho.rsm.module.impl.dungeon.boss.p3.terminal.types;

import com.ricedotwho.rsm.data.Colour;
import com.ricedotwho.rsm.data.TerminalType;
import com.ricedotwho.rsm.module.impl.dungeon.boss.p3.terminal.TermSol;
import com.ricedotwho.rsm.module.impl.dungeon.boss.p3.terminal.Terminals;
import com.ricedotwho.rsm.utils.ChatUtils;
import com.ricedotwho.rsm.utils.ItemUtils;
import com.ricedotwho.rsm.utils.render.render2d.NVGUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

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

    private final String colour;

    public Select(String title) {
        super(title);
        Matcher matcher = pattern.matcher(title);
        if (matcher.find()) {
            colour = matcher.group(1).toLowerCase();
        } else {
            colour = null;
            ChatUtils.chat(Component.literal("Failed to find colour! (" + title + ")").withStyle(ChatFormatting.RED));
        }
    }

    @Override
    public void solve() {
        if (colour == null) {
            ChatUtils.chat(Component.literal("Failed to solve Select! colour is null!").withStyle(ChatFormatting.RED));
            return;
        }
        packetItems.forEach((slot, item) -> {
            if (!item.isEmpty() && !ItemUtils.isEnchanted(item)) {
                String name = fixColorItemName(ChatFormatting.stripFormatting(item.getHoverName().getString().toLowerCase()));
                if (name.startsWith(colour)) {
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
    public int getSlotCount() {
        return 9*6;
    }

    @Override
    public boolean shouldRender() {
        return Terminals.getSelectEnabled().getValue();
    }

    @Override
    public void render(float x, float y, float width, float height, float gap) {
        for (int i = 0; i < getSlotCount(); i++) {
            TermSol sol = getBySlot(i);
            if (sol == null) continue;

            float slotX = i % 9 * gap + x;
            float slotY = (float) (Math.floor((double) i / 9) * gap + y);

            Colour colour;
            if (Terminals.getCanClick().getValue() && canClick(i)) {
                colour = Terminals.getCanClickColour().getValue();
            } else {
                colour = Terminals.getSelect().getValue();
            }

            NVGUtils.drawRect(slotX, slotY, 32, 32, colour);
        }
    }

    @Override
    public TerminalType getType() {
        return TerminalType.SELECT;
    }

    @Override
    public String getTitle() {
        return Terminals.getSelectTitle().getValue();
    }
}
