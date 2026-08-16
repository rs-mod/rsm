package com.ricedotwho.rsm.ui.impl.clickgui.sidebar;

import com.ricedotwho.rsm.module.api.Module;
import com.ricedotwho.rsm.module.api.SubModule;
import com.ricedotwho.rsm.module.api.settings.group.GroupSetting;
import com.ricedotwho.rsm.type.Color;
import com.ricedotwho.rsm.ui.api.Node;
import com.ricedotwho.rsm.ui.api.Palette;
import com.ricedotwho.rsm.ui.api.TextAlignment;
import com.ricedotwho.rsm.ui.impl.animations.CubicBezierAnimation;
import com.ricedotwho.rsm.ui.impl.animations.LinearAnimation;
import com.ricedotwho.rsm.ui.impl.clickgui.ClickGui;
import com.ricedotwho.rsm.ui.impl.clickgui.Contents;
import com.ricedotwho.rsm.ui.impl.clickgui.contents.ModuleTab;
import com.ricedotwho.rsm.ui.impl.elements.ClickHandler;
import com.ricedotwho.rsm.ui.impl.nodes.RectangleNode;
import com.ricedotwho.rsm.ui.impl.nodes.TextNode;
import lombok.Getter;
import lombok.val;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;

public class ModuleButton extends ClickHandler {
    @Nullable
    public final ArrayList<ModuleTab> moduleTabs;

    @Getter
    private final Module module;
    private final RectangleNode background;
    private final TextNode text;
    private final LinearAnimation enabledAnimation = new LinearAnimation(200);
    public final CubicBezierAnimation contentsSelectedAnimation = new CubicBezierAnimation(200);
    private final Node highlightStroke;

    public ModuleTab selectedTab = null;

    public ModuleButton(Module module, Contents contents) {
        val node = new RectangleNode.Builder()
                .height(Palette.largeElementHeight) // I used an odd value so the text looked more centered.
                .flexDirection(Node.FlexDirection.ROW)
                .justifyContent(Node.JustifyContent.FLEX_START)
                .build();

        val empty = module.getGroupSettings().isEmpty();

        super(node, true, !empty);

        this.text = new TextNode.Builder()
                .text(module.getName())
                .fontSize(Palette.fontSize)
                .align(TextAlignment.CenterLeft)
                .font(Palette.font)
                .color(Palette.createColorContainer())
                .heightPercent(100f)
                .build();

        this.background = new RectangleNode.Builder()
                .color(Palette.elementBackgroundLight.clone())
                .display(Node.Display.FLEX)
                .flexDirection(Node.FlexDirection.ROW)
                .alignContent(Node.Align.CENTER)
                .justifyContent(Node.JustifyContent.CENTER)
                .rounding(5)
                .paddingLeft(12f)
                .paddingRight(12f)
                .build();

        this.background.addChild(this.text);
        val highlightStrokeContainer = new RectangleNode.Builder()
                .width(2f)
                .heightPercent(100f)
                .positionType(Node.PositionType.ABSOLUTE)
                .display(Node.Display.FLEX)
                .flexDirection(Node.FlexDirection.ROW)
                .alignItems(Node.Align.CENTER)
                .build();


        highlightStroke = new RectangleNode.Builder()
                .rounding(1f)
                .widthPercent(100f)
                .heightPercent(60f)
                .color(Palette.elementHighlight)
                .left(4f)
                .build();

        highlightStrokeContainer.addChild(highlightStroke);
        highlightStroke.setVisible(false);

        node.addChild(background);
        node.addChild(highlightStrokeContainer);
        this.module = module;
        moduleTabs = empty ? null : buildModuleContents(module, this, contents);
    }

    private static ArrayList<ModuleTab> buildModuleContents(Module module, ModuleButton button, Contents contents) {
        ArrayList<ModuleTab> container = new ArrayList<>();

        for (GroupSetting<? extends SubModule<?>> setting : module.getGroupSettings()) {
            val element = new ModuleTab(setting.getValue(), button, contents);
            if (button.selectedTab == null) button.selectedTab = element;
            container.add(element);
        }

        return container;
    }

    @Override
    protected void onRender(boolean hovered) {
        val enabled = module.isEnabled();
        float alpha = enabled ?
                enabledAnimation.get(0f, 1f, false) :
                hoverAnimation.get(0f, enabledAnimation.get(0.4f, 1f, true), !hovered);


        var backgroundColor = Color.setArgbAlpha(Palette.elementBackgroundLight.darker(getClickedAnimationContribution()), alpha);
        assert text.getColor() != null;
        text.getColor().mutateLerpNoAlpha(
                Palette.elementHighlight,
                Palette.text,
                enabledAnimation.get(1f, 0.6f, !enabled)
        );

        assert background.getColor() != null;
        background.getColor().setToColor(backgroundColor);

        val selected = ClickGui.getInstance().getContents().getModule() == this;
        val percent = contentsSelectedAnimation.get(0f, 60f, !selected);
        highlightStroke.setVisible(percent != 0);
        highlightStroke.setHeightPercent(percent);
    }

    @Override
    protected void onRightTriggered() {
        if (this.moduleTabs == null) return;

        val contents = ClickGui.getInstance().getContents();
        if (contents.getModule() == this) return;

        val otherModule = contents.getModule();
        if (otherModule != null) otherModule.contentsSelectedAnimation.attemptStart();

        this.contentsSelectedAnimation.attemptStart();
        contents.setModuleButton(this, ClickGui.getInstance());
    }

    @Override
    protected void onLeftTriggered() {
        if (enabledAnimation.attemptStart()) {
            module.toggle();
        }
    }

    @Override
    public void close() {
        super.close();
        if (moduleTabs == null) return;
        for (ModuleTab moduleTab : moduleTabs) {
            moduleTab.close();
        }
    }
}
