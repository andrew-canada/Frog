package view;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeListener;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import entity.User;
import interface_adapter.account.AccountState;
import interface_adapter.account.AccountViewModel;
import interface_adapter.account.change_password.ChangePasswordController;
import interface_adapter.account.change_username.ChangeUsernameController;
import interface_adapter.account.delete_account.DeleteAccountController;
import interface_adapter.account.personal_plan.PersonalPlanController;
import interface_adapter.login.LoginController;
import interface_adapter.login.LoginViewModel;
import use_case.account.personal_plan.PersonalPlanInteractor;

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

    private final JButton personalPlanButton = Theme.button("Generate New Plan");

    private final JButton changeUsernameButton = Theme.button("Change Username");
    private final JButton confirmUsernameButton = Theme.button("Confirm Username");
    private final JButton cancelUsernameButton = Theme.button("Cancel");

    private final JButton changePasswordButton = Theme.button("Change Password");
    private final JButton confirmPasswordButton = Theme.button("Confirm Password");
    private final JButton cancelPasswordButton = Theme.button("Cancel");

    private final JButton deleteAccountButton = Theme.button("Delete Account");
    private final JButton confirmDeleteAccountButton = Theme.button("Delete Account");
    private final JButton cancelDeleteAccountButton = Theme.button("Cancel");

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
        container.add(Box.createVerticalStrut(10));
        container.add(changeUsername);
        container.add(Box.createVerticalStrut(10));
        container.add(changePassword);
        container.add(Box.createVerticalStrut(10));
        container.add(deleteAccount);

        personalPlan.setLayout(new BoxLayout(personalPlan, BoxLayout.Y_AXIS));
        personalPlan.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Theme.LINE), Theme.pad(10, 10, 10, 10)));
        JPanel personalPlanTitle = Theme.page();
        personalPlanTitle.setLayout(new BorderLayout());
        personalPlanTitle.add(Theme.title("Personal Washroom Plan"), BorderLayout.WEST);
        JPanel personalPlanContent = Theme.page();
        personalPlanContent.setLayout(new BorderLayout());
        personalPlanContent.add(Theme.label("Your Current Plan:", 14, Theme.INK), BorderLayout.WEST);
        personalPlanContent.add(personalPlanLabel, BorderLayout.CENTER);
        JScrollPane personalPlanScroll = new JScrollPane(personalPlanContent);
        personalPlanScroll.getVerticalScrollBar().setUnitIncrement(32);
        personalPlanScroll.getVerticalScrollBar().setBlockIncrement(192);
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
        changeUsername.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Theme.LINE), Theme.pad(10, 10, 10, 10)));
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
        changePassword.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Theme.LINE), Theme.pad(10, 10, 10, 10)));
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
        deleteAccount.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Theme.LINE), Theme.pad(10, 10, 10, 10)));
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
        scrollPane.getVerticalScrollBar().setUnitIncrement(32);
        scrollPane.getVerticalScrollBar().setBlockIncrement(192);
        add(scrollPane, BorderLayout.CENTER);

        personalPlanButton.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent evt) {
                        personalPlanController.execute(icsChooser.getSelectedFile().getAbsolutePath(), nTripField.getText());
                    }
                }
        );

        changeUsernameButton.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent evt) {

                        changeUsernameContent.removeAll();
                        changeUsernameContent.setLayout(new FlowLayout(FlowLayout.LEFT));
                        changeUsernameContent.add(Theme.label("New Username:", 14, Theme.INK));
                        changeUsernameContent.add(Box.createHorizontalStrut(10));
                        changeUsernameContent.add(usernameField);

                        changeUsernameContent.revalidate();
                        changeUsernameContent.repaint();

                        changeUsernameButtons.removeAll();
                        changeUsernameButtons.setLayout(new FlowLayout(FlowLayout.LEFT));
                        changeUsernameButtons.add(confirmUsernameButton);
                        changeUsernameButtons.add(Box.createHorizontalStrut(10));
                        changeUsernameButtons.add(cancelUsernameButton);
                        changePasswordContent.add(Box.createHorizontalStrut(10));
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
                        changeUsernameContent.setLayout(new FlowLayout(FlowLayout.LEFT));
                        changeUsernameContent.add(usernameStatusLabel);
                        changeUsernameContent.revalidate();
                        changeUsernameContent.repaint();

                        changeUsernameButtons.removeAll();
                        changeUsernameButtons.setLayout(new FlowLayout(FlowLayout.LEFT));
                        changeUsernameButtons.add(changeUsernameButton);

                        changeUsernameButtons.revalidate();
                        changeUsernameButtons.repaint();

                    }
                }
        );

        changePasswordButton.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent evt) {

                        changePasswordContent.setLayout(new FlowLayout(FlowLayout.LEFT));
                        changePasswordContent.add(Theme.label("New Password:", 14, Theme.INK));
                        changePasswordContent.add(Box.createHorizontalStrut(10));
                        changePasswordContent.add(passwordField);
                        changePasswordContent.add(Box.createHorizontalStrut(10));
                        changePasswordContent.add(Theme.label("Confirm Password:", 14, Theme.INK));
                        changePasswordContent.add(Box.createHorizontalStrut(10));
                        changePasswordContent.add(confirmPasswordField);
                        changePasswordContent.add(Box.createHorizontalStrut(10));
                        changePasswordContent.add(passwordStatusLabel);

                        changePasswordContent.revalidate();
                        changePasswordContent.repaint();

                        changePasswordButtons.remove(changePasswordButton);
                        changePasswordButtons.setLayout(new FlowLayout(FlowLayout.LEFT));
                        changePasswordButtons.add(confirmPasswordButton);
                        changePasswordContent.add(Box.createHorizontalStrut(10));
                        changePasswordButtons.add(cancelPasswordButton);

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
                        changePasswordContent.setLayout(new FlowLayout(FlowLayout.LEFT));
                        changePasswordContent.add(passwordStatusLabel);
                        changePasswordContent.revalidate();
                        changePasswordContent.repaint();

                        changePasswordButtons.remove(confirmPasswordButton);
                        changePasswordButtons.remove(cancelPasswordButton);
                        changePasswordContent.setLayout(new FlowLayout(FlowLayout.LEFT));
                        changePasswordButtons.add(changePasswordButton);

                        changePasswordButtons.revalidate();
                        changePasswordButtons.repaint();

                    }
                }
        );

        deleteAccountButton.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent evt) {

                        deleteAccountContent.setLayout(new FlowLayout(FlowLayout.LEFT));
                        deleteAccountContent.add(Theme.label("Are you sure? This action cannot be undone.", 14, Theme.INK));

                        deleteAccountContent.revalidate();
                        deleteAccountContent.repaint();

                        deleteAccountButtons.remove(deleteAccountButton);
                        deleteAccountButtons.setLayout(new FlowLayout(FlowLayout.LEFT));
                        deleteAccountButtons.add(confirmDeleteAccountButton);
                        deleteAccountButtons.add(Box.createHorizontalStrut(10));
                        deleteAccountButtons.add(cancelDeleteAccountButton);

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
                        deleteAccountContent.setLayout(new FlowLayout(FlowLayout.LEFT));
                        deleteAccountButtons.add(deleteAccountButton);

                        deleteAccountButtons.revalidate();
                        deleteAccountButtons.repaint();

                    }
                }
        );

    }

    private void render(AccountState state) {

        accountLabel.setText(state.getUsername());

        renderPlan(state.getPersonalPlan());

        // personalPlanLabel.setText(state.getPersonalPlan());
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

    private void renderPlan(String plan) {

        try {
            ObjectMapper mapper = new ObjectMapper();
            List<PersonalPlanInteractor.WashroomPlan> washroomList = mapper.readValue(plan, new TypeReference<List<PersonalPlanInteractor.WashroomPlan>>() {});

            List<String> days = List.of("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun");

            JPanel planPanel = new JPanel();
            planPanel.setLayout(new BoxLayout(planPanel, BoxLayout.X_AXIS));

            for (String day : days) {
                JPanel dayPanel = new JPanel();
                dayPanel.setMaximumSize(new Dimension(300, 124));
                dayPanel.setLayout(new BoxLayout(dayPanel, BoxLayout.Y_AXIS));
                dayPanel.add(Theme.label(day, 14, Theme.INK));
                for (PersonalPlanInteractor.WashroomPlan washroom : washroomList) {
                    if (washroom.day.contains(day)) {
                        JPanel card = new JPanel(new BorderLayout(4, 4));
                        card.add(Theme.label(washroom.time, 14,  Theme.INK), BorderLayout.WEST);
                        card.add(Theme.label(washroom.washroom, 14,  Theme.INK), BorderLayout.EAST);
                        dayPanel.add(card);
                    }
                }
                planPanel.add(dayPanel);

            }

            personalPlan.add(planPanel);
            personalPlan.revalidate();
            personalPlan.repaint();
            System.out.println("success");
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public void setOnBack(Runnable r) {
        onBack = r;
    }

}
