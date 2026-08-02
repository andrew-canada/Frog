package view;

import interface_adapter.login.SignupController;
import javax.swing.*;
import java.awt.*;


public final class SignupDialog extends JDialog{
    public SignupDialog(Window owner,SignupController controller){super(owner,"Create account",ModalityType.APPLICATION_MODAL);
        JPanel p=Theme.page();
        p.setLayout(new GridLayout(4,2,10,10));

        JTextField name=new JTextField();
        JPasswordField password=new JPasswordField();
        p.add(new JLabel("Username"));
        p.add(name);
        p.add(new JLabel("Password"));
        p.add(password);
        JButton cancel=Theme.button("Cancel"),create=Theme.primary("Create account");
        cancel.addActionListener(e->dispose());
        create.addActionListener(e->{
            controller.execute(name.getText(),new String(password.getPassword()));
            dispose();
        });
        p.add(cancel);
        p.add(create);
        setContentPane(p);
        pack();
        setLocationRelativeTo(owner);
    }
}
