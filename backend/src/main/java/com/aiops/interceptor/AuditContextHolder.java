package com.aiops.interceptor;

/**
 * Thread-local context holder for audit metadata:
 * client IP address, prompt context, request correlation ID, and acting agent ID.
 */
public class AuditContextHolder {

    private static final ThreadLocal<AuditContext> CONTEXT = ThreadLocal.withInitial(AuditContext::new);

    public static AuditContext get() {
        return CONTEXT.get();
    }

    public static String getClientIp() {
        return CONTEXT.get().getClientIp();
    }

    public static void setClientIp(String ip) {
        CONTEXT.get().setClientIp(ip);
    }

    public static String getPromptContext() {
        return CONTEXT.get().getPromptContext();
    }

    public static void setPromptContext(String prompt) {
        CONTEXT.get().setPromptContext(prompt);
    }

    public static String getActingAgentId() {
        return CONTEXT.get().getActingAgentId();
    }

    public static void setActingAgentId(String agentId) {
        CONTEXT.get().setActingAgentId(agentId);
    }

    public static String getRequestId() {
        return CONTEXT.get().getRequestId();
    }

    public static void setRequestId(String requestId) {
        CONTEXT.get().setRequestId(requestId);
    }

    public static void clear() {
        CONTEXT.remove();
    }

    public static class AuditContext {
        private String clientIp = "127.0.0.1";
        private String promptContext;
        private String actingAgentId = "GeminiFlashOperationsAgent";
        private String requestId;

        public String getClientIp() { return clientIp; }
        public void setClientIp(String clientIp) { this.clientIp = clientIp; }

        public String getPromptContext() { return promptContext; }
        public void setPromptContext(String promptContext) { this.promptContext = promptContext; }

        public String getActingAgentId() { return actingAgentId; }
        public void setActingAgentId(String actingAgentId) { this.actingAgentId = actingAgentId; }

        public String getRequestId() { return requestId; }
        public void setRequestId(String requestId) { this.requestId = requestId; }
    }
}
