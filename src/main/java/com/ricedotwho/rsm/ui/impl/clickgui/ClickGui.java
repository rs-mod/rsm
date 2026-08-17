package com.ricedotwho.rsm.ui.impl.clickgui;

import com.ricedotwho.rsm.module.api.Category;
import com.ricedotwho.rsm.module.api.ModuleManager;
import com.ricedotwho.rsm.ui.api.Gui;
import com.ricedotwho.rsm.ui.api.Node;
import com.ricedotwho.rsm.ui.api.Palette;
import com.ricedotwho.rsm.ui.api.TextAlignment;
import com.ricedotwho.rsm.ui.impl.clickgui.sidebar.ModuleButton;
import com.ricedotwho.rsm.ui.impl.clickgui.topbar.CategoryButton;
import com.ricedotwho.rsm.ui.impl.clickgui.topbar.SettingsGear;
import com.ricedotwho.rsm.ui.impl.nodes.RectangleNode;
import com.ricedotwho.rsm.ui.impl.nodes.TextNode;
import lombok.Getter;
import lombok.val;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import static com.ricedotwho.rsm.type.Accessor.mc;


public final class ClickGui extends Gui {
    @Getter
    private Contents contents;
    public static Category currentCategory = Category.values()[0];
    @Getter
    private SideBar sideBar;
    private final TextNode title;
    @Getter
    private static final ClickGui instance = new ClickGui();

    private ClickGui() {
        super(Component.literal("Camel ClickGui"), GuiAlignment.CenterMiddle, new RectangleNode.Builder()
                .display(Node.Display.FLEX)
                .flexDirection(Node.FlexDirection.COLUMN)
                .maxWidth(1338 + 8)
                .widthPercent(100)
                .height(792)
                .color(Palette.backdrop)
                .rounding(10)
                .build());

        title = new TextNode.Builder()
                .align(TextAlignment.CenterLeft)
                .fontSize(Palette.titleFontSize)
                .text("RSM")
                .shadow(false)
                .font(Palette.fontBold)
                .height(36f)
                .width(100f)
                .left(20f)
                .positionType(Node.PositionType.ABSOLUTE)
                .color(Palette.text)
                .build();

        addTopBar(frame, title);

        contents = createContents(frame);
        addBottomBar(frame);


        val firstModule = findFirstModuleWithTabs();
        if (firstModule != null) {
            contents.setModuleButton(firstModule, this);
        }
    }

    @Nullable
    private ModuleButton findFirstModuleWithTabs() {
        Category[] categories = Category.values();
        int startIndex = 0;
        for (int i = 0; i < categories.length; i++) {
            if (categories[i] == currentCategory) {
                startIndex = i;
                break;
            }
        }

        for (int offset = 0; offset < categories.length; offset++) {
            Category category = categories[(startIndex + offset) % categories.length];
            for (ModuleButton moduleButton : sideBar.getModulesInCategory(category)) {
                if (moduleButton.moduleTabs != null && !moduleButton.moduleTabs.isEmpty()) {
                    currentCategory = category;
                    return moduleButton;
                }
            }
        }

        return null;
    }

    private Contents createContents(Node frame) {
        val flexWrapper = new RectangleNode.Builder()
                .display(Node.Display.FLEX)
                .flexDirection(Node.FlexDirection.ROW)
                .padding(16f)
                .flexShrink(1f)
                .flexGrow(1f)
                .gap(16f)
                .build();
        frame.addChild(flexWrapper);

        var contents = new Contents();
        sideBar = new SideBar(contents);

        flexWrapper.addChild(sideBar);
        flexWrapper.addChild(contents);

        return contents;
    }

    private static void addBottomBar(Node frame) {
        var bottomBar = new RectangleNode.Builder()
                .color(Palette.foreground)
                .rounding(0f, 0f, 10f, 10f)
                .height(36)
                .build();
        var bottomStroke = new RectangleNode.Builder()
                .color(Palette.stroke)
                .height(2f)
                .top(0f)
                .maxWidthPercent(100f)
                .build();
        bottomBar.addChild(bottomStroke);

        frame.addChild(bottomBar);
    }


    static public void addTopBar(Node frame, TextNode title) {
        var topBar = new RectangleNode.Builder()
                .display(Node.Display.FLEX)
                .flexDirection(Node.FlexDirection.ROW)
                .alignItems(Node.Align.CENTER)
                .justifyContent(Node.JustifyContent.CENTER)
                .height(68)
                .color(Palette.foreground)
                .rounding(10, 10, 0, 0)
                .build();

        topBar.addChild(new SettingsGear());

        var categoryContainer = new RectangleNode.Builder()
                .display(Node.Display.FLEX)
                .flexDirection(Node.FlexDirection.ROW)
                .left(0)
                .right(0)
                .marginLeftAuto()
                .marginRightAuto()
                .widthAuto()
                .build();

        for (Category category : Category.values()) {
            categoryContainer.addChild(new CategoryButton(category));
        }

        var stroke = new RectangleNode.Builder()
                .height(Palette.strokeThickness)
                .positionType(Node.PositionType.ABSOLUTE)
                .bottom(0)
                .widthPercent(100)
                .color(Palette.stroke)
                .build();

        topBar.addChild(categoryContainer);
        topBar.addChild(title);
        topBar.addChild(stroke);
        frame.addChild(topBar);
    }

    @Override
    public void close() {
        super.close();
    }

    @Override
    public void onClose() {
        super.onClose();
        ModuleManager.saveModules();
    }

    public static void refreshModules() {
        instance.sideBar.updateModuleButtons(instance.contents);
    }

    public static void setName(String name) {
        instance.title.setText(name);
    }

    public static void open() {
        if (mc.screen != null) return;
        mc.setScreen(instance);
    }
}
