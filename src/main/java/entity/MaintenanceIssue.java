package entity;

public enum MaintenanceIssue {
    NONE("No issue"), OUT_OF_PAPER("Out of paper"), CLOGGED("Clogged"), OUT_OF_SOAP("Out of soap"),
    BROKEN_FIXTURE("Broken fixture"), OTHER("Other");

    private final String label;

    MaintenanceIssue(final String label) {
        this.label = label;
    }

    @Override
    public String toString() {
        return label;
    }
}
