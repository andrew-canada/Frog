package view;

import interface_adapter.login.LoginController;
import interface_adapter.login.LoginViewModel;

import java.awt.event.ActionListener;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.border.*;
import java.io.File;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

public class LoginView implements ActionListener {
    private JFrame windowFrame;
    private final JPanel panel1, panel2, panel3, panel4;
    private final JLabel usernameLabel, passwordLabel;
    private final JTextField usernameField;
    private final JPasswordField passwordField;
    private final JButton OKButton, cancelButton, continueAsGuestButton;
    private final JDialog dialog;

    private LoginController loginController;
    private LoginViewModel loginViewModel;


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

public final class LoginView extends JPanel {
    private final JTextField username = new JTextField("sheena_q", 18);
    private final JPasswordField password = new JPasswordField("demo", 18);
    private final JLabel message = Theme.label("Demo account: sheena_q / demo", 12, Theme.MUTED);
    private Runnable onBack = () -> {}, onSignup = () -> {};

    public LoginView(LoginViewModel model, LoginController controller) {
        setLayout(new GridBagLayout());
        setBackground(Theme.CREAM);

        JPanel card = Theme.page();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setPreferredSize(new Dimension(380, 340));
        
        card.add(Theme.title("Welcome to FlushID"));
        card.add(Theme.label("Sign in to save preferences and see your reviews.", 13, Theme.MUTED));
        card.add(Box.createVerticalStrut(24));
        
        card.add(new JLabel("Username"));
        card.add(username);
        card.add(Box.createVerticalStrut(12));
        
        card.add(new JLabel("Password"));
        card.add(password);
        card.add(Box.createVerticalStrut(16));
        
        JButton login = Theme.primary("Log in");
        JButton signup = Theme.button("Create account");
        JButton back = Theme.button("Back to map");
        
        login.setAlignmentX(LEFT_ALIGNMENT);
        signup.setAlignmentX(LEFT_ALIGNMENT);
        back.setAlignmentX(LEFT_ALIGNMENT);
        
        login.addActionListener(e -> controller.execute(username.getText(), new String(password.getPassword())));
        signup.addActionListener(e -> onSignup.run());
        back.addActionListener(e -> onBack.run());
        
        card.add(login);
        card.add(Box.createVerticalStrut(8));
        card.add(signup);
        card.add(Box.createVerticalStrut(8));
        card.add(back);
        card.add(Box.createVerticalStrut(16));
        card.add(message);
        
        add(card);

        model.addPropertyChangeListener(e -> {
            LoginViewModel.State s = model.getState();
            message.setText(s.message());
            message.setForeground(s.success() ? new Color(37, 125, 80) : Theme.BERRY);
        });
    }

    public void setOnBack(Runnable r) { 
        this.onBack = r; 
    }

    public void setOnSignup(Runnable r) { 
        this.onSignup = r; 
    }

    // Optional: Keep main method from demo branch for standalone testing
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Test LoginView");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            LoginViewModel loginViewModel = new LoginViewModel();
            
            // Pass null or a stub controller for testing
            LoginView loginView = new LoginView(loginViewModel, null);
            
            frame.add(loginView);
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}
}
