package com.ricedotwho.rsm.ui.impl.clickgui;

import com.ricedotwho.rsm.ui.api.Node;
import com.ricedotwho.rsm.ui.api.Palette;
import com.ricedotwho.rsm.ui.api.UiElement;
import com.ricedotwho.rsm.ui.api.Widget;
import com.ricedotwho.rsm.ui.impl.clickgui.contents.ModuleTab;
import com.ricedotwho.rsm.ui.impl.clickgui.contents.RevertButton;
import com.ricedotwho.rsm.ui.impl.clickgui.sidebar.ModuleButton;
import com.ricedotwho.rsm.ui.impl.elements.SettingElementContainer;
import com.ricedotwho.rsm.ui.impl.nodes.RectangleNode;
import lombok.Getter;
import lombok.val;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.function.Supplier;


public class Contents extends Widget {

    public Contents() {
        val base = new RectangleNode.Builder()
                .rounding(10f)
                .color(Palette.foreground)
                .outline(Palette.strokeThickness, Palette.stroke)
                .padding(16f)
                .gap(12)
                .flexGrow(1f)
                .flexShrink(1f)
                .build();
        super(base);

        tabContainer = new RectangleNode.Builder()
                .display(Node.Display.FLEX)
                .flexDirection(Node.FlexDirection.ROW)
                .gap(16)
                .height(28)
                .build();

        val topStroke = new RectangleNode.Builder()
                .height(2)
                .color(Palette.stroke)
                .maxWidthPercent(100f)
                .build();

        val contentArea = new RectangleNode.Builder()
                .flexDirection(Node.FlexDirection.ROW)
                .flexGrow(1f)
                .flexShrink(1f)
                .gap(12)
                .build();

        val centerStroke = new RectangleNode.Builder()
                .width(2f)
                .heightPercent(100)
                .color(Palette.stroke)
                .build();

        settingsContainerLeft = new RectangleNode.Builder()
                .flexDirection(Node.FlexDirection.COLUMN)
                .gap(8)
                .flexGrow(1f)
                .flexBasis(0f)
                .flexShrink(1f)
                .build();

        settingsContainerRight = new RectangleNode.Builder()
                .flexDirection(Node.FlexDirection.COLUMN)
                .gap(8)
                .flexGrow(1f)
                .flexBasis(0f)
                .flexShrink(1f)
                .overflow(UiElement.Overflow.SCROLL)
                .build();

        contentArea.addChild(settingsContainerLeft);
        contentArea.addChild(centerStroke);
        contentArea.addChild(settingsContainerRight);

        this.addChild(tabContainer);
        this.addChild(topStroke);
        this.addChild(contentArea);
        this.addChild(new RevertButton());
        this.calculateLayout(Float.NaN, Float.NaN);
    }

    @Nullable
    @Getter
    private ModuleButton module = null;
    private Supplier<ModuleTab> supplier;
    public Node tabContainer;
    public Node settingsContainerLeft;
    public Node settingsContainerRight;



    public void updateSettings(ClickGui instance) {
        settingsContainerLeft.clearChildren();
        settingsContainerRight.clearChildren();

        boolean secondColumn;

        instance.frame.calculateLayout(Float.NaN, Float.NaN);
        val columnHeight = settingsContainerLeft.layoutHeight();
        var height = 0f;

        val selectedTab = getSelectedTab();
        RevertButton.setModuleTab(selectedTab);
        boolean padding = false;
        for (SettingElementContainer setting : selectedTab.getSettings()) {
            if (setting.isVisible()) height += Palette.largeElementHeight + (padding ? 8f : 0f);
            padding = true;
            secondColumn = height > columnHeight;
            if (secondColumn) {
                settingsContainerRight.addChild(setting);
                continue;
            }
            settingsContainerLeft.addChild(setting);
        }
    }

    private ModuleTab getSelectedTab() {
        return module != null ? module.selectedTab : supplier.get();
    }


    /**
     * This is for opening something that isn't necessarily a module. i.e. UniversalSettings
     */
    public void openContainer(List<ModuleTab> moduleTabs, Supplier<ModuleTab> supplier) {
        tabContainer.clearChildren();

        if (module != null) {
            module.contentsSelectedAnimation.forceStart();
            module = null;
        }

        this.supplier = supplier;

        for (ModuleTab moduleTab : moduleTabs) {
            tabContainer.addChild(moduleTab);
        }

        updateSettings(ClickGui.getInstance());
    }

    public void setModuleButton(@NotNull ModuleButton module, ClickGui instance) {
        tabContainer.clearChildren();

        this.module = module;

        if (module.moduleTabs == null) {
            logger.info(module.getModule());
            throw new RuntimeException("Module tabs are null!");
        }
        for (ModuleTab moduleTab : module.moduleTabs) {
            tabContainer.addChild(moduleTab);
        }

        updateSettings(instance);
    }

    @Override
    public void dispatchFrame(float parentX, float parentY, float mouseX, float mouseY, float scrollY) {
        super.dispatchFrame(parentX, parentY, mouseX, mouseY, scrollY);
        if (refresh) {
            updateSettings(ClickGui.getInstance());
            refresh = false;
        }
    }

    private boolean refresh = false;
    public void requestRefresh() {
        refresh = true;
    }
}
