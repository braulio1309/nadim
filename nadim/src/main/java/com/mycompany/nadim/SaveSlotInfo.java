package com.mycompany.nadim;

import java.util.Objects;

public class SaveSlotInfo {
    private final String fileName;
    private final String label;
    private final long createdAtMillis;

    public SaveSlotInfo(String fileName, String label, long createdAtMillis) {
        this.fileName = Objects.requireNonNull(fileName, "fileName");
        this.label = label == null ? "" : label;
        this.createdAtMillis = createdAtMillis;
    }

    public String getFileName() {
        return fileName;
    }

    public String getLabel() {
        return label;
    }

    public long getCreatedAtMillis() {
        return createdAtMillis;
    }

    public String getDisplayName() {
        String base = label == null || label.isBlank() ? "Sin nombre" : label.trim();
        return base + " (" + fileName + ")";
    }

    @Override
    public String toString() {
        return getDisplayName();
    }
}
