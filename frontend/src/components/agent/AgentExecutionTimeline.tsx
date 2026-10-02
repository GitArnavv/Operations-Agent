import React, { useState, useEffect } from 'react';
import { Check, Loader2, Sparkles, ChevronDown, ChevronUp } from 'lucide-react';

interface TimelineStep {
  id: string;
  label: string;
  delayMs: number;
}

const STEPS: TimelineStep[] = [
  { id: 'step-1', label: 'Operational intent recognized & parameterized', delayMs: 200 },
  { id: 'step-2', label: 'Querying enterprise sales orders & line items', delayMs: 600 },
  { id: 'step-3', label: 'Auditing warehouse inventory & reserved safety stock', delayMs: 1100 },
  { id: 'step-4', label: 'Cross-referencing supplier SLA & historical lead times', delayMs: 1600 },
  { id: 'step-5', label: 'Computing delivery risk score & financial cashflow impact', delayMs: 2200 },
  { id: 'step-6', label: 'Synthesizing evidence citations & recommendations', delayMs: 2700 },
];

export const AgentExecutionTimeline: React.FC<{ isActive: boolean }> = ({ isActive }) => {
  const [currentStepIndex, setCurrentStepIndex] = useState(0);
  const [isExpanded, setIsExpanded] = useState(true);

  useEffect(() => {
    if (!isActive) {
      setCurrentStepIndex(0);
      return;
    }

    const timers: ReturnType<typeof setTimeout>[] = [];
    STEPS.forEach((step, idx) => {
      const t = setTimeout(() => {
        setCurrentStepIndex(idx);
      }, step.delayMs);
      timers.push(t);
    });

    return () => timers.forEach(clearTimeout);
  }, [isActive]);

  if (!isActive) return null;

  return (
    <div className="my-3 p-4 rounded-xl border border-border bg-surface-subtle font-sans text-xs theme-surface animate-fade-in">
      <div
        className="flex items-center justify-between cursor-pointer select-none"
        onClick={() => setIsExpanded(!isExpanded)}
      >
        <div className="flex items-center gap-2 text-primary font-semibold">
          <Sparkles className="w-3.5 h-3.5 shrink-0" />
          <span>Autonomous Multi-Hop Investigation</span>
        </div>
        <button className="text-muted-foreground hover:text-foreground">
          {isExpanded ? <ChevronUp className="w-4 h-4" /> : <ChevronDown className="w-4 h-4" />}
        </button>
      </div>

      {isExpanded && (
        <div className="mt-3 space-y-2 pl-1 border-l border-border ml-2.5">
          {STEPS.map((step, idx) => {
            const isCompleted = idx < currentStepIndex;
            const isCurrent = idx === currentStepIndex;
            const isPending = idx > currentStepIndex;

            return (
              <div
                key={step.id}
                className={`flex items-center gap-2.5 transition-opacity duration-150 ${
                  isPending ? 'opacity-40' : 'opacity-100'
                }`}
              >
                <div
                  className={`-ml-[9px] w-4 h-4 rounded-full flex items-center justify-center text-[9px] font-bold ${
                    isCompleted
                      ? 'bg-[var(--status-success-bg)] text-[var(--status-success-text)] border border-[var(--status-success-border)]'
                      : isCurrent
                      ? 'bg-primary text-primary-foreground'
                      : 'bg-surface border border-border text-muted-foreground'
                  }`}
                >
                  {isCompleted ? (
                    <Check className="w-2.5 h-2.5 stroke-[3]" />
                  ) : isCurrent ? (
                    <Loader2 className="w-2.5 h-2.5 animate-spin" />
                  ) : (
                    <span>{idx + 1}</span>
                  )}
                </div>
                <span
                  className={`text-xs ${
                    isCurrent ? 'font-semibold text-primary' : 'text-foreground'
                  }`}
                >
                  {step.label}
                  {isCurrent && <span className="text-muted-foreground ml-1 font-mono">...</span>}
                </span>
              </div>
            );
          })}
        </div>
      )}
    </div>
  );
};
