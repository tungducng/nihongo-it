/* eslint-disable react/prop-types */
/* VocabularyScreen — keyword + JLPT + topic filter, grid of cards.
   Mirrors frontend-user/src/app/(app)/vocabulary/VocabularyBrowser.tsx. */

const JLPT_LEVELS = ['Tất cả', 'N5', 'N4', 'N3', 'N2', 'N1'];

function VocabularyCard({ item, onToggleSave, onOpen }) {
  return (
    <Card interactive onClick={() => onOpen?.(item.id)}>
      <div className="nh-vcard-head">
        <div style={{ minWidth: 0, flex: 1 }}>
          <div className="nh-vcard-term">{item.term}</div>
          <div className="nh-vcard-pron">{item.pron}</div>
        </div>
        <button
          className={cx('nh-bookmark', item.saved && 'saved')}
          onClick={(e) => { e.stopPropagation(); onToggleSave(item.id); }}
          aria-label={item.saved ? 'Bỏ lưu' : 'Lưu từ vựng'}>
          {item.saved ? <Icon.BookmarkFill size={16} /> : <Icon.Bookmark size={16} />}
        </button>
      </div>
      <div className="nh-vcard-meaning">{item.meaning}</div>
      <div className="nh-vcard-chips">
        <JlptBadge level={item.jlpt} />
        <Badge tone="outline">{item.topic}</Badge>
      </div>
    </Card>
  );
}

function VocabularyScreen({ onOpenDetail }) {
  const [keyword, setKeyword] = React.useState('');
  const [jlpt, setJlpt] = React.useState('Tất cả');
  const [topic, setTopic] = React.useState('Tất cả');
  const [items, setItems] = React.useState(window.MOCK.VOCAB);

  const filtered = items.filter(it =>
    (jlpt === 'Tất cả' || it.jlpt === jlpt) &&
    (topic === 'Tất cả' || it.topic === topic) &&
    (!keyword || it.term.includes(keyword) || it.pron.includes(keyword) || it.meaning.toLowerCase().includes(keyword.toLowerCase()))
  );

  function toggleSave(id) {
    setItems(prev => prev.map(it => it.id === id ? { ...it, saved: !it.saved } : it));
  }

  return (
    <div>
      <div className="nh-page-head">
        <h1>Từ vựng</h1>
        <div className="sub">{filtered.length > 0 ? `${filtered.length} từ vựng` : 'Khám phá từ vựng IT theo cấp độ JLPT'}</div>
      </div>

      <div className="nh-filter-bar">
        <div className="nh-field">
          <Label>Tìm kiếm</Label>
          <div className="nh-input-icon">
            <Icon.Search size={14} />
            <Input value={keyword} onChange={e => setKeyword(e.target.value)} placeholder="Tìm theo từ hoặc nghĩa…" />
          </div>
        </div>
        <div className="nh-field">
          <Label>JLPT</Label>
          <select className="nh-input" value={jlpt} onChange={e => setJlpt(e.target.value)}>
            {JLPT_LEVELS.map(l => <option key={l}>{l}</option>)}
          </select>
        </div>
        <div className="nh-field">
          <Label>Chủ đề</Label>
          <select className="nh-input" value={topic} onChange={e => setTopic(e.target.value)}>
            {window.MOCK.TOPICS.map(t => <option key={t}>{t}</option>)}
          </select>
        </div>
      </div>

      {filtered.length === 0 ? (
        <p style={{ textAlign: 'center', color: 'var(--washi-500)', padding: '40px 0', font: '400 13px var(--font-sans)' }}>
          Không tìm thấy từ vựng phù hợp.
        </p>
      ) : (
        <div className="nh-grid nh-grid-4">
          {filtered.map(item => (
            <VocabularyCard key={item.id} item={item} onToggleSave={toggleSave} onOpen={onOpenDetail}/>
          ))}
        </div>
      )}
    </div>
  );
}

window.VocabularyScreen = VocabularyScreen;
window.VocabularyCard = VocabularyCard;
