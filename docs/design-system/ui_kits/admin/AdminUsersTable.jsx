/* eslint-disable react/prop-types */
/* Admin: Users table. Mirrors frontend-admin/src/app/(admin)/users. */

const ADMIN_USERS = [
  { id: 1, name: 'Nguyễn Thị Lan',   email: 'lan.nguyen@nhitech.vn',     role: 'user',  jlpt: 'N3', joined: '2026-02-14', status: 'on'  },
  { id: 2, name: 'Trần Văn Minh',    email: 'minh.tv@brse.io',            role: 'user',  jlpt: 'N4', joined: '2026-05-26', status: 'on'  },
  { id: 3, name: 'Phạm Hoàng Anh',   email: 'h.anh@gmail.com',            role: 'user',  jlpt: 'N2', joined: '2025-11-08', status: 'on'  },
  { id: 4, name: 'Lê Quốc Bảo',      email: 'bao.le@nihongo-it.vn',       role: 'admin', jlpt: 'N1', joined: '2025-08-21', status: 'on'  },
  { id: 5, name: 'Hoàng Thu Hà',     email: 'ha.hoang@vti.com.vn',        role: 'user',  jlpt: 'N3', joined: '2026-01-30', status: 'on'  },
  { id: 6, name: 'Vũ Đăng Khoa',     email: 'khoa.vu@fpt-software.com',   role: 'user',  jlpt: 'N4', joined: '2026-03-12', status: 'off' },
];

function AdminUsersTable() {
  const [keyword, setKeyword] = React.useState('');
  const filtered = ADMIN_USERS.filter(u =>
    !keyword || u.name.toLowerCase().includes(keyword.toLowerCase()) || u.email.toLowerCase().includes(keyword.toLowerCase())
  );
  function initials(name) {
    return name.split(' ').map(w => w[0]).slice(0, 2).join('').toUpperCase();
  }
  return (
    <div>
      <div className="adm-page-head">
        <div>
          <h1 className="adm-h1">Người dùng</h1>
          <div className="sub">{ADMIN_USERS.length} tài khoản · sắp xếp theo ngày tham gia gần nhất</div>
        </div>
        <Button><Icon.UserPlus size={14}/> Tạo người dùng</Button>
      </div>

      <div className="adm-table-card">
        <div className="adm-table-toolbar">
          <div className="nh-input-icon">
            <Icon.Search size={14}/>
            <Input value={keyword} onChange={e => setKeyword(e.target.value)} placeholder="Tìm theo tên hoặc email…"/>
          </div>
          <Button variant="outline" size="sm"><Icon.Filter size={14}/> Bộ lọc</Button>
        </div>

        <table className="adm-table">
          <thead>
            <tr>
              <th>Người dùng</th>
              <th style={{ width: 90 }}>Vai trò</th>
              <th style={{ width: 80 }}>JLPT</th>
              <th style={{ width: 130 }}>Ngày tham gia</th>
              <th style={{ width: 130 }}>Trạng thái</th>
              <th style={{ width: 80 }}></th>
            </tr>
          </thead>
          <tbody>
            {filtered.map(u => (
              <tr key={u.id} className="row">
                <td>
                  <div style={{ display: 'flex', alignItems: 'center', gap: 10 }}>
                    <Avatar initials={initials(u.name)} size={32}/>
                    <div>
                      <div style={{ font: '500 13px var(--font-sans)', color: 'var(--washi-900)' }}>{u.name}</div>
                      <div style={{ font: '400 12px var(--font-sans)', color: 'var(--washi-500)' }}>{u.email}</div>
                    </div>
                  </div>
                </td>
                <td>
                  {u.role === 'admin'
                    ? <Badge tone="neutral" style={{ background: 'var(--ai-500)', color: 'var(--washi-50)' }}>admin</Badge>
                    : <Badge tone="outline">user</Badge>}
                </td>
                <td><JlptBadge level={u.jlpt}/></td>
                <td className="num" style={{ color: 'var(--washi-700)' }}>{u.joined}</td>
                <td>
                  <span className={cx('adm-status', u.status === 'off' && 'off')}>
                    <span className="dot"/>
                    {u.status === 'on' ? 'Hoạt động' : 'Đã khoá'}
                  </span>
                </td>
                <td>
                  <div className="adm-row-actions">
                    <IconButton title="Sửa" style={{ width: 30, height: 30 }}><Icon.Pencil size={14}/></IconButton>
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

window.AdminUsersTable = AdminUsersTable;
