export function parseValidityToMs(validity: string): number {
  if (!validity) return 0;
  const v = validity.toLowerCase().trim();
  const match = v.match(/^(\d+)\s*(day|month|year)s?$/);
  if (!match) return 365 * 24 * 60 * 60 * 1000; // default 1 year fallback
  const num = parseInt(match[1]);
  const unit = match[2];
  if (unit === 'day') return num * 24 * 60 * 60 * 1000;
  if (unit === 'month') return num * 30 * 24 * 60 * 60 * 1000; // approximate
  if (unit === 'year') return num * 365 * 24 * 60 * 60 * 1000;
  return 365 * 24 * 60 * 60 * 1000;
}
