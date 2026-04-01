package com.ricedotwho.rsm.ui.clickgui.impl;

import com.ricedotwho.rsm.RSM;
import com.ricedotwho.rsm.data.Colour;
import com.ricedotwho.rsm.data.StopWatch;
import com.ricedotwho.rsm.module.api.Category;
import com.ricedotwho.rsm.module.impl.render.ClickGUI;
import com.ricedotwho.rsm.ui.clickgui.RSMConfig;
import com.ricedotwho.rsm.ui.clickgui.api.FatalityColours;
import com.ricedotwho.rsm.ui.clickgui.api.Mask;
import com.ricedotwho.rsm.ui.clickgui.impl.category.CategoryComponent;
import com.ricedotwho.rsm.ui.clickgui.impl.module.ModuleComponent;
import com.ricedotwho.rsm.ui.clickgui.impl.module.settings.impl.TextInput;
import com.ricedotwho.rsm.utils.Accessor;
import com.ricedotwho.rsm.utils.ChatUtils;
import com.ricedotwho.rsm.utils.render.animation.Animation;
import com.ricedotwho.rsm.utils.render.animation.Easing;
import com.ricedotwho.rsm.utils.render.render2d.ColourUtils;
import com.ricedotwho.rsm.utils.render.render2d.Image;
import com.ricedotwho.rsm.utils.render.render2d.NVGUtils;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.input.KeyEvent;
import org.joml.Vector2d;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;

import java.util.*;

import static com.ricedotwho.rsm.ui.clickgui.impl.module.ModuleComponent.focusedComponent;
import static org.lwjgl.nanovg.NanoVG.*;

public class Panel implements Accessor {

    @Getter
    private final RSMConfig renderer;
    @Getter
    private final TextInput search;
    private boolean writing = false;
    @Getter
    private final List<Entry> moduleResults = new ArrayList<>();

    public Panel(RSMConfig renderer) {
        this.renderer = renderer;
        this.search = new TextInput("", 12, false, 16);
    }

    @Setter
    private Category selected = Category.MOVEMENT;
    private Category lastCategory = Category.OTHER;
    private final StopWatch stopWatch = new StopWatch();

    @Getter
    private final int width = 850;

    @Getter
    private final int height = 600;

    private static Image searchImage;

    private Image getSearchImage(){
        if (searchImage == null) {
            searchImage = NVGUtils.createImage("/assets/rsm/clickgui/search.png");
        }
        return searchImage;
    }

    public boolean charTyped(char typedChar, int keyCode) {
        if (writing) {
            search.charTyped(typedChar);
            updateSearch();
        }

        boolean value = false;
        for (CategoryComponent category : renderer.categoryList) {
            if (category.charTyped(typedChar, keyCode)) value = true;
        }
        return value;
    }

    public boolean keyTyped(KeyEvent input) {
        if (writing) {
            int key = input.key();
            if (key == 0 || key == GLFW.GLFW_KEY_ESCAPE || key == GLFW.GLFW_KEY_ENTER) {
                writing = false;
                focusedComponent = null;
                return true;
            }
            search.keyTyped(input);
            updateSearch();
        }

        boolean value = false;
        for (CategoryComponent category : renderer.categoryList) {
            if (category.keyTyped(input)) value = true;
        }
        return value;
    }

