package com.dark.restartplugin;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AutoRestartCommand implements CommandExecutor, TabCompleter {
    private final RestartPlugin plugin;

    public AutoRestartCommand(RestartPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!sender.hasPermission("darkrestart.autorestart")) {
            sender.sendMessage(plugin.getMessage("no-permission"));
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage(plugin.getMessage("autorestart-usage"));
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "set":
                if (args.length < 2) {
                    sender.sendMessage(plugin.getMessage("autorestart-usage"));
                    return true;
                }

                try {
                    long hours = Long.parseLong(args[1]);
                    if (hours <= 0) {
                        sender.sendMessage(plugin.getMessage("greater-than-zero"));
                        return true;
                    }

                    plugin.getAutoRestartManager().setInterval(hours);
                    sender.sendMessage(plugin.getMessage("autorestart-set")
                            .replace("%hours%", String.valueOf(hours)));
                } catch (NumberFormatException e) {
                    sender.sendMessage(plugin.getMessage("invalid-number"));
                }
                return true;

            case "cancel":
                if (plugin.getAutoRestartManager().isAutoRestartEnabled()) {
                    plugin.getAutoRestartManager().cancelAutoRestart();
                    sender.sendMessage(plugin.getMessage("autorestart-cancelled"));
                } else {
                    sender.sendMessage(plugin.getMessage("autorestart-not-active"));
                }
                return true;

            case "status":
                sender.sendMessage(plugin.getAutoRestartManager().getStatus());
                return true;

            default:
                sender.sendMessage(plugin.getMessage("autorestart-usage"));
                return true;
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            completions.addAll(Arrays.asList("set", "cancel", "status"));
        } else if (args.length == 2 && args[0].equalsIgnoreCase("set")) {
            completions.addAll(Arrays.asList("1", "2", "3", "4", "6", "8", "12", "24"));
        }

        return completions;
    }
} 