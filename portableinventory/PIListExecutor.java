package portableinventory;
import java.util.ArrayList;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;

public class PIListExecutor implements CommandExecutor{
    private final PortableInventory plugin;
    
    public PIListExecutor(PortableInventory plugin) {
        this.plugin = plugin; // Store the plugin in situations where you need it.
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if(args.length == 1){
            // 允许OP强制查看他人所拥有的仓库列表
            Player player = (Player)sender;
            if(player.isOp()){
                String playerName = args[0];
                ArrayList<String> stockpileMessageList = PIData.listStocks(playerName);
                for(String line : stockpileMessageList){
                    sender.sendMessage(line);
                }
                return true;
            }
            else{
                sender.sendMessage("Sorry, this function can be only used by an operator!");
                return false;
            }
        }
        else if(args.length == 0){
            String playerName = sender.getName();
            ArrayList<String> stockpileMessageList = PIData.listStocks(playerName);
            for(String line : stockpileMessageList){
                sender.sendMessage(line);
            }
            return true;
        }
        else{
            sender.sendMessage("Too many arguments!");
            return false;
        }
    }
}
