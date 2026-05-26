/* eslint-disable react/prop-types */
/* Header — sticky top nav. Mirrors frontend-user/src/components/layout/Header.tsx
   structurally but uses our cosmetic primitives. */

function Header({ active, onNavigate, user, onLogout }) {
  const [menuOpen, setMenuOpen] = React.useState(false);
  const NAV = [
    { id: 'vocabulary',   label: 'Từ vựng' },
    { id: 'conversation', label: 'Hội thoại' },
    { id: 'flashcards',   label: 'Flashcards' },
    { id: 'statistics',   label: 'Thống kê' },
  ];

  return (
    <header className="nh-header">
      <a className="nh-brand" onClick={() => onNavigate('home')}>
        <div className="nh-brand-mark">日</div>
        <div className="nh-brand-word">Nihongo IT</div>
      </a>

      <nav className="nh-nav">
        {NAV.map(n => (
          <a key={n.id}
             className={active === n.id ? 'active' : ''}
             onClick={() => onNavigate(n.id)}>
            {n.label}
          </a>
        ))}
      </nav>

      <div className="nh-header-right">
        <IconButton title="Thông báo">
          <Icon.Bell size={18}/>
          <span className="nh-notif-dot" />
        </IconButton>
        <div style={{ position: 'relative' }}>
          <IconButton onClick={() => setMenuOpen(o => !o)} style={{ padding: 0 }}>
            <Avatar initials={user.initials} size={32}/>
          </IconButton>
          {menuOpen && (
            <div className="nh-popmenu" onMouseLeave={() => setMenuOpen(false)}>
              <div className="nh-popmenu-label">
                <div className="n">{user.name}</div>
                <div className="e">{user.email}</div>
              </div>
              <Separator />
              <div className="nh-popmenu-item" onClick={() => { setMenuOpen(false); onNavigate('profile'); }}>
                <Icon.User size={14}/> Hồ sơ
              </div>
              <div className="nh-popmenu-item">
                <Icon.Settings size={14}/> Cài đặt
              </div>
              <div className="nh-popmenu-item">
                <Icon.Lock size={14}/> Đổi mật khẩu
              </div>
              <Separator />
              <div className="nh-popmenu-item nh-popmenu-danger" onClick={() => { setMenuOpen(false); onLogout(); }}>
                <Icon.LogOut size={14}/> Đăng xuất
              </div>
            </div>
          )}
        </div>
      </div>

      <style>{`
        .nh-popmenu {
          position: absolute; right: 0; top: 40px; min-width: 220px;
          background: var(--popover); border: 1px solid var(--border); border-radius: 10px;
          box-shadow: var(--shadow-lg); padding: 6px; z-index: 50;
        }
        .nh-popmenu-label { padding: 8px 10px; display: flex; flex-direction: column; gap: 1px; }
        .nh-popmenu-label .n { font: 500 13px var(--font-sans); color: var(--washi-800); }
        .nh-popmenu-label .e { font: 400 11px var(--font-sans); color: var(--washi-500); }
        .nh-popmenu-item {
          display: flex; align-items: center; gap: 10px; padding: 8px 10px; border-radius: 6px;
          font: 400 13px var(--font-sans); color: var(--washi-800); cursor: pointer;
        }
        .nh-popmenu-item svg { color: var(--washi-500); }
        .nh-popmenu-item:hover { background: var(--washi-100); }
        .nh-popmenu-danger { color: var(--danger); }
        .nh-popmenu-danger svg { color: var(--danger); }
      `}</style>
    </header>
  );
}

window.Header = Header;
