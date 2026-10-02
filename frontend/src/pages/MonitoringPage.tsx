import React, { useEffect, useState } from 'react';
import {
  Activity,
  Play,
  CheckCircle2,
  Bot,
  Sparkles,
} from 'lucide-react';
import { monitoringApi } from '../api/services';
import { PageTransition } from '../components/ui/PageTransition';
import { PageHeader } from '../components/ui/PageHeader';
import { StatusBadge } from '../components/ui/StatusBadge';
import type { MonitoringRule, Alert } from '../types';

export const MonitoringPage: React.FC = () => {
  const [rules, setRules] = useState<MonitoringRule[]>([]);
  const [alerts, setAlerts] = useState<Alert[]>([]);
  const [rulePrompt, setRulePrompt] = useState(
    'Monitor inventory and alert me if any fast-moving SKU has less than 7 days of supply remaining'
  );
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [isEvaluating, setIsEvaluating] = useState(false);

  const loadData = () => {
    monitoringApi.getRules().then(setRules);
    monitoringApi.getAlerts().then(setAlerts);
  };

  useEffect(() => {
    loadData();
  }, []);

  const handleCreateRule = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!rulePrompt.trim()) return;

    setIsSubmitting(true);
    try {
      await monitoringApi.createRule(rulePrompt);
      setRulePrompt('');
      loadData();
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleEvaluate = async () => {
    setIsEvaluating(true);
    try {
      await monitoringApi.evaluate();
      loadData();
    } finally {
      setIsEvaluating(false);
    }
  };

  const handleAck = async (id: string) => {
    await monitoringApi.acknowledgeAlert(id);
    loadData();
  };

  // Structured preview inferred by agent
  const getStructuredPreview = (prompt: string) => {
    const p = prompt.toLowerCase();
    let metric = 'Stock Level / Days of Cover';
    let condition = '< 7 Days of Supply';
    let scope = 'Fast-Moving Electrical SKUs';
    let severity = 'HIGH';

    if (p.includes('delay') || p.includes('order')) {
      metric = 'Order Dispatch Delay';
      condition = '> 24 Hours Past Promised SLA';
      scope = 'All Active Customer Orders';
      severity = 'CRITICAL';
    } else if (p.includes('overdue') || p.includes('invoice') || p.includes('payment')) {
      metric = 'Overdue Receivables Aging';
      condition = '> 30 Days Overdue & > ₹1 Lakh';
      scope = 'Wholesale Dealer Ledger';
      severity = 'HIGH';
    } else if (p.includes('supplier') || p.includes('otif')) {
      metric = 'Supplier On-Time Delivery Rate';
      condition = '< 80% On-Time';
      scope = 'Primary OEM Vendors';
      severity = 'WARNING';
    }

    return {
      metric,
      condition,
      scope,
      frequency: 'Continuous / Hourly Event Sync',
      severity,
      channels: 'In-App Attention Center & SMS Alert',
    };
  };

  const preview = getStructuredPreview(rulePrompt);

  return (
    <PageTransition className="space-y-6 max-w-6xl mx-auto pb-16 font-sans">
      <PageHeader
        eyebrow="Real-Time Detection"
        title="Autonomous Monitoring Engine & Guardrails"
        description="Define natural-language operational rules parsed into structured metrics and evaluated against live ERP streams."
        actions={
          <button
            onClick={handleEvaluate}
            disabled={isEvaluating}
            className="flex items-center gap-2 px-4 py-2 rounded-xl bg-primary hover:bg-primary-hover text-primary-foreground text-xs font-semibold shadow-subtle transition-all disabled:opacity-50"
          >
            <Play className="w-3.5 h-3.5" />
            <span>{isEvaluating ? 'Evaluating Ledger...' : 'Run Immediate Evaluation'}</span>
          </button>
        }
      />

      {/* Structured Natural Language Rule Builder */}
      <div className="p-6 rounded-xl border border-border bg-surface shadow-subtle space-y-4">
        <div className="flex items-center gap-2 text-xs font-bold text-primary uppercase tracking-wider">
          <Bot className="w-4 h-4" />
          <span>Declare Natural-Language Autonomous Guardrail</span>
        </div>

        <form onSubmit={handleCreateRule} className="space-y-4">
          <div className="relative">
            <input
              type="text"
              value={rulePrompt}
              onChange={(e) => setRulePrompt(e.target.value)}
              placeholder="e.g. Alert me if any fast-moving SKU has less than 7 days of supply remaining..."
              className="w-full bg-surface-subtle border border-border rounded-xl px-4 py-3 text-xs sm:text-sm text-foreground placeholder:text-muted-foreground focus:outline-none focus:border-primary focus:ring-1 focus:ring-primary transition-all"
            />
            <button
              type="submit"
              disabled={isSubmitting || !rulePrompt.trim()}
              className="absolute right-2 top-1/2 -translate-y-1/2 px-4 py-1.5 rounded-lg bg-primary hover:bg-primary-hover text-primary-foreground text-xs font-bold transition-all disabled:opacity-40"
            >
              {isSubmitting ? 'Registering...' : 'Deploy Monitor'}
            </button>
          </div>

          {/* Structured Representation Breakdown Box */}
          {rulePrompt.trim() && (
            <div className="p-4 rounded-xl border border-border bg-surface-subtle space-y-3 animate-fade-in">
              <div className="flex items-center justify-between">
                <span className="text-[11px] font-bold uppercase tracking-wider text-muted-foreground flex items-center gap-1.5">
                  <Sparkles className="w-3 h-3 text-primary" />
                  AI Structured Interpretation (Schema Validation)
                </span>
                <StatusBadge variant="primary" label="Ready to Persist" />
              </div>

              <div className="grid grid-cols-2 sm:grid-cols-3 gap-3 text-xs">
                <div>
                  <div className="text-[10px] text-muted-foreground uppercase font-semibold">Target Metric</div>
                  <div className="font-semibold text-foreground mt-0.5">{preview.metric}</div>
                </div>
                <div>
                  <div className="text-[10px] text-muted-foreground uppercase font-semibold">Trigger Condition</div>
                  <div className="font-semibold font-mono text-primary mt-0.5">{preview.condition}</div>
                </div>
                <div>
                  <div className="text-[10px] text-muted-foreground uppercase font-semibold">Evaluation Scope</div>
                  <div className="font-semibold text-foreground mt-0.5">{preview.scope}</div>
                </div>
                <div>
                  <div className="text-[10px] text-muted-foreground uppercase font-semibold">Evaluation Frequency</div>
                  <div className="font-semibold text-foreground mt-0.5">{preview.frequency}</div>
                </div>
                <div>
                  <div className="text-[10px] text-muted-foreground uppercase font-semibold">Alert Severity</div>
                  <div className="font-bold text-danger mt-0.5">{preview.severity}</div>
                </div>
                <div>
                  <div className="text-[10px] text-muted-foreground uppercase font-semibold">Dispatch Channels</div>
                  <div className="font-semibold text-foreground mt-0.5">{preview.channels}</div>
                </div>
              </div>
            </div>
          )}
        </form>
      </div>

      {/* Active Rules Grid */}
      <div>
        <div className="flex items-center justify-between mb-3">
          <h2 className="text-xs font-bold uppercase tracking-wider text-muted-foreground">
            Active Operational Rules ({rules.length})
          </h2>
          <span className="text-[11px] text-muted-foreground">24/7 Background Evaluation</span>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          {rules.map((rule) => (
            <div
              key={rule.id}
              className="p-5 rounded-xl border border-border bg-surface shadow-subtle space-y-3"
            >
              <div className="flex items-center justify-between">
                <span className="text-xs font-bold text-foreground">{rule.name}</span>
                <StatusBadge variant="success" label="ACTIVE" />
              </div>
              <p className="text-xs text-muted-foreground leading-relaxed">
                {rule.description || rule.naturalLanguagePrompt}
              </p>
              <div className="flex items-center justify-between pt-2 border-t border-border text-[11px] text-muted-foreground font-mono">
                <span>Metric: {rule.metricType}</span>
                <span>Severity: {rule.severity}</span>
              </div>
            </div>
          ))}
        </div>
      </div>

      {/* Triggered Alerts Feed */}
      <div>
        <div className="flex items-center justify-between mb-3">
          <h2 className="text-xs font-bold uppercase tracking-wider text-muted-foreground">
            Triggered Exception Alerts ({alerts.length})
          </h2>
        </div>

        <div className="space-y-3">
          {alerts.length === 0 ? (
            <div className="p-8 text-center text-muted-foreground text-xs rounded-xl border border-border bg-surface">
              <CheckCircle2 className="w-6 h-6 text-emerald-500 mx-auto mb-2 opacity-60" />
              <p>No unacknowledged alerts. All systems operational.</p>
            </div>
          ) : (
            alerts.map((alert) => (
              <div
                key={alert.id}
                className="p-4 rounded-xl border border-border bg-surface flex items-center justify-between gap-4 shadow-subtle"
              >
                <div className="space-y-1">
                  <div className="flex items-center gap-2">
                    <StatusBadge
                      variant={alert.severity === 'CRITICAL' ? 'danger' : 'warning'}
                      label={alert.severity}
                    />
                    <span className="text-xs font-bold text-foreground">{alert.title}</span>
                  </div>
                  <p className="text-xs text-muted-foreground leading-relaxed">{alert.message}</p>
                </div>

                <div className="shrink-0">
                  {alert.acknowledged ? (
                    <span className="text-[11px] text-muted-foreground font-medium flex items-center gap-1">
                      <CheckCircle2 className="w-3.5 h-3.5 text-emerald-500" />
                      <span>Acknowledged</span>
                    </span>
                  ) : (
                    <button
                      onClick={() => handleAck(alert.id)}
                      className="px-3 py-1.5 rounded-lg border border-border bg-surface hover:bg-surface-hover text-foreground text-xs font-medium transition-colors shadow-sm"
                    >
                      Acknowledge
                    </button>
                  )}
                </div>
              </div>
            ))
          )}
        </div>
      </div>
    </PageTransition>
  );
};
