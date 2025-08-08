package org.rexi.customChat.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.rexi.customChat.CustomChat;
import org.rexi.customChat.menus.ChatColorMenu;

public class ChatColorCommand implements CommandExecutor {

    private final CustomChat plugin;

    public ChatColorCommand(CustomChat plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(plugin.getMessage("noconsole"));
            return true;
        }

        if (!player.hasPermission("customchat.chatcolor")) {
            player.sendMessage(plugin.getMessage("no_permission"));
            return true;
        }

        new ChatColorMenu(plugin, player).openMainMenu();
        return true;
    }
}