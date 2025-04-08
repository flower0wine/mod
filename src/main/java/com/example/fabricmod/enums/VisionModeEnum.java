package com.example.fabricmod.enums;

public enum VisionModeEnum {
    CROSSHAIR("crosshair", "准心瞄准"),
    VISION("vision", "视野范围");

    private final String id;
    private final String displayName;

    VisionModeEnum(String id, String displayName) {
        this.id = id;
        this.displayName = displayName;
    }

    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static VisionModeEnum fromId(String id) {
        for (VisionModeEnum mode : values()) {
            if (mode.getId().equals(id)) {
                return mode;
            }
        }
        return CROSSHAIR;
    }
} 