package views;

import java.awt.Color;
import java.awt.Component;
import java.awt.GridBagLayout;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import interface_adapter.login.LoginController;
import interface_adapter.login.LoginViewModel;

public final class LoginPanel extends JPanel {
    private static final String DEFAULT_MESSAGE = "Sign in with an account stored in MongoDB.";
    private final JTextField username = new JTextField(18);
    private final JPasswordField password = new JPasswordField(18);
    private final JLabel message = Theme.label(DEFAULT_MESSAGE, 12, Theme.MUTED);
    private final JButton back = Theme.button("Continue as guest");
    private Runnable onBack = () -> {
    };
    private Runnable onSignup = () -> {
    };

    public LoginPanel(final LoginViewModel model, final LoginController controller) {
        setLayout(new GridBagLayout());
        setBackground(Theme.CREAM);
        final JPanel card = Theme.page();
        // card.setPreferredSize(new Dimension(430, 440));
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));

        final java.net.URL logoUrl = getClass().getResource("/images/FlushIDLogoPhoto.png");
        if (logoUrl != null) {
            final JLabel logo = new JLabel(new ImageIcon(logoUrl));
            logo.setAlignmentX(Component.LEFT_ALIGNMENT);
            card.add(logo);
            card.add(Box.createVerticalStrut(12));
        }
        card.add(Theme.title("Welcome to FlushID"));
        card.add(Theme.label("Sign in to save preferences and submit reports.", 13, Theme.MUTED));
        card.add(Box.createVerticalStrut(20));
        card.add(new JLabel("Username"));
        card.add(username);
        card.add(Box.createVerticalStrut(12));
        card.add(new JLabel("Password"));
        card.add(password);
        card.add(Box.createVerticalStrut(16));

        final JButton login = Theme.primary("Log in");
        final JButton signup = Theme.button("Create account");
        for (final JButton button : new JButton[] {login, signup, back}) {
            button.setAlignmentX(Component.LEFT_ALIGNMENT);
        }
        login.addActionListener(event -> {
            controller.execute(username.getText(), new String(password.getPassword()));
        });
        signup.addActionListener(event -> {
            onSignup.run();
        });
        back.addActionListener(event -> {
            username.setText("");
            password.setText("");
            onBack.run();
        });
        card.add(login);
        card.add(Box.createVerticalStrut(8));
        card.add(signup);
        card.add(Box.createVerticalStrut(8));
        card.add(back);
        card.add(Box.createVerticalStrut(16));
        card.add(message);
        add(card);

        model.addPropertyChangeListener(event -> {
            final LoginViewModel.State state = model.getState();
            if (state
                .message()
                .isBlank()) {
                message.setText(DEFAULT_MESSAGE);
            }
            else {
                message.setText(state.message());
            }
            if (state
                .message()
                .isBlank()) {
                message.setForeground(Theme.MUTED);
            }
            else {
                message.setForeground(state.success() ? new Color(37, 125, 80) : Theme.BERRY);
            }
            if (state.success()) {
                back.setText("View map");
            }
            else {
                back.setText("Continue as guest");
            }
        });
    }

    public void setOnBack(final Runnable action) {
        onBack = action;
    }

    public void setOnSignup(final Runnable action) {
        onSignup = action;
    }
}
