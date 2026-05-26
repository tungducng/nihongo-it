'use client'

import Link from 'next/link'
import { usePathname } from 'next/navigation'
import {
  BarChart3,
  BookOpen,
  FolderTree,
  LayoutDashboard,
  MessageSquare,
  Tag,
  Users,
} from 'lucide-react'
import { cn } from '@/lib/utils'

const NAV_ITEMS = [
  { href: '/', label: 'Dashboard', Icon: LayoutDashboard, exact: true },
  { href: '/users', label: 'Người dùng', Icon: Users },
  { href: '/categories', label: 'Danh mục', Icon: FolderTree },
  { href: '/topics', label: 'Chủ đề', Icon: Tag },
  { href: '/vocabulary', label: 'Từ vựng', Icon: BookOpen },
  { href: '/conversations', label: 'Hội thoại', Icon: MessageSquare },
  { href: '/statistics', label: 'Thống kê', Icon: BarChart3 },
]

function isActive(pathname: string, href: string, exact?: boolean): boolean {
  if (exact) return pathname === href
  return pathname === href || pathname.startsWith(`${href}/`)
}

function BrandHeader() {
  return (
    <div className="border-b px-5 py-4">
      <Link href="/" className="flex items-center gap-2">
        <span className="bg-accent text-accent-foreground font-jp grid h-[26px] w-[26px] place-items-center rounded-md text-[15px] font-bold leading-none">
          日
        </span>
        <span className="text-[16px] font-semibold tracking-tight text-[color:var(--washi-900)]">
          Nihongo IT
        </span>
      </Link>
      <p className="eyebrow mt-1.5">Quản trị</p>
    </div>
  )
}

function NavLink({
  href,
  label,
  Icon,
  active,
  onNavigate,
}: {
  href: string
  label: string
  Icon: typeof LayoutDashboard
  active: boolean
  onNavigate?: () => void
}) {
  return (
    <Link
      href={href}
      onClick={onNavigate}
      className={cn(
        'flex items-center gap-2.5 rounded-md px-3 py-2 text-[13px] font-medium transition-colors',
        active
          ? 'bg-sidebar-primary text-sidebar-primary-foreground'
          : 'text-[color:var(--washi-600)] hover:bg-[color:var(--sidebar-accent)] hover:text-[color:var(--sidebar-accent-foreground)]',
      )}
    >
      <Icon className="size-4" />
      {label}
    </Link>
  )
}

export function Sidebar() {
  const pathname = usePathname()
  return (
    <aside className="bg-sidebar hidden w-60 shrink-0 flex-col border-r md:flex">
      <BrandHeader />
      <nav className="flex-1 space-y-1 p-3">
        {NAV_ITEMS.map(({ href, label, Icon, exact }) => (
          <NavLink
            key={href}
            href={href}
            label={label}
            Icon={Icon}
            active={isActive(pathname, href, exact)}
          />
        ))}
      </nav>
    </aside>
  )
}

export function MobileNavLinks({ onNavigate }: { onNavigate?: () => void }) {
  const pathname = usePathname()
  return (
    <>
      <BrandHeader />
      <nav className="space-y-1 p-3">
        {NAV_ITEMS.map(({ href, label, Icon, exact }) => (
          <NavLink
            key={href}
            href={href}
            label={label}
            Icon={Icon}
            active={isActive(pathname, href, exact)}
            onNavigate={onNavigate}
          />
        ))}
      </nav>
    </>
  )
}
