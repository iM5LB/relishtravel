package relish.relishTravel.config;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import relish.relishTravel.RelishTravel;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Set;

public class ConfigUpdater {
    
    private final RelishTravel plugin;
    private final File configFile;
    private final File backupFolder;
    private static final int CURRENT_CONFIG_VERSION = 1;
    
    public ConfigUpdater(RelishTravel plugin) {
        this.plugin = plugin;
        this.configFile = new File(plugin.getDataFolder(), "config.yml");
        this.backupFolder = new File(plugin.getDataFolder(), "backups");
    }
    
    public void updateConfig() {
        if (!configFile.exists()) {
            plugin.saveDefaultConfig();
            return;
        }
        
        try {
            FileConfiguration oldConfig = YamlConfiguration.loadConfiguration(configFile);
            int configVersion = oldConfig.getInt("config-version", 0);
            
            // Check if migration is needed
            if (configVersion < CURRENT_CONFIG_VERSION) {
                plugin.getLogger().info("Migrating config from version " + configVersion + " to " + CURRENT_CONFIG_VERSION);
                backupConfig();
                migrateConfig(oldConfig, configVersion);
                oldConfig.set("config-version", CURRENT_CONFIG_VERSION);
                oldConfig.save(configFile);
                plugin.getLogger().info("Config migration completed successfully!");
            } else if (configVersion > CURRENT_CONFIG_VERSION) {
                plugin.getLogger().warning("Config version (" + configVersion + ") is newer than plugin version (" + CURRENT_CONFIG_VERSION + ")");
                plugin.getLogger().warning("This may cause issues. Consider updating the plugin.");
            }
            
            // Merge any new keys from default config
            mergeNewKeys(oldConfig);
            
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to update config: " + e.getMessage());
            if (plugin.isDebugMode()) {
                e.printStackTrace();
            }
        }
    }
    
    private void migrateConfig(FileConfiguration config, int fromVersion) {
        // Future migrations will go here
        // Example:
        // if (fromVersion < 1) {
        //     migrateToV1(config);
        // }
        // if (fromVersion < 2) {
        //     migrateToV2(config);
        // }
        
        plugin.getLogger().info("No migrations needed for version " + fromVersion);
    }
    
    private void mergeNewKeys(FileConfiguration oldConfig) {
        try {
            // Load default config from resources
            FileConfiguration defaultConfig = YamlConfiguration.loadConfiguration(
                new java.io.InputStreamReader(plugin.getResource("config.yml"))
            );
            
            boolean updated = false;
            Set<String> defaultKeys = defaultConfig.getKeys(true);
            
            for (String key : defaultKeys) {
                if (defaultConfig.isConfigurationSection(key)) {
                    continue;
                }
                
                // Add new keys that don't exist in old config
                if (!oldConfig.contains(key)) {
                    oldConfig.set(key, defaultConfig.get(key));
                    plugin.getLogger().info("Added new config key: " + key);
                    updated = true;
                }
            }
            
            if (updated) {
                oldConfig.save(configFile);
                plugin.getLogger().info("Config updated with new keys!");
            }
            
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to merge new config keys: " + e.getMessage());
        }
    }
    
    private void backupConfig() throws IOException {
        if (!backupFolder.exists()) {
            backupFolder.mkdirs();
        }
        
        String timestamp = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date());
        File backupFile = new File(backupFolder, "config_" + timestamp + ".yml");
        Files.copy(configFile.toPath(), backupFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        plugin.getLogger().info("Config backed up to: " + backupFile.getName());
    }
}
