package lucene;

/**
 * Created by david on 19/12/15.
 */
public class Tweet {
    private String author, text;
    private String date;

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }


    public boolean equals(Tweet tweet){
        return (this.getAuthor().equals(tweet.getAuthor()) &&
                this.getText().equals(tweet.getText()) &&
                this.getDate().equals(tweet.getDate()));
    }
}
