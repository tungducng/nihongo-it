/* eslint-disable react/prop-types */
/* Admin dashboard. Mirrors frontend-admin/src/app/(admin)/DashboardClient.tsx —
   two grids of stat cards + recent activity feed. */

function AdminDashboard() {
  const summary = [
    { key: 'users',  label: 'Tổng người dùng', value: '2,847', icon: <Icon.Users size={20}/>,  tone: 'primary' },
    { key: 'vocab',  label: 'Từ vựng',         value: '1,204', icon: <Icon.Book  size={20}/>,  tone: 'success' },
    { key: 'cat',    label: 'Danh mục',        value: '28',    icon: <Icon.Folder size={20}/>, tone: 'warn'    },
    { key: 'topic',  label: 'Chủ đề',          value: '142',   icon: <Icon.Tag size={20}/>,    tone: 'rose'    },
  ];
  const today = [
    { key: 'new',    label: 'Tài khoản mới',   value: '34',  icon: <Icon.UserPlus size={20}/>, tone: 'primary' },
    { key: 'active', label: 'Đang hoạt động',  value: '512', icon: <Icon.Activity size={20}/>, tone: 'success' },
    { key: 'search', label: 'Lượt tra cứu',    value: '1,829', icon: <Icon.Search size={20}/>, tone: 'warn'    },
    { key: 'flash',  label: 'Lượt học flashcard', value: '4,103', icon: <Icon.TrendUp size={20}/>, tone: 'rose' },
  ];
  const activity = [
    { user: 'Nguyễn Thị Lan',  action: 'Hoàn thành 24 thẻ flashcard JLPT N3',    t: '14:22 26/05/2026' },
    { user: 'Trần Văn Minh',   action: 'Đã đăng ký tài khoản mới',                t: '13:58 26/05/2026' },
    { user: 'Phạm Hoàng Anh',  action: 'Luyện phát âm hội thoại "Báo cáo tiến độ"', t: '13:41 26/05/2026' },
    { user: 'Lê Quốc Bảo',     action: 'Lưu từ vựng 「仕様書」(N1)',                 t: '13:24 26/05/2026' },
    { user: 'Hoàng Thu Hà',    action: 'Hoàn thành bài hội thoại Bài 7',           t: '13:02 26/05/2026' },
    { user: 'Vũ Đăng Khoa',    action: 'Đổi mật khẩu',                              t: '12:47 26/05/2026' },
  ];

  return (
    <div>
      <div className="adm-page-head">
        <div>
          <h1 className="adm-h1">Dashboard</h1>
          <div className="sub">Tổng quan hệ thống Nihongo IT</div>
        </div>
        <Button variant="outline"><Icon.Calendar size={14}/> Tuần này</Button>
      </div>

      <div style={{ marginBottom: 24 }}>
        <div className="adm-section-head"><h2>Tổng quan</h2></div>
        <div className="nh-grid nh-grid-4">
          {summary.map(s => <StatCard key={s.key} {...s}/>)}
        </div>
      </div>

      <div style={{ marginBottom: 24 }}>
        <div className="adm-section-head"><h2>Hôm nay</h2></div>
        <div className="nh-grid nh-grid-4">
          {today.map(s => <StatCard key={s.key} {...s}/>)}
        </div>
      </div>

      <div>
        <div className="adm-section-head">
          <h2>Hoạt động gần đây</h2>
          <Button variant="ghost" size="sm">Xem tất cả <Icon.ChevronRight size={14}/></Button>
        </div>
        <Card>
          {activity.map((a, i) => (
            <div key={i} className="adm-activity-row">
              <div className="left">
                <div className="n">{a.user}</div>
                <div className="a">{a.action}</div>
              </div>
              <div className="t">{a.t}</div>
            </div>
          ))}
        </Card>
      </div>
    </div>
  );
}

function StatCard({ label, value, icon, tone }) {
  const colour = ({ primary: 'var(--ai-500)', success: 'var(--success)', warn: 'var(--warning)', rose: 'var(--jlpt-n1)' })[tone];
  return (
    <Card>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
        <div>
          <div style={{ font: '500 10px var(--font-sans)', textTransform: 'uppercase', letterSpacing: '0.04em', color: 'var(--washi-500)' }}>{label}</div>
          <div style={{ font: '600 26px var(--font-sans)', color: 'var(--washi-900)', marginTop: 6, fontVariantNumeric: 'tabular-nums', letterSpacing: '-0.01em' }}>{value}</div>
        </div>
        <div style={{ color: colour }}>{icon}</div>
      </div>
    </Card>
  );
}

window.AdminDashboard = AdminDashboard;
