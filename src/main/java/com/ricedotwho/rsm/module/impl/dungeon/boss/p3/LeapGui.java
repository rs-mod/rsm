package com.ricedotwho.rsm.module.impl.dungeon.boss.p3;

import com.google.gson.reflect.TypeToken;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.Window;
import com.ricedotwho.rsm.component.impl.map.handler.Dungeon;
import com.ricedotwho.rsm.data.*;
import com.ricedotwho.rsm.event.api.SubscribeEvent;
import com.ricedotwho.rsm.event.impl.client.PacketEvent;
import com.ricedotwho.rsm.event.impl.game.GuiEvent;
import com.ricedotwho.rsm.module.Module;
import com.ricedotwho.rsm.module.api.Category;
import com.ricedotwho.rsm.module.api.ModuleInfo;
import com.ricedotwho.rsm.ui.clickgui.settings.group.DefaultGroupSetting;
import com.ricedotwho.rsm.ui.clickgui.settings.impl.*;
import com.ricedotwho.rsm.utils.ChatUtils;
import com.ricedotwho.rsm.utils.MouseUtils;
import com.ricedotwho.rsm.utils.StringUtils;
import com.ricedotwho.rsm.utils.Utils;
import com.ricedotwho.rsm.utils.render.render2d.Font;
import com.ricedotwho.rsm.utils.render.render2d.NVGSpecialRenderer;
import com.ricedotwho.rsm.utils.render.render2d.NVGUtils;
import lombok.Getter;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.protocol.game.ClientboundContainerClosePacket;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.network.protocol.game.ClientboundOpenScreenPacket;
import net.minecraft.network.protocol.game.ServerboundContainerClosePacket;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.*;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Getter
@ModuleInfo(aliases = "Leap Gui", id = "LeapGui", category = Category.DUNGEONS)
public class LeapGui extends Module {
    private final BooleanSetting classNames = new BooleanSetting("Class Name", false);
    private final BooleanSetting closeOnClick = new BooleanSetting("Close on Click", false);
    private final NumberSetting scale = new NumberSetting("Scale", 1, 5, 1, 0.1, "x");
    private final BooleanSetting customSorting = new BooleanSetting("Custom Sorting", false);
    private final BooleanSetting leapOnRelease = new BooleanSetting("Leap on Release", false);
    private final BooleanSetting leapAnnounce = new BooleanSetting("Leap Announce", false);
    private final StringSetting leapMessage = new StringSetting("Leap Message", "leaping to {player}", false, false, this.leapAnnounce::getValue);

    private final DefaultGroupSetting numberKeys =  new DefaultGroupSetting("Number Keys", this);
    private final BooleanSetting useNumberKeys = new BooleanSetting("Number keys", true);
    private final KeybindSetting topLeftKey = new KeybindSetting("Top Left", new Keybind(InputConstants.KEY_1,true, false, false, () -> leapAndClose(0)));
    private final KeybindSetting topRightKey = new KeybindSetting("Top Right", new Keybind(InputConstants.KEY_2, true, false, false, () -> leapAndClose(1)));
    private final KeybindSetting bottomLeftKey = new KeybindSetting("Bottom Left", new Keybind(InputConstants.KEY_3, true, false, false, () -> leapAndClose(2)));
    private final KeybindSetting bottomRightKey = new KeybindSetting("Bottom Right", new Keybind(InputConstants.KEY_4, true, false, false, () -> leapAndClose(3)));

    private final DefaultGroupSetting rendering = new DefaultGroupSetting("Rendering", this);
    private final NumberSetting buttonWidth = new NumberSetting("Button Width", 100, 300, 150, 5);
    private final NumberSetting buttonHeight = new NumberSetting("Button Height", 50, 200, 75, 5);
    private final ModeSetting fontSetting = new ModeSetting("Font", "JoseFin", List.of("JoseFin", "JoseFin Bold", "Product Sans", "SF Pro", "Nunito", "Roboto"));
    private final NumberSetting fontSize = new NumberSetting("Text Size", 1, 24, 12, 1);
    private final NumberSetting classFontSize = new NumberSetting("Class Size", 1, 24, 8, 1);
    private final NumberSetting textOffset = new NumberSetting("Class Offset", 0, 50, 10, 1);
    private final NumberSetting buttonDistanceX = new NumberSetting("Button X", 5, 25, 10, 0.1);
    private final NumberSetting buttonDistanceY = new NumberSetting("Button Y", 5, 25, 10, 0.1);
    private final NumberSetting buttonRounding = new NumberSetting("Roundness", 0, 5, 2, 0.1);
    private final NumberSetting outlineWidth = new NumberSetting("Hovered Width", 0.1, 3, 0.5, 0.1);
    private final ColourSetting hoveredOutline = new ColourSetting("Hovering Outline", Colour.WHITE.copy());

