import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class Main {
    public static void putindatabase(HashMap<String, ArrayList<worddata>> worddocs, Object o)
    {
        // Create a thread pool with a fixed number of threads
        int numThreads = Runtime.getRuntime().availableProcessors(); // Number of available processors
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);

        Set<String> keys = worddocs.keySet();
        int size=worddocs.size();
        for (String key : keys) {
            System.out.println(size--);
            // Submit each key to be processed by a separate thread
            executor.submit(new DatabaseTask(key, worddocs.get(key), o));
        }

        // Shutdown the executor to prevent new tasks from being submitted
        executor.shutdown();
    }
    public static void main(String[] args) throws IOException {

        Object o=new Object();
        HashMap<String,ArrayList<worddata>> worddocs=new HashMap<>();

        Thread[] threads = new Thread[40];
        for (int i = 0; i < 40; i++) {
            threads[i] = new Indexing(o,worddocs);
            threads[i].start();

        }

        try {
            for (Thread thread : threads) {
                thread.join();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        putindatabase(worddocs,o);
}

}