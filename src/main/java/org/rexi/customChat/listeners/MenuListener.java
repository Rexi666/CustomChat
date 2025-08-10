package org.rexi.customChat.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.rexi.customChat.CustomChat;
import org.rexi.customChat.menus.ChatColorMenu;

public class MenuListener implements Listener {

    private final CustomChat plugin;

    public MenuListener(CustomChat plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player p)) return;
        String currentTitle = event.getView().getTitle().replace("§", "&");

        String maintitle = plugin.getChatColorString("menu.main.title");
        String colorstitle = plugin.getChatColorString("menu.submenus.submenu_color-title");
        String gradientstitle = plugin.getChatColorString("menu.submenus.submenu_gradient-title");

        if (currentTitle.equals(maintitle)) {
            event.setCancelled(true);
            int custommodeldata = -1;
            if (event.getCurrentItem() != null && event.getCurrentItem().hasItemMeta()
                    && event.getCurrentItem().getItemMeta().hasCustomModelData()) {
                custommodeldata = event.getCurrentItem().getItemMeta().getCustomModelData();
            }

            if (custommodeldata == 1001) {
                new ChatColorMenu(plugin, p).openColorSubMenu();
            } else if (custommodeldata == 1002) {
                new ChatColorMenu(plugin, p).openGradientSubMenu();
            } else if (custommodeldata == 1003) {
                plugin.setPlayerChatColor(p, "");
                p.sendMessage(plugin.getMessage("color_removed"));
                p.closeInventory();
            }
        } else if (currentTitle.equals(colorstitle)) {
            event.setCancelled(true);
            int custommodeldata = -1;
            if (event.getCurrentItem() != null && event.getCurrentItem().hasItemMeta()
                    && event.getCurrentItem().getItemMeta().hasCustomModelData()) {
                custommodeldata = event.getCurrentItem().getItemMeta().getCustomModelData();
            }

            if (plugin.colorItems.containsKey(custommodeldata)) {
                String key = plugin.colorItems.get(custommodeldata);
                String permission = "customchat.colorchat.color." + key;

                String colorCode = plugin.getChatColorString("chatcolor.colors.colors." + key + ".chatcolor");
                String name = plugin.getChatColorString("chatcolor.colors.colors." + key + ".name");
                if (p.hasPermission(permission)) {
                    plugin.setPlayerChatColor(p, colorCode);
                    p.sendMessage(plugin.getMessage("color_selected", "{color}", key));
                    p.closeInventory();
                } else {
                    p.sendMessage(plugin.getMessage("no_permission_color", "{color}", name));
                    p.closeInventory();
                }
            }
        } else if (currentTitle.equals(gradientstitle)) {
            event.setCancelled(true);
            int custommodeldata = -1;
            if (event.getCurrentItem() != null && event.getCurrentItem().hasItemMeta()
                    && event.getCurrentItem().getItemMeta().hasCustomModelData()) {
                custommodeldata = event.getCurrentItem().getItemMeta().getCustomModelData();
            }

            if (plugin.gradientItems.containsKey(custommodeldata)) {
                String key = plugin.gradientItems.get(custommodeldata);
                String permission = "customchat.colorchat.gradient." + key;

                String colorCode = plugin.getChatColorString("chatcolor.gradients.colors." + key + ".color");
                String name = plugin.getChatColorString("chatcolor.gradients.colors." + key + ".name");
                if (p.hasPermission(permission)) {
                    plugin.setPlayerChatColor(p, colorCode);
                    p.sendMessage(plugin.getMessage("color_selected", "{color}", key));
                    p.closeInventory();
                } else {
                    p.sendMessage(plugin.getMessage("no_permission_color", "{color}", name));
                    p.closeInventory();
                }
            }
        }
    }
}
