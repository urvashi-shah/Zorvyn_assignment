package com.taskmanager.model;

/**
 * Beginner note:
 * This is a small embedded object saved inside a Task document (nested JSON).
 * We keep both "summary" and "what to do first" under a single "suggestion" field.
 */
public class AiSuggestion {
    private String summary;
    private String firstStep;

    public AiSuggestion() {
    }

    public AiSuggestion(String summary, String firstStep) {
        this.summary = summary;
        this.firstStep = firstStep;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getFirstStep() {
        return firstStep;
    }

    public void setFirstStep(String firstStep) {
        this.firstStep = firstStep;
    }
}

