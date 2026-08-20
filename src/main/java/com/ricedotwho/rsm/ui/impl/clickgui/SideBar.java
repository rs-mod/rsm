package com.ricedotwho.rsm.ui.impl.clickgui;

import com.ricedotwho.rsm.module.api.Category;
import com.ricedotwho.rsm.module.api.Module;
import com.ricedotwho.rsm.module.api.ModuleManager;
import com.ricedotwho.rsm.type.BKTree;
import com.ricedotwho.rsm.ui.api.Palette;
import com.ricedotwho.rsm.ui.api.TextAlignment;
import com.ricedotwho.rsm.ui.api.UiElement;
import com.ricedotwho.rsm.ui.api.Widget;
import com.ricedotwho.rsm.ui.impl.clickgui.sidebar.ModuleButton;
import com.ricedotwho.rsm.ui.impl.elements.TextInputHandler;
import com.ricedotwho.rsm.ui.impl.nodes.RectangleNode;
import lombok.Getter;
import lombok.val;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Objects;
import java.util.stream.Collectors;

public class SideBar extends Widget {
    final TextInputHandler searchBar;
    final static String[] searchPrompt = {""};
    @Getter
    private final RectangleNode moduleButtonContainer;
    private ArrayList<ModuleButton> moduleButtons;
    private final BKTree<ModuleButton> searchTree = new BKTree<>();
    private final ModuleManager moduleManager;
    private final ClickGui clickGui;

    SideBar(Contents contents, ModuleManager moduleManager, ClickGui clickGui) {
        this.moduleManager = moduleManager;
        val n = new RectangleNode.Builder()
                .display(Display.FLEX)
                .flexDirection(FlexDirection.COLUMN)
                .gap(16)
                .width(160)
                .build();
        super(n);
        this.clickGui = clickGui;
        lastCategory = clickGui.currentCategory;

        searchBar = createSearchBar(this);

        moduleButtonContainer = new RectangleNode.Builder()
                .display(Display.FLEX)
                .flexDirection(FlexDirection.COLUMN)
                .gap(8)
                .flexGrow(1f)
                .flexShrink(1f)
                .overflow(Overflow.SCROLL)
                .build();

        addChild(moduleButtonContainer);

        updateModuleButtons(contents, moduleButtonContainer);
        updateModuleContainer(contents);
    }

    @Override
    public void dispatchMouseClickedUncancelable(int button, float parentX, float parentY, float mouseX, float mouseY, float scrollY) {
        if (!isInteractable() || !isVisible()) return;
        val originX = originX(parentX);
        val originY = originY(parentY);

        moduleButtonContainer.dispatchMouseClickedUncancelable(button, originX, originY, mouseX, mouseY, scrollY);

        if (!moduleButtonContainer.isHovered(originX, originY, mouseX, mouseY, scrollY)) {
            searchBar.dispatchMouseClickedUncancelable(button, originX, originY, mouseX, mouseY, scrollY);
        }

        mouseClickedUncancelable(button, parentX, parentY, mouseX, mouseY, scrollY);
    }

    void updateModuleButtons(Contents contents, RectangleNode container) {
        ArrayList<ModuleButton> buttons = new ArrayList<>();
        for (Module module : moduleManager.getModules()) {
            val button = new ModuleButton(module, contents, container, clickGui);
            buttons.add(button);
            searchTree.add(button, module.getName(), module.getInfo().aliases());
        }
        buttons.sort((b1, b2) -> b1.getModule().getName().compareToIgnoreCase(b2.getModule().getName()));
        moduleButtons = buttons;
    }

    private String lastSearch = searchPrompt[0];
    private boolean wasSearching = false;
    private Category lastCategory;

    private void updateModuleContainer(Contents contents) {
        ArrayList<ModuleButton> modules;
        if (searchPrompt[0].isEmpty()) {
            if (searchBar.isListening()) {
                modules = moduleButtons;
            } else {
                modules = getModulesInCategory(clickGui.currentCategory);
            }
            pushOpenButtonToTop(contents, modules);
        } else {
            modules = getModulesFromSearch();
            pushOpenButtonToTop(contents, modules);
        }

        if (new HashSet<>(modules).size() != modules.size()) {
            throw new IllegalStateException("Duplicate ModuleButton () in modules list: " + modules);
        }

        moduleButtonContainer.clearChildren();

        for (ModuleButton module : modules) {
            moduleButtonContainer.addChild(module);
        }
    }

    private void pushOpenButtonToTop(Contents contents, ArrayList<ModuleButton> modules) {
        val button = contents.getModule();
        if (button == null) return;
        if (button.getModule().getCategory() == clickGui.currentCategory) return;

        modules.removeIf(moduleButton -> contents.getModule() == moduleButton);
        modules.addFirst(button);
    }

    public ArrayList<ModuleButton> getModulesInCategory(Category category) {
        return moduleButtons.stream()
                .filter(moduleButton -> moduleButton.getModule().getCategory() == category)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private ArrayList<ModuleButton> getModulesFromSearch() {
        String query = searchPrompt[0].toLowerCase();
        return searchTree.search(query, 6)
                .stream()
                .map(BKTree.Result::getValue)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private static TextInputHandler createSearchBar(UiElement container) {
        val handler = new TextInputHandler.Builder()
                .height(36)
                .color(Palette.stroke)
                .rounding(5)
                .paddingLeft(10)
                .paddingRight(10)
                .textSupplier(() -> searchPrompt[0])
                .textConsumer(v -> searchPrompt[0] = v)
                .placeHolder("Search...")
                .textColor(Palette.text)
                .highlightColor(Palette.textHighlighted)
                .placeHolderColor(Palette.stroke)
                .textAlign(TextAlignment.CenterLeft)
                .fontSize(Palette.fontSizeLarge)
                .fontSupplier(Palette.font)
                .shadow(true)
                .build();

        container.addChild(handler);
        return handler;
    }

    @Override
    public void dispatchFrame(float parentX, float parentY, float mouseX, float mouseY, float scrollY) {
        super.dispatchFrame(parentX, parentY, mouseX, mouseY, scrollY);
        if (lastCategory != clickGui.currentCategory || !Objects.equals(lastSearch, searchPrompt[0]) || searchBar.isListening() != wasSearching) {
            updateModuleContainer(clickGui.getContents());
            lastSearch = searchPrompt[0];
            lastCategory = clickGui.currentCategory;
            wasSearching = searchBar.isListening();
        }
    }

    @Override
    public void close() {
        moduleButtonContainer.clearChildren();
        super.close();
        for (ModuleButton moduleButton : moduleButtons) {
            moduleButton.close();
        }
    }
}
