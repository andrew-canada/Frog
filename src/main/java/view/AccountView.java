package view;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeListener;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.function.BiConsumer;

import entity.User;
import interface_adapter.account.AccountState;
import interface_adapter.account.AccountViewModel;
import interface_adapter.account.change_password.ChangePasswordController;
import interface_adapter.account.change_username.ChangeUsernameController;
import interface_adapter.account.delete_account.DeleteAccountController;
import interface_adapter.account.personal_plan.PersonalPlanController;
import interface_adapter.login.LoginController;
import interface_adapter.login.LoginViewModel;

public final class AccountView extends JPanel {

    private final AccountViewModel viewModel;
    private Runnable onBack = () -> {
    };
    private ChangeUsernameController changeUsernameController;
    private ChangePasswordController changePasswordController;
    private DeleteAccountController deleteAccountController;
    private PersonalPlanController personalPlanController;

    private final JPanel personalPlan = Theme.page();
    private final JPanel changeUsername = Theme.page();
    private final JPanel changePassword = Theme.page();
    private final JPanel deleteAccount = Theme.page();

    private final JButton back = Theme.button("← Back to Map");

    private final JLabel accountLabel = new JLabel();
    private final JLabel personalPlanLabel = new JLabel();
    private final JLabel personalPlanStatusLabel = new JLabel();

    private final JFileChooser icsChooser = new JFileChooser(); // TODO: make it so that it has to be an ics file
    private final JTextField nTripField = new JTextField(10); // TODO: restrict it just to ints

    private final JTextField usernameField = new JTextField(10);
    private final JLabel usernameStatusLabel = new JLabel();

    private final JPasswordField passwordField = new JPasswordField(10);
    private final JPasswordField confirmPasswordField = new JPasswordField(10);
    private final JLabel passwordStatusLabel = new JLabel();

    private final JLabel deleteAccountLabel = new JLabel();

    private final JButton personalPlanButton = new JButton("Generate New Plan");

    private final JButton changeUsernameButton = new JButton("Change Username");
    private final JButton confirmUsernameButton = new JButton("Confirm Username");
    private final JButton cancelUsernameButton = new JButton("Cancel");

    private final JButton changePasswordButton = new JButton("Change Password");
    private final JButton confirmPasswordButton = new JButton("Confirm Password");
    private final JButton cancelPasswordButton = new JButton("Cancel");

    private final JButton deleteAccountButton = new JButton("Delete Account");
    private final JButton confirmDeleteAccountButton = new JButton("Delete Account");
    private final JButton cancelDeleteAccountButton = new JButton("Cancel");

