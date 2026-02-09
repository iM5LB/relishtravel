package relish.relishTravel.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import relish.relishTravel.RelishTravel;
import relish.relishTravel.config.ConfigManager;
import relish.relishTravel.message.MessageManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LaunchCommand implements CommandExecutor, TabCompleter {
    
    private final RelishTravel plugin;
    private final ConfigManager config;
    private final MessageManager messages;
    
    public LaunchCommand(RelishTravel plugin, ConfigManager config, MessageManager messages) {
        this.plugin = plugin;
        this.config = config;
        this.messages = messages;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            messages.sendConsoleMessage("command.players-only");
            return true;
        }
        
        if (!player.hasPermission("relishtravel.fastlaunch")) {
            messages.sendMessage(player, "command.launch.no-permission");
            return true;
        }
        
        int percent = 100;
        if (args.length > 0) {
            try {
                percent = Integer.parseInt(args[0]);
                if (percent < 0 || percent > 100) {
                    messages.sendMessage(player, "command.launch.invalid-percent");
                    return true;
                }
            } catch (NumberFormatException e) {
                messages.sendMessage(player, "command.launch.invalid-format");
                return true;
            }
        }
        
        executeInstantLaunch(player, percent);
        return true;
    }
    
    private void executeInstantLaunch(Player player, int percent) {
        if (!performPreFlightChecks(player)) {
            return;
        }
        
        double chargePercent = percent / 100.0;
        boolean success = plugin.getLaunchHandler().executeInstantLaunch(player, chargePercent);
        
        if (success) {
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("percent", String.valueOf(percent));
            messages.sendMessage(player, "command.launch.success", placeholders);
        } else {
            messages.sendMessage(player, "command.launch.failed");
        }
    }
    
    private boolean performPreFlightChecks(Player player) {
        if (!config.isEnabled()) {
            messages.sendMessage(player, "command.launch.plugin-disabled");
            return false;
        }
        
        if (!player.hasPermission("relishtravel.use")) {
            messages.sendMessage(player, "command.launch.no-use-permission");
            return false;
        }
        
        String worldName = player.getWorld().getName();
        if (config.getDisabledWorlds().contains(worldName) && 
            !player.hasPermission("relishtravel.bypass.disabled-worlds")) {
            messages.sendMessage(player, "command.launch.world-disabled");
            return false;
        }
        
        relish.relishTravel.validator.SafetyValidator validator = 
            new relish.relishTravel.validator.SafetyValidator(config);
        
        if (!validator.canStartCharge(player, messages)) {
            return false;
        }
        
        if (!plugin.getElytraHandler().canUseElytra(player)) {
            messages.sendMessage(player, "command.launch.cannot-use-elytra");
            return false;
        }
        
        return true;
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> suggestions = new ArrayList<>();
        
        if (args.length == 1) {
            suggestions.add("25");
            suggestions.add("50");
            suggestions.add("75");
            suggestions.add("100");
        }
        
        return suggestions;
    }
}
