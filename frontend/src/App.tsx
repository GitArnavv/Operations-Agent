import React from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { ThemeProvider } from './contexts/ThemeContext';
import { AuthProvider, useAuth } from './contexts/AuthContext';
import { AgentProvider } from './contexts/AgentContext';
import { ToastProvider } from './contexts/ToastContext';
import { RouteTransitionProvider } from './contexts/RouteTransitionContext';
import { ToastContainer } from './components/ui/ToastContainer';
import { ErrorBoundary } from './components/ui/ErrorBoundary';
import { AppLayout } from './components/layout/AppLayout';

// Auth Pages (Loaded synchronously for fast entry)
import { LoginPage } from './pages/LoginPage';
import { SignupPage } from './pages/SignupPage';

// Code-split Lazy Pages with Instant Suspense & Prefetch Support
const DashboardPage = React.lazy(() => import('./pages/DashboardPage').then(m => ({ default: m.DashboardPage })));
const AttentionPage = React.lazy(() => import('./pages/AttentionPage').then(m => ({ default: m.AttentionPage })));
const AgentPage = React.lazy(() => import('./pages/AgentPage').then(m => ({ default: m.AgentPage })));
const AnalyticsPage = React.lazy(() => import('./pages/AnalyticsPage').then(m => ({ default: m.AnalyticsPage })));
const OrdersPage = React.lazy(() => import('./pages/OrdersPage').then(m => ({ default: m.OrdersPage })));
const InventoryPage = React.lazy(() => import('./pages/InventoryPage').then(m => ({ default: m.InventoryPage })));
const SuppliersPage = React.lazy(() => import('./pages/SuppliersPage').then(m => ({ default: m.SuppliersPage })));
const CustomersPage = React.lazy(() => import('./pages/CustomersPage').then(m => ({ default: m.CustomersPage })));
const InvoicesPage = React.lazy(() => import('./pages/InvoicesPage').then(m => ({ default: m.InvoicesPage })));
const DocumentsPage = React.lazy(() => import('./pages/DocumentsPage').then(m => ({ default: m.DocumentsPage })));
const BillingPage = React.lazy(() => import('./pages/BillingPage').then(m => ({ default: m.BillingPage })));
const MonitoringPage = React.lazy(() => import('./pages/MonitoringPage').then(m => ({ default: m.MonitoringPage })));
const ApprovalsPage = React.lazy(() => import('./pages/ApprovalsPage').then(m => ({ default: m.ApprovalsPage })));
const AgentActivityPage = React.lazy(() => import('./pages/AgentActivityPage').then(m => ({ default: m.AgentActivityPage })));
const IntegrationsPage = React.lazy(() => import('./pages/IntegrationsPage').then(m => ({ default: m.IntegrationsPage })));
const TeamPage = React.lazy(() => import('./pages/TeamPage').then(m => ({ default: m.TeamPage })));
const AuditLogsPage = React.lazy(() => import('./pages/AuditLogsPage').then(m => ({ default: m.AuditLogsPage })));
const SettingsPage = React.lazy(() => import('./pages/SettingsPage').then(m => ({ default: m.SettingsPage })));

const ProtectedRoute: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const { token, isLoading } = useAuth();

  if (isLoading) {
    return (
      <div className="h-screen w-screen bg-background flex items-center justify-center text-muted-foreground font-sans">
        <div className="flex flex-col items-center gap-3">
          <div className="w-8 h-8 border-2 border-primary border-t-transparent rounded-full animate-spin"></div>
          <span className="text-xs font-mono">Initializing AIOps India...</span>
        </div>
      </div>
    );
  }

  if (!token) {
    return <Navigate to="/login" replace />;
  }

  return <>{children}</>;
};

export const App: React.FC = () => {
  return (
    <ErrorBoundary>
      <ThemeProvider>
        <ToastProvider>
          <BrowserRouter>
            <AuthProvider>
              <AgentProvider>
                <RouteTransitionProvider>
                  <Routes>
                    {/* Public Auth Routes */}
                    <Route path="/login" element={<LoginPage />} />
                    <Route path="/signup" element={<SignupPage />} />

                    {/* Protected Enterprise Routes with Elevated Persistent Shell */}
                    <Route
                      path="/"
                      element={
                        <ProtectedRoute>
                          <ErrorBoundary fallbackTitle="Workspace Section Interrupted">
                            <AppLayout />
                          </ErrorBoundary>
                        </ProtectedRoute>
                      }
                    >
                      <Route index element={<Navigate to="/dashboard" replace />} />
                      <Route path="dashboard" element={<DashboardPage />} />
                      <Route path="attention" element={<AttentionPage />} />
                      <Route path="agent" element={<AgentPage />} />
                      <Route path="analytics" element={<AnalyticsPage />} />
                      <Route path="orders" element={<OrdersPage />} />
                      <Route path="inventory" element={<InventoryPage />} />
                      <Route path="suppliers" element={<SuppliersPage />} />
                      <Route path="customers" element={<CustomersPage />} />
                      <Route path="invoices" element={<InvoicesPage />} />
                      <Route path="documents" element={<DocumentsPage />} />
                      <Route path="billing" element={<BillingPage />} />
                      <Route path="monitoring" element={<MonitoringPage />} />
                      <Route path="approvals" element={<ApprovalsPage />} />
                      <Route path="agent-activity" element={<AgentActivityPage />} />
                      <Route path="integrations" element={<IntegrationsPage />} />
                      <Route path="team" element={<TeamPage />} />
                      <Route path="audit-logs" element={<AuditLogsPage />} />
                      <Route path="settings" element={<SettingsPage />} />
                    </Route>

                    {/* Catch-all Fallback */}
                    <Route path="*" element={<Navigate to="/dashboard" replace />} />
                  </Routes>
                  <ToastContainer />
                </RouteTransitionProvider>
              </AgentProvider>
            </AuthProvider>
          </BrowserRouter>
        </ToastProvider>
      </ThemeProvider>
    </ErrorBoundary>
  );
};

export default App;
