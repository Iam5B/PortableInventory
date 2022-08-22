package portableinventory;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.Command;

public class PIRemoveExecutor implements CommandExecutor{
    private final PortableInventory plugin;
    
    public PIRemoveExecutor(PortableInventory plugin) {
        this.plugin = plugin; // Store the plugin in situations where you need it.
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        String playerName = sender.getName();
        if(args.length != 2){
            sender.sendMessage("Require 2 arguments!");
            return false;
        }
        if(!PIData.checkStockExistence(args[0])){
            sender.sendMessage("Stock " + args[0] + " doesn't exist!");
            return false;
        }
        if(!PIData.checkOwnership(playerName, args[0])){
            sender.sendMessage("You are not the owner of stockpile: " + args[0]);
            return false;
        }
        if(!PIData.checkPassword(args[0], args[1])){
            sender.sendMessage("Wrong password!");
            return false;
        }
        PIData.removeStock(args[0]);
        sender.sendMessage("You have successfully removed stockpile: " + args[0]);
        return true;
    }
}
