package com.cyr1en.commandprompter.unsafe;

import com.cyr1en.commandprompter.CommandPrompter;
import org.bukkit.command.SimpleCommandMap;

public class CommandMapHacker {
    private final CommandPrompter plugin;

    public CommandMapHacker(CommandPrompter plugin) throws NoSuchFieldException, IllegalAccessException {
        this.plugin = plugin;
        logWarning();
    }

    public void hackCommandMapIn(Object object, SimpleCommandMap newMap) throws NoSuchFieldException, IllegalAccessException {
        PvtFieldMutator.forField("commandMap").in(object).replaceWith(newMap);
        plugin.getPluginLogger().warn("Changed command map in '" + object.getClass().getSimpleName() + "' " +
                "to '" + newMap.getClass().getSimpleName() + "'");
    }

    private void logWarning() {
        plugin.getPluginLogger().warn("Warning! CommandPrompter is now going to use the modified command map.");
        plugin.getPluginLogger().warn("Changing the value of a private final variable can make your program unstable.");
        plugin.getPluginLogger().warn("If you experience any problem, please disable this feature immediately!");
    }
}
