package views;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.util.concurrent.ExecutionException;
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

/**
 * Modal dialog for entering a location by address, coordinates, or a map click.
 */
final class LocationInputDialog extends JDialog {
    private static final int COORDINATE_FIELD_COLUMNS = 10;
    private static final int ADDRESS_FIELD_COLUMNS = 28;
    private static final int LABEL_FONT_SIZE = 14;
    private static final int FORM_INSET = 5;
    private static final String USE_ADDRESS = "useAddress";
    private static final String USE_COORDINATES = "useCoordinates";
    private JTextField address;
    private JTextField latitude;
    private JTextField longitude;
    private final Function<String, GeoPoint> addressLookup;
    private final BiConsumer<Double, Double> onSave;

    LocationInputDialog(final Window owner, final Function<String, GeoPoint> addressLookup,
                               final BiConsumer<Double, Double> onSave, final Double currLat, final Double currLong,
                               final MapClicker mapClicker) {
        super(owner, "Set your location", ModalityType.APPLICATION_MODAL);
        this.addressLookup = addressLookup;
        this.onSave = onSave;
        final JPanel p = buildPanel(mapClicker, currLat, currLong);
        final JButton useAddress = (JButton) p.getClientProperty(USE_ADDRESS);
        final JButton useCoordinates = (JButton) p.getClientProperty(USE_COORDINATES);
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

    private JPanel buildPanel(final MapClicker mapClicker, final Double currLat, final Double currLong) {
        final JPanel panel = Theme.page();
        panel.putClientProperty(USE_ADDRESS, buildAddressControls(panel, mapClicker, currLat, currLong));
        return panel;
    }

    private JButton buildAddressControls(final JPanel panel, final MapClicker mapClicker, final Double currLat,
                                         final Double currLong) {
        panel.setLayout(new GridBagLayout());
        final GridBagConstraints constraints = new GridBagConstraints();
        constraints.insets = new Insets(FORM_INSET, FORM_INSET, FORM_INSET, FORM_INSET);
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.gridwidth = 2;
        panel.add(Theme.label("Enter an address or coordinates", LABEL_FONT_SIZE, Theme.INK), constraints);
        addAddressField(panel, constraints, currLat, currLong);
        addLocationButtons(panel, constraints, mapClicker);
        addCoordinateFields(panel, constraints);
        return (JButton) panel.getClientProperty(USE_ADDRESS);
    }

    private void addAddressField(final JPanel panel, final GridBagConstraints constraints, final Double currLat,
                                 final Double currLong) {
        constraints.gridwidth = 1;
        constraints.gridy++;
        panel.add(new JLabel("Address"), constraints);
        constraints.gridx = 1;
        address = new JTextField(ADDRESS_FIELD_COLUMNS);
        latitude = new JTextField(currLat.toString(), COORDINATE_FIELD_COLUMNS);
        longitude = new JTextField(currLong.toString(), COORDINATE_FIELD_COLUMNS);
        panel.add(address, constraints);
    }

    private void addLocationButtons(final JPanel panel, final GridBagConstraints constraints,
                                    final MapClicker mapClicker) {
        constraints.gridx = 0;
        constraints.gridy++;
        constraints.anchor = GridBagConstraints.WEST;
        constraints.fill = GridBagConstraints.NONE;
        mapClicker.addOnSave(onSave);
        mapClicker.addFrame(this);
        final JButton clickLocation = Theme.primary("Right click on map to select location");
        clickLocation.addActionListener(mapClicker);
        panel.add(clickLocation, constraints);
        final JButton useAddress = Theme.primary("Use address");
        constraints.gridx = 1;
        constraints.anchor = GridBagConstraints.EAST;
        panel.add(useAddress, constraints);
        panel.putClientProperty(USE_ADDRESS, useAddress);
    }

    private void addCoordinateFields(final JPanel panel, final GridBagConstraints constraints) {
        constraints.gridx = 0;
        constraints.gridy++;
        constraints.gridwidth = 2;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        panel.add(new JSeparator(), constraints);
        constraints.gridwidth = 1;
        constraints.gridy++;
        panel.add(new JLabel("Latitude"), constraints);
        constraints.gridx = 1;
        panel.add(latitude, constraints);
        constraints.gridx = 0;
        constraints.gridy++;
        panel.add(new JLabel("Longitude"), constraints);
        constraints.gridx = 1;
        panel.add(longitude, constraints);
        final JButton useCoordinates = Theme.primary("Use coordinates");
        constraints.gridy++;
        constraints.anchor = GridBagConstraints.EAST;
        constraints.fill = GridBagConstraints.NONE;
        panel.add(useCoordinates, constraints);
        panel.putClientProperty(USE_COORDINATES, useCoordinates);
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
        }
        else {
            button.setEnabled(false);
            button.setText("Searching...");
            new SwingWorker<GeoPoint, Void>() {
                @Override
                protected GeoPoint doInBackground() {
                    return addressLookup.apply(query);
                }

                @Override
                protected void done() {
                    handleAddressResult(this, button);
                }
            }.execute();
        }
    }

    private void handleAddressResult(final SwingWorker<GeoPoint, Void> worker, final JButton button) {
        try {
            final GeoPoint point = worker.get();
            onSave.accept(point.latitude(), point.longitude());
            dispose();
        }
        catch (final InterruptedException interrupted) {
            Thread.currentThread().interrupt();
            showAddressFailure("Address search was interrupted.", button);
        }
        catch (final ExecutionException failure) {
            final Throwable cause = failure.getCause();
            final String message;
            if (cause == null || cause.getMessage() == null) {
                message = "Address search failed.";
            }
            else {
                message = cause.getMessage();
            }
            showAddressFailure(message, button);
        }
    }

    private void showAddressFailure(final String message, final JButton button) {
        JOptionPane.showMessageDialog(this, message, "Address search", JOptionPane.WARNING_MESSAGE);
        button.setEnabled(true);
        button.setText("Use address");
    }
}
