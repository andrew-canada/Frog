package view;

import javax.swing.*;
import java.awt.*;
import java.util.function.BiConsumer;

public final class LocationInputDialog extends JDialog{
    private final JTextField latitude=new JTextField("43.6629",10),longitude=new JTextField("-79.3957",10);
    public LocationInputDialog(Window owner,BiConsumer<Double,Double> onSave){super(owner,"Set your location",ModalityType.APPLICATION_MODAL);
        JPanel p=Theme.page();p.setLayout(new GridLayout(3,2,10,10));p.add(new JLabel("Latitude"));p.add(latitude);p.add(new JLabel("Longitude"));p.add(longitude);
        JButton save=Theme.primary("Use location");save.addActionListener(e->{try{onSave.accept(Double.parseDouble(latitude.getText()),Double.parseDouble(longitude.getText()));dispose();}catch(NumberFormatException ex){JOptionPane.showMessageDialog(this,"Enter numeric coordinates");}});
        p.add(new JLabel());p.add(save);setContentPane(p);pack();setLocationRelativeTo(owner);}
}
