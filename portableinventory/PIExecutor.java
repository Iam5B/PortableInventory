/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package portableinventory;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import static org.bukkit.Material.YELLOW_STAINED_GLASS_PANE;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
/**
 *
 * @author User
 */
public class PIExecutor implements CommandExecutor{
    private final PortableInventory plugin;
    public static HashMap<String, Integer> playerPage;
    // 该静态变量用于记录玩家所在页数，主要用于支持连续多次翻页操作
    public static HashSet<String> playerUsed;
    // 该静态变量用于记录玩家是否输入过指令，也可以理解为玩家是否正在使用pi所对应的操作界面
    public static HashMap<String, Integer> playerWishIndex;
    // 该变量用于记录玩家所期望的物品的下标，用于配合物品数量进行指定数量的取操作
    public static HashSet<String> playerWish;
    // 该变量用于记录玩家是否即将进行指定数量的取操作
    
    public PIExecutor(PortableInventory plugin) {
        this.plugin = plugin; // Store the plugin in situations where you need it.
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        String playerName = sender.getName();
        if(!PIData.hasConnection(playerName)){
            sender.sendMessage("You are not connected to any stockpile right now!");
            return false;
        }
        Inventory inv = Bukkit.createInventory(null, 45, "Select item to take away");
        PIExecutor.resetPlayerPage(playerName, false);
        if(playerUsed == null){
            playerUsed = new HashSet<String>();
        }
        playerUsed.add(playerName);
        ArrayList<ItemStack> itemShowList = PIData.showStock(playerName, playerPage.get(playerName), 45 - 9);
        for(int i = 0; i < itemShowList.size(); i++){
            inv.setItem(i, itemShowList.get(i));
        }
        // 填充虚拟物品栏
        
        ItemStack leftButton = new ItemStack(YELLOW_STAINED_GLASS_PANE);
        ItemMeta leftButtonMeta = leftButton.getItemMeta();
        leftButtonMeta.setDisplayName("Previous page");
        leftButton.setItemMeta(leftButtonMeta);
        ItemStack rightButton = new ItemStack(YELLOW_STAINED_GLASS_PANE);
        ItemMeta rightButtonMeta = rightButton.getItemMeta();
        rightButtonMeta.setDisplayName("Next page");
        rightButton.setItemMeta(rightButtonMeta);
        // 构建翻页按钮
        
        inv.setItem(45 - 9, leftButton);
        inv.setItem(45 - 1, rightButton);
        Player player = (Player)sender;
        player.openInventory(inv);
        
        return true;
    }
    
    public static void resetPlayerPage(String playerName, boolean forceReset){
        if(playerPage == null){
            playerPage = new HashMap<String, Integer>();
        }
        if(forceReset){
            playerPage.put(playerName, 0);
        }
        else{
            if(!playerPage.containsKey(playerName)){
                playerPage.put(playerName, 0);
            }
        }
    }
}
