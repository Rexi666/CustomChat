package org.rexi.customChat.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.rexi.customChat.CustomChat;

public class CustomchatCommand implements CommandExecutor {

    private final CustomChat plugin;

    public CustomchatCommand(CustomChat plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("customchat.admin")) {
            sender.sendMessage(plugin.deserialize(plugin.getConfig().getString("messages.no_permission", "&cYou do not have permission to use this command.")));
            return true;
        }

        if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            plugin.reloadConfig();
            plugin.loadFormats();
            sender.sendMessage(plugin.deserialize(plugin.getConfig().getString("messages.reload_success", "&aConfiguration reloaded successfully!")));
            return true;
        }

        sender.sendMessage(plugin.deserialize(plugin.getConfig().getString("messages.usage", "&cUsage: /customchat reload")));
        return true;
    }
}
