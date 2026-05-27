/**
 * Vietnamese relative time formatter using Intl.RelativeTimeFormat.
 * Falls back to "vài giây trước" for sub-minute durations.
 */
const rtf = new Intl.RelativeTimeFormat('vi', { numeric: 'auto' })

export function formatRelative(iso: string): string {
  const then = new Date(iso).getTime()
  if (isNaN(then)) return ''
  const diffMs = Date.now() - then
  const sec = Math.floor(diffMs / 1000)
  if (sec < 60) return 'vài giây trước'
  const min = Math.floor(sec / 60)
  if (min < 60) return rtf.format(-min, 'minute')
  const hr = Math.floor(min / 60)
  if (hr < 24) return rtf.format(-hr, 'hour')
  const day = Math.floor(hr / 24)
  if (day < 30) return rtf.format(-day, 'day')
  const month = Math.floor(day / 30)
  if (month < 12) return rtf.format(-month, 'month')
  return rtf.format(-Math.floor(month / 12), 'year')
}
