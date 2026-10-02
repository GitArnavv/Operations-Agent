import React, { useState, useRef, useEffect } from 'react';
import {
  Sparkles,
  Send,
  Mic,
  MicOff,
  Volume2,
  VolumeX,
  RotateCcw,
  Bot,
  User as UserIcon,
  Layers,
  ArrowRight,
} from 'lucide-react';
import { useAgent } from '../contexts/AgentContext';
import { useVoice } from '../hooks/useVoice';
import { CitationBadge } from '../components/agent/CitationBadge';
import { ToolExecutionPill } from '../components/agent/ToolExecutionPill';
import { ApprovalCard } from '../components/agent/ApprovalCard';
import { AgentExecutionTimeline } from '../components/agent/AgentExecutionTimeline';
import { MarkdownRenderer } from '../components/ui/MarkdownRenderer';
import { EvidenceDrawer } from '../components/ui/EvidenceDrawer';
import { VoiceWaveform, VoiceState } from '../components/ui/VoiceWaveform';
import { PageTransition } from '../components/ui/PageTransition';
import { AgentChatSkeleton } from '../components/ui/Skeleton';
import type { CitationEvidence } from '../types';

export const AgentPage: React.FC = () => {
  const {
    messages,
    isLoading,
    sendMessage,
    approveAction,
    rejectAction,
    clearConversation,
    language,
    setLanguage,
  } = useAgent();

  const [inputPrompt, setInputPrompt] = useState('');
  const [autoVoiceReply, setAutoVoiceReply] = useState(false);
  const [inspectedEvidence, setInspectedEvidence] = useState<CitationEvidence | null>(null);
  const messagesEndRef = useRef<HTMLDivElement>(null);

  const { isListening, transcript, isSpeaking, startListening, stopListening, speak, stopSpeaking } = useVoice({
    language,
    onTranscript: (txt) => setInputPrompt(txt),
  });

  useEffect(() => {
    if (transcript) setInputPrompt(transcript);
  }, [transcript]);

  useEffect(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [messages, isLoading]);

  const handleSend = async (e?: React.FormEvent) => {
    if (e) e.preventDefault();
    if (!inputPrompt.trim() || isLoading) return;

    const query = inputPrompt;
    setInputPrompt('');

    const res = await sendMessage(query);
    if (autoVoiceReply && res.content) {
      speak(res.content);
    }
  };

  const handleQuickPrompt = (p: string) => {
    setInputPrompt(p);
    sendMessage(p);
  };

  const getVoiceState = (): VoiceState => {
    if (isListening) return 'LISTENING';
    if (isLoading) return 'INVESTIGATING';
    if (isSpeaking) return 'SPEAKING';
    return 'IDLE';
  };

  return (
    <PageTransition className="max-w-5xl mx-auto h-[calc(100vh-6rem)] flex flex-col font-sans">
      {/* Top Card Header */}
      <div className="p-4 rounded-xl border border-border bg-surface shadow-subtle flex flex-col sm:flex-row sm:items-center justify-between gap-3 mb-4">
        <div className="flex items-center gap-3">
          <div className="w-10 h-10 rounded-xl bg-primary flex items-center justify-center text-primary-foreground shadow-subtle">
            <Bot className="w-5 h-5" />
          </div>
          <div>
            <div className="flex items-center gap-2">
              <h1 className="text-base font-bold text-foreground tracking-tight">
                AI Operations Orchestrator
              </h1>
              <span className="px-2 py-0.5 rounded text-[10px] font-bold bg-primary-subtle text-primary border border-primary-subtle">
                PROD v1.0
              </span>
            </div>
            <p className="text-xs text-muted-foreground">
              Autonomous Operations Employee for Sharma Electricals Pvt. Ltd.
            </p>
          </div>
        </div>

        <div className="flex items-center gap-2">
          {/* Language Selector */}
          <select
            value={language}
            onChange={(e) => setLanguage(e.target.value)}
            className="bg-surface-elevated border border-border text-foreground text-xs rounded-xl px-3 py-1.5 focus:outline-none focus:border-primary"
          >
            <option value="en-IN">English (India)</option>
            <option value="hi-IN">Hindi (हिन्दी)</option>
          </select>

          {/* Voice Audio Readout Toggle */}
          <button
            onClick={() => {
              if (isSpeaking) stopSpeaking();
              setAutoVoiceReply(!autoVoiceReply);
            }}
            title={autoVoiceReply ? 'Voice Replies Active' : 'Voice Replies Muted'}
            className={`p-2 rounded-xl border transition-colors ${
              autoVoiceReply
                ? 'border-primary bg-primary-subtle text-primary'
                : 'border-border text-muted-foreground hover:text-foreground'
            }`}
          >
            {autoVoiceReply ? <Volume2 className="w-4 h-4" /> : <VolumeX className="w-4 h-4" />}
          </button>

          {/* Reset Conversation */}
          <button
            onClick={clearConversation}
            title="Reset Conversation"
            className="p-2 rounded-xl border border-border text-muted-foreground hover:text-foreground hover:bg-surface-hover transition-colors"
          >
            <RotateCcw className="w-4 h-4" />
          </button>
        </div>
      </div>

      {/* Quick Scenario Chips */}
      <div className="flex gap-2 overflow-x-auto pb-3 text-xs scrollbar-none">
        <button
          onClick={() => handleQuickPrompt('What needs my attention today?')}
          className="px-3 py-1.5 rounded-full border border-border bg-surface hover:bg-surface-hover text-foreground font-medium shrink-0 flex items-center gap-1.5 shadow-sm transition-colors"
        >
          <Sparkles className="w-3.5 h-3.5 text-primary" />
          Morning Attention Briefing
        </button>
        <button
          onClick={() => handleQuickPrompt("Why is Sharma Electronics' order delayed?")}
          className="px-3 py-1.5 rounded-full border border-border bg-surface hover:bg-surface-hover text-foreground font-medium shrink-0 transition-colors shadow-sm"
        >
          Investigate Order #ORD-1042
        </button>
        <button
          onClick={() => handleQuickPrompt('Which products are likely to run out this week?')}
          className="px-3 py-1.5 rounded-full border border-border bg-surface hover:bg-surface-hover text-foreground font-medium shrink-0 transition-colors shadow-sm"
        >
          Stockout Projections (&lt; 7 Days)
        </button>
        <button
          onClick={() => handleQuickPrompt('Which supplier has the worst on-time delivery rate?')}
          className="px-3 py-1.5 rounded-full border border-border bg-surface hover:bg-surface-hover text-foreground font-medium shrink-0 transition-colors shadow-sm"
        >
          Supplier SLA Benchmarking
        </button>
        <button
          onClick={() => handleQuickPrompt('Show me all overdue invoices above ₹1 lakh')}
          className="px-3 py-1.5 rounded-full border border-border bg-surface hover:bg-surface-hover text-foreground font-medium shrink-0 transition-colors shadow-sm"
        >
          Overdue Receivables (&gt; ₹1 Lakh)
        </button>
      </div>

      {/* Main Conversation Window */}
      <div className="flex-1 overflow-y-auto p-4 rounded-xl border border-border bg-surface-elevated shadow-subtle space-y-4 text-sm scrollbar-thin">
        {messages.map((msg) => (
          <div
            key={msg.id}
            className={`flex flex-col ${msg.role === 'USER' ? 'items-end' : 'items-start'} animate-slide-up`}
          >
            <div className="flex items-center gap-2 mb-1 text-[11px] text-muted-foreground">
              {msg.role === 'USER' ? (
                <>
                  <span>You</span>
                  <UserIcon className="w-3.5 h-3.5" />
                </>
              ) : (
                <>
                  <Bot className="w-3.5 h-3.5 text-primary" />
                  <span className="font-semibold text-primary">Operations Orchestrator</span>
                </>
              )}
            </div>

            <div
              className={`p-4 rounded-xl max-w-[85%] leading-relaxed ${
                msg.role === 'USER'
                  ? 'bg-primary text-primary-foreground rounded-br-none shadow-subtle'
                  : 'bg-surface border border-border text-foreground rounded-bl-none shadow-subtle'
              }`}
            >
              {/* Reasoning Summary */}
              {msg.reasoningSummary && (
                <div className="mb-2.5 px-3 py-1.5 rounded-lg bg-surface-subtle text-[11px] text-muted-foreground flex items-center gap-2 border border-border">
                  <Layers className="w-3.5 h-3.5 text-primary shrink-0" />
                  <span>{msg.reasoningSummary}</span>
                </div>
              )}

              {/* Message Content */}
              {msg.role === 'USER' ? (
                <p className="whitespace-pre-wrap">{msg.content}</p>
              ) : (
                <MarkdownRenderer content={msg.content} />
              )}

              {/* Tool Execution Telemetry */}
              {msg.toolExecutions && msg.toolExecutions.length > 0 && (
                <div className="mt-3 pt-2.5 border-t border-border flex flex-wrap gap-1.5">
                  {msg.toolExecutions.map((t, idx) => (
                    <ToolExecutionPill key={idx} execution={t} />
                  ))}
                </div>
              )}

              {/* Citations & Evidence */}
              {msg.citations && msg.citations.length > 0 && (
                <div className="mt-3 pt-2.5 border-t border-border">
                  <div className="text-[10px] uppercase font-bold tracking-wider text-muted-foreground mb-1.5">
                    Grounding Evidence (Click to inspect)
                  </div>
                  <div className="flex flex-wrap gap-1.5">
                    {msg.citations.map((c, idx) => (
                      <CitationBadge
                        key={idx}
                        citation={c}
                        onInspect={(evidence) => setInspectedEvidence(evidence)}
                      />
                    ))}
                  </div>
                </div>
              )}

              {/* Action Proposal Card */}
              {msg.actionProposal && (
                <div className="mt-3">
                  <ApprovalCard
                    proposal={msg.actionProposal}
                    onApprove={approveAction}
                    onReject={rejectAction}
                  />
                </div>
              )}
            </div>
          </div>
        ))}

        {/* Real-time Multi-Hop Investigation Progress */}
        <AgentExecutionTimeline isActive={isLoading} />

        {/* AI Agent Chat Skeleton during reasoning & synthesis */}
        {isLoading && <AgentChatSkeleton />}

        {/* Voice Waveform Activity */}
        <VoiceWaveform state={getVoiceState()} transcript={transcript} />

        <div ref={messagesEndRef} />
      </div>

      {/* Input Bar */}
      <form onSubmit={handleSend} className="mt-3 flex items-center gap-2">
        <button
          type="button"
          onClick={isListening ? stopListening : startListening}
          className={`p-3 rounded-xl border transition-all ${
            isListening
              ? 'border-border bg-danger-subtle text-danger'
              : 'border-border bg-surface hover:bg-surface-hover text-muted-foreground hover:text-foreground shadow-subtle'
          }`}
          title={isListening ? 'Stop listening' : 'Start voice input'}
        >
          {isListening ? <MicOff className="w-5 h-5" /> : <Mic className="w-5 h-5" />}
        </button>

        <input
          type="text"
          value={inputPrompt}
          onChange={(e) => setInputPrompt(e.target.value)}
          disabled={isLoading}
          placeholder={
            language === 'hi-IN'
              ? 'यहाँ पूछें: जैसे "Sharma Electronics ka order delay kyu hai?"...'
              : 'Ask a natural language operational question or instruct the agent...'
          }
          className="flex-1 bg-surface border border-border rounded-xl px-4 py-3 text-xs sm:text-sm text-foreground placeholder:text-muted-foreground focus:outline-none focus:border-primary focus:ring-1 focus:ring-primary shadow-subtle transition-all disabled:opacity-50"
        />

        <button
          type="submit"
          disabled={!inputPrompt.trim() || isLoading}
          className="p-3 rounded-xl bg-primary hover:bg-primary-hover text-primary-foreground transition-colors disabled:opacity-40 shadow-subtle"
        >
          <Send className="w-5 h-5" />
        </button>
      </form>

      {/* Evidence Detail Drawer */}
      <EvidenceDrawer
        isOpen={inspectedEvidence !== null}
        evidence={inspectedEvidence}
        onClose={() => setInspectedEvidence(null)}
      />
    </PageTransition>
  );
};
