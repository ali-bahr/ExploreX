import com.mongodb.MongoException;
import com.mongodb.client.*;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.HashMap;

public class mongo {
    private static MongoClient mongoClient;
    private static MongoDatabase database;
    private static MongoCollection<Document> collection;
    private static MongoCollection<Document> collection_Pagerank;
    private static MongoCollection<Document> TF_IDF_test_collection;
    private static MongoCollection<Document> docs_collection;
    private static MongoCollection<Document> words_collection;

    static {
        mongoClient = MongoClients.create("mongodb://localhost:27017/");
        database = mongoClient.getDatabase("SearchEngine");
        collection = database.getCollection("CrawlerPages");
        collection_Pagerank = database.getCollection("Pagerank");
        TF_IDF_test_collection = database.getCollection("TF_IDF_Test");
        docs_collection = database.getCollection("docs");
        words_collection = database.getCollection("words");
    }

    public static void insert_pagerank(HashMap<String,Double> pageRanks){
        for (HashMap.Entry<String, Double> e : pageRanks.entrySet()) {
            collection_Pagerank.insertOne(new Document("url",e.getKey()).append("pagerank",e.getValue()));
        }
    }

    public static void remove_collection_Pagerank(){
        collection_Pagerank.drop();
    }

    public static void remove_collection_Crawler(){
        collection.drop();
    }

    public static void insert_crawler(String url, String doc) {
        collection.insertOne(new Document("url",url).append("doc",doc).append("dummy",1));
    }

    // a function to get hashmap of <url : pagerank>
    public static HashMap<String, Double> getPageRanks() throws MongoException {
        HashMap<String, Double> dataMap = new HashMap<>();

        // Find all documents in the collection
        FindIterable<Document> documents = collection_Pagerank.find();

        // Iterate through each document and extract key-value pairs
        for (Document document : documents) {
            dataMap.put(document.getString("url"), document.getDouble("pagerank"));
        }

        return dataMap;
    }

    public static long getNumOfDocs() throws MongoException {
        return docs_collection.countDocuments();
    }

    public static MongoCollection<Document> getWordsCollection() throws MongoException {
        return words_collection;
    }

    public static HashMap<String, Integer> getNumWords() throws MongoException {
        HashMap<String, Integer> dataMap = new HashMap<>();
        FindIterable<Document> documents = docs_collection.find();
        for (Document document : documents) {
            String objectId_str = document.getObjectId("_id").toString();
            dataMap.put(objectId_str, document.getInteger("numWords"));
            System.out.println(dataMap.get(objectId_str));
        }
        return dataMap;
    }

}
