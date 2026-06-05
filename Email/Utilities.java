public class Utilities {
    public static int requestEntry(String name, String password) {
	// System.out.println(name.length());
	while(name.length() < 9) {
	    name = "0" + name;
	}
	if(!( name.length() == 0 || password.length()  == 0)) {
	    String request =  "" + Globals.ENTRY_TO_SYSTEM + Utils.encrypt(name, password);
	    int error = NetIO.sendRequest(request, Globals.SERVER_IP_ADDRESS);
	    if(error == Globals.NET_OK) {
		String response = NetIO.receiveRequest();
		if (response.equals("" + Globals.PROCESS_OK)) {
		    return Globals.PROCESS_OK;
		} else {
		    return Globals.PROCESS_ERROR;
		}
	    } else {
		return Globals.NET_SEND_ERROR;
	    }
	} else {
	    return Globals.PROCESS_ERROR;
	}

    }
    public static void main(String[] args) {
	System.out.println(requestEntry("ian", "BRAVO"));
    }
}
