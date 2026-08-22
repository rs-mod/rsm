package com.ricedotwho.rsm.module.impl.dungeon.boss.p3;

import com.google.gson.reflect.TypeToken;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.Window;
import com.ricedotwho.rsm.event.api.SubscribeEvent;
import com.ricedotwho.rsm.event.impl.client.PacketEvent;
import com.ricedotwho.rsm.event.impl.game.GuiEvent;
import com.ricedotwho.rsm.managers.dungeon.DungeonClass;
import com.ricedotwho.rsm.managers.dungeon.DungeonPlayer;
import com.ricedotwho.rsm.managers.dungeon.map.handler.Dungeon;
import com.ricedotwho.rsm.module.api.Category;
import com.ricedotwho.rsm.module.api.Module;
import com.ricedotwho.rsm.module.api.ModuleInfo;
import com.ricedotwho.rsm.module.api.settings.group.DefaultGroupSetting;
import com.ricedotwho.rsm.module.api.settings.impl.*;
import com.ricedotwho.rsm.render.render2d.Font;
import com.ricedotwho.rsm.render.render2d.NVGSpecialRenderer;
import com.ricedotwho.rsm.render.render2d.NVGUtils;
import com.ricedotwho.rsm.type.Color;
import com.ricedotwho.rsm.type.Keybind;
import com.ricedotwho.rsm.utils.MouseUtils;
import com.ricedotwho.rsm.utils.StringUtils;
import com.ricedotwho.rsm.utils.Utils;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.protocol.game.ClientboundContainerClosePacket;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.network.protocol.game.ClientboundOpenScreenPacket;
import net.minecraft.network.protocol.game.ServerboundContainerClosePacket;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Getter
@ModuleInfo(aliases = "Leap Gui", id = "LeapGui", category = Category.DUNGEONS)
public class LeapGui extends Module {
    @Getter
    @SuppressWarnings("FieldMayBeFinal")
    private static LeapGui instance = new LeapGui();

    private final BooleanSetting classNames = new BooleanSetting("Class Name", false);
    private final BooleanSetting closeOnClick = new BooleanSetting("Close on Click", false);
    private final NumberSetting<Float> scale = new NumberSetting<>("Scale", 1f, 5f, 1f, 0.1f, "x");
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
    private final NumberSetting<Integer> buttonWidth = new NumberSetting<>("Button Width", 100, 300, 150, 5);
    private final NumberSetting<Integer> buttonHeight = new NumberSetting<>("Button Height", 50, 200, 75, 5);
    private final ModeSetting fontSetting = new ModeSetting("Font", "JoseFin", List.of("JoseFin", "JoseFin Bold", "Product Sans", "SF Pro", "Nunito", "Roboto"));
    private final NumberSetting<Integer> fontSize = new NumberSetting<>("Text Size", 1, 24, 12, 1);
    private final NumberSetting<Integer> classFontSize = new NumberSetting<>("Class Size", 1, 24, 8, 1);
    private final NumberSetting<Integer> textOffset = new NumberSetting<>("Class Offset", 0, 50, 10, 1);
    private final NumberSetting<Float> buttonDistanceX = new NumberSetting<>("Button X", 5f, 25f, 10f, 0.1f);
    private final NumberSetting<Float> buttonDistanceY = new NumberSetting<>("Button Y", 5f, 25f, 10f, 0.1f);
    private final NumberSetting<Float> buttonRounding = new NumberSetting<>("Roundness", 0f, 5f, 2f, 0.1f);
    private final NumberSetting<Float> outlineWidth = new NumberSetting<>("Hovered Width", 0.1f, 3f, 0.5f, 0.1f);
    private final ColorSetting hoveredOutline = new ColorSetting("Hovering Outline", Color.WHITE.copy());

