import com.github.scribejava.core.model.Token;
import org.eclipse.jetty.websocket.api.*;
import org.eclipse.jetty.websocket.api.annotations.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.*;

@WebSocket
public class StreamingWebSocket {

    // Store sessions if you want to, for example, broadcast a message to all users
    private static final Queue<Session> sessions = new ConcurrentLinkedQueue<>();

    @OnWebSocketConnect
    public void connected(Session session) {
        sessions.add(session);
    }

    @OnWebSocketClose
    public void closed(Session session, int statusCode, String reason) {
        sessions.remove(session);
    }

    @OnWebSocketMessage
    public void message(Session session, String message) throws IOException {
        // First message is the request token
        Token accessToken = Main.accessTokens.get(message);
        RemoteEndpoint remote = session.getRemote();
        TwitterHBC twitter = TwitterHBC.getConnection(accessToken);
        while (!twitter.isDone()) {
            String msg = null;
            try {
                msg = twitter.getMessage();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            remote.sendString(msg);
            System.out.println(msg);
        }
    }
}