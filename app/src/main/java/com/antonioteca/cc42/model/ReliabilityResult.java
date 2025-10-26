package com.antonioteca.cc42.model;

public class ReliabilityResult {
    private final String level;
    private final String color;
    private final int percentage;
    private final String message;

    public ReliabilityResult(String level, String color, int percentage, String message) {
        this.level = level;
        this.color = color;
        this.percentage = percentage;
        this.message = message;
    }

    // Getters
    public String getLevel() {
        return level;
    }

    public String getColor() {
        return color;
    }

    public int getPercentage() {
        return percentage;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return "ReliabilityResult{" +
                "level='" + level + '\'' +
                ", color='" + color + '\'' +
                ", percentage=" + percentage +
                ", message='" + message + '\'' +
                '}';
    }
}
