import { apiClient } from './client';
import type {
  AgentMessage,
  AgentRun,
  Alert,
  ApprovalRequest,
  AuditLog,
  Customer,
  DashboardData,
  DocumentRecord,
  Integration,
  InventoryItem,
  Invoice,
  MonitoringRule,
  Organization,
  Product,
  SalesOrder,
  Supplier,
  User,
  Warehouse,
} from '../types';

export const authApi = {
  login: (email: string, password: string) =>
    apiClient<{ token: string; user: User; organization: Organization }>('/auth/login', {
      method: 'POST',
      body: JSON.stringify({ email, password }),
    }),
  signup: (payload: { email: string; password: string; fullName: string; orgName?: string; gstin?: string }) =>
    apiClient<{ token: string; user: User; organization: Organization }>('/auth/signup', {
      method: 'POST',
      body: JSON.stringify(payload),
    }),
  me: () => apiClient<User>('/auth/me'),
};

export const dashboardApi = {
  getDashboard: () => apiClient<DashboardData>('/dashboard'),
};

export const agentApi = {
  chat: (prompt: string, conversationId = 'conv_default') =>
    apiClient<AgentMessage>('/agent/chat', {
      method: 'POST',
      body: JSON.stringify({ prompt, conversationId }),
    }),
  getRuns: () => apiClient<AgentRun[]>('/agent/runs'),
  getRunById: (id: string) => apiClient<AgentRun>(`/agent/runs/${id}`),
};

export const ordersApi = {
  getOrders: () => apiClient<SalesOrder[]>('/orders'),
  getOrderById: (id: string) => apiClient<SalesOrder>(`/orders/${id}`),
  getOrderByNumber: (num: string) => apiClient<SalesOrder>(`/orders/number/${num}`),
};

export const inventoryApi = {
  getInventory: () => apiClient<InventoryItem[]>('/inventory'),
  getProducts: () => apiClient<Product[]>('/inventory/products'),
  getProductById: (id: string) => apiClient<Product>(`/inventory/products/${id}`),
  getWarehouses: () => apiClient<Warehouse[]>('/inventory/warehouses'),
};

export const suppliersApi = {
  getSuppliers: () => apiClient<Supplier[]>('/suppliers'),
  getSupplierById: (id: string) => apiClient<Supplier>(`/suppliers/${id}`),
};

export const customersApi = {
  getCustomers: () => apiClient<Customer[]>('/customers'),
  getCustomerById: (id: string) => apiClient<Customer>(`/customers/${id}`),
};

export const invoicesApi = {
  getInvoices: () => apiClient<Invoice[]>('/invoices'),
  getInvoiceByNumber: (num: string) => apiClient<Invoice>(`/invoices/${num}`),
  getOverdue: () => apiClient<Invoice[]>('/invoices/overdue'),
};

export const documentsApi = {
  getDocuments: () => apiClient<DocumentRecord[]>('/documents'),
  upload: (title: string, fileName: string, fileType = 'PDF', fileSize = 450000) =>
    apiClient<DocumentRecord>('/documents/upload', {
      method: 'POST',
      body: JSON.stringify({ title, fileName, fileType, fileSize }),
    }),
};

export const approvalsApi = {
  getApprovals: () => apiClient<ApprovalRequest[]>('/approvals'),
  approve: (id: string, notes?: string) =>
    apiClient<ApprovalRequest>(`/approvals/${id}/approve`, {
      method: 'POST',
      body: JSON.stringify({ notes }),
    }),
  reject: (id: string, reason?: string) =>
    apiClient<ApprovalRequest>(`/approvals/${id}/reject`, {
      method: 'POST',
      body: JSON.stringify({ reason }),
    }),
};

export const monitoringApi = {
  getRules: () => apiClient<MonitoringRule[]>('/monitoring/rules'),
  createRule: (prompt: string) =>
    apiClient<MonitoringRule>('/monitoring/rules', {
      method: 'POST',
      body: JSON.stringify({ prompt }),
    }),
  getAlerts: () => apiClient<Alert[]>('/monitoring/alerts'),
  evaluate: () => apiClient<{ evaluatedRulesCount: number; alertsGeneratedCount: number }>('/monitoring/evaluate', {
    method: 'POST',
  }),
  acknowledgeAlert: (id: string) => apiClient<Alert>(`/monitoring/alerts/${id}/ack`, { method: 'POST' }),
};

export const auditLogsApi = {
  getAuditLogs: () => apiClient<AuditLog[]>('/audit-logs'),
};

export const integrationsApi = {
  getIntegrations: () => apiClient<Integration[]>('/integrations'),
  sync: (id: string) => apiClient<Integration>(`/integrations/${id}/sync`, { method: 'POST' }),
  toggle: (id: string, enabled: boolean) =>
    apiClient<Integration>(`/integrations/${id}/toggle`, {
      method: 'POST',
      body: JSON.stringify({ enabled }),
    }),
};

export const voiceApi = {
  synthesize: (text: string, language = 'en-IN') =>
    apiClient<{ status: string; language: string; text: string; pitch: number; rate: number }>('/voice/synthesize', {
      method: 'POST',
      body: JSON.stringify({ text, language }),
    }),
};
