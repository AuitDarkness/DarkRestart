package com.dark.restartplugin;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.concurrent.TimeUnit;

public class RestartMenu implements Listener {
    private final RestartPlugin plugin;
    private final Map<UUID, MenuContext> playerMenus = new HashMap<>();
    
    // Типы меню
    private static final String MAIN_MENU = "main";
    private static final String AUTO_RESTART_MENU = "auto_restart";
    private static final String NORMAL_RESTART_MENU = "normal_restart";
    private static final String URGENT_RESTART_MENU = "urgent_restart";
    private static final String CONFIRM_MENU = "confirm";

    public RestartMenu(RestartPlugin plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public void openMenu(Player player) {
        openMainMenu(player);
    }

    private void openMainMenu(Player player) {
        Inventory menu = Bukkit.createInventory(player, 27, getTitle("menu-title", "Управление рестартом"));

        // Заполняем стеклянными панелями
        ItemStack glass = createGlassPane();
        for (int i = 0; i < 27; i++) {
            menu.setItem(i, glass);
        }

        // Кнопка авторестарта
        menu.setItem(11, createItem(Material.CLOCK, 
            getMessage("menu-auto-restart", "Авторестарт"),
            Arrays.asList(
                getMessage("menu-auto-restart-lore", "Установить автоматический рестарт"),
                "",
                getMessage("menu-auto-restart-cannot-cancel", "&cНельзя отменить!"),
                "",
                "&eСтатус: " + getAutoRestartStatus(),
                "",
                "&aЛКМ - Настроить",
                "&cПКМ - Отключить"
            )));

        // Кнопка срочного рестарта
        menu.setItem(13, createItem(Material.REDSTONE_BLOCK,
            getMessage("menu-urgent-restart", "Срочный рестарт"),
            Arrays.asList(
                getMessage("menu-urgent-restart-lore", "Выполнить технический рестарт"),
                "",
                getMessage("menu-urgent-cannot-cancel", "&cНельзя отменить!"),
                "",
                "&aНажмите для выбора времени"
            )));
        menu.setItem(15, createItem(Material.GREEN_WOOL,
            getMessage("menu-normal-restart", "Обычный рестарт"),
            Arrays.asList(
                getMessage("menu-normal-restart-lore", "Запланировать обычный рестарт"),
                "",
                getMessage("menu-normal-can-cancel", "&aМожно отменить"),
                "",
                "&aНажмите для выбора времени"
            )));

        // Информационная кнопка
        menu.setItem(26, createItem(Material.BOOK,
            "&6Помощь",
            Arrays.asList(
                "&7Используйте команды:",
                "&e/drestart start <время> [причина]",
                "&e/autorestart set <часы>",
                "&e/autorestart cancel",
                "",
                "&7Формат времени:",
                "&f30s &7- секунды",
                "&f5m &7- минуты", 
                "&f1h &7- часы"
            )));

        player.openInventory(menu);
        playerMenus.put(player.getUniqueId(), new MenuContext(MAIN_MENU));
    }

    private void openAutoRestartMenu(Player player) {
        Inventory menu = Bukkit.createInventory(player, 27, getTitle("menu-auto-restart-title", "Настройка авторестарта"));

        ItemStack glass = createGlassPane();
        for (int i = 0; i < 27; i++) {
            menu.setItem(i, glass);
        }

        // Варианты интервалов авторестарта
        int[] intervals = {1, 2, 4, 6, 8, 12, 24};
        int[] slots = {10, 11, 12, 13, 14, 15, 16};

        for (int i = 0; i < intervals.length; i++) {
            int hours = intervals[i];
            menu.setItem(slots[i], createItem(Material.CLOCK,
                getMessage("menu-auto-restart-time", "&eКаждые {hours} часов").replace("{hours}", String.valueOf(hours)),
                Arrays.asList(
                    getMessage("menu-auto-restart-time-lore", "&7Нажмите, чтобы установить"),
                    "",
                    "&6Предупреждения:",
                    "&60 мин, 1 мин, 30 сек, 10 сек, 5 сек",
                    "",
                    "&aЛКМ - Установить",
                    "&cПКМ - Предпросмотр"
                )));
        }

        // Кнопка назад
        menu.setItem(18, createItem(Material.ARROW, "&cНазад", Collections.singletonList("&7Вернуться в главное меню")));

        player.openInventory(menu);
        playerMenus.put(player.getUniqueId(), new MenuContext(AUTO_RESTART_MENU));
    }

    private void openTimeSelectionMenu(Player player, String menuType, String reason, boolean technical) {
        String title = technical ? 
            getTitle("menu-urgent-restart-title", "Выберите время срочного рестарта") :
            getTitle("menu-normal-restart-title", "Выберите время обычного рестарта");

        Inventory menu = Bukkit.createInventory(player, 27, title);

        ItemStack glass = createGlassPane();
        for (int i = 0; i < 27; i++) {
            menu.setItem(i, glass);
        }

        // Варианты времени
        String[] times = {"30s", "1m", "5m", "10m", "30m", "1h", "2h"};
        Material[] materials = {Material.SAND, Material.SUGAR, Material.FEATHER, 
                               Material.PAPER, Material.CLOCK, Material.DIAMOND, Material.EMERALD};
        int[] slots = {10, 11, 12, 13, 14, 15, 16};

        for (int i = 0; i < times.length; i++) {
            String timeStr = times[i];
            long millis = parseTime(timeStr);
            String formattedTime = formatTime(millis);

            menu.setItem(slots[i], createItem(materials[i],
                (technical ? "&cЧерез " : "&aЧерез ") + formattedTime,
                Arrays.asList(
                    "&7Причина: &f" + reason,
                    "&7Тип: &f" + (technical ? "Технический" : "Обычный"),
                    "",
                    "&aЛКМ - Подтвердить",
                    "&eShift+ЛКМ - Изменить причину"
                )));
        }

        // Кнопка назад
        menu.setItem(18, createItem(Material.ARROW, "&cНазад", Collections.singletonList("&7Вернуться в главное меню")));

        player.openInventory(menu);
        playerMenus.put(player.getUniqueId(), new MenuContext(menuType, reason, technical));
    }

    private void openConfirmMenu(Player player, String menuType, String reason, boolean technical, long timeMillis) {
        Inventory menu = Bukkit.createInventory(player, 27, "&6Подтверждение рестарта");

        ItemStack glass = createGlassPane();
        for (int i = 0; i < 27; i++) {
            menu.setItem(i, glass);
        }

        // Информация о рестарте
        List<String> lore = new ArrayList<>();
        lore.add("&7Время: &f" + formatTime(timeMillis));
        lore.add("&7Причина: &f" + reason);
        lore.add("&7Тип: &f" + (technical ? "Технический" : "Обычный"));
        lore.add("");
        lore.add(technical ? "&cНельзя отменить!" : "&aМожно отменить");
        lore.add("");
        lore.add("&aЛКМ - Подтвердить");
        lore.add("&cПКМ - Отменить");

        menu.setItem(13, createItem(technical ? Material.REDSTONE_BLOCK : Material.GREEN_WOOL,
            "&6Подтвердить рестарт", lore));

        // Кнопка назад
        menu.setItem(18, createItem(Material.ARROW, "&cНазад", Collections.singletonList("&7Вернуться к выбору времени")));

        player.openInventory(menu);
        playerMenus.put(player.getUniqueId(), new MenuContext(CONFIRM_MENU, reason, technical, timeMillis));
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        
        Player player = (Player) event.getWhoClicked();
        UUID playerId = player.getUniqueId();
        
        if (!playerMenus.containsKey(playerId)) return;
        
        MenuContext context = playerMenus.get(playerId);
        event.setCancelled(true);
        
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) return;

        String displayName = ChatColor.stripColor(clicked.getItemMeta().getDisplayName());

        try {
            switch (context.getMenuType()) {
                case MAIN_MENU:
                    handleMainMenuClick(player, event, clicked, displayName);
                    break;
                case AUTO_RESTART_MENU:
                    handleAutoRestartMenuClick(player, event, clicked, displayName);
                    break;
                case NORMAL_RESTART_MENU:
                case URGENT_RESTART_MENU:
                    handleTimeMenuClick(player, event, clicked, displayName, context);
                    break;
                case CONFIRM_MENU:
                    handleConfirmMenuClick(player, event, clicked, displayName, context);
                    break;
            }
        } catch (Exception e) {
            player.sendMessage("§cОшибка при обработке клика: " + e.getMessage());
            plugin.getLogger().severe("Ошибка в меню: " + e.getMessage());
        }
    }

