package me.nubdotdev.spyglass;

import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.scoreboard.Team;

import java.util.*;

public class CommandListener implements Listener {

    private final Spyglass plugin;
    private final Map<UUID, UUID> replyRecipients = new HashMap<>();

    public CommandListener(Spyglass plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onCommandPreprocess(PlayerCommandPreprocessEvent e) {
        Player sender = e.getPlayer();
        if (sender.hasPermission("spyglass.admin"))
            return;
        String message = e.getMessage();
        String[] args = message.split(" ");
        String command = args[0].toLowerCase().substring(1);
        if (plugin.getConfig().getBoolean("ignore-unregistered-commands") &&
                Bukkit.getServer().getPluginCommand(command) == null)
            return;
        UUID senderUuid = sender.getUniqueId();
        Player recipient = null;
        String recipientName = null;
        int messageStart;
        if (plugin.getConfig().getBoolean("spy-teammsg") &&
                (command.equalsIgnoreCase("teammsg") || command.equalsIgnoreCase("tm"))) {
            for (Team t : sender.getScoreboard().getTeams())
                if (t.getEntries().contains(sender.getName()))
                    recipientName = "team " + t.getName();
            messageStart = 1;
        } else if (containsCommand(plugin.getConfig().getStringList("social-commands"), command)) {
            if (args.length < 3)
                return;
            recipient = Bukkit.getPlayer(args[1]);
            if (recipient == null || recipient.hasPermission("spyglass.admin"))
                return;
            replyRecipients.put(senderUuid, recipient.getUniqueId());
            recipientName = recipient.getName();
            messageStart = 2;
        } else if (containsCommand(plugin.getConfig().getStringList("reply-commands"), command)) {
            if (args.length < 2)
                return;
            recipient = Bukkit.getPlayer(replyRecipients.get(senderUuid));
            if (recipient == null || recipient.hasPermission("spyglass.admin"))
                return;
            recipientName = recipient.getName();
            messageStart = 1;
        } else {
            if (containsCommand(plugin.getConfig().getStringList("ignored-commands"), command) ^
                    plugin.getConfig().getBoolean("invert-ignored-commands"))
                return;
            boolean blacklisted = plugin.getConfig().getBoolean("blacklist-is-whitelist") ^
                    containsCommand(plugin.getConfig().getStringList("command-blacklist"), command);
            sendCommandSpy(sender, message, blacklisted);
            return;
        }
        if (recipient != null)
            replyRecipients.put(recipient.getUniqueId(), senderUuid);
        if (recipientName != null)
            sendSocialSpy(sender, recipientName, String.join(" ", Arrays.copyOfRange(args, messageStart, args.length)));
        if (plugin.getConfig().getBoolean("command-spy-on-social-commands"))
            sendCommandSpy(sender, message, false);
    }

    private void sendCommandSpy(Player sender, String command, boolean blacklisted) {
        for (UUID uuid : plugin.getCommandSpy()) {
            Player p = Bukkit.getPlayer(uuid);
            if (p != null && !uuid.equals(sender.getUniqueId())) {
                if (!blacklisted || p.hasPermission("spyglass.command.bypass")) {
                    p.sendMessage(plugin.getConfigManager().getMessage("command-spy")
                            .replaceAll("%sender%", sender.getName())
                            .replaceAll("%command%", command)
                    );
                }
            }
        }
    }

    private void sendSocialSpy(Player sender, String recipientName, String message) {
        for (UUID uuid : plugin.getSocialSpy()) {
            Player p = Bukkit.getPlayer(uuid);
            if (p != null && !uuid.equals(sender.getUniqueId()) && !p.getName().equals(recipientName)) {
                p.sendMessage(plugin.getConfigManager().getMessage("social-spy")
                        .replaceAll("%sender%", sender.getName())
                        .replaceAll("%recipient%", recipientName)
                        .replaceAll("%message%", message)
                );
            }
        }
    }

    private boolean containsCommand(List<String> commands, String command) {
        PluginCommand pluginCommand = Bukkit.getServer().getPluginCommand(command);
        if (pluginCommand == null)
            return commands.contains(command);
        String pluginName = pluginCommand.getPlugin().getName();
        boolean ignoreAliases = plugin.getConfig().getBoolean("ignore-aliases");
        for (String s : commands) {
            if ((s.endsWith(":*") && pluginName.equalsIgnoreCase(s.substring(0, s.length() - 2))) ||
                    (!ignoreAliases && (containsIgnoreCase(pluginCommand.getAliases(), s) || pluginCommand.getName().equalsIgnoreCase(s))) ||
                    command.equalsIgnoreCase(s))
                return true;
        }
        return false;
    }

    private boolean containsIgnoreCase(List<String> list, String s) {
        for (String s1 : list)
            if (s1.equalsIgnoreCase(s))
                return true;
        return false;
    }

}
