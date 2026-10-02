import React from 'react';

export type VoiceState =
  | 'IDLE'
  | 'LISTENING'
  | 'TRANSCRIBING'
  | 'THINKING'
  | 'INVESTIGATING'
  | 'ANALYZING'
  | 'RESPONDING'
  | 'SPEAKING'
  | 'ERROR';

interface VoiceWaveformProps {
  state: VoiceState;
  transcript?: string;
  className?: string;
}

export const VoiceWaveform: React.FC<VoiceWaveformProps> = ({ state, transcript, className = '' }) => {
  if (state === 'IDLE') return null;

  return (
    <div className={`p-4 card-digest space-y-3 font-sans ${className}`}>
      <div className="flex items-center justify-between">
        <div className="flex items-center gap-2">
          {state !== 'ERROR' && (
            <span className="flex h-2.5 w-2.5 relative">
              <span className="animate-ping absolute inline-flex h-full w-full rounded-full bg-primary opacity-75"></span>
              <span className="relative inline-flex rounded-full h-2.5 w-2.5 bg-primary"></span>
            </span>
          )}
          <span className="text-xs font-bold uppercase tracking-wider text-primary">
            {state === 'LISTENING' && 'Listening...'}
            {state === 'TRANSCRIBING' && 'Transcribing Speech...'}
            {state === 'THINKING' && 'Understanding Operational Intent...'}
            {state === 'INVESTIGATING' && 'Investigating ERP Records & Stock...'}
            {state === 'ANALYZING' && 'Analyzing Cross-System Dependencies...'}
            {state === 'RESPONDING' && 'Formulating Agent Insights...'}
            {state === 'SPEAKING' && 'Speaking Response...'}
            {state === 'ERROR' && 'Voice Input Error'}
          </span>
        </div>

        {/* Animated Waveform Bars */}
        <div className="flex items-center gap-1 h-6">
          {[4, 12, 22, 16, 8, 20, 14, 6].map((h, i) => (
            <span
              key={i}
              className={`w-1 rounded-full transition-all duration-200 ${
                state === 'LISTENING' || state === 'SPEAKING'
                  ? 'bg-primary animate-wave'
                  : state === 'INVESTIGATING'
                  ? 'bg-amber-500 animate-pulse'
                  : 'bg-border'
              }`}
              style={{
                height: state === 'LISTENING' || state === 'SPEAKING' ? `${h}px` : '4px',
                animationDelay: `${i * 0.15}s`,
              }}
            />
          ))}
        </div>
      </div>

      {/* Transcript or Status Display */}
      {transcript ? (
        <div className="p-2.5 rounded-xl bg-surface-subtle border border-border text-xs text-foreground font-medium italic">
          &ldquo;{transcript}&rdquo;
        </div>
      ) : (
        <p className="text-[11px] text-muted-foreground">
          {state === 'LISTENING'
            ? 'Speak your question in English or Hindi (e.g., "Sharma Electronics ka order delay kyu hai?")...'
            : 'Processing cross-system operational evidence...'}
        </p>
      )}
    </div>
  );
};
