/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package portableinventory;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import org.bukkit.Material;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;


/**
 *
 * @author User
 */
public class StockInfo implements Serializable{
    /* 
    存储仓库信息的复杂结构体
    包含了所有者、密码、物品堆列表，一般来说，相同的物品会存放在一个物品堆中
    该数据结构的核心在于同时设置了面向存储的物品堆列表和面向程序的物品堆列表
    */
    public String owner;
    public String password;
    public ArrayList<ItemStack> deserialized_items;
    public ArrayList<String> serialized_items;
    
    public static String itemStackSerialize(ItemStack itemStack) {
        // https://www.mcbbs.net/thread-1341360-1-1.html
        YamlConfiguration yml = new YamlConfiguration();
        yml.set("item", itemStack);
        return yml.saveToString();
    }
    public static ItemStack itemStackDeserialize(String str) {
        // https://www.mcbbs.net/thread-1341360-1-1.html
        YamlConfiguration yml = new YamlConfiguration();
        ItemStack item;
        try {
            yml.loadFromString(str);
            item = yml.getItemStack("item");
        } catch (InvalidConfigurationException ex) {
            item = new ItemStack(Material.AIR, 1);
        }
        return item;
    }
    
    public StockInfo deserialize(){
        // 读出后准备在程序中使用前则需要将serialized_item转化为deserialized_items，使之变成可操作的物品堆列表
        deserialized_items = new ArrayList<ItemStack>();
        for(String serialized_item : serialized_items){
            deserialized_items.add(itemStackDeserialize(serialized_item));
        }
        return this;
    }
    public StockInfo serialize(){
        // 准备存储前将deserialized_items转化为serialized_items，并将deserialized_items中，使之成为可存储的物品堆列表
        serialized_items = new ArrayList<String>();
        for(ItemStack deserialized_item : deserialized_items){
            serialized_items.add(itemStackSerialize(deserialized_item));
        }
        deserialized_items = null;
        return this;
    }
}
