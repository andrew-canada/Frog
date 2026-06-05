import hsa.*;
public class Send {
    public static void main(String[] args) {
	String message = "";
	String ipAddress = "";
	do {
	    Stdout.print("Message: ");
	    message = Stdin.readLine();
	    Stdout.print("IP address: ");
	    ipAddress = Stdin.readLine();
	    int errorCode = NetIO.sendRequest(message, ipAddress);
	} while (true);
    }
}

// Self: 10.207.98.103
