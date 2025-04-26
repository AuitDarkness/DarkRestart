package com.dark.restartplugin;

import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.concurrent.TimeUnit;

public class AutoRestartManager {
    private final RestartPlugin plugin;
    private BukkitTask autoRestartTask;
    private long interval;
    private long nextRestart;

    public AutoRestartManager(RestartPlugin plugin) {
        this.plugin = plugin;
        loadFromConfig();
    }

    private void loadFromConfig() {
        interval = TimeUnit.HOURS.toMillis(plugin.getConfig().getLong("autorestart.interval"));
        nextRestart = plugin.getConfig().getLong("autorestart.next-restart");

        if (interval > 0 && nextRestart > System.currentTimeMillis()) {
            startAutoRestart();
        }
    }

    public void setInterval(long hours) {
        interval = TimeUnit.HOURS.toMillis(hours);
        nextRestart = System.currentTimeMillis() + interval;

        // Сохраняем в конфиг
        plugin.getConfig().set("autorestart.interval", hours);
        plugin.getConfig().set("autorestart.next-restart", nextRestart);
        plugin.saveConfig();

        startAutoRestart();
    }

    public void cancelAutoRestart() {
        if (autoRestartTask != null) {
            autoRestartTask.cancel();
            autoRestartTask = null;
        }

        interval = 0;
        nextRestart = 0;

        // Очищаем конфиг
        plugin.getConfig().set("autorestart.interval", 0);
        plugin.getConfig().set("autorestart.next-restart", 0);
        plugin.saveConfig();
    }

    private void startAutoRestart() {
        if (autoRestartTask != null) {
            autoRestartTask.cancel();
        }

        autoRestartTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (System.currentTimeMillis() >= nextRestart) {
                    // Запускаем рестарт
                    plugin.getRestartManager().scheduleRestart(
                        TimeUnit.MINUTES.toMillis(5),
                        "Автоматический рестарт (каждые " + TimeUnit.MILLISECONDS.toHours(interval) + " часов)",
                        false
                    );

                    // Устанавливаем следующий рестарт
                    nextRestart = System.currentTimeMillis() + interval;
                    plugin.getConfig().set("autorestart.next-restart", nextRestart);
                    plugin.saveConfig();
                }
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }

    public String getStatus() {
        if (interval == 0) {
            return plugin.getMessage("autorestart-not-active");
        }

        long timeLeft = nextRestart - System.currentTimeMillis();
        String timeString = formatTime(timeLeft);
        String hours = String.valueOf(TimeUnit.MILLISECONDS.toHours(interval));

        return plugin.getMessage("autorestart-status")
                .replace("%status%", timeString + " | Каждые " + hours + " часов");
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

    public boolean isAutoRestartEnabled() {
        return interval > 0;
    }
} 