import java.io.*;
import java.net.*;

public class NetIO{
    public static String myUserName() {
	String userName = "";
	try{
	    userName = System.getProperty("user.name");
	} catch(Exception e) {
	    System.out.println("Unable to return username. ");
	}
	return userName;
    }
    public static String myIPAddress() {
	String ipAddress = "";
	try {
	    InetAddress myComputer = InetAddress.getLocalHost();
	    ipAddress = myComputer.getHostAddress();
	} catch(Exception e) {
	    System.out.println("Unable to retrieve IP address. ");
	}
	return ipAddress;
    }
    
    public static int sendRequest(String message, String destinationIPAddress) {
	int errorCode = Globals.NET_SEND_ERROR;
	int attempts = 1;
	do {
	    try {
		// attempt to connect to server
		Socket myComputer = new Socket();
		InetSocketAddress destination = new InetSocketAddress(destinationIPAddress,
								      Globals.PORT_NUMBER);
		myComputer.connect(destination, Globals.TIME_OUT);
		
		// get output stream to send message through
		DataOutputStream output = new DataOutputStream(myComputer.getOutputStream());
		output.writeUTF(message);
		
		// wait for confirmation ffrom outher computer
		DataInputStream input = new DataInputStream(myComputer.getInputStream());
		String confirmation = input.readUTF();
		if (confirmation.equals("" + Globals.NET_OK)){
		     errorCode = Globals.NET_OK;
		}
		
		myComputer.close();
		
	    } catch(IOException e) {
		/*System.out.println("Connection timeout. Attempt to connect again " + 
				    attempts + " of " + Globals.SENDING_ATTEMPTS_LIMIT);
		*/attempts++;
	    }
	} while(errorCode != Globals.NET_OK && attempts <= Globals.SENDING_ATTEMPTS_LIMIT);
	return errorCode;
    }
    public static int sendRequest(String message, String destinationIPAddress, int port) {
	int errorCode = Globals.NET_SEND_ERROR;
	int attempts = 1;
	do {
	    try {
		// attempt to connect to server
		Socket myComputer = new Socket();
		InetSocketAddress destination = new InetSocketAddress(destinationIPAddress,
								      port);
		myComputer.connect(destination, Globals.TIME_OUT);
		
		// get output stream to send message through
		DataOutputStream output = new DataOutputStream(myComputer.getOutputStream());
		output.writeUTF(message);
		
		// wait for confirmation from other computer
		DataInputStream input = new DataInputStream(myComputer.getInputStream());
		String confirmation = input.readUTF();
		if (confirmation.equals("" + Globals.NET_OK)){
		     errorCode = Globals.NET_OK;
		}
		
		myComputer.close();
		
	    } catch(IOException e) {
		System.out.println("Connection timeout. Attempt to connect again " + 
				    attempts + " of " + Globals.SENDING_ATTEMPTS_LIMIT);
		attempts++;
	    }
	} while(errorCode != Globals.NET_OK && attempts <= Globals.SENDING_ATTEMPTS_LIMIT);
	return errorCode;    
    }
    
    public static String receiveRequest(){
	String request = "";
	int errorCode = Globals.NET_RECEIVE_ERROR;
	try {
	    // create server socket
	    ServerSocket server = new ServerSocket(Globals.PORT_NUMBER, Globals.SERVER_QUEUE_LENGTH);             
	    // create socket and timeout
	    Socket myComputer = server.accept();
	    myComputer.setSoTimeout(Globals.TIME_OUT);
	    // get input stream and receive request
	    DataInputStream input = new DataInputStream(myComputer.getInputStream());
	    request = input.readUTF();
	    // get output stream and send confirmation
	    DataOutputStream output = new DataOutputStream(myComputer.getOutputStream());
	    output.writeUTF("" + Globals.NET_OK);
	    myComputer.close();
	    server.close();
	    errorCode = Globals.NET_OK;
	} catch (IOException e) {
	    System.out.println("Connection/transmission error");
	}
    return request;
    }
    
    public static String receiveRequest(int port){
	String request = "";
	int errorCode = Globals.NET_RECEIVE_ERROR;
	try {
	    // create server socket
	    ServerSocket server = new ServerSocket(port, Globals.SERVER_QUEUE_LENGTH);             
	    // create socket and timeout
	    Socket myComputer = server.accept();
	    myComputer.setSoTimeout(Globals.TIME_OUT);
	    // get input stream and receive request
	    DataInputStream input = new DataInputStream(myComputer.getInputStream());
	    request = input.readUTF();
	    // get output stream and send confirmation
	    DataOutputStream output = new DataOutputStream(myComputer.getOutputStream());
	    output.writeUTF("" + Globals.NET_OK);
	    myComputer.close();
	    server.close();
	    errorCode = Globals.NET_OK;
	} catch (IOException e) {
	    System.out.println("Connection/transmission error");
	}
    return request;
    }
    public static void main(String[] args) {
	System.out.println(myIPAddress());
	sendRequest(" ", " ", 0);
    }
}
