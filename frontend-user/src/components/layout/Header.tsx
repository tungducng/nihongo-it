'use client'

import Link from 'next/link'
import { usePathname, useRouter } from 'next/navigation'
import { LogOut, User as UserIcon, Settings, Lock } from 'lucide-react'
import { useAuthStore } from '@/stores/auth.store'
import { useAppToast } from '@/hooks/useAppToast'
import { Button } from '@/components/ui/button'
import { Avatar, AvatarFallback, AvatarImage } from '@/components/ui/avatar'
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuLabel,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from '@/components/ui/dropdown-menu'
import { NotificationBell } from './NotificationBell'

const NAV_LINKS = [
  { href: '/vocabulary', label: 'Từ vựng' },
  { href: '/conversation', label: 'Hội thoại' },
  { href: '/flashcards/study', label: 'Flashcards' },
  { href: '/statistics', label: 'Thống kê' },
]

export function Header() {
  const router = useRouter()
  const pathname = usePathname()
  const toast = useAppToast()
  const user = useAuthStore((s) => s.user)
  const logout = useAuthStore((s) => s.logout)

  async function handleLogout() {
    await logout()
    toast.success('Đã đăng xuất')
    router.push('/login')
  }

  const initials =
    user?.fullName
      ?.split(' ')
      .map((w) => w[0])
      .join('')
      .slice(0, 2)
      .toUpperCase() ?? '?'

  return (
    <header className="bg-background sticky top-0 z-10 flex h-14 items-center gap-7 border-b px-6">
      <Link href="/" className="flex items-center gap-2">
        <span className="bg-accent text-accent-foreground font-jp grid h-[26px] w-[26px] place-items-center rounded-md text-[15px] font-bold leading-none">
          日
        </span>
        <span className="text-[16px] font-semibold tracking-tight text-[color:var(--washi-900)]">
          Nihongo IT
        </span>
      </Link>

      <nav className="hidden items-center gap-6 md:flex">
        {NAV_LINKS.map((link) => {
          const isActive = pathname === link.href || pathname.startsWith(link.href + '/')
          return (
            <Link
              key={link.href}
              href={link.href}
              className={
                isActive
                  ? 'relative text-[13px] font-medium text-[color:var(--washi-900)] after:absolute after:-bottom-[19px] after:left-0 after:right-0 after:h-[2px] after:rounded-sm after:bg-[color:var(--ai-500)]'
                  : 'text-muted-foreground hover:text-foreground text-[13px] transition-colors'
              }
            >
              {link.label}
            </Link>
          )
        })}
      </nav>

      {user ? (
        <div className="ml-auto flex items-center gap-1">
          <NotificationBell />
          <DropdownMenu>
            <DropdownMenuTrigger asChild>
              <Button variant="ghost" className="relative h-9 w-9 rounded-full p-0">
                <Avatar className="h-8 w-8">
                  {user.profilePicture && (
                    <AvatarImage src={user.profilePicture} alt={user.fullName} />
                  )}
                  <AvatarFallback className="bg-primary text-primary-foreground text-[13px] font-semibold">
                    {initials}
                  </AvatarFallback>
                </Avatar>
              </Button>
            </DropdownMenuTrigger>
            <DropdownMenuContent align="end" className="w-56">
              <DropdownMenuLabel>
                <div className="flex flex-col">
                  <span className="text-sm font-medium">{user.fullName}</span>
                  <span className="text-muted-foreground truncate text-xs">{user.email}</span>
                </div>
              </DropdownMenuLabel>
              <DropdownMenuSeparator />
              <DropdownMenuItem asChild>
                <Link href="/profile" className="cursor-pointer">
                  <UserIcon className="mr-2 size-4" />
                  Hồ sơ
                </Link>
              </DropdownMenuItem>
              <DropdownMenuItem asChild>
                <Link href="/account/settings" className="cursor-pointer">
                  <Settings className="mr-2 size-4" />
                  Cài đặt
                </Link>
              </DropdownMenuItem>
              <DropdownMenuItem asChild>
                <Link href="/account/change-password" className="cursor-pointer">
                  <Lock className="mr-2 size-4" />
                  Đổi mật khẩu
                </Link>
              </DropdownMenuItem>
              <DropdownMenuSeparator />
              <DropdownMenuItem onClick={handleLogout} className="text-destructive cursor-pointer">
                <LogOut className="mr-2 size-4" />
                Đăng xuất
              </DropdownMenuItem>
            </DropdownMenuContent>
          </DropdownMenu>
        </div>
      ) : (
        <div className="ml-auto flex items-center gap-2">
          <Button variant="ghost" asChild>
            <Link href="/login">Đăng nhập</Link>
          </Button>
          <Button asChild>
            <Link href="/register">Đăng ký</Link>
          </Button>
        </div>
      )}
    </header>
  )
}
