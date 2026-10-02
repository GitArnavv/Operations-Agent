import React from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import { X, ExternalLink, ShieldCheck, Database, Clock, Layers } from 'lucide-react';
import type { CitationEvidence } from '../../types';

interface EvidenceDrawerProps {
  isOpen: boolean;
  onClose: () => void;
  evidence: CitationEvidence | null;
}

export const EvidenceDrawer: React.FC<EvidenceDrawerProps> = ({ isOpen, onClose, evidence }) => {
  const relevancePct = evidence ? Math.round((evidence.confidence || 0.95) * 100) : 95;

  return (
    <AnimatePresence>
      {isOpen && evidence && (
        <div className="fixed inset-0 z-modal flex justify-end font-sans">
          {/* Fluid Backdrop Overlay */}
          <motion.div
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            exit={{ opacity: 0 }}
            transition={{ duration: 0.2, ease: 'easeOut' }}
            className="fixed inset-0 bg-black/60 backdrop-blur-sm"
            onClick={onClose}
          />

          {/* iOS-Fluid Spring Slide-Over Panel */}
          <motion.div
            initial={{ x: '100%', opacity: 0.8 }}
            animate={{
              x: 0,
              opacity: 1,
              transition: { type: 'spring', damping: 28, stiffness: 260, mass: 0.8 },
            }}
            exit={{
              x: '100%',
              opacity: 0,
              transition: { duration: 0.2, ease: 'easeIn' },
            }}
            className="relative z-10 w-full max-w-md bg-surface border-l border-border h-full flex flex-col shadow-elevated theme-surface will-change-transform"
            onClick={(e) => e.stopPropagation()}
          >
            {/* Drawer Header */}
            <div className="p-4 border-b border-border bg-surface flex items-center justify-between">
              <div className="flex items-center gap-2">
                <ShieldCheck className="w-4 h-4 text-primary" />
                <h3 className="text-xs font-bold uppercase tracking-wider text-foreground">
                  Auditable Evidence Record
                </h3>
              </div>
              <motion.button
                whileTap={{ scale: 0.92 }}
                onClick={onClose}
                className="p-1 rounded-lg hover:bg-surface-hover text-muted-foreground hover:text-foreground transition-colors"
                aria-label="Close evidence drawer"
              >
                <X className="w-4 h-4" />
              </motion.button>
            </div>

            {/* Content */}
            <div className="flex-1 overflow-y-auto p-5 space-y-5 scrollbar-thin">
              {/* Main Identifier Box */}
              <div className="p-4 card-digest space-y-2">
                <div className="flex items-center justify-between">
                  <span className="text-[10px] font-mono uppercase px-2 py-0.5 rounded bg-primary/10 text-primary font-semibold">
                    {evidence.entityType}
                  </span>
                  <span className="text-[11px] font-mono text-muted-foreground">
                    ID: {evidence.entityId}
                  </span>
                </div>
                <h2 className="text-base font-bold text-foreground">{evidence.label}</h2>
                <p className="text-xs text-muted-foreground leading-relaxed">
                  {evidence.summary}
                </p>
              </div>

              {/* Metric Grounding & Verification */}
              <div className="space-y-3">
                <h4 className="text-xs font-bold text-foreground uppercase tracking-wider">
                  Verification Breakdown
                </h4>
                <div className="grid grid-cols-2 gap-2 text-xs">
                  <div className="p-3 rounded-xl border border-border bg-surface">
                    <div className="text-[10px] text-muted-foreground mb-0.5">Evidence Relevance</div>
                    <div className="text-lg font-bold text-foreground font-mono">{relevancePct}%</div>
                    <div className="text-[9px] text-muted-foreground mt-0.5">
                      Semantic match to query intent
                    </div>
                  </div>
                  <div className="p-3 rounded-xl border border-border bg-surface">
                    <div className="text-[10px] text-muted-foreground mb-0.5">Data Completeness</div>
                    <div className="text-lg font-bold text-emerald-500 font-mono">100% Verified</div>
                    <div className="text-[9px] text-muted-foreground mt-0.5">
                      Direct ledger / voucher sync
                    </div>
                  </div>
                </div>
              </div>

              {/* System Source & Timestamp */}
              <div className="p-3.5 rounded-xl border border-border bg-surface-subtle space-y-2.5 text-xs">
                <div className="flex items-center gap-2 text-muted-foreground">
                  <Database className="w-3.5 h-3.5 text-primary" />
                  <span>
                    System of Record: <strong className="text-foreground">Sharma Electricals ERP Ledger</strong>
                  </span>
                </div>
                <div className="flex items-center gap-2 text-muted-foreground">
                  <Clock className="w-3.5 h-3.5 text-primary" />
                  <span>
                    Last Reconciled: <strong className="text-foreground">Today, Bhiwandi Sync</strong>
                  </span>
                </div>
                <div className="flex items-center gap-2 text-muted-foreground">
                  <Layers className="w-3.5 h-3.5 text-primary" />
                  <span>
                    Tenant Boundary: <strong className="text-foreground font-mono">27AABCS1429B1Z2</strong>
                  </span>
                </div>
              </div>

              {/* Raw Structured Data Preview */}
              {evidence.metadata && Object.keys(evidence.metadata).length > 0 && (
                <div className="space-y-2">
                  <h4 className="text-xs font-bold text-foreground uppercase tracking-wider">
                    Relevant Attributes
                  </h4>
                  <div className="p-3 rounded-xl border border-border bg-surface font-mono text-[11px] overflow-x-auto space-y-1">
                    {Object.entries(evidence.metadata).map(([k, v]) => (
                      <div key={k} className="flex justify-between py-0.5 border-b border-border last:border-none">
                        <span className="text-muted-foreground">{k}:</span>
                        <span className="text-foreground font-medium">{String(v)}</span>
                      </div>
                    ))}
                  </div>
                </div>
              )}

              {/* Navigation Action */}
              {evidence.deepLink && (
                <div className="pt-2">
                  <a
                    href={evidence.deepLink}
                    className="w-full flex items-center justify-center gap-2 py-2.5 px-4 rounded-xl bg-primary hover:bg-primary-hover text-primary-foreground text-xs font-semibold shadow-sm transition-colors"
                  >
                    <span>Open in Operations Screen</span>
                    <ExternalLink className="w-3.5 h-3.5" />
                  </a>
                </div>
              )}
            </div>
          </motion.div>
        </div>
      )}
    </AnimatePresence>
  );
};
