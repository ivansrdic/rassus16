import org.eclipse.jetty.websocket.api.*;
import org.eclipse.jetty.websocket.api.annotations.*;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;

/**
 * Created by Ivan on 10.12.2015..
 */
public class MessageSender {

    private static Session connectedSession;

    public static void setConnectedSession(Session session) {
        connectedSession = session;
    }

    public void sendMessage(String message) {
        try {
            this.connectedSession.getRemote().sendString(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
