import { useEffect, useState } from 'react';

export function useDebouncedState(query: string, delay = 500) {
  const [debouncedValue, setDebouncedValue] = useState(query);

  useEffect(() => {
    const timeout = setTimeout(() => setDebouncedValue(query), delay);
    return () => clearTimeout(timeout);
  }, [query, delay]);

  return debouncedValue;
}