    private void handleMainMenuClick(Player player, InventoryClickEvent event, ItemStack clicked, String displayName) {
        String autoRestartName = ChatColor.stripColor(getMessage("menu-auto-restart", "Авторестарт"));
        String urgentRestartName = ChatColor.stripColor(getMessage("menu-urgent-restart", "Срочный рестарт"));
        String normalRestartName = ChatColor.stripColor(getMessage("menu-normal-restart", "Обычный рестарт"));

        if (displayName.equals(autoRestartName)) {
            if (event.isLeftClick()) {
                openAutoRestartMenu(player);
            } else if (event.isRightClick()) {
                // Отключение авторестарта
                plugin.getAutoRestartManager().setAutoRestart(0, false);
                player.sendMessage("§aАвторестарт отключен!");
                player.closeInventory();
            }
        } else if (displayName.equals(urgentRestartName)) {
            openTimeSelectionMenu(player, URGENT_RESTART_MENU, "Тех.Работы", true);
        } else if (displayName.equals(normalRestartName)) {
            openTimeSelectionMenu(player, NORMAL_RESTART_MENU, "Запланированный рестарт", false);
        } else if (displayName.equals("Назад")) {
            player.closeInventory();
        }
    }

    private void handleAutoRestartMenuClick(Player player, InventoryClickEvent event, ItemStack clicked, String displayName) {
        if (displayName.equals("Назад")) {
            openMainMenu(player);
            return;
        }

        // Парсим количество часов из названия предмета
        if (clicked.getType() == Material.CLOCK) {
            try {
                String[] parts = displayName.split(" ");
                int hours = Integer.parseInt(parts[parts.length - 2]); // "Каждые X часов"
                
                if (event.isLeftClick()) {
                    plugin.getAutoRestartManager().setAutoRestart(hours, true);
                    player.sendMessage("§aАвторестарт установлен на каждые " + hours + " часов!");
                    player.closeInventory();
                } else if (event.isRightClick()) {
                    // Предпросмотр
                    player.sendMessage("§6Предпросмотр авторестарта:");
                    player.sendMessage("§7Интервал: §e" + hours + " часов");
                    player.sendMessage("§7Предупреждения: §e5 мин, 1 мин, 30 сек, 10 сек, 5 сек");
                }
            } catch (NumberFormatException e) {
                player.sendMessage("§cОшибка при обработке интервала");
            }
        }
    }

