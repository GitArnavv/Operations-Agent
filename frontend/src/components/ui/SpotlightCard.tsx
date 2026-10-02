import React, { useRef, useState, useCallback } from 'react';
import { motion, HTMLMotionProps } from 'framer-motion';
import clsx from 'clsx';

export interface SpotlightCardProps extends HTMLMotionProps<'div'> {
  spotlightColor?: string;
  glowSize?: number;
  className?: string;
  children: React.ReactNode;
}

/**
 * Linear / Stripe-grade Spotlight Card with hardware-accelerated mouse-following glow
 * and spring-driven elevation hover physics.
 */
export const SpotlightCard: React.FC<SpotlightCardProps> = ({
  spotlightColor = 'rgba(249, 115, 22, 0.12)',
  glowSize = 400,
  className = '',
  children,
  ...props
}) => {
  const cardRef = useRef<HTMLDivElement>(null);
  const [position, setPosition] = useState({ x: 0, y: 0 });
  const [isHovered, setIsHovered] = useState(false);

  const handleMouseMove = useCallback((e: React.MouseEvent<HTMLDivElement>) => {
    if (!cardRef.current) return;
    const rect = cardRef.current.getBoundingClientRect();
    setPosition({
      x: e.clientX - rect.left,
      y: e.clientY - rect.top,
    });
  }, []);

  const handleMouseEnter = useCallback(() => {
    setIsHovered(true);
  }, []);

  const handleMouseLeave = useCallback(() => {
    setIsHovered(false);
  }, []);

  return (
    <motion.div
      ref={cardRef}
      onMouseMove={handleMouseMove}
      onMouseEnter={handleMouseEnter}
      onMouseLeave={handleMouseLeave}
      whileHover={{
        y: -2,
        scale: 1.008,
        transition: { type: 'spring', stiffness: 300, damping: 24, mass: 0.8 },
      }}
      whileTap={{
        scale: 0.99,
        transition: { type: 'spring', stiffness: 400, damping: 25 },
      }}
      className={clsx(
        'group relative overflow-hidden rounded-xl border border-border bg-surface will-change-transform theme-surface',
        'transition-colors duration-200 hover:border-orange-500/30 hover:shadow-[0_10px_35px_rgba(0,0,0,0.35),0_0_24px_rgba(255,90,0,0.08)]',
        className
      )}
      {...props}
    >
      {/* Hardware-accelerated Spotlight Glow Overlay */}
      <div
        className="pointer-events-none absolute -inset-px rounded-xl opacity-0 transition-opacity duration-300 group-hover:opacity-100 z-0"
        style={{
          background: isHovered
            ? `radial-gradient(${glowSize}px circle at ${position.x}px ${position.y}px, ${spotlightColor}, transparent 65%)`
            : undefined,
        }}
        aria-hidden="true"
      />

      {/* Relative content container */}
      <div className="relative z-10 w-full h-full">
        {children}
      </div>
    </motion.div>
  );
};
