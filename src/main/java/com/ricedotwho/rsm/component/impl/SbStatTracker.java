package com.ricedotwho.rsm.component.impl;

import com.ricedotwho.rsm.component.api.ModComponent;
import com.ricedotwho.rsm.component.impl.location.Island;
import com.ricedotwho.rsm.component.impl.location.Location;
import com.ricedotwho.rsm.event.api.SubscribeEvent;
import com.ricedotwho.rsm.event.impl.game.ChatEvent;
import lombok.Getter;
import net.minecraft.ChatFormatting;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SbStatTracker extends ModComponent {
    private static final Pattern MANA_PATTERN = Pattern.compile("(?<sub>\\d+)/(?<total>\\d+)✎");
    private static final Pattern HEALTH_PATTERN = Pattern.compile("(?<sub>\\d+)/(?<total>\\d+)❤");
    private static final Pattern OVERFLOW_MANA_PATTERN = Pattern.compile("(\\d+)ʬ");
    private static final Pattern DEFENSE_PATTERN = Pattern.compile("(\\d+)❈ Defense");
    private static final Pattern SECRETS_PATTERN = Pattern.compile("([0-9]+)/([0-9]+) Secrets");

    @Getter
    private static final SbStats stats = new SbStats();

    public SbStatTracker() {
        super("SbStatTracker");
    }


    @SubscribeEvent
    public void onActionBar(ChatEvent.ActionBar event) {
        String text = ChatFormatting.stripFormatting(event.getMessage().getString()).replace(",", "");

        Matcher mana = MANA_PATTERN.matcher(text);
        if (mana.find()) {
            stats.mana.current = Integer.parseInt(mana.group("sub"));
            stats.mana.max = Integer.parseInt(mana.group("total"));
        }

        Matcher hp = HEALTH_PATTERN.matcher(text);
        if (hp.find()) {
            stats.hp.current = Integer.parseInt(hp.group("sub"));
            stats.hp.max = Integer.parseInt(hp.group("total"));
        }

        Matcher def = DEFENSE_PATTERN.matcher(text);
        if (def.find()) {
            stats.defense = Integer.parseInt(def.group(1));
        }

        Matcher om = OVERFLOW_MANA_PATTERN.matcher(text);
        if (om.find()) {
            stats.overflowMana = Integer.parseInt(om.group(1));
        }

        if (Location.getArea().is(Island.Dungeon)) {
            Matcher secrets = SECRETS_PATTERN.matcher(text);
            if (secrets.find()) {
                stats.secrets.current = Integer.parseInt(secrets.group(1));
                stats.secrets.max = Integer.parseInt(secrets.group(2));
            } else {
                stats.secrets.current = -1;
                stats.secrets.max = -1;
            }
        }
    }

    @Getter
    public static class SbStats {
        private final Stat hp = new Stat();
        private final Stat mana = new Stat();
        private final Stat secrets = new Stat();
        private int defense;
        private int overflowMana;

        @Override
        public String toString() {
            return "SbStats{hp=" + this.hp
                    + ",mana=" + this.mana
                    + ",defense=" + this.defense
                    + ",overflowMana=" + this.overflowMana + "}";
        }
    }

    @Getter
    public static class Stat {
        private int current;
        private int max;

        public float percent() {
            return (float) current / (float) max;
        }

        @Override
        public String toString() {
            return "Stat{current=" + this.current
                    + ",max=" + this.max + "}";
        }
    }
}
