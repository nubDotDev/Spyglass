package me.nubdotdev.spyglass;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scoreboard.Team;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class CommandListener implements Listener {

    private final Spyglass plugin;
    private final Map<Player, Player> replyRecipients = new HashMap<>();

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
        String command = args[0].toLowerCase().replaceAll("/", "");
        if (plugin.getConfig().getStringList("ignored-commands").contains(command))
            return;
        Player recipient = null;
        String recipientName = null;
        int messageStart;
        if (plugin.getConfig().getBoolean("spy-teammsg") &&
                (command.equalsIgnoreCase("teammsg") || command.equalsIgnoreCase("tm"))) {
            for (Team t :sender.getScoreboard().getTeams())
                if (t.getEntries().contains(sender.getName()))
                    recipientName = "team " + t.getName();
            messageStart = 1;
        } else if (plugin.getConfig().getStringList("social-commands").contains(command)) {
            if (args.length < 3)
                return;
            recipient = Bukkit.getPlayer(args[1]);
            if (recipient == null || recipient.hasPermission("spyglass.admin"))
                return;
            replyRecipients.put(sender, recipient);
            recipientName = recipient.getName();
            messageStart = 2;
        } else if (plugin.getConfig().getStringList("reply-commands").contains(command)) {
            if (args.length < 2)
                return;
            recipient = replyRecipients.get(sender);
            if (recipient == null || !Bukkit.getOnlinePlayers().contains(recipient) || recipient.hasPermission("spyglass.admin")) {
                replyRecipients.remove(sender);
                return;
            }
            recipientName = recipient.getName();
            messageStart = 1;
        } else {
            boolean blacklisted = plugin.getConfig().getBoolean("blacklist-is-whitelist") ^
                    plugin.getConfig().getStringList("command-blacklist").contains(command);
            sendCommandSpy(sender, message, blacklisted);
            return;
        }
        if (recipient != null)
            replyRecipients.put(recipient, sender);
        if (recipientName != null)
            sendSocialSpy(sender, recipientName, String.join(" ", Arrays.copyOfRange(args, messageStart, args.length)));
        if (plugin.getConfig().getBoolean("command-spy-on-social-commands"))
            sendCommandSpy(sender, message, false);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        replyRecipients.remove(e.getPlayer());
    }

    private void sendCommandSpy(Player sender, String command, boolean blacklisted) {
        for (Player p : plugin.getCommandSpy()) {
            if (p != sender) {
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
        for (Player p : plugin.getSocialSpy()) {
            if (p != sender && !p.getName().equals(recipientName)) {
                p.sendMessage(plugin.getConfigManager().getMessage("social-spy")
                        .replaceAll("%sender%", sender.getName())
                        .replaceAll("%recipient%", recipientName)
                        .replaceAll("%message%", message)
                );
            }
        }
    }

}
