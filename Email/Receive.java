public class Receive {
    public static void main(String[] args) {
	String message = "";
	System.out.println("Waiting...");
	do {
	    message = NetIO.receiveRequest();
	    System.out.println(message);
	} while (true);
    }
}
