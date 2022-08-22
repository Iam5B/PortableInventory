/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package portableinventory;
import java.util.ArrayList;
import java.util.HashSet;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;

/**
 *
 * @author User
 */
public class PISaveListener implements Listener{
    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event){
        String playerName = event.getPlayer().getName();
        if(PISaveExecutor.playerUsed == null){
            // 依然需要防止该静态变量未初始化的情况
            PISaveExecutor.playerUsed = new HashSet<String>();
        }
        if(event.getView().getTitle().equals("Put items to be stored")){
            // 先根据虚拟物品栏的title进行一下筛选
            if(!PISaveExecutor.playerUsed.contains(playerName)){
                // 根据playerUsed标记判断玩家是不是刚刚执行过pisave，同时在下面要消除该标记，以防玩家重命名一个同名箱子并尝试往里面放东西
                return;
            }
            PISaveExecutor.playerUsed.remove(playerName);
            ArrayList<ItemStack> items = new ArrayList<ItemStack>();
            for(ItemStack item : event.getInventory().getContents()){
                if(item != null){
                    items.add(item);
                }
            }
            PIData.saveItems(playerName, items);
            event.getPlayer().sendMessage("You have successfully saved you items!");
        }
    }
}
