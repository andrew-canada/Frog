package views;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.util.function.BiConsumer;
import java.util.function.Function;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.SwingWorker;

import entity.GeoPoint;

final class LocationInputDialog extends JDialog {
    private static final int COORDINATE_FIELD_COLUMNS = 10;
    private static final int ADDRESS_FIELD_COLUMNS = 28;
    private static final int LABEL_FONT_SIZE = 14;
    private static final int FORM_INSET = 5;
    private final JTextField address;
    private final JTextField latitude;
    private final JTextField longitude;
    private final Function<String, GeoPoint> addressLookup;
    private final BiConsumer<Double, Double> onSave;

    LocationInputDialog(final Window owner, final Function<String, GeoPoint> addressLookup,
                               final BiConsumer<Double, Double> onSave, final Double currLat, final Double currLong,
                               final MapClicker mapClicker) {
        super(owner, "Set your location", ModalityType.APPLICATION_MODAL);
        this.addressLookup = addressLookup;
        this.onSave = onSave;
        final JPanel p = Theme.page();
        p.setLayout(new GridBagLayout());
        final GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(FORM_INSET, FORM_INSET, FORM_INSET, FORM_INSET);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 2;
        p.add(Theme.label("Enter an address or coordinates", LABEL_FONT_SIZE, Theme.INK), c);
        c.gridwidth = 1;
        c.gridy++;
        p.add(new JLabel("Address"), c);
        c.gridx = 1;
        address = new JTextField(ADDRESS_FIELD_COLUMNS);
        latitude = new JTextField(currLat.toString(), COORDINATE_FIELD_COLUMNS);
        longitude = new JTextField(currLong.toString(), COORDINATE_FIELD_COLUMNS);
        p.add(address, c);
        c.gridx = 0;
        c.gridy++;
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.NONE;
        mapClicker.addOnSave(onSave);
        mapClicker.addFrame(this);
        final JButton clickLocation = Theme.primary("Right click on map to select location");
        clickLocation.addActionListener(mapClicker);
        p.add(clickLocation, c);
        final JButton useAddress = Theme.primary("Use address");
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
        final JButton useCoordinates = Theme.primary("Use coordinates");
        c.gridx = 1;
        c.gridy++;
        c.anchor = GridBagConstraints.EAST;
        c.fill = GridBagConstraints.NONE;
        p.add(useCoordinates, c);
        useCoordinates.addActionListener(entryValue -> {
            useCoordinates();
        });
        useAddress.addActionListener(entryValue -> {
            useAddress(useAddress);
        });
        setContentPane(p);
        pack();
        setLocationRelativeTo(owner);
    }

    private void useCoordinates() {
        try {
            onSave.accept(Double.parseDouble(latitude
                .getText()
                .trim()), Double.parseDouble(longitude
                .getText()
                .trim()));
            dispose();
        }
        catch (final NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Enter numeric coordinates.", "Invalid coordinates",
                JOptionPane.WARNING_MESSAGE);
        }
    }

    private void useAddress(final JButton button) {
        final String query = address
            .getText()
            .trim();
        if (query.isBlank()) {
            JOptionPane.showMessageDialog(this, "Enter an address to search.", "Address required",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        button.setEnabled(false);
        button.setText("Searching...");
        new SwingWorker<GeoPoint, Void>() {
            @Override
            protected GeoPoint doInBackground() {
                return addressLookup.apply(query);
            }

            @Override
            protected void done() {
                try {
                    final GeoPoint point = get();
                    onSave.accept(point.latitude(), point.longitude());
                    dispose();
                }
                catch (final Exception failure) {
                    final Throwable cause;
                    if (failure.getCause() == null) {
                        cause = failure;
                    }
                    else {
                        cause = failure.getCause();
                    }
                    if (cause.getMessage() == null) {
                        JOptionPane.showMessageDialog(LocationInputDialog.this, "Address search failed.",
                            "Address search", JOptionPane.WARNING_MESSAGE);
                    }
                    else {
                        JOptionPane.showMessageDialog(LocationInputDialog.this, cause.getMessage(), "Address search",
                            JOptionPane.WARNING_MESSAGE);
                    }
                    button.setEnabled(true);
                    button.setText("Use address");
                }
            }
        }.execute();
    }
}
