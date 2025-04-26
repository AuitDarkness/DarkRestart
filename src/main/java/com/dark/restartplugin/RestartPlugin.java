package com.dark.restartplugin;

import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

/**
 * Плагин для управления рестартом сервера
 */
public class RestartPlugin extends JavaPlugin implements TabCompleter {
    private BukkitTask restartTask;
    private long restartTime;
    private String restartReason;
    private boolean isTechnical;
    
    private FileConfiguration messagesConfig;
    private File messagesFile;
    private RestartMenu restartMenu;
    private RestartManager restartManager;
    private AutoRestartManager autoRestartManager;

    @Override
    public void onEnable() {
        try {
            loadConfigs();
            initializeManagers();
            registerCommands();
            getLogger().info("§aDark Restart Plugin включен!");
            
            // Восстанавливаем запланированный рестарт
            if (getConfig().getLong("restart-time", 0) > System.currentTimeMillis()) {
                long timeLeft = (getConfig().getLong("restart-time") - System.currentTimeMillis()) / 1000;
                String reason = getConfig().getString("restart-reason", "Запланированный рестарт");
                boolean technical = getConfig().getBoolean("is-technical", false);
                startRestart(timeLeft, reason, technical);
            }
        } catch (Exception e) {
            getLogger().severe("§cОшибка при включении плагина: " + e.getMessage());
            e.printStackTrace();
            getServer().getPluginManager().disablePlugin(this);
        }
    }

