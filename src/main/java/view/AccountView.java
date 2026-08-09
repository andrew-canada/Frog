package view;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import interface_adapter.account.AccountState;
import interface_adapter.account.AccountViewModel;
import interface_adapter.account.IsLoggedInState;
import interface_adapter.account.IsLoggedInViewModel;
import interface_adapter.account.change_password.ChangePasswordController;
import interface_adapter.account.change_username.ChangeUsernameController;
import interface_adapter.account.delete_account.DeleteAccountController;
import interface_adapter.account.personal_plan.PersonalPlanController;
import use_case.account.personal_plan.PersonalPlanInteractor;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.function.Consumer;

public final class AccountView extends JPanel {

    private final AccountViewModel viewModel;
    private final IsLoggedInViewModel isLoggedInViewModel;
    private final JPanel personalPlan = Theme.page();
    private final JPanel changeUsername = Theme.page();
    private final JPanel changePassword = Theme.page();
    private final JPanel deleteAccount = Theme.page();
    private final JButton back = Theme.button("← Back to Map");
    private final JLabel accountLabel = new JLabel();
    private final JLabel personalPlanStatusLabel = new JLabel();
    private final JFileChooser icsChooser = new JFileChooser();

    private final JTextField usernameField = new JTextField(10);
    private final JLabel usernameStatusLabel = new JLabel();
    private final JPasswordField passwordField = new JPasswordField(10);
    private final JPasswordField confirmPasswordField = new JPasswordField(10);
    private final JLabel passwordStatusLabel = new JLabel();
    private final JLabel deleteAccountLabel = new JLabel();

    private final JButton personalPlanUploadFileButton = Theme.button("Upload .ics File");
    private final JLabel personalPlanSelectedFileLabel = new JLabel("Selected File: ");
    private String personalPlanSelectedFilePath = "";
    private final JTextField personalPlanNumField = new JTextField(10);
    private final String[] semesters = {"Summer", "Fall", "Winter"};
    private final JComboBox personalPlanSemesterBox = new JComboBox(semesters);
    private final JButton personalPlanViewButton = Theme.button("View Plan");
    private final JButton personalPlanGenerateButton = Theme.button("Generate New Plan");

    private final JButton changeUsernameButton = Theme.button("Change Username");
    private final JButton confirmUsernameButton = Theme.button("Confirm Username");
    private final JButton cancelUsernameButton = Theme.button("Cancel");
    private final JButton changePasswordButton = Theme.button("Change Password");
    private final JButton confirmPasswordButton = Theme.button("Confirm Password");
    private final JButton cancelPasswordButton = Theme.button("Cancel");
    private final JButton deleteAccountButton = Theme.button("Delete Account");
    private final JButton confirmDeleteAccountButton = Theme.button("Delete Account");
    private final JButton cancelDeleteAccountButton = Theme.button("Cancel");
    private Runnable onBack = () -> {
    };
    private Consumer<String> onViewPlan = id -> {
    };

