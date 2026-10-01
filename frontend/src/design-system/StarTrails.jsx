import React from 'react';
// Star trails rotating around the celestial pole (Polaris). Canvas, fading-trail technique.
function StarTrails({ pole = [0.62, 0.24], anchor, speed = 1, density = 1, bg = '18,17,14' }) {
  const anchorRef = React.useRef(anchor); anchorRef.current = anchor;
  const ref = React.useRef(null);
  const speedRef = React.useRef(speed);
  React.useEffect(() => { speedRef.current = speed; }, [speed]);
  React.useEffect(() => {
    const cv = ref.current, ctx = cv.getContext('2d');
    const reduce = window.matchMedia('(prefers-reduced-motion: reduce)').matches;
    let W, H, dpr, stars = [], raf, cur = speedRef.current, P = pole;
    const palette = ['255,244,220', '255,244,220', '255,244,220', '243,238,228', '157,214,201', '245,213,140'];
    const init = () => {
      dpr = Math.min(window.devicePixelRatio || 1, 1.5);
      W = cv.clientWidth; H = cv.clientHeight;
      const a = anchorRef.current && anchorRef.current(); P = a ? [a[0] / W, a[1] / H] : pole; cv.width = W * dpr; cv.height = H * dpr; ctx.setTransform(dpr, 0, 0, dpr, 0, 0);
      const R = Math.hypot(Math.max(P[0], 1 - P[0]) * W, Math.max(P[1], 1 - P[1]) * H);
      const n = Math.round((W * H) / 1400 * density);
      stars = Array.from({ length: n }, () => ({ r: Math.pow(Math.random(), 0.7) * R + 6, a: Math.random() * Math.PI * 2, s: Math.random() < 0.08 ? 1.5 + Math.random() : 0.5 + Math.random() * 0.8, c: palette[(Math.random() * palette.length) | 0], o: 0.25 + Math.random() * 0.75, tw: Math.random() * 6 }));
      ctx.fillStyle = 'rgb(' + bg + ')'; ctx.fillRect(0, 0, W, H);
    };
    let t = 0;
    const frame = () => {
      cur += (speedRef.current - cur) * 0.04; t += 0.016;
      ctx.fillStyle = 'rgba(' + bg + ',' + (0.045 + Math.min(0.04, cur * 0.004)) + ')'; ctx.fillRect(0, 0, W, H);
      const cx = P[0] * W, cy = P[1] * H, w = 0.0011 * cur;
      for (const st of stars) {
        const a0 = st.a; st.a += w * (0.6 + 40 / (st.r + 40));
        const o = st.o * (0.75 + 0.25 * Math.sin(t * 2 + st.tw));
        ctx.strokeStyle = 'rgba(' + st.c + ',' + o + ')'; ctx.lineWidth = st.s; ctx.lineCap = 'round';
        ctx.beginPath(); ctx.arc(cx, cy, st.r, a0, st.a); ctx.stroke();
      }
      // Polaris itself
      const g = ctx.createRadialGradient(cx, cy, 0, cx, cy, 28);
      g.addColorStop(0, 'rgba(255,244,220,.9)'); g.addColorStop(0.15, 'rgba(157,214,201,.35)'); g.addColorStop(1, 'rgba(91,179,160,0)');
      ctx.fillStyle = g; ctx.beginPath(); ctx.arc(cx, cy, 28, 0, Math.PI * 2); ctx.fill();
      ctx.fillStyle = '#fff4dc'; ctx.beginPath(); ctx.arc(cx, cy, 2.2 + Math.sin(t * 1.6) * 0.4, 0, Math.PI * 2); ctx.fill();
      raf = requestAnimationFrame(frame);
    };
    init();
    if (reduce) { for (let i = 0; i < 160; i++) { cur = 1; } frame(); cancelAnimationFrame(raf); } else raf = requestAnimationFrame(frame);
    const onR = () => init(); setTimeout(init, 60); document.fonts && document.fonts.ready.then(init); window.addEventListener('resize', onR);
    return () => { cancelAnimationFrame(raf); window.removeEventListener('resize', onR); };
  }, []);
  return <canvas ref={ref} style={{ position: 'absolute', inset: 0, width: '100%', height: '100%', display: 'block' }} />;
}
export { StarTrails };
