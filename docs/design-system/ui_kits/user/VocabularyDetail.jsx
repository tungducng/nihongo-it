/* eslint-disable react/prop-types */
/* VocabularyDetail — single-word view. Shows term with furigana, meaning,
   examples, audio button, save toggle. Mirrors VocabularyDetailClient.tsx. */

function VocabularyDetail({ item, onBack, onToggleSave }) {
  if (!item) return null;
  return (
    <div>
      <Button variant="ghost" size="sm" onClick={onBack} style={{ marginBottom: 18 }}>
        <Icon.ArrowLeft size={14}/> Trở lại
      </Button>

      <Card style={{ padding: '32px 36px' }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', gap: 16 }}>
          <div style={{ flex: 1 }}>
            <div style={{ fontFamily: 'var(--font-jp-serif)', fontWeight: 500, fontSize: 56, color: 'var(--washi-900)', lineHeight: 1.05 }}>
              <ruby>{item.term}<rt style={{ fontSize: '0.42em', color: 'var(--washi-500)', fontWeight: 400 }}>{item.pron}</rt></ruby>
            </div>
            <div style={{ display: 'flex', gap: 8, marginTop: 14, alignItems: 'center' }}>
              <JlptBadge level={item.jlpt} />
              <Badge tone="outline">{item.topic}</Badge>
              <IconButton title="Phát âm" style={{ width: 32, height: 32, marginLeft: 4 }}>
                <Icon.Volume size={16}/>
              </IconButton>
            </div>
          </div>
          <button
            className={cx('nh-bookmark', item.saved && 'saved')}
            onClick={() => onToggleSave(item.id)}
            style={{ padding: 8 }}>
            {item.saved ? <Icon.BookmarkFill size={22}/> : <Icon.Bookmark size={22}/>}
          </button>
        </div>

        <Separator style={{ margin: '28px 0' }} />

        <div style={{ display: 'grid', gap: 24 }}>
          <Section title="Nghĩa">
            <p style={{ font: '400 15px/1.6 var(--font-sans)', color: 'var(--washi-800)' }}>{item.meaning}</p>
          </Section>

          <Section title="Ví dụ">
            <ExampleRow
              jp={<>来週の<strong>{item.term}</strong>は火曜日です。</>}
              vi={`Cuộc họp tuần sau là vào thứ Ba.`}
            />
            <ExampleRow
              jp={<><strong>{item.term}</strong>の議事録を共有してください。</>}
              vi={`Vui lòng chia sẻ biên bản cuộc họp.`}
            />
          </Section>

          <Section title="Trợ lý AI" trailing={<Icon.Sparkles size={14}/>}>
            <Card style={{ background: 'var(--ai-50)', borderColor: 'oklch(from var(--ai-500) l c h / 30%)' }}>
              <p style={{ font: '400 13.5px/1.55 var(--font-sans)', color: 'var(--washi-800)' }}>
                <strong>{item.term}</strong> ({item.pron}) là từ thường gặp trong môi trường công sở Nhật. Có thể kết hợp với
                <span className="jp"> 会議室 (kaigishitsu — phòng họp)</span> hoặc <span className="jp">会議資料 (kaigi shiryō — tài liệu họp)</span>.
              </p>
            </Card>
          </Section>
        </div>
      </Card>
    </div>
  );
}

function Section({ title, trailing, children }) {
  return (
    <div>
      <div style={{ display: 'flex', alignItems: 'center', gap: 6, font: '600 11px var(--font-sans)', textTransform: 'uppercase', letterSpacing: '0.08em', color: 'var(--washi-500)', marginBottom: 10 }}>
        {title}{trailing && <span style={{ color: 'var(--shu-500)' }}>{trailing}</span>}
      </div>
      <div>{children}</div>
    </div>
  );
}

function ExampleRow({ jp, vi }) {
  return (
    <div style={{ padding: '12px 0', borderBottom: '1px solid var(--border)' }}>
      <div className="jp" style={{ font: '400 16px var(--font-jp)', color: 'var(--washi-800)' }}>{jp}</div>
      <div style={{ font: '400 13px var(--font-sans)', color: 'var(--washi-500)', marginTop: 4 }}>{vi}</div>
    </div>
  );
}

window.VocabularyDetail = VocabularyDetail;
