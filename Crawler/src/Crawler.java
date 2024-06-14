import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;



import com.github.itechbear.robotstxt.RobotsMatcher;

import javax.swing.*;



public class Crawler implements Runnable{

    private int maxsize;
    private final int ID;
    private SharedMemory memory;


    public Crawler(int id, SharedMemory s_m, int max_s) {
        ID=id;
        maxsize=max_s;
        memory=s_m;
    }


    @Override
    public void run() {
        start_crawl();
    }

    private void start_crawl() {
        while (true) {
            Pair<String,String> pair = memory.queue_poll();
            if (pair == null) {
                if (memory.visited_size() >= maxsize) {
                    return;
                }
            } else {
                int status=allowedRobot(pair.getfirst());
                if (status==1)
                {
                    crawl(pair);
                }
                if (memory.visited_size() >= maxsize) {
                    return;
                }
            }
        }
    }

    private void crawl(Pair<String,String> url) {
        Document doc=request(url);
        if(doc != null) {
            for (Element link : doc.select("a[href]")) {
                String nextlink = link.absUrl("href");

                URI originalURI = null;
                URI normalizedURI = null;

                // now check if there exists a malformed URL
                try {
                    originalURI = new URI(nextlink);
                    URL  check = originalURI.toURL(); // THIS LINE WILL THROW EXCEPTION IF URL IS INVALID

                    normalizedURI = new URI(originalURI.getScheme(), originalURI.getAuthority(), originalURI.getPath(), null, null);
                } catch (URISyntaxException | MalformedURLException e) {}

                if(normalizedURI != null) {
                    String norm_url = normalizedURI.toString();
                    if(memory.visited_contains(norm_url)){
                        memory.Graph_add(norm_url,url.getfirst());
                    }
                    if (!memory.map_contains(norm_url)) {
                        memory.map_add(norm_url);
                        memory.queue_offer(norm_url,url.getfirst());
                    }
                }
            }
        }
    }

    private Document request(Pair<String,String> url) {
        try{
            Connection con = Jsoup.connect(url.getfirst());
            Document doc = con.get();
            if(con.response().statusCode() == 200)
            {
                mongo.insert_crawler(url.getfirst(),(String) doc.outerHtml());
                memory.Graph_add(url.getfirst(),url.getsecond());
                memory.visited_add(url.getfirst());
                memory.map_add(url.getfirst());
                System.out.println("thread "+ ID + ": added Link: " + url.getfirst());
                System.out.println(doc.title());
                System.out.println(memory.visited_size());
                return doc;
            }
            throw new IOException();
        }
        catch (IOException e)
        {
            memory.queue_offer(url.getfirst(),url.getsecond());
            return null;
        }
    }

    private static String getRobotUrl(String site) {
        URL a;
        try {
            // get the address of robots.txt
            a = new URI(site).toURL();
            String s = a.getProtocol() + "://" + a.getHost().toString() + "/robots.txt";
            return s;
        } catch (MalformedURLException | URISyntaxException e) {
            e.printStackTrace();
            return "";
        }
    }

    private static String fetchRobotsTxtOfSite(String str) {
        String roboturl = getRobotUrl(str);
        String data = "";
        try {
            Connection con = Jsoup.connect(roboturl);
            Document doc = con.get();
            if (con.response().statusCode() == 200) {
                data = doc.wholeText();
            }
        } catch (IOException e) {
            System.out.println("cant connect: " + roboturl);
            data="cant connect";
        }
        return data;
    }

    private static int allowedRobot(String website) {
        RobotsMatcher matcher = new RobotsMatcher();
        String robotstxt = fetchRobotsTxtOfSite(website);
        if(robotstxt.equals("cant connect")) return 0;
        boolean a = matcher.OneAgentAllowedByRobots(robotstxt, "*", website);

        return (a)? 1:-1;
    }


}

