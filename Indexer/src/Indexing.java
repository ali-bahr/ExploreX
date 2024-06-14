import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

class StringData {
    int count;
    ArrayList<String> strings;

    public StringData() {
        this.count = 1;
        this.strings = new ArrayList<>();
    }
}
class worddata {
    int count;
    String url;
    String Objectid;
    ArrayList<String> strings;
    ArrayList<String> elements;

    public worddata() {
        url="";
        this.count = 1;
        this.strings = new ArrayList<>();
    }
}
public class Indexing  extends Thread{
    Object o;
    HashMap<String,ArrayList<worddata>> worddocs;
    Indexing(Object o,HashMap<String,ArrayList<worddata>> worddocs)
    {
        this.o=o;
        this.worddocs=worddocs;
    }
    public static Document stringToHtmlDocument(String htmlString) {
        return Jsoup.parse(htmlString);
    }
    public ArrayList<String> parseHtmlDoc(Document doc,String type,ArrayList<String >docelments,HashMap<String, ArrayList<String>> wordelements)
    {
        ArrayList<String> result=new ArrayList<>();
        StringBuilder resultSB=new StringBuilder();
        Elements elements = doc.select(type);
        for (Element element : elements) {
            try {
                docelments.add(element.text());
                // Create an EnglishAnalyzer
                Analyzer analyzer = new EnglishAnalyzer();

                // Tokenize,remove stopwords and stem the text
                TokenStream stream = analyzer.tokenStream(null, new StringReader(element.text())); // tokenStream(fieldname,Stream of words)
                CharTermAttribute termAtt = stream.addAttribute(CharTermAttribute.class);
                stream.reset();
                while (stream.incrementToken()) {
                    result.add(termAtt.toString());
                    if (wordelements.containsKey(termAtt.toString())) {
                        // If it exists, get the list and add the text
                        ArrayList<String> existingList = wordelements.get(termAtt.toString());
                        existingList.add(element.text());
                        wordelements.put(termAtt.toString(), existingList);
                    } else {
                        // If it doesn't exist, create a new list and add the text
                        ArrayList<String> newList = new ArrayList<>();
                        newList.add(element.text());
                        wordelements.put(termAtt.toString(), newList);
                    }
                }
                stream.close();
                analyzer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        return result;
    }

    public  void    indexingandputindatabase(Document doc, String type, String tag, String Html, ArrayList<String> Docelements,HashMap<String,StringData>doctf,HashMap<String, ArrayList<String>> wordelements) {
        // Create an instance of Main class
        ArrayList<String> result=parseHtmlDoc(doc,type,Docelements,wordelements);

        for (String str : result) {
            // Check if the string is already in the hashmap
            if (!doctf.containsKey(str)) {
                StringData data = new StringData();
                data.strings.add(tag);
                doctf.put(str, data);
            } else {
                StringData data = doctf.get(str);
                data.count++;
                data.strings.add(tag);
                doctf.put(str, data); // Put the updated StringData back into the map
            }
        }

    }
    public void putindb(HashMap<String,ArrayList<worddata>>worddocs,HashMap<String,StringData>doctf,String url,String objectid,HashMap<String, ArrayList<String>> wordelements)
    {
        Set<String> keys = doctf.keySet();
        for (String key : keys) {
            // Process key
            if (!worddocs.containsKey(key)) {
                worddata data = new worddata();
                StringData datatf = doctf.get(key);
                data.strings=datatf.strings;
                data.url=url;
                data.Objectid=objectid;
                data.elements=wordelements.get(key);
                ArrayList<worddata>arr=new ArrayList<>();
                arr.add(data);
                worddocs.put(key, arr);
            } else {
                worddata data = new worddata();
                StringData datatf = doctf.get(key);
                data.strings=datatf.strings;
                data.url=url;
                data.Objectid=objectid;
                data.elements=wordelements.get(key);
                int count=datatf.count+1;
                data.count=count;
                ArrayList<worddata>arr=worddocs.get(key);
                arr.add(data);
                worddocs.put(key, arr);
            }
        }

    }


    @Override
    public void run() {

        ArrayList<String> docelements=new ArrayList<>();
        try {
            // Load HTML file

            String []crawledpage=mongo.getcrawledpage(o);
            Document doc;
            while (crawledpage[0]!=null)
            {
                HashMap<String, StringData> doctf = new HashMap<>();
                HashMap<String, ArrayList<String>> wordelements = new HashMap<>();
                doc=stringToHtmlDocument(crawledpage[0]);
                // Get text content from the document
                String textContent = doc.text();
                // Split the text content into words based on whitespace or punctuation
                String[] words = textContent.split("[\\s\\p{Punct}]+");
                // Count the number of words
                int wordCount = words.length;

                indexingandputindatabase(doc,"p","normal",crawledpage[1],docelements,doctf,wordelements);
                indexingandputindatabase(doc,"h1","Heading",crawledpage[1],docelements,doctf,wordelements);
                indexingandputindatabase(doc,"title","title",crawledpage[1],docelements,doctf,wordelements);
                indexingandputindatabase(doc, "li", "normal",crawledpage[1],docelements,doctf,wordelements);

                String objectid= mongo.putdocelments(crawledpage[1],docelements,wordCount,o);
                putindb(worddocs,doctf,crawledpage[1],objectid,wordelements);
                crawledpage=mongo.getcrawledpage(o);
            }
            // Execute the database operation




        }catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}