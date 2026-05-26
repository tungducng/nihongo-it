/* eslint-disable react/prop-types */
/* Admin: Vocabulary table — searchable, filterable list of all vocabulary,
   with row actions (edit, delete). Mirrors frontend-admin/src/app/(admin)/vocabulary. */

const ADMIN_VOCAB = [
  { id: 1, term: '会議',     pron: 'かいぎ',          mean: 'cuộc họp',                          jlpt: 'N3', topic: 'Công sở',  saves: 412, status: 'on' },
  { id: 2, term: '仕様書',   pron: 'しようしょ',      mean: 'tài liệu đặc tả',                   jlpt: 'N1', topic: 'Lập trình', saves: 387, status: 'on' },
  { id: 3, term: '納期',     pron: 'のうき',          mean: 'thời hạn giao hàng',                jlpt: 'N2', topic: 'Dự án',     saves: 298, status: 'on' },
  { id: 4, term: '不具合',   pron: 'ふぐあい',        mean: 'lỗi / bug',                         jlpt: 'N2', topic: 'QA',        saves: 276, status: 'on' },
  { id: 5, term: '実装',     pron: 'じっそう',        mean: 'triển khai / implement',            jlpt: 'N2', topic: 'Lập trình', saves: 254, status: 'on' },
  { id: 6, term: '要件定義', pron: 'ようけんていぎ',  mean: 'xác định yêu cầu',                  jlpt: 'N1', topic: 'Dự án',     saves: 231, status: 'on' },
  { id: 7, term: '検収',     pron: 'けんしゅう',      mean: 'nghiệm thu',                        jlpt: 'N1', topic: 'Dự án',     saves: 198, status: 'off' },
  { id: 8, term: '画面',     pron: 'がめん',          mean: 'màn hình',                          jlpt: 'N4', topic: 'UI/UX',     saves: 174, status: 'on' },
  { id: 9, term: '本番環境', pron: 'ほんばんかんきょう', mean: 'môi trường production',          jlpt: 'N2', topic: 'DevOps',    saves: 162, status: 'on' },
];

function AdminVocabularyTable() {
  const [keyword, setKeyword] = React.useState('');
  const [jlpt, setJlpt] = React.useState('Tất cả');
  const filtered = ADMIN_VOCAB.filter(v =>
    (jlpt === 'Tất cả' || v.jlpt === jlpt) &&
    (!keyword || v.term.includes(keyword) || v.pron.includes(keyword) || v.mean.toLowerCase().includes(keyword.toLowerCase()))
  );

  return (
    <div>
      <div className="adm-page-head">
        <div>
          <h1 className="adm-h1">Từ vựng</h1>
          <div className="sub">Quản lý danh sách từ vựng IT trong hệ thống — tổng {ADMIN_VOCAB.length} từ.</div>
        </div>
        <Button><Icon.Plus size={14}/> Thêm từ vựng</Button>
      </div>

      <div className="adm-table-card">
        <div className="adm-table-toolbar">
          <div className="nh-input-icon">
            <Icon.Search size={14}/>
            <Input value={keyword} onChange={e => setKeyword(e.target.value)} placeholder="Tìm theo từ, cách đọc, nghĩa…"/>
          </div>
          <select className="nh-input" style={{ width: 140 }} value={jlpt} onChange={e => setJlpt(e.target.value)}>
            <option>Tất cả</option><option>N5</option><option>N4</option><option>N3</option><option>N2</option><option>N1</option>
          </select>
          <Button variant="outline" size="sm"><Icon.Filter size={14}/> Bộ lọc</Button>
        </div>

        <table className="adm-table">
          <thead>
            <tr>
              <th style={{ width: 64 }}>ID</th>
              <th style={{ width: 160 }}>Từ</th>
              <th>Nghĩa</th>
              <th style={{ width: 90 }}>JLPT</th>
              <th>Chủ đề</th>
              <th style={{ width: 90 }} className="num">Lượt lưu</th>
              <th style={{ width: 100 }}>Trạng thái</th>
              <th style={{ width: 110 }}></th>
            </tr>
          </thead>
          <tbody>
            {filtered.map(v => (
              <tr key={v.id} className="row">
                <td className="num" style={{ color: 'var(--washi-500)' }}>#{v.id.toString().padStart(4, '0')}</td>
                <td>
                  <div className="jp" style={{ font: '600 16px var(--font-jp)', color: 'var(--washi-900)' }}>{v.term}</div>
                  <div className="jp" style={{ font: '400 12px var(--font-jp)', color: 'var(--washi-500)' }}>{v.pron}</div>
                </td>
                <td style={{ color: 'var(--washi-700)' }}>{v.mean}</td>
                <td><JlptBadge level={v.jlpt}/></td>
                <td><Badge tone="outline">{v.topic}</Badge></td>
                <td className="num" style={{ color: 'var(--washi-700)' }}>{v.saves}</td>
                <td>
                  <span className={cx('adm-status', v.status === 'off' && 'off')}>
                    <span className="dot"/>
                    {v.status === 'on' ? 'Đang hiển thị' : 'Đã ẩn'}
                  </span>
                </td>
                <td>
                  <div className="adm-row-actions">
                    <IconButton title="Sửa" style={{ width: 30, height: 30 }}><Icon.Pencil size={14}/></IconButton>
                    <IconButton title="Xoá" style={{ width: 30, height: 30, color: 'var(--danger)' }}><Icon.Trash size={14}/></IconButton>
                    <IconButton title="Thêm" style={{ width: 30, height: 30 }}><Icon.More size={14}/></IconButton>
                  </div>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}

window.AdminVocabularyTable = AdminVocabularyTable;
