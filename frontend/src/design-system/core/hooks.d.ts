export declare function useIndicator(
  value: string | null | undefined,
  deps?: unknown[],
): [React.MutableRefObject<Record<string, HTMLElement | null>>, { left: number; width: number } | null];
export declare function useMounted(delay?: number): boolean;
export declare function useCountUp(target: number | string, duration?: number): number | string;
export declare function fmt(n: number | string, d?: number): string;
