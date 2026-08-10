package views;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingWorker;
import javax.swing.Timer;

import interface_adapter.account.AccountState;
import interface_adapter.account.AccountViewModel;
import interface_adapter.account.IsLoggedInState;
import interface_adapter.account.IsLoggedInViewModel;
import interface_adapter.account.change_password.ChangePasswordController;
import interface_adapter.account.change_username.ChangeUsernameController;
import interface_adapter.account.delete_account.DeleteAccountController;
import interface_adapter.account.personal_plan.PersonalPlanController;

public final class AccountView extends JPanel {
    private static final String FIELD_SELECTED_FILE = "Selected File: ";
    private static final String FIELD_CANCEL = "Cancel";
    private static final String FIELD_DELETE_ACCOUNT = "Delete Account";
    private static final int LABEL_FONT_SIZE = 14;
    private static final int SECTION_GAP = 10;
    private static final int SCROLL_BLOCK_INCREMENT = 192;
    private static final int SCROLL_UNIT_INCREMENT = 32;
    private static final int TEXT_AREA_COLUMNS = 50;

    private final AccountViewModel viewModel;
    private final IsLoggedInViewModel isLoggedInViewModel;
    private final JPanel personalPlan = Theme.page();
    private final JPanel changeUsername = Theme.page();
    private final JPanel changeUsernameContent = Theme.page();
    private final JPanel changeUsernameButtons = Theme.page();
    private final JPanel changePassword = Theme.page();
    private final JPanel changePasswordContent = Theme.page();
    private final JPanel changePasswordButtons = Theme.page();
    private final JPanel deleteAccount = Theme.page();
    private final JPanel deleteAccountContent = Theme.page();
    private final JPanel deleteAccountButtons = Theme.page();
    private final JButton back = Theme.button("<- Back to Map");
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
    private final JLabel personalPlanSelectedFileLabel = new JLabel(FIELD_SELECTED_FILE);
    private String personalPlanSelectedFilePath = "";
    private final JTextField personalPlanNumField = new JTextField(10);
    private final String[] semesters = {"Summer", "Fall", "Winter"};
    private final JComboBox personalPlanSemesterBox = new JComboBox(semesters);
    private final JButton personalPlanViewButton = Theme.button("View Plan");
    private final JButton personalPlanGenerateButton = Theme.button("Generate New Plan");

    private final JButton changeUsernameButton = Theme.button("Change Username");
    private final JButton confirmUsernameButton = Theme.button("Confirm Username");
    private final JButton cancelUsernameButton = Theme.button(FIELD_CANCEL);
    private final JButton changePasswordButton = Theme.button("Change Password");
    private final JButton confirmPasswordButton = Theme.button("Confirm Password");
    private final JButton cancelPasswordButton = Theme.button(FIELD_CANCEL);
    private final JButton deleteAccountButton = Theme.button(FIELD_DELETE_ACCOUNT);
    private final JButton confirmDeleteAccountButton = Theme.button(FIELD_DELETE_ACCOUNT);
    private final JButton cancelDeleteAccountButton = Theme.button(FIELD_CANCEL);
    private Runnable onBack = () -> {
    };
    private Consumer<String> onViewPlan = id -> {
    };

    public AccountView(final AccountViewModel viewModel, final IsLoggedInViewModel isLoggedInViewModel,
                       final ChangeUsernameController changeUsernameController,
                       final ChangePasswordController changePasswordController,
                       final DeleteAccountController deleteAccountController,
                       final PersonalPlanController personalPlanController) {

        this.viewModel = viewModel;
        this.isLoggedInViewModel = isLoggedInViewModel;

        viewModel
            .getState()
            .addPropertyChangeListener(entryValue -> {
                render(viewModel.getState());
            });
        isLoggedInViewModel
            .getState()
            .addPropertyChangeListener(entryValue -> {
                render(isLoggedInViewModel.getState());
            });

        initializeLayout();

        personalPlanUploadFileButton.addActionListener(entryValue -> chooseCalendarFile());
        personalPlanGenerateButton.addActionListener(entryValue -> generatePlan(personalPlanController));

        personalPlanViewButton.addActionListener(entryValue -> {
            onViewPlan.accept(viewModel.getState().getPersonalPlan());
        });
        changeUsernameButton.addActionListener(entryValue -> {
            showUsernameEditor();
        });
        confirmUsernameButton.addActionListener(entryValue -> {
            changeUsernameController.execute(usernameField.getText());
        });
        cancelUsernameButton.addActionListener(entryValue -> resetUsernameEditor());
        changePasswordButton.addActionListener(entryValue -> showPasswordEditor());
        confirmPasswordButton.addActionListener(entryValue -> {
            changePasswordController.execute(String.valueOf(passwordField.getPassword()),
                String.valueOf(confirmPasswordField.getPassword()));
        });
        cancelPasswordButton.addActionListener(entryValue -> resetPasswordEditor());
        deleteAccountButton.addActionListener(entryValue -> showDeleteConfirmation());
        confirmDeleteAccountButton.addActionListener(entryValue -> deleteAccountController.execute());
        cancelDeleteAccountButton.addActionListener(entryValue -> resetDeleteConfirmation());

    }

