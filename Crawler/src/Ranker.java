import java.util.HashMap;
import java.util.HashSet;

import com.mongodb.client.*;
import com.mongodb.client.model.Filters;
import org.bson.Document;

import javax.lang.model.type.NullType;
import java.util.ArrayList;
import java.lang.Math;


public class Ranker {
    private static final double BETA = 0.85;
    private double eps;
    private HashMap<String, HashSet<String>> Graph;
    private HashMap<String, Double> pageRanks;
    private int numIter;
    private boolean isPageRankedDone;
    public long time_taken;


    // test TF-IDF only
    public static void main(String[] args) {

        System.out.println("Hallo \n");

        appendFinalScoreForAll();
    }

    public Ranker(double e,HashMap<String, HashSet<String>> g){
        eps = e;
        Graph = g;
        pageRanks = new HashMap<>();
        isPageRankedDone =false;
    }

    public void calcPageRank() {
        long start_time = System.currentTimeMillis();
        numIter = 0;
        HashMap<String, Double> pr = new HashMap<String, Double>();
        for(String s : Graph.keySet()) {
            pr.put(s, 1.0 /(double) Graph.size());
        }

        double diff = Double.MAX_VALUE;
        HashMap<String, Double> nextIter;
        while(diff > eps) {
            nextIter = singleIterationCalcPageRank(pr);
            diff = diff(nextIter, pr);
            pr = nextIter;
            numIter++;
        }
        long end_time = System.currentTimeMillis();
        time_taken = (end_time-start_time)/1000;
        pageRanks = pr;
        isPageRankedDone = true;
        System.out.println(pageRanks);
        mongo.insert_pagerank(pageRanks);
    }

    private HashMap<String, Double> singleIterationCalcPageRank(HashMap<String, Double> pr) {
        HashMap<String, Double> newPageRanks = new HashMap<>();
        double sumOfRanks = 0.0;

        // Contribution from incoming edges
        for (String incomingNode : Graph.keySet()) {
            double newPageRank = (1 - BETA); // Initial value (damping factor)
            if (Graph.get(incomingNode) != null && !Graph.get(incomingNode).isEmpty()) {
                for (String outgoingNode : Graph.get(incomingNode)) {
                    if (Graph.containsKey((outgoingNode)))
                        newPageRank += BETA * (pr.get(outgoingNode) / (double) Graph.get(outgoingNode).size());
                }
            }
            newPageRanks.put(incomingNode, newPageRank);
            sumOfRanks += newPageRank;

        }
        // Normalize PageRank values
        for (String node : newPageRanks.keySet()) {
            newPageRanks.put(node, newPageRanks.get(node) / sumOfRanks);
        }

        return newPageRanks;
    }

    private double diff(HashMap<String, Double> nextIter, HashMap<String, Double> prevIter) {
        double sum = 0;
        for(String s : Graph.keySet()) {
            sum += Math.abs(nextIter.get(s) - prevIter.get(s));
        }
        return(Math.sqrt(sum));
    }

    public HashMap<String, Double> getPageRanks() {return pageRanks;}

    public int numberOfIteration() {return numIter;}

    public boolean get_isPageRankDone() { return isPageRankedDone;}


    /**
     * This function takes a token and calculates the TF-IDF for all the documents
     * in which this word appears.
     * */
    public static void appendFinalScore(String token) {

        MongoCollection<Document> collection = mongo.getWordsCollection();

        // get page ranks (from pagerank collection) to append the final score directly
        HashMap<String, Double> docs_pageRanks = mongo.getPageRanks();

        // get numWords (from docs collection)
        HashMap<String, Integer> docs_numWords = mongo.getNumWords();

        System.out.println("Appending Final score for the token '" + token+"'");
        long countAll = mongo.getNumOfDocs();
        // Normalized TF = term count / number of words in doc
        // IDF = log (#docs in db / DF)
        // TF_IDF = nTF * IDF
        Document filter = new Document("wordID", token);
        Document doc = collection.find(filter).first();
        assert doc != null;
        ArrayList<Document> documents = (ArrayList<Document>) doc.get("documents");
        long DF = documents.size();
        double IDF = Math.log((double) countAll / DF);
        int i = 0;
        for (Document d : documents) {
            int tf = d.getInteger("tf");
            int numWords = docs_numWords.get(d.getString("url"));
            double normTF = (double) tf / numWords;
            double TFIDF = IDF * normTF;

            // get pageRank of this URL
            double pgrnk = -1;
            pgrnk = docs_pageRanks.get(d.get("url"));
            if (pgrnk == -1) {
                System.out.println("URL NOT FOUND IN MAP");
                return;
            }

            double final_score = TFIDF * pgrnk * 10000;

            d.append("final_score", final_score);
            Document update = new Document();
            update.append("$set", new Document("documents." + i, d));
            collection.updateOne(filter, update);

            i++;
        }
    }

    public static void appendFinalScoreForAll() {

        MongoCollection<Document> collection = mongo.getWordsCollection();

        // get page ranks (from pagerank collection) to append the final score directly
        HashMap<String, Double> docs_pageRanks = mongo.getPageRanks();

        // get numWords (from docs collection)
        HashMap<String, Integer> docs_numWords = mongo.getNumWords();
        long countAll = mongo.getNumOfDocs();
        // Normalized TF = term count / number of words in doc
        // IDF = log (#docs in db / DF)
        // TF_IDF = nTF * IDF

        // Find all documents (empty filter)
        FindIterable<Document> collection_documents = collection.find();

        // Use a cursor to iterate through documents
        MongoCursor<Document> cursor = collection_documents.iterator();
        while (cursor.hasNext()) {
            Document document = cursor.next();
            String wordId = document.getString("wordID");
            System.out.println("Appending Final score for the token '" + document.get("wordID") + "'");
            ArrayList<Document> documents = (ArrayList<Document>) document.get("documents");
            long DF = documents.size();
            double IDF = Math.log((double) countAll / DF);
            int i = 0;
            for (Document d : documents) {
                int tf = d.getInteger("tf");
                // identify webpages by objectid
                String currObjectId_str = d.getString("objectid");
                if (docs_numWords.get(currObjectId_str) == null) {
                    continue;
                }
                int numWords = docs_numWords.get(currObjectId_str);
                double normTF = (double) tf / numWords;
                double TFIDF = IDF * normTF;

                ArrayList<String> metadata  = (ArrayList<String>) d.get("metadata");
                if (metadata.contains("title"))
                    TFIDF *= 1.5;

                // get pageRank of this URL
                double pgrnk = -1;
                pgrnk = docs_pageRanks.get(d.get("url"));
                if (pgrnk == -1) {
                    System.out.println("URL NOT FOUND IN MAP");
                    return;
                }

                double final_score = TFIDF * pgrnk * 10000;

                d.append("final_score", final_score);
                System.out.println(d.get("url") +"\t" + final_score);
                i++;
            }
            // uncomment this when you want the update to happen
            collection.updateOne(Filters.eq("wordID", wordId), new Document("$set", new Document("documents", documents)));
        }

        cursor.close();
    }

}

