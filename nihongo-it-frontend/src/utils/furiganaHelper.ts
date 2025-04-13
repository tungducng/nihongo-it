import * as wanakana from 'wanakana';
import axios from 'axios';

// Regular expression to identify kanji characters
const kanjiRegex = /[\u4e00-\u9faf\u3400-\u4dbf]/;

// Cache for API responses to avoid repeated calls
const readingCache: Record<string, string> = {};

// Define types for Jisho API responses
interface JishoResponse {
  data: JishoWord[];
}

interface JishoWord {
  japanese: JapaneseWord[];
  senses: any[]; // We don't need the detailed sense data for furigana
}

interface JapaneseWord {
  word?: string;
  reading?: string;
}

/**
 * Adds furigana to Japanese text
 * @param text The Japanese text to process
 * @returns HTML string with ruby annotations for kanji
 */
export async function addFuriganaAsync(text: string): Promise<string> {
  if (!text) return '';

  try {
    // Use Jisho API to get readings
    const response = await axios.get<JishoResponse>(`https://jisho.org/api/v1/search/words?keyword=${encodeURIComponent(text)}`);
    const data = response.data;

    if (data.data && data.data.length > 0) {
      // Process the response to extract readings
      let result = text;

      // Iterate through all matching words (prioritize longer words)
      const words = data.data
        .filter(item => item.japanese && item.japanese.length > 0)
        .sort((a, b) => {
          // Get the longest word from each entry
          const aWord = a.japanese.reduce((longest: string, current) =>
            (current.word && current.word.length > longest.length) ? current.word : longest, '');
          const bWord = b.japanese.reduce((longest: string, current) =>
            (current.word && current.word.length > longest.length) ? current.word : longest, '');
          return bWord.length - aWord.length; // Sort by length (descending)
        });

      // Replace words with ruby tags (start with longest to avoid partial replacements)
      for (const wordData of words) {
        for (const japanese of wordData.japanese) {
          if (japanese.word && japanese.reading && text.includes(japanese.word)) {
            // Cache the reading
            readingCache[japanese.word] = japanese.reading;

            // Create ruby tag
            const rubyTag = `<ruby>${japanese.word}<rt>${japanese.reading}</rt></ruby>`;

            // Replace all occurrences in the text
            const regex = new RegExp(japanese.word, 'g');
            result = result.replace(regex, rubyTag);
          }
        }
      }

      return result;
    }

    // Fallback to manual character-by-character processing if API doesn't return useful data
    return processTextManually(text);

  } catch (error) {
    console.error('Error fetching readings from Jisho API:', error);
    // Fallback to manual processing
    return processTextManually(text);
  }
}

/**
 * Synchronous version that uses the cache and does basic processing
 * Good for when you need immediate results without waiting for API
 */
export function addFurigana(text: string): string {
  if (!text) return '';

  // Process the text using our cache and basic rules
  return processTextManually(text);
}

/**
 * Process text manually using the reading cache and character-by-character approach
 */
function processTextManually(text: string): string {
  let result = '';
  let i = 0;

  while (i < text.length) {
    // Try to match the longest cached word first
    let matched = false;

    // Look for cached words (up to 10 characters long)
    for (let len = Math.min(10, text.length - i); len > 0; len--) {
      const word = text.substring(i, i + len);
      if (readingCache[word]) {
        result += `<ruby>${word}<rt>${readingCache[word]}</rt></ruby>`;
        i += len;
        matched = true;
        break;
      }
    }

    if (!matched) {
      // If no cached word matches, check if it's a kanji character
      const char = text[i];
      if (kanjiRegex.test(char) && readingCache[char]) {
        // We have a reading for this individual kanji
        result += `<ruby>${char}<rt>${readingCache[char]}</rt></ruby>`;
      } else {
        // Just add the character as is
        result += char;
      }
      i++;
    }
  }

  return result;
}

/**
 * Add a reading to the cache manually
 */
export function addReading(kanji: string, reading: string): void {
  readingCache[kanji] = reading;
}

/**
 * Get a reading from the cache
 */
export function getReading(kanji: string): string | undefined {
  return readingCache[kanji];
}

/**
 * Detects if text contains kanji characters
 * @param text The text to check
 * @returns True if text contains kanji
 */
export function containsKanji(text: string): boolean {
  return kanjiRegex.test(text);
}

/**
 * Checks if a character is kanji
 * @param char Single character to check
 * @returns True if character is kanji
 */
export function isKanji(char: string): boolean {
  return char.length === 1 && kanjiRegex.test(char);
}

/**
 * Gets hiragana reading for kanji if available
 * @param kanji Kanji character or compound
 * @returns Hiragana reading or empty string if not found
 */
export function getKanjiReading(kanji: string): string {
  return readingCache[kanji] || '';
}
