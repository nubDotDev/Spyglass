package me.nubdotdev.spyglass;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class SpyglassCommand implements CommandExecutor, TabCompleter {

    private final Spyglass plugin;
    private final List<String> subCommands = Arrays.asList("reload", "command", "social");

    public SpyglassCommand(Spyglass plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length >= 1) {
            if (subCommands.contains(args[0].toLowerCase())) {
                if (args[0].equalsIgnoreCase("reload")) {
                    if (sender.hasPermission("spyglass.reload")) {
                        plugin.getConfigManager().reload();
                        sender.sendMessage(plugin.getConfigManager().getMessage("reload"));
                    } else {
                        sender.sendMessage(plugin.getConfigManager().getMessage("no-perms"));
                    }
                } else {
                    String spy = args[0].toLowerCase();
                    Player p;
                    if (args.length >= 2 && !args[1].equalsIgnoreCase(sender.getName())) {
                        if (!sender.hasPermission("spyglass." + spy + ".other")) {
                            sender.sendMessage(plugin.getConfigManager().getMessage("no-perms"));
                            return true;
                        }
                        p = Bukkit.getPlayer(args[1]);
                        if (p == null) {
                            sender.sendMessage(plugin.getConfigManager().getMessage("no-player"));
                            return true;
                        }
                    } else if (sender instanceof Player) {
                        if (!sender.hasPermission("spyglass." + spy + ".self")) {
                            sender.sendMessage(plugin.getConfigManager().getMessage("no-perms"));
                            return true;
                        }
                        p = (Player) sender;
                    } else {
                        sender.sendMessage("Only players can use that command!");
                        return true;
                    }
                    String mode;
                    UUID uuid = p.getUniqueId();
                    if (spy.equals("command")) {
                        if (plugin.getCommandSpy().contains(uuid)) {
                            plugin.getCommandSpy().remove(uuid);
                            mode = "disabled";
                        } else {
                            plugin.getCommandSpy().add(uuid);
                            mode = "enabled";
                        }
                    } else {
                        if (plugin.getSocialSpy().contains(uuid)) {
                            plugin.getSocialSpy().remove(uuid);
                            mode = "disabled";
                        } else {
                            plugin.getSocialSpy().add(uuid);
                            mode = "enabled";
                        }
                    }
                    String message = plugin.getConfigManager().getMessage("toggle")
                            .replaceAll("%spy%", spy)
                            .replaceAll("%mode%", mode)
                            .replaceAll("%player%", p.getName());
                    sender.sendMessage(message);
                    if (!p.getName().equals(sender.getName()))
                        p.sendMessage(message);
                }
                return true;
            }
        }
        if (sender.hasPermission("spyglass.help"))
            sender.sendMessage(ChatColor.RED + "Usage: " + cmd.getUsage());
        else
            sender.sendMessage(plugin.getConfigManager().getMessage("no-perms"));
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        List<String> completions = new ArrayList<>();
        if (sender.hasPermission("spyglass.help"))
            if (args.length == 1)
                for (String s : subCommands)
                    if (StringUtil.startsWithIgnoreCase(s, args[0]))
                        completions.add(s);
        return completions;
    }

}
