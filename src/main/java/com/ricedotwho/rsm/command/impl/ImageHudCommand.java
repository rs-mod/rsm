package com.ricedotwho.rsm.command.impl;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.ricedotwho.rsm.command.Command;
import com.ricedotwho.rsm.command.api.CommandInfo;
import com.ricedotwho.rsm.command.arguments.StringCompletingArgument;
import com.ricedotwho.rsm.module.impl.render.ImageHud;
import com.ricedotwho.rsm.utils.ChatUtils;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;

import java.util.Objects;

@CommandInfo(name = "ih", aliases = "imagehud", description = "Add or remove images for ImageHud")
public class ImageHudCommand extends Command {

    @Override
    public LiteralArgumentBuilder<ClientSuggestionProvider> build() {
        return literal(name())
                .then(literal("add")
                        .executes(_ -> {
                            String copied = mc.keyboardHandler.getClipboard();
                            if (ImageHud.add(copied)) {
                                ChatUtils.chat("Added {}", copied);
                            } else {
                                ChatUtils.chat("Link is not valid! ({})", copied);
                            }
                            return 1;
                        })
                )
                .then(literal("remove")
                        .then(argument("name", argument())
                                .executes(ctx -> {
                                    String name = StringCompletingArgument.get(ctx, "name");
                                    if (ImageHud.remove(name)) {
                                        ChatUtils.chat("Removed {}", name);
                                    } else {
                                        ChatUtils.chat("No image with name \"{}\" was found!", name);
                                    }
                                    return 1;
                                })
                        )
                )
                .then(literal("list")
                        .executes(_ -> {
                            ImageHud.getInstance().getImages().values().forEach(im -> ChatUtils.chat(im.name + " Url: " + im.url));
                            return 1;
                        })
                )
                .then(literal("load")
                        .executes(_ -> {
                            ImageHud.getInstance().reload();
                            ChatUtils.chat("Loaded!");
                            return 1;
                        })
                );
    }

    private StringCompletingArgument argument() {
        return new StringCompletingArgument(() ->  ImageHud.getInstance().getImages().entrySet()
                .filter(e -> !Objects.equals(e.getValue().url, "LOCAL"))
                .map(e -> e.getKey().getName())
                .toList(), greedy:true);
    }
}
