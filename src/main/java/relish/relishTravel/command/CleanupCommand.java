package relish.relishTravel.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import relish.relishTravel.RelishTravel;
import relish.relishTravel.message.MessageManager;

public class CleanupCommand implements CommandExecutor {
    
    private final RelishTravel plugin;
    private final MessageManager messages;
    
    public CleanupCommand(RelishTravel plugin, MessageManager messages) {
        this.plugin = plugin;
        this.messages = messages;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            messages.sendConsoleMessage("command.players-only");
            return true;
        }
        
        plugin.getElytraHandler().restoreChestplate(player);
        plugin.getLaunchHandler().handleGlideEnd(player);
        plugin.getChargeManager().cancelCharge(player);
        
        messages.sendMessage(player, "command.cleanup-success");
        return true;
    }
}
