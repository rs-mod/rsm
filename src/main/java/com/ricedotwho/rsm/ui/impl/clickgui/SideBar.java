package com.ricedotwho.rsm.ui.impl.clickgui;

import com.ricedotwho.rsm.module.api.Category;
import com.ricedotwho.rsm.module.api.Module;
import com.ricedotwho.rsm.module.api.ModuleManager;
import com.ricedotwho.rsm.type.BKTree;
import com.ricedotwho.rsm.ui.api.Node;
import com.ricedotwho.rsm.ui.api.Palette;
import com.ricedotwho.rsm.ui.api.TextAlignment;
import com.ricedotwho.rsm.ui.impl.clickgui.sidebar.ModuleButton;
import com.ricedotwho.rsm.ui.impl.elements.TextInputHandler;
import com.ricedotwho.rsm.ui.impl.nodes.RectangleNode;
import lombok.val;
import org.jspecify.annotations.NonNull;

import java.util.*;
import java.util.stream.Collectors;

public class SideBar extends Node {
    final TextInputHandler searchBar;
    final static String[] searchPrompt = {""};
    private final RectangleNode moduleButtonContainer;
    private final ArrayList<ModuleButton> moduleButtons;
    private final BKTree searchTree = new BKTree();

    SideBar(Contents contents) {
        val n = new YogaNodeBuilder()
                .display(Display.FLEX)
                .flexDirection(FlexDirection.COLUMN)
                .gap(16)
                .width(160)
                .build();
        super(n, null);

        searchBar = createSearchBar(this);

        moduleButtonContainer = new RectangleNode.Builder()
                .overflow(Node.Overflow.SCROLL)
                .display(Node.Display.FLEX)
                .flexDirection(Node.FlexDirection.COLUMN)
                .gap(8)
                .flexGrow(1f)
                .build();

        addChild(moduleButtonContainer);


        ArrayList<ModuleButton> buttons = new ArrayList<>();
        for (Module module : ModuleManager.getModules()) {
            buttons.add(new ModuleButton(module, contents));
            searchTree.add(module.getName().toLowerCase());
        }
        buttons.sort((b1, b2) -> b1.getModule().getName().compareToIgnoreCase(b2.getModule().getName()));
        moduleButtons = buttons;
        updateModuleContainer();
    }

    private String lastSearch = searchPrompt[0];
    private boolean wasSearching = false;
    private Category lastCategory = ClickGui.currentCategory;

    private void updateModuleContainer() {
        List<ModuleButton> modules;
        if (searchPrompt[0].isEmpty()) {
            if (searchBar.isListening()) {
                modules = moduleButtons;
            } else {
                modules = getModulesInCategory(ClickGui.currentCategory);
            }
        } else {
            modules = getModulesFromSearch();
        }

        if (new HashSet<>(modules).size() != modules.size()) {
            throw new IllegalStateException("Duplicate ModuleButton () in modules list: " + modules);
        }

        moduleButtonContainer.clearChildren();

        for (ModuleButton module : modules) {
            moduleButtonContainer.addChild(module);
        }
    }

    public List<ModuleButton> getModulesInCategory(Category category) {
        return moduleButtons.stream()
                .filter(moduleButton -> moduleButton.getModule().getCategory() == category)
                .toList();
    }

    private List<ModuleButton> getModulesFromSearch() {
        String query = searchPrompt[0].toLowerCase();

        List<ModuleButton> prefixMatches = moduleButtons.stream()
                .filter(b -> b.getModule().getName().toLowerCase().startsWith(query))
                .sorted(Comparator.comparing(b -> b.getModule().getName().length()))
                .collect(Collectors.toCollection(ArrayList::new));

        Set<String> already = prefixMatches.stream()
                .map(b -> b.getModule().getName().toLowerCase())
                .collect(Collectors.toSet());

        val searchResult = searchTree.search(query, 6);
        List<ModuleButton> fuzzyMatches = getModuleButtons(searchResult, already);

        prefixMatches.addAll(fuzzyMatches);
        return prefixMatches;
    }

    private @NonNull List<ModuleButton> getModuleButtons(List<Map.Entry<String, Integer>> searchResult, Set<String> already) {
        List<ModuleButton> fuzzyMatches = new ArrayList<>();
        var remaining = new ArrayList<>(moduleButtons);
        for (Map.Entry<String, Integer> entry : searchResult) {
            if (already.contains(entry.getKey())) continue;
            val iterator = remaining.iterator();
            while (iterator.hasNext()) {
                val module = iterator.next();
                if (!module.getModule().getName().equalsIgnoreCase(entry.getKey())) continue;
                fuzzyMatches.add(module);
                iterator.remove();
                break;
            }
        }
        return fuzzyMatches;
    }


    private static TextInputHandler createSearchBar(Node container) {
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
    public void frame(float parentX, float parentY, float mouseX, float mouseY, float scrollY) {
        if (lastCategory != ClickGui.currentCategory || !Objects.equals(lastSearch, searchPrompt[0]) || searchBar.isListening() != wasSearching) {
            updateModuleContainer();
            lastSearch = searchPrompt[0];
            lastCategory = ClickGui.currentCategory;
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
