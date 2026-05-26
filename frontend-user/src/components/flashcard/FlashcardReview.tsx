'use client'

import { useEffect } from 'react'
import { RotateCw } from 'lucide-react'
import { Card } from '@/components/ui/card'
import type { FlashcardDTO } from '@/types/learning.types'

interface FlashcardReviewProps {
  card: FlashcardDTO
  flipped: boolean
  onFlip: () => void
}

/**
 * Two-sided card. Click anywhere to flip. Uses Tailwind 4 custom utilities
 * defined in globals.css (perspective-card, transform-3d, backface-hidden,
 * rotate-y-180).
 */
export function FlashcardReview({ card, flipped, onFlip }: FlashcardReviewProps) {
  useEffect(() => {
    function onKey(e: KeyboardEvent) {
      if ((e.key === ' ' || e.key === 'Enter') && !flipped) {
        e.preventDefault()
        onFlip()
      }
    }
    window.addEventListener('keydown', onKey)
    return () => window.removeEventListener('keydown', onKey)
  }, [flipped, onFlip])

  return (
    <div className="perspective-card mx-auto w-full max-w-[560px]">
      <button
        type="button"
        onClick={onFlip}
        className="relative block h-[280px] w-full cursor-pointer text-left focus-visible:outline-none"
        aria-label={flipped ? 'Mặt sau thẻ' : 'Bấm để lật thẻ'}
      >
        <div
          className={`transform-3d relative h-full w-full transition-transform duration-500 ease-out ${
            flipped ? 'rotate-y-180' : ''
          }`}
        >
          {/* Front */}
          <Card className="backface-hidden bg-card absolute inset-0 flex items-center justify-center rounded-[14px] p-6 shadow-sm">
            <div className="text-center">
              <p
                data-testid="flashcard-front-term"
                className="jp-display text-[color:var(--washi-900)]"
              >
                {card.frontText}
              </p>
            </div>
            <div className="text-muted-foreground absolute bottom-[18px] left-1/2 inline-flex -translate-x-1/2 items-center gap-1.5 font-mono text-[11px]">
              <RotateCw className="size-3" />
              Bấm để lật · phím Space
            </div>
          </Card>

          {/* Back */}
          <Card className="backface-hidden rotate-y-180 bg-card absolute inset-0 flex items-center justify-center rounded-[14px] p-6 shadow-sm">
            <p
              data-testid="flashcard-back-text"
              className="font-jp text-[22px] font-medium text-[color:var(--washi-900)] sm:text-[24px] text-center"
            >
              {card.backText}
            </p>
          </Card>
        </div>
      </button>
    </div>
  )
}