    @Override
    public void onDisable() {
        if (restartManager != null) {
            restartManager.cancelRestart();
        }
        if (autoRestartManager != null) {
            autoRestartManager.cancelAutoRestart();
        }
        try {
            cancelRestart();
            getLogger().info("§cDark Restart Plugin выключен!");
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "Ошибка при выключении плагина", e);
        }
    }

    private void loadConfigs() {
        // Загружаем основной конфиг
        saveDefaultConfig();

        // Загружаем файл сообщений
        messagesFile = new File(getDataFolder(), "messages.yml");
        if (!messagesFile.exists()) {
            saveResource("messages.yml", false);
        }
        messagesConfig = YamlConfiguration.loadConfiguration(messagesFile);
    }

    private void initializeManagers() {
        restartManager = new RestartManager(this);
        autoRestartManager = new AutoRestartManager(this);
        restartMenu = new RestartMenu(this);
    }

    private void registerCommands() {
        getCommand("drestart").setExecutor(new RestartCommand(this));
        getCommand("autorestart").setExecutor(new AutoRestartCommand(this));
    }

    public String getMessage(String path) {
        return messagesConfig.getString("messages." + path, "§cСообщение не найдено: " + path);
    }

    public RestartManager getRestartManager() {
        return restartManager;
    }

    public AutoRestartManager getAutoRestartManager() {
        return autoRestartManager;
    }

    public RestartMenu getRestartMenu() {
        return restartMenu;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!sender.hasPermission("darkrestart.use")) {
            sender.sendMessage(getMessage("no-permission"));
            return true;
        }

        if (args.length == 0) {
            if (sender instanceof Player) {
                restartMenu.openMenu((Player) sender);
            } else {
                sender.sendMessage(getMessage("players-only"));
            }
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "start":
                if (args.length < 2) {
                    sender.sendMessage("§cИспользование: /darkrestart start <время> [причина]");
                    return true;
                }
                try {
                    long time = parseTime(args[1]);
                    if (time <= 0) {
                        sender.sendMessage("§cНеверный формат времени! Используйте: 30s, 5m, 1h");
                        return true;
                    }
                    String reason = args.length > 2 ? String.join(" ", Arrays.copyOfRange(args, 2, args.length)) : "Запланированный рестарт";
                    boolean technical = reason.toLowerCase().contains("тех") || reason.toLowerCase().contains("tech");
                    startRestart(time, reason, technical);
                    String message = "§aРестарт запланирован через §e" + formatTime(time) + "§a! Причина: §e" + reason;
                    Bukkit.broadcastMessage(message);
                } catch (IllegalArgumentException e) {
                    sender.sendMessage("§cНеверный формат времени! Используйте: 30s, 5m, 1h");
                }
                break;

            case "cancel":
                if (restartTask == null) {
                    sender.sendMessage("§cРестарт не запланирован");
                    return true;
                }
                if (isTechnical) {
                    sender.sendMessage("§cТехнический рестарт нельзя отменить!");
                    return true;
                }
                cancelRestart();
                String message = "§cРестарт отменен администратором §e" + sender.getName();
                Bukkit.broadcastMessage(message);
                break;

            case "status":
                if (restartTask == null) {
                    sender.sendMessage("§cРестарт не запланирован");
                } else {
                    long timeLeft = (restartTime - System.currentTimeMillis()) / 1000;
                    String status = "§6Статус рестарта: §e" + formatTime(timeLeft) + " §6осталось. Причина: §e" + restartReason;
                    sender.sendMessage(status);
                }
                break;

            case "menu":
                if (!(sender instanceof Player)) {
                    sender.sendMessage(getMessage("players-only"));
                    return true;
                }
                restartMenu.openMenu((Player) sender);
                return true;

            default:
                sendHelp(sender);
                break;
        }
        return true;
    }

    private void startRestart(long seconds, String reason, boolean technical) {
        cancelRestart();
        
        restartTime = System.currentTimeMillis() + (seconds * 1000);
        restartReason = reason;
        isTechnical = technical;
        
        // Сохраняем в конфиг
        getConfig().set("restart-time", restartTime);
        getConfig().set("restart-reason", reason);
        getConfig().set("is-technical", technical);
        saveConfig();

        restartTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (System.currentTimeMillis() >= restartTime) {
                    performRestart();
                    cancel();
                    return;
                }

                long timeLeft = (restartTime - System.currentTimeMillis()) / 1000;
                
                // Уведомления
                if (timeLeft % 300 == 0 || timeLeft <= 10) { // Каждые 5 минут или последние 10 секунд
                    sendNotifications(timeLeft);
                }

                // ActionBar
                for (Player player : Bukkit.getOnlinePlayers()) {
                    player.sendActionBar("§cРестарт через §e" + formatTime(timeLeft) + "§c | §e" + restartReason);
                }

                // Звуки
                if (timeLeft <= 10) {
                    for (Player player : Bukkit.getOnlinePlayers()) {
                        player.playSound(player.getLocation(), Sound.valueOf("BLOCK_NOTE_BLOCK_PLING"), 1.0F, 1.0F);
                    }
                }
            }
        }.runTaskTimer(this, 0, 20);
    }

    private void performRestart() {
        // Сохраняем мир
        Bukkit.getWorlds().forEach(world -> world.save());
        
        // Сообщения
        Bukkit.broadcastMessage("§c§lСервер перезапускается!");
        Bukkit.broadcastMessage("§cПричина: §e" + restartReason);

        // Кикаем игроков
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.kickPlayer("§cСервер перезапускается!\n§eПричина: " + restartReason + "\n§aПожалуйста, зайдите через минуту");
        }

        // Выключаем сервер
        new BukkitRunnable() {
            @Override
            public void run() {
                Bukkit.shutdown();
            }
        }.runTaskLater(this, 20L);
    }

    private void cancelRestart() {
        if (restartTask != null) {
            restartTask.cancel();
            restartTask = null;
        }
        getConfig().set("restart-time", 0);
        saveConfig();
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage("§6§l=== Dark Restart Plugin ===");
        sender.sendMessage("§e/darkrestart start <время> [причина] §7- Запланировать рестарт");
        sender.sendMessage("§e/darkrestart cancel §7- Отменить рестарт");
        sender.sendMessage("§e/darkrestart status §7- Показать статус рестарта");
        sender.sendMessage("§7Формат времени: §e30s §7(секунды), §e5m §7(минуты), §e1h §7(часы)");
    }

    private long parseTime(String timeStr) {
        long totalSeconds = 0;
        String[] parts = timeStr.split("(?<=\\d)(?=\\D)");
        
        for (String part : parts) {
            if (part.endsWith("s")) {
                totalSeconds += Long.parseLong(part.substring(0, part.length() - 1));
            } else if (part.endsWith("m")) {
                totalSeconds += Long.parseLong(part.substring(0, part.length() - 1)) * 60;
            } else if (part.endsWith("h")) {
                totalSeconds += Long.parseLong(part.substring(0, part.length() - 1)) * 3600;
            } else {
                throw new IllegalArgumentException("Неверный формат времени");
            }
        }
        
        return totalSeconds;
    }

    private String formatTime(long seconds) {
        if (seconds < 60) {
            return seconds + " сек";
        } else if (seconds < 3600) {
            return (seconds / 60) + " мин " + (seconds % 60) + " сек";
        } else {
            return (seconds / 3600) + " ч " + ((seconds % 3600) / 60) + " мин";
        }
    }

    private void sendNotifications(long timeLeft) {
        String message = getMessage("server-restarting")
                .replace("%time%", formatTime(timeLeft))
                .replace("%reason%", restartReason);

        // Отправляем сообщение в чат
        Bukkit.broadcastMessage(message);

        // Отправляем ActionBar сообщение
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.sendActionBar(getMessage("countdown-actionbar")
                    .replace("%time%", formatTime(timeLeft))
                    .replace("%reason%", restartReason));
            
            // Воспроизводим звук
            String soundName = getConfig().getString("restart.countdown-sound", "block.note_block.pling");
            try {
                Sound sound = Sound.valueOf(soundName.toUpperCase());
                player.playSound(player.getLocation(), sound, 1.0F, 1.0F);
            } catch (IllegalArgumentException e) {
                getLogger().warning("Неверное имя звука в конфиге: " + soundName);
            }
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (!sender.hasPermission("darkrestart.use")) {
            return completions;
        }

        if (args.length == 1) {
            completions.add("start");
            completions.add("cancel");
            completions.add("status");
        } else if (args.length == 2 && args[0].equalsIgnoreCase("start")) {
            completions.add("30s");
            completions.add("1m");
            completions.add("5m");
            completions.add("10m");
            completions.add("30m");
            completions.add("1h");
            completions.add("2h");
            completions.add("6h");
        } else if (args.length == 3 && args[0].equalsIgnoreCase("start")) {
            completions.add("Тех.Работы");
            completions.add("Обновление");
            completions.add("Перезагрузка");
            completions.add("Техническое обслуживание");
        }

        String input = args[args.length - 1].toLowerCase();
        completions.removeIf(s -> !s.toLowerCase().startsWith(input));

        return completions;
    }
}