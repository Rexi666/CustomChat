package org.rexi.customChat.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.rexi.customChat.CustomChat;
import org.rexi.customChat.utils.InventoryManager;
import org.rexi.customChat.utils.UpdateChecker;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CustomchatCommand implements CommandExecutor, TabCompleter {

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
        } else if (args.length == 2 && args[0].equalsIgnoreCase("viewender")) {
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
        } else if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("customchat.admin")) {
                sender.sendMessage(plugin.getMessage("no_permission"));
                return true;
            }

            plugin.reloadAllConfigs();
            plugin.setMenuItems();
            sender.sendMessage(plugin.getMessage("config_reload"));
            return true;
        } else if (args.length == 1 && args[0].equalsIgnoreCase("version")) {
            if (!sender.hasPermission("customchat.admin")) {
                sender.sendMessage(plugin.getMessage("no_permission"));
                return true;
            }

            updateChecker.checkForUpdatesCommand(sender);
            return true;
        }

        if (args.length == 1 && args[0].equalsIgnoreCase("mentiontoggle")) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage(plugin.getMessage("noconsole"));
                return true;
            }

            if (!sender.hasPermission("customchat.mentiontoggle")) {
                sender.sendMessage(plugin.getMessage("no_permission"));
                return true;
            }

            boolean enabled = plugin.getMentionEnabled(player);

            if (!plugin.getConfig().getBoolean("mentioning.enabled")) {
                player.sendMessage(plugin.getMessage("mention_function_disabled"));
            }

            if (enabled) {
                plugin.setMentionEnabled(player, false);
                player.sendMessage(plugin.getMessage("mention_disabled"));
            } else {
                plugin.setMentionEnabled(player, true);
                player.sendMessage(plugin.getMessage("mention_enabled"));
            }

            return true;
        }

        sender.sendMessage(plugin.getMessage("usage"));
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        // Primer argumento: subcomando
        if (args.length == 1) {
            List<String> suggest = new ArrayList<>();
            if (sender.hasPermission("customchat.admin")) suggest.add("reload");
            if (sender.hasPermission("customchat.admin")) suggest.add( "version");
            if (sender.hasPermission("customchat.mentiontoggle")) suggest.add("mentiontoggle");
            return suggest;
        }

        return Collections.emptyList();
    }
}
