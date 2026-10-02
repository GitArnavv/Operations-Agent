package com.aiops.agent;

public class ToolExecutionResult {
    private String toolName;
    private boolean success;
    private Object resultData;
    private String summary;
    private String errorMessage;
    private long executionTimeMs;

    public ToolExecutionResult() {}

    public ToolExecutionResult(String toolName, boolean success, Object resultData, String summary, String errorMessage, long executionTimeMs) {
        this.toolName = toolName;
        this.success = success;
        this.resultData = resultData;
        this.summary = summary;
        this.errorMessage = errorMessage;
        this.executionTimeMs = executionTimeMs;
    }

    public String getToolName() { return toolName; }
    public void setToolName(String toolName) { this.toolName = toolName; }

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public Object getResultData() { return resultData; }
    public void setResultData(Object resultData) { this.resultData = resultData; }

    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }

    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }

    public long getExecutionTimeMs() { return executionTimeMs; }
    public void setExecutionTimeMs(long executionTimeMs) { this.executionTimeMs = executionTimeMs; }
}
