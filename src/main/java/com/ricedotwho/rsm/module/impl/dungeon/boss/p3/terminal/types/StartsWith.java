package com.ricedotwho.rsm.module.impl.dungeon.boss.p3.terminal.types;

import com.ricedotwho.rsm.data.Colour;
import com.ricedotwho.rsm.data.TerminalType;
import com.ricedotwho.rsm.module.impl.dungeon.boss.p3.terminal.TermSol;
import com.ricedotwho.rsm.module.impl.dungeon.boss.p3.terminal.TerminalSolver;
import com.ricedotwho.rsm.utils.ChatUtils;
import com.ricedotwho.rsm.utils.ItemUtils;
import com.ricedotwho.rsm.utils.render.render2d.NVGUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StartsWith extends Term {
    private static final Pattern pattern = Pattern.compile("What starts with: '(\\w+)'?");
    private final String letter;

    public StartsWith(String title) {
        super(title);
        Matcher matcher = pattern.matcher(title);
        if (matcher.find()) {
            letter = matcher.group(1).toLowerCase();
        } else {
            letter = null;
            ChatUtils.chat(Component.literal("Failed to find letter! (" + title + ")").withStyle(ChatFormatting.RED));
        }
    }

    @Override
    public void solve() {
        if (letter == null) {
            ChatUtils.chat(Component.literal("Failed to solve StartsWith! letter is null!").withStyle(ChatFormatting.RED));
            return;
        }
        packetItems.forEach((slot, item) -> {
            if (!item.isEmpty() && !ItemUtils.isEnchanted(item)) {
                String name = ChatFormatting.stripFormatting(item.getHoverName().getString().toLowerCase());
                if (name.startsWith(letter)) {
                    solution.add(new TermSol(slot));
                }
            }
        });
    }

    @Override
    public int getSlotCount() {
        return 9*5;
    }

    @Override
    public boolean shouldRender() {
        return TerminalSolver.getStartsWithEnabled().getValue();
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
                colour = TerminalSolver.getStartsWith().getValue();
            }

            NVGUtils.drawRect(slotX, slotY, 32, 32, colour);
        }
    }

    @Override
    public TerminalType getType() {
        return TerminalType.STARTS_WITH;
    }

    @Override
    public String getTitle() {
        return TerminalSolver.getStartsTitle().getValue();
    }
}
