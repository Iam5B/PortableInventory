package portableinventory;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.Command;

public class PICreateExecutor implements CommandExecutor{
    private final PortableInventory plugin;
    
    public PICreateExecutor(PortableInventory plugin) {
        this.plugin = plugin; // Store the plugin in situations where you need it.
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        String playerName = sender.getName();
        if(args.length != 2){
            sender.sendMessage("Require 2 arguments!");
            return false;
        }
        if(PIData.checkStockExistence(args[0])){
            sender.sendMessage("Stock " + args[0] + " already exists!");
            return false;
        }
        PIData.createStock(playerName, args[0], args[1]);
        sender.sendMessage("Successfully created stockpile: " + args[0]);
        return true;
    }
}
