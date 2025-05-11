package com.dark.restartplugin;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class RestartManager {
    private final RestartPlugin plugin;
    private BukkitTask restartTask;
    private long restartTime;
    private String restartReason;
    private boolean isTechnical;

    public RestartManager(RestartPlugin plugin) {
        this.plugin = plugin;
    }

    public void scheduleRestart(long time, String reason, boolean isTechnical) {
        if (restartTask != null) {
            restartTask.cancel();
        }

        this.restartTime = System.currentTimeMillis() + time;
        this.restartReason = reason;
        this.isTechnical = isTechnical;

        // Сохраняем в конфиг
        plugin.getConfig().set("restart.time", restartTime);
        plugin.getConfig().set("restart.reason", restartReason);
        plugin.getConfig().set("restart.is-technical", isTechnical);
        plugin.saveConfig();

        // Запускаем обратный отсчет
        startCountdown();
    }

    // Add new overloaded method
    public void scheduleRestart(long delay, String reason, boolean technical, List<Integer> warnings) {
        if (isRestartScheduled()) {
            return;
        }

        this.restartTime = System.currentTimeMillis() + delay;
        this.restartReason = reason;
        this.isTechnical = technical;
        
        startWarnings(warnings);
        saveRestartData();
    }

    public void cancelRestart() {
        if (restartTask != null) {
            restartTask.cancel();
            restartTask = null;
        }

        restartTime = 0;
        restartReason = null;
        isTechnical = false;

        // Очищаем конфиг
        plugin.getConfig().set("restart.time", 0);
        plugin.getConfig().set("restart.reason", null);
        plugin.getConfig().set("restart.is-technical", false);
        plugin.saveConfig();
    }

    private void startCountdown() {
        restartTask = new BukkitRunnable() {
            @Override
            public void run() {
                long timeLeft = restartTime - System.currentTimeMillis();
                if (timeLeft <= 0) {
                    performRestart();
                    return;
                }

                // Отправляем уведомления
                sendNotifications(timeLeft);
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }

    private void sendNotifications(long timeLeft) {
        String timeString = formatTime(timeLeft);
        String message = plugin.getMessage("server-restarting")
                .replace("%time%", timeString)
                .replace("%reason%", restartReason);

        // Отправляем в ActionBar
        if (plugin.getConfig().getBoolean("notifications.actionbar")) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                player.sendActionBar(message);
            }
        }

        // Проигрываем звук
        if (plugin.getConfig().getBoolean("notifications.sounds")) {
            String sound = plugin.getConfig().getString("notifications.countdown-sound");
            float volume = (float) plugin.getConfig().getDouble("notifications.sound-volume");
            float pitch = (float) plugin.getConfig().getDouble("notifications.sound-pitch");

            for (Player player : Bukkit.getOnlinePlayers()) {
                player.playSound(player.getLocation(), sound, volume, pitch);
            }
        }
    }

    private void performRestart() {
        String kickMessage = plugin.getMessage("kick-message")
                .replace("%reason%", restartReason);

        // Кикаем всех игроков
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.kickPlayer(kickMessage);
        }

        // Выключаем сервер
        new BukkitRunnable() {
            @Override
            public void run() {
                Bukkit.shutdown();
            }
        }.runTaskLater(plugin, 20L);
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

    public String getStatus() {
        if (restartTime == 0) {
            return plugin.getMessage("restart-not-active");
        }

        long timeLeft = restartTime - System.currentTimeMillis();
        String timeString = formatTime(timeLeft);
        String reason = restartReason;

        return plugin.getMessage("restart-status")
                .replace("%status%", timeString + " | " + reason);
    }

    public boolean isRestartScheduled() {
        return restartTime > 0;
    }

    public boolean isTechnical() {
        return isTechnical;
    }

    private void startWarnings(List<Integer> warnings) {
        // Placeholder for warning logic
    }

    private void saveRestartData() {
        plugin.getConfig().set("restart.time", restartTime);
        plugin.getConfig().set("restart.reason", restartReason);
        plugin.getConfig().set("restart.is-technical", isTechnical);
        plugin.saveConfig();
    }
}