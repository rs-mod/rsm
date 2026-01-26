package com.ricedotwho.rsm.module.impl.movement;

import com.ricedotwho.rsm.data.Colour;
import com.ricedotwho.rsm.data.Keybind;
import com.ricedotwho.rsm.module.Module;
import com.ricedotwho.rsm.module.api.Category;
import com.ricedotwho.rsm.module.api.ModuleInfo;
import com.ricedotwho.rsm.ui.clickgui.settings.impl.*;
import lombok.Getter;
import org.joml.Vector2d;

import java.util.Arrays;

@Getter
@ModuleInfo(aliases = "Example", id = "example", category = Category.MOVEMENT, hasKeybind = true, defaultKey = Keyboard.KEY_BACK)
public class Example extends Module {
    private final BooleanSetting booleanSetting = new BooleanSetting("Boolean", true);
    private final ButtonSetting buttonSetting = new ButtonSetting("Button", "A Button", () -> { /* ... */ });
    private final ColourSetting colourSetting = new ColourSetting("Colour", new Colour(-1));
    private final KeybindSetting keybindSetting = new KeybindSetting("Keybind", new Keybind(Keyboard.KEY_6, () -> { /* ... */ }));
    private final ModeSetting modeSetting = new ModeSetting("Mode", "Item 1", Arrays.asList("Item 1", "Item 2", "Item 3"));
    private final MultiBoolSetting multiBoolSetting = new MultiBoolSetting("MultiBool", Arrays.asList("Option 1", "Option 2", "Option 3"), Arrays.asList("Option 1", "Option 3"));
    private final NumberSetting numberSetting = new NumberSetting("Number", 0, 100, 67, 1);
    private final StringSetting stringSetting = new StringSetting("String", "A String Setting");

    private final GroupSetting groupSetting = new GroupSetting("Group 2");
    private final BooleanSetting booleanSetting2 = new BooleanSetting("Boolean 2", false);

    private final DragSetting dragSetting = new DragSetting("A Gui Element", new Vector2d(0, 0), new Vector2d(1, 1));

    public Example() {
        this.registerProperty(
                booleanSetting,
                buttonSetting,
                colourSetting,
                keybindSetting,
                modeSetting,
                multiBoolSetting,
                numberSetting,
                stringSetting,
                groupSetting,
                dragSetting // Drag settings must be registered for the editor to recognise them
        );
        groupSetting.add(booleanSetting2);
    }

    @Override
    public void onEnable() {
        this.keybindSetting.getValue().register();
    }

    @Override
    public void onDisable() {
        this.keybindSetting.getValue().deregister();
    }

    @Override
    public void reset() {

    }
}
