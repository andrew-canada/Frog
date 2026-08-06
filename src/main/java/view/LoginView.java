package view;

import interface_adapter.login.LoginController;
import interface_adapter.login.LoginViewModel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public final class LoginView implements ActionListener {
    private final JPanel panel1, panel2, panel3, panel4;
    private final JLabel usernameLabel, passwordLabel;
    private final JTextField usernameField;
    private final JPasswordField passwordField;
    private final JButton OKButton, cancelButton, continueAsGuestButton;
    private final JDialog dialog;
    private JFrame windowFrame;
    private LoginController loginController;
    private final LoginViewModel loginViewModel;


    public LoginView(LoginViewModel loginViewModel) {

        this.loginViewModel = loginViewModel;
        // this.loginViewModel.addPropertyChangeListener(this);


        dialog = new JDialog(windowFrame, "Entry - FlushID", true);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setResizable(false);
        dialog.setLocationRelativeTo(null);
        windowFrame = new JFrame();

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

        ImageIcon photo = new ImageIcon(getClass().getResource("/images/FlushIDLogoPhoto.png"));
        JLabel photoLabel = new JLabel(photo);
        Dimension dimension = new Dimension(760, 180);
        photoLabel.setPreferredSize(dimension);

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
        continueAsGuestButton = new JButton("Continue as guest");
        panel4.add(OKButton);
        panel4.add(cancelButton);
        panel4.add(continueAsGuestButton);

        OKButton.setActionCommand("OK");
        cancelButton.setActionCommand("Cancel");
        continueAsGuestButton.setActionCommand("Continue as guest");
        OKButton.addActionListener(this);
        cancelButton.addActionListener(this);
        continueAsGuestButton.addActionListener(this);

        dialog.pack();
        dialog.setVisible(true);


    }

    public void actionPerformed(ActionEvent event) {

        String eventName = event.getActionCommand();
        if (eventName.equals("OK")) {
            // LoginController.execute()

            //int errorCode = Utilities.requestEntry(usernameField.getText(), passwordField.getText());
                /*if(errorCode == Globals.PROCESS_OK) {
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

                 */
        } else if (eventName.equals("Cancel")) {
            // System.out.println("Cancel pressed. ");
            ((Window) dialog.getParent()).dispose();
        } else if (eventName.equals("Continue as guest")) {

        }

    }

    public void setLoginController(LoginController loginController) {
        this.loginController = loginController;
    }

}
