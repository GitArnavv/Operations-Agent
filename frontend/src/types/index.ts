export type UserRole =
  | 'OWNER'
  | 'ADMIN'
  | 'OPERATIONS_MANAGER'
  | 'INVENTORY_MANAGER'
  | 'PROCUREMENT_MANAGER'
  | 'FINANCE_MANAGER'
  | 'SALES_MANAGER'
  | 'ANALYST'
  | 'VIEWER';

export type OrderStatus =
  | 'CREATED'
  | 'PROCESSING'
  | 'PACKED'
  | 'SHIPPED'
  | 'IN_TRANSIT'
  | 'DELIVERED'
  | 'DELAYED'
  | 'CANCELLED'
  | 'DISPATCHED';

export type PaymentStatus = 'PENDING' | 'PAID' | 'PARTIAL' | 'OVERDUE' | 'FAILED';

export type RiskLevel = 'READ_ONLY' | 'LOW_RISK' | 'MEDIUM_RISK' | 'HIGH_RISK';

export type ApprovalStatus = 'PENDING' | 'APPROVED' | 'REJECTED' | 'EXECUTED' | 'CANCELLED';

export type AlertSeverity = 'CRITICAL' | 'HIGH' | 'MEDIUM' | 'LOW' | 'INFO' | 'WARNING';

export interface User {
  id: string;
  email: string;
  fullName: string;
  role: UserRole;
  tenantId: string;
}

export interface Organization {
  id: string;
  name: string;
  gstin: string;
  pan: string;
  state: string;
  city: string;
  industry: string;
  tier: string;
  currency: string;
  address: string;
}

export interface CitationEvidence {
  entityType: string;
  entityId: string;
  label: string;
  summary: string;
  confidence: number;
  deepLink: string;
  metadata?: Record<string, any>;
}

export interface ToolExecutionResult {
  toolName: string;
  success: boolean;
  resultData?: any;
  result?: any;
  summary?: string;
  errorMessage?: string;
  executionTimeMs: number;
}

export interface AgentActionProposal {
  id: string;
  actionType: string;
  riskLevel: RiskLevel;
  title: string;
  description: string;
  reason: string;
  estimatedCostInr: number;
  expectedImpact: string;
  affectedEntities: string;
  payload: Record<string, any>;
  requiresApproval: boolean;
}

export interface AgentMessage {
  id: string;
  role: 'USER' | 'AGENT' | 'SYSTEM';
  content: string;
  runId?: string;
  reasoningSummary?: string;
  citations: CitationEvidence[];
  proposedAction?: AgentActionProposal;
  actionProposal?: AgentActionProposal;
  toolExecutions: ToolExecutionResult[];
  timestamp: string;
}

export interface AttentionItem {
  id: string;
  type: string;
  severity: AlertSeverity;
  title: string;
  description: string;
  link: string;
  recommendedAction: string;
  entityId?: string;
  financialImpactInr?: number;
  promptToResolve?: string;
}

export interface DashboardKpis {
  totalRevenueInr: number;
  totalRevenueLakhs: string;
  totalOrdersCount: number;
  delayedOrdersCount: number;
  criticalStockCount: number;
  totalOverdueInr: number;
  totalOverdueLakhs: string;
  averageSupplierOnTimeRate: number;
  pendingApprovalsCount: number;
  activeAlertsCount: number;
  lowStockItemsCount?: number;
  overdueInvoicesAmountInr?: number;
  activeOrdersCount?: number;
  totalCatalogSkus?: number;
  overdueInvoicesCount?: number;
}

export interface DashboardData {
  attentionItems: AttentionItem[];
  kpis: DashboardKpis;
  pendingApprovals: ApprovalRequest[];
  recentAlerts: Alert[];
  recentAgentRuns: AgentRun[];
  delayedOrders?: any[];
  lowStockItems?: any[];
}

export interface SalesOrder {
  id: string;
  orderNumber: string;
  customerName: string;
  orderDate: string;
  promisedDeliveryDate: string;
  expectedDeliveryDate: string;
  status: OrderStatus;
  paymentStatus: PaymentStatus;
  totalAmount: number;
  totalAmountInr?: number;
  warehouseName?: string;
  deliveryRisk: string;
  delayDays: number;
  delayRootCause?: string;
  items?: SalesOrderItem[];
}

