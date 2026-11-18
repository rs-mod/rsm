package com.ricedotwho.rsm.ui.impl.other;

import com.ricedotwho.rsm.module.impl.player.keyshortcuts.KeyShortcuts;
import com.ricedotwho.rsm.module.impl.player.keyshortcuts.Shortcut;
import com.ricedotwho.rsm.ui.api.Node;
import com.ricedotwho.rsm.ui.api.Palette;
import com.ricedotwho.rsm.ui.api.TextAlignment;
import com.ricedotwho.rsm.ui.impl.elements.ButtonElement;
import com.ricedotwho.rsm.ui.impl.elements.ClickHandler;
import com.ricedotwho.rsm.ui.impl.elements.KeybindElement;
import com.ricedotwho.rsm.ui.impl.elements.TextInputHandler;
import com.ricedotwho.rsm.ui.impl.nodes.RectangleNode;

import java.util.List;

public class KeyShortcutGui extends FeatureGui {
    public KeyShortcutGui(List<Shortcut> shortcuts) {
        super("Key Shortcuts");
        shortcuts.forEach(it -> container.addChild(new Element(it)));
    }

    @Override
    protected RectangleNode createContents(Node frame) {
        var bar = new RectangleNode.Builder()
                .display(Node.Display.FLEX)
                .flexDirection(Node.FlexDirection.ROW)
                .alignItems(Node.Align.CENTER)
                .justifyContent(Node.JustifyContent.FLEX_START)
                .height(34)
                .paddingLeft(20)
                .paddingTop(3)
                .color(Palette.backdrop)
                .rounding(0, 0, 0, 0)
                .build();

        var addButton = new ButtonElement(Palette.elementHighlight, Palette.text, 100, this::newShortcut, "New Shortcut");
        bar.addChild(addButton);
        frame.addChild(bar);

        return super.createContents(frame);
    }

    private void newShortcut() {
        var shortcut = new Shortcut();
        KeyShortcuts.getData().getValue().add(shortcut);
        container.addChild(new Element(shortcut));
    }

    private class Element extends ClickHandler {
        private final Shortcut shortcut;
        private final ButtonElement allowGui;
        private final ButtonElement toggle;

        public Element(Shortcut shortcut) {
            var node = new RectangleNode.Builder()
                    .height(Palette.largeElementHeight)
                    .flexDirection(FlexDirection.ROW)
                    .justifyContent(JustifyContent.FLEX_START)
                    .build();
            super(node, true, true);

            this.shortcut = shortcut;

            var background = new RectangleNode.Builder()
                    .color(Palette.elementBackgroundLight.clone())
                    .display(Display.FLEX)
                    .flexDirection(FlexDirection.ROW)
                    .alignContent(Align.STRETCH)
                    .justifyContent(JustifyContent.CENTER)
                    .rounding(3)
                    .paddingLeft(4f)
                    .paddingRight(4f)
                    .paddingTop(2)
                    .paddingBottom(2)
                    .height(32f)
                    .maxWidth(1330)
                    .widthPercent(100)
                    .gap(4)
                    .build();

            // create internal contents
            var textInput = new TextInputHandler.Builder()
                    .width(Palette.largeElementWidth)
                    .height(Palette.largeElementHeight)
                    .padding(Palette.elementInteriorPadding)
                    .color(Palette.elementFill)
                    .textSupplier(shortcut::getCommand)
                    .textConsumer(shortcut::setCommand)
                    .textColor(Palette.text)
                    .highlightColor(Palette.textHighlighted)
                    .textAlign(TextAlignment.CenterLeft)
                    .fontSize(Palette.fontSize)
                    .fontSupplier(Palette.font)
                    .maxCharacters(256)
                    .flexGrow(1)
                    .build();

            var keybind = new KeybindElement(Palette.elementFill, Palette.text, 100, shortcut.getKeybind(), null);

            allowGui = new ButtonElement(Palette.elementHighlight, Palette.text, 67, this::toggleAllowGui, "Allow Gui");
            toggle = new ButtonElement(Palette.elementHighlight, Palette.text, 67, this::toggle, "On");
            var delete = new ButtonElement(Palette.elementFill, Palette.text, 67, this::delete, "Delete");

            updateColours();

            background.addChild(textInput);
            background.addChild(keybind);
            background.addChild(allowGui);
            background.addChild(toggle);
            background.addChild(delete);


            // highlights

            var highlightStrokeContainer = new RectangleNode.Builder()
                    .width(2f)
                    .heightPercent(100f)
                    .positionType(PositionType.ABSOLUTE)
                    .display(Display.FLEX)
                    .flexDirection(FlexDirection.ROW)
                    .alignItems(Align.CENTER)
                    .build();

            var highlightStroke = new RectangleNode.Builder()
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
        }

        private void toggleAllowGui() {
            var key = shortcut.getKeybind();
            key.setAllowGui(!key.isAllowGui());
            updateColours();
        }

        private void toggle() {
            shortcut.setEnabled(!shortcut.isEnabled());
            updateColours();
        }

        private void delete() {
            shortcut.delete();
            KeyShortcutGui.this.container.removeChild(this);
        }

        private void updateColours() {
            allowGui.color = shortcut.getKeybind().isAllowGui() ? Palette.elementHighlight : Palette.elementFill;
            toggle.color = shortcut.isEnabled() ? Palette.elementHighlight : Palette.elementFill;
            toggle.setText(shortcut.isEnabled() ? "On" : "Off");
        }
    }

    @Override
    public void onClose() {
        super.onClose();
        KeyShortcuts.save();
    }
}
