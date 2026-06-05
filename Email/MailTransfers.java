 

public class MailTransfers {

    public static void sleep(long ms) {
	try{
	    Thread.sleep(ms);
	} catch(Exception e){
	    System.out.println("Sleep failed. ");
	}
    }


    public static int EmailClientRequestAllMail(int whichBox) {
	int error = Globals.NET_RECEIVER_ERROR;
	String identification = NetIO.myUserName();
	while(identification.length() < Globals.CLIENT_ID_LEN) {
	    identification = "0" + identification;
	}
	do {
	    char command;
	    if(whichBox == Globals.RECEIVER_ID) {
		command = Globals.IN_BOX;
	    } else {
		command = Globals.OUTBOX;
	    }
	    String request = command + identification;
	    error = NetIO.sendRequest(request, Globals.SERVER_IP_ADDRESS);
	    if(error == Globals.NET_OK) {
		String allMessages = NetIO.receiveRequest();
		Init.initializeBoxMessages();
		// break up allMessages into individual messages and load them 
		// into Globals.boxMessages (*** see next slide for pseudo-code)
		/*String endOfTransmission = Utils.intToByteStr(Globals.END_OF_MESSAGE);
		String endOfMessage = Utils.intToByteStr(Globals.END_OF_MESSAGES_TRANSMISSION);
		String[] temp = allMessages.split(endOfMessage);
		int end = Math.max(temp.length, Globals.MAX_CLIENT_MESSAGES);
		for(int i = 0; i < end; i++) {
		    Globals.boxMessages[i] = temp[i];
		} */
		int i = 0;
		String endOfTransmission = Utils.intToByteStr(Globals.END_OF_MESSAGES_TRANSMISSION);
		String endOfMessage = Utils.intToByteStr(Globals.END_OF_MESSAGE);
		while (!allMessages.equals(endOfTransmission) && i < Globals.MAX_CLIENT_MESSAGES) {
		    Globals.boxMessages[i] = allMessages.substring(0, allMessages.indexOf(endOfMessage));
		    allMessages = allMessages.substring(allMessages.indexOf(endOfMessage) + Globals.INT_LEN);
		    i++;
		}
	    } 
	    sleep(1000);
	    
	} while(error != Globals.NET_OK);
	return error;
    }
    public static void main(String[] args) {
	System.out.println();
	EmailClientRequestAllMail(Globals.RECEIVER_ID);
	int i = 0;
	while(!(Globals.boxMessages[i].equals(""))) {
	    System.out.println((Globals.boxMessages[i]).substring(0, 19));
	    System.out.println((Globals.boxMessages[i]).substring(27));
	    System.out.println();
	    i++;
	}
    }
}

