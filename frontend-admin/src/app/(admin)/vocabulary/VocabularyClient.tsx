'use client'

import { useCallback, useEffect, useMemo, useState } from 'react'
import { MoreHorizontal, Pencil, Plus, Search, Trash2 } from 'lucide-react'
import type { ColumnDef } from '@tanstack/react-table'
import topicService from '@/services/topic.service'
import vocabularyService from '@/services/vocabulary.service'
import { useDebounce } from '@/hooks/useDebounce'
import { useAppToast } from '@/hooks/useAppToast'
import { useConfirm } from '@/hooks/useConfirm'
import { extractApiError } from '@/types/common.types'
import type { JlptLevel } from '@/types/common.types'
import type { Topic, VocabularyItem } from '@/types/learning.types'
import { DataTable } from '@/components/data-table/DataTable'
import { Pagination } from '@/components/common/Pagination'
import { Loader } from '@/components/common/Loader'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { Badge } from '@/components/ui/badge'
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select'
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from '@/components/ui/dropdown-menu'
import { VocabularyFormDialog } from './VocabularyFormDialog'

const ALL = '__all__'
const JLPT_LEVELS: JlptLevel[] = ['N1', 'N2', 'N3', 'N4', 'N5']
const PAGE_SIZE = 20

export function VocabularyClient() {
  const toast = useAppToast()
  const confirm = useConfirm()

  const [topics, setTopics] = useState<Topic[]>([])
  const [keyword, setKeyword] = useState('')
  const debouncedKeyword = useDebounce(keyword, 400)
  const [topicId, setTopicId] = useState<string | null>(null)
  const [jlptLevel, setJlptLevel] = useState<JlptLevel | null>(null)
  const [page, setPage] = useState(0)

  const [items, setItems] = useState<VocabularyItem[]>([])
  const [totalPages, setTotalPages] = useState(0)
  const [totalElements, setTotalElements] = useState(0)
  const [loading, setLoading] = useState(true)

  const [dialogOpen, setDialogOpen] = useState(false)
  const [editing, setEditing] = useState<VocabularyItem | null>(null)

  useEffect(() => {
    let cancelled = false
    topicService
      .getAll()
      .then((data) => {
        if (!cancelled) setTopics(data)
      })
      .catch(() => {
        // topic dropdown will be empty, non-fatal
      })
    return () => {
      cancelled = true
    }
  }, [])

  const handleKeywordChange = (v: string) => {
    setPage(0)
    setKeyword(v)
  }
  const handleTopicChange = (v: string | null) => {
    setPage(0)
    setTopicId(v)
  }
  const handleJlptChange = (v: JlptLevel | null) => {
    setPage(0)
    setJlptLevel(v)
  }

  const fetchItems = useCallback(async () => {
    setLoading(true)
    try {
      const res = debouncedKeyword
        ? await vocabularyService.search(debouncedKeyword, {
            topicId: topicId ?? undefined,
            jlptLevel: jlptLevel ?? undefined,
            page,
            size: PAGE_SIZE,
          })
        : await vocabularyService.list({
            page,
            size: PAGE_SIZE,
            topicId: topicId ?? undefined,
            jlptLevel: jlptLevel ?? undefined,
          })
      setItems(res.content ?? [])
      setTotalPages(res.totalPages ?? 0)
      setTotalElements(res.totalElements ?? 0)
    } catch (err) {
      toast.error(extractApiError(err, 'Không tải được từ vựng'))
      setItems([])
      setTotalPages(0)
    } finally {
      setLoading(false)
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [debouncedKeyword, topicId, jlptLevel, page])

  useEffect(() => {
    // eslint-disable-next-line react-hooks/set-state-in-effect
    void fetchItems()
  }, [fetchItems])

  const handleDelete = useCallback(
    async (item: VocabularyItem) => {
      const ok = await confirm({
        title: 'Xoá từ vựng',
        message: `"${item.term}" sẽ bị xoá vĩnh viễn.`,
        confirmText: 'Xoá',
        variant: 'destructive',
      })
      if (!ok) return
      try {
        await vocabularyService.delete(item.vocabId)
        toast.success('Đã xoá từ vựng')
        void fetchItems()
      } catch (err) {
        toast.error(extractApiError(err, 'Xoá thất bại'))
      }
    },
    [confirm, fetchItems, toast],
  )

  const handleEdit = useCallback((item: VocabularyItem) => {
    setEditing(item)
    setDialogOpen(true)
  }, [])

  const columns = useMemo<ColumnDef<VocabularyItem>[]>(
    () => [
      {
        accessorKey: 'term',
        header: 'Từ',
        cell: ({ row }) => (
          <div>
            <p className="font-jp text-[16px] font-semibold leading-tight text-[color:var(--washi-900)]">
              {row.original.term}
            </p>
            {row.original.pronunciation && (
              <p className="font-jp text-muted-foreground text-[12px]">
                {row.original.pronunciation}
              </p>
            )}
          </div>
        ),
      },
      {
        accessorKey: 'meaning',
        header: 'Nghĩa',
        cell: ({ row }) => (
          <span className="line-clamp-2 text-[13px] text-[color:var(--washi-800)]">
            {row.original.meaning}
          </span>
        ),
      },
      {
        accessorKey: 'topicName',
        header: 'Chủ đề',
        cell: ({ row }) =>
          row.original.topicName ? (
            <Badge variant="outline" className="text-[11px]">
              {row.original.topicName}
            </Badge>
          ) : (
            <span className="text-muted-foreground">—</span>
          ),
      },
      {
        accessorKey: 'jlptLevel',
        header: 'JLPT',
        cell: ({ row }) => {
          const lvl = row.original.jlptLevel
          const cls =
            lvl === 'N5'
              ? 'jlpt-n5'
              : lvl === 'N4'
                ? 'jlpt-n4'
                : lvl === 'N3'
                  ? 'jlpt-n3'
                  : lvl === 'N2'
                    ? 'jlpt-n2'
                    : 'jlpt-n1'
          return (
            <span
              className={`${cls} inline-flex h-[22px] items-center rounded-full px-2.5 text-[11px] font-semibold`}
            >
              {lvl}
            </span>
          )
        },
      },
      {
        id: 'actions',
        header: '',
        enableSorting: false,
        cell: ({ row }) => (
          <DropdownMenu>
            <DropdownMenuTrigger asChild>
              <Button variant="ghost" size="icon" className="h-8 w-8">
                <MoreHorizontal className="size-4" />
              </Button>
            </DropdownMenuTrigger>
            <DropdownMenuContent align="end">
              <DropdownMenuItem onClick={() => handleEdit(row.original)}>
                <Pencil className="mr-2 size-4" />
                Sửa
              </DropdownMenuItem>
              <DropdownMenuSeparator />
              <DropdownMenuItem
                onClick={() => handleDelete(row.original)}
                className="text-destructive"
              >
                <Trash2 className="mr-2 size-4" />
                Xoá
              </DropdownMenuItem>
            </DropdownMenuContent>
          </DropdownMenu>
        ),
      },
    ],
    [handleDelete, handleEdit],
  )

  return (
    <div className="space-y-4">
      <div className="flex items-end justify-between gap-3">
        <div>
          <h1 className="text-[24px] font-bold tracking-tight text-[color:var(--washi-900)]">
            Từ vựng
          </h1>
          <p className="text-muted-foreground mt-1 text-[13px]">
            {totalElements > 0
              ? `${totalElements} từ vựng · quản lý từ vựng IT trong hệ thống`
              : 'Chưa có từ vựng'}
          </p>
        </div>
        <Button
          onClick={() => {
            setEditing(null)
            setDialogOpen(true)
          }}
        >
          <Plus className="mr-1 size-4" />
          Tạo mới
        </Button>
      </div>

      <div className="grid gap-3 sm:grid-cols-[1fr_220px_140px]">
        <div className="space-y-1.5">
          <Label htmlFor="search">Tìm kiếm</Label>
          <div className="relative">
            <Search className="text-muted-foreground absolute left-2.5 top-1/2 size-4 -translate-y-1/2" />
            <Input
              id="search"
              value={keyword}
              onChange={(e) => handleKeywordChange(e.target.value)}
              placeholder="Tìm theo từ hoặc nghĩa..."
              className="pl-8"
            />
          </div>
        </div>

        <div className="space-y-1.5">
          <Label htmlFor="topic">Chủ đề</Label>
          <Select
            value={topicId ?? ALL}
            onValueChange={(v) => handleTopicChange(v === ALL ? null : v)}
          >
            <SelectTrigger id="topic">
              <SelectValue />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value={ALL}>Tất cả</SelectItem>
              {topics.map((t) => (
                <SelectItem key={t.topicId} value={t.topicId}>
                  {t.name}
                </SelectItem>
              ))}
            </SelectContent>
          </Select>
        </div>

        <div className="space-y-1.5">
          <Label htmlFor="jlpt">JLPT</Label>
          <Select
            value={jlptLevel ?? ALL}
            onValueChange={(v) => handleJlptChange(v === ALL ? null : (v as JlptLevel))}
          >
            <SelectTrigger id="jlpt">
              <SelectValue />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value={ALL}>Tất cả</SelectItem>
              {JLPT_LEVELS.map((lvl) => (
                <SelectItem key={lvl} value={lvl}>
                  {lvl}
                </SelectItem>
              ))}
            </SelectContent>
          </Select>
        </div>
      </div>

      {loading ? (
        <Loader label="Đang tải..." />
      ) : (
        <DataTable columns={columns} data={items} />
      )}

      <Pagination page={page} totalPages={totalPages} onChange={setPage} />

      <VocabularyFormDialog
        open={dialogOpen}
        onOpenChange={setDialogOpen}
        initial={editing}
        topics={topics}
        onSaved={() => void fetchItems()}
      />
    </div>
  )
}
