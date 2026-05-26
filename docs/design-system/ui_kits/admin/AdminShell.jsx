/* eslint-disable react/prop-types */
/* Admin sidebar — fixed 240px nav with sectioned items and a footer profile slot.
   Mirrors frontend-admin/src/components/layout/Sidebar.tsx. */

const ADM_NAV = [
  { id: 'dashboard',     label: 'Dashboard',     Icon: () => <Icon.Dashboard size={16}/> },
  { id: 'users',         label: 'Người dùng',    Icon: () => <Icon.Users     size={16}/> },
  { id: 'categories',    label: 'Danh mục',      Icon: () => <Icon.Folder    size={16}/> },
  { id: 'topics',        label: 'Chủ đề',        Icon: () => <Icon.Tag       size={16}/> },
  { id: 'vocabulary',    label: 'Từ vựng',       Icon: () => <Icon.Book      size={16}/> },
  { id: 'conversations', label: 'Hội thoại',     Icon: () => <Icon.Message   size={16}/> },
  { id: 'statistics',    label: 'Thống kê',      Icon: () => <Icon.Chart     size={16}/> },
];

function AdminSidebar({ active, onNavigate, user }) {
  return (
    <aside className="adm-sidebar">
      <div className="adm-sb-head">
        <div className="adm-sb-brand" onClick={() => onNavigate('dashboard')}>
          <div className="nh-brand-mark">日</div>
          <div className="nh-brand-word">Nihongo IT</div>
        </div>
        <div className="adm-sb-tag">Quản trị</div>
      </div>
      <nav className="adm-sb-nav">
        {ADM_NAV.map(({ id, label, Icon: I }) => (
          <button key={id}
                  className={cx('adm-sb-item', active === id && 'active')}
                  onClick={() => onNavigate(id)}>
            <I/>
            {label}
          </button>
        ))}
      </nav>
      <div className="adm-sb-foot">
        <Avatar initials={user.initials} size={32}/>
        <div className="who">
          <div className="n">{user.name}</div>
          <div className="e">{user.email}</div>
        </div>
      </div>
    </aside>
  );
}

function AdminTopbar({ active }) {
  const labels = Object.fromEntries(ADM_NAV.map(n => [n.id, n.label]));
  return (
    <div className="adm-topbar">
      <div className="adm-crumbs">
        <span>Quản trị</span>
        <Icon.ChevronRight size={14}/>
        <strong>{labels[active] || 'Dashboard'}</strong>
      </div>
      <div style={{ marginLeft: 'auto', display: 'flex', alignItems: 'center', gap: 6 }}>
        <IconButton title="Thông báo">
          <Icon.Bell size={18}/>
          <span className="nh-notif-dot"/>
        </IconButton>
      </div>
    </div>
  );
}

Object.assign(window, { AdminSidebar, AdminTopbar });
