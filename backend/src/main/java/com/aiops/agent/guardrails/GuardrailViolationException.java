package com.aiops.agent.guardrails;

public class GuardrailViolationException extends RuntimeException {

    private final String violationCode;

    public GuardrailViolationException(String violationCode, String message) {
        super(message);
        this.violationCode = violationCode;
    }

    public String getViolationCode() {
        return violationCode;
    }
}