    private void initializeLayout() {
        setLayout(new BorderLayout());
        final JPanel title = Theme.page();
        title.setLayout(new BorderLayout());
        final JPanel titleWords = Theme.page();
        titleWords.setLayout(new BorderLayout());
        titleWords.add(Theme.title("Account"), BorderLayout.NORTH);
        titleWords.add(accountLabel, BorderLayout.SOUTH);
        title.add(titleWords, BorderLayout.WEST);
        back.addActionListener(entryValue -> {
            viewModel.getState().exitResetState();
            resetAccountView();
            onBack.run();
        });
        title.add(back, BorderLayout.EAST);
        add(title, BorderLayout.NORTH);

        final JPanel container = Theme.page();
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        container.add(personalPlan);
        container.add(Box.createVerticalStrut(SECTION_GAP));
        container.add(changeUsername);
        container.add(Box.createVerticalStrut(SECTION_GAP));
        container.add(changePassword);
        container.add(Box.createVerticalStrut(SECTION_GAP));
        container.add(deleteAccount);
        buildPersonalPlanPanel();
        buildAccountSection(changeUsername, "Change Username", changeUsernameContent, changeUsernameButtons,
            changeUsernameButton);
        buildAccountSection(changePassword, "Change Password", changePasswordContent, changePasswordButtons,
            changePasswordButton);
        buildAccountSection(deleteAccount, FIELD_DELETE_ACCOUNT, deleteAccountContent, deleteAccountButtons,
            deleteAccountButton);
        final JScrollPane scrollPane = new JScrollPane(container);
        scrollPane.getVerticalScrollBar().setUnitIncrement(SCROLL_UNIT_INCREMENT);
        scrollPane.getVerticalScrollBar().setBlockIncrement(SCROLL_BLOCK_INCREMENT);
        add(scrollPane, BorderLayout.CENTER);
    }

