package view;

import interface_adapter.login.LoginController;
import interface_adapter.login.LoginViewModel;
import javax.swing.*;
import java.awt.*;

public final class LoginView extends JPanel{
    private final JTextField username=new JTextField("sheena_q",18);private final JPasswordField password=new JPasswordField("demo",18);
    private final JLabel message=Theme.label("Demo account: sheena_q / demo",12,Theme.MUTED);private Runnable onBack=()->{},onSignup=()->{};
    public LoginView(LoginViewModel model,LoginController controller){setLayout(new GridBagLayout());setBackground(Theme.CREAM);JPanel card=Theme.page();card.setLayout(new BoxLayout(card,BoxLayout.Y_AXIS));card.setPreferredSize(new Dimension(380,340));
        card.add(Theme.title("Welcome to FlushID"));card.add(Theme.label("Sign in to save preferences and see your reviews.",13,Theme.MUTED));card.add(Box.createVerticalStrut(24));card.add(new JLabel("Username"));card.add(username);card.add(Box.createVerticalStrut(12));card.add(new JLabel("Password"));card.add(password);card.add(Box.createVerticalStrut(16));
        JButton login=Theme.primary("Log in"),signup=Theme.button("Create account"),back=Theme.button("Back to map");login.setAlignmentX(LEFT_ALIGNMENT);signup.setAlignmentX(LEFT_ALIGNMENT);back.setAlignmentX(LEFT_ALIGNMENT);
        login.addActionListener(e->controller.execute(username.getText(),new String(password.getPassword())));signup.addActionListener(e->onSignup.run());back.addActionListener(e->onBack.run());card.add(login);card.add(Box.createVerticalStrut(8));card.add(signup);card.add(Box.createVerticalStrut(8));card.add(back);card.add(Box.createVerticalStrut(16));card.add(message);add(card);
        model.addPropertyChangeListener(e->{LoginViewModel.State s=model.getState();message.setText(s.message());message.setForeground(s.success()?new Color(37,125,80):Theme.BERRY);});}
    public void setOnBack(Runnable r){onBack=r;}public void setOnSignup(Runnable r){onSignup=r;}
}