    private final ColorSetting background = new ColorSetting("Background", Color.fromHSVA(0f, 0f, 16f, 200f));
    private final ColorSetting archer = new ColorSetting("Archer", Color.MINECRAFT_GOLD);
    private final ColorSetting berserk = new ColorSetting("Berserk", Color.MINECRAFT_RED);
    private final ColorSetting mage = new ColorSetting("Mage", Color.MINECRAFT_AQUA);
    private final ColorSetting tank = new ColorSetting("Tank", Color.MINECRAFT_DARK_GREEN);
    private final ColorSetting healer = new ColorSetting("Healer", Color.MINECRAFT_LIGHT_PURPLE);
    private final ColorSetting unknown = new ColorSetting("Unknown", Color.BLACK.copy());
    @Getter
    private final SaveSetting<List<String>> leapOrder = new SaveSetting<>("Leap Order", "dungeon/leap", "leap_order.json", ArrayList::new, new TypeToken<List<String>>(){}.getType());
    private static final Pattern NAMES = Pattern.compile("^(\\[.*] )?(\\w{3,16})$");

    private final Map<DungeonClass, ColorSetting> colors = Map.of(
            DungeonClass.ARCHER, archer,
            DungeonClass.BERSERKER, berserk,
            DungeonClass.HEALER, healer,
            DungeonClass.MAGE, mage,
            DungeonClass.TANK, tank,
            DungeonClass.NONE, unknown
    );

    protected static final int SLOT_COUNT = 36;

    protected int openingId = -1;
    protected List<LeapCandidate> leapCandidates = new ArrayList<>();
    protected boolean inLeap = false;
    protected boolean clicked = false;

    protected LeapCandidate queuedLeap = null;

    public LeapGui() {
        numberKeys.add(useNumberKeys, topLeftKey, topRightKey, bottomLeftKey, bottomRightKey);
        rendering.add(buttonWidth, buttonHeight, fontSetting, fontSize, classFontSize, textOffset, buttonDistanceX, buttonDistanceY, buttonRounding, outlineWidth, hoveredOutline, background, archer, berserk, mage, tank, healer, unknown);
    }

    @Override
    public void reset() {
        openingId = -1;
        leapCandidates.clear();
        inLeap = false;
        clicked = false;
    }

    @SubscribeEvent
    public void onPacket(PacketEvent.MainReceivePost event, ClientboundContainerSetSlotPacket packet) {
        // set content?
        if (packet.getContainerId() == this.openingId && this.inLeap) {
            int slot = packet.getSlot();
            if (slot < 9 || slot > 18) return;
            handleSlot(slot, packet.getItem());
        }
    }

    @SubscribeEvent
    private void onPostSetSlot(GuiEvent.SlotUpdate event) {
        if (inLeap && event.getPacket().getContainerId() == openingId && event.getPacket().getSlot() == SLOT_COUNT - 1 && queuedLeap != null) {
            click(queuedLeap);
            if (getCloseOnClick().getValue()) mc.player.closeContainer();
        }
    }

