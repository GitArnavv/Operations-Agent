import React, { useEffect, useState } from 'react';
import { Network, RefreshCw, Key, Sliders, Check, X } from 'lucide-react';
import { integrationsApi } from '../api/services';
import type { Integration } from '../types';
import { PageTransition } from '../components/ui/PageTransition';
import { PageHeader } from '../components/ui/PageHeader';
import { StatusBadge } from '../components/ui/StatusBadge';

export const IntegrationsPage: React.FC = () => {
  const [integrations, setIntegrations] = useState<Integration[]>([]);
  const [syncingId, setSyncingId] = useState<string | null>(null);
  const [configModalIntegration, setConfigModalIntegration] = useState<Integration | null>(null);
  const [apiKeyInput, setApiKeyInput] = useState('');
  const [syncFrequency, setSyncFrequency] = useState('HOURLY');
  const [savedSuccess, setSavedSuccess] = useState(false);

  const loadData = () => {
    integrationsApi.getIntegrations().then(setIntegrations);
  };

  useEffect(() => {
    loadData();
  }, []);

  const handleSync = async (id: string) => {
    setSyncingId(id);
    try {
      await integrationsApi.sync(id);
      loadData();
    } finally {
      setSyncingId(null);
    }
  };

  const handleToggle = async (id: string, current: boolean) => {
    await integrationsApi.toggle(id, !current);
    loadData();
  };

  const handleOpenConfig = (itg: Integration) => {
    setConfigModalIntegration(itg);
    setApiKeyInput('••••••••••••••••••••••••');
    setSavedSuccess(false);
  };

  const handleSaveConfig = (e: React.FormEvent) => {
    e.preventDefault();
    setSavedSuccess(true);
    setTimeout(() => {
      setSavedSuccess(false);
      setConfigModalIntegration(null);
    }, 1200);
  };

  return (
    <PageTransition>
      <div className="space-y-6 max-w-6xl mx-auto pb-16 font-sans">
        <PageHeader
          eyebrow="ERP & System Connectors"
          title="Enterprise Operations Integrations"
          description="Connect Tally Prime XML Server, Zoho Books OAuth, IndiaMART Buyer Gateway, and WhatsApp Business API. Telemetry syncs bi-directionally every 15 minutes."
          actions={
            <span className="text-[11px] font-mono px-3 py-1.5 rounded-lg bg-surface border border-border text-muted-foreground">
              4 Active Connectors
            </span>
          }
        />

        {/* Integrations Grid */}
        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          {integrations.map((itg) => (
            <div
              key={itg.id}
              className="p-5 rounded-xl bg-surface border border-border shadow-subtle flex flex-col justify-between space-y-4 transition-all"
            >
              <div>
                <div className="flex items-center justify-between mb-3">
                  <div className="flex items-center gap-2">
                    <span className="text-[10px] font-mono font-bold uppercase tracking-wider px-2 py-0.5 rounded bg-surface-subtle text-foreground border border-border">
                      {itg.category}
                    </span>
                    <StatusBadge
                      variant={itg.status === 'CONNECTED' ? 'success' : 'neutral'}
                      label={itg.status === 'CONNECTED' ? 'Connected' : 'Disconnected'}
                    />
                  </div>

                  {/* Switch */}
                  <label className="relative inline-flex items-center cursor-pointer">
                    <input
                      type="checkbox"
                      checked={itg.enabled}
                      onChange={() => handleToggle(itg.id, itg.enabled)}
                      className="sr-only peer"
                    />
                    <div className="w-9 h-5 bg-border peer-focus:outline-none rounded-full peer peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:border-border after:border after:rounded-full after:h-4 after:w-4 after:transition-all peer-checked:bg-primary"></div>
                  </label>
                </div>

                <h2 className="text-base font-bold text-foreground mb-1">{itg.providerName}</h2>
                <p className="text-xs text-muted-foreground font-mono mb-3">{itg.configSummary}</p>

                {itg.syncStatusMessage && (
                  <div className="text-xs text-emerald-600 dark:text-emerald-400 font-medium bg-status-success-bg p-2.5 rounded-xl border border-border flex items-center gap-2">
                    <span className="w-1.5 h-1.5 rounded-full bg-emerald-500 shrink-0" />
                    <span>{itg.syncStatusMessage}</span>
                  </div>
                )}
              </div>

              <div className="pt-3 border-t border-border flex items-center justify-between text-xs">
                <span className="text-muted-foreground font-mono text-[11px]">
                  Last Sync: {itg.lastSyncedAt ? new Date(itg.lastSyncedAt).toLocaleTimeString() : 'Never'}
                </span>

                <div className="flex items-center gap-2">
                  <button
                    onClick={() => handleOpenConfig(itg)}
                    className="flex items-center gap-1 px-2.5 py-1.5 rounded-lg bg-surface hover:bg-surface-hover text-foreground text-xs font-medium border border-border transition-colors"
                  >
                    <Sliders className="w-3.5 h-3.5 text-muted-foreground" />
                    <span>Configure</span>
                  </button>

                  <button
                    onClick={() => handleSync(itg.id)}
                    disabled={syncingId === itg.id || !itg.enabled}
                    className="flex items-center gap-1.5 px-3 py-1.5 rounded-lg bg-primary hover:bg-primary-hover disabled:opacity-40 text-primary-foreground text-xs font-semibold shadow-sm transition-colors"
                  >
                    <RefreshCw className={`w-3.5 h-3.5 ${syncingId === itg.id ? 'animate-spin' : ''}`} />
                    <span>Sync</span>
                  </button>
                </div>
              </div>
            </div>
          ))}
        </div>

        {/* Configuration Modal */}
        {configModalIntegration && (
          <div className="fixed inset-0 z-50 bg-overlay flex items-center justify-center p-4">
            <div className="w-full max-w-md bg-surface border border-border rounded-xl p-6 shadow-elevated space-y-4 animate-scale-in">
              <div className="flex items-center justify-between border-b border-border pb-3">
                <h3 className="text-base font-bold text-foreground flex items-center gap-2">
                  <Sliders className="w-4 h-4 text-primary" />
                  Configure {configModalIntegration.providerName}
                </h3>
                <button
                  onClick={() => setConfigModalIntegration(null)}
                  className="text-muted-foreground hover:text-foreground"
                >
                  <X className="w-4 h-4" />
                </button>
              </div>

              <form onSubmit={handleSaveConfig} className="space-y-4 text-xs">
                <div>
                  <label className="block text-foreground font-medium mb-1">
                    API Key / Secret Token
                  </label>
                  <div className="relative">
                    <input
                      type="password"
                      value={apiKeyInput}
                      onChange={(e) => setApiKeyInput(e.target.value)}
                      required
                      className="w-full bg-surface-subtle border border-border rounded-xl px-3 py-2 text-foreground font-mono focus:outline-none focus:border-primary"
                    />
                    <Key className="w-3.5 h-3.5 text-muted-foreground absolute right-3 top-2.5" />
                  </div>
                </div>

                <div>
                  <label className="block text-foreground font-medium mb-1">
                    Sync Schedule Frequency
                  </label>
                  <select
                    value={syncFrequency}
                    onChange={(e) => setSyncFrequency(e.target.value)}
                    className="w-full bg-surface-subtle border border-border rounded-xl px-3 py-2 text-foreground focus:outline-none focus:border-primary"
                  >
                    <option value="REALTIME">Continuous Webhooks (Realtime)</option>
                    <option value="HOURLY">Every 60 Minutes</option>
                    <option value="DAILY">Daily at 08:00 IST</option>
                  </select>
                </div>

                <div className="p-3 rounded-xl bg-surface-subtle border border-border text-[11px] text-muted-foreground space-y-1">
                  <p className="font-semibold text-foreground">Active Endpoint:</p>
                  <p className="font-mono break-all">{configModalIntegration.configSummary}</p>
                </div>

                {savedSuccess && (
                  <div className="p-2.5 rounded-xl bg-status-success-bg border border-border text-emerald-600 dark:text-emerald-400 text-xs font-semibold flex items-center gap-2">
                    <Check className="w-4 h-4" />
                    <span>Configuration updated successfully</span>
                  </div>
                )}

                <div className="flex items-center justify-end gap-2 pt-2">
                  <button
                    type="button"
                    onClick={() => setConfigModalIntegration(null)}
                    className="px-4 py-2 rounded-xl text-muted-foreground hover:text-foreground text-xs font-medium"
                  >
                    Cancel
                  </button>
                  <button
                    type="submit"
                    className="px-4 py-2 rounded-xl bg-primary hover:bg-primary-hover text-primary-foreground font-semibold text-xs shadow-sm transition-colors"
                  >
                    Save Changes
                  </button>
                </div>
              </form>
            </div>
          </div>
        )}
      </div>
    </PageTransition>
  );
};
