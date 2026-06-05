import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.border.*;
import hsa.*;

public class SecurityEntry implements ActionListener {
    private JFrame windowFrame;
    private JPanel panel1, panel2 ,panel3 ,panel4;
    private JLabel usernameLabel, passwordLabel;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton OKButton, cancelButton;
    private JDialog dialog;


    public SecurityEntry(){
	dialog = new JDialog(windowFrame, "Entry - ICS4U0", true);
	dialog.setDefaultCloseOperation (JDialog.DISPOSE_ON_CLOSE);
	dialog.setResizable(false);
	// dialog.setLocationRelativeTo(null);        
	windowFrame = new JFrame();
	Icon photo = new ImageIcon("ICSMail.jpg");
	JLabel photoLabel = new JLabel(photo);   
	Dimension dimension = new Dimension(188, 72);
	photoLabel.setPreferredSize(dimension);
	Container container = dialog.getContentPane();
	container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
	panel1 = new JPanel();
	panel2 = new JPanel();
	panel3 = new JPanel();
	panel4 = new JPanel();
	container.add(panel1);
	container.add(panel2);
	container.add(panel3);
	container.add(panel4);
	
	panel1.add(photoLabel);
	
	usernameLabel = new JLabel("Username: ");
	usernameLabel.setFont(new Font("Consolas", Font.PLAIN, 14));
	usernameField = new JTextField(12);
	usernameField.setFont(new Font("Consolas", Font.PLAIN, 14));
	panel2.add(usernameLabel);
	panel2.add(usernameField);
	
	passwordLabel = new JLabel("Password: ");
	passwordLabel.setFont(new Font("Consolas", Font.PLAIN, 14));
	passwordField = new JPasswordField(12);
	passwordField.setFont(new Font("Consolas", Font.PLAIN, 14));
	panel3.add(passwordLabel);
	panel3.add(passwordField); 
	
	OKButton = new JButton("OK");
	cancelButton = new JButton("Cancel");
	panel4.add(OKButton);
	panel4.add(cancelButton);

	OKButton.setActionCommand("OK");
	cancelButton.setActionCommand("Cancel");
	OKButton.addActionListener(this);
	cancelButton.addActionListener(this);

	
	dialog.pack();
	dialog.setVisible(true);



    }
    public void actionPerformed(ActionEvent event) {
	String eventName = event.getActionCommand();
	if(eventName.equals("OK")) {
	    
	    int errorCode = Utilities.requestEntry(usernameField.getText(), passwordField.getText());
	    if(errorCode == Globals.PROCESS_OK) {
		Globals.entryGranted = true;
		((Window) dialog.getParent()).dispose();
	    } else if(errorCode == Globals.PROCESS_ERROR) {
		// System.out.println("Username/Password error. ");
		// ((Window) dialog.getParent()).dispose();
	    } else if(errorCode == Globals.NET_SEND_ERROR) {
		// System.out.println("Network error. ");
		// ((Window) dialog.getParent()).dispose();
	    } else {
		// System.out.println("Username/Password error. ");
		// ((Window) dialog.getParent()).dispose();
	    }
	} else if(eventName.equals("Cancel")) {
	    // System.out.println("Cancel pressed. ");
	    ((Window) dialog.getParent()).dispose();
	}
	
    }
    public static void main(String[] args) {
	SecurityEntry sEntry = new SecurityEntry();
    }
}
