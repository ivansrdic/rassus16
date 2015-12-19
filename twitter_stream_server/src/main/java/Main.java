import com.github.scribejava.apis.TwitterApi;
import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.model.*;
import com.github.scribejava.core.oauth.OAuthService;

import java.util.HashMap;
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
}