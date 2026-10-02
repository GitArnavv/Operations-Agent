import React from 'react';

export const PageTransition: React.FC<{ children: React.ReactNode; className?: string }> = ({
  children,
  className = '',
}) => {
  return (
    <div className={`animate-fade-in animate-slide-up transition-opacity duration-200 ${className}`}>
      {children}
    </div>
  );
};
