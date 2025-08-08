package org.rexi.customChat.menus;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.rexi.customChat.CustomChat;

import java.util.ArrayList;
import java.util.List;

public class ChatColorMenu implements Listener {

    private final CustomChat plugin;
    private final Player player;

    public ChatColorMenu(CustomChat plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public void openMainMenu() {
        Inventory inv = Bukkit.createInventory(null, 27, plugin.deserialize(plugin.getChatColorString("menu.main.title")));

        ItemStack colors = createItem(Material.valueOf(plugin.getChatColorString("chatcolor.colors.material")), plugin.deserialize(plugin.getChatColorString("chatcolor.colors.name")), plugin.getChatColorList("menu.main.lore_color"));
        ItemStack gradients = createItem(Material.valueOf(plugin.getChatColorString("chatcolor.gradients.material")), plugin.deserialize(plugin.getChatColorString("chatcolor.gradients.name")), plugin.getChatColorList("menu.main.lore_gradient"));

        inv.setItem(11, colors);
        inv.setItem(15, gradients);

        player.openInventory(inv);
    }

    private ItemStack createItem(Material material, net.kyori.adventure.text.Component name, List<String> lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        meta.displayName(name);
        List<net.kyori.adventure.text.Component> loreLines = new ArrayList<>();
        for (String line : lore) {
            loreLines.add(plugin.deserialize(line));
        }
        meta.lore(loreLines);

        item.setItemMeta(meta);
        return item;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        // Aquí puedes añadir lógica de detección de clics en los ítems y abrir submenús, guardar colores, etc.
    }
}
