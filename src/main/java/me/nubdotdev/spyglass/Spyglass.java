package me.nubdotdev.spyglass;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashSet;
import java.util.Set;

public class Spyglass extends JavaPlugin implements Listener {

    private ConfigManager configManager;

    private final Set<Player> commandSpy = new HashSet<>();
    private final Set<Player> socialSpy = new HashSet<>();

    @Override
    public void onEnable() {
        configManager = new ConfigManager(this);
        getCommand("spyglass").setExecutor(new SpyglassCommand(this));
        Bukkit.getPluginManager().registerEvents(new CommandListener(this), this);
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public Set<Player> getCommandSpy() {
        return commandSpy;
    }

    public Set<Player> getSocialSpy() {
        return socialSpy;
    }

}
