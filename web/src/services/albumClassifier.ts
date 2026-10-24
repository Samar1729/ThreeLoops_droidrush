import type { AlbumType, LabelResult } from '../types'

export const AlbumRules = {
  receiptKeywords: ['total', 'subtotal', 'invoice', 'gst', 'tax', 'amount', 'bill', 'receipt', 'qty'],
  documentMinOcrLength: 80,
  foodLabels: [
    'food', 'dish', 'cuisine', 'meal', 'fruit', 'vegetable', 'dessert',
    'pizza', 'pasta', 'burger', 'salad', 'coffee', 'cake', 'bread',
    'snack', 'drink', 'beverage', 'tableware', 'recipe', 'breakfast', 'lunch', 'dinner'
  ],
  peopleLabels: [
    'person', 'face', 'smile', 'human', 'boy', 'girl', 'man', 'woman',
    'portrait', 'crowd', 'baby', 'child', 'teenager', 'family', 'selfie'
  ],
  textLikeLabels: [
    'text', 'document', 'paper', 'font', 'handwriting', 'envelope', 'page',
    'receipt', 'book', 'newspaper', 'stationery', 'label', 'ticket', 'diagram'
  ],
  synonyms: {
    'cat': ['pet', 'feline', 'kitten'],
    'dog': ['pet', 'canine', 'puppy'],
    'selfie': ['person', 'portrait', 'face'],
    'car': ['automobile', 'vehicle', 'transport'],
    'latte': ['coffee', 'beverage', 'drink'],
    'receipt': ['bill', 'invoice', 'expense'],
    'food': ['dish', 'meal', 'cuisine']
  } as Record<string, string[]>
}

export function isTextLike(labels: LabelResult[]): boolean {
  const lowercaseLabels = labels.map(l => l.text.toLowerCase())
  return lowercaseLabels.some(l => 
    AlbumRules.textLikeLabels.some(tl => l.includes(tl))
  )
}

export class AlbumClassifier {
  static classify(
    labels: LabelResult[],
    path: string,
    ocr: string,
    faceCount: number = 0
  ): Set<AlbumType> {
    const result = new Set<AlbumType>()
    const lowerPath = path.toLowerCase()
    const lowerOcr = ocr.toLowerCase()
    const lowerLabels = labels.map(l => l.text.toLowerCase())

    // 1. SCREENSHOTS: relativePath contains 'Screenshots' OR displayName starts with 'Screenshot_'
    if (lowerPath.includes('screenshots') || lowerPath.includes('screenshot_') || lowerPath.startsWith('screenshot')) {
      result.add('SCREENSHOTS')
    }

    // Check text-like gate
    const hasTextLabels = isTextLike(labels)

    // Count receipt keywords
    let receiptKeywordHits = 0
    const matchedReceiptWords: string[] = []
    for (const kw of AlbumRules.receiptKeywords) {
      if (lowerOcr.includes(kw)) {
        receiptKeywordHits++
        matchedReceiptWords.push(kw)
      }
    }

    // 2. RECEIPTS: text-like labels AND OCR contains 2+ of [total, subtotal, invoice, gst, tax, amount, bill, receipt, qty]
    const isReceipt = (hasTextLabels || lowerOcr.length > 30) && receiptKeywordHits >= 2
    if (isReceipt) {
      result.add('RECEIPTS')
    }

    // 3. DOCUMENTS: text-like labels AND OCR length above threshold AND not RECEIPTS
    if ((hasTextLabels || lowerOcr.length > 50) && lowerOcr.length >= AlbumRules.documentMinOcrLength && !isReceipt) {
      result.add('DOCUMENTS')
    }

    // 4. FOOD: label in [food, dish, cuisine, meal, fruit, vegetable, dessert, ...]
    const hasFoodLabel = lowerLabels.some(l => 
      AlbumRules.foodLabels.some(fl => l.includes(fl))
    )
    if (hasFoodLabel) {
      result.add('FOOD')
    }

    // 5. PEOPLE: label in [person, face, smile, ...] OR faceCount > 0
    const hasPeopleLabel = lowerLabels.some(l => 
      AlbumRules.peopleLabels.some(pl => l.includes(pl))
    )
    if (hasPeopleLabel || faceCount > 0) {
      result.add('PEOPLE')
    }

    return result
  }

  static explain(
    labels: LabelResult[],
    path: string,
    ocr: string,
    faceCount: number = 0
  ): string {
    const lowerPath = path.toLowerCase()
    const lowerOcr = ocr.toLowerCase()
    const lowerLabels = labels.map(l => l.text.toLowerCase())
    const reasons: string[] = []

    if (lowerPath.includes('screenshots') || lowerPath.includes('screenshot_')) {
      reasons.push('SCREENSHOTS: file path matches "Pictures/Screenshots/" or filename prefix "Screenshot_"')
    }

    const matchedReceiptWords = AlbumRules.receiptKeywords.filter(kw => lowerOcr.includes(kw))
    if (matchedReceiptWords.length >= 2) {
      reasons.push(`RECEIPTS: OCR matched ${matchedReceiptWords.length} financial tokens [${matchedReceiptWords.slice(0, 3).join(', ')}] with text-like ML Kit labels`)
    } else if (lowerOcr.length >= AlbumRules.documentMinOcrLength && isTextLike(labels)) {
      reasons.push(`DOCUMENTS: OCR character volume (${lowerOcr.length} chars) exceeds document threshold (80 chars)`)
    }

    const matchedFood = lowerLabels.filter(l => AlbumRules.foodLabels.some(fl => l.includes(fl)))
    if (matchedFood.length > 0) {
      reasons.push(`FOOD: ML Kit Vision detected food semantics [${matchedFood.slice(0, 2).join(', ')}]`)
    }

    if (faceCount > 0) {
      reasons.push(`PEOPLE: ML Kit Face Detection identified ${faceCount} human face(s)`)
    } else {
      const matchedPeople = lowerLabels.filter(l => AlbumRules.peopleLabels.some(pl => l.includes(pl)))
      if (matchedPeople.length > 0) {
        reasons.push(`PEOPLE: ML Kit Labeler detected person labels [${matchedPeople.slice(0, 2).join(', ')}]`)
      }
    }

    return reasons.length > 0 ? reasons.join(' • ') : 'Standard gallery item (uncategorized)'
  }
}
