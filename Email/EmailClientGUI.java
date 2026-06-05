import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class EmailClientGUI implements ActionListener {
    private JFrame frame = null;
    private JPanel panel1 = null;
    private JPanel panel2 = null;
    private JPanel panel3 = null;
    
    private EmailClientPane eMailClientPane = null;
    
    private JButton fromOrTo = null;
    private JButton dateTime = null;
    private JButton subject  = null;
    private JLabel  photo    = null;
    private JButton compose  = null;
    private JButton reply    = null;
    private JButton delete   = null;
    private JButton inBox    = null;
    private JButton outBox   = null;
    private JButton serverShutdown = null;
    
    private Icon triangleUp = null;
    private Icon triangleDn = null;
    private Icon myPhoto    = null;
    
    private int currentScreen = Globals.RECEIVER_ID;
    private int inBoxSort  = Globals.DATE_TIME_POS;
    private int outBoxSort = Globals.DATE_TIME_POS; 
    
    private boolean inBoxSortOrderAscending  = true;
    private boolean outBoxSortOrderAscending = true;
    
    private void drawTopButtons() {
	fromOrTo.setIcon(null);
	dateTime.setIcon(null);
	subject.setIcon(null);
	
	if (currentScreen == Globals.RECEIVER_ID) {
	    fromOrTo.setText("From");
	    switch (inBoxSort) {
		case Globals.SENDER_POS    : fromOrTo.setIcon(inBoxSortOrderAscending ? triangleUp : triangleDn);
					     break;
		case Globals.DATE_TIME_POS : dateTime.setIcon(inBoxSortOrderAscending ? triangleUp : triangleDn);
					     break;
		case Globals.SUBJECT_POS   : subject.setIcon(inBoxSortOrderAscending ? triangleUp : triangleDn);
					     break;
	    }
	    
	}
	else {
	    fromOrTo.setText("To");
	    switch (outBoxSort) {
		case Globals.RECEIVER_POS  : fromOrTo.setIcon(outBoxSortOrderAscending ? triangleUp : triangleDn);
					     break;
		case Globals.DATE_TIME_POS : dateTime.setIcon(outBoxSortOrderAscending ? triangleUp : triangleDn);
					     break;
		case Globals.SUBJECT_POS   : subject.setIcon(outBoxSortOrderAscending ? triangleUp : triangleDn);
					     break;
	    }
	}
    }

    public EmailClientGUI() {  
	currentScreen = Globals.RECEIVER_ID; 
	
	frame = new JFrame("ICSMail (Licence expired - please update your licence immediately)");
	frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
	frame.setResizable(false);

	Container contentPane = frame.getContentPane();
	BoxLayout contentPaneLayout = new BoxLayout(contentPane, BoxLayout.Y_AXIS);
	contentPane.setLayout(contentPaneLayout);
	
	panel1 = new JPanel();
	panel2 = new JPanel();
	panel3 = new JPanel();
	
	contentPane.add(panel1);
	contentPane.add(panel2);
	contentPane.add(panel3);
	
	// set up the first panel
	FlowLayout panel1Layout = new FlowLayout(FlowLayout.LEFT);
	panel1.setLayout(panel1Layout);
	
	triangleUp = new ImageIcon("triangleUp.jpg");
	triangleDn = new ImageIcon("triangleDn.jpg");
	
	fromOrTo = new JButton("From");
	fromOrTo.setHorizontalTextPosition(JButton.LEFT);
	fromOrTo.setPreferredSize(new Dimension(85, 14));
	
	dateTime  = new JButton("Date Sent");
	dateTime.setHorizontalTextPosition(JButton.LEFT);
	dateTime.setPreferredSize(new Dimension(235, 14));
	
	subject = new JButton("Subject");
	subject.setHorizontalTextPosition(JButton.LEFT);
	subject.setPreferredSize(new Dimension(468, 14));
	
	drawTopButtons();

	fromOrTo.addActionListener(this);
	dateTime.addActionListener(this);
	subject.addActionListener(this);

	panel1.add(fromOrTo);
	panel1.add(dateTime);
	panel1.add(subject);
	
	// set up the second panel
	FlowLayout panel2Layout = new FlowLayout(FlowLayout.LEFT);
	panel2.setLayout(panel2Layout);
	currentScreen = Globals.RECEIVER_ID;
	eMailClientPane = new EmailClientPane(currentScreen);
	panel2.add(eMailClientPane.getSplitPane());
	
	// set up the third panel
	FlowLayout panel3Layout = new FlowLayout(FlowLayout.CENTER);
	panel3.setLayout(panel3Layout);

	compose = new JButton("Compose");
	reply   = new JButton("Reply");
	inBox   = new JButton("InBox");
	outBox  = new JButton("OutBox");
	delete  = new JButton("Delete");

	compose.addActionListener(this);
	reply.addActionListener(this);
	inBox.addActionListener(this);
	outBox.addActionListener(this);
	delete.addActionListener(this);
	
	panel3.add(compose);
	panel3.add(reply);
	panel3.add(inBox);
	panel3.add(outBox);
	panel3.add(delete);
	
	frame.pack();
	frame.setLocationRelativeTo(null);      
	frame.setVisible(true);
    }
    
    private void refreshPane() {
	drawTopButtons();        
	panel2.remove(eMailClientPane.getSplitPane());
	eMailClientPane = new EmailClientPane(currentScreen);
	panel2.add(eMailClientPane.getSplitPane());
	frame.pack();
	frame.setVisible(true);              
    }
    
    public void actionPerformed(ActionEvent event) {
	Object buttonPressed = event.getSource();
	if(buttonPressed.equals(compose)) {
	    EmailClientComposeMessage c = new EmailClientComposeMessage();
	} else if(buttonPressed.equals(inBox)) {
	    int error = MailTransfers.EmailClientRequestAllMail(Globals.RECEIVER_ID);
	    if(error == Globals.PROCESS_OK) {
		String fromOrTo = "From";
		// A call to sorting that will happen later
		refreshPane();
	    } else {
		System.out.println("***fatal error: Globals.boxMessages[] is now not consistent with display");
	      
	    }
	} else if(buttonPressed.equals(outBox)) {
	    int error = MailTransfers.EmailClientRequestAllMail(Globals.SENDER_ID);
	    if(error == Globals.PROCESS_OK) {
		String fromOrTo = "To";
		// A call to sorting that will happen later
		refreshPane();
	    } else {
		System.out.println("***fatal error: Globals.boxMessages[] is now not consistent with display");
	      
	    }            
	} else if(buttonPressed.equals(reply)) {
	    System.out.println(eMailClientPane.messageHeadersJList.getSelectedValue().toString());
	    int endOfSubjectMarker = eMailClientPane.messageHeadersJList.getSelectedValue().toString().indexOf(Globals.END_OF_SUBJECT_MARKER, Globals.SUBJECT_POS);
	    System.out.println(endOfSubjectMarker);
	    String a = eMailClientPane.messageHeadersJList.getSelectedValue().toString().substring(Globals.SENDER_POS - 1, Globals.RECEIVER_POS - 1);
	    String b = eMailClientPane.messageHeadersJList.getSelectedValue().toString().substring(Globals.FIRST_RECORD_MARKER_POS + 1, endOfSubjectMarker);
	    EmailClientComposeMessage c = new EmailClientComposeMessage();
	    c.setReciever(a);
	    c.setSubject(b);
	}
    }
}














