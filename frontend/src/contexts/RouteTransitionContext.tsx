import React, { createContext, useContext, useTransition, useCallback } from 'react';
import { useNavigate, useLocation, NavigateOptions, NavLinkProps, NavLink, LinkProps, Link } from 'react-router-dom';
import { prefetchRoute } from '../utils/routePrefetch';
import { NavigationProgressBar } from '../components/ui/NavigationProgressBar';

interface RouteTransitionContextValue {
  navigateWithTransition: (to: string, options?: NavigateOptions) => void;
  isPending: boolean;
  prefetch: (path: string) => void;
}

const RouteTransitionContext = createContext<RouteTransitionContextValue | null>(null);

export const RouteTransitionProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [isPending, startTransition] = useTransition();
  const navigate = useNavigate();
  const location = useLocation();

  const prefetch = useCallback((path: string) => {
    prefetchRoute(path);
  }, []);

  const navigateWithTransition = useCallback(
    (to: string, options?: NavigateOptions) => {
      // If already on the target path, skip transition
      if (location.pathname === to) {
        return;
      }

      // Prefetch chunk immediately
      prefetchRoute(to);

      // Execute navigation in concurrent transition
      startTransition(() => {
        navigate(to, options);
      });
    },
    [navigate, location.pathname]
  );

  return (
    <RouteTransitionContext.Provider
      value={{
        navigateWithTransition,
        isPending,
        prefetch,
      }}
    >
      <NavigationProgressBar isNavigating={isPending} />
      {children}
    </RouteTransitionContext.Provider>
  );
};

export function useRouteTransition(): RouteTransitionContextValue {
  const context = useContext(RouteTransitionContext);
  if (!context) {
    const navigate = useNavigate();
    return {
      navigateWithTransition: (to, opts) => navigate(to, opts),
      isPending: false,
      prefetch: prefetchRoute,
    };
  }
  return context;
}

export interface PrefetchNavLinkProps extends Omit<NavLinkProps, 'to'> {
  to: string;
}

/**
 * Optimistic NavLink that prefetches chunk on hover/focus and transitions smoothly.
 */
export const PrefetchNavLink = React.forwardRef<HTMLAnchorElement, PrefetchNavLinkProps>(
  ({ to, onClick, onMouseEnter, onFocus, onTouchStart, ...props }, ref) => {
    const { navigateWithTransition, prefetch } = useRouteTransition();

    const handlePrefetch = useCallback(
      (e: React.SyntheticEvent) => {
        prefetch(to);
      },
      [to, prefetch]
    );

    const handleClick = useCallback(
      (e: React.MouseEvent<HTMLAnchorElement>) => {
        // Allow default behavior for meta keys (Ctrl+click, Cmd+click, etc.)
        if (e.metaKey || e.ctrlKey || e.shiftKey || e.altKey || e.button !== 0) {
          if (onClick) onClick(e);
          return;
        }

        e.preventDefault();
        if (onClick) onClick(e);
        navigateWithTransition(to);
      },
      [to, onClick, navigateWithTransition]
    );

    return (
      <NavLink
        ref={ref}
        to={to}
        onClick={handleClick}
        onMouseEnter={(e) => {
          handlePrefetch(e);
          if (onMouseEnter) onMouseEnter(e);
        }}
        onFocus={(e) => {
          handlePrefetch(e);
          if (onFocus) onFocus(e);
        }}
        onTouchStart={(e) => {
          handlePrefetch(e);
          if (onTouchStart) onTouchStart(e);
        }}
        {...props}
      />
    );
  }
);
PrefetchNavLink.displayName = 'PrefetchNavLink';

export interface PrefetchLinkProps extends Omit<LinkProps, 'to'> {
  to: string;
}

/**
 * Optimistic Link with chunk prefetch and concurrent transition.
 */
export const PrefetchLink = React.forwardRef<HTMLAnchorElement, PrefetchLinkProps>(
  ({ to, onClick, onMouseEnter, onFocus, onTouchStart, ...props }, ref) => {
    const { navigateWithTransition, prefetch } = useRouteTransition();

    const handlePrefetch = useCallback(() => {
      prefetch(to);
    }, [to, prefetch]);

    const handleClick = useCallback(
      (e: React.MouseEvent<HTMLAnchorElement>) => {
        if (e.metaKey || e.ctrlKey || e.shiftKey || e.altKey || e.button !== 0) {
          if (onClick) onClick(e);
          return;
        }

        e.preventDefault();
        if (onClick) onClick(e);
        navigateWithTransition(to);
      },
      [to, onClick, navigateWithTransition]
    );

    return (
      <Link
        ref={ref}
        to={to}
        onClick={handleClick}
        onMouseEnter={(e) => {
          handlePrefetch();
          if (onMouseEnter) onMouseEnter(e);
        }}
        onFocus={(e) => {
          handlePrefetch();
          if (onFocus) onFocus(e);
        }}
        onTouchStart={(e) => {
          handlePrefetch();
          if (onTouchStart) onTouchStart(e);
        }}
        {...props}
      />
    );
  }
);
PrefetchLink.displayName = 'PrefetchLink';
