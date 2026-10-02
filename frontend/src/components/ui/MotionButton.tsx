import React from 'react';
import { motion, HTMLMotionProps } from 'framer-motion';
import clsx from 'clsx';

export interface MotionButtonProps extends HTMLMotionProps<'button'> {
  variant?: 'primary' | 'secondary' | 'ghost' | 'danger';
  size?: 'sm' | 'md' | 'lg';
  shimmer?: boolean;
  children: React.ReactNode;
}

/**
 * Enterprise MotionButton with spring tap physics and diagonal light-sweep shimmer.
 */
export const MotionButton = React.forwardRef<HTMLButtonElement, MotionButtonProps>(
  ({ variant = 'primary', size = 'sm', shimmer = true, className = '', children, ...props }, ref) => {
    const sizeClasses = {
      sm: 'px-3 py-1.5 text-xs rounded-lg gap-1.5',
      md: 'px-4 py-2 text-xs font-semibold rounded-xl gap-2',
      lg: 'px-5 py-2.5 text-sm font-semibold rounded-xl gap-2.5',
    }[size];

    const variantClasses = {
      primary:
        'bg-gradient-to-r from-[#FF6500] to-[#FF4D00] text-white shadow-[0_2px_12px_rgba(255,90,0,0.28)] hover:shadow-[0_4px_20px_rgba(255,90,0,0.42)] font-semibold',
      secondary:
        'bg-surface border border-border text-foreground hover:bg-surface-hover hover:border-orange-500/30',
      ghost:
        'bg-transparent text-muted-foreground hover:text-foreground hover:bg-white/[0.04]',
      danger:
        'bg-[var(--status-danger-bg)] text-[var(--status-danger-text)] border border-[var(--status-danger-border)] hover:bg-danger/20',
    }[variant];

    return (
      <motion.button
        ref={ref}
        whileHover={{
          y: -1,
          scale: 1.01,
          transition: { type: 'spring', stiffness: 350, damping: 22, mass: 0.8 },
        }}
        whileTap={{
          scale: 0.96,
          transition: { type: 'spring', stiffness: 450, damping: 20 },
        }}
        className={clsx(
          'relative inline-flex items-center justify-center select-none overflow-hidden will-change-transform group cursor-pointer disabled:opacity-50 disabled:pointer-events-none',
          sizeClasses,
          variantClasses,
          className
        )}
        {...props}
      >
        {/* Shimmer Diagonal Light-Sweep Effect on Hover */}
        {shimmer && (
          <span
            className="pointer-events-none absolute -inset-full top-0 block -translate-x-full rotate-12 bg-gradient-to-r from-transparent via-white/15 to-transparent transition-transform duration-700 ease-in-out group-hover:translate-x-full"
            aria-hidden="true"
          />
        )}
        <span className="relative z-10 flex items-center gap-1.5">
          {children}
        </span>
      </motion.button>
    );
  }
);

MotionButton.displayName = 'MotionButton';
