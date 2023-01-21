package portableinventory;
import java.util.ArrayList;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class PISaveAllExecutor implements CommandExecutor{
    private final PortableInventory plugin;
    
    public PISaveAllExecutor(PortableInventory plugin) {
        this.plugin = plugin; // Store the plugin in situations where you need it.
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        String playerName = sender.getName();
        if(!PIData.hasConnection(playerName)){
            sender.sendMessage("You are not connected to any stock right now!");
            return false;
        }
        Player player = (Player)sender;
        ItemStack[] allItemsList = player.getInventory().getStorageContents();
        ArrayList<ItemStack> allItems = new ArrayList<ItemStack>();
        for(ItemStack item: allItemsList){
            if(item == null){
                continue;
            }
            allItems.add(item);
        }
        PIData.saveItems(playerName, allItems);
        player.getInventory().setStorageContents(new ItemStack[0]);
        sender.sendMessage("All items saved!");
        return true;
    }
}
