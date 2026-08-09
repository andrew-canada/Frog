package views;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.util.HashMap;
import java.util.List;

public final class PersonalPlanView extends JDialog {

    public PersonalPlanView(final Window owner, final String plan) {

        super(owner, "Personal Washroom Schedule", ModalityType.APPLICATION_MODAL);

        final JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        container.setBackground(Theme.PAPER);

        try {
            final ObjectMapper mapper = new ObjectMapper();
            final List<HashMap<String, String>> washroomList =
                mapper.readValue(plan, new TypeReference<List<HashMap<String, String>>>() {
                });

            final List<String> days = List.of("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun");

            final JPanel planPanel = new JPanel(new GridBagLayout());
            planPanel.setBackground(Theme.PAPER);
            final GridBagConstraints constraints = new GridBagConstraints();
            constraints.fill = GridBagConstraints.BOTH;
            constraints.insets = new Insets(5, 5, 5, 5);
            constraints.weightx = 1.0;
            constraints.weighty = 0.0;

            for (int x = 0; x < days.size(); x++) {
                final String day = days.get(x);
                constraints.gridx = x;
                constraints.gridy = 0;
                final JPanel dayPanel = new JPanel();
                dayPanel.add(Theme.label(day.toUpperCase(), 14, Theme.INK));
                planPanel.add(dayPanel, constraints);
                int y = 1;
                for (final HashMap<String, String> washroom : washroomList) {
                    if (washroom
                        .get("day")
                        .contains(day)) {
                        System.out.println(y);
                        constraints.gridx = x;
                        constraints.gridy = y;
                        final JTextArea textArea = new JTextArea(washroom.get("name"));
                        textArea.setLineWrap(true);
                        textArea.setWrapStyleWord(true);
                        textArea.setEditable(false);
                        textArea.setOpaque(false);
                        textArea.setColumns(10);
                        final JPanel card = new JPanel(new FlowLayout());
                        card.setBackground(Theme.PAPER);
                        card.add(Theme.label(washroom.get("time"), 14, Theme.INK));
                        card.add(textArea);
                        planPanel.add(card, constraints);
                        y++;
                    }
                }

            }

            final JPanel personalPlanTitle = new JPanel();
            personalPlanTitle.setLayout(new FlowLayout(FlowLayout.LEFT));
            personalPlanTitle.setBackground(Theme.PAPER);
            personalPlanTitle.add(Theme.title("Personal Washroom Plan"));
            container.add(personalPlanTitle);
            container.add(Box.createVerticalStrut(10));
            container.add(planPanel);

        } catch (final Exception e) {

            container.add(Theme.title("Error - Please try generating a new plan"));

        }

        final JButton back = Theme.button("Back");
        back.addActionListener(e -> dispose());
        container.add(Box.createVerticalStrut(10));
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

}
