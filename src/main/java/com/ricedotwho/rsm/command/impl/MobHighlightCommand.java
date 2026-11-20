package com.ricedotwho.rsm.command.impl;


import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.ricedotwho.rsm.command.Command;
import com.ricedotwho.rsm.command.api.CommandInfo;
import com.ricedotwho.rsm.module.impl.render.MobHighlight;
import com.ricedotwho.rsm.utils.ChatUtils;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;

@CommandInfo(name = "highlight", description = "Custom mob highlight and stuff")
public class MobHighlightCommand extends Command {

    @Override
    public LiteralArgumentBuilder<ClientSuggestionProvider> build() {
        return literal(name())
                .then(literal("add")
                        .then(argument("name", StringArgumentType.greedyString())
                                .executes(ctx -> {
                                    var name = StringArgumentType.getString(ctx, "name");
                                    MobHighlight.getInstance().getCustomEsp().getValue().add(name);
                                    var comp = Component.literal("Added esp '$name'");
                                    if (!MobHighlight.getInstance().getCustomTracer().getValue().contains(name)) {
                                        comp.append(
                                                Component.literal(" [Add Tracer]")
                                                        .withColor(TextColor.RED)
                                                        .withStyle(s -> s.withClickEvent(new ClickEvent.RunCommand("`esp tracer add $name")))
                                        );
                                    }
                                    ChatUtils.chat(comp);
                                    return 1;
                                })
                        )
                )
                .then(literal("remove")
                        .then(argument("name", StringArgumentType.greedyString())
                                .executes(ctx -> {
                                    var name = StringArgumentType.getString(ctx, "name");
                                    MobHighlight.getInstance().getCustomEsp().getValue().remove(name);
                                    var comp = Component.literal("Removed esp '$name'");
                                    if (MobHighlight.getInstance().getCustomTracer().getValue().contains(name)) {
                                        comp.append(
                                                Component.literal(" [Remove Tracer]")
                                                        .withColor(TextColor.RED)
                                                        .withStyle(s -> s.withClickEvent(new ClickEvent.RunCommand("`esp tracer remove $name")))
                                        );
                                    }
                                    ChatUtils.chat(comp);
                                    return 1;
                                })
                        )
                )
                .then(literal("tracer")
                        .then(literal("add")
                                .then(argument("name", StringArgumentType.greedyString())
                                        .executes(ctx -> {
                                            var name = StringArgumentType.getString(ctx, "name");
                                            MobHighlight.getInstance().getCustomTracer().getValue().add(name);
                                            var comp = Component.literal("Added tracer '$name'");
                                            if (!MobHighlight.getInstance().getCustomEsp().getValue().contains(name)) {
                                                comp.append(
                                                        Component.literal(" [Add ESP]")
                                                                .withColor(TextColor.GREEN)
                                                                .withStyle(s -> s.withClickEvent(new ClickEvent.RunCommand("`esp add $name")))
                                                );
                                            }
                                            ChatUtils.chat(comp);
                                            return 1;
                                        })
                                )
                        )
                        .then(literal("remove")
                                .then(argument("name", StringArgumentType.greedyString())
                                        .executes(ctx -> {
                                            var name = StringArgumentType.getString(ctx, "name");
                                            MobHighlight.getInstance().getCustomTracer().getValue().remove(name);
                                            var comp = Component.literal("Removed tracer '$name'");
                                            if (MobHighlight.getInstance().getCustomEsp().getValue().contains(name)) {
                                                comp.append(
                                                        Component.literal(" [Remove ESP]")
                                                                .withColor(TextColor.RED)
                                                                .withStyle(s -> s.withClickEvent(new ClickEvent.RunCommand("`esp remove $name")))
                                                );
                                            }
                                            ChatUtils.chat(comp);
                                            return 1;
                                        })
                                )
                        )
                );
    }
}
