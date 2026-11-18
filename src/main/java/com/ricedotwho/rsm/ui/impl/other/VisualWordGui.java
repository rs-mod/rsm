package com.ricedotwho.rsm.ui.impl.other;

import com.ricedotwho.rsm.module.impl.render.visualwords.VisualWord;
import com.ricedotwho.rsm.module.impl.render.visualwords.VisualWords;
import com.ricedotwho.rsm.ui.api.Node;
import com.ricedotwho.rsm.ui.api.Palette;
import com.ricedotwho.rsm.ui.api.TextAlignment;
import com.ricedotwho.rsm.ui.impl.elements.ButtonElement;
import com.ricedotwho.rsm.ui.impl.elements.ClickHandler;
import com.ricedotwho.rsm.ui.impl.elements.TextInputHandler;
import com.ricedotwho.rsm.ui.impl.nodes.RectangleNode;

import java.util.List;
import java.util.function.Consumer;

public class VisualWordGui extends FeatureGui {
    public VisualWordGui(List<VisualWord> words) {
        super("Visual Words");
        words.forEach(it -> container.addChild(new Element(it)));
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

        var addButton = new ButtonElement(Palette.elementHighlight, Palette.text, 100, this::newShortcut, "New Word");
        bar.addChild(addButton);
        frame.addChild(bar);

        return super.createContents(frame);
    }

    private void newShortcut() {
        var word = new VisualWord();
        VisualWords.getInstance().getData().getValue().add(word);
        container.addChild(new Element(word));
    }

    private class Element extends ClickHandler {
        private final VisualWord word;
        private final ButtonElement toggle;

        public Element(VisualWord word) {
            var node = new RectangleNode.Builder()
                    .height(Palette.largeElementHeight)
                    .flexDirection(FlexDirection.ROW)
                    .justifyContent(JustifyContent.FLEX_START)
                    .build();
            super(node, true, true);

            this.word = word;

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

            var phraseInput = new TextInputHandler.Builder()
                    .width(Palette.smallElementWidth)
                    .height(Palette.largeElementHeight)
                    .padding(Palette.elementInteriorPadding)
                    .color(Palette.elementFill)
                    .textSupplier(word::getPhrase)
                    .textConsumer(it -> format(it, word::setPhrase))
                    .textColor(Palette.text)
                    .highlightColor(Palette.textHighlighted)
                    .textAlign(TextAlignment.CenterLeft)
                    .fontSize(Palette.fontSize)
                    .fontSupplier(Palette.font)
                    .maxCharacters(256)
                    .flexGrow(1)
                    .build();

            var replacementInput = new TextInputHandler.Builder()
                    .width(Palette.smallElementWidth)
                    .height(Palette.largeElementHeight)
                    .padding(Palette.elementInteriorPadding)
                    .color(Palette.elementFill)
                    .textSupplier(word::getReplacement)
                    .textConsumer(it -> format(it, word::setReplacement))
                    .textColor(Palette.text)
                    .highlightColor(Palette.textHighlighted)
                    .textAlign(TextAlignment.CenterLeft)
                    .fontSize(Palette.fontSize)
                    .fontSupplier(Palette.font)
                    .maxCharacters(256)
                    .flexGrow(1)
                    .build();

            toggle = new ButtonElement(Palette.elementHighlight, Palette.text, 67, this::toggle, "On");
            var delete = new ButtonElement(Palette.elementFill, Palette.text, 67, this::delete, "Delete");

            updateColours();

            background.addChild(phraseInput);
            background.addChild(replacementInput);
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

        private void format(String input, Consumer<String> consumer) {
            consumer.accept(input.replace("&&", "§"));
        }

        private void toggle() {
            word.setEnabled(!word.isEnabled());
            updateColours();
        }

        private void delete() {
            VisualWords.getInstance().getData().getValue().remove(word);
            VisualWordGui.this.container.removeChild(this);
        }

        private void updateColours() {
            toggle.color = word.isEnabled() ? Palette.elementHighlight : Palette.elementFill;
            toggle.setText(word.isEnabled() ? "On" : "Off");
        }
    }

    @Override
    public void onClose() {
        super.onClose();
        VisualWords.getInstance().getData().save();
    }
}
