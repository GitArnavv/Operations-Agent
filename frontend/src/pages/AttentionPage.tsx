import React, { useEffect, useState } from 'react';
import {
  AlertTriangle,
  ArrowRight,
  Bot,
  CheckCircle2,
  Clock,
  UserCheck,
  BellOff,
  Check,
} from 'lucide-react';
import { dashboardApi } from '../api/services';
import { useAgent } from '../contexts/AgentContext';
import { PageTransition } from '../components/ui/PageTransition';
import { PageHeader } from '../components/ui/PageHeader';
import { StatusBadge } from '../components/ui/StatusBadge';
import { SpotlightCard } from '../components/ui/SpotlightCard';
import { MotionButton } from '../components/ui/MotionButton';
import { StaggerContainer, StaggerItem } from '../components/ui/StaggerContainer';
import type { AttentionItem } from '../types';

interface ExtendedAttentionItem extends AttentionItem {
  status?: 'ACTIVE' | 'ACKNOWLEDGED' | 'RESOLVED' | 'SNOOZED';
  assignedTo?: string;
  deadline?: string;
}

export const AttentionPage: React.FC = () => {
  const [items, setItems] = useState<ExtendedAttentionItem[]>([]);
  const [filter, setFilter] = useState<'ALL' | 'CRITICAL' | 'HIGH' | 'RESOLVED'>('ALL');
  const [loading, setLoading] = useState(true);
  const { sendMessage, setIsDrawerOpen } = useAgent();

  useEffect(() => {
    dashboardApi.getDashboard().then((res) => {
      const enhanced = (res.attentionItems || []).map((item) => ({
        ...item,
        status: 'ACTIVE' as const,
        assignedTo: 'Amit Patel (Operations)',
        deadline: item.severity === 'CRITICAL' ? 'Today, 5:00 PM' : 'Tomorrow',
      }));
      setItems(enhanced);
      setLoading(false);
    });
  }, []);

  const handleInvestigate = (prompt: string) => {
    sendMessage(prompt);
    setIsDrawerOpen(true);
  };

  const handleAcknowledge = (id: string) => {
    setItems((prev) =>
      prev.map((item) => (item.id === id ? { ...item, status: 'ACKNOWLEDGED' } : item))
    );
  };

  const handleResolve = (id: string) => {
    setItems((prev) =>
      prev.map((item) => (item.id === id ? { ...item, status: 'RESOLVED' } : item))
    );
  };

  const handleSnooze = (id: string) => {
    setItems((prev) =>
      prev.map((item) => (item.id === id ? { ...item, status: 'SNOOZED' } : item))
    );
  };

  const filtered = items.filter((item) => {
    if (filter === 'CRITICAL') return item.severity === 'CRITICAL';
    if (filter === 'HIGH') return item.severity === 'WARNING';
    if (filter === 'RESOLVED') return item.status === 'RESOLVED';
    return item.status !== 'RESOLVED';
  });

  return (
    <PageTransition className="space-y-6 max-w-6xl mx-auto pb-16 font-sans">
      <PageHeader
        eyebrow="Triage & Exception Queue"
        title="Operational Attention Center"
        description="Autonomous exception detector for supply chain disruptions, delivery SLA breaches, and financial delays."
        actions={
          <div className="flex rounded-xl bg-surface p-1 border border-border text-xs shadow-subtle">
            {(['ALL', 'CRITICAL', 'HIGH', 'RESOLVED'] as const).map((tab) => (
              <button
                key={tab}
                onClick={() => setFilter(tab)}
                className={`px-3 py-1 rounded-lg transition-colors font-medium ${
                  filter === tab
                    ? 'bg-primary text-primary-foreground font-semibold shadow-sm'
                    : 'text-muted-foreground hover:text-foreground'
                }`}
              >
                {tab.charAt(0) + tab.slice(1).toLowerCase()}
              </button>
            ))}
          </div>
        }
      />

      {/* Exception Items List */}
      <StaggerContainer className="space-y-4">
        {filtered.length === 0 ? (
          <StaggerItem>
            <div className="p-12 text-center text-muted-foreground text-xs rounded-xl border border-border bg-surface">
              <CheckCircle2 className="w-8 h-8 text-emerald-500 mx-auto mb-2 opacity-60" />
              <p>No active operational exceptions matching this filter.</p>
            </div>
          </StaggerItem>
        ) : (
          filtered.map((item) => {
            const isResolved = item.status === 'RESOLVED';
            const isAcknowledged = item.status === 'ACKNOWLEDGED';

            return (
              <StaggerItem key={item.id}>
                <SpotlightCard
                  className={`p-6 flex flex-col md:flex-row md:items-center justify-between gap-5 ${
                    isResolved ? 'opacity-60' : ''
                  }`}
                >
                  <div className="space-y-2 max-w-2xl flex-1">
                    <div className="flex flex-wrap items-center gap-2">
                      <StatusBadge
                        variant={item.severity === 'CRITICAL' ? 'danger' : 'warning'}
                        label={`${item.severity} • ${item.type}`}
                      />
                      <span className="text-[11px] font-mono text-muted-foreground px-2 py-0.5 rounded bg-surface-subtle">
                        {item.entityId}
                      </span>
                      {item.financialImpactInr != null && item.financialImpactInr > 0 && (
                        <span className="text-xs font-bold font-mono text-primary">
                          ₹{((item.financialImpactInr ?? 0) / 100000).toFixed(2)} Lakh Risk
                        </span>
                      )}
                      {isAcknowledged && (
                        <StatusBadge variant="primary" label="ACKNOWLEDGED" />
                      )}
                    </div>

                    <h2 className="text-base font-bold text-foreground leading-snug">{item.title}</h2>
                    <p className="text-xs text-muted-foreground leading-relaxed">{item.description}</p>

                    <div className="flex flex-wrap items-center gap-4 text-[11px] pt-2">
                      <div className="flex items-center gap-1 text-muted-foreground">
                        <Clock className="w-3 h-3 text-primary" />
                        <span>Deadline: <strong className="text-foreground">{item.deadline}</strong></span>
                      </div>
                      <div className="flex items-center gap-1 text-muted-foreground">
                        <UserCheck className="w-3 h-3 text-primary" />
                        <span>Owner: <strong className="text-foreground">{item.assignedTo}</strong></span>
                      </div>
                      <div className="text-primary font-medium">
                        AI Remedy: {item.recommendedAction}
                      </div>
                    </div>
                  </div>

                  {/* Exception Actions */}
                  <div className="flex flex-wrap items-center gap-2 shrink-0 pt-2 md:pt-0">
                    {!isResolved && (
                      <>
                        <MotionButton
                          variant="secondary"
                          size="sm"
                          onClick={() => handleAcknowledge(item.id)}
                          disabled={isAcknowledged}
                          className="disabled:opacity-40"
                        >
                          Acknowledge
                        </MotionButton>
                        <MotionButton
                          variant="ghost"
                          size="sm"
                          onClick={() => handleSnooze(item.id)}
                          title="Snooze 24 hours"
                        >
                          <BellOff className="w-3.5 h-3.5 text-muted-foreground" />
                        </MotionButton>
                        <MotionButton
                          variant="secondary"
                          size="sm"
                          onClick={() => handleResolve(item.id)}
                          className="!border-[var(--status-success-border)] !bg-[var(--status-success-bg)] !text-[var(--status-success-text)] font-semibold"
                        >
                          <Check className="w-3.5 h-3.5 mr-1" />
                          <span>Resolve</span>
                        </MotionButton>
                      </>
                    )}
                    <MotionButton
                      variant="primary"
                      size="sm"
                      onClick={() => handleInvestigate(item.promptToResolve || `Investigate ${item.title}`)}
                      shimmer={true}
                      className="flex items-center gap-2 px-4"
                    >
                      <Bot className="w-3.5 h-3.5" />
                      <span>Investigate</span>
                      <ArrowRight className="w-3 h-3 btn-arrow-icon" />
                    </MotionButton>
                  </div>
                </SpotlightCard>
              </StaggerItem>
            );
          })
        )}
      </StaggerContainer>
    </PageTransition>
  );
};
