package com.ricedotwho.rsm.ui.impl.clickgui.contents;

import com.ricedotwho.rsm.module.api.SubModule;
import com.ricedotwho.rsm.module.api.settings.Setting;
import com.ricedotwho.rsm.module.api.settings.impl.*;
import com.ricedotwho.rsm.ui.api.Node;
import com.ricedotwho.rsm.ui.api.Palette;
import com.ricedotwho.rsm.ui.api.TextAlignment;
import com.ricedotwho.rsm.ui.api.UiElement;
import com.ricedotwho.rsm.ui.impl.animations.CubicBezierAnimation;
import com.ricedotwho.rsm.ui.impl.animations.LinearAnimation;
import com.ricedotwho.rsm.ui.impl.clickgui.ClickGui;
import com.ricedotwho.rsm.ui.impl.clickgui.Contents;
import com.ricedotwho.rsm.ui.impl.clickgui.sidebar.ModuleButton;
import com.ricedotwho.rsm.ui.impl.elements.*;
import com.ricedotwho.rsm.ui.impl.nodes.RectangleNode;
import com.ricedotwho.rsm.ui.impl.nodes.TextNode;
import lombok.Getter;
import lombok.val;

import java.util.ArrayList;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class ModuleTab extends ClickHandler {
    Consumer<ModuleTab> consumer;
    Supplier<ModuleTab> supplier;
    CubicBezierAnimation selectedAnimation = new CubicBezierAnimation(200);
    private final Node highlightStroke;
    private final Contents contents;
    private final SubModule<?> subModule;
    private final TextNode text;
    private final LinearAnimation toggleAnimation = new LinearAnimation(200);
    @Getter
    private final ArrayList<Setting<?>> bareSettings = new ArrayList<>();

    @Getter
    private final ArrayList<SettingElementContainer> settings = new ArrayList<>();

    public ModuleTab(SubModule<?> subModule, ModuleButton button, Contents contents) {
        this(subModule, (() -> button.selectedTab), (moduleTab -> button.selectedTab = moduleTab), contents);
    }

    public ModuleTab(SubModule<?> subModule, Supplier<ModuleTab> supplier, Consumer<ModuleTab> consumer, Contents contents) {
        var node = new RectangleNode.Builder()
                .heightPercent(100f)
                .build();

        val right = !subModule.getInfo().alwaysDisabled();

        super(node, true, right);

        text = new TextNode.Builder()
                .text(subModule.getName())
                .fontSize(Palette.fontSize)
                .font(Palette.font)
                .align(TextAlignment.CenterMiddle)
                .color(Palette.text.clone())
                .height(24f)
                .top(0)
                .build();

        highlightStroke = new RectangleNode.Builder()
                .widthPercent(100f)
                .color(Palette.elementHighlight)
                
                .height(2f)
                .build();

        node.addChild(text);
        node.addChild(highlightStroke);
        this.consumer = consumer;
        this.supplier = supplier;
        addSettings(subModule);
        this.contents = contents;
        this.subModule = subModule;

    }

    private void addSettings(SubModule<?> subModule) {
        for (Setting<?> setting : subModule.getSettings()) {
            bareSettings.add(setting);
            switch (setting) {

                //DO NOT FORGET TO MAKE SETTING CALL onEdit()

                case ButtonSetting options -> addSetting(options, new ButtonElement(options.getAction(), options.getDefaultValue()));
                case NumberSetting<?> options -> this.settings.add(getNumberSettingElement(options));
                case BooleanSetting options -> addSetting(options, new CheckMark(wrapConsumer(options::setValue, options), options::getValue));
                case ColorSetting options -> addSetting(options, new ColorBoxElement(options.getValue(), options.getOnEdit()));
                case EnumSetting<?> options -> addSetting(options, new ModeElement(
                        options.getDisplayOptions(), options::getIndex, options::setByIndex, setting.getOnEdit()
                ));
                case ModeSetting options -> addSetting(options, new ModeElement(
                        options.getValues().toArray(new String[0]), options::getIndex, options::setByIndex, setting.getOnEdit())
                );
                case MultiBoolSetting options -> addSetting(options, new MultiBooleanElement(options.getValue(), options.getOnEdit()));
                case KeybindSetting options -> addSetting(options, new KeybindElement(options.getValue(), options.getOnEdit()));
                case StringSetting options -> addSetting(options, new TextBox(options::getValue, wrapConsumer(options::setValue, options)));
                case SaveSetting<?> options -> {
                    if (!options.isAllowEdits()) continue;
                    this.addSetting(options, new SaveElement(options::load, options::getFileName, wrapConsumer(options::setFileName, options)));
                }
                default -> {
                }
            }
        }
    }

    private <T> Consumer<T> wrapConsumer(Consumer<T> consumer, Setting<?> setting) {
        return (T value) -> {
            consumer.accept(value);
            setting.onEdit();
        };
    }

    private void addSetting(Setting<?> setting, UiElement element) {
        this.settings.add(new SettingElementContainer(setting.getName(), setting.getIsVisible(), element));
    }

    private SettingElementContainer getNumberSettingElement(NumberSetting<?> setting) {
        val min = setting.getMin().doubleValue();
        val max = setting.getMax().doubleValue();
        Supplier<Double> supplier = () -> setting.getValue().doubleValue();
        Consumer<Double> consumer = wrapConsumer(setting::setValue, setting);

        val slider = new SliderElement(consumer, supplier, min, max, setting.getIncrement().doubleValue());

        val digits = setting.getIncrementAsBigDecimal().stripTrailingZeros().scale();

        val box = new NumberBox(min, max, digits, supplier, consumer);

        return new SettingElementContainer(
                setting.getName(),
                setting.getIsVisible(),
                slider,
                box
        );
    }


    @Override
    protected void onLeftTriggered() {
        val currentSelected = supplier.get();
        if (currentSelected == this) return;
        currentSelected.selectedAnimation.attemptStart();

        consumer.accept(this);
        selectedAnimation.attemptStart();
        contents.updateSettings(ClickGui.getInstance());
    }

    @Override
    protected void onRightTriggered() {
        if (subModule == null) return;
        subModule.toggle();
        toggleAnimation.attemptStart();
    }

    @Override
    protected void onRender(boolean hovered) {
        val selected = supplier.get() == this;
        val percent = selectedAnimation.get(0f, 100f, !selected);

        this.highlightStroke.setVisible(percent != 0);
        this.highlightStroke.setWidthPercent(percent);

        val textColor = text.getColor();

        textColor.mutateLerpNoAlpha(Palette.text, Palette.elementHighlight, toggleAnimation.get(0f, 0.6f, !subModule.isEnabled()));
        //textColor.setToColor(textColor.darker(getClickedAnimationContribution()));
    }

    @Override
    public void close() {
        super.close();
        for (SettingElementContainer setting : settings) {
            setting.close();
        }
    }
}
