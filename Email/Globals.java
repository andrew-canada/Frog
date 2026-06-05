public class Globals{
    public static final int NET_SEND_ERROR = -1;
    public static final int NET_OK = 0;
    public static final int SENDING_ATTEMPTS_LIMIT = 3;
    public static final int PORT_NUMBER = 5000;
    public static final int TIME_OUT = 200;
    public static final int NET_RECEIVE_ERROR = -2;
    public static final int NET_RECEIVER_ERROR = NET_RECEIVE_ERROR; // AHH MR P
    public static final int SERVER_QUEUE_LENGTH = 100;
    public static final int PROCESS_ERROR = -3;
    public static final int PROCESS_OK = 0;
    public static final int SENDER_ID = 0;
    public static final int RECEIVER_ID = 1;
    public static final int END_OF_MESSAGE = -1;
    public static final int END_OF_MESSAGES_TRANSMISSION = -2;
    public static final int MAX_CLIENT_MESSAGES = 500;
    public static final int INT_LEN = 4;
    
    public static final char SEND_MESSAGE = 'S';
    public static final char DELETE_MESSAGE = 'D';
    public static final char IN_BOX = 'I';
    public static final char OUT_BOX = 'O'; // GODDAMIT STANDARDIZE
    public static final char OUTBOX = OUT_BOX;
    public static final char FIRST_RECORD_MARKER  = '@';
    public static final char END_OF_SUBJECT_MARKER = '~';
    public static final char DELETED = '*';
    public static final char ENTRY_TO_SYSTEM = 'E';
    public static final String SERVER_IP_ADDRESS = "10.207.98.170";
    public static final String STR_NULL = "";
    public static final String EMPTY_CLIENT_MESSAGE = "";
    public static boolean entryGranted = false;
    public static String[] boxMessages = new String[500];
    // position constants for message
    // Structure of message:
    // command + sender + receiver + timeStamp + first record marker + subject + end of subject marker + message text
       
    public static final int COMMAND_POS = 0;
    public static final int COMMAND_LEN = 1;
    
    public static final int CLIENT_POS = COMMAND_POS + COMMAND_LEN;   // these two are here separately because sometimes the client's request contains     
    public static final int CLIENT_ID_LEN = 9;                          // only C + sender or C + receiver, in which case the position of sender and receiver are equal
    
    public static final int SENDER_POS  = COMMAND_POS + COMMAND_LEN;
    public static final int SENDER_LEN  = CLIENT_ID_LEN;
    public static final int RECEIVER_POS = SENDER_POS + SENDER_LEN;
    public static final int RECEIVER_LEN  = CLIENT_ID_LEN;
    public static final int DATE_TIME_POS = RECEIVER_POS + RECEIVER_LEN;
    public static final int DATE_TIME_LEN = 8;  // long current milliseconds coded as eight bytes
    public static final int FIRST_RECORD_MARKER_POS = DATE_TIME_POS + DATE_TIME_LEN;
    public static final int FIRST_RECORD_MARKER_LEN = 1;
    public static final int SUBJECT_POS = FIRST_RECORD_MARKER_POS + FIRST_RECORD_MARKER_LEN;
    public static final int END_OF_SUBJECT_MARKER_LEN = 1;
    public static final int IDENTIFICATION_LEN = SENDER_LEN + RECEIVER_LEN + DATE_TIME_LEN;

}
