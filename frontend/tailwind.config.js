/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",
  ],
  darkMode: ['class', '[data-theme="dark"]'],
  theme: {
    extend: {
      colors: {
        background: 'var(--background)',
        foreground: 'var(--foreground)',
        overlay: 'var(--overlay)',
        surface: {
          DEFAULT: 'var(--surface)',
          raised: 'var(--surface-raised)',
          subtle: 'var(--surface-subtle)',
          elevated: 'var(--surface-raised)',
          hover: 'var(--surface-hover)',
          active: 'var(--surface-active)',
        },
        border: {
          subtle: 'var(--border-subtle)',
          DEFAULT: 'var(--border)',
          strong: 'var(--border-strong)',
          focus: 'var(--border-focus)',
        },
        muted: {
          DEFAULT: 'var(--muted)',
          foreground: 'var(--muted-foreground)',
        },
        primary: {
          DEFAULT: 'var(--primary)',
          foreground: 'var(--primary-foreground)',
          hover: 'var(--primary-hover)',
          subtle: 'var(--primary-subtle)',
        },
        secondary: {
          DEFAULT: 'var(--secondary)',
          foreground: 'var(--secondary-foreground)',
        },
        accent: {
          DEFAULT: 'var(--accent)',
          foreground: 'var(--accent-foreground)',
        },
        success: 'var(--success)',
        warning: 'var(--warning)',
        danger: 'var(--danger)',
        info: 'var(--info)',
        focus: 'var(--focus)',
        status: {
          success: {
            bg: 'var(--status-success-bg)',
            text: 'var(--status-success-text)',
            border: 'var(--status-success-border)',
          },
          warning: {
            bg: 'var(--status-warning-bg)',
            text: 'var(--status-warning-text)',
            border: 'var(--status-warning-border)',
          },
          danger: {
            bg: 'var(--status-danger-bg)',
            text: 'var(--status-danger-text)',
            border: 'var(--status-danger-border)',
          },
          info: {
            bg: 'var(--status-info-bg)',
            text: 'var(--status-info-text)',
            border: 'var(--status-info-border)',
          },
        },
        chart: {
          1: 'var(--chart-1)',
          2: 'var(--chart-2)',
          3: 'var(--chart-3)',
          4: 'var(--chart-4)',
          5: 'var(--chart-5)',
        },
        brand: {
          50: '#fff7ed',
          100: '#ffedd5',
          200: '#fed7aa',
          300: '#fdba74',
          400: '#fb923c',
          500: '#ff6500',
          600: '#ff5a00',
          700: '#ea580c',
          800: '#c2410c',
          900: '#7c2d12',
          950: '#431407',
        }
      },
      fontFamily: {
        sans: ['Inter', '-apple-system', 'BlinkMacSystemFont', 'Segoe UI', 'Roboto', 'sans-serif'],
        mono: ['JetBrains Mono', 'Fira Code', 'monospace']
      },
      zIndex: {
        'base': 'var(--z-base)',
        'sticky': 'var(--z-sticky)',
        'header': 'var(--z-header)',
        'sidebar': 'var(--z-sidebar)',
        'dropdown': 'var(--z-dropdown)',
        'popover': 'var(--z-popover)',
        'command': 'var(--z-command)',
        'overlay': 'var(--z-overlay)',
        'modal': 'var(--z-modal)',
        'toast': 'var(--z-toast)',
        'tooltip': 'var(--z-tooltip)',
      },
      boxShadow: {
        'subtle': 'var(--shadow)',
        'elevated': 'var(--shadow-elevated)',
        'card': '0 2px 8px 0 rgba(0, 0, 0, 0.35)',
        'card-hover': '0 10px 40px rgba(0, 0, 0, 0.35), 0 0 24px rgba(255, 90, 0, 0.08)',
        'glow-orange': '0 0 24px rgba(255, 90, 0, 0.14)',
        'glow-orange-focus': '0 0 0 1px rgba(255, 90, 0, 0.35), 0 0 30px rgba(255, 90, 0, 0.10)',
      },
      borderRadius: {
        'sm': '6px',
        DEFAULT: '8px',
        'md': '10px',
        'lg': '12px',
        'xl': '14px',
        '2xl': '16px',
        '3xl': '20px',
        'card': '16px',
      },
      animation: {
        'fade-in': 'fadeIn 0.25s cubic-bezier(0.16, 1, 0.3, 1)',
        'slide-up': 'slideUp 0.35s cubic-bezier(0.16, 1, 0.3, 1)',
        'scale-in': 'scaleIn 0.2s cubic-bezier(0.16, 1, 0.3, 1)',
        'pulse-subtle': 'pulseSubtle 2.5s cubic-bezier(0.4, 0, 0.6, 1) infinite',
        'ambient-glow': 'ambientGlow 6s ease-in-out infinite alternate',
      },
      keyframes: {
        fadeIn: {
          '0%': { opacity: '0' },
          '100%': { opacity: '1' },
        },
        slideUp: {
          '0%': { opacity: '0', transform: 'translateY(12px)' },
          '100%': { opacity: '1', transform: 'translateY(0)' },
        },
        scaleIn: {
          '0%': { opacity: '0', transform: 'scale(0.98)' },
          '100%': { opacity: '1', transform: 'scale(1)' },
        },
        pulseSubtle: {
          '0%, 100%': { opacity: '1' },
          '50%': { opacity: '0.6' },
        },
        ambientGlow: {
          '0%': { opacity: '0.8', transform: 'scale(1)' },
          '100%': { opacity: '1', transform: 'scale(1.05)' },
        },
      }
    },
  },
  plugins: [],
}
