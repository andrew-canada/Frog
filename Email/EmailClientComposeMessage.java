// this code implements only the GUI
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.border.*;
import hsa.*;



// create the class
public class EmailClientComposeMessage implements ActionListener {
     // declare all constants private
    private int RECEIVER_LEN = 9; // this constant will later be moved to Globals.java
    private int SUBJECT_BOX_WIDTH = 51;
    private int TEXT_BOX_HEIGHT = 10;
    private int TEXT_BOX_WIDTH = 70;
    private int FONT_SIZE = 14;
    private JFrame windowFrame;
    private JPanel panel1, panel2, panel3;
    private JLabel toLabel;
    private JLabel subjectLabel;
    private JTextField receiverField;
    private JTextField subjectField;
    private JTextArea messageTextArea;
    private JScrollPane scrollBar;
    private JButton sendButton;
    private JButton cancelButton;



/* declare message text area field as JTextArea
declare scroll bar as JScrollPane
declare send button as JButton
declare cancel button as JButton */
    public EmailClientComposeMessage() {

	// set up the frame
	
	windowFrame = new JFrame("                               ICSMail (Licence expired - please update your licence immediately)");
	windowFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
	windowFrame.setResizable(false);

	/* instantiate window frame with a title
	set the window frame default close operation to DISPOSE_ON_CLOSE
	set a location so the window frame appears centred on screen
	do not allow user to resize window frame */

	 // set up the container with three panels

	Container container = windowFrame.getContentPane();
	container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
	panel1 = new JPanel();
	panel2 = new JPanel();
	panel3 = new JPanel();
	container.add(panel1);
	container.add(panel2);
	container.add(panel3);
	/*declare container as Container and instantiate by assigning frame.getContentPane()
	set the layout of container to BoxLayout aligned by the Y_AXIS
	instantiate the three panels that go on top of the container as JPanel
	add the three panels to the container */

	// set up the first panel
	toLabel = new JLabel("To:");
	receiverField = new JTextField(RECEIVER_LEN);
	//receiverField.paintBorder(Color.blue);
	receiverField.setBorder(new LineBorder(Color.blue,1));
	receiverField.setFont(new Font("Courier New", Font.PLAIN, FONT_SIZE));
	panel1.add(toLabel);
	panel1.add(receiverField);

	subjectLabel = new JLabel("Subject:");
	subjectField = new JTextField(51);
	//subjectField.paintBorder(Color.blue);
	subjectField.setBorder(new LineBorder(Color.blue,1));
	subjectField.setFont(new Font("Courier New", Font.PLAIN, 14));
	panel1.add(subjectLabel);
	panel1.add(subjectField);

	/* instantiate the "To" label
	instantiate the receiver field with a length of 9
	set the border of the receiver field to blue
	set the font of the receiver to "Courier New", PLAIN, 14
	add the "To" label to the first panel
	add the receiver field to the first panel
	instantiate the "Subject" label
	instantiate the subject field with a length of 51
	set the border of the subject field to blue
	set the font of the subject to "Courier New", PLAIN, 14
	add the "Subject" label to the first panel
	add the subject field to the first panel */

	/*windowFrame.pack();
	windowFrame.setVisible(true); */
	// (RUN PROGRAM: do not forget to include here pack() and setVisible())

	// set up the second panel
	messageTextArea = new JTextArea(10, TEXT_BOX_WIDTH);
	messageTextArea.setFont(new Font("Courier New", Font.PLAIN, FONT_SIZE));
	messageTextArea.setLineWrap(true);
	messageTextArea.setWrapStyleWord(true);
	scrollBar = new JScrollPane(messageTextArea,
				    JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				    JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
	scrollBar.setBorder(new LineBorder(Color.blue,1));
	panel2.add(scrollBar);

	/*
	instantiate the message text area with a height of 10 and a width of 70
	set the font of the message text area to "Courier New", PLAIN, 14
	set the message text area to automatic line wrap
	set the line wrap so words do not split at end of line
	instantiate the scroll bar // the scroll bar contains the entire message text area
	set the border of the scroll bar to blue
	add the scroll bar to the second panel
	*/

	// set up the third panel
	sendButton = new JButton("Send");
	cancelButton = new JButton("Cancel");
	panel3.add(sendButton);
	panel3.add(cancelButton);
	/*
	instantiate the send button
	instantiate the receive button
	add the send button to the third panel
	add the cancel button to the third panel
	*/
	sendButton.setActionCommand("Send");
	cancelButton.setActionCommand("Cancel");
	sendButton.addActionListener(this);
	cancelButton.addActionListener(this);
	/* add the send button to the listener using addActionListener(this)
	add the cancel button to the listener using addActionListener(this) */
	// make the whole thing visible

	windowFrame.pack();
	windowFrame.setVisible(true);
	windowFrame.setLocationRelativeTo(null);
	/*
	pack the window frame
	make the window frame visible
	*/

    }
    /*public void actionPerformed(ActionEvent event) {
	Object buttonPressed = event.getSource();
	// declare buttonPressed as an Object and initialize to event.getSource()
	if(buttonPressed.equals(sendButton)) {
	    System.out.println("The send button has been pressed");
	}
	else if(buttonPressed.equals(cancelButton)){
	    System.out.println("The cancel button has been pressed");
	}
    }*/
    public void actionPerformed(ActionEvent event) {
	String eventName = event.getActionCommand();
	if(eventName.equals("Send")) {
	    String text = "" + messageTextArea.getText();
	    String subject = "" + subjectField.getText();
	    String reciever = "" + receiverField.getText();
	    boolean subjectLengthCorrect = true;
	    if(reciever.length() > 9){
		subjectLengthCorrect = false;
	    }
	    if(subjectLengthCorrect){
		String message = StructureMessage.run(reciever, subject, text);
		int errCode = NetIO.sendRequest(message, Globals.SERVER_IP_ADDRESS);
		JOptionPane.showMessageDialog(null, "\"" + message+ "\"" + ". Is this the message you want to send?", "ICSMail", JOptionPane.PLAIN_MESSAGE);
		if(errCode == Globals.NET_OK) {
		    windowFrame.dispose();
		    JOptionPane.showMessageDialog(null, "Message delivered! ", "ICSMail", JOptionPane.PLAIN_MESSAGE);
		} else {
		    // System.out.println("Made it to else statement");
		    JOptionPane.showMessageDialog(null, "Message not delivered. ", "ICSMail", JOptionPane.ERROR_MESSAGE);
		}            
	    } else {
		JOptionPane.showMessageDialog(null, "Error found in reciever ID. ", "ICSMail", JOptionPane.ERROR_MESSAGE);
	    }


	} else if(eventName.equals("Cancel")) {
	    windowFrame.dispose();

	}
    }
    
    public void setReciever(String s) {
	receiverField.setText(s);
    }
    public void setSubject(String s) {
	subjectField.setText(s);
    }


    // main method to test the class
    public static void main(String[] args){
	EmailClientComposeMessage eClient = new EmailClientComposeMessage();
    }
}

class StructureMessage{
    public static String run(String reciever, String subject, String text){
	String senderModified = System.getProperty("user.name");
	for(int i = 0; i < 4; i++) {
	    senderModified = "0" + senderModified;
	}
	String recieverModified = reciever;
	for(int i = 0; i < 9 - reciever.length(); i++) {
	    recieverModified = "0" + recieverModified;
	}
	return Globals.SEND_MESSAGE + senderModified + recieverModified +
	    "00000000" + Globals.FIRST_RECORD_MARKER + subject +
	    Globals.END_OF_SUBJECT_MARKER + text;
    }
    
} 

/*
// this pseudo-code implements functionality to the buttons and checks they are responding correctly
// code in red has already been implemented in previous slides

(RUN PROGRAM)
}

*/
