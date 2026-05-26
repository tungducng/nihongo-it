'use client'

// FSRS rating scale mapped onto the JLPT chroma scale so the same colour
// means the same difficulty everywhere: again=N1 rose, hard=N3 amber,
// good=N4 sky, easy=N5 emerald.
const RATINGS = [
  { value: 1, label: 'Quên', shortcut: '1', cls: 'rate-again' },
  { value: 2, label: 'Khó', shortcut: '2', cls: 'rate-hard' },
  { value: 3, label: 'Tốt', shortcut: '3', cls: 'rate-good' },
  { value: 4, label: 'Dễ', shortcut: '4', cls: 'rate-easy' },
] as const

interface RatingButtonsProps {
  onRate: (rating: number) => void
  disabled?: boolean
}

export function RatingButtons({ onRate, disabled }: RatingButtonsProps) {
  return (
    <div className="mx-auto grid max-w-[560px] grid-cols-4 gap-2.5">
      {RATINGS.map((r) => (
        <button
          key={r.value}
          type="button"
          disabled={disabled}
          onClick={() => onRate(r.value)}
          className={`${r.cls} flex h-11 flex-col items-center justify-center gap-0.5 rounded-[10px] border text-[14px] font-semibold transition-[filter] hover:brightness-[0.97] disabled:opacity-50 disabled:cursor-not-allowed`}
        >
          <span>{r.label}</span>
          <span className="font-mono text-[10px] font-medium opacity-70">{r.shortcut}</span>
        </button>
      ))}
    </div>
  )
}
