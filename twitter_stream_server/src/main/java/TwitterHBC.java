import com.github.scribejava.core.model.Token;
import com.twitter.hbc.ClientBuilder;
import com.twitter.hbc.core.Client;
import com.twitter.hbc.core.Constants;
import com.twitter.hbc.core.Hosts;
import com.twitter.hbc.core.HttpHosts;
import com.twitter.hbc.core.endpoint.UserstreamEndpoint;
import com.twitter.hbc.core.event.Event;
import com.twitter.hbc.core.processor.StringDelimitedProcessor;
import com.twitter.hbc.httpclient.auth.Authentication;
import com.twitter.hbc.httpclient.auth.OAuth1;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Twitter Hosebird Client
 * Further Abstraction of the official Twitter Hosebird Client
 * Used for consuming Twitter's Streaming API
 */
public class TwitterHBC {
    private BlockingQueue<String> msgQueue;
    private BlockingQueue<Event> eventQueue;

    // credentials
    private String consumerKey;
    private String consumerSecret;
    private String token;
    private String secret;

    // name of client, mainly for logging
    private String clientName;

    // original Twitter Hosebird Client
    Client hosebirdClient;

    /**
     * Constructor
     * @param consumerKey       String  Consumer key retrieved from the Twitter login
     * @param consumerSecret    String  Consumer secret retrieved from the Twitter login
     * @param token             String  App token from the Twitter dev account
     * @param secret            String  App secret from the Twitter dev account
     * @param clientName        String  Name of client, mainly for logging
     */
    public TwitterHBC(String consumerKey, String consumerSecret, String token, String secret, String clientName) {
        this.consumerKey = consumerKey;
        this.consumerSecret = consumerSecret;
        this.token = token;
        this.secret = secret;

        this.clientName = clientName;
    }

    /**
     * Used for connecting to the Twitter Streaming API
     */
    public void connect() {
        /** Set up your blocking queues: Be sure to size these properly based on expected TPS of your stream */
        this.msgQueue = new LinkedBlockingQueue<>(100000);
        this.eventQueue = new LinkedBlockingQueue<>(1000);

        /** Declare the host you want to connect to, the endpoint, and authentication (basic auth or oauth) */
        Hosts hosebirdHosts = new HttpHosts(Constants.USERSTREAM_HOST);
        UserstreamEndpoint endpoint = new UserstreamEndpoint();

        // These secrets should be read from a config file
        Authentication hosebirdAuth = new OAuth1(this.consumerKey, this.consumerSecret, this.token, this.secret);

        ClientBuilder builder = new ClientBuilder()
                .name(this.clientName)                              // optional: mainly for the logs
                .hosts(hosebirdHosts)
                .authentication(hosebirdAuth)
                .endpoint(endpoint)
                .processor(new StringDelimitedProcessor(this.msgQueue))
                .eventMessageQueue(this.eventQueue);                          // optional: use this if you want to process client events

        this.hosebirdClient = builder.build();
        // Attempts to establish a connection.
        this.hosebirdClient.connect();
    }

    /**
     * Checks if the original Twitter Hosebird Client is done with reading from the stream
     * @return  boolean
     */
    public boolean isDone() {
        return this.hosebirdClient.isDone();
    }

    /**
     * Gets a message from the message queue
     * @return String JSON message
     * @throws InterruptedException
     */
    public String getMessage() throws InterruptedException {
        return this.msgQueue.take();
    }

    public static TwitterHBC getConnection(Token accessToken) {
        TwitterHBC twitter = new TwitterHBC("St66y6VZapKDx5j08cfRTn6JA", "wdQNykEmXR5vVHJpWdI4jVN5CPz0sPCjSWeuUeG9CtHpyiLAGk",
                accessToken.getToken(), accessToken.getSecret(),
                "Rassus 16");
        twitter.connect();
        return twitter;
    }
}
