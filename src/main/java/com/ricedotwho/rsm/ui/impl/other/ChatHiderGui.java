package com.ricedotwho.rsm.ui.impl.other;

import com.ricedotwho.rsm.module.impl.player.Chat;
import com.ricedotwho.rsm.module.impl.player.chat.HiddenMessage;
import com.ricedotwho.rsm.ui.api.Node;
import com.ricedotwho.rsm.ui.api.Palette;
import com.ricedotwho.rsm.ui.api.TextAlignment;
import com.ricedotwho.rsm.ui.impl.elements.ButtonElement;
import com.ricedotwho.rsm.ui.impl.elements.ClickHandler;
import com.ricedotwho.rsm.ui.impl.elements.TextInputHandler;
import com.ricedotwho.rsm.ui.impl.nodes.RectangleNode;

import java.util.List;

public class ChatHiderGui extends FeatureGui {
    public ChatHiderGui(List<HiddenMessage> messages) {
        super("Chat Hider");
        messages.forEach(it -> container.addChild(new Element(it)));
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

        var addButton = new ButtonElement(Palette.elementHighlight, Palette.text, 100, this::newEntry, "New Filter");
        bar.addChild(addButton);
        frame.addChild(bar);

        return super.createContents(frame);
    }

    private void newEntry() {
        var filter = new HiddenMessage();
        Chat.getInstance().getHiddenMessages().getValue().add(filter);
        container.addChild(new Element(filter));
    }

    private class Element extends ClickHandler {
        private final HiddenMessage filter;
        private final ButtonElement toggle;

        public Element(HiddenMessage filter) {
            var node = new RectangleNode.Builder()
                    .height(Palette.largeElementHeight)
                    .flexDirection(FlexDirection.ROW)
                    .justifyContent(JustifyContent.FLEX_START)
                    .build();
            super(node, true, true);

            this.filter = filter;

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
                    .textSupplier(filter::getMessage)
                    .textConsumer(filter::setMessage)
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

            background.addChild(textInput);
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

        private void toggle() {
            filter.setEnabled(!filter.isEnabled());
            updateColours();
        }

        private void delete() {
            Chat.getInstance().getHiddenMessages().getValue().remove(filter);
            ChatHiderGui.this.container.removeChild(this);
        }

        private void updateColours() {
            toggle.color = filter.isEnabled() ? Palette.elementHighlight : Palette.elementFill;
            toggle.setText(filter.isEnabled() ? "On" : "Off");
        }
    }

    @Override
    public void onClose() {
        super.onClose();
        Chat.save();
    }
}
