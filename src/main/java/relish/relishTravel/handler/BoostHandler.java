package relish.relishTravel.handler;

import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import relish.relishTravel.RelishTravel;
import relish.relishTravel.config.ConfigManager;
import relish.relishTravel.message.MessageManager;
import relish.relishTravel.model.LaunchData;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class BoostHandler {
    
    private final RelishTravel plugin;
    private final ConfigManager config;
    private final MessageManager messages;
    private final LaunchHandler launchHandler;
    private final Map<UUID, Long> normalElytraCooldowns;
    
    public BoostHandler(RelishTravel plugin, ConfigManager config,
                        MessageManager messages, LaunchHandler launchHandler) {
        this.plugin = plugin;
        this.config = config;
        this.messages = messages;
        this.launchHandler = launchHandler;
        this.normalElytraCooldowns = new ConcurrentHashMap<>();
    }
    
    public void applyBoost(Player player) {
        if (!config.isRightClickBoostEnabled()) {
            return;
        }
        
        if (!player.isGliding()) {
            return;
        }
        
        LaunchData launchData = launchHandler.getActiveLaunchData(player);
        
        if (launchData == null) {
            if (!config.isAllowBoostForNormalElytra()) {
                if (config.isDebugMode()) {
                    plugin.getLogger().info("[DEBUG] [" + player.getName() + "] Boost denied - normal elytra boost disabled in config");
                }
                return;
            }
            
            UUID playerId = player.getUniqueId();
            long now = System.currentTimeMillis();
            Long cooldownEnd = normalElytraCooldowns.get(playerId);
            
            if (!player.hasPermission("relishtravel.bypass.boost-cooldown")) {
                if (cooldownEnd != null && now < cooldownEnd) {
                    long remainingMs = cooldownEnd - now;
                    String formattedTime = String.format("%.2fs", remainingMs / 1000.0);
                    Map<String, String> placeholders = new HashMap<>();
                    placeholders.put("time", formattedTime);
                    messages.sendMessage(player, "boost.cooldown", placeholders);
                    
                    if (config.isDebugMode()) {
                        plugin.getLogger().info("[DEBUG] [" + player.getName() + "] Boost on cooldown (normal elytra): " + formattedTime);
                    }
                    return;
                }
            }
            
            applyBoostVelocity(player);
            
            long cooldownMillis = config.getRightClickBoostCooldown() * 1000L;
            normalElytraCooldowns.put(playerId, now + cooldownMillis);
            
            if (config.isDebugMode()) {
                plugin.getLogger().info("[DEBUG] [" + player.getName() + "] Boost applied (normal elytra)");
            }
            return;
        }
        
        if (!player.hasPermission("relishtravel.bypass.boost-cooldown")) {
            if (launchData.isRightClickBoostOnCooldown()) {
                long remainingMs = launchData.rightClickBoostCooldownUntil() - System.currentTimeMillis();
                String formattedTime = String.format("%.2fs", Math.max(0, remainingMs) / 1000.0);
                Map<String, String> placeholders = new HashMap<>();
                placeholders.put("time", formattedTime);
                messages.sendMessage(player, "boost.cooldown", placeholders);
                
                if (config.isDebugMode()) {
                    plugin.getLogger().info("[DEBUG] [" + player.getName() + "] Boost on cooldown (RelishTravel): " + formattedTime);
                }
                return;
            }
        }
        
        int maxUses = getPlayerBoostLimit(player);
        if (maxUses >= 0 && launchData.rightClickBoostCount() >= maxUses) {
            messages.sendMessage(player, "boost.max-uses");
            
            if (config.isDebugMode()) {
                plugin.getLogger().info("[DEBUG] [" + player.getName() + "] Max boosts reached (" + maxUses + ")");
            }
            return;
        }
        
        applyBoostVelocity(player);
        
        long cooldownMillis = config.getRightClickBoostCooldown() * 1000L;
        LaunchData updated = launchData.withRightClickBoost(cooldownMillis);
        launchHandler.updateLaunchData(player, updated);
        
        if (config.isDebugMode()) {
            plugin.getLogger().info("[DEBUG] [" + player.getName() + "] Boost applied (RelishTravel launch)");
        }
    }
    
    private int getPlayerBoostLimit(Player player) {
        Map<String, Integer> permissionLimits = config.getBoostPermissionLimits();
        int highestLimit = config.getMaxBoostsPerGlide();
        
        for (Map.Entry<String, Integer> entry : permissionLimits.entrySet()) {
            if (player.hasPermission(entry.getKey())) {
                int limit = entry.getValue();
                if (limit == -1) {
                    return -1;
                }
                if (limit > highestLimit) {
                    highestLimit = limit;
                }
            }
        }
        
        return highestLimit;
    }
    
    private void applyBoostVelocity(Player player) {
        Vector velocity = player.getVelocity();
        Vector direction = player.getLocation().getDirection().normalize();
        
        velocity.add(direction.multiply(config.getRightClickBoostSpeed()));
        velocity = capVelocity(velocity);
        player.setVelocity(velocity);
        
        if (config.isSoundsEnabled()) {
            player.playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_FLAP, 1.0f, 1.8f);
        }
    }
    
    private Vector capVelocity(Vector velocity) {
        double maxHorizontal = config.getMaxHorizontalVelocity();
        double maxVertical = config.getMaxVerticalVelocity();
        
        if (Math.abs(velocity.getX()) > maxHorizontal) {
            velocity.setX(Math.signum(velocity.getX()) * maxHorizontal);
        }
        if (Math.abs(velocity.getZ()) > maxHorizontal) {
            velocity.setZ(Math.signum(velocity.getZ()) * maxHorizontal);
        }
        if (Math.abs(velocity.getY()) > maxVertical) {
            velocity.setY(Math.signum(velocity.getY()) * maxVertical);
        }
        
        return velocity;
    }
    
    public void clearCooldown(Player player) {
        normalElytraCooldowns.remove(player.getUniqueId());
    }
    
    public void cleanup() {
        normalElytraCooldowns.clear();
    }
}
