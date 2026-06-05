public class Init {
    public static void initializeBoxMessages() {
	for (int i = 0; i < Globals.MAX_CLIENT_MESSAGES; i++) {
	    Globals.boxMessages[i] = "";
	}
    }
    
}
