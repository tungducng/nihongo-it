import Link from 'next/link'
import { Button } from '@/components/ui/button'

const FEATURES = [
  {
    eyebrow: 'TỪ VỰNG',
    title: 'Hơn 1000 từ IT theo JLPT',
    body: 'Từ vựng chuyên ngành phân loại theo cấp độ N5–N1, bao phủ backend, frontend, DevOps và quản lý dự án.',
    sample: { jp: '配備', vi: 'triển khai · deployment' },
  },
  {
    eyebrow: 'HỘI THOẠI',
    title: 'Tình huống thực tế',
    body: 'Báo cáo standup, code review, sprint planning với phân tích phát âm bằng AI.',
    sample: { jp: 'コードレビュー', vi: 'review mã nguồn' },
  },
  {
    eyebrow: 'FLASHCARD',
    title: 'Lặp lại ngắt quãng (SRS)',
    body: 'Học theo thuật toán FSRS — chỉ ôn khi thực sự cần. Tiết kiệm thời gian, nhớ lâu hơn.',
    sample: { jp: '会議', vi: 'cuộc họp' },
  },
]

export default function HomePage() {
  return (
    <main className="bg-background min-h-screen">
      <header className="flex h-14 items-center gap-7 border-b px-6">
        <Link href="/" className="flex items-center gap-2">
          <span className="bg-accent text-accent-foreground font-jp grid h-[26px] w-[26px] place-items-center rounded-md text-[15px] font-bold leading-none">
            日
          </span>
          <span className="text-[16px] font-semibold tracking-tight text-[color:var(--washi-900)]">
            Nihongo IT
          </span>
        </Link>
        <div className="ml-auto flex items-center gap-2">
          <Button variant="ghost" asChild>
            <Link href="/login">Đăng nhập</Link>
          </Button>
          <Button asChild>
            <Link href="/register">Đăng ký</Link>
          </Button>
        </div>
      </header>

      <section className="mx-auto max-w-[1180px] px-6 pb-16 pt-20">
        <p className="eyebrow mb-4">Học tiếng Nhật cho kỹ sư IT</p>
        <h1 className="max-w-[760px] text-[44px] font-bold leading-[1.1] tracking-[-0.02em] text-[color:var(--washi-900)]">
          Từ vựng IT, hội thoại công việc, flashcards có thuật toán
          <span className="text-[color:var(--ai-500)]"> — gọn trong một chỗ.</span>
        </h1>
        <p className="text-muted-foreground mt-5 max-w-[620px] text-[15px] leading-relaxed">
          Học tiếng Nhật chuyên ngành IT với spaced repetition và phân tích phát âm bằng AI. Dành
          cho kỹ sư đang làm việc cho công ty Nhật hoặc chuẩn bị JLPT.
        </p>

        <div className="mt-8 flex flex-wrap gap-3">
          <Button size="lg" asChild>
            <Link href="/register">Bắt đầu miễn phí</Link>
          </Button>
          <Button size="lg" variant="outline" asChild>
            <Link href="/login">Tôi đã có tài khoản</Link>
          </Button>
        </div>
      </section>

      <section className="mx-auto max-w-[1180px] px-6 pb-24">
        <div className="grid gap-4 md:grid-cols-3">
          {FEATURES.map((f) => (
            <article
              key={f.eyebrow}
              className="bg-card border-border rounded-[10px] border p-6 shadow-xs"
            >
              <p className="eyebrow mb-2">{f.eyebrow}</p>
              <h2 className="mb-2 text-[18px] font-semibold tracking-tight text-[color:var(--washi-900)]">
                {f.title}
              </h2>
              <p className="text-muted-foreground text-[13px] leading-relaxed">{f.body}</p>
              <div className="bg-muted mt-4 rounded-[8px] px-3 py-2.5">
                <p className="font-jp text-[16px] font-semibold text-[color:var(--washi-900)]">
                  {f.sample.jp}
                </p>
                <p className="text-muted-foreground mt-0.5 text-[12px]">{f.sample.vi}</p>
              </div>
            </article>
          ))}
        </div>
      </section>
    </main>
  )
}
