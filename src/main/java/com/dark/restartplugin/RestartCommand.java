package com.dark.restartplugin;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class RestartCommand implements CommandExecutor, TabCompleter {
    private final RestartPlugin plugin;

    public RestartCommand(RestartPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!sender.hasPermission("darkrestart.use")) {
            sender.sendMessage(plugin.getMessage("no-permission"));
            return true;
        }

        if (args.length == 0) {
            if (sender instanceof Player) {
                plugin.getRestartMenu().openMenu((Player) sender);
            } else {
                sender.sendMessage(plugin.getMessage("players-only"));
            }
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "menu":
                if (!(sender instanceof Player)) {
                    sender.sendMessage(plugin.getMessage("players-only"));
                    return true;
                }
                plugin.getRestartMenu().openMenu((Player) sender);
                return true;

            case "start":
                if (args.length < 2) {
                    sender.sendMessage(plugin.getMessage("command-usage"));
                    return true;
                }

                long time = parseTime(args[1]);
                if (time <= 0) {
                    sender.sendMessage(plugin.getMessage("invalid-time-format"));
                    return true;
                }

                String reason = "Запланированный рестарт";
                if (args.length > 2) {
                    reason = String.join(" ", Arrays.copyOfRange(args, 2, args.length));
                }

                boolean isTechnical = reason.equalsIgnoreCase("Тех.Работы");
                if (isTechnical && !sender.hasPermission("darkrestart.technical")) {
                    sender.sendMessage(plugin.getMessage("technical-permission"));
                    return true;
                }

                plugin.getRestartManager().scheduleRestart(time, reason, isTechnical);
                sender.sendMessage(plugin.getMessage("restart-scheduled")
                        .replace("%time%", formatTime(time))
                        .replace("%reason%", reason));
                return true;

            case "cancel":
                if (plugin.getRestartManager().isTechnical()) {
                    sender.sendMessage(plugin.getMessage("cannot-cancel-technical"));
                    return true;
                }

                plugin.getRestartManager().cancelRestart();
                sender.sendMessage(plugin.getMessage("restart-cancelled"));
                return true;

            case "status":
                sender.sendMessage(plugin.getRestartManager().getStatus());
                return true;

            case "help":
                sendHelp(sender);
                return true;

            default:
                sender.sendMessage(plugin.getMessage("command-usage"));
                return true;
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            completions.addAll(Arrays.asList("menu", "start", "cancel", "status", "help"));
        } else if (args.length == 2 && args[0].equalsIgnoreCase("start")) {
            completions.addAll(Arrays.asList("30s", "1m", "5m", "10m", "30m", "1h"));
        }

        return completions;
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

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(plugin.getMessage("help-header"));
        sender.sendMessage(plugin.getMessage("help-restart"));
        sender.sendMessage(plugin.getMessage("help-cancel"));
        sender.sendMessage(plugin.getMessage("help-status"));
        sender.sendMessage(plugin.getMessage("help-menu"));
        sender.sendMessage(plugin.getMessage("help-time-format"));
    }
} 