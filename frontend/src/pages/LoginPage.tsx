import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { Sparkles, Bot, ArrowRight, Lock, Mail } from 'lucide-react';
import { useAuth } from '../contexts/AuthContext';
import { ThemeToggle } from '../components/ui/ThemeToggle';
import { PageTransition } from '../components/ui/PageTransition';

export const LoginPage: React.FC = () => {
  const [email, setEmail] = useState('amit.patel@sharmaelectricals.in');
  const [password, setPassword] = useState('demo123');
  const [error, setError] = useState('');
  const [isSubmitting, setIsSubmitting] = useState(false);
  const { login, loginAsDemo } = useAuth();
  const navigate = useNavigate();

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setIsSubmitting(true);
    setError('');
    try {
      await login(email, password);
      navigate('/dashboard');
    } catch (err: any) {
      setError(err.message || 'Login failed. Please check credentials.');
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleDemoLogin = async () => {
    setIsSubmitting(true);
    try {
      await loginAsDemo();
      navigate('/dashboard');
    } catch (err: any) {
      setError(err.message || 'Demo login failed.');
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <PageTransition>
      <div className="min-h-screen w-full bg-background flex flex-col justify-between p-4 sm:p-6 font-sans relative">
        {/* Top Navbar with Logo and Theme Toggle */}
        <div className="w-full max-w-5xl mx-auto flex items-center justify-between py-2">
          <div className="flex items-center gap-2.5">
            <div className="w-8 h-8 rounded-xl bg-primary flex items-center justify-center text-primary-foreground font-black text-sm shadow-sm">
              AI
            </div>
            <div>
              <span className="font-bold text-foreground text-sm tracking-tight block">
                OpsAgent India
              </span>
              <span className="text-[10px] text-muted-foreground font-mono">
                B2B Autonomous Operations
              </span>
            </div>
          </div>
          <ThemeToggle />
        </div>

        {/* Center Card */}
        <div className="w-full max-w-md mx-auto my-auto p-7 sm:p-8 rounded-xl bg-surface border border-border shadow-elevated relative z-10 space-y-6">
          <div className="text-center space-y-2">
            <div className="w-12 h-12 rounded-xl bg-surface-subtle flex items-center justify-center mx-auto text-primary">
              <Sparkles className="w-6 h-6" />
            </div>
            <h1 className="text-2xl font-extrabold text-foreground tracking-tight">
              Enterprise Operations Agent
            </h1>
            <p className="text-xs text-muted-foreground max-w-xs mx-auto leading-relaxed">
              Autonomous supply chain, inventory, and order operations intelligence for Indian businesses.
            </p>
          </div>

          {/* Quick Demo Access Button */}
          <button
            onClick={handleDemoLogin}
            disabled={isSubmitting}
            className="w-full p-3 rounded-xl bg-primary hover:bg-primary-hover text-primary-foreground font-bold text-xs shadow-sm flex items-center justify-center gap-2 transition-all disabled:opacity-50"
          >
            <Bot className="w-4 h-4" />
            <span>Launch Preloaded Demo (Sharma Electricals)</span>
            <ArrowRight className="w-4 h-4" />
          </button>

          <div className="flex items-center gap-3">
            <div className="h-px flex-1 bg-border"></div>
            <span className="text-[10px] font-mono font-semibold text-muted-foreground uppercase tracking-wider">
              Or sign in with credentials
            </span>
            <div className="h-px flex-1 bg-border"></div>
          </div>

          {error && (
            <div className="p-3 rounded-xl bg-status-danger-bg text-rose-600 dark:text-rose-400 text-xs font-medium">
              {error}
            </div>
          )}

          <form onSubmit={handleSubmit} className="space-y-4 text-xs">
            <div>
              <label className="text-foreground font-medium block mb-1">Business Email</label>
              <div className="relative">
                <input
                  type="email"
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                  required
                  placeholder="amit.patel@sharmaelectricals.in"
                  className="w-full bg-surface-subtle border border-border rounded-xl pl-9 pr-3.5 py-2.5 text-foreground placeholder:text-muted-foreground focus:outline-none focus:border-primary"
                />
                <Mail className="w-4 h-4 text-muted-foreground absolute left-3 top-3" />
              </div>
            </div>

            <div>
              <label className="text-foreground font-medium block mb-1">Password</label>
              <div className="relative">
                <input
                  type="password"
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  required
                  placeholder="••••••••"
                  className="w-full bg-surface-subtle border border-border rounded-xl pl-9 pr-3.5 py-2.5 text-foreground placeholder:text-muted-foreground focus:outline-none focus:border-primary"
                />
                <Lock className="w-4 h-4 text-muted-foreground absolute left-3 top-3" />
              </div>
            </div>

            <button
              type="submit"
              disabled={isSubmitting}
              className="w-full py-2.5 rounded-xl bg-surface hover:bg-surface-hover text-foreground font-semibold text-xs border border-border transition-colors shadow-sm disabled:opacity-50"
            >
              {isSubmitting ? 'Authenticating...' : 'Sign In with Password'}
            </button>
          </form>

          <div className="pt-2 text-center text-xs text-muted-foreground">
            <span>New Indian business? </span>
            <Link to="/signup" className="text-primary hover:underline font-bold">
              Register Organization →
            </Link>
          </div>
        </div>

        {/* Footer */}
        <div className="w-full max-w-5xl mx-auto text-center py-2 text-[11px] text-muted-foreground font-mono">
          ISO 27001 & SOC-2 Compliant • Tenancy Isolated by Organization ID • GSTIN Enforced
        </div>
      </div>
    </PageTransition>
  );
};
