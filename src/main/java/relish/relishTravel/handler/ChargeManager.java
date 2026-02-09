package relish.relishTravel.handler;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import relish.relishTravel.RelishTravel;
import relish.relishTravel.model.ChargeState;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ChargeManager {
    
    private final RelishTravel plugin;
    private final Map<UUID, ChargeState> chargingPlayers;
    private final Map<UUID, Long> lastSoundTime;
    private int taskId = -1;
    
    // Cache config values to avoid repeated lookups
    private boolean actionBarEnabled;
    private boolean soundsEnabled;
    private boolean particlesEnabled;
    
    public ChargeManager(RelishTravel plugin) {
        this.plugin = plugin;
        this.chargingPlayers = new ConcurrentHashMap<>();
        this.lastSoundTime = new ConcurrentHashMap<>();
        updateConfigCache();
        startChargeUpdateTask();
    }
    
    public void updateConfigCache() {
        this.actionBarEnabled = plugin.getConfigManager().isActionBarEnabled();
        this.soundsEnabled = plugin.getConfigManager().isSoundsEnabled();
        this.particlesEnabled = plugin.getConfigManager().isParticlesEnabled();
    }
    
    public void startCharge(Player player, double maxChargeTime) {
        UUID playerId = player.getUniqueId();
        Location startLocation = player.getLocation().clone();
        long startTime = System.currentTimeMillis();
        
        ChargeState state = new ChargeState(startLocation, startTime, maxChargeTime, true);
        chargingPlayers.put(playerId, state);
    }
    
    public void cancelCharge(Player player) {
        UUID playerId = player.getUniqueId();
        chargingPlayers.remove(playerId);
        lastSoundTime.remove(playerId);
    }
    
    public boolean isCharging(Player player) {
        return chargingPlayers.containsKey(player.getUniqueId());
    }
    
    public ChargeState getChargeState(Player player) {
        return chargingPlayers.get(player.getUniqueId());
    }
    
    private void startChargeUpdateTask() {
        int updateTicks = plugin.getConfigManager().getActionBarUpdateTicks();
        taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
            // Use cached config values instead of looking them up every tick
            for (UUID playerId : chargingPlayers.keySet()) {
                Player player = Bukkit.getPlayer(playerId);
                if (player == null || !player.isOnline()) {
                    continue;
                }
                
                ChargeState state = chargingPlayers.get(playerId);
                if (state == null) {
                    continue;
                }
                
                if (actionBarEnabled) {
                    updateActionBar(player, state);
                }
                
                if (soundsEnabled) {
                    playSoundEffects(player, state);
                }
                
                if (particlesEnabled) {
                    spawnParticles(player, state);
                }
            }
        }, 0L, updateTicks);
    }
    
    private void updateActionBar(Player player, ChargeState state) {
        int percent = (int) (state.getChargePercent() * 100);
        String progressBar = createProgressBar(state.getChargePercent());
        
        // Use direct component building instead of MiniMessage parsing for better performance
        net.kyori.adventure.text.Component message = net.kyori.adventure.text.Component.text()
            .append(net.kyori.adventure.text.Component.text("⚡ Charging: ", net.kyori.adventure.text.format.NamedTextColor.GOLD))
            .append(net.kyori.adventure.text.Component.text(percent + "%", net.kyori.adventure.text.format.NamedTextColor.YELLOW))
            .append(net.kyori.adventure.text.Component.text(" " + progressBar, net.kyori.adventure.text.format.NamedTextColor.YELLOW))
            .build();
        
        player.sendActionBar(message);
    }
    
    private String createProgressBar(double percent) {
        int bars = 20;
        int filled = (int) (bars * percent);
        StringBuilder bar = new StringBuilder();
        
        for (int i = 0; i < bars; i++) {
            bar.append(i < filled ? "█" : "▁");
        }
        
        return bar.toString();
    }
    
    private void playSoundEffects(Player player, ChargeState state) {
        long now = System.currentTimeMillis();
        UUID playerId = player.getUniqueId();
        Long lastSound = lastSoundTime.get(playerId);
        
        if (lastSound == null || now - lastSound >= 500) {
            try {
                org.bukkit.Sound sound = org.bukkit.Sound.valueOf(plugin.getConfigManager().getSoundType());
                float pitch = 0.5f + (float) state.getChargePercent() * 1.5f;
                player.playSound(player.getLocation(), sound, 0.5f, pitch);
                lastSoundTime.put(playerId, now);
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Invalid sound type: " + plugin.getConfigManager().getSoundType());
            }
        }
    }
    
    private void spawnParticles(Player player, ChargeState state) {
        try {
            org.bukkit.Particle particle = org.bukkit.Particle.valueOf(plugin.getConfigManager().getParticleType());
            Location loc = player.getLocation().add(0, 1, 0);
            
            double radius = 0.5 + state.getChargePercent() * 0.5;
            int particles = 5;
            
            for (int i = 0; i < particles; i++) {
                double angle = (2 * Math.PI * i) / particles;
                double x = radius * Math.cos(angle);
                double z = radius * Math.sin(angle);
                
                player.getWorld().spawnParticle(particle, loc.clone().add(x, 0, z), 1, 0, 0, 0, 0);
            }
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("Invalid particle type: " + plugin.getConfigManager().getParticleType());
        }
    }
    
    public void cleanup() {
        chargingPlayers.clear();
        lastSoundTime.clear();
        if (taskId != -1) {
            Bukkit.getScheduler().cancelTask(taskId);
        }
    }
}
