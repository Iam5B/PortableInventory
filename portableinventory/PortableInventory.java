/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package portableinventory;
import java.io.IOException;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.command.CommandSender;
import org.bukkit.command.Command;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.PlayerInventory;
/**
 *
 * @author User
 */



public class PortableInventory extends JavaPlugin {
    @Override    
    public void onEnable() {
        // 注册指令的Executor，注册各种监听器
        this.getCommand("picreate").setExecutor(new PICreateExecutor(this));
        this.getCommand("pitest").setExecutor(new PITestExecutor(this));
        this.getCommand("pilist").setExecutor(new PIListExecutor(this));
        this.getCommand("piswitch").setExecutor(new PISwitchExecutor(this));
        this.getCommand("piconnect").setExecutor(new PIConnectExecutor(this));
        this.getCommand("piremove").setExecutor(new PIRemoveExecutor(this));
        this.getCommand("pisave").setExecutor(new PISaveExecutor(this));
        this.getCommand("pitake").setExecutor(new PITakeExecutor(this));
        this.getCommand("pisaveall").setExecutor(new PISaveAllExecutor(this));
        this.getCommand("pidisconnect").setExecutor(new PIDisconnectExecutor(this));
        this.getCommand("pi").setExecutor(new PIExecutor(this));
        Bukkit.getPluginManager().registerEvents(new PISaveListener(), this);
        Bukkit.getPluginManager().registerEvents(new PITakeListener(), this);
        Bukkit.getPluginManager().registerEvents(new PIListener(), this);
        PIData.checkOrInitializeData();
        // 尝试调用一次文件初始化
    }
    @Override
    public void onDisable() {
        getLogger().info("PortableInventory disabled!");
    }
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        return true;
    }
}
