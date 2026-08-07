package org.rexi.customChat.commands;

import net.kyori.adventure.text.Component;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.rexi.customChat.CustomChat;


public class ChatlockCommand implements CommandExecutor {
    private final CustomChat plugin;

    public ChatlockCommand(CustomChat plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("customchat.chatlock")) {
            sender.sendMessage(plugin.getMessage("no_permission"));
            return true;
        }

        String name = sender instanceof Player ? sender.getName() : plugin.messagesFile.getConfig().getString("messages.console", "Console");

        Component message;

        if (plugin.chatlockActive) {
            plugin.chatlockActive = false;
            sender.sendMessage(plugin.getMessage("chatlock_disabled"));
            message = plugin.getMessage("chatlock_end", "{player}", name);
        } else {
            plugin.chatlockActive = true;
            sender.sendMessage(plugin.getMessage("chatlock_enabled"));
            message = plugin.getMessage("chatlock_start", "{player}", name);
        }

        for (Player player : plugin.getServer().getOnlinePlayers()) {
            player.sendMessage(message);
        }
        plugin.getServer().getConsoleSender().sendMessage(message);

        return true;
    }
}
