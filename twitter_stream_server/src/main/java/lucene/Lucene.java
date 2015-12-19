package lucene;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
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
    private final int numberOfHits = 10;

    private List<Query> querys;

    private StandardAnalyzer analyzer;
    private Directory index;
    private IndexWriterConfig config;


    public Lucene() {
        querys = new ArrayList<>();

        analyzer = new StandardAnalyzer();
        index = new RAMDirectory();
        config = new IndexWriterConfig(analyzer);
    }


    public void addNewTweet(Tweet tweet){      //boolean or array of changes?
        try{
            IndexWriter writer = new IndexWriter(index, config);
            addDoc(writer, tweet.getAuthor(), tweet.getText());
            writer.close();

        } catch(IOException e){
            e.printStackTrace();
        }



    }

    public void addQuery(String query) throws ParseException {
        Query q = new QueryParser("text", analyzer).parse(query);
        this.querys.add(q);
    }

    public void removeQuery(){

    }

    private void search(){          //special ScoreDOc for every query?
        try {
            IndexReader reader = DirectoryReader.open(index);
            IndexSearcher searcher = new IndexSearcher(reader);

            for(Query query : querys){
                TopDocs docs = searcher.search(query, numberOfHits);
                ScoreDoc[] hits = docs.scoreDocs;
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    private void addDoc(IndexWriter w, String author, String text) throws IOException {
        Document doc = new Document();
        doc.add(new TextField("author", author, Field.Store.YES));
        doc.add(new StringField("text", text, Field.Store.YES));
        w.addDocument(doc);
    }



}
