package com.ricedotwho.rsm.module.impl.other;

import com.ricedotwho.rsm.component.impl.location.Island;
import com.ricedotwho.rsm.component.impl.location.Location;
import com.ricedotwho.rsm.event.api.SubscribeEvent;
import com.ricedotwho.rsm.event.impl.game.ChatEvent;
import com.ricedotwho.rsm.module.Module;
import com.ricedotwho.rsm.module.api.Category;
import com.ricedotwho.rsm.module.api.ModuleInfo;
import lombok.Getter;
import lombok.val;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Getter
@ModuleInfo(aliases = "Sphinx Answer", id = "SphinxAnswer", category = Category.OTHER)
public class SphinxAnswer extends Module {

    private static final Pattern ANSWER_PATTERN = Pattern.compile("\\s{3}([ABC])\\) 7");
    private static final Map<String, String> ANSWERS = Map.ofEntries(
            Map.entry("Who owns the Gold Essence Shop?", "Marigold"),
            Map.entry("Who helps you apply Rod Parts?", "Roddy"),
            Map.entry("How many floors are there in The Catacombs?", "7"),
            Map.entry("How do you obtain the Dark Purple Dye?", "Dark Auction"),
            Map.entry("Which of these is NOT a pet?", "Slime"),
            Map.entry("Which of these is NOT a type of Gemstone?", "Prismite"),
            Map.entry("Who runs the Chocolate Factory?", "Hoppity"),
            Map.entry("What type of mob is exclusive to the Fishing Festival?", "Shark"),
            Map.entry("Where is the Titanoboa found?", "Backwater Bayou"),
            Map.entry("Which type of Gemstone has the lowest Breaking Power?", "Ruby"),
            Map.entry("What item do you use to kill Pests?", "Vacuum"),
            Map.entry("What does Junker Joel collect?", "Junk"),
            Map.entry("Where is Trevor the Trapper found?", "Mushroom Desert"),
            Map.entry("Which item rarity comes after Mythic?", "Divine"),
            Map.entry("What is the first type of slayer Maddox offers?", "Zombie")
    );

    private static final List<String> INDEX = List.of("A", "B", "C");
    private String answer = null;

    @SubscribeEvent
    public void onChat(ChatEvent.Chat event) {
        if (!Location.getArea().is(Island.Hub) || mc.player == null) return;
        if (answer == null) {
            answer = ANSWERS.get(event.getString());
            return;
        }
        Matcher matcher = ANSWER_PATTERN.matcher(event.getString());
        if (matcher.find()) {
            int index = INDEX.indexOf(matcher.group(1));
            mc.getConnection().sendCommand("sphinxanswer " + index);
        }
    }
}