    // schizo render
    public void render(GuiGraphics gfx, double mouseX, double mouseY, float partialTicks) {
        NVGUtils.drawRect(getPosition().x, getPosition().y, width, height, 4, FatalityColours.BACKGROUND);

        float x = (float) getPosition().x, y = (float) (getPosition().y + 50), w = width, h = 525f;
        NVGUtils.drawRect(x, y, w, h, FatalityColours.PANEL);

        NVGUtils.pushScissor(x, y, (int) w, (int) h);

        nvgBeginPath(NVGUtils.getVg());

        for (int i = 0; i < w + h; i += 4) {
            nvgMoveTo(NVGUtils.getVg(), x + i, y);
            nvgLineTo(NVGUtils.getVg(), x, y + i);
        }

        nvgStrokeWidth(NVGUtils.getVg(), 2f);
        NVGUtils.colour(FatalityColours.PANEL_LINES);
        nvgStrokeColor(NVGUtils.getVg(), NVGUtils.getNvgColor());
        nvgStroke(NVGUtils.getVg());

        NVGUtils.popScissor();

        NVGUtils.drawLine((float) getPosition().x, (float) (getPosition().y + 50),
                (float) getPosition().x + width, (float) (getPosition().y + 50), 1f, FatalityColours.LINE);
        NVGUtils.drawLine((float) getPosition().x, (float) (getPosition().y + height - 25f),
                (float) getPosition().x + width, (float) (getPosition().y + height - 25f), 1f, FatalityColours.LINE);

        NVGUtils.drawText(RSM.getName(), (float) (getPosition().x + 20f), (float) (getPosition().y + 20.5), 18, FatalityColours.NAME1, ClickGUI.getFont());

        // Search bar
        float searchX = (float) (getPosition().x + 16f);
        float searchY = (float) (getPosition().y + 67f); // six sevennnnnnnnnnn
        boolean hoveringSearch = NVGUtils.isHovering(mouseX, mouseY, searchX, searchY, 95f, 25);

        NVGUtils.drawRect(searchX, searchY, 94f, 25f, 3f, hoveringSearch ? FatalityColours.GROUP_OUTLINE.brighter() : FatalityColours.GROUP_OUTLINE);
        NVGUtils.drawOutlineRect(searchX, searchY, 94f, 25f, 3f, 1f, FatalityColours.PANEL_LINES);
        search.render(searchX + 8, searchY + 8f, writing);

        if (!writing && search.getValue().isBlank()) {
            NVGUtils.renderImage(getSearchImage(), searchX + 6, searchY + 6, 12.5f, 12.5f);
        }

        int totalWidth = 0;
        for (Category cat : Category.values()) {
            totalWidth += (int) (NVGUtils.getTextWidth(cat.name(), 11, ClickGUI.getFont()) + 40);
        }

        int a = (int) (getPosition().x + (width - totalWidth) / 2f + 20);
        for (Category cat : Category.values()) {
            CategoryComponent component = renderer.categoryList.stream()
                    .filter(c -> c.getCategory().equals(cat))
                    .findFirst()
                    .orElse(null);

            if (component == null) continue;

            boolean isHovered = NVGUtils.isHovering(
                    mouseX, mouseY,
                    (int) (a - 10f), (int) (getPosition().y + 12f),
                    (int) (NVGUtils.getTextWidth(cat.name(), 11, ClickGUI.getFont()) + 39f), (int) 25f
            );

            boolean isSelected = selected == cat;

            Animation hoverAnimation = component.getHoverAnimation();
            Animation selectAnimation = component.getSelectAnimation();

            hoverAnimation
                    .setTargetValue(isSelected || isHovered ? 1 : 0)
                    .run();

            selectAnimation
                    .setTargetValue(isSelected ? 1 : 0)
                    .run();

            float hoverValue = hoverAnimation.getValue().floatValue();
            float selectValue = selectAnimation.getValue().floatValue();

            Colour highlightColor = ColourUtils.interpolateColourC(Colour.TRANSPARENT, FatalityColours.SELECTED_BACKGROUND, selectValue);
            Colour textColor = ColourUtils.interpolateColourC(FatalityColours.UNSELECTED_TEXT, FatalityColours.SELECTED_TEXT, hoverValue);
            float finalWidth = (NVGUtils.getTextWidth(cat.name(), 11, ClickGUI.getFont()) + 30f);

            NVGUtils.drawRect(a - 10f, getPosition().y + 12f, finalWidth, 25f, 3f, highlightColor);
            NVGUtils.drawText(cat.name(), a + 14, (float) (getPosition().y + 20.5f), 11, textColor, ClickGUI.getFont());

            a += (int) (NVGUtils.getTextWidth(cat.name(), 11, ClickGUI.getFont()) + 40);
            int b = (int) (NVGUtils.getTextWidth(cat.name(), 11, ClickGUI.getFont()) + 10);
            NVGUtils.renderImage(cat.getImage(), (a - b) - 38, (float) getPosition().y + 15f, 20, 20, 255);

            if (cat == selected) {
                component.render(gfx, mouseX, mouseY, partialTicks);
            }
        }

        lastCategory = selected;
    }

    public void release(double mouseX, double mouseY, int mouseButton) {
        for (Category category : Category.values()) {
            if (category == selected) {
                renderer.categoryList.stream()
                        .filter(categoryComponent -> categoryComponent.getCategory().equals(category))
                        .findFirst()
                        .orElse(null)
                        .release(mouseX, mouseY, mouseButton);
            }
        }
    }

