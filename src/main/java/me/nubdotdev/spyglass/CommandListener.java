package me.nubdotdev.spyglass;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommandListener implements Listener {

    private final Spyglass plugin;
    private final Map<Player, Player> replyRecipients = new HashMap<>();

    public CommandListener(Spyglass plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    @SuppressWarnings("unchecked")
    public void onCommandPreprocess(PlayerCommandPreprocessEvent e) {
        Player sender = e.getPlayer();
        if (sender.hasPermission("spyglass.admin"))
            return;
        String message = e.getMessage();
        String[] args = message.split(" ");
        String command = args[0].toLowerCase().replaceAll("/", "");
        if (((List<String>) plugin.getConfigManager().getValue("social-commands")).contains(command)) {
            if (args.length < 3)
                return;
            Player recipient = Bukkit.getPlayer(args[1]);
            if (recipient == null || recipient.hasPermission("spyglass.admin"))
                return;
            replyRecipients.put(sender, recipient);
            replyRecipients.put(recipient, sender);
            sendSocialSpy(sender, recipient, String.join(" ", Arrays.copyOfRange(args, 2, args.length)));
            if ((boolean) plugin.getConfigManager().getValue("command-spy-on-social-commands"))
                sendCommandSpy(sender, message, false);
            return;
        } else if (((List<String>) plugin.getConfigManager().getValue("reply-commands")).contains(command)) {
            if (args.length < 2)
                return;
            Player recipient = replyRecipients.get(sender);
            if (!Bukkit.getOnlinePlayers().contains(recipient)) {
                replyRecipients.remove(sender);
                return;
            }
            if (recipient == null || recipient.hasPermission("spyglass.admin"))
                return;
            replyRecipients.put(recipient, sender);
            sendSocialSpy(sender, recipient, String.join(" ", Arrays.copyOfRange(args, 2, args.length)));
            if ((boolean) plugin.getConfigManager().getValue("command-spy-on-social-commands"))
                sendCommandSpy(sender, message, false);
            return;
        }
        boolean blacklisted = (boolean) plugin.getConfigManager().getValue("blacklist-is-whitelist") ^
                ((List<String>) plugin.getConfigManager().getValue("command-blacklist")).contains(command);
        sendCommandSpy(sender, message, blacklisted);
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

    private void sendSocialSpy(Player sender, Player recipient, String message) {
        for (Player p : plugin.getSocialSpy()) {
            if (p != sender && p != recipient) {
                p.sendMessage(plugin.getConfigManager().getMessage("social-spy")
                        .replaceAll("%sender%", sender.getName())
                        .replaceAll("%recipient%", recipient.getName())
                        .replaceAll("%message%", message)
                );
            }
        }
    }

}
