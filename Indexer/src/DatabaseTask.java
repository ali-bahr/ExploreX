import java.util.ArrayList;

public class DatabaseTask implements Runnable {
    private String key;
    private ArrayList<worddata> data;
    private Object obj;

    public DatabaseTask(String key, ArrayList<worddata> data, Object obj) {
        this.key = key;
        this.data = data;
        this.obj = obj;
    }

    @Override
    public void run() {
        System.out.println(key);
        mongo.connectmongo(key, data, obj);
    }
}