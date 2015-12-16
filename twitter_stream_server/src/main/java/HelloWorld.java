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

import static java.lang.Thread.sleep;
import static spark.Spark.*;

public class HelloWorld {
    public static void main(String[] args) {
        staticFileLocation("public");
        webSocket("/echo", EchoWebSocket.class);
        init(); // Needed if you don't define any HTTP routes after your WebSocket routes

        try {
            sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        MessageSender sender = new MessageSender();

        /** Set up your blocking queues: Be sure to size these properly based on expected TPS of your stream */
        BlockingQueue<String> msgQueue = new LinkedBlockingQueue<>(100000);
        BlockingQueue<Event> eventQueue = new LinkedBlockingQueue<>(1000);

        /** Declare the host you want to connect to, the endpoint, and authentication (basic auth or oauth) */
        Hosts hosebirdHosts = new HttpHosts(Constants.USERSTREAM_HOST);
        UserstreamEndpoint endpoint = new UserstreamEndpoint();
        // Optional: set up some followings and track terms
        ArrayList<Long> followings = Lists.newArrayList(1234L, 566788L);
        ArrayList<String> terms = Lists.newArrayList("twitter", "api");
        endpoint.withFollowings(true);

        // These secrets should be read from a config file
        Authentication hosebirdAuth = new OAuth1("St66y6VZapKDx5j08cfRTn6JA", "wdQNykEmXR5vVHJpWdI4jVN5CPz0sPCjSWeuUeG9CtHpyiLAGk",
                "4436645361-YKCs94WpK6bbPrXeKxZBRbhiwNPPWqv91KR8Rzr", "OFrccO6cqsX0P31fP2MNjhZ7s3Dr6dJrEjwAQyXVf2Sb7");

        ClientBuilder builder = new ClientBuilder()
                .name("Rassus 16")                              // optional: mainly for the logs
                .hosts(hosebirdHosts)
                .authentication(hosebirdAuth)
                .endpoint(endpoint)
                .processor(new StringDelimitedProcessor(msgQueue))
                .eventMessageQueue(eventQueue);                          // optional: use this if you want to process client events

        Client hosebirdClient = builder.build();
        // Attempts to establish a connection.
        hosebirdClient.connect();

        while (!hosebirdClient.isDone()) {
            String msg = null;
            try {
                msg = msgQueue.take();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println(msg);
            sender.sendMessage(msg);
        }
    }
}