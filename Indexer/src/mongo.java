import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import java.util.ArrayList;

public class mongo {
    private static MongoClient mongoClient;
    private static MongoDatabase database;
    private static MongoCollection<Document> Words;
    private static MongoCollection<Document> Crawlerpages;
    private static MongoCollection<Document> Docs;

    static {
        mongoClient = MongoClients.create("mongodb://localhost:27017/");
        database = mongoClient.getDatabase("SearchEngine");
        Words = database.getCollection("words");
        Crawlerpages = database.getCollection("CrawlerPages");
        Docs = database.getCollection("docs");
    }

    public static int df_count(String word) {
        Document document = Words.find(new Document("word_id", word)).first();
        if (document != null) return document.getInteger("count");
        else return 0;
    }
    public static String[] getcrawledpage(Object o) {
        Document document;
        synchronized (o) {
            document = Crawlerpages.findOneAndDelete(new Document("dummy", 1));
        }

        if (document != null) {
            String doc = document.getString("doc");
            String url = document.getString("url");
            if (doc != null && url != null) {
                return new String[]{doc, url};
            }
        }
        // Handle the case where no document was found or if doc or url is null
        return new String[]{null, null}; // Or throw an exception, depending on your use case
    }

    public static int tf_count(String word, String doc) {
        Document document = Words.find(new Document("word_id", word)).first();
        if (document != null) {
            @SuppressWarnings("unchecked")
            ArrayList<Document> documents = (ArrayList<Document>) document.get("documents");
            for (Document document1 : documents) {
                if (document1.get("url").equals(doc)) {
                    return document1.getInteger("tf");
                }
            }
        }
        return 0;
    }
    public static String putdocelments(String url, ArrayList<String> docelments, int count,Object o)
    {
        System.out.println(url);
        synchronized (o)
        {
            Document doc = new Document("url", url)
                    .append("elements", docelments)
                    .append("numWords", count);
            Docs.insertOne(doc);
            return doc.getObjectId("_id").toString();

        }
    }
    public static ArrayList<String> getdocelements(String docid)
    {
        // may be wrong
        @SuppressWarnings("unchecked")
        ArrayList<String>docelements=(ArrayList<String>) Docs.find(new Document("Docid",docid)).first().get("elements");
        return docelements;
    }
    public static ArrayList<String> getworddocs(String word)
    {
        ArrayList<Document>docs=(ArrayList<Document>)Words.find(new Document("word_id",word)).first().get("documents");
        ArrayList<String>worddocs=new ArrayList<>();
        for (Document document:docs)
        {
            worddocs.add((String) document.get("url"));
        }
        return worddocs;
    }

    public static ArrayList<String> metadata(String word, String doc) {
        Document document = Words.find(new Document("word_id", word)).first();
        if (document != null) {
            @SuppressWarnings("unchecked")
            ArrayList<Document> documents = (ArrayList<Document>) document.get("documents");
            for (Document document1 : documents) {
                if (document1.get("url").equals(doc)) {
                    return (ArrayList<String>) document1.get("metadata");
                }
            }
        }
        return null;
    }

    public static void connectmongo(String word, ArrayList<worddata> data,Object o) {

        try {
            Document result = Words.find(new Document("wordID", word)).first();
            if (result != null) {
                @SuppressWarnings("unchecked")
                ArrayList<Document> documents = (ArrayList<Document>) result.get("documents");
                for (worddata w:data) {
                    Document d=new Document("url",w.url).append("metadata",w.strings).append("tf",w.count).append("objectid",w.Objectid).append("elements",w.elements);
                    documents.add(d);
                }
                synchronized(o)
                {
                    Words.updateOne(
                            new Document("wordID", word),
                            new Document("$set", new Document("documents",documents))
                    );
                }


            } else {
                ArrayList<Document> documents = new ArrayList<>();

                for (worddata w:data) {
                    Document d=new Document("url",w.url).append("metadata",w.strings).append("tf",w.count).append("objectid",w.Objectid).append("elements",w.elements);
                    documents.add(d);
                }
                synchronized (o)
                {
                    Words.insertOne(new Document("wordID", word)
                            .append("documents", documents)
                    );
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}