package portableinventory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;

public class PIDataUtil {
    /*
    存放一些在PIData中用到的工具函数，但这些工具函数并不会直接在各个Executor中被用到，不是在重写数据层面实现时必须重写的方法，因此放在这里
    这么说的话应该和StockInfo放在一起形成个文件夹
    */
    public static void saveObject(Object o, File f){
        // 存对象工具函数，将对象存到指定文件中（该文件是否需要提前建立好并没有确认），此外File可以通过new File("")快速创建
        try{
            ObjectOutputStream objectOutputStream = 
            new ObjectOutputStream(new FileOutputStream(f));
            objectOutputStream.writeObject(o);
            objectOutputStream.close();
        } catch (IOException e) {e.printStackTrace();}
    }
    public static Object loadObject(File f){
        // 读取对象的工具函数
        Object ret = null;
        try{
            ObjectInputStream objectInputStream = new ObjectInputStream(new FileInputStream(f));
            ret = objectInputStream.readObject();
            objectInputStream.close();
        } catch (IOException | ClassNotFoundException e) {e.printStackTrace();}
        return ret;
    }
    
}




