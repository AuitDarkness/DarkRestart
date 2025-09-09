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
        if (!plugin.getConfig().getBoolean("auto-restart.enabled", false)) {
            return;
        }

        long intervalHours = plugin.getConfig().getInt("auto-restart.interval", 6);
        List<Integer> warnings = plugin.getConfig().getIntegerList("auto-restart.warnings");

        cancelAutoRestart();

        long intervalTicks = TimeUnit.HOURS.toSeconds(intervalHours) * 20L;

        autoRestartTask = plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            String reason = "Автоматический рестарт (каждые " + intervalHours + " часов)";
            plugin.getRestartManager().scheduleRestart(0, reason, false, warnings);
        }, intervalTicks, intervalTicks);

        plugin.getLogger().info("Авторестарт запущен с интервалом " + intervalHours + " часов");
    }

    public void cancelAutoRestart() {
        if (autoRestartTask != null) {
            autoRestartTask.cancel();
            autoRestartTask = null;
        }
    }

    public void loadAutoRestart() {
        if (plugin.getConfig().getBoolean("auto-restart.enabled", false)) {
            startAutoRestart();
        }
    }

    public void setAutoRestart(int hours, boolean enabled) {
        plugin.getConfig().set("auto-restart.enabled", enabled);
        plugin.getConfig().set("auto-restart.interval", hours);
        plugin.saveConfig();

        if (enabled) {
            startAutoRestart();
        } else {
            cancelAutoRestart();
        }
    }

    public String getStatus() {
        if (!plugin.getConfig().getBoolean("auto-restart.enabled", false)) {
            return plugin.getConfigManager().getMessage("autorestart-not-active");
        }

        int hours = plugin.getConfig().getInt("auto-restart.interval", 6);
        return plugin.getConfigManager().getMessage("autorestart-status")
                .replace("%status%", "Активен | Каждые " + hours + " часов");
    }

    public void reload() {
        cancelAutoRestart();
        startAutoRestart();
    }
}