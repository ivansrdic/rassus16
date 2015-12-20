import com.github.scribejava.core.model.Token;


public class User {

	private String requestToken;
	private Token accessToken;
	
	public User(String requestToken, Token accessToken){
		this.requestToken = requestToken;
		this.accessToken = accessToken;
	}
	
	@Override
	public boolean equals(Object obj){
		User user = (User) obj;
		return this.requestToken.equals(user.getRequestToken()) && this.accessToken.equals(user.getAccessToken());
	}
	
	public String getRequestToken(){
		return requestToken;
	}
	
	public Token getAccessToken(){
		return accessToken;
	}
}