    private final ColourSetting background = new ColourSetting("Background", new Colour(0f, 0f, 16f, 200f));
    private final ColourSetting archer = new ColourSetting("Archer", Colour.MINECRAFT_GOLD.copy());
    private final ColourSetting berserk = new ColourSetting("Berserk", Colour.MINECRAFT_RED.copy());
    private final ColourSetting mage = new ColourSetting("Mage", Colour.MINECRAFT_AQUA.copy());
    private final ColourSetting tank = new ColourSetting("Tank", Colour.MINECRAFT_DARK_GREEN.copy());
    private final ColourSetting healer = new ColourSetting("Healer", Colour.MINECRAFT_LIGHT_PURPLE.copy());
    private final ColourSetting unknown = new ColourSetting("Unknown", Colour.BLACK.copy());
    @Getter
    private static final SaveSetting<List<String>> leapOrder = new SaveSetting<>("Leap Order", "dungeon/leap", "leap_order.json", ArrayList::new, new TypeToken<List<String>>(){}.getType());
    private static final Pattern NAMES = Pattern.compile("^(\\[.*] )?(\\w{3,16})$");

    private final Map<DungeonClass, ColourSetting> colours = Map.of(
            DungeonClass.ARCHER, archer,
            DungeonClass.BERSERKER, berserk,
            DungeonClass.HEALER, healer,
            DungeonClass.MAGE, mage,
            DungeonClass.TANK, tank,
            DungeonClass.NONE, unknown
    );

    protected int openingId = -1;
    protected List<LeapCandidate> leapCandidates = new ArrayList<>();
    protected boolean inLeap = false;
    protected boolean clicked = false;

    public LeapGui() {
        this.registerProperty(
                classNames,
                closeOnClick,
                scale,
                customSorting,
                leapOnRelease,
                leapAnnounce,
                leapMessage,
                numberKeys,
                rendering,
                leapOrder
        );

        numberKeys.add(useNumberKeys, topLeftKey, topRightKey, bottomLeftKey, bottomRightKey);
        rendering.add(buttonWidth, buttonHeight, fontSetting, fontSize, classFontSize, textOffset, buttonDistanceX, buttonDistanceY, buttonRounding, outlineWidth, hoveredOutline, background, archer, berserk, mage, tank, healer);
    }

    @Override
    public void reset() {
        openingId = -1;
        leapCandidates.clear();
        inLeap = false;
        clicked = false;
    }

    @SubscribeEvent
    public void onPacket(PacketEvent.Receive event) {
        // set content?
        if (event.getPacket() instanceof ClientboundContainerSetSlotPacket packet && packet.getContainerId() == this.openingId && this.inLeap) {
            int slot = packet.getSlot();
            if (slot < 9 || slot > 18) return;
            handleSlot(slot, packet.getItem());
        }
    }

    @SubscribeEvent
    public void onOpenAndClose(PacketEvent.Receive event) {
        if (event.getPacket() instanceof ClientboundContainerClosePacket) {
            this.reset();
        }
        else if (event.getPacket() instanceof ClientboundOpenScreenPacket packet) {
            reset();
            if (Utils.equalsOneOf(packet.getTitle().getString(), "Spirit Leap", "Teleport to Player")) {
                openingId = packet.getContainerId();
                inLeap = true;
                updateCandidatesFromDungeonPlayers();
            }
        }
    }

    @SubscribeEvent
    public void onPacketSent(PacketEvent.Send event) {
        if (event.getPacket() instanceof ServerboundContainerClosePacket) {
            this.reset();
        }
    }

