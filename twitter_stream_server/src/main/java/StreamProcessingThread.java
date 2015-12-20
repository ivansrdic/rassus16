
public class StreamProcessingThread implements Runnable {

	private User owner;
	private StreamUpdateListener client;
	
	public StreamProcessingThread(User user, StreamUpdateListener client){
		this.owner = user;
		this.client = client;
	}
	
	@Override
	public void run() {
		TwitterHBC twitter = TwitterHBC.getConnection(owner.getAccessToken());
        while (!twitter.isDone()) {
            String msg = null;
            try {
                msg = twitter.getMessage();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            //TODO parse message, push it to Lucene
            client.onStreamUpdate(owner, msg);
            System.out.println(msg);
        }
	}
	
	/**
	 * Function that allows polling for data instead of listening. Intended to be used when user reconnects and wants to update his stream.
	 * @return new top 10 tweets
	 */
	public String getMessages(){
		//TODO ask Lucene for new data
		return null;
	}

}
