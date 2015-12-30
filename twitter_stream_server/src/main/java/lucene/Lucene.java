package lucene;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;

/**
 * Created by david on 19/12/15.
 */
public class Lucene {
    private static final int numberOfHits = 10;
    private static final String sAuthor = "author";
    private static final String sText = "text";
    private static final String sDate = "date";

    private HashMap<String, Query> querys;
    private HashMap<String, List<Tweet>> topTweets;

    private StandardAnalyzer analyzer;
    private Directory index;

    public Lucene() {
        querys = new HashMap<>();
        topTweets = new HashMap();

        analyzer = new StandardAnalyzer();
        index = new RAMDirectory();
    }


    public HashMap<String, List<Tweet>> addNewTweet(Tweet tweet){      //hashmap of query, list of tweets
        try{
            IndexWriterConfig config = new IndexWriterConfig(analyzer);
            IndexWriter writer = new IndexWriter(index, config);
            addDoc(writer, tweet);
            writer.close();

        } catch(IOException e){
            e.printStackTrace();
        }

        return search();

    }

    public HashMap<String, List<Tweet>> addQuery(String query){
        Query q = null;
        try {
            q = new QueryParser("text", analyzer).parse(query);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
        this.querys.put(query, q);

        //populate toptweets
        IndexReader reader = null;
        try {
            reader = DirectoryReader.open(index);

            IndexSearcher searcher = new IndexSearcher(reader);

            TopDocs docs = searcher.search(q, numberOfHits);
            ScoreDoc[] hits = docs.scoreDocs;

            List<Tweet> tweets = new ArrayList<>();

            for(int i=0; i<hits.length; ++i) {
                int docId = hits[i].doc;
                Document d = searcher.doc(docId);

                Tweet tweet = new Tweet();
                tweet.setAuthor(d.get(sAuthor));
                tweet.setDate(d.get(sDate));
                tweet.setText(d.get(sText));

                tweets.add(tweet);
            }

            topTweets.put(query, tweets);

        } catch (org.apache.lucene.index.IndexNotFoundException e){
            //when query is set before anything is added to index
            topTweets.put(query, new ArrayList<Tweet>());

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                reader.close();
            } catch (IOException e) {
                //
            } catch (NullPointerException e){
                //
            }
        }

        HashMap<String, List<Tweet>> ret = new HashMap<>();
        ret.put(query, topTweets.get(query));

        return ret;

    }

    public void removeQuery(){

    }

    private HashMap<String, List<Tweet>> search(){          //return changes, populate top tweets

        HashMap<String, List<Tweet>> changes = new HashMap<>();
        IndexReader reader = null;

        try {
            reader = DirectoryReader.open(index);
            IndexSearcher searcher = new IndexSearcher(reader);

            for(Map.Entry<String, Query> entry : querys.entrySet()) {
                String queryString = entry.getKey();
                Query query = entry.getValue();

                TopDocs docs = searcher.search(query, numberOfHits);
                ScoreDoc[] hits = docs.scoreDocs;

                List<Tweet> tweets= new ArrayList<>();
                List<Tweet> oldTweets = topTweets.get(queryString);

                boolean change = false;

                for(int i=0; i<hits.length; ++i) {
                    int docId = hits[i].doc;
                    Document d = searcher.doc(docId);

                    Tweet tweet = new Tweet();
                    tweet.setAuthor(d.get(sAuthor));
                    tweet.setText(d.get(sText));
                    tweet.setDate(d.get(sDate));

                    tweets.add(tweet);
                    try{
                        if(!tweet.equals(oldTweets.get(i)))
                            change = true;
                    } catch(IndexOutOfBoundsException e) {
                        change = true;
                    }
                }

                if(change){
                    changes.put(queryString, tweets);
                    topTweets.replace(queryString, tweets);
                }

            }



        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return changes;

    }



    private void addDoc(IndexWriter w, Tweet tweet) throws IOException {
        Document doc = new Document();
        doc.add(new TextField(sAuthor, tweet.getAuthor(), Field.Store.YES));
        doc.add(new TextField(sText, tweet.getText(), Field.Store.YES));
        doc.add(new StringField(sDate, tweet.getDate(), Field.Store.YES));

        w.addDocument(doc);
    }



}
