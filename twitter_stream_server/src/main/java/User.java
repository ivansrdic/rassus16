

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;
import com.github.scribejava.core.model.Token;


public class User {

	private String requestToken;
	private Token accessToken;
	private long userId;
	
	public User(String requestToken, Token accessToken){
		this.requestToken = requestToken;
		this.accessToken = accessToken;
		try {
			this.userId = acquireUserId();
		} catch (TwitterException e) {  //if there is an error with obtaining unique userid, then user is considered unique only while his request token is active
			this.userId = -1;
			e.printStackTrace();
		}
	}
	
	private long acquireUserId() throws TwitterException{
		ConfigurationBuilder cb = new ConfigurationBuilder();
		cb.setDebugEnabled(true)
		  .setOAuthConsumerKey("St66y6VZapKDx5j08cfRTn6JA")
		  .setOAuthConsumerSecret("wdQNykEmXR5vVHJpWdI4jVN5CPz0sPCjSWeuUeG9CtHpyiLAGk")
		  .setOAuthAccessToken(this.accessToken.getToken())
		  .setOAuthAccessTokenSecret(this.accessToken.getSecret());
		TwitterFactory tf = new TwitterFactory(cb.build());
		Twitter twitter = tf.getInstance();
		twitter4j.User user = twitter.users().verifyCredentials();
		return user.getId();
	}
	
	@Override
	public boolean equals(Object obj){
		User user = (User) obj;
		if(this.userId!=-1){
			return this.userId == user.getUserId();
		}
		return this.requestToken.equals(user.getRequestToken()) && this.accessToken.equals(user.getAccessToken());
	}
	
	@Override
	public int hashCode(){
		if(this.userId!=-1){
			return (int) this.userId;
		}
		return this.requestToken.hashCode() + this.accessToken.hashCode();
	}
	
	@Override
	public String toString(){
		return "{userId="+this.userId+"}";
	}
	
	public String getRequestToken(){
		return requestToken;
	}
	
	public Token getAccessToken(){
		return accessToken;
	}
	
	public long getUserId(){
		return userId;
	}
}
