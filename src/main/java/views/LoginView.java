package views;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import interface_adapter.login.LoginController;
import interface_adapter.login.LoginViewModel;

final class LoginView implements ActionListener {
    private static final String FIELD_CONSOLAS = "Consolas";
    private static final String FIELD_OK = "OK";
    private static final String FIELD_CANCEL = "Cancel";
    private static final String FIELD_CONTINUE_AS_GUEST = "Continue as guest";
    private static final int LABEL_FONT_SIZE = 14;
    private static final int TEXT_FIELD_COLUMNS = 12;
    private final JPanel panel1;
    private final JPanel panel2;
    private final JPanel panel3;
    private final JPanel panel4;
    private final JLabel usernameLabel;
    private final JLabel passwordLabel;
    private final JTextField usernameField;
    private final JPasswordField passwordField;
    private final JButton OKButton;
    private final JButton cancelButton;
    private final JButton continueAsGuestButton;
    private final JDialog dialog;
    private final LoginViewModel loginViewModel;
    private JFrame windowFrame;
    private LoginController loginController;

    LoginView(final LoginViewModel loginViewModel) {

        // this.loginViewModel.addPropertyChangeListener(this);

        this.loginViewModel = loginViewModel;

        dialog = new JDialog(windowFrame, "Entry - FlushID", true);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setResizable(false);
        dialog.setLocationRelativeTo(null);
        windowFrame = new JFrame();

        final Container container = dialog.getContentPane();
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        panel1 = new JPanel();
        panel2 = new JPanel();
        panel3 = new JPanel();
        panel4 = new JPanel();

        container.add(panel1);
        container.add(panel2);
        container.add(panel3);
        container.add(panel4);

        final ImageIcon photo = new ImageIcon(getClass().getResource("/images/FlushIDLogoPhoto.png"));
        final JLabel photoLabel = new JLabel(photo);
        final Dimension dimension = new Dimension(760, 180);
        photoLabel.setPreferredSize(dimension);

        panel1.add(photoLabel);

        usernameLabel = new JLabel("Username: ");
        usernameLabel.setFont(new Font(FIELD_CONSOLAS, Font.PLAIN, LABEL_FONT_SIZE));
        usernameField = new JTextField(TEXT_FIELD_COLUMNS);
        usernameField.setFont(new Font(FIELD_CONSOLAS, Font.PLAIN, LABEL_FONT_SIZE));
        panel2.add(usernameLabel);
        panel2.add(usernameField);

        passwordLabel = new JLabel("Password: ");
        passwordLabel.setFont(new Font(FIELD_CONSOLAS, Font.PLAIN, LABEL_FONT_SIZE));
        passwordField = new JPasswordField(TEXT_FIELD_COLUMNS);
        passwordField.setFont(new Font(FIELD_CONSOLAS, Font.PLAIN, LABEL_FONT_SIZE));
        panel3.add(passwordLabel);
        panel3.add(passwordField);

        OKButton = new JButton(FIELD_OK);
        cancelButton = new JButton(FIELD_CANCEL);
        continueAsGuestButton = new JButton(FIELD_CONTINUE_AS_GUEST);
        panel4.add(OKButton);
        panel4.add(cancelButton);
        panel4.add(continueAsGuestButton);

        OKButton.setActionCommand(FIELD_OK);
        cancelButton.setActionCommand(FIELD_CANCEL);
        continueAsGuestButton.setActionCommand(FIELD_CONTINUE_AS_GUEST);
        OKButton.addActionListener(this);
        cancelButton.addActionListener(this);
        continueAsGuestButton.addActionListener(this);

        dialog.pack();
        dialog.setVisible(true);

    }

    @Override
    public void actionPerformed(final ActionEvent event) {

        final String eventName = event.getActionCommand();
        if (eventName.equals(FIELD_OK)) {
            // int errorCode = Utilities.requestEntry(usernameField.getText(), passwordField.getText());
            // LoginController.execute()
                /*if(errorCode == Globals.PROCESS_OK) {
                    Globals.entryGranted = true;
                    ((Window) dialog.getParent()).dispose();
                // System.out.println("Username/Password error. ");
                } else if(errorCode == Globals.PROCESS_ERROR) {
                   // ((Window) dialog.getParent()).dispose();

                // System.out.println("Network error. ");
                } else if(errorCode == Globals.NET_SEND_ERROR) {
                   // ((Window) dialog.getParent()).dispose();

                // System.out.println("Username/Password error. ");
                } else {
                   // ((Window) dialog.getParent()).dispose();

                }

                 */
        }
        else if (eventName.equals(FIELD_CANCEL)) {
           // System.out.println("Cancel pressed. ");

            ((Window) dialog.getParent()).dispose();
        }
        else if (eventName.equals(FIELD_CONTINUE_AS_GUEST)) {

        }

    }

    public void setLoginController(final LoginController loginController) {
        this.loginController = loginController;
    }

}
