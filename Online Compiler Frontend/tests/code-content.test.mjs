import test from 'node:test';
import assert from 'node:assert/strict';
import { normalizeStoredCode } from '../src/code-content.js';

test('decodes legacy file content containing literal escaped newlines and quotes', () => {
  const stored = 'public class Main {\\n    public static void main(String[] args) {\\n        System.out.println(\\"Hello, Online Compiler!\\");\\n    }\\n}';
  const expected = 'public class Main {\n    public static void main(String[] args) {\n        System.out.println("Hello, Online Compiler!");\n    }\n}';
  assert.equal(normalizeStoredCode(stored), expected);
});

test('leaves correctly stored source with real newlines unchanged', () => {
  const source = 'class Main {\n  public static void main(String[] args) {}\n}';
  assert.equal(normalizeStoredCode(source), source);
});

test('returns empty source safely', () => {
  assert.equal(normalizeStoredCode(''), '');
  assert.equal(normalizeStoredCode(null), '');
});

test('decodes a complete JSON-stringified source value from legacy saves', () => {
  const source = 'public class Main2 {\n    public static void main(String[] args) {\n        System.out.println("Hello, Online Compiler!");\n    }\n}';
  assert.equal(normalizeStoredCode(JSON.stringify(source)), source);
});

test('does not alter a legitimate Java string containing an escaped newline', () => {
  const source = 'class Main {\n  String line = "first\\nsecond";\n}';
  assert.equal(normalizeStoredCode(source), source);
});
