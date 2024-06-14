import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;


public class CrawlerSystem {
    private final int number_threads;
    private SharedMemory memory;
    ArrayList<Pair<String,String>> Start_urls;
    ArrayList<String> visited_urls;
    HashMap<String, HashSet<String>> newGraph;
    ArrayList<Thread> threads=new ArrayList<>();
    private long time_taken;
    private int maxsize;

    CrawlerSystem(int s, SharedMemory s_m, ArrayList<Pair<String,String>> s_u, ArrayList<String> v, HashMap<String, HashSet<String>> Graph , int max) {
        number_threads = s;
        memory = s_m;
        Start_urls = s_u;
        maxsize = max;
        visited_urls = v;
        newGraph = Graph;
    }

    public void Start() {
        // Start_urls are already normalized
        for(Pair<String,String> s:Start_urls) {
            memory.queue_offer(s.getfirst(),s.getsecond());
            memory.map_add(s.getfirst());
        }

        for(String s : visited_urls) {
            memory.visited_add(s);
            memory.map_add(s);
        }

        if(newGraph!=null)memory.set_Graph(newGraph);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            memory.saveState();
            System.out.println("State Saved Successfully");
            Runtime.getRuntime().halt(0);
        }));

        for(int i=0;i<number_threads;i++)
            threads.add(new Thread(new Crawler(i,memory,maxsize)));

        for(Thread t:threads)t.start();

        long start_time = System.currentTimeMillis();

        for(Thread t:threads) {
            try {
                t.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        long end_time = System.currentTimeMillis();

        time_taken = (end_time-start_time)/1000;
    }

    public long get_time() {
        return time_taken;
    }


}
