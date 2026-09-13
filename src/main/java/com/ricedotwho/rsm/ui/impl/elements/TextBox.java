package com.ricedotwho.rsm.ui.impl.elements;

import com.ricedotwho.rsm.ui.api.Palette;
import com.ricedotwho.rsm.ui.api.TextAlignment;
import lombok.val;
import org.jspecify.annotations.Nullable;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class TextBox extends TextInputHandler {
    public TextBox(
            Supplier<String> textSupplier,
            Consumer<String> textConsumer,
            String placeHolder,
            Runnable onUnlisten,
            @Nullable String forbiddenCharacters,
            long yogaNode,
            TextAlignment alignment,
            int maxLength
    ) {
        super(
                yogaNode,
                textSupplier,
                textConsumer,
                placeHolder,
                onUnlisten,
                Palette.text,
                Palette.textHighlighted,
                Palette.descriptions, //this is to be set later
                alignment,
                Palette.fontSize, Palette.font,
                true,
                forbiddenCharacters,
                Palette.elementBackgroundDark,
                null,
                Palette.strokeThickness,
                Palette.stroke,
                maxLength
        );
    }

    public TextBox(
            Supplier<String> textSupplier,
            Consumer<String> textConsumer,
            int maxLength
    ) {
        val yogaNode = new YogaNodeBuilder()
                .width(Palette.largeElementWidth)
                .height(Palette.largeElementHeight)
                .padding(Palette.elementInteriorPadding)
                .build();
        this(textSupplier, textConsumer, "", () -> {}, null, yogaNode, TextAlignment.CenterLeft, maxLength);
    }

    public TextBox(
            long yogaNode,
            Supplier<String> textSupplier,
            Consumer<String> textConsumer
    ) {
        this(textSupplier, textConsumer, "", () -> {}, null, yogaNode, TextAlignment.CenterLeft, 35);
    }


    public TextBox(
            long yogaNode,
            Supplier<String> textSupplier,
            Consumer<String> textConsumer,
            String placeHolder,
            Runnable onUnlisten,
            @Nullable String allowedCharacters
    ) {
        this(textSupplier, textConsumer, placeHolder, onUnlisten, allowedCharacters, yogaNode, TextAlignment.CenterMiddle, 35);
    }
}