    @SubscribeEvent
    public void onOpenAndClose(PacketEvent.MainReceivePre event) {
        if (event.getPacket() instanceof ClientboundContainerClosePacket) {
            this.reset();
            this.queuedLeap = null;
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
    public void onPacketSent(PacketEvent.Send event, ServerboundContainerClosePacket packet) {
        this.reset();
        queuedLeap = null;
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
            Optional<LeapCandidate> lc = leapCandidates.stream().filter(c -> Objects.equals(c.player.getName(), finalPlayer.getName())).findFirst();
            if (lc.isPresent()) {
                if (lc.get().slot != -1) return;
                lc.get().slot = slot;
                return;
            }

            leapCandidates.add(new LeapCandidate(slot, player));
        }
    }

    public boolean shouldRender() {
        return inLeap && !leapCandidates.isEmpty();
    }

    @SubscribeEvent
    public void onDraw(GuiEvent.Draw event) {
        if (!shouldRender()) return;
        if (queuedLeap == null) this.render(event.getGfx());
        event.setCancelled(true);
    }

    protected void render(GuiGraphicsExtractor gfx) {
        NVGSpecialRenderer.draw(gfx, 0, 0, gfx.guiWidth(), gfx.guiHeight(), () -> {
            if (!shouldRender()) return;
            float scale = this.scale.getValue() + 1;
            NVGUtils.scale(scale);
            Window window = mc.getWindow();
            int width = window.getScreenWidth();
            int height = window.getScreenHeight();
            float centerX = width / 2f / scale;
            float centerY = height / 2f / scale;
            float buttonWidth = this.buttonWidth.getValue().floatValue();
            float buttonHeight = this.buttonHeight.getValue().floatValue();
            float r = buttonRounding.getValue();
            float fs = fontSize.getValue().floatValue();
            float cfs = classFontSize.getValue().floatValue();
            Font font = getFont();
            float th = NVGUtils.getTextHeight(fs, font);
            float cth = NVGUtils.getTextHeight(cfs, font);
            int hovered = getQuadrant() - 1;

            for (int i = 0; i < Math.min(leapCandidates.size(), 4); i++) {
                LeapCandidate lc = leapCandidates.get(i);
                if (lc == null) continue;
                float x = getQuadrantX(i, centerX);
                float y = getQuadrantY(i, centerY);
                NVGUtils.drawRect(x, y, buttonWidth, buttonHeight, r, background.getValue());
                if (i == hovered) NVGUtils.drawOutlineRect(x, y, buttonWidth, buttonHeight, r, this.outlineWidth.getValue(), this.hoveredOutline.getValue());

                String name = classNames.getValue() ? lc.player.getDClass().getDClass() : lc.player.getName();
                float nameWidth = NVGUtils.getTextWidth(name, fs, font);
                NVGUtils.drawText(name, x + buttonWidth / 2 - nameWidth / 2, y + buttonHeight / 2 - th / 2, fs, colors.get(lc.player.getDClass()).getValue(), font);
                if (!classNames.getValue()) {
                    String clazz = lc.player.isDead() ? "Dead" : lc.player.getDClass().getDClass();
                    float clazzWidth = NVGUtils.getTextWidth(clazz, cfs, font);
                    NVGUtils.drawText(clazz, x + buttonWidth / 2 - clazzWidth / 2, y + buttonHeight / 2 - cth + this.textOffset.getValue().floatValue(), cfs, lc.player.isDead() ? Color.MINECRAFT_DARK_RED : Color.WHITE, font);
                }
            }
        });
    }

    protected Font getFont() {
        return NVGUtils.getFont(this.fontSetting.getValue());
    }

    private float getQuadrantX(int q, float center) {
        return switch (q) {
            case 0, 2 -> center - buttonDistanceX.getValue() - this.buttonWidth.getValue().floatValue();
            case 1, 3 -> center + buttonDistanceX.getValue();
            default -> throw new IllegalStateException("Unexpected value: " + q);
        };
    }

    private float getQuadrantY(int q, float center) {
        return switch (q) {
            case 0, 1 -> center - buttonDistanceY.getValue() - this.buttonHeight.getValue().floatValue();
            case 2, 3 -> center + buttonDistanceY.getValue();
            default -> throw new IllegalStateException("Unexpected value: " + q);
        };
    }

    protected void click(LeapCandidate lc) {
        if (!this.inLeap || !(mc.screen instanceof AbstractContainerScreen<?> screen)) return;

//        if (lc.player.isDead()) {
//            ChatUtils.chat("Player is dead!");
//            return;
//        }

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
                //ChatUtils.chat("Failed to find slot for \"{}\"", name);
                queuedLeap = lc;
                return;
            }
            index = slot.index;
        }
        if (index < 0) return;

        assert mc.gameMode != null;
        assert mc.player != null;
        mc.gameMode.handleContainerInput(menu.containerId, index, 0, ContainerInput.PICKUP, mc.player);
        clicked = true;
        if (this.leapAnnounce.getValue()) Objects.requireNonNull(mc.getConnection()).sendCommand("pc " + StringUtils.format(this.leapMessage.getValue(), Map.of("{me}", mc.player.getName().getString(), "{player}", lc.player.getName())));
        queuedLeap = null;
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

            assert mc.player != null;
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

    @AllArgsConstructor
    public static class LeapCandidate {
        public int slot;
        public final DungeonPlayer player;
    }
}
