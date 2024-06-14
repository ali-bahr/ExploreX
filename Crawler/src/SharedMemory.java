import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.io.FileWriter;
import java.io.IOException;




public class SharedMemory {
    private ConcurrentLinkedQueue<Pair<String,String>> queue;
    private ConcurrentHashMap<String, Boolean> map;
    private HashMap<String, HashSet<String>> Graph;
    private ConcurrentHashMap<String, Boolean> visited;
    private boolean queueReachMaxSize;


    SharedMemory() {
        queue=new ConcurrentLinkedQueue<>();
        map = new ConcurrentHashMap<>();
        Graph = new HashMap<>();
        visited = new ConcurrentHashMap<>();
        queueReachMaxSize = false;
    }

    public boolean isqueueReachMaxSize() {
        return queueReachMaxSize;
    }

    public void map_add(String element) {
        map.put(element,true) ;
    }

    public boolean map_contains(String element) {
        return map.containsKey(element);
    }

    public boolean map_remove(String element) {
        return map.remove(element) != null;
    }

    public void queue_offer(String child, String parent) {
        queue.offer(new Pair<>(child,parent));

    }

    public boolean queue_contains(String element) {
        return queue.contains(element);
    }

    public Pair<String, String> queue_poll() {
        if(queue.isEmpty())return null;
        return queue.poll();
    }

    public long queue_size() {return queue.size();}


    public boolean visited_contains(String element) {
        return visited.containsKey(element);
    }


    public void visited_add(String e) {
        visited.put(e,true);
    }

    public long visited_size() {
        return visited.size();
    }

    public synchronized void Graph_add(String child , String parent) {
        if(Graph.containsKey(child)) {
            Graph.get(child).add(parent);
        }
        else{
            HashSet<String> temp = new HashSet<>();
            temp.add(parent);
            Graph.put(child,temp);
        }
    }

    public int Graph_size(){
        return Graph.size();
    }

    public void set_Graph(HashMap<String, HashSet<String>> newgraph){
        Graph = newgraph;
    }

    public HashMap<String, HashSet<String>> get_Graph() {return Graph;}


    public void saveState(){
        try {
            FileWriter fileWriter = new FileWriter("Start_Urls.txt");
            fileWriter.close();
            System.out.println("File cleared successfully.");
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
        try {
            FileWriter fileWriter = new FileWriter("Start_Urls.txt");
            while (!queue.isEmpty()) {
                Pair<String,String> element = queue.poll();
                fileWriter.write( element.getfirst()+ " "+ element.getsecond() + "\n");
            }
            fileWriter.close();
            System.out.println("Queue contents saved to file successfully.");
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
        try {
            FileWriter fileWriter = new FileWriter("visited.txt");
            fileWriter.close();
            System.out.println("File cleared successfully.");
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
        try {
            FileWriter fileWriter = new FileWriter("visited.txt");
            for (HashMap.Entry<String, Boolean> entry : visited.entrySet()) {
                String key = entry.getKey();
                fileWriter.write(key + "\n");
            }
            fileWriter.close();
            System.out.println("Queue contents saved to file successfully.");
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("graph.ser"))) {
            oos.writeObject(Graph);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