    public void click(double mouseX, double mouseY, int mouseButton) {
        renderer.maskList.clear();

        if (NVGUtils.isHovering(mouseX, mouseY, (float) (getPosition().x + 16f), (float) (getPosition().y + 67f), 94f, 25f) && mouseButton == 0) {
            writing = true;
            focusedComponent = null;
            search.click((float) (mouseX - (getPosition().x + 16f)), mouseButton);
        } else {
            focusedComponent = null;
            writing = false;
        }

        int totalWidth = 0;
        for (Category cat : Category.values()) {
            totalWidth += (int) (NVGUtils.getTextWidth(cat.name(), 11, ClickGUI.getFont()) + 40);
        }

        int a = (int) (getPosition().x + (width - totalWidth) / 2f + 20);
        String last;

        for (Category category : Category.values()) {
            String categoryName = category.name();

            renderer.maskList.add(new Mask((int) (a - 10f), (int) (getPosition().y + 12f), (int) (NVGUtils.getTextWidth(categoryName, 11, ClickGUI.getFont()) + 39f), (int) 25f));

            if (NVGUtils.isHovering(mouseX, mouseY, (int) (a - 10f), (int) (getPosition().y + 12f), (int) (NVGUtils.getTextWidth(categoryName, 11, ClickGUI.getFont()) + 39f), (int) 25f) && mouseButton == 0) {
                selected = category;
                this.search.setValue("");
                this.updateSearch();
                Objects.requireNonNull(renderer.categoryList.stream()
                        .filter(categoryComponent -> categoryComponent.getCategory().equals(category))
                        .findFirst()
                        .orElse(null)).selected =
                        Objects.requireNonNull(renderer.categoryList.stream()
                                .filter(categoryComponent -> categoryComponent.getCategory().equals(category))
                                .findFirst()
                                .orElse(null)).lastSelected = null;
            }

            if (category == selected) {
                Objects.requireNonNull(renderer.categoryList.stream()
                        .filter(categoryComponent -> categoryComponent.getCategory().equals(category))
                        .findFirst().orElse(null)).click(mouseX, mouseY, mouseButton);
            }

            last = categoryName;
            a += (int) (NVGUtils.getTextWidth(last, 11, ClickGUI.getFont()) + 40);
        }
    }

    public void onGuiClosed(){
        getCategory(selected).onGuiClosed();
        search.setValue("");
        search.updateSelection();
        updateSearch();
    }

    public boolean scroll(double x, double y, int amount) {
        return getCategory(selected).scroll(x, y, amount);
    }

    private float lerp(float a, float b, float t) {
        return a + (b - a) * t;
    }

    public Vector2d getPosition() {
        return renderer.getPosition();
    }

    // search
    private void updateSearch() {
        this.moduleResults.clear();
        if (search.getValue().isBlank()) return;
        CategoryComponent cat = getCategory(selected);

        for (ModuleComponent module : getRenderer().moduleList) {
            int score = (cat != null && cat.selected == module ? 1500 : scoreModule(search.getValue(), module));
            if (score > 200)
                this.moduleResults.add(new Entry(module, score));
        }

        this.moduleResults.sort(Comparator.comparingInt(Entry::score).reversed());
    }

    private int scoreModule(String input, ModuleComponent module) {
        String name = module.getModule().getName();
        int best = score(input, name);
        for (String c : module.getModule().getInfo().aliases()) {
            best = Math.max(best, score(c, input) - 25); // aliases are slightly less scored
        }
        return best;
    }

    private static int score(String candidate, String query) {
        candidate = candidate.toLowerCase(Locale.ROOT);
        query = query.toLowerCase(Locale.ROOT);
        if (query.isEmpty()) return 0;
        if (candidate.equals(query)) return 1000;
        if (candidate.startsWith(query)) return 850;

        for (String word : candidate.split("[ _-]")) {
            if (word.startsWith(query)) return 800;
        }

        if (candidate.contains(query)) return 650;

        int qi = 0;
        for (int i = 0; i < candidate.length() && qi < query.length(); i++) {
            if (candidate.charAt(i) == query.charAt(qi)) qi++;
        }
        if (qi == query.length()) return 500;

        int dist = levenshtein(candidate, query);
        return Math.max(0, 400 - dist * 25);
    }

    private static int levenshtein(String a, String b) {
        int[][] dp = new int[a.length() + 1][b.length() + 1];

        for (int i = 0; i <= a.length(); i++) dp[i][0] = i;
        for (int j = 0; j <= b.length(); j++) dp[0][j] = j;

        for (int i = 1; i <= a.length(); i++) {
            for (int j = 1; j <= b.length(); j++) {
                int cost = a.charAt(i - 1) == b.charAt(j - 1) ? 0 : 1;
                dp[i][j] = Math.min(
                        Math.min(dp[i - 1][j] + 1, dp[i][j - 1] + 1),
                        dp[i - 1][j - 1] + cost
                );
            }
        }
        return dp[a.length()][b.length()];
    }

    public CategoryComponent getCategory(Category cat) {
        return renderer.categoryList.stream().filter(categoryComponent -> categoryComponent.getCategory().equals(cat)).findFirst().orElse(null);
    }

    public record Entry(ModuleComponent module, int score) {}
}