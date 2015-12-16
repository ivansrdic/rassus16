import com.google.common.collect.Lists;
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

import java.util.ArrayList;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by Ivan on 16.12.2015..
 */
public class TwitterHBC {
    private BlockingQueue<String> msgQueue;
    private BlockingQueue<Event> eventQueue;

    private String consumerKey;
    private String consumerSecret;
    private String token;
    private String secret;

    private String clientName;

    Client hosebirdClient;

    public TwitterHBC(String consumerKey, String consumerSecret, String token, String secret, String clientName) {
        this.consumerKey = consumerKey;
        this.consumerSecret = consumerSecret;
        this.token = token;
        this.secret = secret;

        this.clientName = clientName;
    }

    public void connect() {
        /** Set up your blocking queues: Be sure to size these properly based on expected TPS of your stream */
        this.msgQueue = new LinkedBlockingQueue<>(100000);
        this.eventQueue = new LinkedBlockingQueue<>(1000);

        /** Declare the host you want to connect to, the endpoint, and authentication (basic auth or oauth) */
        Hosts hosebirdHosts = new HttpHosts(Constants.USERSTREAM_HOST);
        UserstreamEndpoint endpoint = new UserstreamEndpoint();
        // Optional: set up some followings and track terms
        ArrayList<Long> followings = Lists.newArrayList(1234L, 566788L);
        ArrayList<String> terms = Lists.newArrayList("twitter", "api");
        endpoint.withFollowings(true);

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

    public void run() {
        while (!this.hosebirdClient.isDone()) {
            String msg = null;
            String event = null;
            try {
                msg = this.msgQueue.take();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println(msg);
        }
    }
}
