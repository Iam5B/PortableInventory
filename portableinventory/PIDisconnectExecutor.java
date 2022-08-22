package portableinventory;
import java.util.ArrayList;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;

public class PIDisconnectExecutor implements CommandExecutor{
    private final PortableInventory plugin;
    
    public PIDisconnectExecutor(PortableInventory plugin) {
        this.plugin = plugin; 
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        String playerName = sender.getName();
        if(args.length != 1){
            sender.sendMessage("Require 1 argument!");
            return false;
        }
        if(!PIData.checkStockExistence(args[0])){
            sender.sendMessage("Stockpile doesn't exists!");
            return false;
        }
        if(PIData.checkOwnership(playerName, args[0])){
            sender.sendMessage("You are the owner of that stockpile, please consider removing that stockpile!");
            return false;
        }
        if(PIData.checkAccess(playerName, args[0])){
            PIData.disconnect(playerName, args[0]);
            sender.sendMessage("You have successfully disconnected from stock: " + args[0]);
            return true;
        }
        return true;
    }
}
