package com.searchengine.Search;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.StringReader;
import java.util.*;

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
    public ArrayList<ArrayList<String>> searchly(String query){
        // result will store the stemming of the query
        ArrayList<String>result = stemandremovestopwordsfunc(query);
        /*  for(String str:result){
            System.out.println(str);
        }
        */

        // this map is used to accumulate the final score of each ObjectId
        HashMap<ObjectId,Double> mp =new HashMap<ObjectId,Double>();
        // ret will store the ObjectIDs
        ArrayList<ObjectId>ret= new ArrayList<>();

        // calc the final score of each ObjectID and update the mp
        for(String str:result){
            ArrayList<documnentOfWord>temp  = getWordDocuments(str) ;

            for(documnentOfWord s:temp){
                ObjectId obj = s.getObjectid();
                if(mp.containsKey(obj)==true){
                    mp.put(obj,mp.get(obj)+s.getFinal_score());
                }else {
                    mp.put(obj, s.getFinal_score());
                    ret.add(obj);
                }
            }
        }
        /////////////////////////////this part to handle pharse searching////
        if(query.contains("\"")){
            // will handle if thier is multiple " in one query we will search for all of them
            String [] arr=query.split("\"");
            // test will store the pharses that we need to search for
            ArrayList<String>test=new ArrayList<String>();
            for (int i=0;i<arr.length;i++) {
                if (i % 2 == 1) {
                    test.add(arr[i]);
                }
            }
            // will filter the ret array so that only the docs with the excat phrases will be returned
            ArrayList<ArrayList<Object>>phraseResult=new ArrayList<ArrayList<Object>>();
            for(ObjectId obj:ret ){
                ArrayList<Object >toadd =new ArrayList<Object>();
                String urL = indexerDocumentRepository.findFirstBy_id(obj).getUrl() ;
                toadd.add(obj);
                toadd.add(urL);

                // search for the snippets
                ArrayList<String>elementsArr=getdocelements(obj);

                // this will contain the frequences of finding a phrase in the different elements of the same doc so decideing which one to return as snippest
                ArrayList<Integer>eleFreq = new ArrayList<>() ;
                // cnt will store how many phrases of test are found in the doc
                int cnt = 0 ;
                int i = 0;
                // variables to store the ind of the element that store the max freq so we can find it in  O(1)
                int mxfreq = 0 ;
                int mxind = 0 ;

                for (i=0;i<test.size();i++){
                    boolean found = false ;
                    int ind = 0 ;
                    int cntt = 0 ;
                    for (int k = 0 ; k<elementsArr.size();k++){
                        if(i==0) eleFreq.add(0);
                        if(elementsArr.get(k).contains(test.get(i))){
                            found=true;
                            eleFreq.set(ind,eleFreq.get(ind)+1);
                            elementsArr.set(k,elementsArr.get(k).replace(test.get(i),"<mark>"+test.get(i)+"</mark>" )) ;
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
                    //int maxValue = Collections.max(eleFreq);
                    //int maxIndex = eleFreq.indexOf(maxValue);
                    toadd.add(elementsArr.get(mxind));
                    phraseResult.add(toadd);
                }else{
                    mp.remove(obj);
                }
            }

            //sort the results according to the value of final score stored in mp
            Collections.sort(phraseResult, new Comparator<ArrayList<Object>>() {
                @Override
                public int compare(ArrayList<Object> arr1,ArrayList<Object> arr2) {
                    return Double.compare(mp.get(arr2.get(0)) , mp.get(arr1.get(0)));  // for descending order
                }
            });
            // filter the pharseResult so return only the url and the snniest
            ArrayList<ArrayList<String>> ans = new ArrayList<>();
            // Print the sorted ArrayList
            for (ArrayList<Object> str : phraseResult) {
                ArrayList<String> t = new ArrayList<>();
                t.add(str.get(1).toString());
                t.add(str.get(2).toString());
                ans.add(t);
                for (Object s:str)
                    System.out.println(s);
            }

            return ans ;

        }


        ArrayList<ArrayList<Object>>retu=new ArrayList<ArrayList<Object>>();
        // we will iterate of the each word in the doc and if it and the query have the same stemming so we will return that element as snippest
        for(ObjectId obj:ret ){
            ArrayList<Object>toAdd=new ArrayList<>();
            String urL = indexerDocumentRepository.findFirstBy_id(obj).getUrl() ;
            toAdd.add(obj);
            toAdd.add(urL);

            int freq = 0 ;
            String temp ="" ;
            ArrayList<String>elementsArr=getdocelements(obj);
            for (String e :elementsArr) {
                String eAfterEdit= e ;
                String [] wordsOfEle=e.split(" ");
                int cnt = 0;
                for(String w : wordsOfEle){

                    ArrayList<String> strarr = stemandremovestopwordsfunc(w);
                    String str = "";
                    if (strarr.size() > 0) str = strarr.get(0);
                    for (String stemWord : result) {

                        boolean check = (str.length()==stemWord.length());
                        if(check){
                            int j = 0 ;
                            for (;check&&j<str.length();j++){
                                check=check&&(str.charAt(j)==stemWord.charAt(j));
                                if(!check)
                                    break;
                            }
                            if(check){

                            }
                        }
                    if(check){
                        cnt++;
                        String wordAfterEdit = "<b>"+w+"</b>";
                        e=e.replace(w,wordAfterEdit);
                    }
                    }
                }
                if(cnt>freq){
                    freq=cnt;
                    temp=e;
                }
            }
            toAdd.add(temp);
            // TODO: here if the elem is  "" we may multiply the final score of the url by 0 so it appears last in the results
            retu.add(toAdd);
        }

        //sort the results according to the value of final score stored in mp
        Collections.sort(retu, new Comparator<ArrayList<Object>>() {
            @Override
            public int compare(ArrayList<Object> arr1,ArrayList<Object> arr2) {
                return Double.compare(mp.get(arr2.get(0)) , mp.get(arr1.get(0)));  // for descending order
            }
        });
        // filter the retu array so return only url and snippest
        ArrayList<ArrayList<String>> ans = new ArrayList<>();
        // Print the sorted ArrayList

        for (ArrayList<Object> str : retu) {

            ArrayList<String> t = new ArrayList<>();
            t.add(str.get(1).toString());
            t.add(str.get(2).toString());
            ans.add(t);
           // for (Object s:str)
           // System.out.println(s);

        }

        ////////////////////////////////////////////////////////////////////
        return ans ;
    }

}
