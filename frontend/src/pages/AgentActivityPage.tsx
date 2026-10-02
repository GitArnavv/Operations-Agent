import React, { useEffect, useState } from 'react';
import {
  History,
  Bot,
  Terminal,
  Clock,
  Layers,
  Check,
} from 'lucide-react';
import { agentApi } from '../api/services';
import { PageTransition } from '../components/ui/PageTransition';
import { PageHeader } from '../components/ui/PageHeader';
import { StatusBadge } from '../components/ui/StatusBadge';
import type { AgentRun } from '../types';

export const AgentActivityPage: React.FC = () => {
  const [runs, setRuns] = useState<AgentRun[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    agentApi.getRuns().then((data) => {
      setRuns(data);
      setLoading(false);
    });
  }, []);

  return (
    <PageTransition className="space-y-6 max-w-6xl mx-auto pb-16 font-sans">
      <PageHeader
        eyebrow="Agent Observability & Traces"
        title="Execution Telemetry & State Audit"
        description="Auditable state-machine execution traces across Orchestrator, Specialist Agents, and Controlled Tools."
        actions={
          <StatusBadge
            variant="neutral"
            label="Chain-of-Thought Encrypted & Summarized"
          />
        }
      />

      {/* Runs Feed */}
      <div className="space-y-4">
        {runs.map((run) => (
          <div
            key={run.id}
            className="p-6 rounded-xl border border-border bg-surface shadow-subtle space-y-4"
          >
            {/* Run Header */}
            <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-2 pb-3 border-b border-border">
              <div className="flex items-center gap-3">
                <div className="w-8 h-8 rounded-xl bg-surface-subtle text-primary border border-border flex items-center justify-center font-bold text-xs">
                  <Bot className="w-4 h-4" />
                </div>
                <div>
                  <div className="flex items-center gap-2">
                    <span className="text-sm font-bold font-mono text-foreground">{run.id}</span>
                    <StatusBadge variant="success" label={run.status} />
                  </div>
                  <div className="text-xs font-medium text-foreground mt-0.5">
                    User Query: &ldquo;{run.userPrompt}&rdquo;
                  </div>
                </div>
              </div>

              <div className="flex items-center gap-2 text-xs text-muted-foreground font-mono">
                <Clock className="w-3.5 h-3.5 text-primary" />
                <span>{(run.durationMs / 1000).toFixed(2)}s execution</span>
              </div>
            </div>

            {/* Execution Plan Checklist */}
            <div className="p-4 rounded-xl border border-border bg-surface-subtle space-y-2">
              <div className="text-[10px] uppercase font-bold text-muted-foreground tracking-wider">
                Autonomous Execution Plan & Milestones
              </div>
              <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-2 text-xs">
                {[
                  '1. Intent Classification',
                  '2. Multi-hop Entity Resolution',
                  '3. Controlled Tool Invocation',
                  '4. Evidence & Citation Verification',
                  '5. Risk Gate Assessment',
                  '6. Final Structured Synthesis',
                ].map((step, idx) => (
                  <div key={idx} className="flex items-center gap-2 text-foreground">
                    <div className="w-4 h-4 rounded-full bg-status-success-bg text-emerald-600 dark:text-emerald-400 flex items-center justify-center shrink-0 border border-border">
                      <Check className="w-2.5 h-2.5 stroke-[3]" />
                    </div>
                    <span className="font-mono text-[11px]">{step}</span>
                  </div>
                ))}
              </div>
            </div>

            {/* Specialist Agents & Tools Grid */}
            <div className="grid grid-cols-1 sm:grid-cols-2 gap-3 text-xs">
              <div className="p-3.5 rounded-xl bg-surface-subtle border border-border space-y-1">
                <div className="text-[10px] uppercase font-bold text-muted-foreground tracking-wider">
                  Specialist Agents Invoked
                </div>
                <div className="text-foreground font-semibold flex items-center gap-1.5">
                  <Layers className="w-3.5 h-3.5 text-primary" />
                  <span>{run.agentsUsed || 'ORCHESTRATOR & ORDER_AGENT'}</span>
                </div>
              </div>

              <div className="p-3.5 rounded-xl bg-surface-subtle border border-border space-y-1">
                <div className="text-[10px] uppercase font-bold text-muted-foreground tracking-wider">
                  Audited Tool Execution Chain
                </div>
                <div className="text-primary font-mono text-[11px] truncate">
                  <Terminal className="w-3.5 h-3.5 inline mr-1" />
                  {run.toolsUsed || 'order.investigateDelay, inventory.checkStock'}
                </div>
              </div>
            </div>

            {/* Safe Summary */}
            {run.finalResponse && (
              <div className="p-3.5 rounded-xl bg-surface-subtle border border-border text-xs space-y-1">
                <div className="text-[10px] uppercase font-bold text-muted-foreground tracking-wider">
                  Agent Operational Summary (Result)
                </div>
                <p className="text-foreground leading-relaxed font-sans line-clamp-3">
                  {run.finalResponse}
                </p>
              </div>
            )}
          </div>
        ))}
      </div>
    </PageTransition>
  );
};
