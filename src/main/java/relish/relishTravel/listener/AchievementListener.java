package relish.relishTravel.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerAdvancementDoneEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.Material;
import org.bukkit.inventory.meta.ItemMeta;
import relish.relishTravel.RelishTravel;
import relish.relishTravel.config.ConfigManager;

public class AchievementListener implements Listener {
    
    private final RelishTravel plugin;
    private final ConfigManager config;
    
    public AchievementListener(RelishTravel plugin, ConfigManager config) {
        this.plugin = plugin;
        this.config = config;
    }
    
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerAdvancement(PlayerAdvancementDoneEvent event) {
        if (!config.isBlockVanillaAchievements()) {
            return;
        }
        
        Player player = event.getPlayer();
        String advancementKey = event.getAdvancement().getKey().getKey();
        
        // Block elytra-related achievements if using virtual elytra
        if (advancementKey.equals("elytra") || advancementKey.equals("story/elytra")) {
            ItemStack chestplate = player.getInventory().getChestplate();
            
            if (chestplate != null && chestplate.getType() == Material.ELYTRA) {
                // Check if it's a virtual elytra
                if (isVirtualElytra(chestplate)) {
                    if (config.isDebugMode()) {
                        plugin.getLogger().info("[DEBUG] Blocked vanilla elytra achievement for " + player.getName() + " (using virtual elytra)");
                    }
                    
                    // Revoke the advancement immediately
                    plugin.getServer().getScheduler().runTask(plugin, () -> {
                        player.getAdvancementProgress(event.getAdvancement()).getAwardedCriteria().forEach(criteria -> {
                            player.getAdvancementProgress(event.getAdvancement()).revokeCriteria(criteria);
                        });
                    });
                    
                    // Grant custom achievement if enabled
                    if (config.isGrantRelishAchievement()) {
                        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                            grantCustomAchievement(player);
                        }, 2L);
                    }
                }
            }
        }
    }
    
    private boolean isVirtualElytra(ItemStack item) {
        if (item == null || item.getType() != Material.ELYTRA) {
            return false;
        }
        
        if (!item.hasItemMeta()) {
            return false;
        }
        
        ItemMeta meta = item.getItemMeta();
        
        // Check if it's unbreakable and has RelishTravel display name
        return meta.isUnbreakable() && 
               meta.hasDisplayName() &&
               meta.getDisplayName().contains("RelishTravel");
    }
    
    private void grantCustomAchievement(Player player) {
        String title = config.getCustomAchievementTitle();
        String description = config.getCustomAchievementDescription();
        
        // Send in chat like vanilla achievements
        // Format: [PlayerName] has made the advancement [Achievement Title]
        String message = "§e" + player.getName() + " §rhas made the advancement §a[" + title + "]";
        
        // Broadcast to all players (like vanilla)
        plugin.getServer().broadcastMessage(message);
        
        // Send hover message to player only
        player.sendMessage("§7" + description);
        
        if (config.isDebugMode()) {
            plugin.getLogger().info("[DEBUG] Granted custom achievement to " + player.getName() + ": " + title);
        }
    }
}
