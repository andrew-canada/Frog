package view;

import interface_adapter.login.LoginController;
import interface_adapter.login.LoginViewModel;
import javax.swing.*;
import java.awt.*;

public final class LoginPanel extends JPanel {
    private final JTextField username = new JTextField(18);
    private final JPasswordField password = new JPasswordField(18);
    private final JLabel message = Theme.label("Sign in with an account stored in MongoDB.", 12, Theme.MUTED);
    private Runnable onBack = () -> { };
    private Runnable onSignup = () -> { };

    public LoginPanel(LoginViewModel model, LoginController controller) {
        setLayout(new GridBagLayout());
        setBackground(Theme.CREAM);
        JPanel card = Theme.page();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        //card.setPreferredSize(new Dimension(430, 440));

        java.net.URL logoUrl = getClass().getResource("/images/FlushIDLogoPhoto.png");
        if (logoUrl != null) {
            JLabel logo = new JLabel(new ImageIcon(logoUrl));
            logo.setAlignmentX(Component.LEFT_ALIGNMENT);
            card.add(logo);
            card.add(Box.createVerticalStrut(12));
        }
        card.add(Theme.title("Welcome to FlushID"));
        card.add(Theme.label("Sign in to save preferences and submit reports.", 13, Theme.MUTED));
        card.add(Box.createVerticalStrut(20));
        card.add(new JLabel("Username")); card.add(username);
        card.add(Box.createVerticalStrut(12));
        card.add(new JLabel("Password")); card.add(password);
        card.add(Box.createVerticalStrut(16));

        JButton login = Theme.primary("Log in");
        JButton signup = Theme.button("Create account");
        JButton back = Theme.button("Continue as guest");
        for (JButton button : new JButton[]{login, signup, back}) button.setAlignmentX(Component.LEFT_ALIGNMENT);
        login.addActionListener(event -> controller.execute(username.getText(), new String(password.getPassword())));
        signup.addActionListener(event -> onSignup.run());
        back.addActionListener(event -> onBack.run());
        card.add(login); card.add(Box.createVerticalStrut(8)); card.add(signup);
        card.add(Box.createVerticalStrut(8)); card.add(back); card.add(Box.createVerticalStrut(16)); card.add(message);
        add(card);

        model.addPropertyChangeListener(event -> {
            LoginViewModel.State state = model.getState();
            message.setText(state.message());
            message.setForeground(state.success() ? new Color(37, 125, 80) : Theme.BERRY);
            back.setText(state.success() ? "Back to map" : "Continue as guest");
        });
    }

    public void setOnBack(Runnable action) { onBack = action; }
    public void setOnSignup(Runnable action) { onSignup = action; }
}
