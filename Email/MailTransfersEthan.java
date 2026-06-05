public class MailTransfersEthan {
    public static int EmailClientRequestAllMail(int whichBox) {
        int error = Globals.NET_RECEIVE_ERROR;
        String identification = NetIO.myUserName();
        while (identification.length() < Globals.CLIENT_ID_LEN) {
                identification = "0" + identification;
        }
        
        do {
            char command = whichBox == Globals.RECEIVER_ID ? Globals.IN_BOX : Globals.OUTBOX;
            String request = command + identification;
            
            error = NetIO.sendRequest(request, Globals.SERVER_IP_ADDRESS);
            
            if (error == Globals.NET_OK) {
                String allMessages = NetIO.receiveRequest(); 
                
                Init.initializeBoxMessages();
                
                int i = 0;
                String endOfTransmission = Utils.intToByteStr(Globals.END_OF_MESSAGES_TRANSMISSION);
                String endOfMessage = Utils.intToByteStr(Globals.END_OF_MESSAGE);
                while (!allMessages.equals(endOfTransmission) && i < Globals.MAX_CLIENT_MESSAGES) {
                    Globals.boxMessages[i] = allMessages.substring(0, allMessages.indexOf(endOfMessage));
                    allMessages = allMessages.substring(allMessages.indexOf(endOfMessage) + Globals.INT_LEN);
                    i++;
                }
            }
            Utils.delay(1000);
        } while (error != Globals.NET_OK);
        return error;
    }
    
    public static void main(String[] args) {
        int error = EmailClientRequestAllMail(Globals.RECEIVER_ID);
        
        int i = 0;
        while (!Globals.boxMessages[i].equals("")) {
            System.out.print(Globals.boxMessages[i].substring(0, 18));
            System.out.println(Globals.boxMessages[i].substring(26));
            i++;
        }
    }
}
