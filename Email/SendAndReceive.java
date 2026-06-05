import hsa.*;
public class SendAndReceive {
    public static void main(String[] args) {
	// call the receiver thread
	ReceiverThread r = new ReceiverThread();
	r.run();
	// the sender thread runs as default thread
	Console c = new Console("Sender Console. My address is " + NetIO.myIPAddress());
	c.println("To: (ip-address): ");
	String ipAddress = c.readLine();
	/*do {
	    String message = c.readLine();
	    int errorCode = NetIO.sendRequest(message, ipAddress, 5001);
	} while(true); */
    }
}
