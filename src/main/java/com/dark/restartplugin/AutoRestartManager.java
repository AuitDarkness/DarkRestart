// AutoRestartManager.java
package com.dark.restartplugin;

import org.bukkit.scheduler.BukkitTask;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class AutoRestartManager {
    private final RestartPlugin plugin;
    private BukkitTask autoRestartTask;

    public AutoRestartManager(RestartPlugin plugin) {
        this.plugin = plugin;
    }

    public void startAutoRestart() {
        cancelAutoRestart();
        
        if (!plugin.getConfig().getBoolean("auto-restart.enabled", false)) {
            plugin.getLogger().info("Авторестарт отключен в конфиге");
            return;
        }

        int intervalHours = plugin.getConfig().getInt("auto-restart.interval", 6);
        
        if (intervalHours < 1) {
            plugin.getLogger().warning("Интервал авторестарта слишком мал: " + intervalHours + " часов. Минимум 1 час.");
            return;
        }

        List<Integer> warnings = plugin.getConfig().getIntegerList("auto-restart.warnings");
        long intervalTicks = intervalHours * 72000L;

        plugin.getLogger().info("Настраиваю авторестарт с интервалом " + intervalHours + " часов (" + intervalTicks + " тиков)");

        autoRestartTask = plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            String reason = "Автоматический рестарт (каждые " + intervalHours + " часов)";
            plugin.getLogger().info("Запускаю авторестарт: " + reason);
            
            plugin.getRestartManager().scheduleRestart(
                TimeUnit.MINUTES.toMillis(5), 
                reason, 
                false, 
                warnings
            );
        }, intervalTicks, intervalTicks);

        plugin.getLogger().info("✅ Авторестарт активирован. Первый рестарт через " + intervalHours + " часов");
    }

    public void cancelAutoRestart() {
        if (autoRestartTask != null) {
            autoRestartTask.cancel();
            autoRestartTask = null;
            plugin.getLogger().info("Авторестарт отменен");
        }
    }

    public void loadAutoRestart() {
        if (plugin.getConfig().getBoolean("auto-restart.enabled", false)) {
            plugin.getLogger().info("Загружаю настройки авторестарта...");
            startAutoRestart();
        } else {
            plugin.getLogger().info("Авторестарт отключен в конфигурации");
        }
    }

    public void setAutoRestart(int hours, boolean enabled) {
        plugin.getConfig().set("auto-restart.enabled", enabled);
        plugin.getConfig().set("auto-restart.interval", hours);
        plugin.saveConfig();

        if (enabled) {
            plugin.getLogger().info("Устанавливаю авторестарт на каждые " + hours + " часов");
            startAutoRestart();
        } else {
            plugin.getLogger().info("Отключаю авторестарт");
            cancelAutoRestart();
        }
    }

    public String getStatus() {
        if (!plugin.getConfig().getBoolean("auto-restart.enabled", false)) {
            return "§cАвторестарт отключен";
        }

        int hours = plugin.getConfig().getInt("auto-restart.interval", 6);
        return "§aАвторестарт активен | Каждые §e" + hours + "§a часов";
    }
}