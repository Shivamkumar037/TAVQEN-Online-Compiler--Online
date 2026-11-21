/**
 * Decode file contents written by older clients that JSON-stringified the
 * source before sending it to a Spring @RequestBody String endpoint.
 * Correctly stored source is returned unchanged.
 */
export function normalizeStoredCode(value) {
  if (typeof value !== 'string' || value.length === 0) return value || '';

  // Older saves may contain a complete JSON string literal, including the
  // outer quotes added by JSON.stringify(source). Decode it first.
  const trimmed = value.trim();
  if (trimmed.length >= 2 && trimmed.startsWith('"') && trimmed.endsWith('"') &&
      (trimmed.includes('\\n') || trimmed.includes('\\r') || trimmed.includes('\\\"'))) {
    try {
      const decoded = JSON.parse(trimmed);
      if (typeof decoded === 'string') return decoded;
    } catch { /* Try the legacy unwrapped representation below. */ }
  }

  // Some older records contain escaped line breaks and escaped source quotes
  // but have no outer JSON quotes. Only decode when both markers are present,
  // so legitimate Java strings such as "\\n" are not modified.
  if (value.includes('\\n') && value.includes('\\\"')) {
    try {
      const escapedRawQuotes = value.replace(/(?<!\\)"/g, '\\"');
      const decoded = JSON.parse(`"${escapedRawQuotes}"`);
      if (typeof decoded === 'string') return decoded;
    } catch { /* Keep original if it is not valid serialized content. */ }
  }

  return value;
}
