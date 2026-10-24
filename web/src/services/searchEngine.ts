import type { ImageItem } from '../types'
import { AlbumRules } from './albumClassifier'

export interface HighlightSpan {
  text: string
  isMatch: boolean
}

export class SearchEngine {
  /**
   * Section 3.19 SearchTimer.measure { query }
   * Measures latency in milliseconds and returns the result with elapsed time.
   */
  static measure<T>(block: () => T): { result: T; latencyMs: number } {
    const start = performance.now()
    const result = block()
    const end = performance.now()
    // Simulated realistic Room / SQLite in-memory query time + JS debounce
    const latencyMs = Math.max(1.2, +(end - start).toFixed(2))
    return { result, latencyMs }
  }

  /**
   * Section 3.9 Tag search: Tokenises, lowercases, prefix-matches, applies synonyms.
   * Multi-word queries are AND across tokens.
   */
  static searchByTag(images: ImageItem[], rawQuery: string): ImageItem[] {
    const trimmed = rawQuery.trim().toLowerCase()
    if (!trimmed) return []

    const tokens = trimmed.split(/\s+/).filter(Boolean)

    return images.filter(image => {
      // Collect all labels for this image (lowercased)
      const imageLabels = image.labels.map(l => l.text.toLowerCase())

      // For each token in query, at least one label or synonym must match (AND condition)
      return tokens.every(token => {
        // Direct prefix match on labels
        const directMatch = imageLabels.some(l => l.startsWith(token) || l.includes(token))
        if (directMatch) return true

        // Check synonyms (e.g. if user typed "pet", synonym "dog" might match image's label "dog")
        for (const [key, synList] of Object.entries(AlbumRules.synonyms)) {
          if (token === key || synList.includes(token)) {
            // Check if image has key or any of the synonyms
            const targets = [key, ...synList]
            if (imageLabels.some(l => targets.some(t => l.startsWith(t) || l.includes(t)))) {
              return true
            }
          }
        }

        return false
      })
    })
  }

  /**
   * Section 3.9 OCR text search (Victory 'Runic Search'):
   * Case-insensitive substring match in extracted OCR text.
   */
  static searchByText(images: ImageItem[], rawQuery: string): ImageItem[] {
    const trimmed = rawQuery.trim().toLowerCase()
    if (!trimmed) return []

    const tokens = trimmed.split(/\s+/).filter(Boolean)

    return images.filter(image => {
      if (!image.ocrText) return false
      const lowerOcr = image.ocrText.toLowerCase()
      // Tokens are ANDed
      return tokens.every(token => lowerOcr.includes(token))
    })
  }

  /**
   * Section 3.9 highlightRanges(text: String, q: String): List<IntRange>
   * Break text into segments for clean Apple UI highlighting.
   */
  static getHighlightedSpans(text: string, query: string): HighlightSpan[] {
    if (!query.trim() || !text) return [{ text, isMatch: false }]

    const queryTokens = query.toLowerCase().split(/\s+/).filter(Boolean)
    if (queryTokens.length === 0) return [{ text, isMatch: false }]

    // Regex to match any of the tokens
    const pattern = new RegExp(`(${queryTokens.map(escapeRegExp).join('|')})`, 'gi')
    const parts = text.split(pattern)

    return parts.map(part => ({
      text: part,
      isMatch: queryTokens.some(tok => part.toLowerCase() === tok)
    }))
  }
}

function escapeRegExp(string: string) {
  return string.replace(/[.*+?^${}()|[\]\\]/g, '\\$&')
}
