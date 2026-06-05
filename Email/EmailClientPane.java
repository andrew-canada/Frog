import javax.swing.*;
import java.awt.*;
import javax.swing.event.*;
import java.util.Date;

public class EmailClientPane extends JPanel implements ListSelectionListener {
    private String[] messageHeaders = new String[Globals.MAX_CLIENT_MESSAGES];
    private String messageText = "";
    
    public JList messageHeadersJList = null;
    private JTextArea messageJTextArea = null;
    
    private JScrollPane messageHeadersJScrollPane = null;
    private JScrollPane messageTextJScrollPane = null;

    private JSplitPane splitPane = null;
    private int itemIndex = -1;
    private int previouslySelectedItemIndex = 0; // maintains the last selected item; needed in case the user goes beyond the list
    
    public EmailClientPane() {
    }
    
    public EmailClientPane(int whatId) {
	itemIndex = Globals.boxMessages.length == 0 ? - 1 : 0;
	initializeMessageHeaders(whatId);
	
	//Create the list of messages and put it in a scroll pane.
	messageHeadersJList = new JList(messageHeaders);
	messageHeadersJList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
	messageHeadersJList.setSelectedIndex(0);
	messageHeadersJList.setFont(new Font("Courier New", Font.PLAIN, 14));
    
	messageHeadersJList.addListSelectionListener(this);
	messageHeadersJScrollPane = new JScrollPane(messageHeadersJList);
    
	int endOfSubjectMarker = Globals.boxMessages[0].indexOf(Globals.END_OF_SUBJECT_MARKER, Globals.SUBJECT_POS + 1);
	messageText = Globals.boxMessages[0].substring( endOfSubjectMarker + 1);

	messageJTextArea = new JTextArea(messageText);
	messageJTextArea.setLineWrap(true);
	messageJTextArea.setWrapStyleWord(true);
	messageJTextArea.setEditable(false);
	messageJTextArea.setFont(new Font("Courier New", Font.PLAIN, 14));
	messageTextJScrollPane = new JScrollPane(messageJTextArea);
    
	//Create a split pane with the two scroll panes in it.
	splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, messageHeadersJScrollPane, messageTextJScrollPane);
	splitPane.setOneTouchExpandable(true);
	splitPane.setDividerLocation(250);
    
	//Provide minimum sizes for the two components in the split pane.
	Dimension minimumSize = new Dimension(800, 50);
	messageHeadersJScrollPane.setMinimumSize(minimumSize);
	messageTextJScrollPane.setMinimumSize(minimumSize);
    
	//Provide a preferred size for the split pane.
	splitPane.setPreferredSize(new Dimension(800, 400));
    }
    
    // initialize to From       Subject             Date Received     
    public void initializeMessageHeaders(int whatId) {
	for (int i = 0; i < Globals.boxMessages.length; i++) {
	    if( !(Globals.boxMessages[i]).equals(Globals.EMPTY_CLIENT_MESSAGE) ) {
		String dateTime = (Globals.boxMessages[i]).substring(Globals.DATE_TIME_POS, Globals.FIRST_RECORD_MARKER_POS);
		Date date = new Date(Utils.bytesStrToLong(dateTime));
		dateTime = date.toString();
		System.out.println((Globals.boxMessages[i]).indexOf(Globals.END_OF_SUBJECT_MARKER, Globals.SUBJECT_POS + 1));
		int endOfSubjectMarker = (Globals.boxMessages[i]).indexOf(Globals.END_OF_SUBJECT_MARKER, Globals.SUBJECT_POS + 1);
		if(whatId == Globals.RECEIVER_ID ) {
		    String cleanedName = cleanString((Globals.boxMessages[i]).substring(Globals.SENDER_POS, Globals.RECEIVER_POS));
		    messageHeaders[i] = cleanedName;
		}else{
		    String cleanedName = cleanString((Globals.boxMessages[i]).substring(Globals.RECEIVER_POS, Globals.DATE_TIME_POS));
		    messageHeaders[i] = cleanedName;
		}
		    messageHeaders[i] += "  ";
		    messageHeaders[i] += dateTime;
		    messageHeaders[i] += "  ";
		    messageHeaders[i] += (Globals.boxMessages[i]).substring(Globals.FIRST_RECORD_MARKER_POS + 1, endOfSubjectMarker);
		
	    } else {
		messageHeaders[i] = Globals.EMPTY_CLIENT_MESSAGE;
	    }
	}
    }
    
    public int getItemIndex() {
	return itemIndex;
    }
    
    public void valueChanged(ListSelectionEvent e) {
	itemIndex = messageHeadersJList.getSelectedIndex();
	if (!Globals.boxMessages[itemIndex].equals(Globals.STR_NULL)) {
	    int endOfSubjectMarker = Globals.boxMessages[itemIndex].indexOf(Globals.END_OF_SUBJECT_MARKER, Globals.SUBJECT_POS + 1);
	    messageText = Globals.boxMessages[itemIndex].substring(endOfSubjectMarker + 1);
	    messageJTextArea.setText(messageText);
	    previouslySelectedItemIndex = itemIndex;
	}
	else {
	    messageHeadersJList.setSelectedIndex(previouslySelectedItemIndex);
	}
    } 
    
    public JSplitPane getSplitPane() {
	return splitPane;
    }
   
    public String getMessageText() {
	return messageText;
    }

    
    public static String cleanString(String s) {
	while(s.charAt(0) == '0') {
	    s = s.substring(1) + ' ';
	}
	return s;
    }
}
