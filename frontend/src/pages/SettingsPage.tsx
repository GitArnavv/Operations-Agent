import React, { useState } from 'react';
import { Settings, Building2, Bell, Globe, Shield, Check, Smartphone, Mail } from 'lucide-react';
import { useAuth } from '../contexts/AuthContext';
import { useAgent } from '../contexts/AgentContext';
import { PageTransition } from '../components/ui/PageTransition';
import { PageHeader } from '../components/ui/PageHeader';
import { StatusBadge } from '../components/ui/StatusBadge';

export const SettingsPage: React.FC = () => {
  const { organization } = useAuth();
  const { language, setLanguage } = useAgent();
  const [whatsAppNotif, setWhatsAppNotif] = useState(true);
  const [emailNotif, setEmailNotif] = useState(true);
  const [autoReorderThreshold, setAutoReorderThreshold] = useState(25000);
  const [autoSlaEscalation, setAutoSlaEscalation] = useState(true);
  const [saved, setSaved] = useState(false);

  const handleSave = (e: React.FormEvent) => {
    e.preventDefault();
    setSaved(true);
    setTimeout(() => setSaved(false), 3000);
  };

  return (
    <PageTransition>
      <div className="space-y-6 max-w-4xl mx-auto pb-16 font-sans">
        <PageHeader
          eyebrow="System & Policy Configuration"
          title="Organization & Autonomous Agent Settings"
          description="Manage Indian business entity profile, voice language models, autonomous approval financial caps, and multi-channel alerts."
          actions={
            saved ? (
              <StatusBadge variant="success" label="Settings Saved" />
            ) : undefined
          }
        />

        <form onSubmit={handleSave} className="space-y-6">
          {/* Organization Info */}
          <div className="p-6 rounded-xl bg-surface border border-border shadow-subtle space-y-4">
            <h2 className="text-sm font-bold text-foreground flex items-center gap-2">
              <Building2 className="w-4 h-4 text-primary" />
              Indian Business Identity
            </h2>

            <div className="grid grid-cols-1 sm:grid-cols-2 gap-4 text-xs">
              <div>
                <label className="text-muted-foreground block mb-1">Company Legal Name</label>
                <input
                  type="text"
                  disabled
                  defaultValue={organization?.name || 'Sharma Electricals Pvt. Ltd.'}
                  className="w-full bg-surface-subtle border border-border rounded-xl p-2.5 text-foreground font-medium"
                />
              </div>

              <div>
                <label className="text-muted-foreground block mb-1">GSTIN Number</label>
                <input
                  type="text"
                  disabled
                  defaultValue={organization?.gstin || '27AABCS1429B1Z2'}
                  className="w-full bg-surface-subtle border border-border rounded-xl p-2.5 text-foreground font-mono"
                />
              </div>

              <div>
                <label className="text-muted-foreground block mb-1">Permanent Account Number (PAN)</label>
                <input
                  type="text"
                  disabled
                  defaultValue={organization?.pan || 'AABCS1429B'}
                  className="w-full bg-surface-subtle border border-border rounded-xl p-2.5 text-foreground font-mono"
                />
              </div>

              <div>
                <label className="text-muted-foreground block mb-1">Registered Logistics Hubs</label>
                <input
                  type="text"
                  disabled
                  defaultValue="Bhiwandi Central Hub & Pune West Distribution"
                  className="w-full bg-surface-subtle border border-border rounded-xl p-2.5 text-foreground"
                />
              </div>
            </div>
          </div>

          {/* Autonomous Agent Policy Caps */}
          <div className="p-6 rounded-xl bg-surface border border-border shadow-subtle space-y-4">
            <h2 className="text-sm font-bold text-foreground flex items-center gap-2">
              <Shield className="w-4 h-4 text-primary" />
              Autonomous Decision Boundaries & Financial Guardrails
            </h2>
            <p className="text-xs text-muted-foreground">
              Configure maximum monetary thresholds for autonomous procurement orders before requiring explicit human manager signature.
            </p>

            <div className="space-y-4 pt-2">
              <div>
                <div className="flex items-center justify-between text-xs mb-1.5">
                  <span className="font-semibold text-foreground">
                    Auto-Purchase Order Approval Threshold
                  </span>
                  <span className="font-mono font-bold text-primary">
                    ₹{autoReorderThreshold.toLocaleString('en-IN')}
                  </span>
                </div>
                <input
                  type="range"
                  min="5000"
                  max="100000"
                  step="5000"
                  value={autoReorderThreshold}
                  onChange={(e) => setAutoReorderThreshold(Number(e.target.value))}
                  className="w-full accent-primary h-2 bg-surface-subtle rounded-lg cursor-pointer"
                />
                <span className="text-[11px] text-muted-foreground block mt-1">
                  Actions above this value are routed to the Human-in-the-Loop Approvals inbox.
                </span>
              </div>

              <div className="flex items-center justify-between p-3.5 rounded-xl bg-surface-subtle">
                <div>
                  <span className="text-xs font-semibold text-foreground block">
                    Automatic Supplier Escalation on Delayed Dispatch
                  </span>
                  <span className="text-[11px] text-muted-foreground">
                    Agent automatically triggers WhatsApp ping to supplier representative if PO is overdue by 24h.
                  </span>
                </div>
                <label className="relative inline-flex items-center cursor-pointer">
                  <input
                    type="checkbox"
                    checked={autoSlaEscalation}
                    onChange={(e) => setAutoSlaEscalation(e.target.checked)}
                    className="sr-only peer"
                  />
                  <div className="w-9 h-5 bg-border peer-focus:outline-none rounded-full peer peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:border-border after:border after:rounded-full after:h-4 after:w-4 after:transition-all peer-checked:bg-primary"></div>
                </label>
              </div>
            </div>
          </div>

          {/* Voice & Speech Preferences */}
          <div className="p-6 rounded-xl bg-surface border border-border shadow-subtle space-y-4">
            <h2 className="text-sm font-bold text-foreground flex items-center gap-2">
              <Globe className="w-4 h-4 text-primary" />
              Voice & Multilingual Speech Settings
            </h2>

            <div className="space-y-3 text-xs">
              <div>
                <label className="text-foreground block mb-1.5 font-medium">Default Speech-to-Text Language</label>
                <select
                  value={language}
                  onChange={(e) => setLanguage(e.target.value)}
                  className="w-full sm:w-72 bg-surface-subtle border border-border rounded-xl p-2.5 text-foreground text-xs focus:outline-none focus:border-primary"
                >
                  <option value="en-IN">Indian English (en-IN)</option>
                  <option value="hi-IN">Hindi (हिंदी - hi-IN)</option>
                  <option value="hinglish">Hinglish (Colloquial Indian Business)</option>
                </select>
                <p className="text-muted-foreground mt-1 text-[11px]">
                  Optimized for Indian accents, phonetic brand names, and vernacular operations terminology.
                </p>
              </div>
            </div>
          </div>

          {/* Notification Channels */}
          <div className="p-6 rounded-xl bg-surface border border-border shadow-subtle space-y-4">
            <h2 className="text-sm font-bold text-foreground flex items-center gap-2">
              <Bell className="w-4 h-4 text-primary" />
              Multi-Channel Urgent Alerts
            </h2>

            <div className="space-y-3">
              <div className="flex items-center justify-between p-3.5 rounded-xl bg-surface-subtle">
                <div className="flex items-center gap-3">
                  <Smartphone className="w-4 h-4 text-emerald-500" />
                  <div>
                    <span className="text-xs font-semibold text-foreground block">
                      WhatsApp Business Notifications
                    </span>
                    <span className="text-[11px] text-muted-foreground">
                      Send high-severity stockout and approval alerts to authorized manager numbers.
                    </span>
                  </div>
                </div>
                <label className="relative inline-flex items-center cursor-pointer">
                  <input
                    type="checkbox"
                    checked={whatsAppNotif}
                    onChange={(e) => setWhatsAppNotif(e.target.checked)}
                    className="sr-only peer"
                  />
                  <div className="w-9 h-5 bg-border peer-focus:outline-none rounded-full peer peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:border-border after:border after:rounded-full after:h-4 after:w-4 after:transition-all peer-checked:bg-primary"></div>
                </label>
              </div>

              <div className="flex items-center justify-between p-3.5 rounded-xl bg-surface-subtle">
                <div className="flex items-center gap-3">
                  <Mail className="w-4 h-4 text-primary" />
                  <div>
                    <span className="text-xs font-semibold text-foreground block">
                      Executive Email Digest
                    </span>
                    <span className="text-[11px] text-muted-foreground">
                      Daily 08:30 AM summary of delivery risks, receivable aging, and warehouse capacity.
                    </span>
                  </div>
                </div>
                <label className="relative inline-flex items-center cursor-pointer">
                  <input
                    type="checkbox"
                    checked={emailNotif}
                    onChange={(e) => setEmailNotif(e.target.checked)}
                    className="sr-only peer"
                  />
                  <div className="w-9 h-5 bg-border peer-focus:outline-none rounded-full peer peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:border-border after:border after:rounded-full after:h-4 after:w-4 after:transition-all peer-checked:bg-primary"></div>
                </label>
              </div>
            </div>
          </div>

          <div className="flex justify-end pt-2">
            <button
              type="submit"
              className="px-6 py-2.5 rounded-xl bg-primary hover:bg-primary-hover text-primary-foreground font-semibold text-xs shadow-sm transition-all"
            >
              Save Organization Settings
            </button>
          </div>
        </form>
      </div>
    </PageTransition>
  );
};
