package com.dark.restartplugin;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.scheduler.BukkitTask;
import java.util.concurrent.TimeUnit;
import java.util.List;

public class AutoRestartManager {
    private final RestartPlugin plugin;
    private BukkitTask autoRestartTask;
    private final FileConfiguration config;

    public AutoRestartManager(RestartPlugin plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfig();
        startAutoRestart();
    }

    private void startAutoRestart() {
        if (!config.getBoolean("auto-restart.enabled")) {
            return;
        }

        long intervalHours = config.getInt("auto-restart.interval");
        List<Integer> warnings = config.getIntegerList("auto-restart.warnings");

        if (autoRestartTask != null) {
            autoRestartTask.cancel();
        }

        autoRestartTask = plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            String reason = "Автоматический рестарт (каждые " + intervalHours + " часов)";
            plugin.getRestartManager().scheduleRestart(
                0, // мгновенный рестарт после окончания предупреждений
                reason,
                false,
                warnings
            );
        }, TimeUnit.HOURS.toSeconds(intervalHours) * 20L, TimeUnit.HOURS.toSeconds(intervalHours) * 20L);

        plugin.getLogger().info("Авторестарт запущен с интервалом " + intervalHours + " часов");
    }

    public String getStatus() {
        if (!config.getBoolean("auto-restart.enabled")) {
            return plugin.getMessage("autorestart-not-active");
        }

        int hours = config.getInt("auto-restart.interval");
        return plugin.getMessage("autorestart-status")
                .replace("%status%", "Активен | Каждые " + hours + " часов");
    }

    public void reload() {
        if (autoRestartTask != null) {
            autoRestartTask.cancel();
        }
        startAutoRestart();
    }

    public void cancelAutoRestart() {
        if (autoRestartTask != null) {
            autoRestartTask.cancel();
            autoRestartTask = null;
        }
        plugin.getLogger().info("Авторестарт отключен");
    }
}