import React, { useState, useRef, useEffect } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import {
  Sparkles,
  X,
  Send,
  Mic,
  MicOff,
  Volume2,
  VolumeX,
  RotateCcw,
  Bot,
  User as UserIcon,
  Layers,
} from 'lucide-react';
import { useAgent } from '../../contexts/AgentContext';
import { useVoice } from '../../hooks/useVoice';
import { CitationBadge } from './CitationBadge';
import { ToolExecutionPill } from './ToolExecutionPill';
import { ApprovalCard } from './ApprovalCard';
import { AgentExecutionTimeline } from './AgentExecutionTimeline';
import { MarkdownRenderer } from '../ui/MarkdownRenderer';
import { EvidenceDrawer } from '../ui/EvidenceDrawer';
import { VoiceWaveform, VoiceState } from '../ui/VoiceWaveform';
import { AgentChatSkeleton } from '../ui/Skeleton';
import type { CitationEvidence } from '../../types';

interface AgentDrawerProps {
  isMobileDrawer?: boolean;
}

export const AgentDrawer: React.FC<AgentDrawerProps> = ({ isMobileDrawer = false }) => {
  const {
    messages,
    isLoading,
    isDrawerOpen,
    setIsDrawerOpen,
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
    onTranscript: (txt) => {
      setInputPrompt(txt);
    },
  });

  useEffect(() => {
    if (transcript) {
      setInputPrompt(transcript);
    }
  }, [transcript]);

  useEffect(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [messages, isLoading]);

  const handleSubmit = async (e?: React.FormEvent) => {
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

  const panelInner = (
    <div className="flex flex-col h-full min-h-0 bg-surface overflow-hidden">
      {/* Header */}
      <div className="p-4 border-b border-border bg-surface flex items-center justify-between shrink-0">
        <div className="flex items-center gap-3">
          <div className="w-8 h-8 rounded-xl bg-primary flex items-center justify-center text-primary-foreground shadow-subtle">
            <Bot className="w-4 h-4" />
          </div>
          <div>
            <div className="flex items-center gap-2">
              <h2 className="text-xs font-bold text-foreground tracking-tight">AI Operations Agent</h2>
              <span className="px-1.5 py-0.5 rounded text-[9px] font-bold bg-primary/10 text-primary">
                SHARMA ERP
              </span>
            </div>
            <p className="text-[11px] text-muted-foreground">Autonomous Supply Chain Intelligence</p>
          </div>
        </div>

        <div className="flex items-center gap-2">
          {/* Language Selector */}
          <select
            value={language}
            onChange={(e) => setLanguage(e.target.value as any)}
            className="bg-surface-subtle border border-border text-foreground text-[11px] rounded-lg px-2 py-1 focus:outline-none focus:border-primary"
          >
            <option value="en-IN">English (India)</option>
            <option value="hi-IN">Hindi (हिन्दी)</option>
          </select>

          {/* Voice Response Toggle */}
          <motion.button
            whileTap={{ scale: 0.92 }}
            onClick={() => {
              if (isSpeaking) stopSpeaking();
              setAutoVoiceReply(!autoVoiceReply);
            }}
            title={autoVoiceReply ? 'Voice Replies Active' : 'Voice Replies Muted'}
            className={`p-1.5 rounded-lg transition-colors ${
              autoVoiceReply ? 'bg-primary/10 text-primary' : 'text-muted-foreground hover:text-foreground hover:bg-white/[0.04]'
            }`}
          >
            {autoVoiceReply ? <Volume2 className="w-4 h-4" /> : <VolumeX className="w-4 h-4" />}
          </motion.button>

          {/* Reset Conversation */}
          <motion.button
            whileTap={{ scale: 0.92 }}
            onClick={clearConversation}
            title="Reset Conversation"
            className="p-1.5 rounded-lg text-muted-foreground hover:text-foreground hover:bg-white/[0.04] transition-colors"
          >
            <RotateCcw className="w-4 h-4" />
          </motion.button>

          {/* Close Panel */}
          <motion.button
            whileTap={{ scale: 0.92 }}
            onClick={() => setIsDrawerOpen(false)}
            className="p-1.5 rounded-lg text-muted-foreground hover:text-foreground hover:bg-white/[0.04] transition-colors"
          >
            <X className="w-4 h-4" />
          </motion.button>
        </div>
      </div>

      {/* Suggested Prompts Pill Strip with Magnetic Pull */}
      <div className="px-4 py-2 bg-surface-subtle border-b border-border overflow-x-auto whitespace-nowrap flex gap-2 scrollbar-none text-xs shrink-0">
        {[
          { label: 'What needs my attention?', prompt: 'What needs my attention today?' },
          { label: 'Why is ORD-1042 delayed?', prompt: "Why is Sharma Electronics' order delayed?" },
          { label: 'Stockout risk this week', prompt: 'Which products are likely to run out this week?' },
          { label: 'Overdue > ₹1 Lakh', prompt: 'Show me overdue invoices above ₹1 lakh' },
        ].map((item) => (
          <motion.button
            key={item.label}
            whileHover={{ y: -1, scale: 1.02 }}
            whileTap={{ scale: 0.96 }}
            onClick={() => handleQuickPrompt(item.prompt)}
            className="px-2.5 py-1 rounded-lg bg-white/[0.03] hover:bg-white/[0.07] text-foreground/90 border border-border/50 hover:border-orange-500/30 flex items-center gap-1.5 transition-colors cursor-pointer"
          >
            <Sparkles className="w-3 h-3 text-primary" />
            <span>{item.label}</span>
          </motion.button>
        ))}
      </div>

      {/* Messages Stream */}
      <div className="flex-1 overflow-y-auto p-4 space-y-4 text-sm scrollbar-thin">
        {messages.map((msg) => (
          <motion.div
            key={msg.id}
            initial={{ opacity: 0, y: 8, filter: 'blur(3px)' }}
            animate={{ opacity: 1, y: 0, filter: 'blur(0px)' }}
            transition={{ type: 'spring', stiffness: 320, damping: 24 }}
            className={`flex flex-col ${msg.role === 'USER' ? 'items-end' : 'items-start'}`}
          >
            {/* Sender Label */}
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

            {/* Bubble */}
            <div
              className={`p-4 rounded-xl max-w-[94%] leading-relaxed ${
                msg.role === 'USER'
                  ? 'bg-primary text-primary-foreground rounded-br-none shadow-subtle'
                  : 'bg-surface border border-border text-foreground rounded-bl-none shadow-subtle'
              }`}
            >
              {/* Reasoning Summary Banner */}
              {msg.reasoningSummary && (
                <div className="mb-2.5 px-2.5 py-1.5 rounded-lg bg-surface-subtle text-[11px] text-muted-foreground flex items-center gap-2 border border-border">
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

              {/* Tool Execution Badges */}
              {msg.toolExecutions && msg.toolExecutions.length > 0 && (
                <div className="mt-3 pt-2.5 border-t border-border flex flex-wrap gap-1.5">
                  {msg.toolExecutions.map((exec, idx) => (
                    <ToolExecutionPill key={idx} execution={exec} />
                  ))}
                </div>
              )}

              {/* Citations & Evidence Records */}
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

              {/* Action Proposals / Human-In-The-Loop */}
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
          </motion.div>
        ))}

        {/* Live Multi-Hop Execution Timeline */}
        <AgentExecutionTimeline isActive={isLoading} />

        {/* Dynamic Skeleton while Agent is Investigating */}
        {isLoading && <AgentChatSkeleton />}

        {/* Real-Time Voice Waveform */}
        <VoiceWaveform state={getVoiceState()} transcript={transcript} />

        <div ref={messagesEndRef} />
      </div>

      {/* Input Dock */}
      <form onSubmit={handleSubmit} className="p-3 border-t border-border bg-surface flex items-center gap-2 shrink-0">
        <motion.button
          type="button"
          whileTap={{ scale: 0.92 }}
          onClick={isListening ? stopListening : startListening}
          className={`p-2.5 rounded-xl border transition-all ${
            isListening
              ? 'border-border bg-danger-subtle text-danger'
              : 'border-border bg-surface-subtle hover:bg-surface-hover text-muted-foreground hover:text-foreground'
          }`}
          title={isListening ? 'Stop listening' : 'Start voice input'}
        >
          {isListening ? <MicOff className="w-4 h-4" /> : <Mic className="w-4 h-4" />}
        </motion.button>

        <input
          type="text"
          value={inputPrompt}
          onChange={(e) => setInputPrompt(e.target.value)}
          disabled={isLoading}
          placeholder={
            language === 'hi-IN'
              ? 'यहाँ पूछें: जैसे "Sharma Electronics ka order delay kyu hai?"...'
              : 'Ask AI operations agent or specify an action to investigate...'
          }
          className="flex-1 bg-surface-elevated border border-border rounded-xl px-4 py-2.5 text-xs text-foreground placeholder:text-muted-foreground focus:outline-none focus:border-primary focus:ring-1 focus:ring-primary transition-all disabled:opacity-50"
        />

        <motion.button
          type="submit"
          whileTap={{ scale: 0.92 }}
          disabled={!inputPrompt.trim() || isLoading}
          className="p-2.5 rounded-xl bg-primary hover:bg-primary-hover text-primary-foreground transition-colors disabled:opacity-40 shadow-subtle cursor-pointer"
        >
          <Send className="w-4 h-4" />
        </motion.button>
      </form>

      {/* Slide-over Evidence Detail Drawer */}
      <EvidenceDrawer
        isOpen={inspectedEvidence !== null}
        evidence={inspectedEvidence}
        onClose={() => setInspectedEvidence(null)}
      />
    </div>
  );

  if (isMobileDrawer) {
    return (
      <AnimatePresence>
        {isDrawerOpen && (
          <div className="fixed inset-0 z-modal flex justify-end font-sans">
            <motion.div
              initial={{ opacity: 0 }}
              animate={{ opacity: 1 }}
              exit={{ opacity: 0 }}
              transition={{ duration: 0.2 }}
              className="fixed inset-0 bg-black/60 backdrop-blur-sm"
              onClick={() => setIsDrawerOpen(false)}
            />
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
              className="relative z-10 w-full max-w-xl bg-surface border-l border-border h-full flex flex-col shadow-elevated theme-surface will-change-transform"
              onClick={(e) => e.stopPropagation()}
            >
              {panelInner}
            </motion.div>
          </div>
        )}
      </AnimatePresence>
    );
  }

  return (
    <AnimatePresence>
      {isDrawerOpen && (
        <motion.aside
          initial={{ width: 0, opacity: 0 }}
          animate={{
            width: 'var(--ai-panel-width, 400px)',
            opacity: 1,
            transition: { type: 'spring', stiffness: 300, damping: 26, mass: 0.8 },
          }}
          exit={{
            width: 0,
            opacity: 0,
            transition: { duration: 0.2, ease: 'easeInOut' },
          }}
          className="ai-agent-panel bg-surface border-l border-border h-full flex flex-col min-w-0 min-h-0 select-none theme-surface overflow-hidden shrink-0 will-change-transform"
        >
          {panelInner}
        </motion.aside>
      )}
    </AnimatePresence>
  );
};
