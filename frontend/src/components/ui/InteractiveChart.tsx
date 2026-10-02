import React, { useState } from 'react';
import { Bot, TrendingUp, Sparkles } from 'lucide-react';
import { useAgent } from '../../contexts/AgentContext';

export interface ChartDataPoint {
  label: string;
  value: number;
  secondaryValue?: number;
}

interface InteractiveChartProps {
  title: string;
  subtitle?: string;
  data: ChartDataPoint[];
  type?: 'line' | 'bar' | 'area';
  valuePrefix?: string;
  valueSuffix?: string;
  colorVar?: string;
  secondaryColorVar?: string;
  secondaryLabel?: string;
  height?: number;
  onAskAI?: (prompt: string) => void;
}

export const InteractiveChart: React.FC<InteractiveChartProps> = ({
  title,
  subtitle,
  data,
  type = 'line',
  valuePrefix = '',
  valueSuffix = '',
  colorVar = 'var(--chart-1)',
  secondaryColorVar = 'var(--chart-2)',
  secondaryLabel,
  height = 200,
  onAskAI,
}) => {
  const [hoveredIndex, setHoveredIndex] = useState<number | null>(null);
  const [timeRange, setTimeRange] = useState<'7D' | '30D' | '90D' | '12M'>('30D');
  const { sendMessage, setIsDrawerOpen } = useAgent();

  if (!data || data.length === 0) {
    return (
      <div className="p-6 rounded-xl border border-border bg-surface text-center text-muted-foreground text-xs">
        No chart data available
      </div>
    );
  }

  const maxValue = Math.max(...data.map((d) => Math.max(d.value, d.secondaryValue || 0))) * 1.15 || 100;
  const paddingX = 40;
  const paddingY = 25;
  const width = 600;
  const graphWidth = width - paddingX * 2;
  const graphHeight = height - paddingY * 2;

  // Calculate coordinates
  const points = data.map((d, i) => {
    const x = paddingX + (i / (data.length - 1 || 1)) * graphWidth;
    const y = height - paddingY - (d.value / maxValue) * graphHeight;
    return { x, y, ...d };
  });

  const secondaryPoints = data
    .filter((d) => d.secondaryValue !== undefined)
    .map((d, i) => {
      const x = paddingX + (i / (data.length - 1 || 1)) * graphWidth;
      const y = height - paddingY - ((d.secondaryValue || 0) / maxValue) * graphHeight;
      return { x, y, ...d };
    });

  // SVG path generation
  const linePath = points.reduce((acc, p, i) => `${acc} ${i === 0 ? 'M' : 'L'} ${p.x} ${p.y}`, '');
  const areaPath = `${linePath} L ${points[points.length - 1].x} ${height - paddingY} L ${points[0].x} ${height - paddingY} Z`;

  const secondaryLinePath = secondaryPoints.reduce(
    (acc, p, i) => `${acc} ${i === 0 ? 'M' : 'L'} ${p.x} ${p.y}`,
    ''
  );

  const handleAskAIAboutChart = () => {
    const prompt = `Analyze this trend: "${title}" across ${timeRange}. Highlight key operational anomalies, seasonal shifts, and recommend proactive actions.`;
    if (onAskAI) {
      onAskAI(prompt);
    } else {
      sendMessage(prompt);
      setIsDrawerOpen(true);
    }
  };

  return (
    <div className="p-5 card-digest theme-surface font-sans space-y-4">
      {/* Header with Title and Range Picker */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3">
        <div>
          <div className="flex items-center gap-2">
            <h3 className="text-sm font-semibold text-foreground tracking-tight">{title}</h3>
            {subtitle && (
              <span className="text-[11px] text-muted-foreground hidden md:inline">
                &bull; {subtitle}
              </span>
            )}
          </div>
          {secondaryLabel && (
            <div className="flex items-center gap-4 mt-1 text-[11px] text-muted-foreground">
              <span className="flex items-center gap-1.5">
                <span className="w-2.5 h-0.5 rounded-full" style={{ backgroundColor: colorVar }}></span>
                Primary Trend
              </span>
              <span className="flex items-center gap-1.5">
                <span className="w-2.5 h-0.5 rounded-full" style={{ backgroundColor: secondaryColorVar }}></span>
                {secondaryLabel}
              </span>
            </div>
          )}
        </div>

        <div className="flex items-center gap-2">
          {/* Time range selector */}
          <div className="flex rounded-lg bg-surface-subtle p-0.5 text-[11px]">
            {(['7D', '30D', '90D', '12M'] as const).map((range) => (
              <button
                key={range}
                onClick={() => setTimeRange(range)}
                className={`px-2 py-0.5 rounded font-mono font-medium transition-colors ${
                  timeRange === range
                    ? 'bg-surface text-foreground font-semibold shadow-subtle'
                    : 'text-muted-foreground hover:text-foreground'
                }`}
              >
                {range}
              </button>
            ))}
          </div>

          {/* Ask AI button */}
          <button
            onClick={handleAskAIAboutChart}
            title="Ask AI to interpret this chart"
            className="flex items-center gap-1.5 px-2.5 py-1 rounded-lg text-muted-foreground hover:text-foreground hover:bg-white/[0.04] text-[11px] font-medium transition-colors"
          >
            <Bot className="w-3.5 h-3.5 text-primary" />
            <span className="hidden sm:inline">Ask AI</span>
          </button>
        </div>
      </div>

      {/* SVG Canvas - min-w-0 for flex/grid safety */}
      <div className="relative w-full min-w-0 overflow-visible">
        <svg
          viewBox={`0 0 ${width} ${height}`}
          className="w-full h-auto overflow-visible select-none"
          preserveAspectRatio="none"
        >
          <defs>
            <linearGradient id={`gradient-${title.replace(/\s+/g, '')}`} x1="0" y1="0" x2="0" y2="1">
              <stop offset="0%" stopColor={colorVar} stopOpacity="0.25" />
              <stop offset="100%" stopColor={colorVar} stopOpacity="0.0" />
            </linearGradient>
          </defs>

          {/* Horizontal Grid lines */}
          {[0, 0.25, 0.5, 0.75, 1].map((ratio, idx) => {
            const y = height - paddingY - ratio * graphHeight;
            return (
              <g key={idx}>
                <line
                  x1={paddingX}
                  y1={y}
                  x2={width - paddingX}
                  y2={y}
                  stroke="var(--border)"
                  strokeDasharray="3 3"
                  strokeWidth="1"
                />
                <text
                  x={paddingX - 8}
                  y={y + 3}
                  textAnchor="end"
                  fontSize="9"
                  fill="var(--muted-foreground)"
                  fontFamily="JetBrains Mono"
                >
                  {valuePrefix}
                  {Math.round(ratio * maxValue)}
                </text>
              </g>
            );
          })}

          {/* Area Fill */}
          {type === 'area' && <path d={areaPath} fill={`url(#gradient-${title.replace(/\s+/g, '')})`} />}

          {/* Line Chart */}
          {(type === 'line' || type === 'area') && (
            <>
              {secondaryPoints.length > 0 && (
                <path
                  d={secondaryLinePath}
                  fill="none"
                  stroke={secondaryColorVar}
                  strokeWidth="2"
                  strokeDasharray="4 4"
                />
              )}
              <path
                d={linePath}
                fill="none"
                stroke={colorVar}
                strokeWidth="2.5"
                strokeLinecap="round"
                strokeLinejoin="round"
              />
            </>
          )}

          {/* Bar Chart */}
          {type === 'bar' &&
            points.map((p, idx) => {
              const barWidth = (graphWidth / points.length) * 0.6;
              const barX = p.x - barWidth / 2;
              const barHeight = (p.value / maxValue) * graphHeight;
              const barY = height - paddingY - barHeight;
              const isHovered = hoveredIndex === idx;

              return (
                <rect
                  key={idx}
                  x={barX}
                  y={barY}
                  width={barWidth}
                  height={barHeight}
                  rx="3"
                  fill={isHovered ? 'var(--primary-hover)' : colorVar}
                  className="transition-all duration-200 cursor-pointer"
                  onMouseEnter={() => setHoveredIndex(idx)}
                  onMouseLeave={() => setHoveredIndex(null)}
                />
              );
            })}

          {/* Data Points and Interaction Circles */}
          {type !== 'bar' &&
            points.map((p, idx) => {
              const isHovered = hoveredIndex === idx;
              return (
                <g key={idx}>
                  {isHovered && (
                    <line
                      x1={p.x}
                      y1={paddingY}
                      x2={p.x}
                      y2={height - paddingY}
                      stroke="var(--foreground)"
                      strokeOpacity="0.2"
                      strokeDasharray="2 2"
                    />
                  )}
                  <circle
                    cx={p.x}
                    cy={p.y}
                    r={isHovered ? 5 : 3}
                    fill="var(--surface-elevated)"
                    stroke={colorVar}
                    strokeWidth="2"
                    className="cursor-pointer transition-all duration-200"
                    onMouseEnter={() => setHoveredIndex(idx)}
                    onMouseLeave={() => setHoveredIndex(null)}
                  />
                </g>
              );
            })}

          {/* X Axis Labels */}
          {points.map((p, idx) => {
            if (points.length > 8 && idx % Math.ceil(points.length / 6) !== 0 && idx !== points.length - 1) {
              return null;
            }
            return (
              <text
                key={idx}
                x={p.x}
                y={height - 8}
                textAnchor="middle"
                fontSize="9"
                fill="var(--muted-foreground)"
                fontFamily="Plus Jakarta Sans"
              >
                {p.label}
              </text>
            );
          })}
        </svg>

        {/* Hover Tooltip Overlay */}
        {hoveredIndex !== null && points[hoveredIndex] && (
          <div
            className="absolute z-10 pointer-events-none p-2 rounded-xl border border-border bg-surface-elevated shadow-elevated text-xs space-y-0.5 animate-scale-in"
            style={{
              left: `${(points[hoveredIndex].x / width) * 100}%`,
              top: '10px',
              transform: 'translateX(-50%)',
            }}
          >
            <div className="text-[10px] text-muted-foreground font-medium">
              {points[hoveredIndex].label}
            </div>
            <div className="font-bold font-mono text-foreground text-sm">
              {valuePrefix}
              {points[hoveredIndex].value.toLocaleString('en-IN')}
              {valueSuffix}
            </div>
            {points[hoveredIndex].secondaryValue !== undefined && (
              <div className="text-[10px] font-mono text-muted-foreground">
                Secondary: {valuePrefix}
                {points[hoveredIndex].secondaryValue?.toLocaleString('en-IN')}
              </div>
            )}
          </div>
        )}
      </div>
    </div>
  );
};
