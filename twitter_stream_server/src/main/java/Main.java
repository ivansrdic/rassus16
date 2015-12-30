import com.github.scribejava.apis.TwitterApi;
import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.model.*;
import com.github.scribejava.core.oauth.OAuthService;
import lucene.Lucene;
import lucene.Tweet;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static spark.Spark.*;

/**
 * Created by Ivan on 16.12.2015..
 */
public class Main {
    public static Map<String, Token> accessTokens;

    public static void main (String[] args) {
        accessTokens = new HashMap<>();
        // Create an OAuth service using ScribeJava using app key and secret and a callback after authenticating on Twitter
        OAuthService service = new ServiceBuilder()
                .provider(TwitterApi.class)
                .apiKey("St66y6VZapKDx5j08cfRTn6JA")
                .apiSecret("wdQNykEmXR5vVHJpWdI4jVN5CPz0sPCjSWeuUeG9CtHpyiLAGk")
                .callback("http://127.0.0.1:4567/confirmAuth")
                .build();

        // Initialize a web socket server
        webSocket("/stream", StreamingWebSocket.class);

        // Permit JavaScript redirection
        before("/auth", (req, resp) -> resp.header("Access-Control-Allow-Origin", "*"));
        // First step of authorization
        get("/auth", (req, resp) -> {
            // If a user has a requestToken cookie, and the requestToken is a valid key, just redirect him to the stream
            if(req.cookie("requestToken") != null && accessTokens.containsKey(req.cookie("requestToken"))) {
                resp.cookie("requestToken", req.cookie("requestToken"));
                resp.redirect("http://127.0.0.1/RASSUS/rassus16/public_html/stream.html");
                return "";
            }
            // If he doesn't get a new request token and redirect him to Twitter to log in and authorize the app
            else {
                Token requestToken = service.getRequestToken();
                req.session().attribute("requestToken", requestToken);
                resp.redirect("https://api.twitter.com/oauth/authenticate?oauth_token="+requestToken.getToken(), 302);
                return "";
            }
        });

        // Second step of authorization
        get("/confirmAuth", (req, resp) -> {
            Token requestToken = req.session().attribute("requestToken");
            Verifier v = new Verifier(req.queryParams("oauth_verifier"));
            // Get the access token by using the request token and verifier (from Twitter)
            Token accessToken = service.getAccessToken(requestToken, v);
            // Save it for further use
            accessTokens.put(requestToken.getToken(), accessToken);
            // Redirect the user to the stream with requestToken cookie
            resp.cookie("requestToken", requestToken.getToken());
            resp.redirect("http://127.0.0.1/RASSUS/rassus16/public_html/stream.html");
            return "";
        });
    }



    private void testLucene(){
        Lucene lucene = new Lucene();


        Tweet tweet = new Tweet();
        tweet.setText("moja mama je dobra");
        tweet.setDate("jucer");
        tweet.setAuthor("inglesias");

        HashMap<String, List<Tweet>> changes= lucene.addQuery("mama");

        HashMap<String, List<Tweet>> changes2= lucene.addNewTweet(tweet);


        Tweet tweet2 = new Tweet();
        tweet2.setText("moj tata je dobar");
        tweet2.setDate("jucer");
        tweet2.setAuthor("inglesias");

        HashMap<String, List<Tweet>> changes3= lucene.addNewTweet(tweet2);

        Tweet tweet3 = new Tweet();
        tweet3.setText("mama ima pivo");
        tweet3.setDate("jucefafasr");
        tweet3.setAuthor("inglesddfasfias");

        HashMap<String, List<Tweet>> changes4= lucene.addNewTweet(tweet3);

        HashMap<String, List<Tweet>> changes5= lucene.addQuery("tata");

        Tweet tweet4 = new Tweet();
        tweet4.setText("mama i tata pjevaju");
        tweet4.setDate("jucefafasr");
        tweet4.setAuthor("inglesddfasfias");

        HashMap<String, List<Tweet>> changes6= lucene.addNewTweet(tweet4);

        return;
    }
}