package me.nubdotdev.spyglass;

import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class Spyglass extends JavaPlugin implements Listener {

    private ConfigManager configManager;

    private final Set<UUID> commandSpy = new HashSet<>();
    private final Set<UUID> socialSpy = new HashSet<>();

    @Override
    public void onEnable() {
        configManager = new ConfigManager(this);
        getCommand("spyglass").setExecutor(new SpyglassCommand(this));
        Bukkit.getPluginManager().registerEvents(new CommandListener(this), this);
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public Set<UUID> getCommandSpy() {
        return commandSpy;
    }

    public Set<UUID> getSocialSpy() {
        return socialSpy;
    }

}
