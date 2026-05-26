/* eslint-disable react/prop-types */
/* ConversationScreen — list of practice conversations. Filters by keyword
   and JLPT. Mirrors ConversationList.tsx. */

const CONV_JLPT = ['Tất cả', 'N5', 'N4', 'N3', 'N2', 'N1'];

function ConversationCard({ item, onOpen }) {
  return (
    <Card interactive onClick={() => onOpen?.(item.id)}>
      <div className="nh-ccard-top">
        <span className="ic"><Icon.Message size={16}/></span>
        <div className="nh-ccard-title">{item.title}</div>
      </div>
      <div className="nh-vcard-meaning" style={{ marginTop: 8 }}>{item.desc}</div>
      <div className="nh-vcard-chips">
        <JlptBadge level={item.jlpt} />
        <Badge tone="outline">Bài {item.unit}</Badge>
      </div>
    </Card>
  );
}

function ConversationScreen() {
  const [keyword, setKeyword] = React.useState('');
  const [jlpt, setJlpt] = React.useState('Tất cả');
  const items = window.MOCK.CONVERSATIONS;
  const filtered = items.filter(c =>
    (jlpt === 'Tất cả' || c.jlpt === jlpt) &&
    (!keyword || c.title.toLowerCase().includes(keyword.toLowerCase()))
  );

  return (
    <div>
      <div className="nh-page-head">
        <h1>Hội thoại</h1>
        <div className="sub">{filtered.length} bài hội thoại · luyện phát âm với phân tích AI</div>
      </div>

      <div className="nh-filter-bar" style={{ gridTemplateColumns: '1fr 160px' }}>
        <div className="nh-field">
          <Label>Tìm kiếm</Label>
          <div className="nh-input-icon">
            <Icon.Search size={14}/>
            <Input value={keyword} onChange={e => setKeyword(e.target.value)} placeholder="Tìm theo tiêu đề…" disabled={jlpt !== 'Tất cả'}/>
          </div>
          {jlpt !== 'Tất cả' && <div style={{ font: '400 11px var(--font-sans)', color: 'var(--washi-500)', marginTop: 4 }}>Tìm kiếm tạm khoá khi đang lọc theo JLPT.</div>}
        </div>
        <div className="nh-field">
          <Label>JLPT</Label>
          <select className="nh-input" value={jlpt} onChange={e => setJlpt(e.target.value)}>
            {CONV_JLPT.map(l => <option key={l}>{l}</option>)}
          </select>
        </div>
      </div>

      <div className="nh-grid nh-grid-3">
        {filtered.map(item => <ConversationCard key={item.id} item={item}/>)}
      </div>
    </div>
  );
}

window.ConversationScreen = ConversationScreen;
window.ConversationCard = ConversationCard;
