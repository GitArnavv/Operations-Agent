import React, { useEffect, useState } from 'react';

export interface NavigationProgressBarProps {
  isNavigating: boolean;
}

/**
 * High-performance 2px top navigation progress indicator.
 * Displays immediate visual feedback upon link click to eliminate perception of sluggishness.
 */
export const NavigationProgressBar: React.FC<NavigationProgressBarProps> = ({ isNavigating }) => {
  const [progress, setProgress] = useState(0);
  const [visible, setVisible] = useState(false);

  useEffect(() => {
    let timer: any;
    let incrementTimer: any;

    if (isNavigating) {
      setVisible(true);
      setProgress(25);

      // Smooth trickle progress
      incrementTimer = setInterval(() => {
        setProgress((prev) => {
          if (prev >= 85) return prev;
          const delta = Math.max(1, (90 - prev) * 0.15);
          return Math.min(prev + delta, 85);
        });
      }, 100);
    } else {
      setProgress(100);
      timer = setTimeout(() => {
        setVisible(false);
        setProgress(0);
      }, 250);
    }

    return () => {
      clearInterval(incrementTimer);
      clearTimeout(timer);
    };
  }, [isNavigating]);

  if (!visible && progress === 0) return null;

  return (
    <div
      className="fixed top-0 left-0 right-0 z-[9999] pointer-events-none h-[2.5px] bg-transparent overflow-hidden"
      role="progressbar"
      aria-valuemin={0}
      aria-valuemax={100}
      aria-valuenow={Math.round(progress)}
    >
      <div
        className="h-full bg-gradient-to-r from-[#FF6500] via-[#FF8533] to-[#FF4D00] transition-all ease-out shadow-[0_0_10px_rgba(255,90,0,0.8),0_0_20px_rgba(255,101,0,0.5)]"
        style={{
          width: `${progress}%`,
          transitionDuration: isNavigating ? '200ms' : '150ms',
        }}
      />
    </div>
  );
};
