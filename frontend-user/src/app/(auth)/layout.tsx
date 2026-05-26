import Link from 'next/link'
import type { ReactNode } from 'react'

export default function AuthLayout({ children }: { children: ReactNode }) {
  return (
    <div className="bg-muted grid min-h-screen place-items-center p-6">
      <div className="w-full max-w-[380px]">
        <Link href="/" className="mb-6 flex flex-col items-center gap-2.5">
          <span className="bg-accent text-accent-foreground font-jp grid h-11 w-11 place-items-center rounded-[10px] text-[24px] font-bold leading-none">
            日
          </span>
          <span className="text-[18px] font-semibold tracking-tight text-[color:var(--washi-900)]">
            Nihongo IT
          </span>
        </Link>
        {children}
      </div>
    </div>
  )
}
