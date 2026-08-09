package view;

import entity.GeoPoint;

import javax.swing.*;
import java.awt.*;
import java.util.function.BiConsumer;
import java.util.function.Function;

public final class LocationInputDialog extends JDialog {
    private final JTextField address;
    private final JTextField latitude;
    private final JTextField longitude;
    private final Function<String, GeoPoint> addressLookup;
    private final BiConsumer<Double, Double> onSave;

    public LocationInputDialog(Window owner, Function<String, GeoPoint> addressLookup, BiConsumer<Double, Double> onSave, Double currLat, Double currLong, MapClicker mapClicker) {
        super(owner, "Set your location", ModalityType.APPLICATION_MODAL);
        this.addressLookup = addressLookup;
        this.onSave = onSave;
        JPanel p = Theme.page();
        p.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(5, 5, 5, 5);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 2;
        p.add(Theme.label("Enter an address or coordinates", 14, Theme.INK), c);
        c.gridwidth = 1;
        c.gridy++;
        p.add(new JLabel("Address"), c);
        c.gridx = 1;
        address = new JTextField(28);
        latitude = new JTextField(currLat.toString(), 10);
        longitude = new JTextField(currLong.toString(), 10);
        p.add(address, c);
        c.gridx = 0;
        c.gridy++;
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.NONE;
        mapClicker.addOnSave(onSave);
        mapClicker.addFrame(this);
        JButton clickLocation = Theme.primary("Right click on map to select location");
        clickLocation.addActionListener(mapClicker);
        p.add(clickLocation, c);
        JButton useAddress = Theme.primary("Use address");
        c.gridx = 1;
        c.anchor = GridBagConstraints.EAST;
        c.fill = GridBagConstraints.NONE;
        p.add(useAddress, c);
        c.gridx = 0;
        c.gridy++;
        c.gridwidth = 2;
        c.fill = GridBagConstraints.HORIZONTAL;
        p.add(new JSeparator(), c);
        c.gridwidth = 1;
        c.gridy++;
        p.add(new JLabel("Latitude"), c);
        c.gridx = 1;
        p.add(latitude, c);
        c.gridx = 0;
        c.gridy++;
        p.add(new JLabel("Longitude"), c);
        c.gridx = 1;
        p.add(longitude, c);
        JButton useCoordinates = Theme.primary("Use coordinates");
        c.gridx = 1;
        c.gridy++;
        c.anchor = GridBagConstraints.EAST;
        c.fill = GridBagConstraints.NONE;
        p.add(useCoordinates, c);
        useCoordinates.addActionListener(e -> useCoordinates());
        useAddress.addActionListener(e -> useAddress(useAddress));
        setContentPane(p);
        pack();
        setLocationRelativeTo(owner);
    }

    private void useCoordinates() {
        try {
            onSave.accept(Double.parseDouble(latitude.getText().trim()), Double.parseDouble(longitude.getText().trim()));
            dispose();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Enter numeric coordinates.", "Invalid coordinates", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void useAddress(JButton button) {
        String query = address.getText().trim();
        if (query.isBlank()) {
            JOptionPane.showMessageDialog(this, "Enter an address to search.", "Address required", JOptionPane.WARNING_MESSAGE);
            return;
        }
        button.setEnabled(false);
        button.setText("Searching…");
        new SwingWorker<GeoPoint, Void>() {
            @Override
            protected GeoPoint doInBackground() {
                return addressLookup.apply(query);
            }

            @Override
            protected void done() {
                try {
                    GeoPoint point = get();
                    onSave.accept(point.latitude(), point.longitude());
                    dispose();
                } catch (Exception failure) {
                    Throwable cause = failure.getCause() == null ? failure : failure.getCause();
                    JOptionPane.showMessageDialog(LocationInputDialog.this, cause.getMessage() == null ? "Address search failed." : cause.getMessage(), "Address search", JOptionPane.WARNING_MESSAGE);
                    button.setEnabled(true);
                    button.setText("Use address");
                }
            }
        }.execute();
    }
}
