/* eslint-disable react/prop-types */
/* Login screen — mirrors frontend-user/src/app/(auth)/login/LoginForm.tsx */

function LoginScreen({ onLogin }) {
  const [email, setEmail] = React.useState('hoa@nihongo-it.vn');
  const [password, setPassword] = React.useState('demopass');
  const [showError, setShowError] = React.useState(false);
  const [busy, setBusy] = React.useState(false);

  function submit(e) {
    e.preventDefault();
    if (!password || password.length < 6) { setShowError(true); return; }
    setShowError(false);
    setBusy(true);
    setTimeout(() => { setBusy(false); onLogin({ email, name: 'Hoàng Văn Anh', initials: 'HV' }); }, 700);
  }

  return (
    <div className="nh-auth-shell">
      <div className="nh-auth-card">
        <div className="nh-auth-brand">
          <div className="nh-brand-mark" style={{ width: 44, height: 44, fontSize: 24, borderRadius: 10 }}>日</div>
          <div style={{ font: '600 18px var(--font-sans)', color: 'var(--washi-900)' }}>Nihongo IT</div>
        </div>
        <Card>
          <div className="nh-auth-title">Đăng nhập</div>
          <div className="nh-auth-sub">Học tiếng Nhật chuyên ngành IT.</div>
          <form onSubmit={submit} noValidate>
            <div className="nh-field">
              <Label htmlFor="email">Email</Label>
              <Input id="email" type="email" autoComplete="email"
                     value={email} onChange={e => setEmail(e.target.value)} />
            </div>
            <div className="nh-field">
              <Label htmlFor="pw">Mật khẩu</Label>
              <Input id="pw" type="password" autoComplete="current-password" invalid={showError}
                     value={password} onChange={e => setPassword(e.target.value)} />
              {showError && <div className="nh-field-help">Mật khẩu phải có ít nhất 6 ký tự.</div>}
            </div>
            <Button type="submit" disabled={busy}>{busy ? 'Đang đăng nhập...' : 'Đăng nhập'}</Button>
          </form>
          <div className="nh-auth-foot">
            <a>Quên mật khẩu?</a>
            <a>Tạo tài khoản</a>
          </div>
        </Card>
      </div>
    </div>
  );
}

window.LoginScreen = LoginScreen;
