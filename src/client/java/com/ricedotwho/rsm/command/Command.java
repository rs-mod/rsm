package com.ricedotwho.rsm.command;

import com.ricedotwho.rsm.command.api.CommandInfo;
import com.ricedotwho.rsm.utils.Accessor;

public abstract class Command implements Accessor {

    private CommandInfo info;

    public Command() {
        if (this.getClass().isAnnotationPresent(CommandInfo.class)) {
            info = this.getClass().getAnnotation(CommandInfo.class);
        } else {
            throw new RuntimeException("Command doesn't have a CommandInfo annotation");
        }
    }

    public abstract void execute(final String[] args, final String message);

    public String[] getAliases() {
        return this.info.aliases();
    }

    public CommandInfo getInfo() {
        return getClass().getAnnotation(CommandInfo.class);
    }
}
