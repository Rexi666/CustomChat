package org.rexi.customChat.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.rexi.customChat.CustomChat;
import org.rexi.customChat.utils.InventoryManager;
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
        if (args.length == 2 && args[0].equalsIgnoreCase("viewinv")) {
            if (!(sender instanceof Player player)) return true;
            String sha1 = args[1];
            Inventory inv = InventoryManager.getInventory(sha1);

            if (inv == null) {
                player.sendMessage(plugin.getMessage("inv_not_available"));
                return true;
            }

            player.openInventory(inv);
            InventoryManager.opened.put(player.getUniqueId(), true);
            return true;
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("viewender")) {
            if (!(sender instanceof Player player)) return true;
            String sha1 = args[1];
            Inventory inv = InventoryManager.getInventory(sha1);

            if (inv == null) {
                player.sendMessage(plugin.getMessage("ender_not_available"));
                return true;
            }

            player.openInventory(inv);
            InventoryManager.opened.put(player.getUniqueId(), true);
            return true;
        }

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
