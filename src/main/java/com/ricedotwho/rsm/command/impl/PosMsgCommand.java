package com.ricedotwho.rsm.command.impl;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.ricedotwho.rsm.command.Command;
import com.ricedotwho.rsm.command.api.CommandInfo;
import com.ricedotwho.rsm.data.Pos;
import com.ricedotwho.rsm.module.impl.dungeon.posmsg.PosMsg;
import com.ricedotwho.rsm.utils.ChatUtils;
import lombok.Getter;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;

import java.util.*;
import java.util.concurrent.CompletableFuture;

@CommandInfo(name = "posmsg", aliases = "pm", description = "Add or remove PosMsg")
public class PosMsgCommand extends Command {

    @Override
    public LiteralArgumentBuilder<ClientSuggestionProvider> build() {
        return literal(name())
                .then(literal("add")
                        .then(argument("message", StringArgumentType.string())
                                .executes(this::create)
                                .then(argument("args", new MsgListArgumentType())
                                        .executes(ctx -> create(ctx, MsgListArgumentType.get(ctx, "args")))
                                )
                        )
                )
                .then(literal("remove")
                        .then(argument("message", StringArgumentType.greedyString())
                                .executes(ctx -> {
                                    String message = StringArgumentType.getString(ctx, "message");
                                    if (PosMsg.remove(message)) {
                                        ChatUtils.chat("Removed \"%s\"", message);
                                    } else {
                                        ChatUtils.chat("No message found for \"%s\"", message);
                                    }
                                    return 1;
                                })
                        )
                )
                .then(literal("list")
                        .executes(ctx -> {
                            PosMsg.getMsgs().forEach(msg -> ChatUtils.chat("\"%s\" at %s", msg.message, msg.lower.toChatString()));
                            return 1;
                        })
                )
                .then(literal("clear")
                        .executes(ctx -> {
                            PosMsg.clear();
                            ChatUtils.chat("Cleared");
                            return 1;
                        })
                );
    }

    private int create(CommandContext<ClientSuggestionProvider> ctx) {
        return create(ctx, new HashMap<>());
    }

    private int create(CommandContext<ClientSuggestionProvider> ctx, Map<DimensionType, DimensionArg<?>> args) {
        String message = StringArgumentType.getString(ctx, "message").trim();
        Pos player = getViewerPos(args.containsKey(DimensionType.EXACT));
        Pos dims;
        if (args.containsKey(DimensionType.RADIUS)) {
            double r = (double) args.get(DimensionType.RADIUS).getValue();
            dims = new Pos(r, args.containsKey(DimensionType.HEIGHT) ? (double) args.get(DimensionType.HEIGHT).getValue() : 0.5D, r);
        } else {
            DimensionArg<?> t;
            dims = new Pos(
                    (t = args.get(DimensionType.WIDTH)) == null ? 0.5D : (double) t.getValue(),
                    (t = args.get(DimensionType.HEIGHT)) == null ? 0.5D : (double) t.getValue(),
                    (t = args.get(DimensionType.LENGTH)) == null ? 0.5D : (double) t.getValue()
            );
        }
        PosMsg.Msg msg = new PosMsg.Msg(
                player.add(dims.x(), dims.y() * 2, dims.z()),
                player.subtract(dims.x(), 0, dims.z()),
                !args.containsKey(DimensionType.SELF_ONLY),
                !args.containsKey(DimensionType.OTHERS_ONLY),
                args.containsKey(DimensionType.SILENT),
                args.containsKey(DimensionType.NO_TITLE),
                message
        );
        if (PosMsg.add(msg)) {
            ChatUtils.chat("Added \"%s\"", msg.message);
        } else {
            ChatUtils.chat("Failed to add PosMsg");
        }
        return 1;
    }

    public static class MsgArgumentType implements ArgumentType<DimensionArg<?>> {
        private static final DynamicCommandExceptionType INVALID =
                new DynamicCommandExceptionType(o -> Component.literal("Invalid dimension: " + o));

        public DimensionArg<?> parse(StringReader reader) throws CommandSyntaxException {
            StringBuilder prefix = new StringBuilder();
            while (reader.canRead() && Character.isLetter(reader.peek())) {
                prefix.append(reader.read());
            }

            DimensionType type = DimensionType.byPrefix(prefix.toString());
            if (type == null) throw INVALID.createWithContext(reader, prefix.toString());

            if (type.getValueClass() == Void.class) {
                return new DimensionArg<>(type, null, false);
            }

            int numberStart = reader.getCursor();
            while (reader.canRead() && ("0123456789.".indexOf(reader.peek()) != -1)) reader.skip();
            String part = reader.getString().substring(numberStart, reader.getCursor());
            try {
                return new DimensionArg<>(type, Double.parseDouble(part), true);
            } catch (NumberFormatException e) {
                throw INVALID.createWithContext(reader, part);
            }
        }