export interface SalesOrderItem {
  id: string;
  productName: string;
  sku: string;
  quantity: number;
  unitPrice: number;
  lineTotal: number;
}

export interface InventoryItem {
  id: string;
  productId: string;
  productName: string;
  sku: string;
  warehouseId: string;
  warehouseName: string;
  currentStock: number;
  reservedStock: number;
  availableStock: number;
  availableQuantity?: number;
  reservedQuantity?: number;
  daysOfStockRemaining: number;
  daysOfInventoryRemaining?: number;
  stockoutRisk: string;
  unitOfMeasure?: string;
  dailyDemandRate?: number;
  reorderPoint?: number;
}

export interface Product {
  id: string;
  sku: string;
  name: string;
  category: string;
  hsnCode: string;
  unit: string;
  unitPrice: number;
  gstRate: number;
  reorderPoint: number;
  safetyStock: number;
  leadTimeDays: number;
  avgDailyDemand: number;
  description: string;
}

export interface Warehouse {
  id: string;
  name: string;
  code: string;
  city: string;
  state: string;
  capacityUnits: number;
}

export interface Supplier {
  id: string;
  name: string;
  gstin: string;
  city: string;
  state: string;
  contactPerson: string;
  phone: string;
  leadTimeDays: number;
  averageLeadTimeDays?: number;
  onTimeDeliveryRate: number;
  defectRate: number;
  reliabilityScore: number;
  paymentTerms: string;
  category?: string;
}

export interface Customer {
  id: string;
  name: string;
  gstin: string;
  city: string;
  state: string;
  phone: string;
  creditLimit: number;
  outstandingAmount: number;
}

export interface Invoice {
  id: string;
  invoiceNumber: string;
  poNumber?: string;
  entityName: string;
  gstin: string;
  entityGstin?: string;
  invoiceDate: string;
  dueDate: string;
  subtotal: number;
  subtotalInr?: number;
  cgst: number;
  cgstAmountInr?: number;
  sgst: number;
  sgstAmountInr?: number;
  igst: number;
  igstAmountInr?: number;
  totalAmount: number;
  totalAmountInr?: number;
  paymentStatus: PaymentStatus;
  overdueDays: number;
  validationStatus: string;
  extractionConfidence: number;
}

export interface DocumentRecord {
  id: string;
  title: string;
  fileName: string;
  fileType: string;
  fileSize: number;
  fileSizeBytes?: number;
  status: string;
  extractionSummary: string;
  confidenceScore: number;
  createdAt: string;
}

export interface ApprovalRequest {
  id: string;
  title: string;
  actionType: string;
  riskLevel: RiskLevel;
  status: ApprovalStatus;
  description: string;
  reason: string;
  estimatedCostInr: number;
  expectedImpact: string;
  affectedEntities: string;
  proposedPayload?: string;
  decidedByUserId?: string;
  decidedAt?: string;
  createdAt: string;
}

export interface MonitoringRule {
  id: string;
  ruleName: string;
  name?: string;
  description?: string;
  severity?: string;
  naturalLanguagePrompt: string;
  metricType: string;
  operator: string;
  threshold: number;
  frequency: string;
  active: boolean;
}

export interface Alert {
  id: string;
  title: string;
  message: string;
  severity: AlertSeverity;
  entityType: string;
  entityId: string;
  recommendedAction: string;
  acknowledged: boolean;
  createdAt: string;
}

export interface AuditLog {
  id: string;
  actorName: string;
  action: string;
  entityType: string;
  entityId: string;
  beforeState?: string;
  afterState?: string;
  reason?: string;
  createdAt: string;
}

export interface Integration {
  id: string;
  providerName: string;
  category: string;
  status: string;
  enabled: boolean;
  lastSyncedAt?: string;
  syncStatusMessage?: string;
  configSummary?: string;
}

export interface AgentRun {
  id: string;
  userPrompt: string;
  finalResponse: string;
  currentStep: string;
  agentsUsed: string;
  toolsUsed: string;
  status: string;
  durationMs: number;
  startedAt: string;
}
