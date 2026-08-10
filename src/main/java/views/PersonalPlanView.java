package views;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.util.List;
import java.util.Map;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public final class PersonalPlanView extends JDialog {
    private static final int SECTION_GAP = 10;
    private static final int BODY_FONT_SIZE = 14;
    private static final int FORM_INSET = 5;

    public PersonalPlanView(final Window owner, final String plan) {

        super(owner, "Personal Washroom Schedule", ModalityType.APPLICATION_MODAL);

        final JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        container.setBackground(Theme.PAPER);

        try {
            final JPanel planPanel = buildPlanPanel(plan);
            final JPanel personalPlanTitle = new JPanel();
            personalPlanTitle.setLayout(new FlowLayout(FlowLayout.LEFT));
            personalPlanTitle.setBackground(Theme.PAPER);
            personalPlanTitle.add(Theme.title("Personal Washroom Plan"));
            container.add(personalPlanTitle);
            container.add(Box.createVerticalStrut(SECTION_GAP));
            container.add(planPanel);

        }
        catch (final JsonProcessingException entryValue) {

            container.add(Theme.title("Error - Please try generating a new plan"));

        }

        final JButton back = Theme.button("Back");
        back.addActionListener(entryValue -> {
            dispose();
        });
        container.add(Box.createVerticalStrut(SECTION_GAP));
        container.add(back);
        container.revalidate();
        container.repaint();

        final JScrollPane scroll = new JScrollPane(container);
        final JPanel scrollPanel = new JPanel();
        scrollPanel.setBackground(Theme.PAPER);
        scrollPanel.setLayout(new BorderLayout());
        scrollPanel.add(scroll, BorderLayout.CENTER);

        setContentPane(scrollPanel);
        pack();
        setLocationRelativeTo(owner);
        setVisible(true);

    }

    private static JPanel buildPlanPanel(final String plan) throws JsonProcessingException {
        final ObjectMapper mapper = new ObjectMapper();
        final List<Map<String, String>> washroomList = mapper.readValue(plan,
            new TypeReference<List<Map<String, String>>>() {
            });
        final JPanel planPanel = new JPanel(new GridBagLayout());
        planPanel.setBackground(Theme.PAPER);
        final GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.BOTH;
        constraints.insets = new Insets(FORM_INSET, FORM_INSET, FORM_INSET, FORM_INSET);
        constraints.weightx = 1.0;
        constraints.weighty = 0.0;
        final List<String> days = List.of("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun");
        for (int x = 0; x < days.size(); x++) {
            addDayColumn(planPanel, constraints, days.get(x), x, washroomList);
        }
        return planPanel;
    }

    private static void addDayColumn(final JPanel planPanel, final GridBagConstraints constraints, final String day,
                                     final int column, final List<Map<String, String>> washroomList) {
        constraints.gridx = column;
        constraints.gridy = 0;
        final JPanel dayPanel = new JPanel();
        dayPanel.add(Theme.label(day.toUpperCase(), BODY_FONT_SIZE, Theme.INK));
        planPanel.add(dayPanel, constraints);
        int row = 1;
        for (final Map<String, String> washroom : washroomList) {
            if (washroom.get("day").contains(day)) {
                row = addWashroomCard(planPanel, constraints, washroom, column, row);
            }
        }
    }

    private static int addWashroomCard(final JPanel planPanel, final GridBagConstraints constraints,
                                       final Map<String, String> washroom, final int column, final int row) {
        constraints.gridx = column;
        constraints.gridy = row;
        final JTextArea textArea = new JTextArea(washroom.get("name"));
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setEditable(false);
        textArea.setOpaque(false);
        textArea.setColumns(SECTION_GAP);
        final JPanel card = new JPanel(new FlowLayout());
        card.setBackground(Theme.PAPER);
        card.add(Theme.label(washroom.get("time"), BODY_FONT_SIZE, Theme.INK));
        card.add(textArea);
        planPanel.add(card, constraints);
        return row + 1;
    }

}
