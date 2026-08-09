package views;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import interface_adapter.login.SignupController;
import java.awt.GridLayout;
import java.awt.Window;


public final class SignupDialog extends JDialog {
    public SignupDialog(final Window owner, final SignupController controller) {
        super(owner, "Create account", ModalityType.APPLICATION_MODAL);
        final JPanel p = Theme.page();
        p.setLayout(new GridLayout(4, 2, 10, 10));

        final JTextField name = new JTextField();
        final JPasswordField password = new JPasswordField();
        p.add(new JLabel("Username"));
        p.add(name);
        p.add(new JLabel("Password"));
        p.add(password);
        final JButton cancel = Theme.button("Cancel");
        final JButton create = Theme.primary("Create account");
        cancel.addActionListener(e -> dispose());
        create.addActionListener(e -> {
            controller.execute(name.getText(), new String(password.getPassword()));
            dispose();
        });
        p.add(cancel);
        p.add(create);
        setContentPane(p);
        pack();
        setLocationRelativeTo(owner);
    }
}
