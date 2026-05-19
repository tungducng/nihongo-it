'use client'

import { useCallback, useEffect, useState } from 'react'
import Link from 'next/link'
import { Bell, CheckCheck, Trash2 } from 'lucide-react'
import notificationService from '@/services/notification.service'
import { useAppToast } from '@/hooks/useAppToast'
import { extractApiError } from '@/types/common.types'
import { Button } from '@/components/ui/button'
import { Badge } from '@/components/ui/badge'
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuLabel,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from '@/components/ui/dropdown-menu'
import type { NotificationItem } from '@/types/notification.types'

const POLL_INTERVAL_MS = 60_000

function timeAgo(dateString: string): string {
  const date = new Date(dateString)
  const diff = Date.now() - date.getTime()
  if (Number.isNaN(diff)) return ''
  const minutes = Math.floor(diff / 60_000)
  if (minutes < 1) return 'vừa xong'
  if (minutes < 60) return `${minutes} phút trước`
  const hours = Math.floor(minutes / 60)
  if (hours < 24) return `${hours} giờ trước`
  const days = Math.floor(hours / 24)
  if (days < 30) return `${days} ngày trước`
  return date.toLocaleDateString('vi-VN')
}

export function NotificationBell() {
  const toast = useAppToast()
  const [open, setOpen] = useState(false)
  const [unread, setUnread] = useState(0)
  const [items, setItems] = useState<NotificationItem[]>([])
  const [loading, setLoading] = useState(false)

  const fetchUnread = useCallback(async () => {
    try {
      setUnread(await notificationService.getUnreadCount())
    } catch {
      /* silent */
    }
  }, [])

  const fetchList = useCallback(async () => {
    setLoading(true)
    try {
      const page = await notificationService.list(0, 10)
      setItems(page.content)
    } catch (err) {
      toast.error(extractApiError(err, 'Không tải được thông báo'))
    } finally {
      setLoading(false)
    }
  }, [toast])

  useEffect(() => {
    // eslint-disable-next-line react-hooks/set-state-in-effect
    void fetchUnread()
    const id = window.setInterval(fetchUnread, POLL_INTERVAL_MS)
    return () => window.clearInterval(id)
  }, [fetchUnread])

  useEffect(() => {
    // eslint-disable-next-line react-hooks/set-state-in-effect
    if (open) void fetchList()
  }, [open, fetchList])

  async function handleMarkAsRead(id: string) {
    try {
      await notificationService.markAsRead(id)
      setItems((prev) => prev.map((n) => (n.id === id ? { ...n, isRead: true } : n)))
      setUnread((n) => Math.max(0, n - 1))
    } catch (err) {
      toast.error(extractApiError(err, 'Đánh dấu thất bại'))
    }
  }

  async function handleMarkAll() {
    try {
      const updated = await notificationService.markAllAsRead()
      setItems((prev) => prev.map((n) => ({ ...n, isRead: true })))
      setUnread(0)
      toast.success(`Đã đánh dấu ${updated} thông báo là đã đọc`)
    } catch (err) {
      toast.error(extractApiError(err, 'Thao tác thất bại'))
    }
  }

  async function handleDelete(id: string) {
    try {
      await notificationService.deleteNotification(id)
      const wasUnread = items.find((n) => n.id === id)?.isRead === false
      setItems((prev) => prev.filter((n) => n.id !== id))
      if (wasUnread) setUnread((n) => Math.max(0, n - 1))
    } catch (err) {
      toast.error(extractApiError(err, 'Xoá thất bại'))
    }
  }

  return (
    <DropdownMenu open={open} onOpenChange={setOpen}>
      <DropdownMenuTrigger asChild>
        <Button variant="ghost" size="icon" className="relative" aria-label="Thông báo">
          <Bell className="size-5" />
          {unread > 0 && (
            <Badge
              variant="destructive"
              className="absolute -right-1 -top-1 h-5 min-w-5 justify-center rounded-full px-1 text-[10px]"
            >
              {unread > 99 ? '99+' : unread}
            </Badge>
          )}
        </Button>
      </DropdownMenuTrigger>
      <DropdownMenuContent align="end" className="w-80">
        <DropdownMenuLabel className="flex items-center justify-between">
          <span>Thông báo</span>
          {items.some((n) => !n.isRead) && (
            <Button variant="ghost" size="sm" className="h-7 text-xs" onClick={handleMarkAll}>
              <CheckCheck className="mr-1 size-3" />
              Đọc hết
            </Button>
          )}
        </DropdownMenuLabel>
        <DropdownMenuSeparator />
        <div className="max-h-96 overflow-y-auto">
          {loading ? (
            <div className="text-muted-foreground p-4 text-center text-sm">Đang tải…</div>
          ) : items.length === 0 ? (
            <div className="text-muted-foreground p-6 text-center text-sm">Không có thông báo</div>
          ) : (
            items.map((n) => (
              <div
                key={n.id}
                className={`group flex gap-2 border-b px-3 py-2 text-sm last:border-0 ${
                  n.isRead ? '' : 'bg-accent/40'
                }`}
              >
                <div className="min-w-0 flex-1">
                  {n.actionUrl ? (
                    <Link
                      href={n.actionUrl}
                      onClick={() => {
                        if (!n.isRead) void handleMarkAsRead(n.id)
                        setOpen(false)
                      }}
                      className="font-medium hover:underline"
                    >
                      {n.title}
                    </Link>
                  ) : (
                    <p className="font-medium">{n.title}</p>
                  )}
                  <p className="text-muted-foreground mt-0.5 line-clamp-2 text-xs">{n.message}</p>
                  <p className="text-muted-foreground mt-1 text-[10px]">{timeAgo(n.sentAt)}</p>
                </div>
                <div className="flex flex-col gap-1 opacity-0 transition-opacity group-hover:opacity-100">
                  {!n.isRead && (
                    <Button
                      variant="ghost"
                      size="icon"
                      className="size-6"
                      title="Đánh dấu đã đọc"
                      onClick={() => handleMarkAsRead(n.id)}
                    >
                      <CheckCheck className="size-3" />
                    </Button>
                  )}
                  <Button
                    variant="ghost"
                    size="icon"
                    className="size-6 text-destructive"
                    title="Xoá"
                    onClick={() => handleDelete(n.id)}
                  >
                    <Trash2 className="size-3" />
                  </Button>
                </div>
              </div>
            ))
          )}
        </div>
      </DropdownMenuContent>
    </DropdownMenu>
  )
}
