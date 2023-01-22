/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package portableinventory;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import org.bukkit.Material;
import static org.bukkit.Material.YELLOW_STAINED_GLASS_PANE;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import static org.bukkit.event.inventory.ClickType.LEFT;
import static org.bukkit.event.inventory.ClickType.RIGHT;
import static org.bukkit.event.inventory.ClickType.SHIFT_LEFT;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
/**
 *
 * @author User
 */
public class PIListener implements Listener{
    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event){
        String playerName = event.getPlayer().getName();
        if(event.getView().getTitle().equals("Select item to take away")){
            if(PIExecutor.playerUsed == null){
                PIExecutor.playerUsed = new HashSet<String>();
            }
            if(!PIExecutor.playerUsed.contains(playerName)){
                return;
            }
            PIExecutor.playerUsed.remove(playerName);
        }
    }
    
    @EventHandler
    public void onInputNum(AsyncPlayerChatEvent event){
        // 在玩家点击物品并输入数字后取物品给玩家
        String playerName = event.getPlayer().getName();
        if(PIExecutor.playerWish == null){
            PIExecutor.playerWish = new HashSet<String>();
        }
        if(PIExecutor.playerWish.contains(playerName)){
            event.setCancelled(true);
            PIExecutor.playerWish.remove(playerName);
            String input = event.getMessage();
            int numToTake = 0;
            try {
                numToTake = Integer.parseInt(input);
            } catch(NumberFormatException e) {
                event.getPlayer().sendMessage("Incorrect item amount!");
                return;
            }
            if(numToTake < 0){
                numToTake = 0;
            }
            ItemStack itemToTake = PIData.takeItem(playerName, PIExecutor.playerWishIndex.get(playerName),numToTake);
            HashMap<Integer, ItemStack> itemRemains = event.getPlayer().getInventory().addItem(itemToTake);
            // 将物品放入玩家背包 并获取未放进去的物品
            
            if(itemRemains.containsKey(0)){
                // 若存在没放进玩家背包的内容，存回仓库
                ArrayList<ItemStack> itemToSave = new ArrayList<ItemStack>();
                itemToSave.add(itemRemains.get(0));
                PIData.saveItems(playerName, itemToSave);
            }
        }
    }
    
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event){
        if(PIExecutor.playerUsed == null){
            PIExecutor.playerUsed = new HashSet<String>();
        }
        String playerName = event.getWhoClicked().getName();
        if(event.getView().getTitle().equals("Select item to take away")){
            if(!PIExecutor.playerUsed.contains(playerName)){
                // 注意这个playerUsed并不应该立刻消除，因为翻页、无效操作下，playerUsed依然应该保持
                return;
            }
            event.setCancelled(true);
            if(event.getRawSlot() == 45 - 9 | event.getRawSlot() == 45 - 1){
                // 翻页按钮
                int currentPage = PIExecutor.playerPage.get(playerName);
                int nextPage;
                if(event.getRawSlot() == 45 - 9){
                    nextPage = currentPage - 1;
                }
                else{
                    nextPage = currentPage + 1;
                }
                if(nextPage < 0){
                    nextPage = 0;
                }
                Inventory inv = event.getInventory();
                inv.clear();
                PIExecutor.playerPage.put(playerName, nextPage);
                ArrayList<ItemStack> itemShowList = PIData.showStock(playerName, PIExecutor.playerPage.get(playerName), 45 - 9);
                for(int i = 0; i < itemShowList.size(); i++){
                        inv.setItem(i, itemShowList.get(i));
                }
                ItemStack leftButton = new ItemStack(YELLOW_STAINED_GLASS_PANE);
                ItemMeta leftButtonMeta = leftButton.getItemMeta();
                leftButtonMeta.setDisplayName("Previous page");
                leftButton.setItemMeta(leftButtonMeta);
                ItemStack rightButton = new ItemStack(YELLOW_STAINED_GLASS_PANE);
                ItemMeta rightButtonMeta = rightButton.getItemMeta();
                rightButtonMeta.setDisplayName("Next page");
                rightButton.setItemMeta(rightButtonMeta);

                inv.setItem(45 - 9, leftButton);
                inv.setItem(45 - 1, rightButton);
            }
            else if(event.getRawSlot() >= 45 & event.getRawSlot() <= 80){
                int inventoryIndex = event.getRawSlot() - 45;
                if(inventoryIndex >= 0 & inventoryIndex <= 26){
                    inventoryIndex += 9;
                }
                else{
                    inventoryIndex -= 27;
                }
                if(event.getClick() == LEFT | event.getClick() == SHIFT_LEFT){
                    ItemStack itemToSave = event.getWhoClicked().getInventory().getItem(inventoryIndex);
                    if(itemToSave == null){
                        return;
                    }
                    else{
                        int curItemNum = itemToSave.getAmount();
                        if(event.getClick() == LEFT){
                            itemToSave.setAmount(1);
                            ArrayList<ItemStack> items = new ArrayList<ItemStack>();
                            items.add(itemToSave);
                            PIData.saveItems(playerName, items);
                            itemToSave.setAmount(curItemNum - 1);
                            event.getWhoClicked().getInventory().setItem(inventoryIndex, itemToSave);
                        }
                        else if(event.getClick() == SHIFT_LEFT){
                            ArrayList<ItemStack> items = new ArrayList<ItemStack>();
                            items.add(itemToSave);
                            PIData.saveItems(playerName, items);
                            event.getWhoClicked().getInventory().setItem(inventoryIndex, null);
                        }
                        Inventory inv = event.getInventory();
                        inv.clear();
                        ArrayList<ItemStack> itemShowList = PIData.showStock(playerName,PIExecutor.playerPage.get(playerName), 45 - 9);
                        for(int i = 0; i < itemShowList.size(); i++){
                            inv.setItem(i, itemShowList.get(i));
                        }
                        ItemStack leftButton = new ItemStack(YELLOW_STAINED_GLASS_PANE);
                        ItemMeta leftButtonMeta = leftButton.getItemMeta();
                        leftButtonMeta.setDisplayName("Previous page");
                        leftButton.setItemMeta(leftButtonMeta);
                        ItemStack rightButton = new ItemStack(YELLOW_STAINED_GLASS_PANE);
                        ItemMeta rightButtonMeta = rightButton.getItemMeta();
                        rightButtonMeta.setDisplayName("Next page");
                        rightButton.setItemMeta(rightButtonMeta);

                        inv.setItem(45 - 9, leftButton);
                        inv.setItem(45 - 1, rightButton);
                    }
                }
                else if(event.getClick() == RIGHT){
                    // TODO 准备存入若干物品的case，但是我懒得做了
//                    if(PIExecutor.playerUsed == null){
//                        PIExecutor.playerUsed = new HashSet<String>();
//                    }
//                    if(!PIExecutor.playerUsed.contains(playerName)){
//                        return;
//                    }
//                    PIExecutor.playerUsed.remove(playerName);
//                    event.getWhoClicked().closeInventory();
                }
                
            }
            else if(event.getRawSlot() >= 0 & event.getRawSlot() < 45 - 9){
                // 点击了有效的物品区域
                if(PIExecutor.playerWishIndex == null){
                    PIExecutor.playerWishIndex = new HashMap<String, Integer>();
                    PIExecutor.playerWish = new HashSet<String>();
                }
                int itemIndex = PIExecutor.playerPage.get(playerName) * (45 - 9) + event.getRawSlot();
                PIExecutor.playerWishIndex.put(playerName, itemIndex);
                int itemNum = PIData.queryItemNum(playerName, itemIndex);
                if(itemNum <= 0){
                    event.getWhoClicked().sendMessage("You can't take air!");
                    PIExecutor.playerUsed.remove(playerName);
                    event.getWhoClicked().closeInventory();
                }
                if(event.getClick() == LEFT){
                    // 快速取一件物品的case
                    ItemStack itemToTake = PIData.takeItem(playerName, PIExecutor.playerWishIndex.get(playerName), 1);
                    HashMap<Integer, ItemStack> itemRemains = event.getWhoClicked().getInventory().addItem(itemToTake);
                    if(itemRemains.containsKey(0)){
                        ArrayList<ItemStack> itemToSave = new ArrayList<ItemStack>();
                        itemToSave.add(itemRemains.get(0));
                        PIData.saveItems(playerName, itemToSave);
                    }
                    Inventory inv = event.getInventory();
                    inv.clear();
                    ArrayList<ItemStack> itemShowList = PIData.showStock(playerName,PIExecutor.playerPage.get(playerName), 45 - 9);
                    for(int i = 0; i < itemShowList.size(); i++){
                        inv.setItem(i, itemShowList.get(i));
                    }
                    ItemStack leftButton = new ItemStack(YELLOW_STAINED_GLASS_PANE);
                    ItemMeta leftButtonMeta = leftButton.getItemMeta();
                    leftButtonMeta.setDisplayName("Previous page");
                    leftButton.setItemMeta(leftButtonMeta);
                    ItemStack rightButton = new ItemStack(YELLOW_STAINED_GLASS_PANE);
                    ItemMeta rightButtonMeta = rightButton.getItemMeta();
                    rightButtonMeta.setDisplayName("Next page");
                    rightButton.setItemMeta(rightButtonMeta);

                    inv.setItem(45 - 9, leftButton);
                    inv.setItem(45 - 1, rightButton);
                }
                else if(event.getClick() == RIGHT){
                    // 准备取出若干物品的case，记录物品index（在前面完成了），关闭GUI，准备监听输入的数字
                    Material itemType = PIData.queryItemType(playerName, itemIndex);
                    event.getWhoClicked().sendMessage("You have " + itemNum + " " + itemType.name() + ". Enter the num you want:");
                    PIExecutor.playerWish.add(playerName);
                    PIExecutor.playerUsed.remove(playerName);
                    event.getWhoClicked().closeInventory();
                }
                else if(event.getClick() == SHIFT_LEFT){
                    // 快速取64的case
                    ItemStack itemToTake = PIData.takeItem(playerName, PIExecutor.playerWishIndex.get(playerName), 64);
                    HashMap<Integer, ItemStack> itemRemains = event.getWhoClicked().getInventory().addItem(itemToTake);
                    if(itemRemains.containsKey(0)){
                        ArrayList<ItemStack> itemToSave = new ArrayList<ItemStack>();
                        itemToSave.add(itemRemains.get(0));
                        PIData.saveItems(playerName, itemToSave);
                    }
                    Inventory inv = event.getInventory();
                    inv.clear();
                    ArrayList<ItemStack> itemShowList = PIData.showStock(playerName, PIExecutor.playerPage.get(playerName), 45 - 9);
                    for(int i = 0; i < itemShowList.size(); i++){
                        inv.setItem(i, itemShowList.get(i));
                    }
                    ItemStack leftButton = new ItemStack(YELLOW_STAINED_GLASS_PANE);
                    ItemMeta leftButtonMeta = leftButton.getItemMeta();
                    leftButtonMeta.setDisplayName("Previous page");
                    leftButton.setItemMeta(leftButtonMeta);
                    ItemStack rightButton = new ItemStack(YELLOW_STAINED_GLASS_PANE);
                    ItemMeta rightButtonMeta = rightButton.getItemMeta();
                    rightButtonMeta.setDisplayName("Next page");
                    rightButton.setItemMeta(rightButtonMeta);

                    inv.setItem(45 - 9, leftButton);
                    inv.setItem(45 - 1, rightButton);
                }
            }
        }
    }
}