    private void buildPersonalPlanPanel() {
        personalPlan.setLayout(new BoxLayout(personalPlan, BoxLayout.Y_AXIS));
        personalPlan.setBackground(Theme.PAPER);
        personalPlan.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Theme.LINE),
            Theme.pad(SECTION_GAP, SECTION_GAP, SECTION_GAP, SECTION_GAP)));
        final JPanel title = new JPanel(new FlowLayout(FlowLayout.LEFT));
        title.setBackground(Theme.PAPER);
        title.add(Theme.title("Personal Washroom Plan"));
        personalPlan.add(title);
        personalPlan.add(Box.createVerticalStrut(SECTION_GAP));
        final JLabel instructions = Theme.label("Upload your acorn timetable to generate a personal washroom "
            + "schedule! Download your timetable from Acorn as an .ics file, upload it, then enter the number of "
            + "trips per day and semester.", LABEL_FONT_SIZE, Theme.INK);
        personalPlan.add(instructions);
        personalPlan.add(fileUploadPanel());
        personalPlan.add(tripPanel());
        personalPlan.add(semesterPanel());
        personalPlan.add(generateButtonPanel());
    }

    private JPanel fileUploadPanel() {
        final JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.setBackground(Theme.PAPER);
        panel.add(personalPlanUploadFileButton);
        panel.add(Box.createHorizontalStrut(SECTION_GAP));
        panel.add(personalPlanSelectedFileLabel);
        return panel;
    }

    private JPanel tripPanel() {
        final JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.setBackground(Theme.PAPER);
        panel.add(Theme.label("Number of Washroom Trips Per Day:", LABEL_FONT_SIZE, Theme.INK));
        panel.add(Box.createHorizontalStrut(SECTION_GAP));
        panel.add(personalPlanNumField);
        return panel;
    }

    private JPanel semesterPanel() {
        final JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.setBackground(Theme.PAPER);
        panel.add(Theme.label("Semester:", LABEL_FONT_SIZE, Theme.INK));
        panel.add(Box.createHorizontalStrut(SECTION_GAP));
        panel.add(personalPlanSemesterBox);
        return panel;
    }

    private JPanel generateButtonPanel() {
        final JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.setBackground(Theme.PAPER);
        panel.add(personalPlanViewButton);
        panel.add(Box.createHorizontalStrut(SECTION_GAP));
        panel.add(personalPlanGenerateButton);
        panel.add(Box.createHorizontalStrut(SECTION_GAP));
        panel.add(personalPlanStatusLabel);
        return panel;
    }

    private static void buildAccountSection(final JPanel section, final String titleText, final JPanel content,
                                            final JPanel buttons, final JButton initialButton) {
        section.setLayout(new BoxLayout(section, BoxLayout.Y_AXIS));
        section.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Theme.LINE),
            Theme.pad(SECTION_GAP, SECTION_GAP, SECTION_GAP, SECTION_GAP)));
        final JPanel title = Theme.page();
        title.setLayout(new BorderLayout());
        title.add(Theme.title(titleText), BorderLayout.WEST);
        buttons.setLayout(new BorderLayout());
        buttons.add(initialButton, BorderLayout.WEST);
        section.add(title);
        section.add(content);
        section.add(buttons);
    }

    private void showUsernameEditor() {
        changeUsernameContent.removeAll();
        changeUsernameContent.setLayout(new FlowLayout(FlowLayout.LEFT));
        changeUsernameContent.add(Theme.label("New Username:", LABEL_FONT_SIZE, Theme.INK));
        changeUsernameContent.add(Box.createHorizontalStrut(SECTION_GAP));
        changeUsernameContent.add(usernameField);
        changeUsernameContent.revalidate();
        changeUsernameContent.repaint();
        changeUsernameButtons.removeAll();
        changeUsernameButtons.setLayout(new FlowLayout(FlowLayout.LEFT));
        changeUsernameButtons.add(confirmUsernameButton);
        changeUsernameButtons.add(Box.createHorizontalStrut(SECTION_GAP));
        changeUsernameButtons.add(cancelUsernameButton);
        changeUsernameButtons.add(usernameStatusLabel, BorderLayout.EAST);
        changeUsernameButtons.revalidate();
        changeUsernameButtons.repaint();
    }

    private void resetUsernameEditor() {
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

    private void showPasswordEditor() {
        passwordField.setText("");
        confirmPasswordField.setText("");
        changePasswordContent.setLayout(new FlowLayout(FlowLayout.LEFT));
        changePasswordContent.add(Theme.label("New Password:", LABEL_FONT_SIZE, Theme.INK));
        changePasswordContent.add(Box.createHorizontalStrut(SECTION_GAP));
        changePasswordContent.add(passwordField);
        changePasswordContent.add(Box.createHorizontalStrut(SECTION_GAP));
        changePasswordContent.add(Theme.label("Confirm Password:", LABEL_FONT_SIZE, Theme.INK));
        changePasswordContent.add(Box.createHorizontalStrut(SECTION_GAP));
        changePasswordContent.add(confirmPasswordField);
        changePasswordContent.add(Box.createHorizontalStrut(SECTION_GAP));
        changePasswordContent.add(passwordStatusLabel);
        changePasswordContent.revalidate();
        changePasswordContent.repaint();
        changePasswordButtons.remove(changePasswordButton);
        changePasswordButtons.setLayout(new FlowLayout(FlowLayout.LEFT));
        changePasswordButtons.add(confirmPasswordButton);
        changePasswordButtons.add(Box.createHorizontalStrut(SECTION_GAP));
        changePasswordButtons.add(cancelPasswordButton);
        changePasswordButtons.revalidate();
        changePasswordButtons.repaint();
    }

    private void resetPasswordEditor() {
        changePasswordContent.removeAll();
        changePasswordContent.setLayout(new FlowLayout(FlowLayout.LEFT));
        changePasswordContent.add(passwordStatusLabel);
        changePasswordContent.revalidate();
        changePasswordContent.repaint();
        changePasswordButtons.remove(confirmPasswordButton);
        changePasswordButtons.remove(cancelPasswordButton);
        changePasswordButtons.setLayout(new FlowLayout(FlowLayout.LEFT));
        changePasswordButtons.add(changePasswordButton);
        changePasswordButtons.revalidate();
        changePasswordButtons.repaint();
    }

    private void showDeleteConfirmation() {
        deleteAccountContent.setLayout(new FlowLayout(FlowLayout.LEFT));
        deleteAccountContent.add(Theme.label("Are you sure? This action cannot be undone.", LABEL_FONT_SIZE,
            Theme.INK));
        deleteAccountContent.revalidate();
        deleteAccountContent.repaint();
        deleteAccountButtons.remove(deleteAccountButton);
        deleteAccountButtons.setLayout(new FlowLayout(FlowLayout.LEFT));
        deleteAccountButtons.add(confirmDeleteAccountButton);
        deleteAccountButtons.add(Box.createHorizontalStrut(SECTION_GAP));
        deleteAccountButtons.add(cancelDeleteAccountButton);
        deleteAccountButtons.revalidate();
        deleteAccountButtons.repaint();
    }

    private void resetDeleteConfirmation() {
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

    private void chooseCalendarFile() {
        final int result = icsChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            personalPlanSelectedFilePath = icsChooser.getSelectedFile().getAbsolutePath();
            personalPlanSelectedFileLabel.setText(FIELD_SELECTED_FILE + icsChooser.getSelectedFile().getName());
        }
        else if (result == JFileChooser.CANCEL_OPTION) {
            personalPlanSelectedFilePath = "";
            personalPlanSelectedFileLabel.setText(FIELD_SELECTED_FILE);
        }
    }

    private void generatePlan(final PersonalPlanController controller) {
        personalPlanGenerateButton.setEnabled(false);
        personalPlanStatusLabel.setText("Loading");
        final Timer loadingTimer = new Timer(500, entryValue -> updateLoadingLabel());
        final SwingWorker<String, Void> worker = new SwingWorker<String, Void>() {
            @Override
            protected String doInBackground() {
                controller.execute(personalPlanSelectedFilePath, personalPlanNumField.getText(),
                    (String) personalPlanSemesterBox.getSelectedItem());
                return "";
            }

            @Override
            protected void done() {
                finishPlanGeneration(this, loadingTimer);
            }
        };
        loadingTimer.start();
        worker.execute();
    }

    private void updateLoadingLabel() {
        final String current = personalPlanStatusLabel.getText();
        if (current.endsWith("...")) {
            personalPlanStatusLabel.setText("Loading");
        }
        else {
            personalPlanStatusLabel.setText(current + ".");
        }
    }

    private void finishPlanGeneration(final SwingWorker<String, Void> worker, final Timer loadingTimer) {
        loadingTimer.stop();
        try {
            worker.get();
            personalPlanViewButton.doClick();
        }
        catch (final InterruptedException interrupted) {
            Thread.currentThread().interrupt();
            personalPlanStatusLabel.setText("Please try again");
        }
        catch (final ExecutionException failure) {
            personalPlanStatusLabel.setText("Please try again");
        }
        finally {
            personalPlanGenerateButton.setEnabled(true);
        }
    }

    private void render(final AccountState state) {

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

    private void render(final IsLoggedInState state) {

        accountLabel.setText(state.getUsername());
        viewModel
            .getState()
            .setUsername(state.getUsername());
        if (!state.getIsLoggedIn()) {
            resetAccountView();
            viewModel
                .getState()
                .logoutResetState();
        }

    }

    private void resetAccountView() {
        personalPlanSelectedFilePath = "";
        personalPlanSelectedFileLabel.setText(FIELD_SELECTED_FILE);
        personalPlanNumField.setText("");
        personalPlanSemesterBox.setSelectedIndex(0);
        cancelUsernameButton.doClick();
        cancelPasswordButton.doClick();
        cancelDeleteAccountButton.doClick();
    }

    public void setOnViewPlan(final Consumer<String> thirdValue) {
        onViewPlan = thirdValue;
    }

    public void setOnBack(final Runnable reviewValue) {
        onBack = reviewValue;
    }

}
