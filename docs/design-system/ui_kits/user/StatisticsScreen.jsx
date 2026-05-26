/* eslint-disable react/prop-types */
/* Statistics — small chart-less view to show the dashboard treatment.
   Mirrors the stats page conceptually. */

function StatisticsScreen() {
  return (
    <div>
      <div className="nh-page-head">
        <h1>Thống kê học tập</h1>
        <div className="sub">Tổng quan tiến độ tuần này · cập nhật mỗi đêm</div>
      </div>

      <div style={{ display: 'flex', alignItems: 'center', gap: 12, marginBottom: 24, padding: '14px 18px', background: 'var(--shu-50)', border: '1px solid oklch(from var(--shu-500) l c h / 30%)', borderRadius: 12 }}>
        <div style={{ width: 40, height: 40, borderRadius: 10, background: 'var(--shu-500)', color: '#fff7f1', display: 'inline-flex', alignItems: 'center', justifyContent: 'center' }}>
          <Icon.Flame size={20}/>
        </div>
        <div>
          <div style={{ font: '600 16px var(--font-sans)', color: 'var(--shu-800)' }}>12 ngày học liên tiếp</div>
          <div style={{ font: '400 12px var(--font-sans)', color: 'var(--shu-700)' }}>Tiếp tục thêm 1 thẻ hôm nay để giữ chuỗi.</div>
        </div>
      </div>

      <div className="nh-grid nh-grid-4">
        <StatCard icon={<Icon.Layers size={20}/>} tone="primary" label="Tổng thẻ" value="428"/>
        <StatCard icon={<Icon.Brain size={20}/>} tone="success" label="Đã thuộc" value="186"/>
        <StatCard icon={<Icon.Calendar size={20}/>} tone="warn" label="Cần ôn hôm nay" value="24"/>
        <StatCard icon={<Icon.TrendUp size={20}/>} tone="rose" label="Tỷ lệ đúng" value="87%"/>
      </div>

      <div style={{ marginTop: 24 }}>
        <div style={{ font: '600 11px var(--font-sans)', textTransform: 'uppercase', letterSpacing: '0.08em', color: 'var(--washi-500)', marginBottom: 10 }}>Phân bổ theo JLPT</div>
        <Card>
          <JlptBar level="N5" count={48}  total={120} />
          <JlptBar level="N4" count={62}  total={140} />
          <JlptBar level="N3" count={94}  total={138} />
          <JlptBar level="N2" count={26}  total={88}  />
          <JlptBar level="N1" count={14}  total={64}  />
        </Card>
      </div>
    </div>
  );
}

function StatCard({ icon, label, value, tone }) {
  const colourVar = ({ primary: 'var(--ai-500)', success: 'var(--success)', warn: 'var(--warning)', rose: 'var(--jlpt-n1)' }[tone]);
  return (
    <Card>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
        <div>
          <div style={{ font: '500 10px var(--font-sans)', textTransform: 'uppercase', letterSpacing: '0.04em', color: 'var(--washi-500)' }}>{label}</div>
          <div style={{ font: '600 26px var(--font-sans)', color: 'var(--washi-900)', marginTop: 6, fontVariantNumeric: 'tabular-nums', letterSpacing: '-0.01em' }}>{value}</div>
        </div>
        <div style={{ color: colourVar }}>{icon}</div>
      </div>
    </Card>
  );
}

function JlptBar({ level, count, total }) {
  const pct = Math.round((count / total) * 100);
  const lvl = level.toLowerCase();
  return (
    <div style={{ display: 'grid', gridTemplateColumns: '44px 1fr 90px', alignItems: 'center', gap: 12, padding: '8px 0' }}>
      <JlptBadge level={level}/>
      <div style={{ height: 8, background: 'var(--washi-200)', borderRadius: 999, overflow: 'hidden' }}>
        <div style={{ height: '100%', width: `${pct}%`, background: `var(--jlpt-${lvl})`, borderRadius: 999, transition: 'width 600ms var(--ease-out)' }}/>
      </div>
      <div style={{ font: '500 12px var(--font-mono)', color: 'var(--washi-700)', textAlign: 'right' }}>{count} / {total}</div>
    </div>
  );
}

window.StatisticsScreen = StatisticsScreen;
