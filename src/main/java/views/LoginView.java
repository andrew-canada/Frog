package views;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
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
    private static final int LOGO_WIDTH = 760;
    private static final int LOGO_HEIGHT = 180;
    private final JPanel panel1 = new JPanel();
    private final JPanel panel2 = new JPanel();
    private final JPanel panel3 = new JPanel();
    private final JPanel panel4 = new JPanel();
    private final JLabel usernameLabel = new JLabel("Username: ");
    private final JLabel passwordLabel = new JLabel("Password: ");
    private final JTextField usernameField = new JTextField(TEXT_FIELD_COLUMNS);
    private final JPasswordField passwordField = new JPasswordField(TEXT_FIELD_COLUMNS);
    private final JButton okButton = new JButton(FIELD_OK);
    private final JButton cancelButton = new JButton(FIELD_CANCEL);
    private final JButton continueAsGuestButton = new JButton(FIELD_CONTINUE_AS_GUEST);
    private final JDialog dialog;
    private final LoginViewModel loginViewModel;
    private JFrame windowFrame;
    private LoginController loginController;

    LoginView(final LoginViewModel loginViewModel) {
        this.loginViewModel = loginViewModel;
        dialog = new JDialog(windowFrame, "Entry - FlushID", true);
        initializeDialog();
    }

    private void initializeDialog() {
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setResizable(false);
        dialog.setLocationRelativeTo(null);
        windowFrame = new JFrame();
        final Container container = dialog.getContentPane();
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        container.add(panel1);
        container.add(panel2);
        container.add(panel3);
        container.add(panel4);
        addLogo();
        addCredentials();
        addButtons();
        dialog.pack();
        dialog.setVisible(true);
    }

    private void addLogo() {
        final ImageIcon photo = new ImageIcon(getClass().getResource("/images/FlushIDLogoPhoto.png"));
        final JLabel photoLabel = new JLabel(photo);
        photoLabel.setPreferredSize(new Dimension(LOGO_WIDTH, LOGO_HEIGHT));
        panel1.add(photoLabel);
    }

    private void addCredentials() {
        usernameLabel.setFont(new Font(FIELD_CONSOLAS, Font.PLAIN, LABEL_FONT_SIZE));
        usernameField.setFont(new Font(FIELD_CONSOLAS, Font.PLAIN, LABEL_FONT_SIZE));
        panel2.add(usernameLabel);
        panel2.add(usernameField);
        passwordLabel.setFont(new Font(FIELD_CONSOLAS, Font.PLAIN, LABEL_FONT_SIZE));
        passwordField.setFont(new Font(FIELD_CONSOLAS, Font.PLAIN, LABEL_FONT_SIZE));
        panel3.add(passwordLabel);
        panel3.add(passwordField);
    }

    private void addButtons() {
        panel4.add(okButton);
        panel4.add(cancelButton);
        panel4.add(continueAsGuestButton);
        okButton.setActionCommand(FIELD_OK);
        cancelButton.setActionCommand(FIELD_CANCEL);
        continueAsGuestButton.setActionCommand(FIELD_CONTINUE_AS_GUEST);
        okButton.addActionListener(this);
        cancelButton.addActionListener(this);
        continueAsGuestButton.addActionListener(this);
    }

    @Override
    public void actionPerformed(final ActionEvent event) {
        final String eventName = event.getActionCommand();
        if (eventName.equals(FIELD_OK)) {
            if (loginController != null) {
                loginController.execute(usernameField.getText(), String.valueOf(passwordField.getPassword()));
            }
        }
        else if (eventName.equals(FIELD_CANCEL) || eventName.equals(FIELD_CONTINUE_AS_GUEST)) {
            dialog.dispose();
        }
    }

    public void setLoginController(final LoginController loginController) {
        this.loginController = loginController;
    }
}
