package portableinventory;
import java.util.HashSet;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;

public class PISaveExecutor implements CommandExecutor, Listener{
    private final PortableInventory plugin;
    
    public PISaveExecutor(PortableInventory plugin) {
        this.plugin = plugin; // Store the plugin in situations where you need it.
    }
    
    public static HashSet<String> playerUsed;
    // 用于标记玩家是否刚刚执行过pisave，从而引导其它listener进行反馈
    
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        String playerName = sender.getName();
        if(!PIData.hasConnection(playerName)){
            sender.sendMessage("You are not connected to any stock right now!");
            return false;
        }
        if(playerUsed == null){
            // 防止该静态变量尚未初始化
            playerUsed = new HashSet<String>();
        }
        playerUsed.add(playerName);
        Inventory inv = Bukkit.createInventory(null, 36, "Put items to be stored");
        // 打开一个36格虚拟物品栏（和玩家物品栏大小一样）用来让玩家把物品放入并上传
        Player player = (Player)sender;
        player.openInventory(inv);
        return true;
    }
}
