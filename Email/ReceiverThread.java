import hsa.*;
public class ReceiverThread extends Thread {
    public ReceiverThread(){}
    public void run() {
	Console receiveWindow = new Console("Receiver Console - My address is " + NetIO.myIPAddress());
	do {
	    String message =  NetIO.receiveRequest(5000);
	} while(true);
    }
}
