package portableinventory;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.Command;

public class PIConnectExecutor implements CommandExecutor{
    private final PortableInventory plugin;
    
    public PIConnectExecutor(PortableInventory plugin) {
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
        if(PIData.checkAccess(playerName, args[0])){
            sender.sendMessage("You already have the access to that stock!");
            return false;
        }
        if(!PIData.checkPassword(args[0], args[1])){
            sender.sendMessage("Wrong password!");
            return false;
        }
        PIData.connectStock(playerName, args[0]);
        sender.sendMessage("You have successfully connected to stock: " + args[0]);
        return true;
    }
}
