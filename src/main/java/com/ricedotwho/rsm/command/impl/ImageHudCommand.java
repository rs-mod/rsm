package com.ricedotwho.rsm.command.impl;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.ricedotwho.rsm.command.Command;
import com.ricedotwho.rsm.command.api.CommandInfo;
import com.ricedotwho.rsm.module.impl.render.ImageHud;
import com.ricedotwho.rsm.utils.ChatUtils;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;

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
                        .then(argument("name", StringArgumentType.string())
                                .executes(ctx -> {
                                    String name = StringArgumentType.getString(ctx, "name");
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
}
