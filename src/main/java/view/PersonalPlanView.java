package view;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import interface_adapter.report_review.ReportReviewController;
import interface_adapter.report_review.ReportReviewViewModel;
import use_case.account.personal_plan.PersonalPlanInteractor;

import javax.swing.*;
import java.awt.*;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

public final class PersonalPlanView extends JDialog {

    public PersonalPlanView(Window owner, String plan) {

        super(owner, "Personal Washroom Schedule", ModalityType.APPLICATION_MODAL);

        JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        container.setBackground(Theme.PAPER);

        try {
            ObjectMapper mapper = new ObjectMapper();
            List<PersonalPlanInteractor.WashroomPlan> washroomList = mapper.readValue(plan, new TypeReference<List<PersonalPlanInteractor.WashroomPlan>>() {});

            List<String> days = List.of("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun");

            JPanel planPanel = new JPanel(new GridBagLayout());
            planPanel.setBackground(Theme.PAPER);
            GridBagConstraints constraints = new GridBagConstraints();
            constraints.fill = GridBagConstraints.BOTH;
            constraints.insets = new Insets(5, 5, 5, 5);
            constraints.weightx = 1.0;
            constraints.weighty = 0.0;

            for (int x = 0; x < days.size(); x++) {
                String day = days.get(x);
                constraints.gridx = x;
                constraints.gridy = 0;
                JPanel dayPanel = new JPanel();
                dayPanel.add(Theme.label(day.toUpperCase(), 14, Theme.INK));
                planPanel.add(dayPanel, constraints);
                int y = 1;
                for (PersonalPlanInteractor.WashroomPlan washroom : washroomList) {
                    if (washroom.day.contains(day)) {
                        System.out.println(y);
                        constraints.gridx = x;
                        constraints.gridy = y;
                        JTextArea textArea = new JTextArea(washroom.washroom);
                        textArea.setLineWrap(true);
                        textArea.setWrapStyleWord(true);
                        textArea.setEditable(false);
                        textArea.setOpaque(false);
                        textArea.setColumns(10);
                        JPanel card = new JPanel(new FlowLayout());
                        card.setBackground(Theme.PAPER);
                        card.add(Theme.label(washroom.time, 14,  Theme.INK));
                        card.add(textArea);
                        planPanel.add(card, constraints);
                        y++;
                    }
                }

            }

            JPanel personalPlanTitle = new JPanel();
            personalPlanTitle.setLayout(new FlowLayout(FlowLayout.LEFT));
            personalPlanTitle.setBackground(Theme.PAPER);
            personalPlanTitle.add(Theme.title("Personal Washroom Plan"));
            container.add(personalPlanTitle);
            container.add(Box.createVerticalStrut(10));
            container.add(planPanel);

        } catch (Exception e) {

            container.add(Theme.title("Error - Please try generating a new plan"));

        }

        JButton back = Theme.button("Back");
        back.addActionListener(e -> dispose());
        container.add(Box.createVerticalStrut(10));
        container.add(back);
        container.revalidate();
        container.repaint();

        JScrollPane scroll = new JScrollPane(container);
        JPanel scrollPanel = new JPanel();
        scrollPanel.setBackground(Theme.PAPER);
        scrollPanel.setLayout(new BorderLayout());
        scrollPanel.add(scroll, BorderLayout.CENTER);

        setContentPane(scrollPanel);
        pack();
        setLocationRelativeTo(owner);
        setVisible(true);

    }

}
