package org.rexi.customChat.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.rexi.customChat.CustomChat;
import org.rexi.customChat.utils.UpdateChecker;

public class CustomchatCommand implements CommandExecutor {

    private final CustomChat plugin;
    private final UpdateChecker updateChecker;

    public CustomchatCommand(CustomChat plugin, UpdateChecker updateChecker) {
        this.updateChecker = updateChecker;
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("customchat.admin")) {
            sender.sendMessage(plugin.getMessage("no_permission"));
            return true;
        }

        if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            plugin.reloadAllConfigs();
            plugin.setMenuItems();
            sender.sendMessage(plugin.getMessage("config_reload"));
            return true;
        }

        if (args.length == 1 && args[0].equalsIgnoreCase("version")) {
            updateChecker.checkForUpdatesCommand(sender);
            return true;
        }

        sender.sendMessage(plugin.getMessage("usage"));
        return true;
    }
}