    private void handleTimeMenuClick(Player player, InventoryClickEvent event, ItemStack clicked, String displayName, MenuContext context) {
        if (displayName.equals("Назад")) {
            openMainMenu(player);
            return;
        }

        // Парсим время из названия предмета
        if (displayName.startsWith("Через ")) {
            String timePart = displayName.substring(6); // Убираем "Через "
            long timeMillis = 0;
            
            // Парсим время обратно в миллисекунды
            if (timePart.contains("ч")) {
                String[] parts = timePart.split("ч");
                int hours = Integer.parseInt(parts[0].trim());
                timeMillis = TimeUnit.HOURS.toMillis(hours);
            } else if (timePart.contains("м")) {
                String[] parts = timePart.split("м");
                int minutes = Integer.parseInt(parts[0].trim());
                timeMillis = TimeUnit.MINUTES.toMillis(minutes);
            } else if (timePart.contains("с")) {
                String[] parts = timePart.split("с");
                int seconds = Integer.parseInt(parts[0].trim());
                timeMillis = TimeUnit.SECONDS.toMillis(seconds);
            }

            if (event.isShiftClick() && event.isLeftClick()) {
                // Изменение причины - открываем чат для ввода
                player.closeInventory();
                player.sendMessage("§eВведите новую причину рестарта в чат:");
                player.sendMessage("§7Текущая причина: §f" + context.getReason());
                // Здесь можно добавить систему ожидания ввода из чата
            } else if (event.isLeftClick()) {
                openConfirmMenu(player, CONFIRM_MENU, context.getReason(), context.isTechnical(), timeMillis);
            }
        }
    }

