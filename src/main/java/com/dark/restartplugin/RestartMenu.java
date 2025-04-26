package com.dark.restartplugin;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

public class RestartMenu implements Listener {
    private final RestartPlugin plugin;
    private final String title;

    public RestartMenu(RestartPlugin plugin) {
        this.plugin = plugin;
        this.title = ChatColor.translateAlternateColorCodes('&', plugin.getMessage("menu-title"));
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public void openMenu(Player player) {
        Inventory menu = Bukkit.createInventory(player, 27, title);

        // Кнопка авторестарта
        ItemStack autoRestart = createItem(Material.CLOCK, 
            plugin.getMessage("menu-auto-restart"),
            Arrays.asList(
                plugin.getMessage("menu-auto-restart-lore"),
                plugin.getMessage("menu-auto-restart-cannot-cancel"),
                "",
                "&eИспользование:",
                "&f/autorestart set <часы>",
                "&fПример: /autorestart set 6"
            ));

        // Кнопка срочного рестарта
        ItemStack urgentRestart = createItem(Material.REDSTONE_BLOCK,
            plugin.getMessage("menu-urgent-restart"),
            Arrays.asList(
                plugin.getMessage("menu-urgent-restart-lore"),
                plugin.getMessage("menu-urgent-cannot-cancel"),
                "",
                "&eИспользование:",
                "&f/drestart start <время> Тех.Работы",
                "&fПример: /drestart start 5m Тех.Работы"
            ));

        // Кнопка обычного рестарта
        ItemStack normalRestart = createItem(Material.GREEN_WOOL,
            plugin.getMessage("menu-normal-restart"),
            Arrays.asList(
                plugin.getMessage("menu-normal-restart-lore"),
                "&aМожно отменить",
                "",
                "&eИспользование:",
                "&f/drestart start <время> <причина>",
                "&fПример: /drestart start 1h Обновление"
            ));

        menu.setItem(11, autoRestart);
        menu.setItem(13, urgentRestart);
        menu.setItem(15, normalRestart);

        player.openInventory(menu);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        if (!event.getView().getTitle().equals(title)) return;

        event.setCancelled(true);
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) return;

        Player player = (Player) event.getWhoClicked();
        String displayName = clicked.getItemMeta().getDisplayName();

        if (displayName.equals(plugin.getMessage("menu-auto-restart"))) {
            player.closeInventory();
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', 
                "&eВведите команду: &f/autorestart set <часы>"));
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', 
                "&eПример: &f/autorestart set 6"));
        } else if (displayName.equals(plugin.getMessage("menu-urgent-restart"))) {
            player.closeInventory();
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', 
                "&eВведите команду: &f/drestart start <время> Тех.Работы"));
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', 
                "&eПример: &f/drestart start 5m Тех.Работы"));
        } else if (displayName.equals(plugin.getMessage("menu-normal-restart"))) {
            player.closeInventory();
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', 
                "&eВведите команду: &f/drestart start <время> <причина>"));
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', 
                "&eПример: &f/drestart start 1h Обновление"));
        }
    }

    private ItemStack createItem(Material material, String name, java.util.List<String> lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }
} 