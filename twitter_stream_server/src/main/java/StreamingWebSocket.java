import com.github.scribejava.core.model.Token;

import org.eclipse.jetty.websocket.api.*;
import org.eclipse.jetty.websocket.api.annotations.*;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;

@WebSocket
public class StreamingWebSocket implements StreamUpdateListener{

    private static final Queue<Session> unindentified_users = new ConcurrentLinkedQueue<>();
    private static final Map<User, Session> sessions = new ConcurrentHashMap<>();
    private static final Map<User, StreamProcessingThread> threads = new ConcurrentHashMap<>();

    @OnWebSocketConnect
    public void connected(Session session) {
    	unindentified_users.add(session);
    }

    @OnWebSocketClose
    public void closed(Session session, int statusCode, String reason) {
    	unindentified_users.remove(session);
    	User user = null;
    	for(Map.Entry<User, Session> row : sessions.entrySet()){
			if(row.getValue().equals(session)){
				user = row.getKey();
				break;
			}
		}
    	sessions.remove(user);
    }

    @OnWebSocketMessage
    public void message(Session session, String message) throws IOException {
    	if(unindentified_users.contains(session)){   // First message is the request token
    		Token accessToken = Main.accessTokens.get(message);
    		User user = new User(message, accessToken);
    		sessions.put(user, session);
    		if(!threads.containsKey(user)){   //if client has not been connected before
    			StreamProcessingThread thread = new StreamProcessingThread(user, this);
        		threads.put(user, thread);
        		new Thread(thread).start();
    		} else{
    			String msg = threads.get(user).getMessages();
    			if(msg!=null){
					onStreamUpdate(user, msg);
				}
    		}
    		unindentified_users.remove(session);
    	}else{
    		for(Map.Entry<User, Session> row : sessions.entrySet()){
    			if(row.getValue().equals(session)){
    				String msg = threads.get(row.getKey()).addQuery(message);
    				if(msg!=null){
    					onStreamUpdate(row.getKey(), msg);
    				}
    			}
    		}
    	}
    }

	@Override
	public void onStreamUpdate(User user, String message) {
		try {
			Session session = sessions.get(user);
			if(session==null || !session.isOpen())  //client is disconnected
				return;
			session.getRemote().sendString(message);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}