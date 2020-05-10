package me.nubdotdev.spyglass;

import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.Map;

public class ConfigManager {

    private final Plugin plugin;
    private FileConfiguration config;
    private final Map<String, Object> values = new HashMap<>();
    private final Map<String, String> messages = new HashMap<>();

    public ConfigManager(Plugin plugin) {
        this.plugin = plugin;
        plugin.saveDefaultConfig();
        this.config = plugin.getConfig();
        reloadMessages();
    }

    public void reload() {
        plugin.reloadConfig();
        config = plugin.getConfig();
        reloadMessages();
    }

    public void reloadMessages() {
        values.clear();
        messages.clear();
        for (Map.Entry<String, Object> entry : config.getValues(false).entrySet())
            if (!(entry.getValue() instanceof ConfigurationSection))
                values.put(entry.getKey(), entry.getValue());
        ConfigurationSection messageSection = config.getConfigurationSection("messages");
        if (messageSection == null)
            return;
        for (Map.Entry<String, Object> message : messageSection.getValues(false).entrySet()) {
            try {
                messages.put(message.getKey(), cc((String) message.getValue()));
            } catch (ClassCastException e) {
                e.printStackTrace();
            }
        }
    }

    public Object getValue(String key) {
        return values.get(key);
    }

    public String getMessage(String key) {
        return messages.getOrDefault(key, "Unknown message '" + key + "'");
    }

    public FileConfiguration getConfig() {
        return config;
    }

    public Map<String, Object> getValues() {
        return values;
    }

    private String cc(String s) {
        return ChatColor.translateAlternateColorCodes('&', s);
    }

}
