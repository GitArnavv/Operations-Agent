import React from 'react';
import { motion, HTMLMotionProps, Variants } from 'framer-motion';

export const staggerContainerVariants: Variants = {
  initial: {},
  animate: {
    transition: {
      staggerChildren: 0.04,
      delayChildren: 0.05,
    },
  },
};

export const staggerItemVariants: Variants = {
  initial: {
    opacity: 0,
    y: 12,
    filter: 'blur(4px)',
  },
  animate: {
    opacity: 1,
    y: 0,
    filter: 'blur(0px)',
    transition: {
      type: 'spring',
      stiffness: 300,
      damping: 24,
      mass: 0.8,
    },
  },
};

export interface StaggerContainerProps extends HTMLMotionProps<'div'> {
  children: React.ReactNode;
}

/**
 * High-performance StaggerContainer for fluid parent-child page mounting without visual jumps.
 */
export const StaggerContainer: React.FC<StaggerContainerProps> = ({
  children,
  className = '',
  ...props
}) => {
  return (
    <motion.div
      variants={staggerContainerVariants}
      initial="initial"
      animate="animate"
      className={className}
      {...props}
    >
      {children}
    </motion.div>
  );
};

export interface StaggerItemProps extends HTMLMotionProps<'div'> {
  children: React.ReactNode;
}

/**
 * Child item inside StaggerContainer that mounts with spring elevation and zero reflow.
 */
export const StaggerItem: React.FC<StaggerItemProps> = ({
  children,
  className = '',
  ...props
}) => {
  return (
    <motion.div variants={staggerItemVariants} className={className} {...props}>
      {children}
    </motion.div>
  );
};
