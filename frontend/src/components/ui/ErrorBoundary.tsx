import React, { Component, ErrorInfo, ReactNode } from 'react';
import { AlertOctagon, RotateCcw, Home, ChevronDown, ChevronRight } from 'lucide-react';

interface ErrorBoundaryProps {
  children: ReactNode;
  fallbackTitle?: string;
  fallbackMessage?: string;
  onReset?: () => void;
}

interface ErrorBoundaryState {
  hasError: boolean;
  error: Error | null;
  errorInfo: ErrorInfo | null;
  showDetails: boolean;
}

export class ErrorBoundary extends Component<ErrorBoundaryProps, ErrorBoundaryState> {
  constructor(props: ErrorBoundaryProps) {
    super(props);
    this.state = {
      hasError: false,
      error: null,
      errorInfo: null,
      showDetails: false,
    };
  }

  static getDerivedStateFromError(error: Error): Partial<ErrorBoundaryState> {
    return { hasError: true, error };
  }

  componentDidCatch(error: Error, errorInfo: ErrorInfo): void {
    this.setState({ errorInfo });
    // In production, error telemetry can be sent to audit log / monitoring endpoint
    console.error('Unhandled React Error in Workspace:', error, errorInfo);
  }

  handleReset = () => {
    this.setState({
      hasError: false,
      error: null,
      errorInfo: null,
      showDetails: false,
    });
    if (this.props.onReset) {
      this.props.onReset();
    }
  };

  handleReload = () => {
    window.location.reload();
  };

  handleGoDashboard = () => {
    window.location.href = '/dashboard';
  };

  toggleDetails = () => {
    this.setState((prev) => ({ showDetails: !prev.showDetails }));
  };

  render(): ReactNode {
    if (this.state.hasError) {
      const { fallbackTitle, fallbackMessage } = this.props;
      const { error, errorInfo, showDetails } = this.state;

      return (
        <div className="min-h-[400px] w-full p-6 flex flex-col items-center justify-center font-sans">
          <div className="max-w-xl w-full p-8 rounded-2xl border border-[var(--status-danger-border)] bg-surface shadow-elevated theme-surface space-y-6">
            <div className="flex items-start gap-4">
              <div className="p-3 rounded-xl bg-[var(--status-danger-bg)] border border-[var(--status-danger-border)] text-[var(--status-danger-text)] shrink-0">
                <AlertOctagon className="w-6 h-6" />
              </div>
              <div className="space-y-1.5">
                <h3 className="text-base font-bold text-foreground tracking-tight">
                  {fallbackTitle || 'Operational Workspace Interrupted'}
                </h3>
                <p className="text-xs text-muted-foreground leading-relaxed">
                  {fallbackMessage ||
                    'An unexpected runtime exception occurred while rendering this operational view. Telemetry and state have been protected.'}
                </p>
              </div>
            </div>

            {/* Action buttons */}
            <div className="flex flex-wrap items-center gap-3 pt-2">
              <button
                onClick={this.handleReset}
                className="px-4 py-2 rounded-xl bg-primary hover:bg-primary-hover text-primary-foreground text-xs font-semibold flex items-center gap-2 shadow-subtle transition-colors"
              >
                <RotateCcw className="w-3.5 h-3.5" />
                <span>Try Again</span>
              </button>

              <button
                onClick={this.handleReload}
                className="px-4 py-2 rounded-xl border border-border bg-surface-subtle hover:bg-surface-hover text-foreground text-xs font-semibold transition-colors"
              >
                Reload Workspace
              </button>

              <button
                onClick={this.handleGoDashboard}
                className="px-4 py-2 rounded-xl border border-border bg-surface-subtle hover:bg-surface-hover text-muted-foreground hover:text-foreground text-xs font-semibold flex items-center gap-1.5 transition-colors"
              >
                <Home className="w-3.5 h-3.5" />
                <span>Dashboard</span>
              </button>
            </div>

            {/* Collapsible Technical Diagnostics */}
            {error && (
              <div className="pt-2 border-t border-border">
                <button
                  onClick={this.toggleDetails}
                  className="flex items-center gap-1.5 text-[11px] font-mono text-muted-foreground hover:text-foreground transition-colors"
                >
                  {showDetails ? (
                    <ChevronDown className="w-3.5 h-3.5" />
                  ) : (
                    <ChevronRight className="w-3.5 h-3.5" />
                  )}
                  <span>Technical Diagnostics & Stack Trace</span>
                </button>

                {showDetails && (
                  <div className="mt-3 p-3 rounded-lg bg-surface-subtle border border-border text-[11px] font-mono space-y-2 overflow-x-auto text-muted-foreground">
                    <div className="text-[var(--status-danger-text)] font-semibold">
                      {error.toString()}
                    </div>
                    {errorInfo?.componentStack && (
                      <pre className="whitespace-pre-wrap text-[10px] leading-relaxed max-h-48 overflow-y-auto">
                        {errorInfo.componentStack}
                      </pre>
                    )}
                  </div>
                )}
              </div>
            )}
          </div>
        </div>
      );
    }

    return this.props.children;
  }
}