        @Override
        public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
            String remaining = builder.getRemaining();

            for (DimensionType t : DimensionType.values()) {
                if (t.getName().startsWith(remaining)) {
                    builder.suggest(t.getName());
                }
            }

            return builder.buildFuture();
        }

        public static DimensionArg<?> get(CommandContext<ClientSuggestionProvider> context, String name) {
            return context.getArgument(name, DimensionArg.class);
        }
    }

    @Getter
    public enum DimensionType {
        RADIUS("r", Double.class),
        WIDTH("w", Double.class),
        HEIGHT("h", Double.class),
        LENGTH("l", Double.class),
        SELF_ONLY("selfonly", Void.class),
        OTHERS_ONLY("othersonly", Void.class),
        NO_TITLE("notitle", Void.class),
        SILENT("silent", Void.class),
        EXACT("exact", Void.class);

        private final String name;
        private final Class<?> valueClass;

        DimensionType(String name, Class<?> valueClass) {
            this.name = name;
            this.valueClass = valueClass;
        }

        public static DimensionType byPrefix(String input) {
            for (DimensionType t : values()) {
                if (input.startsWith(t.name)) return t;
            }
            return null;
        }
    }

    public static class MsgListArgumentType implements ArgumentType<Map<DimensionType, DimensionArg<?>>> {

        private final MsgArgumentType single = new MsgArgumentType();

        @Override
        public Map<DimensionType, DimensionArg<?>> parse(StringReader reader) throws CommandSyntaxException {
            Map<DimensionType, DimensionArg<?>> result = new EnumMap<>(DimensionType.class);

            while (reader.canRead()) {
                int before = reader.getCursor();
                DimensionArg<?> arg = single.parse(reader);

                if (result.putIfAbsent(arg.type(), arg) != null) {
                    throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherParseException().createWithContext(reader, "Duplicate argument: " + arg.type().getName());
                }

                if (reader.canRead() && reader.peek() == ' ') {
                    reader.skip();
                }

                if (reader.getCursor() == before) {
                    throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherUnknownArgument()
                            .createWithContext(reader);
                }
            }

            return result;
        }

        @Override
        public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
            String input = builder.getRemaining();
            StringReader reader = new StringReader(input);
            boolean end = input.endsWith(" ");

            Set<DimensionType> used = EnumSet.noneOf(DimensionType.class);

            try {
                while (reader.canRead()) {
                    int before = reader.getCursor();
                    DimensionArg<?> arg = single.parse(reader);
                    used.add(arg.type());

                    if (reader.canRead() && reader.peek() == ' ') {
                        reader.skip();
                    } else {
                        break;
                    }

                    if (reader.getCursor() == before) break;
                }
            } catch (CommandSyntaxException ignored) {

            }

            if (!end && reader.getCursor() == input.length()) {
                return Suggestions.empty();
            }

            int offset = builder.getStart() + reader.getCursor();
            SuggestionsBuilder b = builder.createOffset(offset);
            String remaining = b.getRemaining().toLowerCase();
            for (DimensionType t : DimensionType.values()) {
                if (used.contains(t)) continue;
                String name = t.getName();
                if (name.startsWith(remaining)) {
                    b.suggest(name);
                }
            }

            return b.buildFuture();
        }

        @SuppressWarnings("unchecked")
        public static Map<DimensionType, DimensionArg<?>> get(CommandContext<?> ctx, String name) {
            return (Map<DimensionType, DimensionArg<?>>) ctx.getArgument(name, Map.class);
        }
    }

    public record DimensionArg<T>(DimensionType type, T value, boolean hasValue) {
        public T getValue() {
            return hasValue ? value : null;
        }
    }

    private Pos getViewerPos(boolean exact) {
        Entity camera = mc.getCameraEntity();
        return exact ? new Pos(camera.getX(), camera.getY(), camera.getZ())
                : new Pos(Math.round(camera.getX() * 2) / 2.0, Math.round(camera.getY() * 2) / 2.0, Math.round(camera.getZ() * 2) / 2.0);
    }
}
