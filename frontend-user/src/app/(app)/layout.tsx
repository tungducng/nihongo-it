'use client'

import type { ReactNode } from 'react'
import { useAuthStore } from '@/stores/auth.store'
import { Header } from '@/components/layout/Header'
import { Loader } from '@/components/common/Loader'

/**
 * Authenticated route group layout. Gates rendering on the auth store having
 * finished initialize() — this prevents child client components from firing
 * data-fetch useEffects (which would 401 and trigger the axios interceptor's
 * full-page redirect) before the /auth/refresh-token roundtrip completes.
 *
 * proxy.ts already gates by cookie presence; this layout handles the
 * post-paint race for in-flight session restore.
 */
export default function AppLayout({ children }: { children: ReactNode }) {
  const initialized = useAuthStore((s) => s.initialized)

  if (!initialized) {
    return (
      <div className="flex min-h-screen items-center justify-center">
        <Loader label="Đang khôi phục phiên làm việc..." />
      </div>
    )
  }

  return (
    <div className="flex min-h-screen flex-col">
      <Header />
      <main className="container mx-auto flex-1 px-4 py-6">{children}</main>
    </div>
  )
}
