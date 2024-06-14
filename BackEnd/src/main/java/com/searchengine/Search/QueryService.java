package com.searchengine.Search;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.StringReader;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

@Service
public class QueryService {

@Autowired
private IndexerRepository indexerRepository;

@Autowired
private IndexerDocumentRepository indexerDocumentRepository;

public ArrayList<documnentOfWord> getWordDocuments(String w)
{
    System.out.println(w);

    // this line is commented to test the DB connection problem
    List<word> temp= indexerRepository.findByWordID(w);


    ArrayList<documnentOfWord>wordDocument= new ArrayList<>();

    for (word ww :temp)
    {
        for (documnentOfWord doc : ww.getDocuments()) {
            wordDocument.add( doc);
        }
    }
    return wordDocument;
}

    public  ArrayList<String> getdocelements(ObjectId objID)
    {
        @SuppressWarnings("unchecked")
        docelement d = indexerDocumentRepository.findFirstBy_id(objID);
        ArrayList<String> elements = new ArrayList<>() ;

        elements.addAll(d.getElements());

        return elements;
    }
    public ArrayList<String> stemandremovestopwordsfunc(String text)
    {

        ArrayList<String> result=new ArrayList<>();
        try {
            // Create an EnglishAnalyzer
            Analyzer analyzer = new EnglishAnalyzer();

            // Tokenize,remove stopwords and stem the text
            TokenStream stream = analyzer.tokenStream(null, new StringReader(text)); // tokenStream(fieldname,Stream of words)
            CharTermAttribute termAtt = stream.addAttribute(CharTermAttribute.class);
            stream.reset();
            while (stream.incrementToken()) {
                result.add(termAtt.toString());
            }
            stream.close();
            analyzer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return result;
    }




    public ArrayList<ArrayList<String>> searchly(String query) throws InterruptedException {
        // result will store the stemming of the query
        ArrayList<String>result = stemandremovestopwordsfunc(query);

        // this map is used to accumulate the final score of each ObjectId
        HashMap<ObjectId,Double> mp =new HashMap<ObjectId,Double>();
        // ret will store the ObjectIDs
        ArrayList<ArrayList<Object>>ret= new ArrayList<>();


        /////////////////////////////this part to handle pharse searching////
        if(query.contains("\"")){

            String [] arr=query.split("\"");
            // test will store the pharses that we need to search for
            ArrayList<String>test=new ArrayList<String>();
            for (int i=0;i<arr.length;i++) {
                if (i % 2 == 1) {
                    test.add(arr[i]);
                }
            }
            String mystr = test.get(0);
            result = stemandremovestopwordsfunc(mystr);

            ArrayList<documnentOfWord>stemResult = new ArrayList<>();


            for(String str:result){
                ArrayList<documnentOfWord>temp  = getWordDocuments(str) ;
                    if (stemResult.size() == 0 || temp.size() < stemResult.size()) {
                        stemResult = temp;
                    }

            }

            // calculate the final score of each objectId
           Thread [] threads = new Thread[stemResult.size()];
           int i = 0 ;
           for(documnentOfWord s:stemResult){
                threads[i]=new Thread(()->{
                    ObjectId obj = s.getObjectid();
                    synchronized (mp) {
                        if (mp.containsKey(obj) == true) {
                            mp.put(obj, mp.get(obj) + s.getFinal_score());
                        } else {
                            mp.put(obj, s.getFinal_score());
                        }
                    }
                });
                threads[i++].start();
            }

            for(Thread t : threads ) t.join();
            // will handle if thier is multiple " in one query we will search for all of them

            // will filter the ret array so that only the docs with the excat phrases will be returned
            ArrayList<ArrayList<Object>>phraseResult=new ArrayList<ArrayList<Object>>();
            i=0;
            threads = new Thread[stemResult.size()];
            for(documnentOfWord obj:stemResult ){
                threads[i]=new Thread(()->{
                    ArrayList<Object >toadd =new ArrayList<Object>();
                    ArrayList<String> elementsArr = new ArrayList<>() ;
                    synchronized (obj) {
                        toadd.add(obj.getObjectid());
                        toadd.add(obj.getUrl());
                        // search for the snippets
                        elementsArr= getdocelements(obj.getObjectid());
                    }


                    // this will contain the frequences of finding a phrase in the different elements of the same doc so decideing which one to return as snippest
                    ArrayList<Integer>eleFreq = new ArrayList<>() ;
                    // cnt will store how many phrases of test are found in the doc
                    int cnt = 0 ;
                    int j = 0;
                    // variables to store the ind of the element that store the max freq so we can find it in  O(1)
                    int mxfreq = 0 ;
                    int mxind = 0 ;
                    synchronized (test){
                    for (j=0;j<test.size();j++){
                        boolean found = false ;
                        int ind = 0 ;
                        int cntt = 0 ;
                        for (int k = 0 ; k<elementsArr.size();k++){
                            if(j==0) eleFreq.add(0);
                            if(elementsArr.get(k).contains(test.get(j))){
                                found=true;
                                eleFreq.set(ind,eleFreq.get(ind)+1);
                                elementsArr.set(k,elementsArr.get(k).replace(test.get(j),"<mark>"+test.get(j)+"</mark>" )) ;
                                cntt++;
                            }
                            // update max element freq
                            if(mxfreq==-1){
                                mxfreq = eleFreq.get(k);
                                mxind = k ;
                            }else if (eleFreq.get(k)>eleFreq.get(mxind)){
                                mxfreq = eleFreq.get(k);
                                mxind = k ;
                            }
                            ind++;
                        }
                        if(cntt>0)cnt++;
                        if(!found){
                            break;
                        }
                    }
                    // if we found all the phrases add the doc to phrase result
                    if(cnt==test.size()){

                        toadd.add(elementsArr.get(mxind));
                        synchronized (phraseResult) {
                            phraseResult.add(toadd);
                        }
                    }else{
                        synchronized (mp) {
                            mp.remove(obj);
                        }
                    }
                    }
                });
                threads[i++].start();

            }
            for(Thread t :threads) t.join();
            //sort the results according to the value of final score stored in mp
            Collections.sort(phraseResult, new Comparator<ArrayList<Object>>() {
                @Override
                public int compare(ArrayList<Object> arr1,ArrayList<Object> arr2) {
                    return Double.compare(mp.get(arr2.get(0)) , mp.get(arr1.get(0)));  // for descending order
                }
            });
            // filter the pharseResult so return only the url and the snniest
            ArrayList<ArrayList<String>> ans = new ArrayList<>();
            for (ArrayList<Object> str : phraseResult) {
                ArrayList<String> t = new ArrayList<>();
                t.add(str.get(1).toString());
                t.add(str.get(2).toString());
                ans.add(t);
            }

            return ans ;

        }


        // calc the final score of each ObjectID and update the mp
        for(String str:result){
            ArrayList<documnentOfWord>temp  = getWordDocuments(str) ;
            Thread [] threads = new Thread[temp.size()];
            int i = 0 ;
            // this atomic reference will be used to give higher score for objects that contain multiple words of the search query
            var ref = new Object() {
                AtomicReference<Double> mul = new AtomicReference<>((double) 1);
            };
            boolean change = true ;
            for(documnentOfWord s:temp){
                threads[i]=new Thread(()->{
                    ObjectId obj = s.getObjectid();

                    synchronized (mp) {
                        if (mp.containsKey(obj) == true) {

                                mp.put(obj, mp.get(obj)*(ref.mul.get()) + s.getFinal_score() * (ref.mul.get()));
                        } else {
                            mp.put(obj, s.getFinal_score());

                            ArrayList<Object> t = new ArrayList<>();
                            t.add(obj);
                            t.add(s.getUrl());
                            String snip = s.getElements().get(0) ;
                            /*String [] wordsOfEle=snip.split(" ");
                            for(String w : wordsOfEle){
                                ArrayList<String> strarr = stemandremovestopwordsfunc(w);
                                String strAfterStemming = "";
                                if (strarr.size() > 0) strAfterStemming= strarr.get(0);


                                    boolean check = (strAfterStemming.length()==str.length());
                                    if(check){
                                        int j = 0 ;
                                        for (;check&&j<strAfterStemming.length();j++){
                                            check=check&&(strAfterStemming.charAt(j)==str.charAt(j));
                                            if(!check)
                                                break;
                                        }
                                    }
                                    if(check){
                                        String wordAfterEdit = "<b>"+w+"</b>";
                                        snip=snip.replace(w,wordAfterEdit);
                                    }

                            }*/
                            //adjust it here TODO: adjust snipest

                            t.add(snip);
                            synchronized (ret) {
                                ret.add(t);
                            }
                        }
                    }
                    ref.mul.updateAndGet(v -> {v+=.5;
                    return v ;
                    });
                });
                threads[i++].start();
            }
            for(Thread t : threads ) t.join();
        }

        Thread [] threads = new Thread[ret.size()];
        int i = 0 ;
        int ind = -1 ;
        for(ArrayList<Object> arr:ret){
            ind++;
            ArrayList<String> finalResult = result;
            int finalInd = ind;
            threads[i]=new Thread(()->{
                String snip = arr.get(2).toString();
                String [] wordsOfEle=snip.split(" ");
                for(String w : wordsOfEle){
                    ArrayList<String> strarr = stemandremovestopwordsfunc(w);
                    String strAfterStemming = "";
                    if (strarr.size() > 0) strAfterStemming= strarr.get(0);

                    for (String str: finalResult) {
                        if(Objects.equals(str, strAfterStemming)){
                            String wordAfterEdit = "<b>" + w + "</b>";
                            snip = snip.replace(w, wordAfterEdit);
                        }
                    }
                }
                synchronized (ret){
                    arr.set(2,snip);
                    ret.set(finalInd,arr);
                }
            });
            threads[i++].start();
        }
        for(Thread t :threads) t.join();
        //sort the results according to the value of final score stored in mp
        Collections.sort(ret, new Comparator<ArrayList<Object>>() {
            @Override
            public int compare(ArrayList<Object> arr1,ArrayList<Object> arr2) {
                return Double.compare(mp.get(arr2.get(0)) , mp.get(arr1.get(0)));  // for descending order
            }
        });
        // filter the retu array so return only url and snippest
        ArrayList<ArrayList<String>> ans = new ArrayList<>();
        // Print the sorted ArrayList

        threads = new Thread[ret.size()];
       i =  0 ;
        for (ArrayList<Object> str : ret) {
            threads[i]=new Thread(()->{
                ArrayList<String> t = new ArrayList<>();
                t.add(str.get(1).toString());
                t.add(str.get(2).toString());
                synchronized (ans) {
                    ans.add(t);
                }
            });
            threads[i++].start();
        }
        for(Thread t : threads ) t.join();
        ////////////////////////////////////////////////////////////////////
        return ans ;
    }





}
