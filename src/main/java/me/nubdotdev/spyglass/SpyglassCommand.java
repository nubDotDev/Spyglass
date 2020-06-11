package me.nubdotdev.spyglass;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.*;

public class SpyglassCommand implements CommandExecutor, TabCompleter {

    private final ConfigManager manager;
    private final Set<UUID> commandSpy;
    private final Set<UUID> socialSpy;
    private final List<String> subCommands = Arrays.asList("reload", "command", "social");

    public SpyglassCommand(Spyglass plugin) {
        this.manager = plugin.getConfigManager();
        this.commandSpy = plugin.getCommandSpy();
        this.socialSpy = plugin.getSocialSpy();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length >= 1) {
            if (subCommands.contains(args[0].toLowerCase())) {
                if (args[0].equalsIgnoreCase("reload")) {
                    if (sender.hasPermission("spyglass.reload")) {
                        manager.reload();
                        sender.sendMessage(manager.getMessage("reload"));
                    } else {
                        sender.sendMessage(manager.getMessage("no-perms"));
                    }
                } else {
                    String spy = args[0].toLowerCase();
                    Player p;
                    if (args.length >= 2 && !args[1].equalsIgnoreCase(sender.getName())) {
                        if (!sender.hasPermission("spyglass." + spy + ".other")) {
                            sender.sendMessage(manager.getMessage("no-perms"));
                            return true;
                        }
                        p = Bukkit.getPlayer(args[1]);
                        if (p == null) {
                            sender.sendMessage(manager.getMessage("no-player"));
                            return true;
                        }
                    } else if (sender instanceof Player) {
                        if (!sender.hasPermission("spyglass." + spy + ".self")) {
                            sender.sendMessage(manager.getMessage("no-perms"));
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
                        if (commandSpy.contains(uuid)) {
                            commandSpy.remove(uuid);
                            mode = "disabled";
                        } else {
                            commandSpy.add(uuid);
                            mode = "enabled";
                        }
                    } else {
                        if (socialSpy.contains(uuid)) {
                            socialSpy.remove(uuid);
                            mode = "disabled";
                        } else {
                            socialSpy.add(uuid);
                            mode = "enabled";
                        }
                    }
                    String message = manager.getMessage("toggle")
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
            sender.sendMessage(manager.getMessage("no-perms"));
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
