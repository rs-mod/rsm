package com.ricedotwho.rsm.mixins;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.context.CommandContextBuilder;
import com.mojang.brigadier.context.SuggestionContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.ricedotwho.rsm.RSM;
import com.ricedotwho.rsm.module.impl.render.ClickGUI;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.CommandSuggestions;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import org.spongepowered.asm.mixin.*;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Mixin(CommandSuggestions.class)
public class MixinCommandSuggestions {

    @Unique
    private static final Pattern WHITESPACE_PATTERN = Pattern.compile("(\\s+)");

    @Shadow
    @Final
    EditBox input;

    @Shadow
    @Final
    private boolean commandsOnly;

    @Shadow
    private ParseResults<ClientSuggestionProvider> currentParse;

    @Shadow
    boolean keepSuggestions;

    @Shadow
    private CommandSuggestions.SuggestionsList suggestions;

    @Shadow
    @Final
    Minecraft minecraft;

    @Shadow
    @Final
    private final List<FormattedCharSequence> commandUsage = Lists.newArrayList();

    @Shadow
    @Final
    private boolean onlyShowIfCursorPastError;

    @Shadow
    private CompletableFuture<Suggestions> pendingSuggestions;

    @Shadow
    private int commandUsagePosition;

    @Shadow
    private int commandUsageWidth;

    @Shadow
    @Final
    private Screen screen;

    @Shadow
    @Final
    Font font;

    @Shadow
    private boolean allowSuggestions;

    /**
     * @author ricedotwho
     * @reason custom command chat completions
     * id ont like overwrite but
     */
    @Overwrite
    public void updateCommandInfo() {
        String string = this.input.getValue();
        if (this.currentParse != null && !this.currentParse.getReader().getString().equals(string)) {
            this.currentParse = null;
        }

        if (!this.keepSuggestions) {
            this.input.setSuggestion((String)null);
            this.suggestions = null;
        }

        this.commandUsage.clear();
        StringReader stringReader = new StringReader(string);
        boolean custom = stringReader.canRead() && stringReader.peek() == RSM.getModule(ClickGUI.class).getCommandPrefix().getValue().charAt(0);
        boolean isCommand = stringReader.canRead() && (stringReader.peek() == '/' || custom);
        if (isCommand) {
            stringReader.skip();
        }
        assert this.minecraft.player != null;

        boolean bl2 = this.commandsOnly || isCommand;
        int i = this.input.getCursorPosition();
        if (bl2) {
            CommandDispatcher<ClientSuggestionProvider> commandDispatcher = custom ? RSM.getInstance().getCommandManager().getDispatcher() : this.minecraft.player.connection.getCommands();
            if (this.currentParse == null) {
                this.currentParse = commandDispatcher.parse(stringReader, this.minecraft.player.connection.getSuggestionsProvider());
            }

            int j = this.onlyShowIfCursorPastError ? stringReader.getCursor() : 1;
            if (i >= j && (this.suggestions == null || !this.keepSuggestions)) {
                this.pendingSuggestions = commandDispatcher.getCompletionSuggestions(this.currentParse, i);
                this.pendingSuggestions.thenRun(() -> {
                    if (this.pendingSuggestions.isDone()) {
                        this.updateUsageInfo();
                    }
                });
            }
        } else {
            String string2 = string.substring(0, i);
            int j = getLastWordIndex(string2);
            Collection<String> collection = this.minecraft.player.connection.getSuggestionsProvider().getCustomTabSugggestions();
            this.pendingSuggestions = SharedSuggestionProvider.suggest(collection, new SuggestionsBuilder(string2, j));
        }
    }

    @Unique
    private static int getLastWordIndex(String string) {
        if (Strings.isNullOrEmpty(string)) {
            return 0;
        } else {
            int i = 0;

            for (Matcher matcher = WHITESPACE_PATTERN.matcher(string); matcher.find(); i = matcher.end()) {
            }

            return i;
        }
    }

    @Unique
    private void updateUsageInfo() {
        boolean bl = false;
        if (this.input.getCursorPosition() == this.input.getValue().length()) {
            if (this.pendingSuggestions.join().isEmpty() && !this.currentParse.getExceptions().isEmpty()) {
                int i = 0;

                for(Map.Entry<CommandNode<ClientSuggestionProvider>, CommandSyntaxException> entry : this.currentParse.getExceptions().entrySet()) {
                    CommandSyntaxException commandSyntaxException = (CommandSyntaxException)entry.getValue();
                    if (commandSyntaxException.getType() == CommandSyntaxException.BUILT_IN_EXCEPTIONS.literalIncorrect()) {
                        ++i;
                    } else {
                        this.commandUsage.add(getExceptionMessage(commandSyntaxException));
                    }
                }

                if (i > 0) {
                    this.commandUsage.add(getExceptionMessage(CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherUnknownArgument().createWithContext(this.currentParse.getReader())));
                }
            } else if (this.currentParse.getReader().canRead()) {
                bl = true;
            }
        }

        this.commandUsagePosition = 0;
        this.commandUsageWidth = this.screen.width;
        if (this.commandUsage.isEmpty() && !this.fillNodeUsage(ChatFormatting.GRAY) && bl) {
            this.commandUsage.add(getExceptionMessage(Commands.getParseException(this.currentParse)));
        }

        this.suggestions = null;
        if (this.allowSuggestions && this.minecraft.options.autoSuggestions().get()) {
            ((CommandSuggestions) (Object) this).showSuggestions(false);
        }

    }

    @Unique
    private static FormattedCharSequence getExceptionMessage(CommandSyntaxException commandSyntaxException) {
        Component component = ComponentUtils.fromMessage(commandSyntaxException.getRawMessage());
        String string = commandSyntaxException.getContext();
        return string == null ? component.getVisualOrderText() : Component.translatable("command.context.parse_error", component, commandSyntaxException.getCursor(), string).getVisualOrderText();
    }

    @Unique
    private boolean fillNodeUsage(ChatFormatting chatFormatting) {
        List<FormattedCharSequence> list = Lists.newArrayList();
        Style style = Style.EMPTY.withColor(chatFormatting);
        int i = 0;

        CommandContextBuilder<ClientSuggestionProvider> commandContextBuilder = this.currentParse.getContext();
        SuggestionContext<ClientSuggestionProvider> suggestionContext = commandContextBuilder.findSuggestionContext(this.input.getCursorPosition());
        int startPos = suggestionContext.startPos;
        Map<CommandNode<ClientSuggestionProvider>, String> map = this.minecraft.player.connection.getCommands().getSmartUsage(suggestionContext.parent, this.minecraft.player.connection.getSuggestionsProvider());

        for(Map.Entry<CommandNode<ClientSuggestionProvider>, String> entry : map.entrySet()) {
            if (!(entry.getKey() instanceof LiteralCommandNode)) {
                list.add(FormattedCharSequence.forward(entry.getValue(), style));
                i = Math.max(i, this.font.width(entry.getValue()));
            }
        }

        if (!list.isEmpty()) {
            this.commandUsage.addAll(list);
            this.commandUsagePosition = Mth.clamp(this.input.getScreenX(startPos), 0, this.input.getScreenX(0) + this.input.getInnerWidth() - i);
            this.commandUsageWidth = i;
            return true;
        } else {
            return false;
        }
    }
}