    public AccountView(AccountViewModel viewModel, IsLoggedInViewModel isLoggedInViewModel, ChangeUsernameController changeUsernameController, ChangePasswordController changePasswordController, DeleteAccountController deleteAccountController, PersonalPlanController personalPlanController) {

        this.viewModel = viewModel;
        this.isLoggedInViewModel = isLoggedInViewModel;

        viewModel.getState().addPropertyChangeListener(e -> render(viewModel.getState()));
        isLoggedInViewModel.getState().addPropertyChangeListener(e -> render(isLoggedInViewModel.getState()));

        setLayout(new BorderLayout());

        JPanel title = Theme.page();
        title.setLayout(new BorderLayout());

        JPanel titleWords = Theme.page();
        titleWords.setLayout(new BorderLayout());
        titleWords.add(Theme.title("Account"), BorderLayout.NORTH);
        titleWords.add(accountLabel, BorderLayout.SOUTH);

        title.add(titleWords, BorderLayout.WEST);
        back.addActionListener(e -> {
            viewModel.getState().exitResetState();
            resetAccountView();
            onBack.run();
        });
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
        personalPlan.setBackground(Theme.PAPER);
        personalPlan.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Theme.LINE), Theme.pad(10, 10, 10, 10)));
        JPanel personalPlanTitle = new JPanel();
        personalPlanTitle.setLayout(new FlowLayout(FlowLayout.LEFT));
        personalPlanTitle.setBackground(Theme.PAPER);
        personalPlanTitle.add(Theme.title("Personal Washroom Plan"));
        personalPlan.add(personalPlanTitle);
        personalPlan.add(Box.createVerticalStrut(10));

        JTextArea personalPlanInstructions = new JTextArea("Upload your acorn timetable to generate a personal washroom schedule!\n" +
                "To generate plan, download your timetable from Acorn Timetable as an .ics file and upload it here, then enter your desired number of washroom trips per day and the current semester. \n" +
                "Click the Generate Plan button then the View Plan button to see the schedule (this may take a few minutes)");
        personalPlanInstructions.setLineWrap(true);
        personalPlanInstructions.setWrapStyleWord(true);
        personalPlanInstructions.setEditable(false);
        personalPlanInstructions.setOpaque(false);
        personalPlanInstructions.setColumns(50);
        personalPlan.add(personalPlanInstructions);

        JPanel fileUploadPanel = new JPanel();
        fileUploadPanel.setBackground(Theme.PAPER);
        fileUploadPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        fileUploadPanel.add(personalPlanUploadFileButton);
        fileUploadPanel.add(Box.createHorizontalStrut(10));
        fileUploadPanel.add(personalPlanSelectedFileLabel);
        personalPlan.add(fileUploadPanel);
        personalPlan.add(Box.createVerticalStrut(10));

        JPanel numTripPanel = new JPanel();
        numTripPanel.setBackground(Theme.PAPER);
        numTripPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        numTripPanel.add(Theme.label("Number of Washroom Trips Per Day:", 14, Theme.INK));
        numTripPanel.add(Box.createHorizontalStrut(10));
        numTripPanel.add(personalPlanNumField);
        personalPlan.add(numTripPanel);
        personalPlan.add(Box.createVerticalStrut(10));

        JPanel semesterPanel = new JPanel();
        semesterPanel.setBackground(Theme.PAPER);
        semesterPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        semesterPanel.add(Theme.label("Semester:", 14, Theme.INK));
        semesterPanel.add(Box.createHorizontalStrut(10));
        semesterPanel.add(personalPlanSemesterBox);
        personalPlan.add(semesterPanel);
        personalPlan.add(Box.createVerticalStrut(10));

        JPanel generateButtonPanel = new JPanel();
        generateButtonPanel.setBackground(Theme.PAPER);
        generateButtonPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        generateButtonPanel.add(personalPlanViewButton);
        generateButtonPanel.add(Box.createHorizontalStrut(10));
        generateButtonPanel.add(personalPlanGenerateButton);
        generateButtonPanel.add(Box.createHorizontalStrut(10));
        generateButtonPanel.add(personalPlanStatusLabel);
        personalPlan.add(generateButtonPanel);

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

        personalPlanUploadFileButton.addActionListener(e -> {

            int result = icsChooser.showOpenDialog(this);
            if (result == JFileChooser.APPROVE_OPTION) {
                personalPlanSelectedFilePath = icsChooser.getSelectedFile().getAbsolutePath();
                personalPlanSelectedFileLabel.setText("Selected File: " + icsChooser.getSelectedFile().getName());
            } else if (result == JFileChooser.CANCEL_OPTION) {
                personalPlanSelectedFilePath = "";
                personalPlanSelectedFileLabel.setText("Selected File: ");
            }

        });

        personalPlanGenerateButton.addActionListener(e -> {

            personalPlanGenerateButton.setEnabled(false);

            personalPlanStatusLabel.setText("Loading");
            Timer loadingTimer = new Timer(500, null);
            loadingTimer.addActionListener(evt -> {
                String curr = personalPlanStatusLabel.getText();
                if (curr.endsWith("...")) {
                    personalPlanStatusLabel.setText("Loading");
                } else {
                    personalPlanStatusLabel.setText(curr + ".");
                }
            });

            SwingWorker<String, Void> worker = new SwingWorker<String, Void>() {

                @Override
                protected String doInBackground() throws Exception {

                    personalPlanController.execute(personalPlanSelectedFilePath, personalPlanNumField.getText(), (String) personalPlanSemesterBox.getSelectedItem());
                    return "";

                }

                @Override
                protected void done() {

                    loadingTimer.stop();
                    try {
                        get();
                        personalPlanViewButton.doClick();
                    } catch (Exception ex) {
                        personalPlanStatusLabel.setText("Please try again");
                    } finally {
                        personalPlanGenerateButton.setEnabled(true);
                    }

                }

            };

            loadingTimer.start();
            worker.execute();

        });

        personalPlanViewButton.addActionListener(e -> {

            onViewPlan.accept(viewModel.getState().getPersonalPlan());

        });

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

                        usernameField.setText("");
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

                        passwordField.setText("");
                        confirmPasswordField.setText("");
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

                        deleteAccountContent.removeAll();
                        deleteAccountContent.revalidate();
                        deleteAccountContent.repaint();

                        deleteAccountButtons.remove(confirmDeleteAccountButton);
                        deleteAccountButtons.remove(cancelDeleteAccountButton);
                        deleteAccountButtons.setLayout(new FlowLayout(FlowLayout.LEFT));
                        deleteAccountButtons.add(deleteAccountButton);

                        deleteAccountButtons.revalidate();
                        deleteAccountButtons.repaint();

                    }
                }
        );

    }

    private void render(AccountState state) {

        accountLabel.setText(state.getUsername());

        personalPlanStatusLabel.setText(state.getPersonalPlanMessage());

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
        deleteAccountLabel.setText(state.getDeleteAccountMessage());

        personalPlan.revalidate();
        personalPlan.repaint();

    }

    private void render(IsLoggedInState state) {

        accountLabel.setText(state.getUsername());
        viewModel.getState().setUsername(state.getUsername());
        if (!state.getIsLoggedIn()) {
            resetAccountView();
            viewModel.getState().logoutResetState();
        }

    }

    private void resetAccountView() {
        personalPlanSelectedFilePath = "";
        personalPlanSelectedFileLabel.setText("Selected File: ");
        personalPlanNumField.setText("");
        personalPlanSemesterBox.setSelectedIndex(0);
        cancelUsernameButton.doClick();
        cancelPasswordButton.doClick();
        cancelDeleteAccountButton.doClick();
    }

    public void setOnViewPlan(Consumer<String> c) {
        onViewPlan = c;
    }

    public void setOnBack(Runnable r) {
        onBack = r;
    }

}
