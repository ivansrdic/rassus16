/**
 * Created by Ivan on 16.12.2015..
 */
public class Main {
    public static void main (String[] args) {
        TwitterHBC twitter = new TwitterHBC("St66y6VZapKDx5j08cfRTn6JA", "wdQNykEmXR5vVHJpWdI4jVN5CPz0sPCjSWeuUeG9CtHpyiLAGk",
                "4436645361-YKCs94WpK6bbPrXeKxZBRbhiwNPPWqv91KR8Rzr", "OFrccO6cqsX0P31fP2MNjhZ7s3Dr6dJrEjwAQyXVf2Sb7",
                "Rassus 16");
        twitter.connect();
        while (!twitter.isDone()) {
            String msg = null;
            try {
                msg = twitter.getMessage();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println(msg);
        }
    }
}