    private void handleConfirmMenuClick(Player player, InventoryClickEvent event, ItemStack clicked, String displayName, MenuContext context) {
        if (displayName.equals("Назад")) {
            String previousMenu = context.isTechnical() ? URGENT_RESTART_MENU : NORMAL_RESTART_MENU;
            openTimeSelectionMenu(player, previousMenu, context.getReason(), context.isTechnical());
            return;
        }

        if (displayName.equals("Подтвердить рестарт")) {
            if (event.isLeftClick()) {
                plugin.getRestartManager().scheduleRestart(context.getTimeMillis(), context.getReason(), context.isTechnical());
                player.sendMessage("§aРестарт запланирован!");
                player.closeInventory();
            } else if (event.isRightClick()) {
                player.closeInventory();
            }
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        playerMenus.remove(event.getPlayer().getUniqueId());
    }

    private String getAutoRestartStatus() {
        if (plugin.getConfig().getBoolean("auto-restart.enabled", false)) {
            int hours = plugin.getConfig().getInt("auto-restart.interval", 6);
            return "§aАктивен (§e" + hours + "ч§a)";
        } else {
            return "§cНеактивен";
        }
    }

    private ItemStack createGlassPane() {
        ItemStack glass = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta meta = glass.getItemMeta();
        meta.setDisplayName(" ");
        glass.setItemMeta(meta);
        return glass;
    }

    private ItemStack createItem(Material material, String name, List<String> lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
        
        List<String> coloredLore = new ArrayList<>();
        for (String line : lore) {
            coloredLore.add(ChatColor.translateAlternateColorCodes('&', line));
        }
        meta.setLore(coloredLore);
        
        item.setItemMeta(meta);
        return item;
    }

    private String getTitle(String key, String defaultValue) {
        return ChatColor.translateAlternateColorCodes('&', getMessage(key, defaultValue));
    }

    private String getMessage(String key, String defaultValue) {
        try {
            return plugin.getConfigManager().getMessage(key);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    private long parseTime(String timeStr) {
        try {
            if (timeStr.endsWith("s")) {
                return TimeUnit.SECONDS.toMillis(Long.parseLong(timeStr.substring(0, timeStr.length() - 1)));
            } else if (timeStr.endsWith("m")) {
                return TimeUnit.MINUTES.toMillis(Long.parseLong(timeStr.substring(0, timeStr.length() - 1)));
            } else if (timeStr.endsWith("h")) {
                return TimeUnit.HOURS.toMillis(Long.parseLong(timeStr.substring(0, timeStr.length() - 1)));
            }
        } catch (NumberFormatException e) {
            return -1;
        }
        return -1;
    }

    private String formatTime(long millis) {
        long seconds = TimeUnit.MILLISECONDS.toSeconds(millis);
        long minutes = TimeUnit.SECONDS.toMinutes(seconds);
        long hours = TimeUnit.MINUTES.toHours(minutes);

        if (hours > 0) {
            return String.format("%dч %dм", hours, minutes % 60);
        } else if (minutes > 0) {
            return String.format("%dм %dс", minutes, seconds % 60);
        } else {
            return String.format("%dс", seconds);
        }
    }

    // Класс для хранения контекста меню
    private static class MenuContext {
        private final String menuType;
        private String reason;
        private boolean technical;
        private long timeMillis;

        public MenuContext(String menuType) {
            this.menuType = menuType;
        }

        public MenuContext(String menuType, String reason, boolean technical) {
            this.menuType = menuType;
            this.reason = reason;
            this.technical = technical;
        }

        public MenuContext(String menuType, String reason, boolean technical, long timeMillis) {
            this.menuType = menuType;
            this.reason = reason;
            this.technical = technical;
            this.timeMillis = timeMillis;
        }

        public String getMenuType() { return menuType; }
        public String getReason() { return reason; }
        public boolean isTechnical() { return technical; }
        public long getTimeMillis() { return timeMillis; }
    }
}