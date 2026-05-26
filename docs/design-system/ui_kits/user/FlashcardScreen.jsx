/* eslint-disable react/prop-types */
/* FlashcardScreen — SRS-style study session. Mirrors FlashcardReview.tsx +
   RatingButtons.tsx. Click card to flip; rating advances to next card. */

function FlashcardScreen() {
  const deck = window.MOCK.VOCAB.slice(0, 8);
  const [idx, setIdx] = React.useState(0);
  const [flipped, setFlipped] = React.useState(false);
  const card = deck[idx];

  React.useEffect(() => {
    function onKey(e) {
      if (e.key === ' ' || e.key === 'Enter') { e.preventDefault(); setFlipped(f => !f); }
      if (flipped && ['1','2','3','4'].includes(e.key)) rate(parseInt(e.key));
    }
    window.addEventListener('keydown', onKey);
    return () => window.removeEventListener('keydown', onKey);
  });

  function rate(score) {
    setFlipped(false);
    setTimeout(() => setIdx(i => (i + 1) % deck.length), 220);
  }

  return (
    <div>
      <div className="nh-page-head">
        <h1>Học flashcard</h1>
        <div className="sub">Spaced repetition — đánh giá mỗi thẻ sau khi xem mặt sau.</div>
      </div>

      <div className="nh-flash-prog">
        <span>Thẻ {idx + 1} / {deck.length}</span>
        <span style={{ display: 'inline-flex', alignItems: 'center', gap: 6, color: 'var(--shu-600)' }}>
          <Icon.Flame size={14}/> 12 ngày liên tiếp
        </span>
      </div>

      <div className="nh-flash-wrap">
        <button className="nh-flash-btn" onClick={() => setFlipped(f => !f)} aria-label={flipped ? 'Mặt sau' : 'Bấm để lật thẻ'}>
          <div className={cx('nh-flash-inner', flipped && 'flipped')}>
            <div className="nh-flash-face nh-flash-front">
              <div className="term">{card.term}</div>
              <div className="hint"><Icon.RotateCw size={11}/> Bấm để lật · phím Space</div>
            </div>
            <div className="nh-flash-face nh-flash-back">
              <div className="body">
                <div className="reading">{card.pron}</div>
                <div className="meaning">{card.meaning.split('—')[0].trim()}</div>
                <div style={{ font: '400 13px/1.45 var(--font-sans)', color: 'var(--washi-500)', maxWidth: 360 }}>
                  {card.meaning.split('—')[1]?.trim()}
                </div>
                <div style={{ marginTop: 6 }}>
                  <JlptBadge level={card.jlpt} />
                </div>
              </div>
            </div>
          </div>
        </button>
      </div>

      <div className="nh-rating" style={{ opacity: flipped ? 1 : 0.45, pointerEvents: flipped ? 'auto' : 'none', transition: 'opacity 200ms var(--ease-out)' }}>
        <button className="nh-rate nh-rate-again" onClick={() => rate(1)}>Quên<span className="k">1</span></button>
        <button className="nh-rate nh-rate-hard"  onClick={() => rate(2)}>Khó<span className="k">2</span></button>
        <button className="nh-rate nh-rate-good"  onClick={() => rate(3)}>Tốt<span className="k">3</span></button>
        <button className="nh-rate nh-rate-easy"  onClick={() => rate(4)}>Dễ<span className="k">4</span></button>
      </div>

      <div style={{ textAlign: 'center', marginTop: 14, font: '400 11px var(--font-mono)', color: 'var(--washi-500)' }}>
        {flipped ? 'Đánh giá độ khó để chuyển sang thẻ tiếp theo' : 'Mặt trước — bấm để lật'}
      </div>
    </div>
  );
}

window.FlashcardScreen = FlashcardScreen;