    protected void handleSlot(int slot, ItemStack item) {
        if (slot == 17) {
            sort();
        } else {
            if (!item.is(Items.PLAYER_HEAD)) return;
            String name = ChatFormatting.stripFormatting(item.getHoverName().getString());
            DungeonPlayer player = Dungeon.getPlayer(name);
            if (player == null) {
                player = new DungeonPlayer(DungeonClass.NONE, name, 0, 0);
            }
            DungeonPlayer finalPlayer = player;
            if (leapCandidates.stream().anyMatch(c -> Objects.equals(c.player.getName(), finalPlayer.getName()))) return;
            leapCandidates.add(new LeapCandidate(slot, player));
        }
    }

    public boolean shouldRender() {
        return inLeap && !leapCandidates.isEmpty();
    }

    @SubscribeEvent
    public void onDraw(GuiEvent.Draw event) {
        if (!shouldRender()) return;
        event.setCancelled(true);
    }

    @SubscribeEvent
    public void onDrawBg(GuiEvent.DrawBackground event) {
        if (!shouldRender()) return;
        this.render(event.getGfx());
        event.setCancelled(true);
    }

    protected void render(GuiGraphicsExtractor gfx) {
        NVGSpecialRenderer.draw(gfx, 0, 0, gfx.guiWidth(), gfx.guiHeight(), () -> {
            if (!shouldRender()) return;
            float scale = this.scale.getValue().floatValue() + 1;
            NVGUtils.scale(scale);
            Window window = mc.getWindow();
            int width = window.getScreenWidth();
            int height = window.getScreenHeight();
            float centerX = width / 2f / scale;
            float centerY = height / 2f / scale;
            float buttonWidth = this.buttonWidth.getValue().floatValue();
            float buttonHeight = this.buttonHeight.getValue().floatValue();
            float r = buttonRounding.getValue().floatValue();
            float fs = fontSize.getValue().floatValue();
            float cfs = classFontSize.getValue().floatValue();
            Font font = getFont();
            float th = NVGUtils.getTextHeight(fs, font);
            float cth = NVGUtils.getTextHeight(cfs, font);
            int hovered = getQuadrant() - 1;

            for (int i = 0; i < leapCandidates.size(); i++) {
                LeapCandidate lc = leapCandidates.get(i);
                if (lc == null) continue;
                float x = getQuadrantX(i, centerX);
                float y = getQuadrantY(i, centerY);
                NVGUtils.drawRect(x, y, buttonWidth, buttonHeight, r, background.getValue());
                if (i == hovered) NVGUtils.drawOutlineRect(x, y, buttonWidth, buttonHeight, r, this.outlineWidth.getValue().floatValue(), this.hoveredOutline.getValue());

                String name = classNames.getValue() ? lc.player.getDClass().getDClass() : lc.player.getName();
                float nameWidth = NVGUtils.getTextWidth(name, fs, font);
                NVGUtils.drawText(name, x + buttonWidth / 2 - nameWidth / 2, y + buttonHeight / 2 - th / 2, fs, colours.get(lc.player.getDClass()).getValue(), font);
                if (!classNames.getValue()) {
                    String clazz = lc.player.getDClass().getDClass();
                    float clazzWidth = NVGUtils.getTextWidth(clazz, cfs, font);
                    NVGUtils.drawText(clazz, x + buttonWidth / 2 - clazzWidth / 2, y + buttonHeight / 2 - cth + this.textOffset.getValue().floatValue(), cfs, Colour.WHITE, font);
                }
            }
        });
    }

    protected Font getFont() {
        return switch (this.fontSetting.getValue()) {
            case "JoseFin Bold" ->NVGUtils.JOSEFIN_BOLD;
            case "Product Sans" ->NVGUtils.PRODUCT_SANS;
            case "SF Pro" ->NVGUtils.SF_PRO;
            case "Nunito" ->NVGUtils.NUNITO;
            case "Roboto" ->NVGUtils.ROBOTO;
            case null, default -> NVGUtils.JOSEFIN;
        };
    }

    private float getQuadrantX(int q, float center) {
        return switch (q) {
            case 0, 2 -> center - buttonDistanceX.getValue().floatValue() - this.buttonWidth.getValue().floatValue();
            case 1, 3 -> center + buttonDistanceX.getValue().floatValue();
            default -> throw new IllegalStateException("Unexpected value: " + q);
        };
    }

    private float getQuadrantY(int q, float center) {
        return switch (q) {
            case 0, 1 -> center - buttonDistanceY.getValue().floatValue() - this.buttonHeight.getValue().floatValue();
            case 2, 3 -> center + buttonDistanceY.getValue().floatValue();
            default -> throw new IllegalStateException("Unexpected value: " + q);
        };
    }

