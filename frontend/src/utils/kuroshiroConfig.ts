import Kuroshiro from 'kuroshiro';
import KuromojiAnalyzer from 'kuroshiro-analyzer-kuromoji';

/**
 * Creates and initializes a Kuroshiro instance with proper browser configuration
 */
export async function createKuroshiro(): Promise<Kuroshiro> {
  try {
    // Create a new Kuroshiro instance
    const kuroshiro = new Kuroshiro();

    // Configure the dictionary path properly for browser environments
    const dictPath = window.location.origin + '/dict';
    console.log(`Initializing Kuroshiro with dictionary at: ${dictPath}`);

    // Create an analyzer with the browser-compatible path
    const analyzer = new KuromojiAnalyzer({ dictPath });

    // Initialize with the analyzer
    await kuroshiro.init(analyzer);
    console.log('Kuroshiro initialization complete');

    return kuroshiro;
  } catch (error) {
    console.error('Failed to initialize Kuroshiro:', error);

    // Attempt to provide helpful diagnostics
    if (error instanceof Error && error.message.includes('path.join')) {
      console.error('This error is likely caused by path.join not being available in the browser.');
      console.error('Solution: Use browser-compatible path concatenation instead of path.join');
    }

    // Re-throw for the caller to handle
    throw error;
  }
}

/**
 * Simple furigana formatter that uses basic HTML without requiring Kuroshiro
 * This can be used as a fallback when Kuroshiro fails to initialize
 */
export function simpleFurigana(text: string, readings?: Record<string, string>): string {
  if (!text) return '';

  // If no readings provided, just return the original text
  if (!readings) return `<span class="japanese-text">${text}</span>`;

  // Otherwise use the readings to create basic ruby markup
  let result = '';

  // This is a very basic implementation - in a real app you'd need
  // more sophisticated text segmentation and reading assignment
  for (const kanji of Object.keys(readings)) {
    if (text.includes(kanji)) {
      const reading = readings[kanji];
      text = text.replace(
        kanji,
        `<ruby>${kanji}<rt>${reading}</rt></ruby>`
      );
    }
  }

  return `<span class="japanese-text">${text}</span>`;
}