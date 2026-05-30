package com.ricedotwho.rsm.command.impl;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.ricedotwho.rsm.command.Command;
import com.ricedotwho.rsm.command.api.CommandInfo;
import com.ricedotwho.rsm.command.arguments.ModuleArgumentType;
import com.ricedotwho.rsm.module.Module;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;

@CommandInfo(name = "toggle", aliases = "t", description = "Toggles a module")
public class ToggleCommand extends Command {

    @Override
    public LiteralArgumentBuilder<ClientSuggestionProvider> build() {
        return literal(name())
                .then(argument("module", ModuleArgumentType.moduleArgument())
                        .executes(ctx -> {
                            Module module = ModuleArgumentType.getModule(ctx, "module");
                            module.onKeyToggle();
                            return 1;
                        })
                );
    }
}
