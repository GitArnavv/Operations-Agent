import React, { useState, useRef, useEffect } from 'react';
import { Sun, Moon, Monitor, Check } from 'lucide-react';
import { useTheme, Theme } from '../../contexts/ThemeContext';

export const ThemeToggle: React.FC = () => {
  const { theme, effectiveTheme, setTheme } = useTheme();
  const [isOpen, setIsOpen] = useState(false);
  const dropdownRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    const handleClickOutside = (e: MouseEvent) => {
      if (dropdownRef.current && !dropdownRef.current.contains(e.target as Node)) {
        setIsOpen(false);
      }
    };
    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, []);

  const options: { label: string; value: Theme; icon: React.ElementType }[] = [
    { label: 'Light', value: 'light', icon: Sun },
    { label: 'Dark', value: 'dark', icon: Moon },
    { label: 'System', value: 'system', icon: Monitor },
  ];

  return (
    <div className="relative" ref={dropdownRef}>
      <button
        onClick={() => setIsOpen(!isOpen)}
        aria-label="Toggle theme"
        title={`Theme: ${theme.charAt(0).toUpperCase() + theme.slice(1)}`}
        className="p-2 rounded-lg text-muted-foreground hover:text-foreground hover:bg-white/[0.04] transition-colors focus:outline-none flex items-center justify-center"
      >
        {effectiveTheme === 'dark' ? (
          <Moon className="w-4 h-4 text-foreground transition-transform duration-300 transform rotate-0" />
        ) : (
          <Sun className="w-4 h-4 text-foreground transition-transform duration-300 transform rotate-0" />
        )}
      </button>

      {isOpen && (
        <div className="absolute right-0 top-full mt-2 w-36 py-1.5 rounded-xl border border-border bg-[#141318] shadow-elevated z-popover animate-scale-in text-xs font-sans">
          <div className="px-3 py-1 text-[10px] font-semibold uppercase tracking-wider text-muted-foreground">
            Theme Mode
          </div>
          {options.map((opt) => {
            const Icon = opt.icon;
            const isSelected = theme === opt.value;
            return (
              <button
                key={opt.value}
                onClick={() => {
                  setTheme(opt.value);
                  setIsOpen(false);
                }}
                className={`w-full flex items-center justify-between px-3 py-2 text-left hover:bg-surface-hover transition-colors ${
                  isSelected ? 'text-primary font-medium' : 'text-foreground'
                }`}
              >
                <div className="flex items-center gap-2">
                  <Icon className="w-3.5 h-3.5" />
                  <span>{opt.label}</span>
                </div>
                {isSelected && <Check className="w-3.5 h-3.5 text-primary" />}
              </button>
            );
          })}
        </div>
      )}
    </div>
  );
};
