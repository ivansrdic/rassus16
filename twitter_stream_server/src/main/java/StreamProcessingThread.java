

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import lucene.Lucene;
import lucene.Tweet;


public class StreamProcessingThread implements Runnable {

	private User owner;
	private StreamUpdateListener client;
	private Lucene lucene = new Lucene();
	private Timer timer = new Timer();
	
	public StreamProcessingThread(User user, StreamUpdateListener client){
		this.owner = user;
		this.client = client;
		
		Calendar four_am = Calendar.getInstance();
		four_am.set(Calendar.HOUR_OF_DAY, 4);
		timer.scheduleAtFixedRate(reset_tweets, Math.abs(new Date().getTime() - four_am.getTimeInMillis()), 24 * 60 * 60 * 1000);
	}
	
	@Override
	public void run() {
		TwitterHBC twitter = TwitterHBC.getConnection(owner.getAccessToken());
        while (!twitter.isDone()) {
            String msg = null;
            try {
                msg = twitter.getMessage();
                System.out.println("User "+this.owner.getUserId()+" - message: "+msg);
                JSONObject tweet_raw = new JSONObject(msg);
                Tweet tweet = new Tweet();
                tweet.setAuthor(tweet_raw.getJSONObject("user").getString("name"));
                tweet.setDate(tweet_raw.getString("created_at"));
                tweet.setText(tweet_raw.getString("text"));
                Map<String, List<Tweet>> data = lucene.addNewTweet(tweet);
                if(data.size()==0){
                	continue;
                }
                String processed_msg = mapToJSON(data);
                client.onStreamUpdate(owner, processed_msg);
            } catch (InterruptedException | JSONException e) {
                e.printStackTrace();
            }
        }
	}
	
	/**
	 * Function that allows polling for data instead of listening. Intended to be used when user reconnects and wants to update his stream.
	 * @return new top 10 tweets
	 */
	public String getMessages(){
		try {
			return mapToJSON(lucene.getTopTweets());
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public String addQuery(String query){
		try {
			return mapToJSON(lucene.addQuery(query));
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	private String mapToJSON(Map<String, List<Tweet>> data) throws JSONException{
		JSONObject main = new JSONObject();
		for(Map.Entry<String, List<Tweet>> row : data.entrySet()){
			JSONArray tweets = new JSONArray();
			for(Tweet tweet_data : row.getValue()){
				JSONObject tweet = new JSONObject();
				tweet.put("author", tweet_data.getAuthor());
				tweet.put("date", tweet_data.getDate());
				tweet.put("text", tweet_data.getText());
				tweets.put(tweet);
			}
			main.put(row.getKey(), tweets);
		}
		return main.toString();
	}
	
	TimerTask reset_tweets = new TimerTask(){
		
		@Override
		public void run() {
			lucene.resetTweets();
		}
		
	};

}
