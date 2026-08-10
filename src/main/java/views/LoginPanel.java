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
    private static final int SUCCESS_COLOR_BLUE = 80;
    private static final int SUCCESS_COLOR_GREEN = 125;
    private static final int SUCCESS_COLOR_RED = 37;
    private static final int LARGE_VERTICAL_GAP = 16;
    private static final int SMALL_VERTICAL_GAP = 8;
    private static final int MEDIUM_VERTICAL_GAP = 12;
    private static final int SECTION_VERTICAL_GAP = 20;
    private static final int BODY_FONT_SIZE = 13;
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
        add(buildCard(controller));

        model.addPropertyChangeListener(event -> {
            updateMessage(model.getState());
        });
    }

    private JPanel buildCard(final LoginController controller) {
        final JPanel card = Theme.page();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        addLogo(card);
        card.add(Theme.title("Welcome to FlushID"));
        card.add(Theme.label("Sign in to save preferences and submit reports.", BODY_FONT_SIZE, Theme.MUTED));
        card.add(Box.createVerticalStrut(SECTION_VERTICAL_GAP));
        card.add(new JLabel("Username"));
        card.add(username);
        card.add(Box.createVerticalStrut(MEDIUM_VERTICAL_GAP));
        card.add(new JLabel("Password"));
        card.add(password);
        card.add(Box.createVerticalStrut(LARGE_VERTICAL_GAP));
        final Buttons buttons = createButtons(controller);
        card.add(buttons.login());
        card.add(Box.createVerticalStrut(SMALL_VERTICAL_GAP));
        card.add(buttons.signup());
        card.add(Box.createVerticalStrut(SMALL_VERTICAL_GAP));
        card.add(back);
        card.add(Box.createVerticalStrut(LARGE_VERTICAL_GAP));
        card.add(message);
        return card;
    }

    private void addLogo(final JPanel card) {
        final java.net.URL logoUrl = getClass().getResource("/images/FlushIDLogoPhoto.png");
        if (logoUrl != null) {
            final JLabel logo = new JLabel(new ImageIcon(logoUrl));
            logo.setAlignmentX(Component.LEFT_ALIGNMENT);
            card.add(logo);
            card.add(Box.createVerticalStrut(MEDIUM_VERTICAL_GAP));
        }
    }

    private Buttons createButtons(final LoginController controller) {
        final JButton login = Theme.primary("Log in");
        final JButton signup = Theme.button("Create account");
        login.setAlignmentX(Component.LEFT_ALIGNMENT);
        signup.setAlignmentX(Component.LEFT_ALIGNMENT);
        back.setAlignmentX(Component.LEFT_ALIGNMENT);
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
        return new Buttons(login, signup);
    }

    private void updateMessage(final LoginViewModel.State state) {
        final boolean blank = state.message().isBlank();
        final String messageText;
        if (blank) {
            messageText = DEFAULT_MESSAGE;
        }
        else {
            messageText = state.message();
        }
        message.setText(messageText);
        final Color color;
        if (blank) {
            color = Theme.MUTED;
        }
        else if (state.success()) {
            color = new Color(SUCCESS_COLOR_RED, SUCCESS_COLOR_GREEN, SUCCESS_COLOR_BLUE);
        }
        else {
            color = Theme.BERRY;
        }
        message.setForeground(color);
        final String backText;
        if (state.success()) {
            backText = "View map";
        }
        else {
            backText = "Continue as guest";
        }
        back.setText(backText);
    }

    public void setOnBack(final Runnable action) {
        onBack = action;
    }

    public void setOnSignup(final Runnable action) {
        onSignup = action;
    }

    private record Buttons(JButton login, JButton signup) {
    }
}