    protected void click(LeapCandidate lc) {
        if (!this.inLeap || !(mc.screen instanceof AbstractContainerScreen<?> screen)) return;
        AbstractContainerMenu menu = screen.getMenu();
        String name = lc.player.getName();
        int index = lc.slot;

        if (index == -1) {
            Slot slot = menu.slots.stream().filter(i -> {
                if (i.index < 9 || i.index > 18) return false;
                Matcher matcher = NAMES.matcher(i.getItem().getHoverName().getString());
                return matcher.find() && name.equals(matcher.group());
            }).findFirst().orElse(null);
            if (slot == null) {
                ChatUtils.chat("Failed to find slot for \"%s\"", name);
                return;
            }
            index = slot.index;
        }
        if (index < 0) return;
        mc.gameMode.handleInventoryMouseClick(menu.containerId, index, 0, ClickType.PICKUP, mc.player);
        clicked = true;
        if (this.leapAnnounce.getValue()) mc.getConnection().sendCommand("pc " + StringUtils.format(this.leapMessage.getValue(), Map.of("{me}", mc.player.getName().getString(), "{player}", lc.player.getName())));
    }

    @SubscribeEvent
    public void onMouse(GuiEvent.Click event) {
        if (!inLeap) return;
        event.setCancelled(true);
        int quad = getQuadrant();
        leapAndClose(quad - 1);
    }

    @SubscribeEvent
    public void onMouse(GuiEvent.Release event) {
        if (!inLeap || !leapOnRelease.getValue()) return;
        event.setCancelled(true);
        int quad = getQuadrant();
        leapAndClose(quad - 1);
    }

    protected int getQuadrant() {
        double x = MouseUtils.mouseX();
        double y = MouseUtils.mouseY();
        int centerX = mc.getWindow().getScreenWidth() / 2;
        int centerY = mc.getWindow().getScreenHeight() / 2;

        if (x >= centerX) {
            return y >= centerY ? 4 : 2;
        } else {
            return y >= centerY ? 3 : 1;
        }
    }

    protected boolean leapAndClose(int i) {
        if (!inLeap) return false;
        if (clicked) return true;
        if (leapTo(i) && closeOnClick.getValue()) {
            mc.player.closeContainer();
        }
        return true;
    }

    protected boolean leapTo(int i) {
        if (i < 0 || i >= leapCandidates.size()) return false;
        click(leapCandidates.get(i));
        return true;
    }

    protected void updateCandidatesFromDungeonPlayers() {
        List<DungeonPlayer> players = new ArrayList<>(Dungeon.getPlayers());
        players.remove(Dungeon.getMyPlayer());
        // -1 is slot not currently known
        leapCandidates = new ArrayList<>(players.stream().map(p -> new LeapCandidate(-1, p)).toList());
        sort();
    }

    protected void sort() {
        if (customSorting.getValue()) {
            List<String> order = leapOrder.getValue();
            leapCandidates.sort(Comparator.comparing(c -> order.contains(c.player.getName()) ? order.indexOf(c.player.getName()) : 4));
        }
        else {
            List<LeapCandidate> temp = new ArrayList<>(Arrays.asList(null, null, null, null));
            List<LeapCandidate> secondRound = new ArrayList<>();

            List<LeapCandidate> sorted = new ArrayList<>(leapCandidates);
            sorted.sort(Comparator.comparing(d -> d.player.getDClass().getPriority()));

            for (LeapCandidate player : sorted) {
                int q = player.player.getDClass().getQuadrant();
                if (q != -1 && temp.get(q) == null) {
                    temp.set(q, player);
                } else {
                    secondRound.add(player);
                }
            }

            if (secondRound.isEmpty()) {
                leapCandidates = temp;
                leapCandidates.removeIf(Objects::isNull);
                return;
            }

            for (int i = 0; i < temp.size(); i++) {
                if (temp.get(i) == null) {
                    temp.set(i, secondRound.removeFirst());
                    if (secondRound.isEmpty()) break;
                }
            }
            leapCandidates = temp;
            leapCandidates.removeIf(Objects::isNull);
        }
    }

    public record LeapCandidate(int slot, DungeonPlayer player) {}
}
