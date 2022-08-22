package portableinventory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import org.bukkit.Material;
import static org.bukkit.Material.AIR;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class PIData {
    // 将来自于各个Executor的抽象数据操作实现，若只是要重新实现数据层面的实现而不变动交互流程，则重写该类下的各个方法即可
    
    private static File stockData = new File("./plugins/PortableInventory/stockData");
    private static File dataFolder = new File("./plugins/PortableInventory");
    private static File ownRelationData = new File("./plugins/PortableInventory/ownRelationData");
    private static File useRelationData = new File("./plugins/PortableInventory/useRelationData");
    private static File onRelationData = new File("./plugins/PortableInventory/onRelationData");
    // 方便打开各种数据文件，依次为仓库文件、所有数据的文件夹、仓库拥有关系文件、仓库访问权文件、仓库使用关系文件
    // stockData的抽象的数据结构大致是{"stockName":{owner:"userName", password:"password": items:[ItemStack1, ItemStack2, ...]}}
    
    public static void checkOrInitializeData(){
        // 确认数据文件、数据文件夹是否存在，若不存在，则创建并初始化
        if(!dataFolder.exists()){
            dataFolder.mkdir();
        }
        if(!stockData.exists()){
            try{
                stockData.createNewFile();
                HashMap<String, StockInfo> stock = new HashMap<String, StockInfo>();
                // 初始化文件的时候同时需要放入一个空的对象
                PIDataUtil.saveObject(stock, stockData);
            } catch (IOException e){}
        }
        if(!ownRelationData.exists()){
            try{
                ownRelationData.createNewFile();
                HashMap<String, ArrayList<String>> own = new HashMap<String, ArrayList<String>>();
                PIDataUtil.saveObject(own, ownRelationData);
            } catch (IOException e){}
        }
        if(!useRelationData.exists()){
            try{
                useRelationData.createNewFile();
                HashMap<String, ArrayList<String>> use = new HashMap<String, ArrayList<String>>();
                PIDataUtil.saveObject(use, useRelationData);
            } catch (IOException e){}
        }
        if(!onRelationData.exists()){
            try{
                onRelationData.createNewFile();
                HashMap<String, String> on = new HashMap<String, String>();
                PIDataUtil.saveObject(on, onRelationData);
            } catch (IOException e){}
        }
    }
    public static boolean checkStockExistence(String stockName){
        HashMap<String, StockInfo> stock = (HashMap<String, StockInfo>)PIDataUtil.loadObject(stockData);
        return stock.containsKey(stockName);
    }
    public static synchronized void createStock(String playerName, String stockName, String password){
        /*
        创建仓库操作，需要提供玩家名、仓库名、密码
        
        注意下涉及到写入操作的方法应该都需要加上synchronized锁住本类，使得同一时间内，来自本数据实现类的写方法只有一个在执行，这能避免较为严重的覆盖逻辑错误
        
        例如，userA和userB同时查看仓库stockA，userA抽走了物品堆itemA，userB抽走了物品堆itemB，假设两人取出物品的方法恰巧并发执行
        并且都在对方写入之前发生了读取，则事实上userA和userB会交替写入stockA - itemA和stockA - itemB这两种状态，这两种状态无论哪一种被写入，都是有错误的
        
        加上类锁可以强制一方等待另一方的写入方法彻底结束后，再读取状态正确的数据进行写入
        
        加上类锁后可能会是这样的，userA和userB同时查看了相同状态的仓库stockA，userA加了类锁抽走了物品堆itemA，userB在认为仓库无变化的条件下抽物品堆itemB，事实上userB操作后
        仓库状态是stockA - itemA - itemB，但是userB会认为仓库状态应该是stockA - itemB，这种偏差会让userB感到困惑，但是数据本身并没有出现逻辑错误
        
        */
        HashMap<String, StockInfo> stock = (HashMap<String, StockInfo>)PIDataUtil.loadObject(stockData);
        StockInfo newStock = new StockInfo();
        newStock.owner = playerName;
        newStock.password = password;
        newStock.deserialized_items = new ArrayList<ItemStack>();
        newStock.serialize();
        stock.put(stockName, newStock);
        PIDataUtil.saveObject(stock, stockData);
        HashMap<String, ArrayList<String>> own = (HashMap<String, ArrayList<String>>)PIDataUtil.loadObject(ownRelationData);
        ArrayList<String> ownList;
        if(!own.containsKey(playerName)){
            ownList = new ArrayList<String>();
            own.put(playerName, ownList);
        }
        else{
            ownList = own.get(playerName);
        }
        ownList.add(stockName);
        PIDataUtil.saveObject(own, ownRelationData);
    }
    public static ArrayList<String> listStocks(String playerName){
        /*
        列出玩家可访问的仓库，包括有所有权的以及有访问权的，将仓库输出信息存成列表返回上一层
        这样设计有坏处，无法将所有交互文字集中在Executor/Listener层，应该用数据结构来传导
        */
        ArrayList<String> stockMessageList = new ArrayList<String>();
        HashMap<String, StockInfo> stock = (HashMap<String, StockInfo>)PIDataUtil.loadObject(stockData);
        HashMap<String, ArrayList<String>> own = (HashMap<String, ArrayList<String>>)PIDataUtil.loadObject(ownRelationData);
        HashMap<String, ArrayList<String>> use = (HashMap<String, ArrayList<String>>)PIDataUtil.loadObject(useRelationData);
        HashMap<String, String> on = (HashMap<String, String>)PIDataUtil.loadObject(onRelationData);
        
        String onStockName = on.get(playerName);
        if(onStockName == null){
            onStockName = "";
        }
        
        stockMessageList.add("You have access to the following stocks:");
        
        ArrayList<String> ownStockNameList = own.get(playerName);
        if(ownStockNameList != null){
            for(String ownStockName : ownStockNameList){
                StockInfo ownStockInfo = stock.get(ownStockName);
                String password = ownStockInfo.password;
                if(ownStockName.equals(onStockName)){
                    password = password + " <<<  You are using this";
                }
                stockMessageList.add("  name: " + ownStockName + " password: " + password);
            }
        }
        ArrayList<String> useStockNameList = use.get(playerName);
        if(useStockNameList != null){
            for(String useStockName : useStockNameList){
                if(useStockName.equals(onStockName)){
                    useStockName = useStockName + " <<<  You are using this";
                }
                stockMessageList.add("  name: " + useStockName);
            }
        }
        return stockMessageList;
    }
    public static boolean checkAccess(String playerName, String stockName){
        /*
        检查玩家访问仓库的权限，即玩家的own和use列表中是否包含了仓库名
        注意own和use的数据结构都是{"playerName":["stockName", ...], }
        */
        HashMap<String, ArrayList<String>> own = (HashMap<String, ArrayList<String>>)PIDataUtil.loadObject(ownRelationData);
        HashMap<String, ArrayList<String>> use = (HashMap<String, ArrayList<String>>)PIDataUtil.loadObject(useRelationData);
        ArrayList<String> ownStockNameList = own.get(playerName);
        ArrayList<String> useStockNameList = use.get(playerName);
        if(ownStockNameList != null){
            for(String ownStockName : ownStockNameList){
                if(stockName.equals(ownStockName)){
                    return true;
                }
            }
        }
        if(useStockNameList != null){
            for(String useStockName : useStockNameList){
                if(stockName.equals(useStockName)){
                    return true;
                }
            }
        }
        return false;
    }
    public static synchronized void switchStock(String playerName, String stockName){
        /*
        切换玩家正在使用的仓库
        on数据的结构是{"userName":"stockName"}
        */
        HashMap<String, String> on = (HashMap<String, String>)PIDataUtil.loadObject(onRelationData);
        on.put(playerName, stockName);
        PIDataUtil.saveObject(on, onRelationData);
    }
    public static boolean checkPassword(String stockName, String password){
        // 核对仓库密码与输入密码
        HashMap<String, StockInfo> stock = (HashMap<String, StockInfo>)PIDataUtil.loadObject(stockData);
        StockInfo info = stock.get(stockName);
        return info.password.equals(password);
    }
    public static synchronized void connectStock(String playerName, String stockName){
        // 修改use对象的数据从而建立玩家对仓库的访问权
        HashMap<String, ArrayList<String>> use = (HashMap<String, ArrayList<String>>)PIDataUtil.loadObject(useRelationData);
        ArrayList<String> useStockNameList;
        if(!use.containsKey(playerName)){
            // 玩家名从未在use关系数据中存在过的情况，此类情况需要初始化玩家具有访问权限的仓库名列表
            useStockNameList = new ArrayList<String>();
        }
        else{
            useStockNameList = use.get(playerName);
        }
        useStockNameList.add(stockName);
        use.put(playerName, useStockNameList);
        PIDataUtil.saveObject(use, useRelationData);
    }
    public static boolean checkOwnership(String playerName, String stockName){
        // 检查玩家是否是一个仓库的所有者
        HashMap<String, ArrayList<String>> own = (HashMap<String, ArrayList<String>>)PIDataUtil.loadObject(ownRelationData);
        if(!own.containsKey(playerName)){
            // 关于该玩家的所有权的列表尚未被初始化过的情况
            return false;
        }
        ArrayList<String> ownStockNameList = own.get(playerName);
        if(ownStockNameList != null){
            for(String ownStockName : ownStockNameList){
                if(stockName.equals(ownStockName)){
                    return true;
                }
            }
        }
        return false;
    }
    public static synchronized void removeStock(String stockName){
        // 高复杂度的删除仓库操作
        HashMap<String, StockInfo> stock = (HashMap<String, StockInfo>)PIDataUtil.loadObject(stockData);
        HashMap<String, ArrayList<String>> own = (HashMap<String, ArrayList<String>>)PIDataUtil.loadObject(ownRelationData);
        HashMap<String, ArrayList<String>> use = (HashMap<String, ArrayList<String>>)PIDataUtil.loadObject(useRelationData);
        HashMap<String, String> on = (HashMap<String, String>)PIDataUtil.loadObject(onRelationData);
        stock.remove(stockName);
        
        // 在下面删除失效的关系
        // 由于仓库的标识并不是使用的唯一ID，所以下面操作是有必要的
        for(String owner : own.keySet()){
            ArrayList<String> ownStockNameList = own.get(owner);
            int listLength = ownStockNameList.size();
            for(int i = 0; i < listLength; i++){
                if(ownStockNameList.get(i).equals(stockName)){
                    ownStockNameList.remove(i);
                    break;
                }
            }
        }
        for(String user : use.keySet()){
            ArrayList<String> useStockNameList = use.get(user);
            int listLength = useStockNameList.size();
            for(int i = 0; i < listLength; i++){
                if(useStockNameList.get(i).equals(stockName)){
                    useStockNameList.remove(i);
                    break;
                }
            }
        }
        for(String player : on.keySet()){
            if(on.get(player).equals(stockName)){
                on.remove(player);
                break;
            }
        }
        PIDataUtil.saveObject(stock, stockData);
        PIDataUtil.saveObject(own, ownRelationData);
        PIDataUtil.saveObject(use, useRelationData);
        PIDataUtil.saveObject(on, onRelationData);
    }
    public static boolean hasConnection(String playerName){
        // 确认玩家是否正在使用某个仓库，这是玩家和仓库交互的前提
        HashMap<String, String> on = (HashMap<String, String>)PIDataUtil.loadObject(onRelationData);
        return on.containsKey(playerName);
    }
    public static synchronized void saveItems(String playerName, ArrayList<ItemStack> items){
        // 存放物品逻辑 主要是将传入的ItemStack列表与仓库所对应的ItemStack列表合并
        HashMap<String, String> on = (HashMap<String, String>)PIDataUtil.loadObject(onRelationData);
        HashMap<String, StockInfo> stock = (HashMap<String, StockInfo>)PIDataUtil.loadObject(stockData);
        String stockName = on.get(playerName);
        StockInfo saveStock = stock.get(stockName);
        saveStock.deserialize();
        // 读取后解序列化
        
        for(ItemStack item : items){
            int found = 0;
            for(ItemStack stockItem : saveStock.deserialized_items){
            // 注意内部循环最好是这个长度可能会变化的列表
                if(stockItem.isSimilar(item)){
                    // 重复物品堆叠即可
                    stockItem.setAmount(stockItem.getAmount()+item.getAmount());
                    found = 1;
                }
                if(found == 1){
                    break;
                }
            }
            if(found == 1){
                continue;
            }
            else{
                saveStock.deserialized_items.add(item);
                // 并非存在于仓库中的物品，追加到最后
            }
        }
        
        saveStock.serialize();
        // 存储前序列化
        PIDataUtil.saveObject(stock, stockData);
    }
    public static ArrayList<ItemStack> showStock(String playerName, int page, int itemsPerPage){
        // 根据页数 每页展示物品数 抽取出ItemStack列表供展示给玩家用
        ArrayList<ItemStack> ItemShowList = new ArrayList<ItemStack>();
        HashMap<String, String> on = (HashMap<String, String>)PIDataUtil.loadObject(onRelationData);
        HashMap<String, StockInfo> stock = (HashMap<String, StockInfo>)PIDataUtil.loadObject(stockData);
        String stockName = on.get(playerName);
        ArrayList<ItemStack> items = stock.get(stockName).deserialize().deserialized_items;
        for(int i = page * itemsPerPage; i < items.size() & i < (page + 1) * itemsPerPage; i++){
            // 该循环需要兼容页数过大的情况
            ItemShowList.add(items.get(i));
        }
        return ItemShowList;
    }
    
    public static synchronized int queryItemNum(String playerName, int itemIndex){
        // 根据物品在仓库中的下标检查某仓库物品的实际数量，用于提示用户正在获取的物品的库存
        HashMap<String, String> on = (HashMap<String, String>)PIDataUtil.loadObject(onRelationData);
        HashMap<String, StockInfo> stock = (HashMap<String, StockInfo>)PIDataUtil.loadObject(stockData);
        String stockName = on.get(playerName);
        ArrayList<ItemStack> items = stock.get(stockName).deserialize().deserialized_items;
        if(itemIndex >= items.size()){
            // 预防并发导致的非法操作
            return 0;
        }
        else{
            return items.get(itemIndex).getAmount();
        }
    }
    
    public static Material queryItemType(String playerName, int itemIndex){
        // 根据物品在仓库中的下标获取物品名，用于提示用户正在获取的物品类型
        HashMap<String, String> on = (HashMap<String, String>)PIDataUtil.loadObject(onRelationData);
        HashMap<String, StockInfo> stock = (HashMap<String, StockInfo>)PIDataUtil.loadObject(stockData);
        String stockName = on.get(playerName);
        ArrayList<ItemStack> items = stock.get(stockName).deserialize().deserialized_items;
        if(itemIndex >= items.size()){
            // 预防非法操作
            return AIR;
        }
        else{
            return items.get(itemIndex).getType();
        }
    }
    public static synchronized ItemStack takeItem(String playerName, int itemIndex, int numToTake){
        HashMap<String, String> on = (HashMap<String, String>)PIDataUtil.loadObject(onRelationData);
        HashMap<String, StockInfo> stock = (HashMap<String, StockInfo>)PIDataUtil.loadObject(stockData);
        String stockName = on.get(playerName);
        ArrayList<ItemStack> items = stock.get(stockName).deserialize().deserialized_items;
        if(itemIndex >= items.size()){
            return new ItemStack(AIR);
        }
        if(numToTake >= items.get(itemIndex).getAmount()){
            // 申请获取的数量过多，则取最大可获取的数量，并从仓库物品列表中删除对应项
            ItemStack itemToTake = items.get(itemIndex);
            items.remove(itemIndex);
            stock.get(stockName).serialize();
            PIDataUtil.saveObject(stock, stockData);
            return itemToTake;
        }
        else{
            // 申请数量合适，则修改物品数目
            ItemStack itemToTake = items.get(itemIndex).clone();
            itemToTake.setAmount(numToTake);
            ItemStack remainItem = items.get(itemIndex);
            remainItem.setAmount(remainItem.getAmount() - numToTake);
            stock.get(stockName).serialize();
            PIDataUtil.saveObject(stock, stockData);
            return itemToTake;
        }
        
    }
    public static synchronized void disconnect(String playerName, String stockName){
        HashMap<String, String> on = (HashMap<String, String>)PIDataUtil.loadObject(onRelationData);
        HashMap<String, ArrayList<String>> use = (HashMap<String, ArrayList<String>>)PIDataUtil.loadObject(useRelationData);
        if(on.containsKey(playerName)){
            if(on.get(playerName).equals(stockName)){
                on.remove(playerName);
            }
        }
        if(use.containsKey(playerName)){
            ArrayList<String> stockNameList = use.get(playerName);
            for(int i = 0; i < stockNameList.size(); i++){
                if(stockNameList.get(i).equals(stockName)){
                    use.get(playerName).remove(i);
                    break;
                }
            }
        }
        PIDataUtil.saveObject(on, onRelationData);
        PIDataUtil.saveObject(use, useRelationData);
    }
}