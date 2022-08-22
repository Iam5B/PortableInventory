package portableinventory;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.Command;

public class PISwitchExecutor implements CommandExecutor{
    private final PortableInventory plugin;
    
    public PISwitchExecutor(PortableInventory plugin) {
        this.plugin = plugin; // Store the plugin in situations where you need it.
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        String playerName = sender.getName();
        if(args.length != 1){
            sender.sendMessage("Require 1 argument!");
            return false;
        }
        if(!PIData.checkAccess(playerName, args[0])){
            sender.sendMessage("Stock not found, or you don't have the access to that stockpile!");
            return false;
        }
        PIData.switchStock(playerName, args[0]);
        PITakeExecutor.resetPlayerPage(playerName, true);
        sender.sendMessage("You have successfully switched to stockpile: " + args[0]);
        return true;
    }
}