    public AccountView(AccountViewModel viewModel, ChangeUsernameController changeUsernameController, ChangePasswordController changePasswordController, DeleteAccountController deleteAccountController, PersonalPlanController personalPlanController) {

        this.viewModel = viewModel;
        viewModel.getState().addPropertyChangeListener(e -> render(viewModel.getState()));

        this.changeUsernameController = changeUsernameController;
        this.changePasswordController = changePasswordController;
        this.deleteAccountController = deleteAccountController;
        this.personalPlanController = personalPlanController;

        setLayout(new BorderLayout());

        JPanel title = Theme.page();
        title.setLayout(new BorderLayout());

        JPanel titleWords = Theme.page();
        titleWords.setLayout(new BorderLayout());
        titleWords.add(Theme.title("Account"), BorderLayout.NORTH);
        titleWords.add(accountLabel, BorderLayout.SOUTH);

        title.add(titleWords, BorderLayout.WEST);
        back.addActionListener(e -> onBack.run());
        title.add(back, BorderLayout.EAST);
        add(title, BorderLayout.NORTH);

        JPanel container = Theme.page();
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        container.add(personalPlan);
        container.add(changeUsername);
        container.add(changePassword);
        container.add(deleteAccount);

        personalPlan.setLayout(new BoxLayout(personalPlan, BoxLayout.Y_AXIS));
        JPanel personalPlanTitle = Theme.page();
        personalPlanTitle.setLayout(new BorderLayout());
        personalPlanTitle.add(Theme.title("Personal Washroom Plan"), BorderLayout.WEST);
        JPanel personalPlanContent = Theme.page();
        personalPlanContent.setLayout(new BorderLayout());
        personalPlanContent.add(Theme.label("Your Current Plan:", 14, Theme.INK), BorderLayout.WEST);
        personalPlanContent.add(personalPlanLabel, BorderLayout.CENTER);
        JScrollPane personalPlanScroll = new JScrollPane(personalPlanContent);
        JPanel personalPlanInput = Theme.page();
        personalPlanInput.add(icsChooser);
        personalPlanInput.add(nTripField);
        personalPlanContent.add(personalPlanInput, BorderLayout.SOUTH);
        JPanel personalPlanButtons = Theme.page();
        personalPlanButtons.setLayout(new BorderLayout());
        personalPlanButtons.add(personalPlanButton, BorderLayout.WEST);
        personalPlanButtons.add(personalPlanStatusLabel, BorderLayout.EAST);
        personalPlan.add(personalPlanTitle);
        personalPlan.add(personalPlanScroll);
        personalPlan.add(personalPlanButtons);

        changeUsername.setLayout(new BoxLayout(changeUsername, BoxLayout.Y_AXIS));
        JPanel changeUsernameTitle = Theme.page();
        changeUsernameTitle.setLayout(new BorderLayout());
        changeUsernameTitle.add(Theme.title("Change Username"), BorderLayout.WEST);
        JPanel changeUsernameContent = Theme.page();
        JPanel changeUsernameButtons = Theme.page();
        changeUsernameButtons.setLayout(new BorderLayout());
        changeUsernameButtons.add(changeUsernameButton, BorderLayout.WEST);
        changeUsername.add(changeUsernameTitle);
        changeUsername.add(changeUsernameContent);
        changeUsername.add(changeUsernameButtons);

        changePassword.setLayout(new BoxLayout(changePassword, BoxLayout.Y_AXIS));
        JPanel changePasswordTitle = Theme.page();
        changePasswordTitle.setLayout(new BorderLayout());
        changePasswordTitle.add(Theme.title("Change Password"), BorderLayout.WEST);
        JPanel changePasswordContent = Theme.page();
        JPanel changePasswordButtons = Theme.page();
        changePasswordButtons.setLayout(new BorderLayout());
        changePasswordButtons.add(changePasswordButton, BorderLayout.WEST);
        changePassword.add(changePasswordTitle);
        changePassword.add(changePasswordContent);
        changePassword.add(changePasswordButtons);

        deleteAccount.setLayout(new BoxLayout(deleteAccount, BoxLayout.Y_AXIS));
        JPanel deleteAccountTitle = Theme.page();
        deleteAccountTitle.setLayout(new BorderLayout());
        deleteAccountTitle.add(Theme.title("Delete Account"), BorderLayout.WEST);
        JPanel deleteAccountContent = Theme.page();
        deleteAccountContent.setLayout(new BorderLayout());
        JPanel deleteAccountButtons = Theme.page();
        deleteAccountButtons.setLayout(new BorderLayout());
        deleteAccountButtons.add(deleteAccountButton, BorderLayout.WEST);
        deleteAccount.add(deleteAccountTitle);
        deleteAccount.add(deleteAccountContent);
        deleteAccount.add(deleteAccountButtons);

        JScrollPane scrollPane = new JScrollPane(container);
        add(scrollPane, BorderLayout.CENTER);

        personalPlanButton.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent evt) {
                        System.out.println(viewModel.getState().getPersonalPlanMessage());
                        personalPlanController.execute(icsChooser.getSelectedFile().getAbsolutePath(), nTripField.getText());
                        System.out.println(viewModel.getState().getPersonalPlanMessage());
                    }
                }
        );

        changeUsernameButton.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent evt) {

                        changeUsernameContent.removeAll();
                        changeUsernameContent.add(Theme.label("New Username:", 14, Theme.INK));
                        changeUsernameContent.add(usernameField);

                        changeUsernameContent.revalidate();
                        changeUsernameContent.repaint();

                        changeUsernameButtons.removeAll();
                        changeUsernameButtons.add(confirmUsernameButton, BorderLayout.WEST);
                        changeUsernameButtons.add(cancelUsernameButton, BorderLayout.CENTER);
                        changeUsernameButtons.add(usernameStatusLabel, BorderLayout.EAST);

                        changeUsernameButtons.revalidate();
                        changeUsernameButtons.repaint();

                    }
                }
        );

        confirmUsernameButton.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent evt) {

                        changeUsernameController.execute(usernameField.getText());

                    }
                }
        );

        cancelUsernameButton.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent evt) {

                        changeUsernameContent.removeAll();
                        changeUsernameContent.add(usernameStatusLabel);
                        changeUsernameContent.revalidate();
                        changeUsernameContent.repaint();

                        changeUsernameButtons.removeAll();
                        changeUsernameButtons.add(changeUsernameButton, BorderLayout.WEST);

                        changeUsernameButtons.revalidate();
                        changeUsernameButtons.repaint();

                    }
                }
        );

        changePasswordButton.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent evt) {

                        changePasswordContent.add(Theme.label("New Password:", 14, Theme.INK));
                        changePasswordContent.add(passwordField);
                        changePasswordContent.add(Theme.label("Confirm Password:", 14, Theme.INK));
                        changePasswordContent.add(confirmPasswordField);
                        changePasswordContent.add(passwordStatusLabel);

                        changePasswordContent.revalidate();
                        changePasswordContent.repaint();

                        changePasswordButtons.remove(changePasswordButton);
                        changePasswordButtons.add(confirmPasswordButton, BorderLayout.WEST);
                        changePasswordButtons.add(cancelPasswordButton, BorderLayout.CENTER);

                        changePasswordButtons.revalidate();
                        changePasswordButtons.repaint();

                    }
                }
        );

        confirmPasswordButton.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent evt) {

                        changePasswordController.execute(String.valueOf(passwordField.getPassword()), String.valueOf(confirmPasswordField.getPassword()));

                    }
                }
        );

        cancelPasswordButton.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent evt) {

                        changePasswordContent.removeAll();
                        changePasswordContent.add(passwordStatusLabel);
                        changePasswordContent.revalidate();
                        changePasswordContent.repaint();

                        changePasswordButtons.remove(confirmPasswordButton);
                        changePasswordButtons.remove(cancelPasswordButton);
                        changePasswordButtons.add(changePasswordButton, BorderLayout.WEST);

                        changePasswordButtons.revalidate();
                        changePasswordButtons.repaint();

                    }
                }
        );

        deleteAccountButton.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent evt) {

                        deleteAccountContent.add(Theme.label("r u sure lil bro", 14, Theme.INK));

                        deleteAccountContent.revalidate();
                        deleteAccountContent.repaint();

                        deleteAccountButtons.remove(deleteAccountButton);
                        deleteAccountButtons.add(confirmDeleteAccountButton, BorderLayout.WEST);
                        deleteAccountButtons.add(cancelDeleteAccountButton, BorderLayout.CENTER);

                        deleteAccountButtons.revalidate();
                        deleteAccountButtons.repaint();

                    }
                }
        );

        confirmDeleteAccountButton.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent evt) {

                        deleteAccountController.execute();

                    }
                }
        );

        cancelDeleteAccountButton.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent evt) {

                        deleteAccountContent.remove(0);

                        deleteAccountContent.revalidate();
                        deleteAccountContent.repaint();

                        deleteAccountButtons.remove(confirmDeleteAccountButton);
                        deleteAccountButtons.remove(cancelDeleteAccountButton);
                        deleteAccountButtons.add(deleteAccountButton, BorderLayout.WEST);

                        deleteAccountButtons.revalidate();
                        deleteAccountButtons.repaint();

                    }
                }
        );

    }

    private void render(AccountState state) {

        System.out.println("?h");
        accountLabel.setText(state.getUsername());

        personalPlanLabel.setText(state.getPersonalPlan());
        personalPlanStatusLabel.setText(state.getPersonalPlanMessage());

        System.out.println(state.getChangeUsernameSuccess());

        if (state.getChangeUsernameSuccess()) {
            cancelUsernameButton.doClick();
            state.setChangeUsernameSuccess(false);
        }
        usernameStatusLabel.setText(state.getChangeUsernameMessage());

        if (state.getChangePasswordSuccess()) {
            cancelPasswordButton.doClick();
            state.setChangePasswordSuccess(false);
        }
        passwordStatusLabel.setText(state.getChangePasswordMessage());

        if (state.getDeleteAccountSuccess()) {
            back.doClick();
            cancelDeleteAccountButton.doClick();
            state.setDeleteAccountSuccess(false);
        }

        personalPlan.revalidate();
        personalPlan.repaint();

    }

    public void setOnBack(Runnable r) {
        onBack = r;
    }

}
