/* @ds-bundle: {"format":4,"namespace":"PolarisDesignSystem_b0ab94","components":[{"name":"Avatar","sourcePath":"components/core/Avatar.jsx"},{"name":"Badge","sourcePath":"components/core/Badge.jsx"},{"name":"Button","sourcePath":"components/core/Button.jsx"},{"name":"Card","sourcePath":"components/core/Card.jsx"},{"name":"Eyebrow","sourcePath":"components/core/Eyebrow.jsx"},{"name":"Icon","sourcePath":"components/core/Icon.jsx"},{"name":"IconButton","sourcePath":"components/core/IconButton.jsx"},{"name":"Logo","sourcePath":"components/core/Logo.jsx"},{"name":"StateGlyph","sourcePath":"components/core/StatusBadge.jsx"},{"name":"StatusBadge","sourcePath":"components/core/StatusBadge.jsx"},{"name":"TypeTag","sourcePath":"components/core/TypeTag.jsx"},{"name":"BarChart","sourcePath":"components/data/BarChart.jsx"},{"name":"LineChart","sourcePath":"components/data/LineChart.jsx"},{"name":"ListRow","sourcePath":"components/data/ListRow.jsx"},{"name":"ListHeader","sourcePath":"components/data/ListRow.jsx"},{"name":"ProgressBar","sourcePath":"components/data/ProgressBar.jsx"},{"name":"Rating","sourcePath":"components/data/Rating.jsx"},{"name":"RingChart","sourcePath":"components/data/RingChart.jsx"},{"name":"Stat","sourcePath":"components/data/Stat.jsx"},{"name":"Alert","sourcePath":"components/feedback/Alert.jsx"},{"name":"Dialog","sourcePath":"components/feedback/Dialog.jsx"},{"name":"Toast","sourcePath":"components/feedback/Toast.jsx"},{"name":"Tooltip","sourcePath":"components/feedback/Tooltip.jsx"},{"name":"Checkbox","sourcePath":"components/forms/Checkbox.jsx"},{"name":"Input","sourcePath":"components/forms/Input.jsx"},{"name":"Kbd","sourcePath":"components/forms/Input.jsx"},{"name":"SegmentedControl","sourcePath":"components/forms/SegmentedControl.jsx"},{"name":"Select","sourcePath":"components/forms/Select.jsx"},{"name":"Switch","sourcePath":"components/forms/Switch.jsx"},{"name":"NavBar","sourcePath":"components/navigation/NavBar.jsx"},{"name":"Tabs","sourcePath":"components/navigation/Tabs.jsx"}],"sourceHashes":{"assets/icons/icons.js":"8d0691d4e611","components/core/Avatar.jsx":"4ada7b88bde3","components/core/Badge.jsx":"d739ee693adb","components/core/Button.jsx":"bde461151670","components/core/Card.jsx":"a33469dcc0ee","components/core/Eyebrow.jsx":"785a13f63b48","components/core/Icon.jsx":"a690dd37189f","components/core/IconButton.jsx":"257ca1044c69","components/core/Logo.jsx":"9075802014b0","components/core/StatusBadge.jsx":"6fa2277220f8","components/core/TypeTag.jsx":"6f057a027ebf","components/core/hooks.jsx":"849d6d27b975","components/data/BarChart.jsx":"f8a0f32a324c","components/data/LineChart.jsx":"7feefc905f28","components/data/ListRow.jsx":"da505de16717","components/data/ProgressBar.jsx":"71134fc23f47","components/data/Rating.jsx":"432a2b4e13d9","components/data/RingChart.jsx":"7e7af0b2be60","components/data/Stat.jsx":"7a95a309a40c","components/feedback/Alert.jsx":"75a5f735c9dd","components/feedback/Dialog.jsx":"366f3404db4e","components/feedback/Toast.jsx":"8768722c8521","components/feedback/Tooltip.jsx":"f6f3737e7f4d","components/forms/Checkbox.jsx":"b17b11bd966f","components/forms/Input.jsx":"f086a95d810f","components/forms/SegmentedControl.jsx":"dd702142ccad","components/forms/Select.jsx":"fb31947ab427","components/forms/Switch.jsx":"5f0eabed8c5a","components/navigation/NavBar.jsx":"5b5a85947247","components/navigation/Tabs.jsx":"0b78621df2bd","ui_kits/polaris-mobile/MHome.jsx":"4b15ff40af7c","ui_kits/polaris-mobile/MModules.jsx":"fb16762f2fbb","ui_kits/polaris-mobile/MOdisea.jsx":"9535c4fa06de","ui_kits/polaris-mobile/MShell.jsx":"252ab9bb9ae6","ui_kits/polaris-mobile/ios-frame.jsx":"24642b887be3","ui_kits/polaris-web/Atlas.jsx":"62de8b2e5566","ui_kits/polaris-web/Dashboard.jsx":"3b9dc85cca26","ui_kits/polaris-web/DashboardGrid.jsx":"8e3ae2c31dcb","ui_kits/polaris-web/Fusion.jsx":"5d5466cb02fc","ui_kits/polaris-web/Kuiper.jsx":"106125907680","ui_kits/polaris-web/Landing.jsx":"929287addd10","ui_kits/polaris-web/Odisea.jsx":"1fd0c251c156","ui_kits/polaris-web/Perfil.jsx":"7df9c99400e9","ui_kits/polaris-web/Shell.jsx":"176a7e32ae74","ui_kits/polaris-web/StarTrails.jsx":"f7e00759ab4a","ui_kits/polaris-web/data.js":"200a0ad2c587"},"inlinedExternals":[],"unexposedExports":[{"name":"fmt","sourcePath":"components/core/hooks.jsx"},{"name":"useCountUp","sourcePath":"components/core/hooks.jsx"},{"name":"useIndicator","sourcePath":"components/core/hooks.jsx"},{"name":"useMounted","sourcePath":"components/core/hooks.jsx"}]} */

(() => {

const __ds_ns = (window.PolarisDesignSystem_b0ab94 = window.PolarisDesignSystem_b0ab94 || {});

const __ds_scope = {};

(__ds_ns.__errors = __ds_ns.__errors || []);

// assets/icons/icons.js
try { (() => {
// Mapa local de iconos Lucide (evita depender del CDN). Cargar antes de _ds_bundle.js.
(() => {
  const base = new URL('.', document.currentScript.src).href;
  const names = ["arrow-down-right", "arrow-right", "arrow-up-right", "book-open", "calendar", "check", "chevrons-up-down", "circle-alert", "circle-check", "clapperboard", "compass", "dumbbell", "flame", "gamepad-2", "house", "image-up", "info", "key-round", "link-2", "link-2-off", "lock", "log-out", "mail", "mail-warning", "minus", "orbit", "play", "plus", "repeat", "scale", "search", "send", "shopping-basket", "sparkles", "ticket", "train-front", "trophy", "tv", "unlink", "user-round", "utensils", "wallet", "x", "star", "chevron-left", "chevron-right", "chevron-down", "arrow-left"];
  window.__resources = window.__resources || {};
  for (const n of names) if (!window.__resources['ic-' + n]) window.__resources['ic-' + n] = base + n + '.svg';
})();
})(); } catch (e) { __ds_ns.__errors.push({ path: "assets/icons/icons.js", error: String((e && e.message) || e) }); }

// components/core/Avatar.jsx
try { (() => {
function Avatar({
  src,
  name = '',
  size = 32,
  style
}) {
  const initials = name.split(/\s+/).filter(Boolean).slice(0, 2).map(w => w[0]).join('').toUpperCase();
  return /*#__PURE__*/React.createElement("span", {
    className: "pl-avatar",
    style: {
      width: size,
      height: size,
      fontSize: Math.round(size * 0.4),
      ...style
    },
    title: name
  }, src ? /*#__PURE__*/React.createElement("img", {
    src: src,
    alt: name
  }) : initials);
}
Object.assign(__ds_scope, { Avatar });
})(); } catch (e) { __ds_ns.__errors.push({ path: "components/core/Avatar.jsx", error: String((e && e.message) || e) }); }

// components/core/Badge.jsx
try { (() => {
const TONES = {
  neutral: 'var(--text-2)',
  accent: 'var(--accent)',
  success: 'var(--success)',
  warning: 'var(--warning)',
  danger: 'var(--danger)',
  info: 'var(--info)'
};
function Badge({
  tone = 'neutral',
  variant = 'soft',
  color,
  children,
  style
}) {
  return /*#__PURE__*/React.createElement("span", {
    className: 'pl-badge' + (variant !== 'soft' ? ' pl-badge--' + variant : ''),
    style: {
      '--tone': color || TONES[tone],
      ...style
    }
  }, children);
}
Object.assign(__ds_scope, { Badge });
})(); } catch (e) { __ds_ns.__errors.push({ path: "components/core/Badge.jsx", error: String((e && e.message) || e) }); }

// components/core/Eyebrow.jsx
try { (() => {
function Eyebrow({
  children,
  star,
  coord,
  style
}) {
  return /*#__PURE__*/React.createElement("span", {
    className: "pl-eyebrow",
    style: style
  }, star && /*#__PURE__*/React.createElement("span", {
    className: "pl-eyebrow__star"
  }, "\u2726"), /*#__PURE__*/React.createElement("span", null, children), coord && /*#__PURE__*/React.createElement(React.Fragment, null, /*#__PURE__*/React.createElement("span", {
    className: "pl-eyebrow__sep"
  }, "\xB7"), /*#__PURE__*/React.createElement("span", null, coord)));
}
Object.assign(__ds_scope, { Eyebrow });
})(); } catch (e) { __ds_ns.__errors.push({ path: "components/core/Eyebrow.jsx", error: String((e && e.message) || e) }); }

// components/core/Card.jsx
try { (() => {
const cx = (...a) => a.filter(Boolean).join(' ');
function Card({
  eyebrow,
  title,
  action,
  footer,
  interactive,
  variant = 'default',
  padding,
  children,
  className,
  style,
  onClick,
  delay
}) {
  return /*#__PURE__*/React.createElement("div", {
    className: cx('pl-card', interactive && 'pl-card--interactive', variant !== 'default' && 'pl-card--' + variant, delay != null && 'pl-rise', className),
    style: {
      animationDelay: delay != null ? delay + 'ms' : undefined,
      ...style
    },
    onClick: onClick
  }, (eyebrow || title || action) && /*#__PURE__*/React.createElement("div", {
    className: "pl-card__head"
  }, /*#__PURE__*/React.createElement("div", {
    className: "pl-card__titles"
  }, eyebrow && (typeof eyebrow === 'string' ? /*#__PURE__*/React.createElement(__ds_scope.Eyebrow, {
    star: true
  }, eyebrow) : eyebrow), title && /*#__PURE__*/React.createElement("h3", {
    className: "pl-card__title"
  }, title)), action), /*#__PURE__*/React.createElement("div", {
    className: "pl-card__body",
    style: padding != null ? {
      padding
    } : undefined
  }, children), footer && /*#__PURE__*/React.createElement("div", {
    className: "pl-card__foot"
  }, footer));
}
Object.assign(__ds_scope, { Card });
})(); } catch (e) { __ds_ns.__errors.push({ path: "components/core/Card.jsx", error: String((e && e.message) || e) }); }

// components/core/Icon.jsx
try { (() => {
const LUCIDE = 'https://unpkg.com/lucide-static@0.460.0/icons/';
function Icon({
  name,
  size = 16,
  color,
  style,
  className = '',
  title
}) {
  return /*#__PURE__*/React.createElement("span", {
    role: title ? 'img' : undefined,
    "aria-label": title,
    "aria-hidden": title ? undefined : true,
    className: 'pl-icon ' + className,
    style: {
      '--pl-icon': `url(${window.__resources && window.__resources['ic-' + name] || LUCIDE + name + '.svg'})`,
      width: size,
      height: size,
      color,
      ...style
    }
  });
}
Object.assign(__ds_scope, { Icon });
})(); } catch (e) { __ds_ns.__errors.push({ path: "components/core/Icon.jsx", error: String((e && e.message) || e) }); }

// components/core/Button.jsx
try { (() => {
function _extends() { return _extends = Object.assign ? Object.assign.bind() : function (n) { for (var e = 1; e < arguments.length; e++) { var t = arguments[e]; for (var r in t) ({}).hasOwnProperty.call(t, r) && (n[r] = t[r]); } return n; }, _extends.apply(null, arguments); }
const cx = (...a) => a.filter(Boolean).join(' ');
function Button({
  variant = 'primary',
  size = 'md',
  icon,
  iconRight,
  loading,
  block,
  disabled,
  children,
  className,
  ...rest
}) {
  const is = size === 'sm' ? 14 : size === 'lg' ? 18 : 16;
  return /*#__PURE__*/React.createElement("button", _extends({
    className: cx('pl-btn', 'pl-btn--' + variant, size !== 'md' && 'pl-btn--' + size, block && 'pl-btn--block', className),
    disabled: disabled || loading
  }, rest), loading ? /*#__PURE__*/React.createElement("span", {
    className: "pl-btn__spin"
  }) : icon && /*#__PURE__*/React.createElement(__ds_scope.Icon, {
    name: icon,
    size: is
  }), children, iconRight && !loading && /*#__PURE__*/React.createElement(__ds_scope.Icon, {
    name: iconRight,
    size: is
  }));
}
Object.assign(__ds_scope, { Button });
})(); } catch (e) { __ds_ns.__errors.push({ path: "components/core/Button.jsx", error: String((e && e.message) || e) }); }

// components/core/IconButton.jsx
try { (() => {
function _extends() { return _extends = Object.assign ? Object.assign.bind() : function (n) { for (var e = 1; e < arguments.length; e++) { var t = arguments[e]; for (var r in t) ({}).hasOwnProperty.call(t, r) && (n[r] = t[r]); } return n; }, _extends.apply(null, arguments); }
const cx = (...a) => a.filter(Boolean).join(' ');
function IconButton({
  icon,
  label,
  variant = 'ghost',
  size = 'md',
  pressed,
  className,
  ...rest
}) {
  return /*#__PURE__*/React.createElement("button", _extends({
    "aria-label": label,
    title: label,
    "aria-pressed": pressed,
    className: cx('pl-iconbtn', variant !== 'ghost' && 'pl-iconbtn--' + variant, size === 'sm' && 'pl-iconbtn--sm', className)
  }, rest), /*#__PURE__*/React.createElement(__ds_scope.Icon, {
    name: icon,
    size: size === 'sm' ? 14 : 16
  }));
}
Object.assign(__ds_scope, { IconButton });
})(); } catch (e) { __ds_ns.__errors.push({ path: "components/core/IconButton.jsx", error: String((e && e.message) || e) }); }

// components/core/Logo.jsx
try { (() => {
const STAR = 'M46,16 Q49.6,42.4 76,46 Q49.6,49.6 46,76 Q42.4,49.6 16,46 Q42.4,42.4 46,16Z';
function Logo({
  variant = 'full',
  size = 28,
  wordmarkSize,
  color,
  style
}) {
  const mark = /*#__PURE__*/React.createElement("svg", {
    width: size,
    height: size,
    viewBox: "0 0 100 100",
    "aria-hidden": "true",
    style: {
      flex: 'none',
      overflow: 'visible'
    }
  }, /*#__PURE__*/React.createElement("rect", {
    x: "12",
    y: "12",
    width: "84",
    height: "84",
    rx: "14",
    fill: "var(--accent-deep)"
  }), /*#__PURE__*/React.createElement("rect", {
    x: "4",
    y: "4",
    width: "84",
    height: "84",
    rx: "14",
    fill: color || 'var(--accent)'
  }), /*#__PURE__*/React.createElement("path", {
    d: STAR,
    fill: "var(--text-on-accent)"
  }), /*#__PURE__*/React.createElement("circle", {
    cx: "71",
    cy: "21",
    r: "3.6",
    fill: "var(--text-on-accent)"
  }));
  if (variant === 'mark') return /*#__PURE__*/React.createElement("span", {
    role: "img",
    "aria-label": "Polaris",
    style: {
      display: 'inline-flex',
      ...style
    }
  }, mark);
  const ws = wordmarkSize || Math.round(size * 0.62);
  return /*#__PURE__*/React.createElement("span", {
    role: "img",
    "aria-label": "Polaris",
    style: {
      display: 'inline-flex',
      alignItems: 'center',
      gap: Math.round(size * 0.32),
      ...style
    }
  }, variant === 'full' && mark, /*#__PURE__*/React.createElement("span", {
    style: {
      fontFamily: 'var(--font-display)',
      fontStretch: '125%',
      fontWeight: 800,
      fontSize: ws,
      lineHeight: 1,
      letterSpacing: '0.04em',
      color: 'var(--text-1)',
      textTransform: 'uppercase'
    }
  }, "Polaris"));
}
Object.assign(__ds_scope, { Logo });
})(); } catch (e) { __ds_ns.__errors.push({ path: "components/core/Logo.jsx", error: String((e && e.message) || e) }); }

// components/core/StatusBadge.jsx
try { (() => {
const S = {
  PENDIENTE: ['Pendiente', 'pendiente'],
  EN_CURSO: ['En curso', 'en-curso'],
  TERMINADO: ['Terminado', 'terminado'],
  ABANDONADO: ['Abandonado', 'abandonado']
};
function StateGlyph({
  estado = 'PENDIENTE',
  size = 10
}) {
  const k = S[estado][1];
  return /*#__PURE__*/React.createElement("span", {
    className: 'pl-glyph pl-glyph--' + k,
    style: {
      '--tone': `var(--state-${k})`,
      width: size,
      height: size
    }
  });
}
function StatusBadge({
  estado = 'PENDIENTE',
  variant = 'badge'
}) {
  const [label, k] = S[estado];
  const glyph = /*#__PURE__*/React.createElement(StateGlyph, {
    estado: estado,
    size: 8
  });
  if (variant === 'dot') return /*#__PURE__*/React.createElement("span", {
    style: {
      display: 'inline-flex',
      alignItems: 'center',
      gap: 8,
      fontSize: 13,
      color: 'var(--text-2)',
      whiteSpace: 'nowrap'
    }
  }, glyph, label);
  return /*#__PURE__*/React.createElement("span", {
    className: "pl-badge",
    style: {
      '--tone': `var(--state-${k})`
    }
  }, glyph, label);
}
Object.assign(__ds_scope, { StateGlyph, StatusBadge });
})(); } catch (e) { __ds_ns.__errors.push({ path: "components/core/StatusBadge.jsx", error: String((e && e.message) || e) }); }

// components/core/TypeTag.jsx
try { (() => {
const T = {
  PELICULA: ['Película', 'clapperboard'],
  SERIE: ['Serie', 'tv'],
  JUEGO: ['Juego', 'gamepad-2'],
  LIBRO: ['Libro', 'book-open']
};
function TypeTag({
  tipo = 'PELICULA',
  showLabel = true,
  size = 14
}) {
  const [label, icon] = T[tipo];
  return /*#__PURE__*/React.createElement("span", {
    className: "pl-type",
    title: label
  }, /*#__PURE__*/React.createElement(__ds_scope.Icon, {
    name: icon,
    size: size
  }), showLabel && label);
}
Object.assign(__ds_scope, { TypeTag });
})(); } catch (e) { __ds_ns.__errors.push({ path: "components/core/TypeTag.jsx", error: String((e && e.message) || e) }); }

// components/core/hooks.jsx
try { (() => {
function useIndicator(value, deps) {
  const refs = React.useRef({});
  const [pos, setPos] = React.useState(null);
  const measure = () => {
    const el = refs.current[value];
    if (el) setPos({
      left: el.offsetLeft,
      width: el.offsetWidth
    });
  };
  React.useLayoutEffect(measure, [value, ...(deps || [])]);
  React.useEffect(() => {
    document.fonts && document.fonts.ready.then(measure);
    window.addEventListener('resize', measure);
    return () => window.removeEventListener('resize', measure);
  }, [value]);
  return [refs, pos];
}
function useMounted(delay = 30) {
  const [m, setM] = React.useState(false);
  React.useEffect(() => {
    const t = setTimeout(() => setM(true), delay);
    return () => clearTimeout(t);
  }, []);
  return m;
}
function useCountUp(target, duration = 900) {
  const [v, setV] = React.useState(typeof target === 'number' ? 0 : target);
  React.useEffect(() => {
    if (typeof target !== 'number') {
      setV(target);
      return;
    }
    let raf, start;
    const from = 0;
    const step = t => {
      if (!start) start = t;
      const p = Math.min(1, (t - start) / duration);
      const e = 1 - Math.pow(1 - p, 3);
      setV(from + (target - from) * e);
      if (p < 1) raf = requestAnimationFrame(step);
    };
    raf = requestAnimationFrame(step);
    return () => cancelAnimationFrame(raf);
  }, [target]);
  return v;
}
const fmt = (n, d = 0) => typeof n === 'number' ? n.toLocaleString('es-ES', {
  minimumFractionDigits: d,
  maximumFractionDigits: d
}) : n;
Object.assign(__ds_scope, { useIndicator, useMounted, useCountUp, fmt });
})(); } catch (e) { __ds_ns.__errors.push({ path: "components/core/hooks.jsx", error: String((e && e.message) || e) }); }

// components/data/BarChart.jsx
try { (() => {
function BarChart({
  data = [],
  height = 160,
  max,
  target,
  targetLabel,
  highlight,
  format = v => __ds_scope.fmt(v),
  gap = 6,
  showAxis = true,
  gridLines = 3
}) {
  const m = __ds_scope.useMounted();
  const top = max || Math.max(...data.map(d => d.value), target || 0) * 1.1 || 1;
  return /*#__PURE__*/React.createElement("div", {
    className: "pl-chart"
  }, /*#__PURE__*/React.createElement("div", {
    style: {
      position: 'relative',
      height
    }
  }, Array.from({
    length: gridLines
  }, (_, i) => /*#__PURE__*/React.createElement("div", {
    key: i,
    className: "pl-chart__grid",
    style: {
      top: i / gridLines * 100 + '%'
    }
  })), target != null && /*#__PURE__*/React.createElement("div", {
    className: "pl-chart__target",
    style: {
      bottom: target / top * 100 + '%'
    }
  }, targetLabel && /*#__PURE__*/React.createElement("span", null, targetLabel)), /*#__PURE__*/React.createElement("div", {
    className: "pl-bars",
    style: {
      '--pl-bar-gap': gap + 'px'
    }
  }, data.map((d, i) => {
    const h = d.value / top * 100;
    const hl = highlight === i || highlight === d.label || d.highlight;
    return /*#__PURE__*/React.createElement("div", {
      key: i,
      className: 'pl-bar' + (hl ? ' pl-bar--hl' : ''),
      style: {
        '--h': h + '%'
      }
    }, /*#__PURE__*/React.createElement("div", {
      className: "pl-bar__fill",
      style: {
        height: (m ? h : 0) + '%',
        transitionDelay: i * 30 + 'ms',
        background: d.color
      }
    }), /*#__PURE__*/React.createElement("span", {
      className: "pl-bar__tip"
    }, format(d.value)));
  }))), showAxis && /*#__PURE__*/React.createElement("div", {
    style: {
      display: 'flex',
      gap,
      marginTop: 8
    }
  }, data.map((d, i) => /*#__PURE__*/React.createElement("span", {
    key: i,
    className: "pl-chart__axis",
    style: {
      flex: 1,
      textAlign: 'center',
      minWidth: 0
    }
  }, d.label))));
}
Object.assign(__ds_scope, { BarChart });
})(); } catch (e) { __ds_ns.__errors.push({ path: "components/data/BarChart.jsx", error: String((e && e.message) || e) }); }

// components/data/LineChart.jsx
try { (() => {
function LineChart({
  series = [],
  labels = [],
  height = 180,
  min,
  max,
  area = true,
  format = v => __ds_scope.fmt(v),
  gridLines = 4,
  showAxis = true,
  showLegend
}) {
  const m = __ds_scope.useMounted(60);
  const id = React.useId ? React.useId().replace(/:/g, '') : 'lc';
  const all = series.flatMap(s => s.points).filter(v => v != null);
  const lo = min ?? Math.min(...all),
    hi = max ?? Math.max(...all);
  const pad = (hi - lo) * 0.12 || 1,
    y0 = lo - pad,
    y1 = hi + pad;
  const n = Math.max(...series.map(s => s.points.length));
  const X = i => n <= 1 ? 50 : i / (n - 1) * 100,
    Y = v => 100 - (v - y0) / (y1 - y0) * 100;
  return /*#__PURE__*/React.createElement("div", {
    className: "pl-chart pl-line"
  }, showLegend && /*#__PURE__*/React.createElement("div", {
    className: "pl-legend",
    style: {
      marginBottom: 12
    }
  }, series.map(s => /*#__PURE__*/React.createElement("span", {
    key: s.name
  }, /*#__PURE__*/React.createElement("i", {
    style: {
      background: s.color || 'var(--accent)'
    }
  }), s.name))), /*#__PURE__*/React.createElement("div", {
    style: {
      position: 'relative',
      height,
      marginLeft: showAxis ? 34 : 0
    }
  }, Array.from({
    length: gridLines + 1
  }, (_, i) => /*#__PURE__*/React.createElement("div", {
    key: i,
    className: "pl-chart__grid",
    style: {
      top: i / gridLines * 100 + '%',
      borderTopStyle: i === gridLines ? 'solid' : 'dashed'
    }
  }, showAxis && /*#__PURE__*/React.createElement("span", {
    className: "pl-chart__axis",
    style: {
      position: 'absolute',
      left: -34,
      top: -7,
      width: 28,
      textAlign: 'right'
    }
  }, format(y1 - i / gridLines * (y1 - y0))))), /*#__PURE__*/React.createElement("svg", {
    viewBox: "0 0 100 100",
    preserveAspectRatio: "none",
    width: "100%",
    height: "100%",
    style: {
      position: 'absolute',
      inset: 0,
      overflow: 'visible'
    }
  }, /*#__PURE__*/React.createElement("defs", null, series.map((s, k) => /*#__PURE__*/React.createElement("linearGradient", {
    key: k,
    id: id + k,
    x1: "0",
    x2: "0",
    y1: "0",
    y2: "1"
  }, /*#__PURE__*/React.createElement("stop", {
    offset: "0",
    stopColor: s.color || 'var(--accent)',
    stopOpacity: ".22"
  }), /*#__PURE__*/React.createElement("stop", {
    offset: "1",
    stopColor: s.color || 'var(--accent)',
    stopOpacity: "0"
  })))), series.map((s, k) => {
    const pts = s.points.map((v, i) => [X(i), Y(v)]);
    const d = pts.map((p, i) => (i ? 'L' : 'M') + p[0] + ',' + p[1]).join(' ');
    return /*#__PURE__*/React.createElement("g", {
      key: k
    }, area && !s.dashed && k === 0 && /*#__PURE__*/React.createElement("path", {
      d: d + ` L${pts[pts.length - 1][0]},100 L${pts[0][0]},100 Z`,
      fill: `url(#${id + k})`,
      style: {
        opacity: m ? 1 : 0,
        transition: 'opacity 1200ms'
      }
    }), /*#__PURE__*/React.createElement("path", {
      className: "pl-line__stroke",
      d: d,
      fill: "none",
      stroke: s.color || 'var(--accent)',
      strokeWidth: s.dashed ? 1.5 : 2,
      vectorEffect: "non-scaling-stroke",
      strokeDasharray: s.dashed ? '4 4' : 1,
      pathLength: s.dashed ? undefined : 1,
      strokeDashoffset: s.dashed ? 0 : m ? 0 : 1,
      strokeLinejoin: "round",
      strokeLinecap: "round"
    }));
  })), series.map((s, k) => !s.dashed && /*#__PURE__*/React.createElement("span", {
    key: k,
    className: "pl-line__dot",
    style: {
      left: X(s.points.length - 1) + '%',
      top: Y(s.points[s.points.length - 1]) + '%',
      borderColor: s.color || 'var(--accent)',
      opacity: m ? 1 : 0,
      transition: 'opacity 300ms 1200ms'
    }
  }))), showAxis && labels.length > 0 && /*#__PURE__*/React.createElement("div", {
    style: {
      display: 'flex',
      justifyContent: 'space-between',
      marginTop: 8,
      marginLeft: 34
    }
  }, labels.map((l, i) => /*#__PURE__*/React.createElement("span", {
    key: i,
    className: "pl-chart__axis"
  }, l))));
}
Object.assign(__ds_scope, { LineChart });
})(); } catch (e) { __ds_ns.__errors.push({ path: "components/data/LineChart.jsx", error: String((e && e.message) || e) }); }

// components/data/ProgressBar.jsx
try { (() => {
function ProgressBar({
  value = 0,
  max = 100,
  label,
  valueLabel,
  target,
  color,
  size = 'md',
  style
}) {
  const m = __ds_scope.useMounted();
  const pct = Math.min(100, value / max * 100);
  const over = value > max;
  return /*#__PURE__*/React.createElement("div", {
    className: 'pl-progress' + (over ? ' pl-progress--over' : '') + (size === 'lg' ? ' pl-progress--lg' : ''),
    style: style
  }, (label || valueLabel !== false) && /*#__PURE__*/React.createElement("div", {
    className: "pl-progress__meta"
  }, /*#__PURE__*/React.createElement("span", null, label), valueLabel !== false && /*#__PURE__*/React.createElement("span", {
    className: "pl-progress__val"
  }, valueLabel ?? __ds_scope.fmt(value) + ' / ' + __ds_scope.fmt(max))), /*#__PURE__*/React.createElement("div", {
    className: "pl-progress__track"
  }, /*#__PURE__*/React.createElement("div", {
    className: "pl-progress__fill",
    style: {
      width: (m ? pct : 0) + '%',
      background: over ? undefined : color
    }
  }), target != null && /*#__PURE__*/React.createElement("div", {
    className: "pl-progress__target",
    style: {
      left: Math.min(100, target / max * 100) + '%'
    }
  })));
}
Object.assign(__ds_scope, { ProgressBar });
})(); } catch (e) { __ds_ns.__errors.push({ path: "components/data/ProgressBar.jsx", error: String((e && e.message) || e) }); }

// components/data/Rating.jsx
try { (() => {
const STAR = 'polygon(50% 0%,61.8% 35.4%,100% 38.2%,69.1% 61.8%,80.9% 100%,50% 76.4%,19.1% 100%,30.9% 61.8%,0% 38.2%,38.2% 35.4%)';
function Rating({
  value,
  onChange,
  size = 14,
  showValue = true
}) {
  const [hover, setHover] = React.useState(null);
  const v = hover ?? value ?? 0;
  return /*#__PURE__*/React.createElement("span", {
    className: 'pl-rating' + (onChange ? ' pl-rating--interactive' : ''),
    onMouseLeave: () => setHover(null)
  }, /*#__PURE__*/React.createElement("span", {
    className: "pl-rating__stars"
  }, [0, 1, 2, 3, 4].map(i => {
    const fill = Math.max(0, Math.min(1, (v - i * 2) / 2));
    return /*#__PURE__*/React.createElement("span", {
      key: i,
      className: "pl-rating__star",
      style: {
        width: size,
        height: size,
        clipPath: STAR,
        background: 'var(--surface-3)'
      },
      onMouseMove: onChange ? e => {
        const r = e.currentTarget.getBoundingClientRect();
        setHover(i * 2 + (e.clientX - r.left < r.width / 2 ? 1 : 2));
      } : undefined,
      onClick: onChange ? () => onChange(hover) : undefined
    }, /*#__PURE__*/React.createElement("span", {
      className: "pl-rating__fill",
      style: {
        width: fill * 100 + '%',
        background: 'var(--accent)'
      }
    }));
  })), showValue && /*#__PURE__*/React.createElement("span", {
    className: "pl-rating__num"
  }, value != null ? value : '–', "/10"));
}
Object.assign(__ds_scope, { Rating });
})(); } catch (e) { __ds_ns.__errors.push({ path: "components/data/Rating.jsx", error: String((e && e.message) || e) }); }

// components/data/ListRow.jsx
try { (() => {
const unit = (tipo, d) => d == null ? null : tipo === 'LIBRO' ? d + ' pág' : d + ' min';
function ListRow({
  titulo,
  tituloOriginal,
  tipo,
  anio,
  duracionMin,
  estado,
  valoracion,
  favorito,
  selected,
  dense,
  index = 0,
  onClick
}) {
  const empty = /*#__PURE__*/React.createElement("span", {
    className: "pl-row__empty"
  }, "\u2014");
  return /*#__PURE__*/React.createElement("div", {
    role: "row",
    className: 'pl-row' + (selected ? ' pl-row--selected' : '') + (dense ? ' pl-row--dense' : ''),
    style: {
      animationDelay: index * 35 + 'ms'
    },
    onClick: onClick
  }, /*#__PURE__*/React.createElement(__ds_scope.StateGlyph, {
    estado: estado
  }), /*#__PURE__*/React.createElement("span", {
    className: "pl-row__title"
  }, /*#__PURE__*/React.createElement("span", null, titulo, tituloOriginal && tituloOriginal !== titulo && /*#__PURE__*/React.createElement("span", {
    className: "pl-row__sub"
  }, " \xB7 ", tituloOriginal)), favorito && /*#__PURE__*/React.createElement("span", {
    className: "pl-row__fav"
  }, "\u2726")), /*#__PURE__*/React.createElement(__ds_scope.TypeTag, {
    tipo: tipo
  }), /*#__PURE__*/React.createElement("span", {
    className: "pl-row__num"
  }, anio || empty), /*#__PURE__*/React.createElement("span", {
    className: "pl-row__num"
  }, unit(tipo, duracionMin) || empty), valoracion != null ? /*#__PURE__*/React.createElement(__ds_scope.Rating, {
    value: valoracion,
    size: 11,
    showValue: false
  }) : empty, /*#__PURE__*/React.createElement(__ds_scope.StatusBadge, {
    estado: estado,
    variant: "dot"
  }));
}
function ListHeader({
  columns = ['', 'Título', 'Tipo', 'Año', 'Duración', 'Valoración', 'Estado']
}) {
  return /*#__PURE__*/React.createElement("div", {
    className: "pl-row",
    style: {
      height: 32,
      cursor: 'default',
      animation: 'none',
      background: 'none'
    }
  }, columns.map((c, i) => /*#__PURE__*/React.createElement("span", {
    key: i,
    className: "pl-eyebrow",
    style: {
      fontSize: 10
    }
  }, c)));
}
Object.assign(__ds_scope, { ListRow, ListHeader });
})(); } catch (e) { __ds_ns.__errors.push({ path: "components/data/ListRow.jsx", error: String((e && e.message) || e) }); }

// components/data/RingChart.jsx
try { (() => {
function RingChart({
  value = 0,
  max = 100,
  size = 96,
  thickness = 8,
  color,
  label,
  sublabel,
  children
}) {
  const m = __ds_scope.useMounted(60);
  const r = (size - thickness) / 2,
    c = 2 * Math.PI * r;
  const p = Math.min(1, value / max);
  return /*#__PURE__*/React.createElement("div", {
    className: "pl-ring",
    style: {
      width: size,
      height: size
    }
  }, /*#__PURE__*/React.createElement("svg", {
    width: size,
    height: size
  }, /*#__PURE__*/React.createElement("circle", {
    cx: size / 2,
    cy: size / 2,
    r: r,
    fill: "none",
    stroke: "var(--surface-3)",
    strokeWidth: thickness
  }), /*#__PURE__*/React.createElement("circle", {
    cx: size / 2,
    cy: size / 2,
    r: r,
    fill: "none",
    stroke: value > max ? 'var(--danger)' : color || 'var(--accent)',
    strokeWidth: thickness,
    strokeDasharray: c,
    strokeDashoffset: m ? c * (1 - p) : c,
    strokeLinecap: "butt"
  })), /*#__PURE__*/React.createElement("div", {
    className: "pl-ring__label"
  }, children || /*#__PURE__*/React.createElement(React.Fragment, null, /*#__PURE__*/React.createElement("span", {
    style: {
      fontFamily: 'var(--font-display)',
      fontStretch: '118%',
      fontWeight: 800,
      fontSize: size * 0.22,
      lineHeight: 1
    }
  }, label ?? Math.round(p * 100) + '%'), sublabel && /*#__PURE__*/React.createElement("span", {
    className: "pl-chart__axis"
  }, sublabel))));
}
Object.assign(__ds_scope, { RingChart });
})(); } catch (e) { __ds_ns.__errors.push({ path: "components/data/RingChart.jsx", error: String((e && e.message) || e) }); }

// components/data/Stat.jsx
try { (() => {
function Stat({
  label,
  value,
  unit,
  decimals = 0,
  delta,
  deltaTone,
  caption,
  size = 40,
  style
}) {
  const v = __ds_scope.useCountUp(value);
  const dir = deltaTone || (delta == null ? null : String(delta).trim().startsWith('-') ? 'down' : String(delta).trim().startsWith('+') ? 'up' : 'flat');
  return /*#__PURE__*/React.createElement("div", {
    className: "pl-stat",
    style: style
  }, label && /*#__PURE__*/React.createElement("span", {
    className: "pl-eyebrow"
  }, label), /*#__PURE__*/React.createElement("span", {
    className: "pl-stat__value",
    style: {
      fontSize: size
    }
  }, __ds_scope.fmt(v, decimals), unit && /*#__PURE__*/React.createElement("span", {
    className: "pl-stat__unit"
  }, unit)), (delta != null || caption) && /*#__PURE__*/React.createElement("span", {
    className: "pl-stat__foot"
  }, delta != null && /*#__PURE__*/React.createElement("span", {
    className: 'pl-delta pl-delta--' + dir
  }, /*#__PURE__*/React.createElement(__ds_scope.Icon, {
    name: dir === 'up' ? 'arrow-up-right' : dir === 'down' ? 'arrow-down-right' : 'minus',
    size: 12
  }), " ", delta), caption));
}
Object.assign(__ds_scope, { Stat });
})(); } catch (e) { __ds_ns.__errors.push({ path: "components/data/Stat.jsx", error: String((e && e.message) || e) }); }

// components/feedback/Alert.jsx
try { (() => {
const T = {
  info: ['var(--info)', 'info'],
  success: ['var(--success)', 'circle-check'],
  warning: ['var(--warning)', 'mail-warning'],
  danger: ['var(--danger)', 'circle-alert'],
  accent: ['var(--accent)', 'sparkles']
};
function Alert({
  tone = 'info',
  title,
  children,
  action,
  icon,
  style
}) {
  const [c, i] = T[tone];
  return /*#__PURE__*/React.createElement("div", {
    className: "pl-alert",
    role: "status",
    style: {
      '--tone': c,
      ...style
    }
  }, /*#__PURE__*/React.createElement(__ds_scope.Icon, {
    name: icon || i,
    size: 17,
    className: "pl-alert__icon"
  }), /*#__PURE__*/React.createElement("div", {
    className: "pl-alert__main"
  }, title && /*#__PURE__*/React.createElement("span", {
    className: "pl-alert__title"
  }, title), children && /*#__PURE__*/React.createElement("span", {
    className: "pl-alert__body"
  }, children)), action && /*#__PURE__*/React.createElement("div", {
    className: "pl-alert__action"
  }, action));
}
Object.assign(__ds_scope, { Alert });
})(); } catch (e) { __ds_ns.__errors.push({ path: "components/feedback/Alert.jsx", error: String((e && e.message) || e) }); }

// components/feedback/Dialog.jsx
try { (() => {
function Dialog({
  open,
  title,
  children,
  footer,
  onClose,
  width = 440
}) {
  React.useEffect(() => {
    if (!open) return;
    const k = e => e.key === 'Escape' && onClose && onClose();
    window.addEventListener('keydown', k);
    return () => window.removeEventListener('keydown', k);
  }, [open, onClose]);
  if (!open) return null;
  return /*#__PURE__*/React.createElement("div", {
    className: "pl-dialog",
    onMouseDown: e => e.target === e.currentTarget && onClose && onClose()
  }, /*#__PURE__*/React.createElement("div", {
    className: "pl-dialog__panel",
    role: "dialog",
    "aria-modal": "true",
    style: {
      '--pl-dialog-w': width + 'px'
    }
  }, /*#__PURE__*/React.createElement("div", {
    className: "pl-dialog__head"
  }, /*#__PURE__*/React.createElement("h2", {
    className: "pl-dialog__title"
  }, title), onClose && /*#__PURE__*/React.createElement(__ds_scope.IconButton, {
    icon: "x",
    label: "Cerrar",
    size: "sm",
    onClick: onClose
  })), /*#__PURE__*/React.createElement("div", {
    className: "pl-dialog__body"
  }, children), footer && /*#__PURE__*/React.createElement("div", {
    className: "pl-dialog__foot"
  }, footer)));
}
Object.assign(__ds_scope, { Dialog });
})(); } catch (e) { __ds_ns.__errors.push({ path: "components/feedback/Dialog.jsx", error: String((e && e.message) || e) }); }

// components/feedback/Toast.jsx
try { (() => {
const T = {
  accent: ['var(--accent)', 'sparkles'],
  success: ['var(--success)', 'circle-check'],
  danger: ['var(--danger)', 'circle-alert'],
  info: ['var(--info)', 'info']
};
function Toast({
  tone = 'accent',
  children,
  action,
  onClose,
  fixed,
  icon
}) {
  const [c, i] = T[tone];
  return /*#__PURE__*/React.createElement("div", {
    className: 'pl-toast' + (fixed ? ' pl-toast--fixed' : ''),
    role: "status",
    style: {
      '--tone': c
    }
  }, /*#__PURE__*/React.createElement(__ds_scope.Icon, {
    name: icon || i,
    size: 16,
    className: "pl-toast__icon"
  }), /*#__PURE__*/React.createElement("span", null, children), action, onClose && /*#__PURE__*/React.createElement(__ds_scope.IconButton, {
    icon: "x",
    label: "Cerrar",
    size: "sm",
    onClick: onClose
  }));
}
Object.assign(__ds_scope, { Toast });
})(); } catch (e) { __ds_ns.__errors.push({ path: "components/feedback/Toast.jsx", error: String((e && e.message) || e) }); }

// components/feedback/Tooltip.jsx
try { (() => {
function Tooltip({
  label,
  children,
  placement = 'top',
  wrap
}) {
  return /*#__PURE__*/React.createElement("span", {
    className: 'pl-tip' + (placement === 'bottom' ? ' pl-tip--bottom' : '')
  }, children, /*#__PURE__*/React.createElement("span", {
    role: "tooltip",
    className: 'pl-tip__bubble' + (wrap ? ' pl-tip__bubble--wrap' : '')
  }, label));
}
Object.assign(__ds_scope, { Tooltip });
})(); } catch (e) { __ds_ns.__errors.push({ path: "components/feedback/Tooltip.jsx", error: String((e && e.message) || e) }); }

// components/forms/Checkbox.jsx
try { (() => {
function Checkbox({
  label,
  checked,
  defaultChecked,
  onChange,
  disabled
}) {
  return /*#__PURE__*/React.createElement("label", {
    className: 'pl-check' + (disabled ? ' pl-check--disabled' : '')
  }, /*#__PURE__*/React.createElement("input", {
    type: "checkbox",
    checked: checked,
    defaultChecked: defaultChecked,
    disabled: disabled,
    onChange: e => onChange && onChange(e.target.checked)
  }), /*#__PURE__*/React.createElement("span", {
    className: "pl-check__box"
  }, /*#__PURE__*/React.createElement(__ds_scope.Icon, {
    name: "check",
    size: 12
  })), label);
}
Object.assign(__ds_scope, { Checkbox });
})(); } catch (e) { __ds_ns.__errors.push({ path: "components/forms/Checkbox.jsx", error: String((e && e.message) || e) }); }

// components/forms/Input.jsx
try { (() => {
function _extends() { return _extends = Object.assign ? Object.assign.bind() : function (n) { for (var e = 1; e < arguments.length; e++) { var t = arguments[e]; for (var r in t) ({}).hasOwnProperty.call(t, r) && (n[r] = t[r]); } return n; }, _extends.apply(null, arguments); }
const cx = (...a) => a.filter(Boolean).join(' ');
function Input({
  label,
  hint,
  error,
  icon,
  trailing,
  locked,
  size = 'md',
  id,
  className,
  style,
  ...rest
}) {
  const iid = id || (label ? 'in-' + String(label).replace(/\W+/g, '-').toLowerCase() : undefined);
  return /*#__PURE__*/React.createElement("div", {
    className: cx('pl-field', className),
    style: style
  }, label && /*#__PURE__*/React.createElement("label", {
    className: "pl-field__label",
    htmlFor: iid
  }, label), /*#__PURE__*/React.createElement("div", {
    className: cx('pl-input', size === 'sm' && 'pl-input--sm', error && 'pl-input--error', locked && 'pl-input--locked')
  }, (icon || locked) && /*#__PURE__*/React.createElement(__ds_scope.Icon, {
    name: locked ? 'lock' : icon,
    size: 15
  }), /*#__PURE__*/React.createElement("input", _extends({
    id: iid,
    readOnly: locked || rest.readOnly
  }, rest)), trailing), error ? /*#__PURE__*/React.createElement("span", {
    className: "pl-field__error"
  }, /*#__PURE__*/React.createElement(__ds_scope.Icon, {
    name: "circle-alert",
    size: 13
  }), error) : hint && /*#__PURE__*/React.createElement("span", {
    className: "pl-field__hint"
  }, hint));
}
function Kbd({
  children
}) {
  return /*#__PURE__*/React.createElement("span", {
    className: "pl-kbd"
  }, children);
}
Object.assign(__ds_scope, { Input, Kbd });
})(); } catch (e) { __ds_ns.__errors.push({ path: "components/forms/Input.jsx", error: String((e && e.message) || e) }); }

// components/forms/SegmentedControl.jsx
try { (() => {
function SegmentedControl({
  options = [],
  value,
  onChange,
  style
}) {
  const [refs, pos] = __ds_scope.useIndicator(value, [options.length]);
  return /*#__PURE__*/React.createElement("div", {
    className: "pl-seg",
    role: "group",
    style: style
  }, pos && /*#__PURE__*/React.createElement("span", {
    className: "pl-seg__ind",
    style: {
      left: pos.left,
      width: pos.width
    }
  }), options.map(o => /*#__PURE__*/React.createElement("button", {
    key: o.value,
    ref: el => refs.current[o.value] = el,
    className: "pl-seg__opt",
    "aria-pressed": o.value === value,
    onClick: () => onChange && onChange(o.value)
  }, o.icon && /*#__PURE__*/React.createElement(__ds_scope.Icon, {
    name: o.icon,
    size: 14
  }), o.label, o.count != null && /*#__PURE__*/React.createElement("span", {
    className: "pl-seg__count"
  }, o.count))));
}
Object.assign(__ds_scope, { SegmentedControl });
})(); } catch (e) { __ds_ns.__errors.push({ path: "components/forms/SegmentedControl.jsx", error: String((e && e.message) || e) }); }

// components/forms/Select.jsx
try { (() => {
function _extends() { return _extends = Object.assign ? Object.assign.bind() : function (n) { for (var e = 1; e < arguments.length; e++) { var t = arguments[e]; for (var r in t) ({}).hasOwnProperty.call(t, r) && (n[r] = t[r]); } return n; }, _extends.apply(null, arguments); }
function Select({
  label,
  hint,
  options = [],
  icon,
  size = 'md',
  id,
  style,
  ...rest
}) {
  return /*#__PURE__*/React.createElement("div", {
    className: "pl-field",
    style: style
  }, label && /*#__PURE__*/React.createElement("label", {
    className: "pl-field__label",
    htmlFor: id
  }, label), /*#__PURE__*/React.createElement("div", {
    className: 'pl-input' + (size === 'sm' ? ' pl-input--sm' : '')
  }, icon && /*#__PURE__*/React.createElement(__ds_scope.Icon, {
    name: icon,
    size: 15
  }), /*#__PURE__*/React.createElement("select", _extends({
    id: id
  }, rest), options.map(o => typeof o === 'string' ? /*#__PURE__*/React.createElement("option", {
    key: o,
    value: o
  }, o) : /*#__PURE__*/React.createElement("option", {
    key: o.value,
    value: o.value
  }, o.label))), /*#__PURE__*/React.createElement(__ds_scope.Icon, {
    name: "chevrons-up-down",
    size: 14
  })), hint && /*#__PURE__*/React.createElement("span", {
    className: "pl-field__hint"
  }, hint));
}
Object.assign(__ds_scope, { Select });
})(); } catch (e) { __ds_ns.__errors.push({ path: "components/forms/Select.jsx", error: String((e && e.message) || e) }); }

// components/forms/Switch.jsx
try { (() => {
function Switch({
  label,
  checked,
  defaultChecked,
  onChange,
  disabled
}) {
  return /*#__PURE__*/React.createElement("label", {
    className: 'pl-switch' + (disabled ? ' pl-check--disabled' : '')
  }, /*#__PURE__*/React.createElement("input", {
    type: "checkbox",
    role: "switch",
    checked: checked,
    defaultChecked: defaultChecked,
    disabled: disabled,
    onChange: e => onChange && onChange(e.target.checked)
  }), /*#__PURE__*/React.createElement("span", {
    className: "pl-switch__track"
  }), label);
}
Object.assign(__ds_scope, { Switch });
})(); } catch (e) { __ds_ns.__errors.push({ path: "components/forms/Switch.jsx", error: String((e && e.message) || e) }); }

// components/navigation/NavBar.jsx
try { (() => {
function NavBar({
  items = [],
  value,
  onChange,
  onBrand,
  onSearch,
  user,
  style
}) {
  const [refs, pos] = __ds_scope.useIndicator(value, [items.length]);
  const cur = items.find(i => i.id === value);
  return /*#__PURE__*/React.createElement("nav", {
    className: "pl-nav",
    style: style
  }, /*#__PURE__*/React.createElement("button", {
    className: "pl-nav__brand",
    onClick: onBrand,
    "aria-label": "Polaris \u2014 inicio"
  }, /*#__PURE__*/React.createElement(__ds_scope.Logo, {
    variant: "mark",
    size: 24
  })), /*#__PURE__*/React.createElement("div", {
    className: "pl-nav__items"
  }, pos && cur && /*#__PURE__*/React.createElement("span", {
    className: "pl-nav__ind",
    style: {
      left: pos.left,
      width: pos.width,
      '--ind': cur.color || 'var(--accent)'
    }
  }), items.map(it => /*#__PURE__*/React.createElement("button", {
    key: it.id,
    ref: el => refs.current[it.id] = el,
    className: "pl-nav__item",
    "aria-current": it.id === value ? 'page' : undefined,
    style: {
      '--c': it.color || 'var(--accent)'
    },
    onClick: () => onChange && onChange(it.id)
  }, it.icon && /*#__PURE__*/React.createElement(__ds_scope.Icon, {
    name: it.icon,
    size: 16
  }), it.label))), onSearch !== null && /*#__PURE__*/React.createElement("button", {
    className: "pl-nav__search",
    onClick: onSearch
  }, /*#__PURE__*/React.createElement(__ds_scope.Icon, {
    name: "search",
    size: 14
  }), /*#__PURE__*/React.createElement("span", null, "Buscar")), user && /*#__PURE__*/React.createElement("button", {
    className: "pl-nav__user",
    onClick: user.onClick,
    "aria-label": "Perfil"
  }, /*#__PURE__*/React.createElement(__ds_scope.Avatar, {
    name: user.name,
    src: user.src,
    size: 34
  })));
}
Object.assign(__ds_scope, { NavBar });
})(); } catch (e) { __ds_ns.__errors.push({ path: "components/navigation/NavBar.jsx", error: String((e && e.message) || e) }); }

// components/navigation/Tabs.jsx
try { (() => {
function Tabs({
  items = [],
  value,
  onChange,
  style
}) {
  const [refs, pos] = __ds_scope.useIndicator(value, [items.length]);
  return /*#__PURE__*/React.createElement("div", {
    className: "pl-tabs",
    role: "tablist",
    style: style
  }, items.map(t => /*#__PURE__*/React.createElement("button", {
    key: t.value,
    ref: el => refs.current[t.value] = el,
    role: "tab",
    "aria-selected": t.value === value,
    className: "pl-tabs__tab",
    onClick: () => onChange && onChange(t.value)
  }, t.icon && /*#__PURE__*/React.createElement(__ds_scope.Icon, {
    name: t.icon,
    size: 15
  }), t.label)), pos && /*#__PURE__*/React.createElement("span", {
    className: "pl-tabs__ind",
    style: {
      left: pos.left,
      width: pos.width
    }
  }));
}
Object.assign(__ds_scope, { Tabs });
})(); } catch (e) { __ds_ns.__errors.push({ path: "components/navigation/Tabs.jsx", error: String((e && e.message) || e) }); }

// ui_kits/polaris-mobile/MHome.jsx
try { (() => {
function MLogin({
  onLogin
}) {
  const {
    Logo,
    Button,
    Input,
    Eyebrow
  } = DS();
  const [user, setUser] = React.useState('dani');
  const [pass, setPass] = React.useState('');
  const [err, setErr] = React.useState(null);
  const [loading, setLoading] = React.useState(null);
  const [leaving, setLeaving] = React.useState(false);
  const go = via => {
    if (via === 'pass' && pass.length < 4) {
      setErr('Usuario o contraseña incorrectos');
      return;
    }
    setErr(null);
    setLoading(via);
    setTimeout(() => {
      setLeaving(true);
      setTimeout(onLogin, 800);
    }, 600);
  };
  return /*#__PURE__*/React.createElement("div", {
    className: "m-login",
    "data-module": "polaris"
  }, /*#__PURE__*/React.createElement(StarTrails, {
    pole: [0.5, 0.2],
    speed: leaving ? 14 : loading ? 3 : 1,
    density: 1.2
  }), /*#__PURE__*/React.createElement("div", {
    className: "m-login__veil"
  }), /*#__PURE__*/React.createElement("div", {
    className: "m-login__top pl-rise"
  }, /*#__PURE__*/React.createElement(Logo, {
    size: 26
  }), /*#__PURE__*/React.createElement("span", {
    className: "lp__coord"
  }, "\u03B1 UMi \xB7 +89\xB0 15\u2032")), /*#__PURE__*/React.createElement("section", {
    className: 'm-login__hero' + (leaving ? ' m-login__hero--out' : '')
  }, /*#__PURE__*/React.createElement("div", {
    className: "pl-rise",
    style: {
      animationDelay: '80ms'
    }
  }, /*#__PURE__*/React.createElement(Eyebrow, {
    star: true
  }, "Tu norte, cada d\xEDa")), /*#__PURE__*/React.createElement("h1", {
    className: "m-login__word pl-rise",
    style: {
      animationDelay: '140ms'
    }
  }, "Polaris"), /*#__PURE__*/React.createElement("p", {
    className: "m-login__lead pl-rise",
    style: {
      animationDelay: '200ms'
    }
  }, "Lo que ves, lo que gastas, lo que comes y lo que entrenas. En un solo sitio.")), /*#__PURE__*/React.createElement("form", {
    className: 'm-login__form pl-rise' + (leaving ? ' m-login__form--out' : ''),
    style: {
      animationDelay: '260ms'
    },
    onSubmit: e => {
      e.preventDefault();
      go('pass');
    }
  }, /*#__PURE__*/React.createElement(Input, {
    label: "Usuario",
    icon: "user-round",
    value: user,
    onChange: e => setUser(e.target.value),
    autoComplete: "username"
  }), /*#__PURE__*/React.createElement(Input, {
    label: "Contrase\xF1a",
    icon: "key-round",
    type: "password",
    placeholder: "\u2022\u2022\u2022\u2022\u2022\u2022\u2022\u2022",
    value: pass,
    onChange: e => {
      setPass(e.target.value);
      setErr(null);
    },
    error: err,
    autoComplete: "current-password"
  }), /*#__PURE__*/React.createElement(Button, {
    type: "submit",
    size: "lg",
    block: true,
    iconRight: "arrow-right",
    loading: loading === 'pass'
  }, "Entrar"), /*#__PURE__*/React.createElement("div", {
    className: "lp__or"
  }, /*#__PURE__*/React.createElement("span", null, "o")), /*#__PURE__*/React.createElement(Button, {
    type: "button",
    variant: "secondary",
    size: "lg",
    block: true,
    loading: loading === 'google',
    onClick: () => go('google')
  }, "Continuar con Google"), /*#__PURE__*/React.createElement("p", {
    className: "lp__fine"
  }, "Cualquier contrase\xF1a de 4+ caracteres sirve en esta maqueta.")));
}
function MInicio({
  go
}) {
  const {
    Stat,
    ProgressBar,
    LineChart,
    RingChart,
    TypeTag,
    IconButton,
    Icon,
    Card
  } = DS();
  const h = new Date().getHours();
  const saludo = h < 13 ? 'Buenos días' : h < 21 ? 'Buenas tardes' : 'Buenas noches';
  const fecha = new Date().toLocaleDateString('es-ES', {
    weekday: 'short',
    day: 'numeric',
    month: 'short'
  });
  const E = PD.entradas,
    enCurso = E.filter(e => e.estado === 'EN_CURSO');
  const K = PD.kuiper,
    Fu = PD.fusion,
    A = PD.atlas,
    N = PD.nucleo;
  const dias = ['L', 'M', 'X', 'J', 'V', 'S', 'D'];
  const Band = ({
    mod,
    icon,
    name,
    sub,
    to,
    children
  }) => /*#__PURE__*/React.createElement("div", {
    className: "m-band",
    "data-module": mod,
    onClick: to ? () => go(to) : undefined
  }, /*#__PURE__*/React.createElement("div", {
    className: "m-band__id"
  }, /*#__PURE__*/React.createElement("span", {
    className: "m-band__ic"
  }, /*#__PURE__*/React.createElement(Icon, {
    name: icon,
    size: 17
  })), /*#__PURE__*/React.createElement("div", {
    className: "stack-4"
  }, /*#__PURE__*/React.createElement("b", null, name), /*#__PURE__*/React.createElement("span", {
    className: "pl-eyebrow"
  }, sub)), to && /*#__PURE__*/React.createElement(Icon, {
    name: "chevron-right",
    size: 18
  })), children);
  return /*#__PURE__*/React.createElement(MScreen, {
    go: go,
    eyebrow: "Inicio",
    coord: fecha,
    short: saludo,
    title: /*#__PURE__*/React.createElement(React.Fragment, null, saludo, ",", /*#__PURE__*/React.createElement("br", null), /*#__PURE__*/React.createElement("span", {
      style: {
        color: 'var(--accent)'
      }
    }, PD.user.nombre)),
    action: /*#__PURE__*/React.createElement(IconButton, {
      icon: "search",
      label: "Buscar",
      variant: "ghost"
    })
  }, /*#__PURE__*/React.createElement("div", {
    className: "m-today pl-rise",
    style: {
      animationDelay: '120ms'
    }
  }, /*#__PURE__*/React.createElement("div", {
    "data-module": "kuiper",
    onClick: () => go('kuiper')
  }, /*#__PURE__*/React.createElement("span", {
    className: "today__k"
  }, /*#__PURE__*/React.createElement(Icon, {
    name: "wallet",
    size: 13
  }), "Hoy"), /*#__PURE__*/React.createElement(Stat, {
    value: K.dias[K.dias.length - 1],
    decimals: 2,
    unit: "\u20AC",
    size: 26,
    caption: 'media ' + meur(K.gastado / K.dias.length)
  })), /*#__PURE__*/React.createElement("div", {
    "data-module": "fusion",
    onClick: () => go('fusion')
  }, /*#__PURE__*/React.createElement("span", {
    className: "today__k"
  }, /*#__PURE__*/React.createElement(Icon, {
    name: "flame",
    size: 13
  }), "Quedan"), /*#__PURE__*/React.createElement(Stat, {
    value: Fu.objetivo - Fu.kcal,
    unit: "kcal",
    size: 26,
    caption: "falta la cena"
  })), /*#__PURE__*/React.createElement("div", {
    "data-module": "atlas",
    onClick: () => go('atlas')
  }, /*#__PURE__*/React.createElement("span", {
    className: "today__k"
  }, /*#__PURE__*/React.createElement(Icon, {
    name: "dumbbell",
    size: 13
  }), "Toca"), /*#__PURE__*/React.createElement(Stat, {
    value: A.siguiente.rutina,
    size: 26,
    caption: 'última: ' + A.ultima.rutina
  })), /*#__PURE__*/React.createElement("div", {
    "data-module": "odisea",
    onClick: () => go('odisea')
  }, /*#__PURE__*/React.createElement("span", {
    className: "today__k"
  }, /*#__PURE__*/React.createElement(Icon, {
    name: "clapperboard",
    size: 13
  }), "En curso"), /*#__PURE__*/React.createElement(Stat, {
    value: enCurso.length,
    size: 26,
    caption: enCurso.map(e => e.titulo).join(' · ')
  }))), /*#__PURE__*/React.createElement(MSec, {
    title: "M\xF3dulos",
    delay: 180
  }, /*#__PURE__*/React.createElement("div", {
    className: "m-box"
  }, /*#__PURE__*/React.createElement(Band, {
    mod: "odisea",
    icon: "clapperboard",
    name: "Odisea",
    sub: "Ocio \xB7 en curso",
    to: "odisea"
  }, /*#__PURE__*/React.createElement("div", {
    className: "stack-12"
  }, enCurso.map(e => /*#__PURE__*/React.createElement("div", {
    key: e.id,
    className: "m-prow"
  }, /*#__PURE__*/React.createElement(TypeTag, {
    tipo: e.tipo,
    showLabel: false,
    size: 14
  }), /*#__PURE__*/React.createElement("b", null, e.titulo), /*#__PURE__*/React.createElement("span", {
    className: "pl-row__num"
  }, e.tipo === 'LIBRO' ? `${e.progreso}/${e.duracionMin}` : `ep. ${e.progreso}`), /*#__PURE__*/React.createElement(ProgressBar, {
    value: e.progreso,
    max: e.tipo === 'LIBRO' ? e.duracionMin : 62,
    valueLabel: false
  }))))), /*#__PURE__*/React.createElement(Band, {
    mod: "kuiper",
    icon: "wallet",
    name: "Kuiper",
    sub: "Septiembre \xB7 maqueta",
    to: "kuiper"
  }, /*#__PURE__*/React.createElement("div", {
    className: "stack-12"
  }, /*#__PURE__*/React.createElement(Stat, {
    value: K.gastado,
    decimals: 2,
    unit: "\u20AC",
    size: 30
  }), /*#__PURE__*/React.createElement(ProgressBar, {
    value: K.gastado,
    max: K.presupuesto,
    target: K.presupuesto * 25 / 30,
    valueLabel: `de ${meur(K.presupuesto, 0)}`,
    label: `Quedan ${meur(K.presupuesto - K.gastado)}`
  }))), /*#__PURE__*/React.createElement(Band, {
    mod: "fusion",
    icon: "flame",
    name: "Fusi\xF3n",
    sub: "Hoy \xB7 maqueta",
    to: "fusion"
  }, /*#__PURE__*/React.createElement("div", {
    className: "m-band__row"
  }, /*#__PURE__*/React.createElement("div", {
    className: "stack-8"
  }, Fu.macros.map(m => /*#__PURE__*/React.createElement(ProgressBar, {
    key: m.nombre,
    label: m.nombre,
    value: m.g,
    max: m.obj,
    valueLabel: m.g + '/' + m.obj
  }))), /*#__PURE__*/React.createElement(RingChart, {
    value: Fu.kcal,
    max: Fu.objetivo,
    size: 88,
    thickness: 8,
    label: Fu.kcal.toLocaleString('es-ES'),
    sublabel: "kcal"
  }))), /*#__PURE__*/React.createElement(Band, {
    mod: "atlas",
    icon: "dumbbell",
    name: "Atlas",
    sub: "Semana 39 \xB7 maqueta",
    to: "atlas"
  }, /*#__PURE__*/React.createElement("div", {
    className: "m-band__row"
  }, /*#__PURE__*/React.createElement("div", {
    className: "stack-4"
  }, /*#__PURE__*/React.createElement("span", {
    className: "pl-eyebrow",
    style: {
      color: 'var(--accent)'
    }
  }, "Siguiente \xB7 hoy"), /*#__PURE__*/React.createElement("b", {
    className: "big"
  }, A.siguiente.rutina)), /*#__PURE__*/React.createElement("div", {
    className: "week week--sm",
    style: {
      width: 170,
      flex: 'none'
    }
  }, dias.map((d, i) => /*#__PURE__*/React.createElement("div", {
    key: d,
    className: 'week__d' + (PD.semanaAtlas[i] ? ' on' : '') + (i === 4 ? ' today' : '')
  }, /*#__PURE__*/React.createElement("span", null, d), /*#__PURE__*/React.createElement("i", null)))))), /*#__PURE__*/React.createElement(Band, {
    mod: "nucleo",
    icon: "orbit",
    name: "N\xFAcleo",
    sub: "Peso \xB7 maqueta"
  }, /*#__PURE__*/React.createElement("div", {
    className: "m-band__row"
  }, /*#__PURE__*/React.createElement(Stat, {
    value: N.peso,
    decimals: 1,
    unit: "kg",
    size: 30,
    delta: '−' + Math.abs(N.delta).toLocaleString('es-ES') + ' kg',
    deltaTone: "up"
  }), /*#__PURE__*/React.createElement("div", {
    style: {
      width: 150,
      flex: 'none'
    }
  }, /*#__PURE__*/React.createElement(LineChart, {
    height: 48,
    series: [{
      name: 'Peso',
      points: N.serie
    }],
    showAxis: false,
    gridLines: 2
  })))))), /*#__PURE__*/React.createElement(MSec, {
    title: "Lo \xFAltimo",
    delay: 240
  }, /*#__PURE__*/React.createElement("div", {
    className: "m-box m-feed"
  }, PD.actividad.map((a, i) => /*#__PURE__*/React.createElement("div", {
    key: i,
    className: "m-feed__r",
    "data-module": a.mod,
    onClick: () => a.mod !== 'nucleo' && go(a.mod)
  }, /*#__PURE__*/React.createElement("span", {
    className: "feed__ic"
  }, /*#__PURE__*/React.createElement(Icon, {
    name: a.icon,
    size: 14
  })), /*#__PURE__*/React.createElement("span", {
    className: "stack-4",
    style: {
      minWidth: 0
    }
  }, /*#__PURE__*/React.createElement("span", {
    className: "feed__t"
  }, a.txt), /*#__PURE__*/React.createElement("span", {
    className: "pl-row__num"
  }, a.det)), /*#__PURE__*/React.createElement("span", {
    className: "pl-row__num"
  }, a.hora))))));
}
Object.assign(window, {
  MLogin,
  MInicio
});
})(); } catch (e) { __ds_ns.__errors.push({ path: "ui_kits/polaris-mobile/MHome.jsx", error: String((e && e.message) || e) }); }

// ui_kits/polaris-mobile/MModules.jsx
try { (() => {
function MKuiper({
  go
}) {
  const {
    Stat,
    BarChart,
    ProgressBar,
    SegmentedControl,
    IconButton,
    Icon,
    Badge
  } = DS();
  const K = PD.kuiper;
  const [tab, setTab] = React.useState('resumen');
  const media = K.gastado / K.dias.length;
  const icon = c => (K.categorias.find(x => x.nombre === c) || {
    icon: 'wallet'
  }).icon;
  const Movs = ({
    rows
  }) => /*#__PURE__*/React.createElement("div", {
    className: "m-box"
  }, rows.map((m, i) => /*#__PURE__*/React.createElement("div", {
    key: i,
    className: "m-mov"
  }, /*#__PURE__*/React.createElement("span", {
    className: "catic"
  }, /*#__PURE__*/React.createElement(Icon, {
    name: icon(m.categoria),
    size: 15
  })), /*#__PURE__*/React.createElement("span", {
    style: {
      minWidth: 0
    }
  }, /*#__PURE__*/React.createElement("b", null, m.concepto), /*#__PURE__*/React.createElement("small", null, m.fecha, " \xB7 ", m.categoria)), /*#__PURE__*/React.createElement("span", {
    className: "money",
    style: {
      color: m.tipo === 'INGRESO' ? 'var(--success)' : 'var(--text-1)'
    }
  }, m.tipo === 'INGRESO' ? '+' : '−', meur(m.importe)))));
  return /*#__PURE__*/React.createElement(MScreen, {
    go: go,
    eyebrow: "Kuiper",
    coord: "Sep 2026",
    short: "Gastos",
    title: "Gastos",
    badge: /*#__PURE__*/React.createElement(MMaqueta, null),
    action: /*#__PURE__*/React.createElement(IconButton, {
      icon: "plus",
      label: "Movimiento",
      variant: "solid"
    })
  }, /*#__PURE__*/React.createElement("div", {
    className: "pl-rise",
    style: {
      animationDelay: '120ms'
    }
  }, /*#__PURE__*/React.createElement(SegmentedControl, {
    value: tab,
    onChange: setTab,
    options: [{
      value: 'resumen',
      label: 'Resumen'
    }, {
      value: 'mov',
      label: 'Movimientos'
    }],
    style: {
      display: 'grid',
      gridTemplateColumns: '1fr 1fr'
    }
  })), tab === 'resumen' ? /*#__PURE__*/React.createElement(React.Fragment, null, /*#__PURE__*/React.createElement(MSec, {
    delay: 160
  }, /*#__PURE__*/React.createElement("div", {
    className: "m-box"
  }, /*#__PURE__*/React.createElement("div", {
    className: "m-hero"
  }, /*#__PURE__*/React.createElement(Stat, {
    label: "Gastado en septiembre",
    value: K.gastado,
    decimals: 2,
    unit: "\u20AC",
    size: 40,
    delta: "\u22128 %",
    deltaTone: "up",
    caption: "vs agosto"
  }), /*#__PURE__*/React.createElement(ProgressBar, {
    value: K.gastado,
    max: K.presupuesto,
    target: K.presupuesto * 25 / 30,
    valueLabel: `de ${meur(K.presupuesto, 0)}`,
    label: `Quedan ${meur(K.presupuesto - K.gastado)} · 5 días`
  })), /*#__PURE__*/React.createElement("div", {
    className: "m-kv",
    style: {
      borderTop: '1px solid var(--border-1)'
    }
  }, /*#__PURE__*/React.createElement(Stat, {
    label: "Ingresos",
    value: K.ingresos,
    decimals: 0,
    unit: "\u20AC"
  }), /*#__PURE__*/React.createElement(Stat, {
    label: "Balance",
    value: K.ingresos - K.gastado,
    decimals: 0,
    unit: "\u20AC",
    delta: "+12 %"
  })))), /*#__PURE__*/React.createElement(MSec, {
    title: "D\xEDa a d\xEDa",
    action: /*#__PURE__*/React.createElement("span", {
      className: "pl-row__num"
    }, "media ", meur(media)),
    delay: 200
  }, /*#__PURE__*/React.createElement("div", {
    className: "m-box m-pad"
  }, /*#__PURE__*/React.createElement(BarChart, {
    height: 140,
    gap: 3,
    highlight: 13,
    target: media,
    data: K.dias.slice(-14).map((v, i) => ({
      label: i % 4 === 0 ? String(12 + i) : '',
      value: v
    })),
    format: v => meur(v)
  }))), /*#__PURE__*/React.createElement(MSec, {
    title: "Por categor\xEDa",
    delay: 240
  }, /*#__PURE__*/React.createElement("div", {
    className: "m-box"
  }, K.categorias.map(c => /*#__PURE__*/React.createElement("div", {
    key: c.nombre,
    className: "m-cat"
  }, /*#__PURE__*/React.createElement("span", {
    className: "catic"
  }, /*#__PURE__*/React.createElement(Icon, {
    name: c.icon,
    size: 15
  })), /*#__PURE__*/React.createElement(ProgressBar, {
    label: /*#__PURE__*/React.createElement(React.Fragment, null, c.nombre, c.gastado > c.limite && /*#__PURE__*/React.createElement(Badge, {
      tone: "danger",
      style: {
        marginLeft: 8,
        height: 18
      }
    }, "+", meur(c.gastado - c.limite, 0))),
    value: c.gastado,
    max: c.limite,
    valueLabel: meur(c.gastado, 0) + ' / ' + meur(c.limite, 0)
  }))))), /*#__PURE__*/React.createElement(MSec, {
    title: "\xDAltimos",
    action: /*#__PURE__*/React.createElement("button", {
      className: "m-back",
      style: {
        height: 24
      },
      onClick: () => setTab('mov')
    }, "Ver todos"),
    delay: 280
  }, /*#__PURE__*/React.createElement(Movs, {
    rows: K.movimientos.slice(0, 4)
  }))) : /*#__PURE__*/React.createElement(MSec, {
    delay: 100
  }, /*#__PURE__*/React.createElement(Movs, {
    rows: [...K.movimientos, ...K.movimientos.map(m => ({
      ...m,
      fecha: m.fecha.replace('2', '1')
    }))]
  })));
}
function MFusion({
  go
}) {
  const {
    RingChart,
    Stat,
    LineChart,
    Button,
    IconButton
  } = DS();
  const Fu = PD.fusion;
  const colors = ['var(--accent)', 'var(--mod-kuiper)', 'var(--mod-nucleo)'];
  return /*#__PURE__*/React.createElement(MScreen, {
    go: go,
    eyebrow: "Fusi\xF3n",
    coord: "Jue 25 sep",
    short: "Hoy",
    title: "Hoy",
    badge: /*#__PURE__*/React.createElement(MMaqueta, null),
    action: /*#__PURE__*/React.createElement(IconButton, {
      icon: "plus",
      label: "Registrar comida",
      variant: "solid"
    })
  }, /*#__PURE__*/React.createElement(MSec, {
    delay: 120
  }, /*#__PURE__*/React.createElement("div", {
    className: "m-box"
  }, /*#__PURE__*/React.createElement("div", {
    className: "m-ring"
  }, /*#__PURE__*/React.createElement(RingChart, {
    value: Fu.kcal,
    max: Fu.objetivo,
    size: 132,
    thickness: 12,
    label: Fu.kcal.toLocaleString('es-ES'),
    sublabel: 'de ' + Fu.objetivo.toLocaleString('es-ES')
  }), /*#__PURE__*/React.createElement("div", {
    className: "stack-8",
    style: {
      flex: 1,
      minWidth: 0
    }
  }, /*#__PURE__*/React.createElement(Stat, {
    label: "Te quedan",
    value: Fu.objetivo - Fu.kcal,
    unit: "kcal",
    size: 30
  }), /*#__PURE__*/React.createElement("span", {
    className: "muted",
    style: {
      fontSize: 12.5
    }
  }, "Falta la cena."))), /*#__PURE__*/React.createElement("div", {
    className: "m-macros"
  }, Fu.macros.map((m, i) => /*#__PURE__*/React.createElement("div", {
    key: m.nombre
  }, /*#__PURE__*/React.createElement(RingChart, {
    value: m.g,
    max: m.obj,
    size: 72,
    thickness: 6,
    color: colors[i],
    label: m.g,
    sublabel: '/ ' + m.obj
  }), /*#__PURE__*/React.createElement("b", null, m.nombre)))))), /*#__PURE__*/React.createElement(MSec, {
    title: "Comidas",
    delay: 180
  }, /*#__PURE__*/React.createElement("div", {
    className: "m-box"
  }, Fu.comidas.map(c => {
    const k = c.lineas.reduce((a, l) => a + l[2], 0);
    return /*#__PURE__*/React.createElement("div", {
      key: c.momento,
      className: "m-meal"
    }, /*#__PURE__*/React.createElement("div", {
      className: "meal__h"
    }, /*#__PURE__*/React.createElement("b", null, c.momento), /*#__PURE__*/React.createElement("span", {
      className: "pl-row__num"
    }, c.hora), k ? /*#__PURE__*/React.createElement("span", {
      className: "money"
    }, k, " kcal") : /*#__PURE__*/React.createElement("span", {
      style: {
        marginLeft: 'auto'
      }
    }, /*#__PURE__*/React.createElement(Button, {
      size: "sm",
      variant: "secondary",
      icon: "plus"
    }, "A\xF1adir"))), c.lineas.map(l => /*#__PURE__*/React.createElement("div", {
      key: l[0],
      className: "meal__l"
    }, /*#__PURE__*/React.createElement("span", null, l[0]), /*#__PURE__*/React.createElement("span", {
      className: "pl-row__num"
    }, l[1], " g"), /*#__PURE__*/React.createElement("span", {
      className: "pl-row__num"
    }, l[2], " kcal"))));
  }))), /*#__PURE__*/React.createElement(MSec, {
    title: "\xDAltimos 14 d\xEDas",
    delay: 240
  }, /*#__PURE__*/React.createElement("div", {
    className: "m-box m-pad"
  }, /*#__PURE__*/React.createElement(LineChart, {
    height: 150,
    labels: ['12 sep', '18 sep', '25 sep'],
    format: v => Math.round(v / 100) / 10 + 'k',
    series: [{
      name: 'Kcal',
      points: Fu.semana
    }, {
      name: 'Objetivo',
      points: Fu.semana.map(() => Fu.objetivo),
      dashed: true,
      color: 'var(--text-3)'
    }]
  }))));
}
function MAtlas({
  go
}) {
  const {
    Stat,
    LineChart,
    Button,
    Badge
  } = DS();
  const A = PD.atlas,
    dias = ['L', 'M', 'X', 'J', 'V', 'S', 'D'];
  return /*#__PURE__*/React.createElement(MScreen, {
    go: go,
    eyebrow: "Atlas",
    coord: "Semana 39",
    short: "Progresi\xF3n",
    title: "Progresi\xF3n",
    badge: /*#__PURE__*/React.createElement(MMaqueta, null)
  }, /*#__PURE__*/React.createElement(MSec, {
    delay: 120
  }, /*#__PURE__*/React.createElement("div", {
    className: "m-next"
  }, /*#__PURE__*/React.createElement("span", {
    className: "pl-eyebrow",
    style: {
      color: 'var(--accent)'
    }
  }, "Siguiente \xB7 ", A.siguiente.dia), /*#__PURE__*/React.createElement("span", {
    className: "m-next__h"
  }, A.siguiente.rutina), /*#__PURE__*/React.createElement("div", {
    className: "week"
  }, dias.map((d, i) => /*#__PURE__*/React.createElement("div", {
    key: d,
    className: 'week__d' + (PD.semanaAtlas[i] ? ' on' : '') + (i === 4 ? ' today' : '')
  }, /*#__PURE__*/React.createElement("span", null, d), /*#__PURE__*/React.createElement("i", null)))), /*#__PURE__*/React.createElement(Button, {
    size: "lg",
    block: true,
    icon: "play"
  }, "Empezar ", A.siguiente.rutina))), /*#__PURE__*/React.createElement(MSec, {
    delay: 180
  }, /*#__PURE__*/React.createElement("div", {
    className: "m-box m-kv"
  }, /*#__PURE__*/React.createElement(Stat, {
    label: "Sesiones \xB7 sep",
    value: 11,
    delta: "+2"
  }), /*#__PURE__*/React.createElement(Stat, {
    label: "Volumen sem.",
    value: 25.3,
    decimals: 1,
    unit: "t",
    delta: "+15 %"
  }), /*#__PURE__*/React.createElement(Stat, {
    label: "1RM banca",
    value: 80,
    unit: "kg",
    caption: "Epley"
  }), /*#__PURE__*/React.createElement(Stat, {
    label: "Peso",
    value: PD.nucleo.peso,
    decimals: 1,
    unit: "kg",
    caption: "N\xFAcleo"
  }))), /*#__PURE__*/React.createElement(MSec, {
    title: "Press banca \xB7 1RM",
    delay: 220
  }, /*#__PURE__*/React.createElement("div", {
    className: "m-box m-pad"
  }, /*#__PURE__*/React.createElement(LineChart, {
    height: 150,
    labels: A.semanas.filter((_, i) => i % 3 === 0),
    format: v => Math.round(v) + '',
    series: [{
      name: '1RM',
      points: A.progresion
    }, {
      name: 'Objetivo',
      points: A.progresion.map(() => 85),
      dashed: true,
      color: 'var(--text-3)'
    }]
  }))), /*#__PURE__*/React.createElement(MSec, {
    title: "R\xE9cords",
    delay: 260
  }, /*#__PURE__*/React.createElement("div", {
    className: "m-box"
  }, A.records.map(r => /*#__PURE__*/React.createElement("div", {
    key: r.ej,
    className: "rec"
  }, /*#__PURE__*/React.createElement("div", {
    className: "stack-4"
  }, /*#__PURE__*/React.createElement("b", null, r.ej), /*#__PURE__*/React.createElement("span", {
    className: "pl-row__num"
  }, r.fecha)), /*#__PURE__*/React.createElement("span", {
    className: "money"
  }, r.v), r.nuevo && /*#__PURE__*/React.createElement(Badge, {
    tone: "accent",
    variant: "solid"
  }, "Nuevo"))))));
}
function MPerfil({
  go,
  logout
}) {
  const {
    Avatar,
    Icon,
    Badge,
    Button,
    Switch
  } = DS();
  const U = PD.user;
  const [notif, setNotif] = React.useState(true);
  const Row = ({
    icon,
    t,
    s,
    right
  }) => /*#__PURE__*/React.createElement("div", {
    className: "m-set"
  }, /*#__PURE__*/React.createElement("span", {
    className: "catic"
  }, /*#__PURE__*/React.createElement(Icon, {
    name: icon,
    size: 15
  })), /*#__PURE__*/React.createElement("span", null, /*#__PURE__*/React.createElement("span", null, t), s && /*#__PURE__*/React.createElement("small", null, s)), right || /*#__PURE__*/React.createElement(Icon, {
    name: "chevron-right",
    size: 16
  }));
  return /*#__PURE__*/React.createElement(MScreen, {
    go: go,
    back: () => go('inicio'),
    eyebrow: "Cuenta",
    coord: '@' + U.username,
    short: "Perfil",
    title: "Perfil"
  }, /*#__PURE__*/React.createElement("div", {
    className: "m-pf pl-rise",
    style: {
      animationDelay: '100ms'
    }
  }, /*#__PURE__*/React.createElement(Avatar, {
    name: U.nombre,
    size: 84
  }), /*#__PURE__*/React.createElement("b", null, U.nombre), /*#__PURE__*/React.createElement("span", {
    className: "muted",
    style: {
      fontSize: 13.5
    }
  }, U.email)), /*#__PURE__*/React.createElement(MSec, {
    title: "Datos",
    delay: 160
  }, /*#__PURE__*/React.createElement("div", {
    className: "m-box"
  }, /*#__PURE__*/React.createElement(Row, {
    icon: "user-round",
    t: "Nombre y usuario",
    s: U.nombre + ' · @' + U.username
  }), /*#__PURE__*/React.createElement(Row, {
    icon: "mail",
    t: "Correo",
    s: U.email,
    right: /*#__PURE__*/React.createElement(Badge, {
      tone: "success"
    }, "Verificado")
  }), /*#__PURE__*/React.createElement(Row, {
    icon: "image-up",
    t: "Foto de perfil",
    s: "Sin foto"
  }))), /*#__PURE__*/React.createElement(MSec, {
    title: "Acceso",
    delay: 200
  }, /*#__PURE__*/React.createElement("div", {
    className: "m-box"
  }, /*#__PURE__*/React.createElement(Row, {
    icon: "lock",
    t: "Contrase\xF1a",
    s: "Cambiada hace 28 d\xEDas"
  }), /*#__PURE__*/React.createElement(Row, {
    icon: "link-2",
    t: "Google",
    s: "Vinculada",
    right: /*#__PURE__*/React.createElement(Badge, {
      tone: "success"
    }, "Activa")
  }), /*#__PURE__*/React.createElement(Row, {
    icon: "info",
    t: "Avisos",
    s: "Resumen diario a las 21:00",
    right: /*#__PURE__*/React.createElement(Switch, {
      checked: notif,
      onChange: setNotif
    })
  }))), /*#__PURE__*/React.createElement("div", {
    className: "pl-rise",
    style: {
      marginTop: 26,
      animationDelay: '240ms'
    }
  }, /*#__PURE__*/React.createElement(Button, {
    variant: "danger",
    size: "lg",
    block: true,
    icon: "log-out",
    onClick: logout
  }, "Cerrar sesi\xF3n")));
}
Object.assign(window, {
  MKuiper,
  MFusion,
  MAtlas,
  MPerfil
});
})(); } catch (e) { __ds_ns.__errors.push({ path: "ui_kits/polaris-mobile/MModules.jsx", error: String((e && e.message) || e) }); }

// ui_kits/polaris-mobile/MOdisea.jsx
try { (() => {
function MCover({
  e,
  big
}) {
  const {
    TypeTag
  } = DS();
  return /*#__PURE__*/React.createElement("div", {
    className: "cover",
    "data-module": "odisea"
  }, big && /*#__PURE__*/React.createElement("span", {
    className: "cover__star"
  }, "\u2726"), /*#__PURE__*/React.createElement(TypeTag, {
    tipo: e.tipo,
    showLabel: false,
    size: big ? 18 : 16
  }), big && /*#__PURE__*/React.createElement("span", null, e.anio || '—'));
}
function MDetail({
  e,
  update
}) {
  const {
    TypeTag,
    Rating,
    ProgressBar,
    Switch,
    Button,
    Eyebrow,
    SegmentedControl
  } = DS();
  const dur = e.duracionMin == null ? null : e.tipo === 'LIBRO' ? e.duracionMin + ' pág.' : e.duracionMin + ' min';
  return /*#__PURE__*/React.createElement("div", {
    className: "m-det"
  }, /*#__PURE__*/React.createElement("div", {
    className: "m-det__top"
  }, /*#__PURE__*/React.createElement(MCover, {
    e: e,
    big: true
  }), /*#__PURE__*/React.createElement("div", {
    className: "stack-8",
    style: {
      minWidth: 0
    }
  }, /*#__PURE__*/React.createElement(Eyebrow, {
    coord: e.fuenteExterna.replace('_', ' ')
  }, /*#__PURE__*/React.createElement(TypeTag, {
    tipo: e.tipo,
    size: 12
  })), /*#__PURE__*/React.createElement("h2", null, e.titulo), /*#__PURE__*/React.createElement("span", {
    className: "detail__meta"
  }, [e.anio, dur].filter(Boolean).join(' · ') || 'Sin año ni duración'))), e.sinopsis ? /*#__PURE__*/React.createElement("p", {
    className: "detail__syn"
  }, e.sinopsis) : /*#__PURE__*/React.createElement("p", {
    className: "detail__syn detail__syn--empty"
  }, "Sin sinopsis."), /*#__PURE__*/React.createElement("div", {
    className: "detail__block"
  }, /*#__PURE__*/React.createElement("span", {
    className: "pl-eyebrow"
  }, "Estado"), /*#__PURE__*/React.createElement(SegmentedControl, {
    value: e.estado,
    onChange: v => update({
      estado: v
    }),
    options: [{
      value: 'PENDIENTE',
      label: 'Pendiente'
    }, {
      value: 'EN_CURSO',
      label: 'En curso'
    }, {
      value: 'TERMINADO',
      label: 'Terminado'
    }, {
      value: 'ABANDONADO',
      label: 'Abandon.'
    }]
  })), e.estado === 'EN_CURSO' && e.progreso != null && /*#__PURE__*/React.createElement("div", {
    className: "detail__block"
  }, e.tipo === 'LIBRO' ? /*#__PURE__*/React.createElement(ProgressBar, {
    label: "Progreso",
    value: e.progreso,
    max: e.duracionMin,
    valueLabel: `pág. ${e.progreso} / ${e.duracionMin}`,
    size: "lg"
  }) : /*#__PURE__*/React.createElement("div", {
    style: {
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'space-between'
    }
  }, /*#__PURE__*/React.createElement("div", {
    className: "stack-4"
  }, /*#__PURE__*/React.createElement("span", {
    className: "pl-eyebrow"
  }, "Progreso"), /*#__PURE__*/React.createElement("b", {
    className: "big"
  }, "Episodio ", e.progreso)), /*#__PURE__*/React.createElement(Button, {
    variant: "secondary",
    icon: "plus",
    onClick: () => update({
      progreso: e.progreso + 1
    })
  }, "Episodio"))), /*#__PURE__*/React.createElement("div", {
    className: "detail__grid"
  }, /*#__PURE__*/React.createElement("div", {
    className: "stack-8"
  }, /*#__PURE__*/React.createElement("span", {
    className: "pl-eyebrow"
  }, "Valoraci\xF3n"), /*#__PURE__*/React.createElement(Rating, {
    value: e.valoracion,
    size: 18,
    onChange: v => update({
      valoracion: v
    })
  })), /*#__PURE__*/React.createElement("div", {
    className: "stack-8"
  }, /*#__PURE__*/React.createElement("span", {
    className: "pl-eyebrow"
  }, "Favorito"), /*#__PURE__*/React.createElement(Switch, {
    checked: e.favorito,
    onChange: v => update({
      favorito: v
    }),
    label: e.favorito ? 'Sí' : 'No'
  }))), /*#__PURE__*/React.createElement("div", {
    className: "detail__block"
  }, /*#__PURE__*/React.createElement("span", {
    className: "pl-eyebrow"
  }, "Notas"), e.notas ? /*#__PURE__*/React.createElement("p", {
    className: "detail__notes"
  }, e.notas) : /*#__PURE__*/React.createElement("p", {
    className: "detail__syn--empty",
    style: {
      margin: 0,
      fontSize: 13
    }
  }, "Sin notas todav\xEDa.")));
}
function MOdisea({
  go,
  toast
}) {
  const {
    StatusBadge,
    IconButton,
    Input,
    TypeTag,
    Badge,
    Button
  } = DS();
  const [items, setItems] = React.useState(PD.entradas);
  const [estado, setEstado] = React.useState('ALL');
  const [q, setQ] = React.useState('');
  const [sel, setSel] = React.useState(null);
  const [add, setAdd] = React.useState(false);
  const [aq, setAq] = React.useState('');
  const count = s => items.filter(e => e.estado === s).length;
  const list = items.filter(e => (estado === 'ALL' || e.estado === estado) && e.titulo.toLowerCase().includes(q.toLowerCase()));
  const cur = items.find(e => e.id === sel);
  const update = patch => setItems(xs => xs.map(x => x.id === sel ? {
    ...x,
    ...patch
  } : x));
  const res = PD.catalogo.filter(c => c.titulo.toLowerCase().includes(aq.toLowerCase()));
  const onAdd = c => {
    setItems(xs => [{
      id: Date.now(),
      ...c,
      tituloOriginal: null,
      duracionMin: null,
      generos: '',
      estado: 'PENDIENTE',
      valoracion: null,
      favorito: false,
      progreso: null,
      notas: null,
      sinopsis: null
    }, ...xs]);
    setAdd(false);
    toast(c.titulo + ' añadido a Pendientes');
  };
  const chips = [['ALL', 'Todo', items.length], ['EN_CURSO', 'En curso', count('EN_CURSO')], ['PENDIENTE', 'Pendiente', count('PENDIENTE')], ['TERMINADO', 'Terminado', count('TERMINADO')], ['ABANDONADO', 'Abandonado', count('ABANDONADO')]];
  return /*#__PURE__*/React.createElement(React.Fragment, null, /*#__PURE__*/React.createElement(MScreen, {
    go: go,
    eyebrow: "Odisea",
    coord: items.length + ' títulos',
    short: "Tu lista",
    title: "Tu lista",
    action: /*#__PURE__*/React.createElement(IconButton, {
      icon: "plus",
      label: "A\xF1adir t\xEDtulo",
      variant: "solid",
      onClick: () => setAdd(true)
    })
  }, /*#__PURE__*/React.createElement("div", {
    className: "stack-12 pl-rise",
    style: {
      animationDelay: '120ms'
    }
  }, /*#__PURE__*/React.createElement(Input, {
    icon: "search",
    placeholder: "Filtrar por t\xEDtulo",
    value: q,
    onChange: e => setQ(e.target.value)
  }), /*#__PURE__*/React.createElement("div", {
    className: "m-chips"
  }, chips.map(([v, l, n]) => /*#__PURE__*/React.createElement("button", {
    key: v,
    className: 'm-chip' + (estado === v ? ' on' : ''),
    onClick: () => setEstado(v)
  }, l, /*#__PURE__*/React.createElement("em", null, n))))), /*#__PURE__*/React.createElement(MSec, {
    delay: 180
  }, /*#__PURE__*/React.createElement("div", {
    className: "m-box"
  }, list.map(e => /*#__PURE__*/React.createElement("div", {
    key: e.id,
    className: "m-entry",
    onClick: () => setSel(e.id)
  }, /*#__PURE__*/React.createElement(MCover, {
    e: e
  }), /*#__PURE__*/React.createElement("div", {
    style: {
      minWidth: 0
    }
  }, /*#__PURE__*/React.createElement("b", null, e.titulo), /*#__PURE__*/React.createElement("div", {
    className: "m-entry__meta"
  }, /*#__PURE__*/React.createElement(TypeTag, {
    tipo: e.tipo,
    size: 11
  }), /*#__PURE__*/React.createElement("span", null, e.anio))), /*#__PURE__*/React.createElement("div", {
    className: "m-entry__r"
  }, /*#__PURE__*/React.createElement(StatusBadge, {
    estado: e.estado,
    variant: "dot"
  }), e.valoracion != null && /*#__PURE__*/React.createElement("span", {
    className: "pl-row__num"
  }, "\u2605 ", e.valoracion)))), !list.length && /*#__PURE__*/React.createElement("div", {
    className: "cmd__empty",
    style: {
      padding: 32
    }
  }, "Nada con estos filtros.")))), /*#__PURE__*/React.createElement(MSheet, {
    open: !!cur,
    onClose: () => setSel(null),
    title: "Entrada"
  }, cur && /*#__PURE__*/React.createElement(MDetail, {
    e: cur,
    update: update
  })), /*#__PURE__*/React.createElement(MSheet, {
    open: add,
    onClose: () => setAdd(false),
    title: "A\xF1adir a Odisea"
  }, /*#__PURE__*/React.createElement("div", {
    className: "stack-12"
  }, /*#__PURE__*/React.createElement(Input, {
    icon: "search",
    placeholder: "TMDB, IGDB, OpenLibrary",
    value: aq,
    onChange: e => setAq(e.target.value)
  }), /*#__PURE__*/React.createElement("div", {
    className: "m-box"
  }, res.map(c => /*#__PURE__*/React.createElement("div", {
    key: c.titulo,
    className: "m-mov"
  }, /*#__PURE__*/React.createElement("span", {
    className: "catic",
    "data-module": "odisea"
  }, /*#__PURE__*/React.createElement(TypeTag, {
    tipo: c.tipo,
    showLabel: false,
    size: 15
  })), /*#__PURE__*/React.createElement("span", {
    style: {
      minWidth: 0
    }
  }, /*#__PURE__*/React.createElement("b", null, c.titulo), /*#__PURE__*/React.createElement("small", null, c.anio, " \xB7 ", c.fuenteExterna.replace('_', ' '))), /*#__PURE__*/React.createElement(IconButton, {
    icon: "plus",
    label: "A\xF1adir",
    variant: "outline",
    onClick: () => onAdd(c)
  }))), !res.length && /*#__PURE__*/React.createElement("div", {
    className: "cmd__empty"
  }, "Sin resultados para \xAB", aq, "\xBB.")))));
}
window.MOdisea = MOdisea;
})(); } catch (e) { __ds_ns.__errors.push({ path: "ui_kits/polaris-mobile/MOdisea.jsx", error: String((e && e.message) || e) }); }

// ui_kits/polaris-mobile/MShell.jsx
try { (() => {
const DS = () => window.PolarisDesignSystem_b0ab94;
const MNAV = [{
  id: 'inicio',
  label: 'Inicio',
  icon: 'compass',
  mod: 'polaris'
}, {
  id: 'odisea',
  label: 'Odisea',
  icon: 'clapperboard',
  mod: 'odisea'
}, {
  id: 'kuiper',
  label: 'Kuiper',
  icon: 'wallet',
  mod: 'kuiper'
}, {
  id: 'fusion',
  label: 'Fusión',
  icon: 'flame',
  mod: 'fusion'
}, {
  id: 'atlas',
  label: 'Atlas',
  icon: 'dumbbell',
  mod: 'atlas'
}];
const meur = (n, d = 2) => n.toLocaleString('es-ES', {
  minimumFractionDigits: d,
  maximumFractionDigits: d
}) + ' €';
function MTabBar({
  route,
  go
}) {
  const {
    Icon
  } = DS();
  const i = MNAV.findIndex(n => n.id === route);
  return /*#__PURE__*/React.createElement("nav", {
    className: "m-tabs"
  }, /*#__PURE__*/React.createElement("div", {
    className: "m-tabs__in"
  }, i >= 0 && /*#__PURE__*/React.createElement("span", {
    className: "m-tabs__ind",
    "data-module": MNAV[i].mod,
    style: {
      left: `calc(${i} * (100% - 8px) / 5 + ${i * 2}px)`
    }
  }), MNAV.map(n => /*#__PURE__*/React.createElement("button", {
    key: n.id,
    "data-module": n.mod,
    className: 'm-tab' + (route === n.id ? ' on' : ''),
    "aria-current": route === n.id ? 'page' : undefined,
    onClick: () => go(n.id)
  }, /*#__PURE__*/React.createElement(Icon, {
    name: n.icon,
    size: 19
  }), /*#__PURE__*/React.createElement("span", null, n.label)))));
}

// Pantalla con cabecera grande que colapsa a barra compacta al hacer scroll
function MScreen({
  eyebrow,
  coord,
  title,
  short,
  action,
  badge,
  back,
  go,
  children
}) {
  const {
    Eyebrow,
    Avatar,
    Logo,
    Icon
  } = DS();
  const [sc, setSc] = React.useState(false);
  return /*#__PURE__*/React.createElement(React.Fragment, null, /*#__PURE__*/React.createElement("div", {
    className: 'm-top' + (sc ? ' on' : '')
  }, short || eyebrow), /*#__PURE__*/React.createElement("div", {
    className: "m-scroll",
    onScroll: e => setSc(e.currentTarget.scrollTop > 70)
  }, /*#__PURE__*/React.createElement("header", {
    className: "m-hd"
  }, /*#__PURE__*/React.createElement("div", {
    className: "m-hd__bar pl-rise"
  }, back ? /*#__PURE__*/React.createElement("button", {
    className: "m-back",
    onClick: back
  }, /*#__PURE__*/React.createElement(Icon, {
    name: "chevron-left",
    size: 20
  }), "Inicio") : /*#__PURE__*/React.createElement(Logo, {
    variant: "mark",
    size: 24
  }), /*#__PURE__*/React.createElement("div", {
    className: "m-hd__act"
  }, action, !back && /*#__PURE__*/React.createElement("button", {
    className: "m-av",
    onClick: () => go('perfil'),
    "aria-label": "Perfil"
  }, /*#__PURE__*/React.createElement(Avatar, {
    name: PD.user.nombre,
    size: 34
  })))), /*#__PURE__*/React.createElement("div", {
    className: "pl-rise",
    style: {
      display: 'flex',
      gap: 10,
      alignItems: 'center',
      animationDelay: '40ms'
    }
  }, /*#__PURE__*/React.createElement(Eyebrow, {
    star: true,
    coord: coord
  }, eyebrow), badge), /*#__PURE__*/React.createElement("h1", {
    className: "m-hd__h pl-rise",
    style: {
      animationDelay: '80ms'
    }
  }, title)), children));
}
function MSec({
  title,
  action,
  children,
  delay = 0
}) {
  return /*#__PURE__*/React.createElement("section", {
    className: "m-sec pl-rise",
    style: {
      animationDelay: delay + 'ms'
    }
  }, title && /*#__PURE__*/React.createElement("div", {
    className: "m-sec__h"
  }, /*#__PURE__*/React.createElement("b", null, title), action), children);
}
function MSheet({
  open,
  onClose,
  title,
  children
}) {
  const {
    IconButton
  } = DS();
  return /*#__PURE__*/React.createElement("div", {
    className: 'm-sheet' + (open ? ' on' : ''),
    "aria-hidden": !open
  }, /*#__PURE__*/React.createElement("div", {
    className: "m-sheet__bg",
    onClick: onClose
  }), /*#__PURE__*/React.createElement("div", {
    className: "m-sheet__p",
    role: "dialog"
  }, /*#__PURE__*/React.createElement("div", {
    className: "m-sheet__grab"
  }, /*#__PURE__*/React.createElement("i", null)), /*#__PURE__*/React.createElement("div", {
    className: "m-sheet__hd"
  }, /*#__PURE__*/React.createElement("b", null, title), /*#__PURE__*/React.createElement(IconButton, {
    icon: "x",
    label: "Cerrar",
    variant: "ghost",
    onClick: onClose
  })), /*#__PURE__*/React.createElement("div", {
    className: "m-sheet__b"
  }, children)));
}
function MMaqueta() {
  const {
    Badge
  } = DS();
  return /*#__PURE__*/React.createElement(Badge, {
    variant: "outline",
    color: "var(--text-3)"
  }, "Maqueta");
}
Object.assign(window, {
  DS,
  MNAV,
  meur,
  MTabBar,
  MScreen,
  MSec,
  MSheet,
  MMaqueta
});
})(); } catch (e) { __ds_ns.__errors.push({ path: "ui_kits/polaris-mobile/MShell.jsx", error: String((e && e.message) || e) }); }

// ui_kits/polaris-mobile/ios-frame.jsx
try { (() => {
// @ds-adherence-ignore -- omelette starter scaffold (raw elements/hex/px by design)
// Copied omelette starter. Re-running copy_starter_component with this kind overwrites this file with the latest version (page content is unaffected).

/* BEGIN USAGE */
// iOS.jsx — Simplified iOS 26 (Liquid Glass) device frame
// Based on the iOS 26 UI Kit + Figma status bar spec. No assets, no deps.
// Exports (to window): IOSDevice, IOSStatusBar, IOSNavBar, IOSGlassPill, IOSList, IOSListRow, IOSKeyboard
//
// Usage — wrap your screen content in <IOSDevice> to get the bezel, status bar
// and home indicator (props: title, dark, keyboard):
//
//   <IOSDevice title="Settings">
//     ...your screen content...
//   </IOSDevice>
//   <IOSDevice dark title="Search" keyboard>…</IOSDevice>
/* END USAGE */

// ─────────────────────────────────────────────────────────────
// Status bar
// ─────────────────────────────────────────────────────────────
function IOSStatusBar({
  dark = false,
  time = '9:41'
}) {
  const c = dark ? '#fff' : '#000';
  return /*#__PURE__*/React.createElement("div", {
    style: {
      display: 'flex',
      gap: 154,
      alignItems: 'center',
      justifyContent: 'center',
      padding: '21px 24px 19px',
      boxSizing: 'border-box',
      position: 'relative',
      zIndex: 20,
      width: '100%'
    }
  }, /*#__PURE__*/React.createElement("div", {
    style: {
      flex: 1,
      height: 22,
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'center',
      paddingTop: 1.5
    }
  }, /*#__PURE__*/React.createElement("span", {
    style: {
      fontFamily: '-apple-system, "SF Pro", system-ui',
      fontWeight: 590,
      fontSize: 17,
      lineHeight: '22px',
      color: c
    }
  }, time)), /*#__PURE__*/React.createElement("div", {
    style: {
      flex: 1,
      height: 22,
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'center',
      gap: 7,
      paddingTop: 1,
      paddingRight: 1
    }
  }, /*#__PURE__*/React.createElement("svg", {
    width: "19",
    height: "12",
    viewBox: "0 0 19 12"
  }, /*#__PURE__*/React.createElement("rect", {
    x: "0",
    y: "7.5",
    width: "3.2",
    height: "4.5",
    rx: "0.7",
    fill: c
  }), /*#__PURE__*/React.createElement("rect", {
    x: "4.8",
    y: "5",
    width: "3.2",
    height: "7",
    rx: "0.7",
    fill: c
  }), /*#__PURE__*/React.createElement("rect", {
    x: "9.6",
    y: "2.5",
    width: "3.2",
    height: "9.5",
    rx: "0.7",
    fill: c
  }), /*#__PURE__*/React.createElement("rect", {
    x: "14.4",
    y: "0",
    width: "3.2",
    height: "12",
    rx: "0.7",
    fill: c
  })), /*#__PURE__*/React.createElement("svg", {
    width: "17",
    height: "12",
    viewBox: "0 0 17 12"
  }, /*#__PURE__*/React.createElement("path", {
    d: "M8.5 3.2C10.8 3.2 12.9 4.1 14.4 5.6L15.5 4.5C13.7 2.7 11.2 1.5 8.5 1.5C5.8 1.5 3.3 2.7 1.5 4.5L2.6 5.6C4.1 4.1 6.2 3.2 8.5 3.2Z",
    fill: c
  }), /*#__PURE__*/React.createElement("path", {
    d: "M8.5 6.8C9.9 6.8 11.1 7.3 12 8.2L13.1 7.1C11.8 5.9 10.2 5.1 8.5 5.1C6.8 5.1 5.2 5.9 3.9 7.1L5 8.2C5.9 7.3 7.1 6.8 8.5 6.8Z",
    fill: c
  }), /*#__PURE__*/React.createElement("circle", {
    cx: "8.5",
    cy: "10.5",
    r: "1.5",
    fill: c
  })), /*#__PURE__*/React.createElement("svg", {
    width: "27",
    height: "13",
    viewBox: "0 0 27 13"
  }, /*#__PURE__*/React.createElement("rect", {
    x: "0.5",
    y: "0.5",
    width: "23",
    height: "12",
    rx: "3.5",
    stroke: c,
    strokeOpacity: "0.35",
    fill: "none"
  }), /*#__PURE__*/React.createElement("rect", {
    x: "2",
    y: "2",
    width: "20",
    height: "9",
    rx: "2",
    fill: c
  }), /*#__PURE__*/React.createElement("path", {
    d: "M25 4.5V8.5C25.8 8.2 26.5 7.2 26.5 6.5C26.5 5.8 25.8 4.8 25 4.5Z",
    fill: c,
    fillOpacity: "0.4"
  }))));
}

// ─────────────────────────────────────────────────────────────
// Liquid glass pill — blur + tint + shine
// ─────────────────────────────────────────────────────────────
function IOSGlassPill({
  children,
  dark = false,
  style = {}
}) {
  return /*#__PURE__*/React.createElement("div", {
    style: {
      height: 44,
      minWidth: 44,
      borderRadius: 9999,
      position: 'relative',
      overflow: 'hidden',
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'center',
      boxShadow: dark ? '0 2px 6px rgba(0,0,0,0.35), 0 6px 16px rgba(0,0,0,0.2)' : '0 1px 3px rgba(0,0,0,0.07), 0 3px 10px rgba(0,0,0,0.06)',
      ...style
    }
  }, /*#__PURE__*/React.createElement("div", {
    style: {
      position: 'absolute',
      inset: 0,
      borderRadius: 9999,
      backdropFilter: 'blur(12px) saturate(180%)',
      WebkitBackdropFilter: 'blur(12px) saturate(180%)',
      background: dark ? 'rgba(120,120,128,0.28)' : 'rgba(255,255,255,0.5)'
    }
  }), /*#__PURE__*/React.createElement("div", {
    style: {
      position: 'absolute',
      inset: 0,
      borderRadius: 9999,
      boxShadow: dark ? 'inset 1.5px 1.5px 1px rgba(255,255,255,0.15), inset -1px -1px 1px rgba(255,255,255,0.08)' : 'inset 1.5px 1.5px 1px rgba(255,255,255,0.7), inset -1px -1px 1px rgba(255,255,255,0.4)',
      border: dark ? '0.5px solid rgba(255,255,255,0.15)' : '0.5px solid rgba(0,0,0,0.06)'
    }
  }), /*#__PURE__*/React.createElement("div", {
    style: {
      position: 'relative',
      zIndex: 1,
      display: 'flex',
      alignItems: 'center',
      padding: '0 4px'
    }
  }, children));
}

// ─────────────────────────────────────────────────────────────
// Navigation bar — glass pills + large title
// ─────────────────────────────────────────────────────────────
function IOSNavBar({
  title = 'Title',
  dark = false,
  trailingIcon = true
}) {
  const muted = dark ? 'rgba(255,255,255,0.6)' : '#404040';
  const text = dark ? '#fff' : '#000';
  const pillIcon = content => /*#__PURE__*/React.createElement(IOSGlassPill, {
    dark: dark
  }, /*#__PURE__*/React.createElement("div", {
    style: {
      width: 36,
      height: 36,
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'center'
    }
  }, content));
  return /*#__PURE__*/React.createElement("div", {
    style: {
      display: 'flex',
      flexDirection: 'column',
      gap: 10,
      paddingTop: 62,
      paddingBottom: 10,
      position: 'relative',
      zIndex: 5
    }
  }, /*#__PURE__*/React.createElement("div", {
    style: {
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'space-between',
      padding: '0 16px'
    }
  }, pillIcon(/*#__PURE__*/React.createElement("svg", {
    width: "12",
    height: "20",
    viewBox: "0 0 12 20",
    fill: "none",
    style: {
      marginLeft: -1
    }
  }, /*#__PURE__*/React.createElement("path", {
    d: "M10 2L2 10l8 8",
    stroke: muted,
    strokeWidth: "2.5",
    strokeLinecap: "round",
    strokeLinejoin: "round"
  }))), trailingIcon && pillIcon(/*#__PURE__*/React.createElement("svg", {
    width: "22",
    height: "6",
    viewBox: "0 0 22 6"
  }, /*#__PURE__*/React.createElement("circle", {
    cx: "3",
    cy: "3",
    r: "2.5",
    fill: muted
  }), /*#__PURE__*/React.createElement("circle", {
    cx: "11",
    cy: "3",
    r: "2.5",
    fill: muted
  }), /*#__PURE__*/React.createElement("circle", {
    cx: "19",
    cy: "3",
    r: "2.5",
    fill: muted
  })))), /*#__PURE__*/React.createElement("div", {
    style: {
      padding: '0 16px',
      fontFamily: '-apple-system, system-ui',
      fontSize: 34,
      fontWeight: 700,
      lineHeight: '41px',
      color: text,
      letterSpacing: 0.4
    }
  }, title));
}

// ─────────────────────────────────────────────────────────────
// Grouped list (inset card, r:26) + row (52px)
// ─────────────────────────────────────────────────────────────
function IOSListRow({
  title,
  detail,
  icon,
  chevron = true,
  isLast = false,
  dark = false
}) {
  const text = dark ? '#fff' : '#000';
  const sec = dark ? 'rgba(235,235,245,0.6)' : 'rgba(60,60,67,0.6)';
  const ter = dark ? 'rgba(235,235,245,0.3)' : 'rgba(60,60,67,0.3)';
  const sep = dark ? 'rgba(84,84,88,0.65)' : 'rgba(60,60,67,0.12)';
  return /*#__PURE__*/React.createElement("div", {
    style: {
      display: 'flex',
      alignItems: 'center',
      minHeight: 52,
      padding: '0 16px',
      position: 'relative',
      fontFamily: '-apple-system, system-ui',
      fontSize: 17,
      letterSpacing: -0.43
    }
  }, icon && /*#__PURE__*/React.createElement("div", {
    style: {
      width: 30,
      height: 30,
      borderRadius: 7,
      background: icon,
      marginRight: 12,
      flexShrink: 0
    }
  }), /*#__PURE__*/React.createElement("div", {
    style: {
      flex: 1,
      color: text
    }
  }, title), detail && /*#__PURE__*/React.createElement("span", {
    style: {
      color: sec,
      marginRight: 6
    }
  }, detail), chevron && /*#__PURE__*/React.createElement("svg", {
    width: "8",
    height: "14",
    viewBox: "0 0 8 14",
    style: {
      flexShrink: 0
    }
  }, /*#__PURE__*/React.createElement("path", {
    d: "M1 1l6 6-6 6",
    stroke: ter,
    strokeWidth: "2",
    fill: "none",
    strokeLinecap: "round",
    strokeLinejoin: "round"
  })), !isLast && /*#__PURE__*/React.createElement("div", {
    style: {
      position: 'absolute',
      bottom: 0,
      right: 0,
      left: icon ? 58 : 16,
      height: 0.5,
      background: sep
    }
  }));
}
function IOSList({
  header,
  children,
  dark = false
}) {
  const hc = dark ? 'rgba(235,235,245,0.6)' : 'rgba(60,60,67,0.6)';
  const bg = dark ? '#1C1C1E' : '#fff';
  return /*#__PURE__*/React.createElement("div", null, header && /*#__PURE__*/React.createElement("div", {
    style: {
      fontFamily: '-apple-system, system-ui',
      fontSize: 13,
      color: hc,
      textTransform: 'uppercase',
      padding: '8px 36px 6px',
      letterSpacing: -0.08
    }
  }, header), /*#__PURE__*/React.createElement("div", {
    style: {
      background: bg,
      borderRadius: 26,
      margin: '0 16px',
      overflow: 'hidden'
    }
  }, children));
}

// ─────────────────────────────────────────────────────────────
// Device frame
// ─────────────────────────────────────────────────────────────
function IOSDevice({
  children,
  width = 402,
  height = 874,
  dark = false,
  title,
  keyboard = false
}) {
  return (
    /*#__PURE__*/
    // data-om-starter: inert presence marker — Claude Design's starter-usage
    // probe reads it; it renders nothing. Keep it on this root element.
    React.createElement("div", {
      "data-om-starter": "ios-frame",
      style: {
        width,
        height,
        borderRadius: 48,
        overflow: 'hidden',
        position: 'relative',
        background: dark ? '#000' : '#F2F2F7',
        boxShadow: '0 40px 80px rgba(0,0,0,0.18), 0 0 0 1px rgba(0,0,0,0.12)',
        fontFamily: '-apple-system, system-ui, sans-serif',
        WebkitFontSmoothing: 'antialiased'
      }
    }, /*#__PURE__*/React.createElement("div", {
      style: {
        position: 'absolute',
        top: 11,
        left: '50%',
        transform: 'translateX(-50%)',
        width: 126,
        height: 37,
        borderRadius: 24,
        background: '#000',
        zIndex: 50
      }
    }), /*#__PURE__*/React.createElement("div", {
      style: {
        position: 'absolute',
        top: 0,
        left: 0,
        right: 0,
        zIndex: 10
      }
    }, /*#__PURE__*/React.createElement(IOSStatusBar, {
      dark: dark
    })), /*#__PURE__*/React.createElement("div", {
      style: {
        height: '100%',
        display: 'flex',
        flexDirection: 'column'
      }
    }, title !== undefined && /*#__PURE__*/React.createElement(IOSNavBar, {
      title: title,
      dark: dark
    }), /*#__PURE__*/React.createElement("div", {
      style: {
        flex: 1,
        overflow: 'auto'
      }
    }, children), keyboard && /*#__PURE__*/React.createElement(IOSKeyboard, {
      dark: dark
    })), /*#__PURE__*/React.createElement("div", {
      style: {
        position: 'absolute',
        bottom: 0,
        left: 0,
        right: 0,
        zIndex: 60,
        height: 34,
        display: 'flex',
        justifyContent: 'center',
        alignItems: 'flex-end',
        paddingBottom: 8,
        pointerEvents: 'none'
      }
    }, /*#__PURE__*/React.createElement("div", {
      style: {
        width: 139,
        height: 5,
        borderRadius: 100,
        background: dark ? 'rgba(255,255,255,0.7)' : 'rgba(0,0,0,0.25)'
      }
    })))
  );
}

// ─────────────────────────────────────────────────────────────
// Keyboard — iOS 26 liquid glass
// ─────────────────────────────────────────────────────────────
function IOSKeyboard({
  dark = false
}) {
  const glyph = dark ? 'rgba(255,255,255,0.7)' : '#595959';
  const sugg = dark ? 'rgba(255,255,255,0.6)' : '#333';
  const keyBg = dark ? 'rgba(255,255,255,0.22)' : 'rgba(255,255,255,0.85)';

  // special-key icons
  const icons = {
    shift: /*#__PURE__*/React.createElement("svg", {
      width: "19",
      height: "17",
      viewBox: "0 0 19 17"
    }, /*#__PURE__*/React.createElement("path", {
      d: "M9.5 1L1 9.5h4.5V16h8V9.5H18L9.5 1z",
      fill: glyph
    })),
    del: /*#__PURE__*/React.createElement("svg", {
      width: "23",
      height: "17",
      viewBox: "0 0 23 17"
    }, /*#__PURE__*/React.createElement("path", {
      d: "M7 1h13a2 2 0 012 2v11a2 2 0 01-2 2H7l-6-7.5L7 1z",
      fill: "none",
      stroke: glyph,
      strokeWidth: "1.6",
      strokeLinejoin: "round"
    }), /*#__PURE__*/React.createElement("path", {
      d: "M10 5l7 7M17 5l-7 7",
      stroke: glyph,
      strokeWidth: "1.6",
      strokeLinecap: "round"
    })),
    ret: /*#__PURE__*/React.createElement("svg", {
      width: "20",
      height: "14",
      viewBox: "0 0 20 14"
    }, /*#__PURE__*/React.createElement("path", {
      d: "M18 1v6H4m0 0l4-4M4 7l4 4",
      fill: "none",
      stroke: "#fff",
      strokeWidth: "1.8",
      strokeLinecap: "round",
      strokeLinejoin: "round"
    }))
  };
  const key = (content, {
    w,
    flex,
    ret,
    fs = 25,
    k
  } = {}) => /*#__PURE__*/React.createElement("div", {
    key: k,
    style: {
      height: 42,
      borderRadius: 8.5,
      flex: flex ? 1 : undefined,
      width: w,
      minWidth: 0,
      background: ret ? '#08f' : keyBg,
      boxShadow: '0 1px 0 rgba(0,0,0,0.075)',
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'center',
      fontFamily: '-apple-system, "SF Compact", system-ui',
      fontSize: fs,
      fontWeight: 458,
      color: ret ? '#fff' : glyph
    }
  }, content);
  const row = (keys, pad = 0) => /*#__PURE__*/React.createElement("div", {
    style: {
      display: 'flex',
      gap: 6.5,
      justifyContent: 'center',
      padding: `0 ${pad}px`
    }
  }, keys.map(l => key(l, {
    flex: true,
    k: l
  })));
  return /*#__PURE__*/React.createElement("div", {
    style: {
      position: 'relative',
      zIndex: 15,
      borderRadius: 27,
      overflow: 'hidden',
      padding: '11px 0 2px',
      display: 'flex',
      flexDirection: 'column',
      alignItems: 'center',
      boxShadow: dark ? '0 -2px 20px rgba(0,0,0,0.09)' : '0 -1px 6px rgba(0,0,0,0.018), 0 -3px 20px rgba(0,0,0,0.012)'
    }
  }, /*#__PURE__*/React.createElement("div", {
    style: {
      position: 'absolute',
      inset: 0,
      borderRadius: 27,
      backdropFilter: 'blur(12px) saturate(180%)',
      WebkitBackdropFilter: 'blur(12px) saturate(180%)',
      background: dark ? 'rgba(120,120,128,0.14)' : 'rgba(255,255,255,0.25)'
    }
  }), /*#__PURE__*/React.createElement("div", {
    style: {
      position: 'absolute',
      inset: 0,
      borderRadius: 27,
      boxShadow: dark ? 'inset 1.5px 1.5px 1px rgba(255,255,255,0.15)' : 'inset 1.5px 1.5px 1px rgba(255,255,255,0.7), inset -1px -1px 1px rgba(255,255,255,0.4)',
      border: dark ? '0.5px solid rgba(255,255,255,0.15)' : '0.5px solid rgba(0,0,0,0.06)',
      pointerEvents: 'none'
    }
  }), /*#__PURE__*/React.createElement("div", {
    style: {
      display: 'flex',
      gap: 20,
      alignItems: 'center',
      padding: '8px 22px 13px',
      width: '100%',
      boxSizing: 'border-box',
      position: 'relative'
    }
  }, ['"The"', 'the', 'to'].map((w, i) => /*#__PURE__*/React.createElement(React.Fragment, {
    key: i
  }, i > 0 && /*#__PURE__*/React.createElement("div", {
    style: {
      width: 1,
      height: 25,
      background: '#ccc',
      opacity: 0.3
    }
  }), /*#__PURE__*/React.createElement("div", {
    style: {
      flex: 1,
      textAlign: 'center',
      fontFamily: '-apple-system, system-ui',
      fontSize: 17,
      color: sugg,
      letterSpacing: -0.43,
      lineHeight: '22px'
    }
  }, w)))), /*#__PURE__*/React.createElement("div", {
    style: {
      display: 'flex',
      flexDirection: 'column',
      gap: 13,
      padding: '0 6.5px',
      width: '100%',
      boxSizing: 'border-box',
      position: 'relative'
    }
  }, row(['q', 'w', 'e', 'r', 't', 'y', 'u', 'i', 'o', 'p']), row(['a', 's', 'd', 'f', 'g', 'h', 'j', 'k', 'l'], 20), /*#__PURE__*/React.createElement("div", {
    style: {
      display: 'flex',
      gap: 14.25,
      alignItems: 'center'
    }
  }, key(icons.shift, {
    w: 45,
    k: 'shift'
  }), /*#__PURE__*/React.createElement("div", {
    style: {
      display: 'flex',
      gap: 6.5,
      flex: 1
    }
  }, ['z', 'x', 'c', 'v', 'b', 'n', 'm'].map(l => key(l, {
    flex: true,
    k: l
  }))), key(icons.del, {
    w: 45,
    k: 'del'
  })), /*#__PURE__*/React.createElement("div", {
    style: {
      display: 'flex',
      gap: 6,
      alignItems: 'center'
    }
  }, key('ABC', {
    w: 92.25,
    fs: 18,
    k: 'abc'
  }), key('', {
    flex: true,
    k: 'space'
  }), key(icons.ret, {
    w: 92.25,
    ret: true,
    k: 'ret'
  }))), /*#__PURE__*/React.createElement("div", {
    style: {
      height: 56,
      width: '100%',
      position: 'relative'
    }
  }));
}
Object.assign(window, {
  IOSDevice,
  IOSStatusBar,
  IOSNavBar,
  IOSGlassPill,
  IOSList,
  IOSListRow,
  IOSKeyboard
});
})(); } catch (e) { __ds_ns.__errors.push({ path: "ui_kits/polaris-mobile/ios-frame.jsx", error: String((e && e.message) || e) }); }

// ui_kits/polaris-web/Atlas.jsx
try { (() => {
function Atlas() {
  const {
    Card,
    Stat,
    LineChart,
    BarChart,
    Select,
    Button,
    Badge,
    Icon
  } = window.PolarisDesignSystem_b0ab94;
  const A = PD.atlas;
  return /*#__PURE__*/React.createElement("div", null, /*#__PURE__*/React.createElement(PageHeader, {
    eyebrow: "Atlas",
    coord: "SEMANA 39",
    title: "Progresi\xF3n",
    badge: /*#__PURE__*/React.createElement(Maqueta, null),
    actions: /*#__PURE__*/React.createElement(Button, {
      icon: "play"
    }, "Empezar ", A.siguiente.rutina)
  }), /*#__PURE__*/React.createElement("div", {
    className: "grid"
  }, /*#__PURE__*/React.createElement("div", {
    className: "span-12 stats pl-rise"
  }, /*#__PURE__*/React.createElement(Stat, {
    label: "Sesiones \xB7 sep",
    value: 11,
    delta: "+2",
    caption: "vs agosto"
  }), /*#__PURE__*/React.createElement(Stat, {
    label: "Volumen semana",
    value: 25.3,
    decimals: 1,
    unit: "t",
    delta: "+15 %"
  }), /*#__PURE__*/React.createElement(Stat, {
    label: "1RM est. press banca",
    value: 80,
    unit: "kg",
    delta: "+2,5 kg",
    caption: "Epley"
  }), /*#__PURE__*/React.createElement(Stat, {
    label: "Peso corporal",
    value: PD.nucleo.peso,
    decimals: 1,
    unit: "kg",
    caption: "desde N\xFAcleo"
  })), /*#__PURE__*/React.createElement("div", {
    className: "span-8"
  }, /*#__PURE__*/React.createElement(Card, {
    delay: 100,
    eyebrow: "1RM estimado",
    title: "Press banca",
    action: /*#__PURE__*/React.createElement("div", {
      style: {
        width: 170
      }
    }, /*#__PURE__*/React.createElement(Select, {
      size: "sm",
      options: ['Press banca', 'Sentadilla', 'Peso muerto', 'Dominadas']
    }))
  }, /*#__PURE__*/React.createElement(LineChart, {
    height: 220,
    showLegend: true,
    labels: A.semanas.filter((_, i) => i % 3 === 0),
    format: v => Math.round(v) + '',
    series: [{
      name: '1RM estimado (kg)',
      points: A.progresion
    }, {
      name: 'Objetivo 85 kg',
      points: A.progresion.map(() => 85),
      dashed: true,
      color: 'var(--text-3)'
    }]
  }))), /*#__PURE__*/React.createElement("div", {
    className: "span-4"
  }, /*#__PURE__*/React.createElement(Card, {
    delay: 160,
    eyebrow: "Mejores marcas",
    title: "R\xE9cords",
    padding: "4px 0 8px"
  }, A.records.map((r, i) => /*#__PURE__*/React.createElement("div", {
    key: r.ej,
    className: "rec pl-rise",
    style: {
      animationDelay: 200 + i * 60 + 'ms'
    }
  }, /*#__PURE__*/React.createElement("div", {
    className: "stack-4"
  }, /*#__PURE__*/React.createElement("b", null, r.ej), /*#__PURE__*/React.createElement("span", {
    className: "pl-row__num"
  }, r.fecha)), /*#__PURE__*/React.createElement("span", {
    className: "money"
  }, r.v), r.nuevo && /*#__PURE__*/React.createElement(Badge, {
    tone: "accent",
    variant: "solid"
  }, "Nuevo"))))), /*#__PURE__*/React.createElement("div", {
    className: "span-7"
  }, /*#__PURE__*/React.createElement(Card, {
    delay: 220,
    eyebrow: "Historial",
    title: "\xDAltimas sesiones",
    padding: "8px 0 0"
  }, /*#__PURE__*/React.createElement("div", {
    className: "table"
  }, A.sesiones.map((s, i) => /*#__PURE__*/React.createElement("div", {
    key: i,
    className: "table__r table__r--5 pl-rise",
    style: {
      animationDelay: 240 + i * 40 + 'ms'
    }
  }, /*#__PURE__*/React.createElement("span", {
    className: "pl-row__num"
  }, s.fecha), /*#__PURE__*/React.createElement("b", null, s.rutina, s.rutina === 'Improvisado' && /*#__PURE__*/React.createElement(Badge, {
    variant: "outline",
    style: {
      marginLeft: 8
    }
  }, "Sin rutina")), /*#__PURE__*/React.createElement("span", {
    className: "muted"
  }, s.ej, " ejercicios"), /*#__PURE__*/React.createElement("span", {
    className: "muted"
  }, s.series, " series"), /*#__PURE__*/React.createElement("span", {
    className: "money"
  }, s.vol.toLocaleString('es-ES'), " kg")))))), /*#__PURE__*/React.createElement("div", {
    className: "span-5"
  }, /*#__PURE__*/React.createElement(Card, {
    delay: 280,
    eyebrow: "Volumen",
    title: "Toneladas por semana"
  }, /*#__PURE__*/React.createElement(BarChart, {
    height: 170,
    data: A.volumen.map((v, i) => ({
      label: A.semanas[i],
      value: v
    })),
    highlight: 9,
    format: v => v.toLocaleString('es-ES') + ' t'
  })))));
}
window.Atlas = Atlas;
})(); } catch (e) { __ds_ns.__errors.push({ path: "ui_kits/polaris-web/Atlas.jsx", error: String((e && e.message) || e) }); }

// ui_kits/polaris-web/Dashboard.jsx
try { (() => {
// Inicio — layout "bandas": un panel de módulos (una fila por módulo) + columna de actividad.
function Band({
  mod,
  icon,
  name,
  sub,
  mock,
  go,
  to,
  children,
  aside
}) {
  const {
    Icon,
    Badge
  } = window.PolarisDesignSystem_b0ab94;
  return /*#__PURE__*/React.createElement("div", {
    className: "band",
    "data-module": mod,
    onClick: to ? () => go(to) : undefined,
    style: {
      cursor: to ? 'pointer' : 'default'
    }
  }, /*#__PURE__*/React.createElement("div", {
    className: "band__id"
  }, /*#__PURE__*/React.createElement("span", {
    className: "band__ic"
  }, /*#__PURE__*/React.createElement(Icon, {
    name: icon,
    size: 18
  })), /*#__PURE__*/React.createElement("div", {
    className: "stack-4",
    style: {
      minWidth: 0
    }
  }, /*#__PURE__*/React.createElement("b", {
    className: "band__name"
  }, name, to && /*#__PURE__*/React.createElement(Icon, {
    name: "arrow-up-right",
    size: 14,
    className: "band__go"
  })), /*#__PURE__*/React.createElement("span", {
    className: "pl-eyebrow"
  }, sub), mock && /*#__PURE__*/React.createElement("span", {
    title: "Datos inventados: el m\xF3dulo a\xFAn no tiene backend",
    style: {
      marginTop: 6
    }
  }, /*#__PURE__*/React.createElement(Badge, {
    variant: "outline",
    color: "var(--text-3)"
  }, "Maqueta")))), /*#__PURE__*/React.createElement("div", {
    className: "band__main"
  }, children), /*#__PURE__*/React.createElement("div", {
    className: "band__aside"
  }, aside));
}
function Dashboard({
  go
}) {
  const {
    Card,
    Stat,
    ProgressBar,
    BarChart,
    LineChart,
    RingChart,
    StateGlyph,
    TypeTag,
    Button,
    Icon
  } = window.PolarisDesignSystem_b0ab94;
  const h = new Date().getHours();
  const saludo = h < 13 ? 'Buenos días' : h < 21 ? 'Buenas tardes' : 'Buenas noches';
  const fecha = new Date().toLocaleDateString('es-ES', {
    weekday: 'long',
    day: 'numeric',
    month: 'long'
  });
  const E = PD.entradas,
    enCurso = E.filter(e => e.estado === 'EN_CURSO');
  const K = PD.kuiper,
    Fu = PD.fusion,
    A = PD.atlas,
    N = PD.nucleo;
  const cnt = s => E.filter(e => e.estado === s).length;
  const dias = ['L', 'M', 'X', 'J', 'V', 'S', 'D'];
  const peor = [...K.categorias].sort((a, b) => b.gastado / b.limite - a.gastado / a.limite)[0];
  return /*#__PURE__*/React.createElement("div", {
    className: "dash"
  }, /*#__PURE__*/React.createElement(PageHeader, {
    eyebrow: "Inicio",
    coord: fecha.toUpperCase(),
    title: /*#__PURE__*/React.createElement(React.Fragment, null, saludo, ", ", /*#__PURE__*/React.createElement("span", {
      style: {
        color: 'var(--accent)'
      }
    }, PD.user.nombre)),
    actions: /*#__PURE__*/React.createElement(Button, {
      variant: "secondary",
      icon: "plus",
      onClick: () => go('odisea')
    }, "A\xF1adir")
  }), /*#__PURE__*/React.createElement("div", {
    className: "stats stats--today pl-rise",
    style: {
      animationDelay: '60ms',
      marginBottom: 24
    }
  }, /*#__PURE__*/React.createElement("div", {
    "data-module": "kuiper",
    className: "today",
    onClick: () => go('kuiper')
  }, /*#__PURE__*/React.createElement("span", {
    className: "today__k"
  }, /*#__PURE__*/React.createElement(Icon, {
    name: "wallet",
    size: 14
  }), "Gastado hoy"), /*#__PURE__*/React.createElement(Stat, {
    value: K.dias[K.dias.length - 1],
    decimals: 2,
    unit: "\u20AC",
    size: 34,
    caption: 'media ' + eur(K.gastado / K.dias.length)
  })), /*#__PURE__*/React.createElement("div", {
    "data-module": "fusion",
    className: "today",
    onClick: () => go('fusion')
  }, /*#__PURE__*/React.createElement("span", {
    className: "today__k"
  }, /*#__PURE__*/React.createElement(Icon, {
    name: "flame",
    size: 14
  }), "Te quedan"), /*#__PURE__*/React.createElement(Stat, {
    value: Fu.objetivo - Fu.kcal,
    unit: "kcal",
    size: 34,
    caption: "falta la cena"
  })), /*#__PURE__*/React.createElement("div", {
    "data-module": "atlas",
    className: "today",
    onClick: () => go('atlas')
  }, /*#__PURE__*/React.createElement("span", {
    className: "today__k"
  }, /*#__PURE__*/React.createElement(Icon, {
    name: "dumbbell",
    size: 14
  }), "Hoy toca"), /*#__PURE__*/React.createElement(Stat, {
    value: A.siguiente.rutina,
    size: 34,
    caption: 'última: ' + A.ultima.rutina + ', ' + A.ultima.dia.toLowerCase()
  })), /*#__PURE__*/React.createElement("div", {
    "data-module": "odisea",
    className: "today",
    onClick: () => go('odisea')
  }, /*#__PURE__*/React.createElement("span", {
    className: "today__k"
  }, /*#__PURE__*/React.createElement(Icon, {
    name: "clapperboard",
    size: 14
  }), "En curso"), /*#__PURE__*/React.createElement(Stat, {
    value: enCurso.length,
    size: 34,
    caption: enCurso.map(e => e.titulo).join(' · ')
  }))), /*#__PURE__*/React.createElement("div", {
    className: "dash__cols"
  }, /*#__PURE__*/React.createElement("section", {
    className: "bands pl-rise",
    style: {
      animationDelay: '120ms'
    }
  }, /*#__PURE__*/React.createElement(Band, {
    mod: "odisea",
    icon: "clapperboard",
    name: "Odisea",
    sub: "Ocio",
    go: go,
    to: "odisea",
    aside: /*#__PURE__*/React.createElement("div", {
      className: "states"
    }, [['EN_CURSO', 'En curso'], ['PENDIENTE', 'Pendiente'], ['TERMINADO', 'Terminado'], ['ABANDONADO', 'Abandonado']].map(([s, l]) => /*#__PURE__*/React.createElement("span", {
      key: s
    }, /*#__PURE__*/React.createElement(StateGlyph, {
      estado: s
    }), /*#__PURE__*/React.createElement("b", null, cnt(s)), l)))
  }, /*#__PURE__*/React.createElement("div", {
    className: "stack-12"
  }, enCurso.map(e => /*#__PURE__*/React.createElement("div", {
    key: e.id,
    className: "prow"
  }, /*#__PURE__*/React.createElement(TypeTag, {
    tipo: e.tipo,
    showLabel: false,
    size: 15
  }), /*#__PURE__*/React.createElement("b", null, e.titulo), /*#__PURE__*/React.createElement(ProgressBar, {
    value: e.progreso,
    max: e.tipo === 'LIBRO' ? e.duracionMin : 62,
    valueLabel: false
  }), /*#__PURE__*/React.createElement("span", {
    className: "pl-row__num"
  }, e.tipo === 'LIBRO' ? `pág. ${e.progreso}/${e.duracionMin}` : `ep. ${e.progreso}`))))), /*#__PURE__*/React.createElement(Band, {
    mod: "kuiper",
    icon: "wallet",
    name: "Kuiper",
    sub: "Septiembre",
    mock: true,
    go: go,
    to: "kuiper",
    aside: /*#__PURE__*/React.createElement(BarChart, {
      height: 64,
      gap: 3,
      data: K.dias.slice(-14).map(v => ({
        label: '',
        value: v
      })),
      highlight: 13,
      format: v => eur(v),
      showAxis: false,
      gridLines: 2
    })
  }, /*#__PURE__*/React.createElement("div", {
    className: "bmain"
  }, /*#__PURE__*/React.createElement(Stat, {
    value: K.gastado,
    decimals: 2,
    unit: "\u20AC",
    size: 36
  }), /*#__PURE__*/React.createElement("div", {
    className: "stack-8",
    style: {
      flex: 1
    }
  }, /*#__PURE__*/React.createElement(ProgressBar, {
    value: K.gastado,
    max: K.presupuesto,
    target: K.presupuesto * 25 / 30,
    valueLabel: `de ${eur(K.presupuesto, 0)}`,
    label: `Quedan ${eur(K.presupuesto - K.gastado)}`
  }), /*#__PURE__*/React.createElement("span", {
    className: "muted",
    style: {
      fontSize: 12.5
    }
  }, peor.nombre, " va por encima: ", eur(peor.gastado, 0), " de ", eur(peor.limite, 0))))), /*#__PURE__*/React.createElement(Band, {
    mod: "fusion",
    icon: "flame",
    name: "Fusi\xF3n",
    sub: "Hoy",
    mock: true,
    go: go,
    to: "fusion",
    aside: /*#__PURE__*/React.createElement("div", {
      style: {
        display: 'flex',
        justifyContent: 'flex-end'
      }
    }, /*#__PURE__*/React.createElement(RingChart, {
      value: Fu.kcal,
      max: Fu.objetivo,
      size: 84,
      thickness: 8,
      label: Fu.kcal.toLocaleString('es-ES'),
      sublabel: "KCAL"
    }))
  }, /*#__PURE__*/React.createElement("div", {
    className: "macros3"
  }, Fu.macros.map(m => /*#__PURE__*/React.createElement(ProgressBar, {
    key: m.nombre,
    label: m.nombre,
    value: m.g,
    max: m.obj,
    valueLabel: m.g + '/' + m.obj + ' g'
  })))), /*#__PURE__*/React.createElement(Band, {
    mod: "atlas",
    icon: "dumbbell",
    name: "Atlas",
    sub: "Semana 39",
    mock: true,
    go: go,
    to: "atlas",
    aside: /*#__PURE__*/React.createElement("div", {
      className: "week week--sm"
    }, dias.map((d, i) => /*#__PURE__*/React.createElement("div", {
      key: d,
      className: 'week__d' + (PD.semanaAtlas[i] ? ' on' : '') + (i === 4 ? ' today' : '')
    }, /*#__PURE__*/React.createElement("span", null, d), /*#__PURE__*/React.createElement("i", null))))
  }, /*#__PURE__*/React.createElement("div", {
    className: "bmain"
  }, /*#__PURE__*/React.createElement("div", {
    className: "stack-4"
  }, /*#__PURE__*/React.createElement("span", {
    className: "pl-eyebrow"
  }, "\xDAltima \xB7 ", A.ultima.dia), /*#__PURE__*/React.createElement("b", {
    className: "big"
  }, A.ultima.rutina), /*#__PURE__*/React.createElement("span", {
    className: "muted",
    style: {
      fontSize: 12.5
    }
  }, A.ultima.series, " series \xB7 ", A.ultima.volumen.toLocaleString('es-ES'), " kg")), /*#__PURE__*/React.createElement("div", {
    className: "stack-4 next"
  }, /*#__PURE__*/React.createElement("span", {
    className: "pl-eyebrow",
    style: {
      color: 'var(--accent)'
    }
  }, "Siguiente \xB7 ", A.siguiente.dia), /*#__PURE__*/React.createElement("b", {
    className: "big"
  }, A.siguiente.rutina)), /*#__PURE__*/React.createElement(Button, {
    size: "sm",
    icon: "play",
    onClick: e => {
      e.stopPropagation();
      go('atlas');
    }
  }, "Empezar"))), /*#__PURE__*/React.createElement(Band, {
    mod: "nucleo",
    icon: "orbit",
    name: "N\xFAcleo",
    sub: "Peso",
    mock: true,
    go: go,
    aside: /*#__PURE__*/React.createElement(LineChart, {
      height: 56,
      series: [{
        name: 'Peso',
        points: N.serie
      }],
      showAxis: false,
      gridLines: 2
    })
  }, /*#__PURE__*/React.createElement("div", {
    className: "bmain"
  }, /*#__PURE__*/React.createElement(Stat, {
    value: N.peso,
    decimals: 1,
    unit: "kg",
    size: 36,
    delta: '−' + Math.abs(N.delta).toLocaleString('es-ES') + ' kg',
    deltaTone: "up",
    caption: "en 10 d\xEDas"
  })))), /*#__PURE__*/React.createElement("aside", {
    className: "dash__side"
  }, /*#__PURE__*/React.createElement(Card, {
    delay: 180,
    eyebrow: "Actividad",
    title: "Lo \xFAltimo",
    padding: "4px 0 6px"
  }, /*#__PURE__*/React.createElement("div", {
    className: "feed"
  }, PD.actividad.map((a, i) => /*#__PURE__*/React.createElement("div", {
    key: i,
    className: "feed__r feed__r--stack pl-rise",
    "data-module": a.mod,
    style: {
      animationDelay: 220 + i * 50 + 'ms'
    },
    onClick: () => go(a.mod === 'nucleo' ? 'inicio' : a.mod)
  }, /*#__PURE__*/React.createElement("span", {
    className: "feed__ic"
  }, /*#__PURE__*/React.createElement(Icon, {
    name: a.icon,
    size: 14
  })), /*#__PURE__*/React.createElement("span", {
    className: "stack-4",
    style: {
      minWidth: 0
    }
  }, /*#__PURE__*/React.createElement("span", {
    className: "feed__t"
  }, a.txt), /*#__PURE__*/React.createElement("span", {
    className: "pl-row__num"
  }, a.det)), /*#__PURE__*/React.createElement("span", {
    className: "pl-row__num feed__h"
  }, a.hora))))))));
}
window.Dashboard = Dashboard;
})(); } catch (e) { __ds_ns.__errors.push({ path: "ui_kits/polaris-web/Dashboard.jsx", error: String((e && e.message) || e) }); }

// ui_kits/polaris-web/DashboardGrid.jsx
try { (() => {
function DashboardGrid({
  go
}) {
  const {
    Card,
    Stat,
    ProgressBar,
    BarChart,
    LineChart,
    RingChart,
    StatusBadge,
    TypeTag,
    Rating,
    Button,
    Icon,
    Badge
  } = window.PolarisDesignSystem_b0ab94;
  const h = new Date().getHours();
  const saludo = h < 13 ? 'Buenos días' : h < 21 ? 'Buenas tardes' : 'Buenas noches';
  const fecha = new Date().toLocaleDateString('es-ES', {
    weekday: 'long',
    day: 'numeric',
    month: 'long'
  });
  const enCurso = PD.entradas.filter(e => e.estado === 'EN_CURSO');
  const pendientes = PD.entradas.filter(e => e.estado === 'PENDIENTE');
  const hecho = PD.entradas.filter(e => e.estado === 'TERMINADO');
  const K = PD.kuiper,
    Fu = PD.fusion,
    A = PD.atlas,
    N = PD.nucleo;
  const hoyGasto = K.dias[K.dias.length - 1];
  const top = [...K.categorias].sort((a, b) => b.gastado / b.limite - a.gastado / a.limite).slice(0, 3);
  const dias = ['L', 'M', 'X', 'J', 'V', 'S', 'D'];
  const colorMod = m => `var(--mod-${m})`;
  return /*#__PURE__*/React.createElement("div", null, /*#__PURE__*/React.createElement(PageHeader, {
    eyebrow: "Inicio",
    coord: fecha.toUpperCase(),
    title: /*#__PURE__*/React.createElement(React.Fragment, null, saludo, ", ", /*#__PURE__*/React.createElement("span", {
      style: {
        color: 'var(--accent)'
      }
    }, PD.user.nombre)),
    actions: /*#__PURE__*/React.createElement(Button, {
      variant: "secondary",
      icon: "plus",
      onClick: () => go('odisea')
    }, "A\xF1adir")
  }), /*#__PURE__*/React.createElement("div", {
    className: "grid"
  }, /*#__PURE__*/React.createElement("div", {
    className: "span-12 stats stats--today pl-rise",
    style: {
      animationDelay: '60ms'
    }
  }, /*#__PURE__*/React.createElement("div", {
    "data-module": "odisea",
    className: "today",
    onClick: () => go('odisea')
  }, /*#__PURE__*/React.createElement("span", {
    className: "today__k"
  }, /*#__PURE__*/React.createElement(Icon, {
    name: "clapperboard",
    size: 14
  }), "En curso"), /*#__PURE__*/React.createElement(Stat, {
    value: enCurso.length,
    size: 34,
    caption: pendientes.length + ' pendiente · ' + hecho.length + ' terminado'
  })), /*#__PURE__*/React.createElement("div", {
    "data-module": "kuiper",
    className: "today",
    onClick: () => go('kuiper')
  }, /*#__PURE__*/React.createElement("span", {
    className: "today__k"
  }, /*#__PURE__*/React.createElement(Icon, {
    name: "wallet",
    size: 14
  }), "Gastado hoy"), /*#__PURE__*/React.createElement(Stat, {
    value: hoyGasto,
    decimals: 2,
    unit: "\u20AC",
    size: 34,
    caption: 'media ' + eur(K.gastado / K.dias.length)
  })), /*#__PURE__*/React.createElement("div", {
    "data-module": "fusion",
    className: "today",
    onClick: () => go('fusion')
  }, /*#__PURE__*/React.createElement("span", {
    className: "today__k"
  }, /*#__PURE__*/React.createElement(Icon, {
    name: "flame",
    size: 14
  }), "Te quedan"), /*#__PURE__*/React.createElement(Stat, {
    value: Fu.objetivo - Fu.kcal,
    unit: "kcal",
    size: 34,
    caption: "falta la cena"
  })), /*#__PURE__*/React.createElement("div", {
    "data-module": "atlas",
    className: "today",
    onClick: () => go('atlas')
  }, /*#__PURE__*/React.createElement("span", {
    className: "today__k"
  }, /*#__PURE__*/React.createElement(Icon, {
    name: "dumbbell",
    size: 14
  }), "Hoy toca"), /*#__PURE__*/React.createElement(Stat, {
    value: A.siguiente.rutina,
    size: 34,
    caption: 'última: ' + A.ultima.rutina + ', ' + A.ultima.dia.toLowerCase()
  }))), /*#__PURE__*/React.createElement("div", {
    "data-module": "odisea",
    className: "span-5"
  }, /*#__PURE__*/React.createElement(Card, {
    delay: 100,
    interactive: true,
    onClick: () => go('odisea'),
    eyebrow: "Odisea \xB7 en curso",
    title: "Lo que tienes entre manos",
    action: /*#__PURE__*/React.createElement(Icon, {
      name: "arrow-up-right",
      size: 16,
      color: "var(--text-3)"
    })
  }, /*#__PURE__*/React.createElement("div", {
    className: "wfill"
  }, enCurso.map(e => /*#__PURE__*/React.createElement("div", {
    key: e.id,
    className: "wrow"
  }, /*#__PURE__*/React.createElement("span", {
    className: "wrow__ic"
  }, /*#__PURE__*/React.createElement(TypeTag, {
    tipo: e.tipo,
    showLabel: false,
    size: 16
  })), /*#__PURE__*/React.createElement("div", {
    style: {
      flex: 1,
      minWidth: 0
    }
  }, /*#__PURE__*/React.createElement("div", {
    className: "wrow__t"
  }, /*#__PURE__*/React.createElement("b", null, e.titulo), /*#__PURE__*/React.createElement("span", null, e.tipo === 'LIBRO' ? `pág. ${e.progreso} / ${e.duracionMin}` : `episodio ${e.progreso}`)), e.tipo === 'LIBRO' ? /*#__PURE__*/React.createElement(ProgressBar, {
    value: e.progreso,
    max: e.duracionMin,
    valueLabel: false
  }) : /*#__PURE__*/React.createElement(ProgressBar, {
    value: e.progreso,
    max: 62,
    valueLabel: false
  })))), /*#__PURE__*/React.createElement("div", {
    className: "wsub"
  }, /*#__PURE__*/React.createElement("span", {
    className: "pl-eyebrow"
  }, "Recientes")), [...hecho, ...pendientes].map(e => /*#__PURE__*/React.createElement("div", {
    key: e.id,
    className: "wmini"
  }, /*#__PURE__*/React.createElement(StatusBadge, {
    estado: e.estado,
    variant: "dot"
  }), /*#__PURE__*/React.createElement("b", null, e.titulo), /*#__PURE__*/React.createElement("span", {
    className: "pl-row__num"
  }, e.anio), e.valoracion != null ? /*#__PURE__*/React.createElement(Rating, {
    value: e.valoracion,
    size: 11
  }) : /*#__PURE__*/React.createElement("span", {
    className: "pl-row__num"
  }, "\u2014")))))), /*#__PURE__*/React.createElement("div", {
    "data-module": "kuiper",
    className: "span-4"
  }, /*#__PURE__*/React.createElement(Card, {
    delay: 140,
    interactive: true,
    onClick: () => go('kuiper'),
    eyebrow: "Kuiper \xB7 septiembre",
    title: "Gastado este mes",
    action: /*#__PURE__*/React.createElement(Maqueta, null)
  }, /*#__PURE__*/React.createElement("div", {
    className: "wfill"
  }, /*#__PURE__*/React.createElement(Stat, {
    value: K.gastado,
    decimals: 2,
    unit: "\u20AC",
    size: 44,
    caption: `de ${eur(K.presupuesto, 0)} · quedan ${eur(K.presupuesto - K.gastado)}`
  }), /*#__PURE__*/React.createElement(ProgressBar, {
    value: K.gastado,
    max: K.presupuesto,
    target: K.presupuesto * 25 / 30,
    valueLabel: false,
    size: "lg"
  }), /*#__PURE__*/React.createElement("div", {
    className: "stack-8"
  }, top.map(c => /*#__PURE__*/React.createElement(ProgressBar, {
    key: c.nombre,
    label: c.nombre,
    value: c.gastado,
    max: c.limite,
    valueLabel: eur(c.gastado, 0) + ' / ' + eur(c.limite, 0)
  }))), /*#__PURE__*/React.createElement("div", {
    className: "wbottom"
  }, /*#__PURE__*/React.createElement(BarChart, {
    height: 64,
    gap: 3,
    data: K.dias.slice(-14).map((v, i) => ({
      label: '',
      value: v
    })),
    highlight: 13,
    format: v => eur(v),
    showAxis: false,
    gridLines: 2
  }))))), /*#__PURE__*/React.createElement("div", {
    "data-module": "fusion",
    className: "span-3"
  }, /*#__PURE__*/React.createElement(Card, {
    delay: 180,
    interactive: true,
    onClick: () => go('fusion'),
    eyebrow: "Fusi\xF3n \xB7 hoy",
    title: "Macros",
    action: /*#__PURE__*/React.createElement(Maqueta, null)
  }, /*#__PURE__*/React.createElement("div", {
    className: "wfill",
    style: {
      alignItems: 'stretch'
    }
  }, /*#__PURE__*/React.createElement("div", {
    style: {
      display: 'flex',
      justifyContent: 'center',
      padding: '4px 0'
    }
  }, /*#__PURE__*/React.createElement(RingChart, {
    value: Fu.kcal,
    max: Fu.objetivo,
    size: 148,
    thickness: 12,
    label: Fu.kcal.toLocaleString('es-ES'),
    sublabel: '/ ' + Fu.objetivo.toLocaleString('es-ES') + ' KCAL'
  })), /*#__PURE__*/React.createElement("div", {
    className: "stack-12 wbottom"
  }, Fu.macros.map(m => /*#__PURE__*/React.createElement(ProgressBar, {
    key: m.nombre,
    label: m.nombre,
    value: m.g,
    max: m.obj,
    valueLabel: m.g + ' / ' + m.obj + ' g'
  })))))), /*#__PURE__*/React.createElement("div", {
    "data-module": "atlas",
    className: "span-5"
  }, /*#__PURE__*/React.createElement(Card, {
    delay: 220,
    interactive: true,
    onClick: () => go('atlas'),
    eyebrow: "Atlas \xB7 semana 39",
    title: "Entrenamiento",
    action: /*#__PURE__*/React.createElement(Maqueta, null)
  }, /*#__PURE__*/React.createElement("div", {
    className: "wfill"
  }, /*#__PURE__*/React.createElement("div", {
    className: "split"
  }, /*#__PURE__*/React.createElement("div", {
    className: "stack-8"
  }, /*#__PURE__*/React.createElement("span", {
    className: "pl-eyebrow"
  }, "\xDAltima \xB7 ", A.ultima.dia), /*#__PURE__*/React.createElement("b", {
    className: "big"
  }, A.ultima.rutina), /*#__PURE__*/React.createElement("span", {
    className: "muted",
    style: {
      fontSize: 13
    }
  }, A.ultima.ejercicios, " ejercicios \xB7 ", A.ultima.series, " series \xB7 ", A.ultima.volumen.toLocaleString('es-ES'), " kg")), /*#__PURE__*/React.createElement("div", {
    className: "stack-8 next"
  }, /*#__PURE__*/React.createElement("span", {
    className: "pl-eyebrow",
    style: {
      color: 'var(--accent)'
    }
  }, "Siguiente \xB7 ", A.siguiente.dia), /*#__PURE__*/React.createElement("b", {
    className: "big"
  }, A.siguiente.rutina), /*#__PURE__*/React.createElement(Button, {
    size: "sm",
    icon: "play",
    onClick: e => {
      e.stopPropagation();
      go('atlas');
    }
  }, "Empezar"))), /*#__PURE__*/React.createElement("div", {
    className: "week"
  }, dias.map((d, i) => /*#__PURE__*/React.createElement("div", {
    key: d,
    className: 'week__d' + (PD.semanaAtlas[i] ? ' on' : '') + (i === 4 ? ' today' : ''),
    style: {
      animationDelay: 260 + i * 40 + 'ms'
    }
  }, /*#__PURE__*/React.createElement("span", null, d), /*#__PURE__*/React.createElement("i", null)))), /*#__PURE__*/React.createElement("div", {
    className: "wbottom"
  }, /*#__PURE__*/React.createElement(BarChart, {
    height: 72,
    data: A.volumen.map((v, i) => ({
      label: A.semanas[i],
      value: v
    })),
    highlight: 9,
    gap: 4,
    format: v => v.toLocaleString('es-ES') + ' t',
    gridLines: 2
  }))))), /*#__PURE__*/React.createElement("div", {
    "data-module": "nucleo",
    className: "span-3"
  }, /*#__PURE__*/React.createElement(Card, {
    delay: 260,
    eyebrow: "N\xFAcleo",
    title: "Peso",
    action: /*#__PURE__*/React.createElement(Maqueta, null)
  }, /*#__PURE__*/React.createElement("div", {
    className: "wfill"
  }, /*#__PURE__*/React.createElement(Stat, {
    value: N.peso,
    decimals: 1,
    unit: "kg",
    size: 44,
    delta: '−' + Math.abs(N.delta).toLocaleString('es-ES') + ' kg',
    deltaTone: "up",
    caption: "en 10 d\xEDas"
  }), /*#__PURE__*/React.createElement("div", {
    className: "wbottom"
  }, /*#__PURE__*/React.createElement(LineChart, {
    height: 120,
    series: [{
      name: 'Peso',
      points: N.serie
    }],
    labels: ['15 sep', '20 sep', '25 sep'],
    format: v => v.toFixed(1).replace('.', ','),
    gridLines: 2
  }))))), /*#__PURE__*/React.createElement("div", {
    className: "span-4"
  }, /*#__PURE__*/React.createElement(Card, {
    delay: 300,
    eyebrow: "Actividad",
    title: "Lo \xFAltimo",
    padding: "4px 0 6px"
  }, /*#__PURE__*/React.createElement("div", {
    className: "feed"
  }, PD.actividad.map((a, i) => /*#__PURE__*/React.createElement("div", {
    key: i,
    className: "feed__r pl-rise",
    "data-module": a.mod,
    style: {
      animationDelay: 340 + i * 50 + 'ms'
    },
    onClick: () => go(a.mod === 'nucleo' ? 'inicio' : a.mod)
  }, /*#__PURE__*/React.createElement("span", {
    className: "feed__ic"
  }, /*#__PURE__*/React.createElement(Icon, {
    name: a.icon,
    size: 14
  })), /*#__PURE__*/React.createElement("span", {
    className: "feed__t"
  }, a.txt), /*#__PURE__*/React.createElement("span", {
    className: "pl-row__num"
  }, a.det), /*#__PURE__*/React.createElement("span", {
    className: "pl-row__num feed__h"
  }, a.hora))))))));
}
window.DashboardGrid = DashboardGrid;
})(); } catch (e) { __ds_ns.__errors.push({ path: "ui_kits/polaris-web/DashboardGrid.jsx", error: String((e && e.message) || e) }); }

// ui_kits/polaris-web/Fusion.jsx
try { (() => {
function Fusion() {
  const {
    Card,
    RingChart,
    ProgressBar,
    LineChart,
    Stat,
    Button,
    IconButton,
    Icon
  } = window.PolarisDesignSystem_b0ab94;
  const Fu = PD.fusion;
  const colors = ['var(--accent)', 'var(--mod-kuiper)', 'var(--mod-nucleo)'];
  return /*#__PURE__*/React.createElement("div", null, /*#__PURE__*/React.createElement(PageHeader, {
    eyebrow: "Fusi\xF3n",
    coord: "JUEVES 25 SEP",
    title: "Hoy",
    badge: /*#__PURE__*/React.createElement(Maqueta, null),
    actions: /*#__PURE__*/React.createElement(Button, {
      icon: "plus"
    }, "Registrar comida")
  }), /*#__PURE__*/React.createElement("div", {
    className: "grid"
  }, /*#__PURE__*/React.createElement("div", {
    className: "span-5"
  }, /*#__PURE__*/React.createElement(Card, {
    delay: 60,
    eyebrow: "Energ\xEDa",
    title: "Calor\xEDas del d\xEDa"
  }, /*#__PURE__*/React.createElement("div", {
    style: {
      display: 'flex',
      gap: 28,
      alignItems: 'center'
    }
  }, /*#__PURE__*/React.createElement(RingChart, {
    value: Fu.kcal,
    max: Fu.objetivo,
    size: 168,
    thickness: 14,
    label: Fu.kcal.toLocaleString('es-ES'),
    sublabel: 'DE ' + Fu.objetivo.toLocaleString('es-ES') + ' KCAL'
  }), /*#__PURE__*/React.createElement("div", {
    className: "stack-16",
    style: {
      flex: 1
    }
  }, /*#__PURE__*/React.createElement(Stat, {
    label: "Te quedan",
    value: Fu.objetivo - Fu.kcal,
    unit: "kcal",
    size: 32
  }), /*#__PURE__*/React.createElement("span", {
    className: "muted",
    style: {
      fontSize: 13
    }
  }, "Objetivo vigente desde el 1 de septiembre."))))), /*#__PURE__*/React.createElement("div", {
    className: "span-7"
  }, /*#__PURE__*/React.createElement(Card, {
    delay: 120,
    eyebrow: "Macros",
    title: "Contra objetivo"
  }, /*#__PURE__*/React.createElement("div", {
    className: "macros"
  }, Fu.macros.map((m, i) => /*#__PURE__*/React.createElement("div", {
    key: m.nombre,
    className: "macro"
  }, /*#__PURE__*/React.createElement(RingChart, {
    value: m.g,
    max: m.obj,
    size: 104,
    thickness: 8,
    color: colors[i],
    label: m.g,
    sublabel: '/ ' + m.obj + ' G'
  }), /*#__PURE__*/React.createElement("b", null, m.nombre), /*#__PURE__*/React.createElement("span", {
    className: "pl-row__num"
  }, Math.round(m.g / m.obj * 100), " %")))))), /*#__PURE__*/React.createElement("div", {
    className: "span-6"
  }, /*#__PURE__*/React.createElement(Card, {
    delay: 180,
    eyebrow: "Registro",
    title: "Comidas",
    padding: "4px 0 8px"
  }, Fu.comidas.map((c, i) => {
    const k = c.lineas.reduce((a, l) => a + l[2], 0);
    return /*#__PURE__*/React.createElement("div", {
      key: c.momento,
      className: "meal pl-rise",
      style: {
        animationDelay: 200 + i * 60 + 'ms'
      }
    }, /*#__PURE__*/React.createElement("div", {
      className: "meal__h"
    }, /*#__PURE__*/React.createElement("b", null, c.momento), /*#__PURE__*/React.createElement("span", {
      className: "pl-row__num"
    }, c.hora), /*#__PURE__*/React.createElement("span", {
      className: "money"
    }, k ? k + ' kcal' : ''), !c.lineas.length && /*#__PURE__*/React.createElement(Button, {
      size: "sm",
      variant: "ghost",
      icon: "plus"
    }, "A\xF1adir")), c.lineas.map(l => /*#__PURE__*/React.createElement("div", {
      key: l[0],
      className: "meal__l"
    }, /*#__PURE__*/React.createElement("span", null, l[0]), /*#__PURE__*/React.createElement("span", {
      className: "pl-row__num"
    }, l[1], " g"), /*#__PURE__*/React.createElement("span", {
      className: "pl-row__num"
    }, l[2], " kcal"))));
  }))), /*#__PURE__*/React.createElement("div", {
    className: "span-6"
  }, /*#__PURE__*/React.createElement(Card, {
    delay: 240,
    eyebrow: "Tendencia",
    title: "\xDAltimos 14 d\xEDas"
  }, /*#__PURE__*/React.createElement(LineChart, {
    height: 220,
    showLegend: true,
    labels: ['12 sep', '18 sep', '25 sep'],
    format: v => Math.round(v / 100) / 10 + 'k',
    series: [{
      name: 'Kcal',
      points: Fu.semana
    }, {
      name: 'Objetivo',
      points: Fu.semana.map(() => Fu.objetivo),
      dashed: true,
      color: 'var(--text-3)'
    }]
  })))));
}
window.Fusion = Fusion;
})(); } catch (e) { __ds_ns.__errors.push({ path: "ui_kits/polaris-web/Fusion.jsx", error: String((e && e.message) || e) }); }

// ui_kits/polaris-web/Kuiper.jsx
try { (() => {
function Kuiper() {
  const {
    Stat,
    Card,
    BarChart,
    ProgressBar,
    Tabs,
    Select,
    Button,
    Icon,
    Badge
  } = window.PolarisDesignSystem_b0ab94;
  const K = PD.kuiper;
  const [tab, setTab] = React.useState('resumen');
  const media = K.gastado / K.dias.length;
  const Movs = ({
    rows
  }) => /*#__PURE__*/React.createElement("div", {
    className: "table"
  }, rows.map((m, i) => /*#__PURE__*/React.createElement("div", {
    key: i,
    className: "table__r pl-rise",
    style: {
      animationDelay: i * 40 + 'ms'
    }
  }, /*#__PURE__*/React.createElement("span", {
    className: "pl-row__num"
  }, m.fecha), /*#__PURE__*/React.createElement("b", null, m.concepto), /*#__PURE__*/React.createElement("span", {
    className: "muted"
  }, m.categoria), /*#__PURE__*/React.createElement("span", {
    className: "money",
    style: {
      color: m.tipo === 'INGRESO' ? 'var(--success)' : 'var(--text-1)'
    }
  }, m.tipo === 'INGRESO' ? '+' : '−', eur(m.importe)))));
  return /*#__PURE__*/React.createElement("div", null, /*#__PURE__*/React.createElement(PageHeader, {
    eyebrow: "Kuiper",
    coord: "SEPTIEMBRE 2026",
    title: "Gastos",
    badge: /*#__PURE__*/React.createElement(Maqueta, null),
    actions: /*#__PURE__*/React.createElement(React.Fragment, null, /*#__PURE__*/React.createElement("div", {
      style: {
        width: 190
      }
    }, /*#__PURE__*/React.createElement(Select, {
      size: "sm",
      icon: "calendar",
      options: ['Septiembre 2026', 'Agosto 2026', 'Julio 2026']
    })), /*#__PURE__*/React.createElement(Button, {
      icon: "plus"
    }, "Movimiento"))
  }), /*#__PURE__*/React.createElement(Tabs, {
    value: tab,
    onChange: setTab,
    items: [{
      value: 'resumen',
      label: 'Resumen'
    }, {
      value: 'mov',
      label: 'Movimientos'
    }],
    style: {
      marginBottom: 24
    }
  }), tab === 'resumen' ? /*#__PURE__*/React.createElement("div", {
    className: "grid"
  }, /*#__PURE__*/React.createElement("div", {
    className: "span-12 stats pl-rise"
  }, /*#__PURE__*/React.createElement(Stat, {
    label: "Gastado",
    value: K.gastado,
    decimals: 2,
    unit: "\u20AC",
    delta: "\u22128 %",
    deltaTone: "up",
    caption: "vs agosto"
  }), /*#__PURE__*/React.createElement(Stat, {
    label: "Ingresos",
    value: K.ingresos,
    decimals: 2,
    unit: "\u20AC"
  }), /*#__PURE__*/React.createElement(Stat, {
    label: "Balance",
    value: K.ingresos - K.gastado,
    decimals: 2,
    unit: "\u20AC",
    delta: "+12 %",
    caption: "vs agosto"
  }), /*#__PURE__*/React.createElement(Stat, {
    label: "Queda de presupuesto",
    value: K.presupuesto - K.gastado,
    decimals: 2,
    unit: "\u20AC",
    caption: 'para 5 días'
  })), /*#__PURE__*/React.createElement("div", {
    className: "span-8"
  }, /*#__PURE__*/React.createElement(Card, {
    delay: 100,
    eyebrow: "D\xEDa a d\xEDa",
    title: "Gasto diario",
    action: /*#__PURE__*/React.createElement("span", {
      className: "pl-row__num"
    }, "media ", eur(media))
  }, /*#__PURE__*/React.createElement(BarChart, {
    height: 200,
    gap: 5,
    highlight: 24,
    target: media,
    targetLabel: "Media",
    data: K.dias.map((v, i) => ({
      label: (i + 1) % 5 === 0 || i === 0 ? String(i + 1) : '',
      value: v
    })),
    format: v => eur(v)
  }))), /*#__PURE__*/React.createElement("div", {
    className: "span-4"
  }, /*#__PURE__*/React.createElement(Card, {
    delay: 160,
    eyebrow: "Presupuestos",
    title: "Por categor\xEDa"
  }, /*#__PURE__*/React.createElement("div", {
    className: "stack-16"
  }, K.categorias.map(c => /*#__PURE__*/React.createElement("div", {
    key: c.nombre,
    style: {
      display: 'flex',
      gap: 12,
      alignItems: 'center'
    }
  }, /*#__PURE__*/React.createElement("span", {
    className: "catic"
  }, /*#__PURE__*/React.createElement(Icon, {
    name: c.icon,
    size: 15
  })), /*#__PURE__*/React.createElement(ProgressBar, {
    style: {
      flex: 1
    },
    label: /*#__PURE__*/React.createElement(React.Fragment, null, c.nombre, c.gastado > c.limite && /*#__PURE__*/React.createElement(Badge, {
      tone: "danger",
      style: {
        marginLeft: 8,
        height: 18
      }
    }, "+", eur(c.gastado - c.limite, 0))),
    value: c.gastado,
    max: c.limite,
    valueLabel: eur(c.gastado, 0) + ' / ' + eur(c.limite, 0)
  })))))), /*#__PURE__*/React.createElement("div", {
    className: "span-12"
  }, /*#__PURE__*/React.createElement(Card, {
    delay: 220,
    eyebrow: "\xDAltimos",
    title: "Movimientos",
    action: /*#__PURE__*/React.createElement(Button, {
      variant: "ghost",
      size: "sm",
      iconRight: "arrow-right",
      onClick: () => setTab('mov')
    }, "Ver todos"),
    padding: "8px 0 0"
  }, /*#__PURE__*/React.createElement(Movs, {
    rows: K.movimientos.slice(0, 4)
  })))) : /*#__PURE__*/React.createElement(Card, {
    padding: "8px 0 0"
  }, /*#__PURE__*/React.createElement(Movs, {
    rows: [...K.movimientos, ...K.movimientos.map(m => ({
      ...m,
      fecha: m.fecha.replace('2', '1')
    }))]
  })));
}
window.Kuiper = Kuiper;
})(); } catch (e) { __ds_ns.__errors.push({ path: "ui_kits/polaris-web/Kuiper.jsx", error: String((e && e.message) || e) }); }

// ui_kits/polaris-web/Landing.jsx
try { (() => {
function Landing({
  onLogin
}) {
  const {
    Logo,
    Button,
    Input,
    Eyebrow
  } = window.PolarisDesignSystem_b0ab94;
  const [user, setUser] = React.useState('dani');
  const [pass, setPass] = React.useState('');
  const [err, setErr] = React.useState(null);
  const [loading, setLoading] = React.useState(null);
  const [leaving, setLeaving] = React.useState(false);
  const [now, setNow] = React.useState(new Date());
  React.useEffect(() => {
    const i = setInterval(() => setNow(new Date()), 1000);
    return () => clearInterval(i);
  }, []);
  const go = via => {
    if (via === 'pass' && pass.length < 4) {
      setErr('Usuario o contraseña incorrectos');
      return;
    }
    setErr(null);
    setLoading(via);
    setTimeout(() => {
      setLeaving(true);
      setTimeout(onLogin, 900);
    }, 650);
  };
  const mods = [['Odisea', 'Ocio', 'var(--mod-odisea)'], ['Kuiper', 'Gastos', 'var(--mod-kuiper)'], ['Fusión', 'Nutrición', 'var(--mod-fusion)'], ['Atlas', 'Gym', 'var(--mod-atlas)']];
  const time = now.toLocaleTimeString('es-ES', {
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit'
  });
  return /*#__PURE__*/React.createElement("div", {
    className: "lp",
    "data-module": "polaris"
  }, /*#__PURE__*/React.createElement(StarTrails, {
    anchor: () => {
      const c = document.querySelector('.lp__card'),
        h = document.querySelector('.lp__word');
      if (!c || !h) return null;
      const cr = c.getBoundingClientRect(),
        hr = h.getBoundingClientRect();
      return [(hr.right + cr.left) / 2, Math.max(70, cr.top - 70)];
    },
    speed: leaving ? 14 : loading ? 3 : 1
  }), /*#__PURE__*/React.createElement("div", {
    className: "lp__veil"
  }), /*#__PURE__*/React.createElement("header", {
    className: "lp__top pl-rise"
  }, /*#__PURE__*/React.createElement(Logo, {
    size: 30
  }), /*#__PURE__*/React.createElement("span", {
    className: "lp__coord"
  }, "\u03B1 UMi ", /*#__PURE__*/React.createElement("b", null, "\xB7"), " RA 02h 31m 49s ", /*#__PURE__*/React.createElement("b", null, "\xB7"), " Dec +89\xB0 15\u2032 51\u2033 ", /*#__PURE__*/React.createElement("b", null, "\xB7"), " ", time)), /*#__PURE__*/React.createElement("main", {
    className: 'lp__main' + (leaving ? ' lp__main--out' : '')
  }, /*#__PURE__*/React.createElement("section", {
    className: "lp__hero"
  }, /*#__PURE__*/React.createElement("div", {
    className: "pl-rise",
    style: {
      animationDelay: '80ms'
    }
  }, /*#__PURE__*/React.createElement(Eyebrow, {
    star: true
  }, "Tu norte, cada d\xEDa")), /*#__PURE__*/React.createElement("h1", {
    className: "lp__word pl-rise",
    style: {
      animationDelay: '140ms'
    }
  }, "Polaris"), /*#__PURE__*/React.createElement("p", {
    className: "lp__lead pl-rise",
    style: {
      animationDelay: '220ms'
    }
  }, "Lo que ves, lo que gastas, lo que comes y lo que entrenas. En un solo sitio, sin ruido."), /*#__PURE__*/React.createElement("ul", {
    className: "lp__mods"
  }, mods.map(([n, d, c], i) => /*#__PURE__*/React.createElement("li", {
    key: n,
    className: "pl-rise",
    style: {
      animationDelay: 300 + i * 70 + 'ms',
      '--c': c
    }
  }, /*#__PURE__*/React.createElement("i", null), /*#__PURE__*/React.createElement("b", null, n), /*#__PURE__*/React.createElement("span", null, d))))), /*#__PURE__*/React.createElement("section", {
    className: "lp__card pl-rise",
    style: {
      animationDelay: '260ms'
    }
  }, /*#__PURE__*/React.createElement("div", {
    className: "lp__cardhead"
  }, /*#__PURE__*/React.createElement("h2", null, "Entrar"), /*#__PURE__*/React.createElement("span", {
    className: "pl-eyebrow"
  }, "Sesi\xF3n personal")), /*#__PURE__*/React.createElement("form", {
    className: "lp__form",
    onSubmit: e => {
      e.preventDefault();
      go('pass');
    }
  }, /*#__PURE__*/React.createElement(Input, {
    label: "Usuario",
    icon: "user-round",
    value: user,
    onChange: e => setUser(e.target.value),
    autoComplete: "username"
  }), /*#__PURE__*/React.createElement(Input, {
    label: "Contrase\xF1a",
    icon: "key-round",
    type: "password",
    placeholder: "\u2022\u2022\u2022\u2022\u2022\u2022\u2022\u2022",
    value: pass,
    onChange: e => {
      setPass(e.target.value);
      setErr(null);
    },
    error: err,
    autoComplete: "current-password"
  }), /*#__PURE__*/React.createElement(Button, {
    type: "submit",
    size: "lg",
    block: true,
    iconRight: "arrow-right",
    loading: loading === 'pass'
  }, "Entrar")), /*#__PURE__*/React.createElement("div", {
    className: "lp__or"
  }, /*#__PURE__*/React.createElement("span", null, "o")), /*#__PURE__*/React.createElement(Button, {
    variant: "secondary",
    size: "lg",
    block: true,
    loading: loading === 'google',
    onClick: () => go('google')
  }, "Continuar con Google"), /*#__PURE__*/React.createElement("p", {
    className: "lp__fine"
  }, "Cualquier contrase\xF1a de 4+ caracteres sirve en esta maqueta."))), /*#__PURE__*/React.createElement("footer", {
    className: "lp__foot"
  }, /*#__PURE__*/React.createElement("span", null, "Uso personal \xB7 un solo usuario"), /*#__PURE__*/React.createElement("span", null, "Odisea \xB7 Kuiper \xB7 Fusi\xF3n \xB7 Atlas \xB7 N\xFAcleo")));
}
window.Landing = Landing;
})(); } catch (e) { __ds_ns.__errors.push({ path: "ui_kits/polaris-web/Landing.jsx", error: String((e && e.message) || e) }); }

// ui_kits/polaris-web/Odisea.jsx
try { (() => {
function _extends() { return _extends = Object.assign ? Object.assign.bind() : function (n) { for (var e = 1; e < arguments.length; e++) { var t = arguments[e]; for (var r in t) ({}).hasOwnProperty.call(t, r) && (n[r] = t[r]); } return n; }, _extends.apply(null, arguments); }
function Cover({
  e,
  big
}) {
  const {
    TypeTag
  } = window.PolarisDesignSystem_b0ab94;
  return /*#__PURE__*/React.createElement("div", {
    className: 'cover' + (big ? ' cover--big' : '')
  }, /*#__PURE__*/React.createElement("span", {
    className: "cover__star"
  }, "\u2726"), /*#__PURE__*/React.createElement(TypeTag, {
    tipo: e.tipo,
    showLabel: false,
    size: big ? 18 : 12
  }), /*#__PURE__*/React.createElement("b", null, e.titulo), /*#__PURE__*/React.createElement("span", null, e.anio || '—'));
}
function Detail({
  e,
  update
}) {
  const {
    StatusBadge,
    TypeTag,
    Rating,
    ProgressBar,
    Switch,
    Badge,
    Button,
    Eyebrow,
    SegmentedControl
  } = window.PolarisDesignSystem_b0ab94;
  const dur = e.duracionMin == null ? null : e.tipo === 'LIBRO' ? e.duracionMin + ' páginas' : e.duracionMin + ' min';
  const fmtD = d => d ? new Date(d).toLocaleDateString('es-ES', {
    day: 'numeric',
    month: 'short',
    year: 'numeric'
  }) : '—';
  return /*#__PURE__*/React.createElement("aside", {
    className: "detail",
    key: e.id
  }, /*#__PURE__*/React.createElement("div", {
    className: "detail__top"
  }, /*#__PURE__*/React.createElement(Cover, {
    e: e,
    big: true
  }), /*#__PURE__*/React.createElement("div", {
    className: "stack-8",
    style: {
      minWidth: 0
    }
  }, /*#__PURE__*/React.createElement(Eyebrow, {
    star: true,
    coord: e.fuenteExterna.replace('_', ' ')
  }, /*#__PURE__*/React.createElement(TypeTag, {
    tipo: e.tipo,
    size: 12
  })), /*#__PURE__*/React.createElement("h2", {
    className: "detail__h"
  }, e.titulo), e.tituloOriginal && e.tituloOriginal !== e.titulo && /*#__PURE__*/React.createElement("span", {
    className: "muted"
  }, e.tituloOriginal), /*#__PURE__*/React.createElement("span", {
    className: "detail__meta"
  }, [e.anio, dur].filter(Boolean).join(' · ') || 'Sin año ni duración'), /*#__PURE__*/React.createElement("span", {
    className: "muted",
    style: {
      fontSize: 13
    }
  }, e.generos))), e.sinopsis ? /*#__PURE__*/React.createElement("p", {
    className: "detail__syn"
  }, e.sinopsis) : /*#__PURE__*/React.createElement("p", {
    className: "detail__syn detail__syn--empty"
  }, "Sin sinopsis."), /*#__PURE__*/React.createElement("div", {
    className: "detail__block"
  }, /*#__PURE__*/React.createElement("span", {
    className: "pl-eyebrow"
  }, "Estado"), /*#__PURE__*/React.createElement(SegmentedControl, {
    value: e.estado,
    onChange: v => update({
      estado: v
    }),
    options: [{
      value: 'PENDIENTE',
      label: 'Pendiente'
    }, {
      value: 'EN_CURSO',
      label: 'En curso'
    }, {
      value: 'TERMINADO',
      label: 'Terminado'
    }, {
      value: 'ABANDONADO',
      label: 'Abandonado'
    }]
  })), e.estado === 'EN_CURSO' && e.progreso != null && /*#__PURE__*/React.createElement("div", {
    className: "detail__block"
  }, e.tipo === 'LIBRO' ? /*#__PURE__*/React.createElement(ProgressBar, {
    label: "Progreso",
    value: e.progreso,
    max: e.duracionMin,
    valueLabel: `pág. ${e.progreso} / ${e.duracionMin}`,
    size: "lg"
  }) : /*#__PURE__*/React.createElement("div", {
    className: "stack-8"
  }, /*#__PURE__*/React.createElement("span", {
    className: "pl-eyebrow"
  }, "Progreso"), /*#__PURE__*/React.createElement("span", {
    style: {
      display: 'flex',
      alignItems: 'center',
      gap: 12
    }
  }, /*#__PURE__*/React.createElement("b", {
    className: "big"
  }, "Episodio ", e.progreso), /*#__PURE__*/React.createElement(Button, {
    size: "sm",
    variant: "secondary",
    icon: "plus",
    onClick: () => update({
      progreso: e.progreso + 1
    })
  }, "1")))), /*#__PURE__*/React.createElement("div", {
    className: "detail__grid"
  }, /*#__PURE__*/React.createElement("div", {
    className: "stack-8"
  }, /*#__PURE__*/React.createElement("span", {
    className: "pl-eyebrow"
  }, "Valoraci\xF3n"), /*#__PURE__*/React.createElement(Rating, {
    value: e.valoracion,
    size: 20,
    onChange: v => update({
      valoracion: v
    })
  })), /*#__PURE__*/React.createElement("div", {
    className: "stack-8"
  }, /*#__PURE__*/React.createElement("span", {
    className: "pl-eyebrow"
  }, "Favorito"), /*#__PURE__*/React.createElement(Switch, {
    checked: e.favorito,
    onChange: v => update({
      favorito: v
    }),
    label: e.favorito ? 'Sí' : 'No'
  })), /*#__PURE__*/React.createElement("div", {
    className: "stack-8"
  }, /*#__PURE__*/React.createElement("span", {
    className: "pl-eyebrow"
  }, "Inicio"), /*#__PURE__*/React.createElement("span", {
    className: "detail__meta"
  }, fmtD(e.fechaInicio))), /*#__PURE__*/React.createElement("div", {
    className: "stack-8"
  }, /*#__PURE__*/React.createElement("span", {
    className: "pl-eyebrow"
  }, "Fin"), /*#__PURE__*/React.createElement("span", {
    className: "detail__meta"
  }, fmtD(e.fechaFin)))), /*#__PURE__*/React.createElement("div", {
    className: "detail__block"
  }, /*#__PURE__*/React.createElement("span", {
    className: "pl-eyebrow"
  }, "Notas"), e.notas ? /*#__PURE__*/React.createElement("p", {
    className: "detail__notes"
  }, e.notas) : /*#__PURE__*/React.createElement("p", {
    className: "detail__syn--empty",
    style: {
      margin: 0,
      fontSize: 13
    }
  }, "Sin notas todav\xEDa.")));
}
function AddDialog({
  open,
  onClose,
  onAdd
}) {
  const {
    Dialog,
    Input,
    SegmentedControl,
    TypeTag,
    Badge,
    Button
  } = window.PolarisDesignSystem_b0ab94;
  const [tipo, setTipo] = React.useState('ALL');
  const [q, setQ] = React.useState('');
  const res = PD.catalogo.filter(c => (tipo === 'ALL' || c.tipo === tipo) && c.titulo.toLowerCase().includes(q.toLowerCase()));
  return /*#__PURE__*/React.createElement(Dialog, {
    open: open,
    onClose: onClose,
    title: "A\xF1adir a Odisea",
    width: 560
  }, /*#__PURE__*/React.createElement("div", {
    className: "stack-12"
  }, /*#__PURE__*/React.createElement(Input, {
    icon: "search",
    autoFocus: true,
    placeholder: "Busca en TMDB, IGDB y OpenLibrary",
    value: q,
    onChange: e => setQ(e.target.value)
  }), /*#__PURE__*/React.createElement(SegmentedControl, {
    value: tipo,
    onChange: setTipo,
    options: [{
      value: 'ALL',
      label: 'Todo'
    }, {
      value: 'PELICULA',
      label: 'Pelis',
      icon: 'clapperboard'
    }, {
      value: 'SERIE',
      label: 'Series',
      icon: 'tv'
    }, {
      value: 'JUEGO',
      label: 'Juegos',
      icon: 'gamepad-2'
    }, {
      value: 'LIBRO',
      label: 'Libros',
      icon: 'book-open'
    }]
  }), /*#__PURE__*/React.createElement("div", {
    className: "results"
  }, res.map(c => /*#__PURE__*/React.createElement("div", {
    key: c.titulo,
    className: "result"
  }, /*#__PURE__*/React.createElement(TypeTag, {
    tipo: c.tipo,
    showLabel: false,
    size: 15
  }), /*#__PURE__*/React.createElement("b", null, c.titulo), /*#__PURE__*/React.createElement("span", {
    className: "pl-row__num"
  }, c.anio), /*#__PURE__*/React.createElement(Badge, {
    variant: "outline"
  }, c.fuenteExterna.replace('_', ' ')), /*#__PURE__*/React.createElement(Button, {
    size: "sm",
    variant: "secondary",
    icon: "plus",
    onClick: () => onAdd(c)
  }, "A\xF1adir"))), !res.length && /*#__PURE__*/React.createElement("div", {
    className: "cmd__empty"
  }, "Sin resultados para \xAB", q, "\xBB."))));
}
function Odisea() {
  const {
    ListRow,
    ListHeader,
    SegmentedControl,
    Input,
    Button,
    IconButton,
    Toast
  } = window.PolarisDesignSystem_b0ab94;
  const [items, setItems] = React.useState(PD.entradas);
  const [estado, setEstado] = React.useState('ALL');
  const [tipo, setTipo] = React.useState(null);
  const [q, setQ] = React.useState('');
  const [sel, setSel] = React.useState(2);
  const [add, setAdd] = React.useState(false);
  const [toast, setToast] = React.useState(null);
  const count = s => items.filter(e => e.estado === s).length;
  const list = items.filter(e => (estado === 'ALL' || e.estado === estado) && (!tipo || e.tipo === tipo) && e.titulo.toLowerCase().includes(q.toLowerCase()));
  const cur = items.find(e => e.id === sel);
  const update = patch => setItems(xs => xs.map(x => x.id === sel ? {
    ...x,
    ...patch
  } : x));
  const onAdd = c => {
    const id = Date.now();
    setItems(xs => [{
      id,
      ...c,
      tituloOriginal: null,
      duracionMin: null,
      generos: '',
      estado: 'PENDIENTE',
      valoracion: null,
      favorito: false,
      progreso: null,
      notas: null,
      sinopsis: null,
      fechaInicio: null,
      fechaFin: null
    }, ...xs]);
    setAdd(false);
    setSel(id);
    setToast(c.titulo);
    setTimeout(() => setToast(null), 3200);
  };
  return /*#__PURE__*/React.createElement("div", null, /*#__PURE__*/React.createElement(PageHeader, {
    eyebrow: "Odisea",
    coord: items.length + ' TÍTULOS',
    title: "Tu lista",
    actions: /*#__PURE__*/React.createElement(Button, {
      icon: "plus",
      onClick: () => setAdd(true)
    }, "A\xF1adir t\xEDtulo")
  }), /*#__PURE__*/React.createElement("div", {
    className: "toolbar pl-rise",
    style: {
      animationDelay: '140ms'
    }
  }, /*#__PURE__*/React.createElement(SegmentedControl, {
    value: estado,
    onChange: setEstado,
    options: [{
      value: 'ALL',
      label: 'Todo',
      count: items.length
    }, {
      value: 'EN_CURSO',
      label: 'En curso',
      count: count('EN_CURSO')
    }, {
      value: 'PENDIENTE',
      label: 'Pendiente',
      count: count('PENDIENTE')
    }, {
      value: 'TERMINADO',
      label: 'Terminado',
      count: count('TERMINADO')
    }, {
      value: 'ABANDONADO',
      label: 'Abandonado',
      count: count('ABANDONADO')
    }]
  }), /*#__PURE__*/React.createElement("div", {
    className: "toolbar__types"
  }, [['PELICULA', 'clapperboard', 'Películas'], ['SERIE', 'tv', 'Series'], ['JUEGO', 'gamepad-2', 'Juegos'], ['LIBRO', 'book-open', 'Libros']].map(([t, i, l]) => /*#__PURE__*/React.createElement(IconButton, {
    key: t,
    icon: i,
    label: l,
    pressed: tipo === t,
    variant: tipo === t ? 'outline' : 'ghost',
    onClick: () => setTipo(tipo === t ? null : t)
  }))), /*#__PURE__*/React.createElement(Input, {
    size: "sm",
    icon: "search",
    placeholder: "Filtrar por t\xEDtulo",
    value: q,
    onChange: e => setQ(e.target.value),
    style: {
      width: 240
    }
  })), /*#__PURE__*/React.createElement("div", {
    className: "odisea"
  }, /*#__PURE__*/React.createElement("div", {
    className: "listbox pl-rise",
    style: {
      animationDelay: '180ms'
    }
  }, /*#__PURE__*/React.createElement(ListHeader, null), list.map((e, i) => /*#__PURE__*/React.createElement(ListRow, _extends({
    key: e.id,
    index: i
  }, e, {
    selected: e.id === sel,
    onClick: () => setSel(e.id)
  }))), !list.length && /*#__PURE__*/React.createElement("div", {
    className: "cmd__empty",
    style: {
      padding: 32
    }
  }, "Nada con estos filtros.")), cur && /*#__PURE__*/React.createElement(Detail, {
    e: cur,
    update: update
  })), /*#__PURE__*/React.createElement(AddDialog, {
    open: add,
    onClose: () => setAdd(false),
    onAdd: onAdd
  }), toast && /*#__PURE__*/React.createElement(Toast, {
    fixed: true,
    tone: "success",
    onClose: () => setToast(null)
  }, toast, " a\xF1adido a Pendientes"));
}
window.Odisea = Odisea;
})(); } catch (e) { __ds_ns.__errors.push({ path: "ui_kits/polaris-web/Odisea.jsx", error: String((e && e.message) || e) }); }

// ui_kits/polaris-web/Perfil.jsx
try { (() => {
const PERFIL_ESTADOS = {
  completa: {
    tieneGoogle: true,
    tienePassword: true,
    emailVerificado: true
  },
  google: {
    tieneGoogle: true,
    tienePassword: false,
    emailVerificado: true
  },
  nativa: {
    tieneGoogle: false,
    tienePassword: true,
    emailVerificado: true
  },
  sinverificar: {
    tieneGoogle: true,
    tienePassword: true,
    emailVerificado: false
  }
};
function Perfil() {
  const {
    Card,
    Avatar,
    Input,
    Button,
    Alert,
    Tooltip,
    Dialog,
    SegmentedControl,
    Badge,
    Icon,
    Toast
  } = window.PolarisDesignSystem_b0ab94;
  const [demo, setDemo] = React.useState('completa');
  const u = {
    ...PD.user,
    ...PERFIL_ESTADOS[demo]
  };
  const [email, setEmail] = React.useState(u.email);
  const [emailErr, setEmailErr] = React.useState(null);
  const [actual, setActual] = React.useState('');
  const [nueva, setNueva] = React.useState('');
  const [passErr, setPassErr] = React.useState(null);
  const [unlink, setUnlink] = React.useState(false);
  const [toast, setToast] = React.useState(null);
  const flash = t => {
    setToast(t);
    setTimeout(() => setToast(null), 2800);
  };
  const saveEmail = () => {
    if (email === u.email) return setEmailErr('Ese ya es tu email.');
    if (email.startsWith('otro')) return setEmailErr('Ese email ya está registrado por otra cuenta.');
    setEmailErr(null);
    setDemo('sinverificar');
    flash('Te hemos enviado un enlace a ' + email);
  };
  const savePass = () => {
    if (u.tienePassword && actual !== 'polaris') return setPassErr('La contraseña actual no es correcta.');
    setPassErr(null);
    setActual('');
    setNueva('');
    flash(u.tienePassword ? 'Contraseña cambiada' : 'Contraseña creada');
  };
  const creado = new Date(u.creadoEn).toLocaleDateString('es-ES', {
    day: 'numeric',
    month: 'long',
    year: 'numeric'
  });
  return /*#__PURE__*/React.createElement("div", {
    className: "perfil"
  }, /*#__PURE__*/React.createElement(PageHeader, {
    eyebrow: "Cuenta",
    coord: 'DESDE ' + creado.toUpperCase(),
    title: "Perfil",
    actions: /*#__PURE__*/React.createElement(Button, {
      variant: "ghost",
      icon: "log-out",
      onClick: () => window.polarisLogout && window.polarisLogout()
    }, "Cerrar sesi\xF3n")
  }), /*#__PURE__*/React.createElement("div", {
    className: "demo pl-rise"
  }, /*#__PURE__*/React.createElement("span", {
    className: "pl-eyebrow"
  }, "Estado de demo"), /*#__PURE__*/React.createElement(SegmentedControl, {
    value: demo,
    onChange: setDemo,
    options: [{
      value: 'completa',
      label: 'Cuenta completa'
    }, {
      value: 'google',
      label: 'Solo Google'
    }, {
      value: 'nativa',
      label: 'Solo nativa'
    }, {
      value: 'sinverificar',
      label: 'Email sin verificar'
    }]
  })), /*#__PURE__*/React.createElement("div", {
    className: "stack-16"
  }, /*#__PURE__*/React.createElement(Card, {
    delay: 60,
    eyebrow: "01",
    title: "Tus datos"
  }, /*#__PURE__*/React.createElement("div", {
    className: "pf-datos"
  }, /*#__PURE__*/React.createElement("div", {
    className: "stack-8",
    style: {
      alignItems: 'center'
    }
  }, /*#__PURE__*/React.createElement(Avatar, {
    name: u.nombre,
    size: 88
  }), /*#__PURE__*/React.createElement(Button, {
    size: "sm",
    variant: "ghost",
    icon: "image-up"
  }, "Cambiar")), /*#__PURE__*/React.createElement("div", {
    className: "pf-2"
  }, /*#__PURE__*/React.createElement(Input, {
    label: "Nombre",
    defaultValue: u.nombre
  }), /*#__PURE__*/React.createElement(Input, {
    label: "Nombre de usuario",
    value: '@' + u.username,
    locked: true,
    hint: "Es la mitad de tus credenciales: no se puede cambiar."
  })))), /*#__PURE__*/React.createElement(Card, {
    delay: 120,
    eyebrow: "02",
    title: "Email",
    action: u.emailVerificado ? /*#__PURE__*/React.createElement(Badge, {
      tone: "success"
    }, "Verificado") : /*#__PURE__*/React.createElement(Badge, {
      tone: "warning"
    }, "Sin verificar")
  }, /*#__PURE__*/React.createElement("div", {
    className: "stack-12"
  }, !u.emailVerificado && /*#__PURE__*/React.createElement(Alert, {
    tone: "warning",
    title: "Falta un paso: verifica tu nuevo email",
    action: /*#__PURE__*/React.createElement(Button, {
      size: "sm",
      variant: "secondary",
      icon: "send",
      onClick: () => flash('Correo reenviado')
    }, "Reenviar correo")
  }, "Te enviamos un enlace. Hasta que lo abras, el email aparece como sin verificar."), /*#__PURE__*/React.createElement("div", {
    className: "pf-row"
  }, /*#__PURE__*/React.createElement(Input, {
    style: {
      flex: 1
    },
    label: "Direcci\xF3n",
    icon: "mail",
    value: email,
    onChange: e => {
      setEmail(e.target.value);
      setEmailErr(null);
    },
    error: emailErr,
    hint: "Si lo cambias, quedar\xE1 sin verificar hasta que abras el enlace."
  }), /*#__PURE__*/React.createElement(Button, {
    variant: "secondary",
    onClick: saveEmail
  }, "Guardar")))), /*#__PURE__*/React.createElement(Card, {
    delay: 180,
    eyebrow: "03",
    title: u.tienePassword ? 'Cambiar contraseña' : 'Poner una contraseña'
  }, /*#__PURE__*/React.createElement("div", {
    className: "stack-12"
  }, !u.tienePassword && /*#__PURE__*/React.createElement("span", {
    className: "muted",
    style: {
      fontSize: 13
    }
  }, "Entraste con Google y a\xFAn no tienes contrase\xF1a. Con una podr\xE1s entrar tambi\xE9n con tu usuario."), /*#__PURE__*/React.createElement("div", {
    className: "pf-3"
  }, u.tienePassword && /*#__PURE__*/React.createElement(Input, {
    label: "Contrase\xF1a actual",
    type: "password",
    value: actual,
    onChange: e => {
      setActual(e.target.value);
      setPassErr(null);
    },
    error: passErr,
    hint: "Pista de la maqueta: polaris"
  }), /*#__PURE__*/React.createElement(Input, {
    label: "Nueva contrase\xF1a",
    type: "password",
    value: nueva,
    onChange: e => setNueva(e.target.value)
  }), /*#__PURE__*/React.createElement(Input, {
    label: "Repite la nueva",
    type: "password"
  })), /*#__PURE__*/React.createElement("div", null, /*#__PURE__*/React.createElement(Button, {
    onClick: savePass
  }, u.tienePassword ? 'Cambiar contraseña' : 'Poner contraseña')))), /*#__PURE__*/React.createElement(Card, {
    delay: 240,
    eyebrow: "04",
    title: "Google"
  }, u.tieneGoogle ? /*#__PURE__*/React.createElement("div", {
    className: "pf-google"
  }, /*#__PURE__*/React.createElement("span", {
    className: "gmark"
  }, /*#__PURE__*/React.createElement(Icon, {
    name: "link-2",
    size: 16
  })), /*#__PURE__*/React.createElement("div", {
    className: "stack-4",
    style: {
      flex: 1
    }
  }, /*#__PURE__*/React.createElement("b", null, "Vinculada"), /*#__PURE__*/React.createElement("span", {
    className: "muted",
    style: {
      fontSize: 13
    }
  }, PD.user.email)), u.tienePassword ? /*#__PURE__*/React.createElement(Button, {
    variant: "danger",
    icon: "unlink",
    onClick: () => setUnlink(true)
  }, "Desvincular") : /*#__PURE__*/React.createElement(Tooltip, {
    wrap: true,
    label: "Pon antes una contrase\xF1a: sin ella no te quedar\xEDa ninguna forma de entrar."
  }, /*#__PURE__*/React.createElement(Button, {
    variant: "danger",
    icon: "unlink",
    disabled: true
  }, "Desvincular"))) : /*#__PURE__*/React.createElement("div", {
    className: "pf-google"
  }, /*#__PURE__*/React.createElement("span", {
    className: "gmark gmark--off"
  }, /*#__PURE__*/React.createElement(Icon, {
    name: "link-2-off",
    size: 16
  })), /*#__PURE__*/React.createElement("div", {
    className: "stack-4",
    style: {
      flex: 1
    }
  }, /*#__PURE__*/React.createElement("b", null, "No vinculada"), /*#__PURE__*/React.createElement("span", {
    className: "muted",
    style: {
      fontSize: 13
    }
  }, "Conecta tu cuenta para entrar con un clic.")), /*#__PURE__*/React.createElement(Button, {
    variant: "secondary",
    icon: "link-2"
  }, "Conectar con Google")), u.tieneGoogle && !u.tienePassword && /*#__PURE__*/React.createElement("div", {
    style: {
      marginTop: 12
    }
  }, /*#__PURE__*/React.createElement(Alert, {
    tone: "info",
    title: "\xBFPor qu\xE9 no puedo desvincular?"
  }, "Google es ahora tu \xFAnica forma de entrar. Pon una contrase\xF1a en el bloque 03 y podr\xE1s hacerlo.")))), /*#__PURE__*/React.createElement(Dialog, {
    open: unlink,
    onClose: () => setUnlink(false),
    title: "\xBFDesvincular Google?",
    footer: /*#__PURE__*/React.createElement(React.Fragment, null, /*#__PURE__*/React.createElement(Button, {
      variant: "ghost",
      onClick: () => setUnlink(false)
    }, "Cancelar"), /*#__PURE__*/React.createElement(Button, {
      variant: "danger",
      onClick: () => {
        setUnlink(false);
        setDemo('nativa');
        flash('Google desvinculado');
      }
    }, "Desvincular"))
  }, "Seguir\xE1s entrando con ", /*#__PURE__*/React.createElement("b", {
    style: {
      color: 'var(--text-1)'
    }
  }, "@", u.username), " y tu contrase\xF1a. Puedes volver a conectarla cuando quieras."), toast && /*#__PURE__*/React.createElement(Toast, {
    fixed: true,
    tone: "success",
    onClose: () => setToast(null)
  }, toast));
}
window.Perfil = Perfil;
})(); } catch (e) { __ds_ns.__errors.push({ path: "ui_kits/polaris-web/Perfil.jsx", error: String((e && e.message) || e) }); }

// ui_kits/polaris-web/Shell.jsx
try { (() => {
const NAV = [{
  id: 'inicio',
  label: 'Inicio',
  icon: 'compass',
  color: 'var(--mod-polaris)',
  mod: 'polaris'
}, {
  id: 'odisea',
  label: 'Odisea',
  icon: 'clapperboard',
  color: 'var(--mod-odisea)',
  mod: 'odisea'
}, {
  id: 'kuiper',
  label: 'Kuiper',
  icon: 'wallet',
  color: 'var(--mod-kuiper)',
  mod: 'kuiper'
}, {
  id: 'fusion',
  label: 'Fusión',
  icon: 'flame',
  color: 'var(--mod-fusion)',
  mod: 'fusion'
}, {
  id: 'atlas',
  label: 'Atlas',
  icon: 'dumbbell',
  color: 'var(--mod-atlas)',
  mod: 'atlas'
}];
function AppShell({
  route,
  go,
  children
}) {
  const {
    NavBar
  } = window.PolarisDesignSystem_b0ab94;
  const [cmd, setCmd] = React.useState(false);
  React.useEffect(() => {
    const k = e => {
      if ((e.metaKey || e.ctrlKey) && e.key.toLowerCase() === 'k') {
        e.preventDefault();
        setCmd(c => !c);
      }
    };
    window.addEventListener('keydown', k);
    return () => window.removeEventListener('keydown', k);
  }, []);
  const mod = route === 'perfil' ? 'nucleo' : (NAV.find(n => n.id === route) || NAV[0]).mod;
  return /*#__PURE__*/React.createElement("div", {
    className: "app",
    "data-module": mod
  }, /*#__PURE__*/React.createElement("div", {
    className: "app__sky"
  }, /*#__PURE__*/React.createElement(StarTrails, {
    pole: [0.5, -0.08],
    speed: 0.35,
    density: 0.7
  })), /*#__PURE__*/React.createElement("div", {
    className: "app__veil"
  }), /*#__PURE__*/React.createElement("div", {
    className: "app__glow"
  }), /*#__PURE__*/React.createElement("div", {
    className: "app__nav"
  }, /*#__PURE__*/React.createElement(NavBar, {
    items: NAV,
    value: route === 'perfil' ? null : route,
    onChange: go,
    onBrand: () => go('inicio'),
    onSearch: () => setCmd(true),
    user: {
      name: PD.user.nombre,
      onClick: () => go('perfil')
    }
  })), /*#__PURE__*/React.createElement("main", {
    className: "app__main",
    key: route
  }, children), /*#__PURE__*/React.createElement(CommandPalette, {
    open: cmd,
    onClose: () => setCmd(false),
    go: r => {
      setCmd(false);
      go(r);
    }
  }));
}
function PageHeader({
  eyebrow,
  coord,
  title,
  actions,
  badge
}) {
  const {
    Eyebrow
  } = window.PolarisDesignSystem_b0ab94;
  return /*#__PURE__*/React.createElement("header", {
    className: "ph"
  }, /*#__PURE__*/React.createElement("div", {
    className: "ph__t"
  }, /*#__PURE__*/React.createElement("div", {
    className: "pl-rise",
    style: {
      display: 'flex',
      gap: 12,
      alignItems: 'center'
    }
  }, /*#__PURE__*/React.createElement(Eyebrow, {
    star: true,
    coord: coord
  }, eyebrow), badge), /*#__PURE__*/React.createElement("h1", {
    className: "ph__h pl-rise",
    style: {
      animationDelay: '60ms'
    }
  }, title)), actions && /*#__PURE__*/React.createElement("div", {
    className: "ph__a pl-rise",
    style: {
      animationDelay: '120ms'
    }
  }, actions));
}
function CommandPalette({
  open,
  onClose,
  go
}) {
  const {
    Dialog,
    Input,
    Icon,
    TypeTag
  } = window.PolarisDesignSystem_b0ab94;
  const [q, setQ] = React.useState('');
  const items = [...NAV.map(n => ({
    k: n.id,
    label: n.label,
    icon: n.icon,
    hint: 'Módulo',
    go: n.id
  })), {
    k: 'perfil',
    label: 'Perfil de cuenta',
    icon: 'user-round',
    hint: 'Cuenta',
    go: 'perfil'
  }, ...PD.entradas.map(e => ({
    k: 'e' + e.id,
    label: e.titulo,
    tipo: e.tipo,
    hint: 'Odisea',
    go: 'odisea'
  }))].filter(i => i.label.toLowerCase().includes(q.toLowerCase()));
  return /*#__PURE__*/React.createElement(Dialog, {
    open: open,
    onClose: onClose,
    title: "Buscar",
    width: 520
  }, /*#__PURE__*/React.createElement(Input, {
    icon: "search",
    autoFocus: true,
    placeholder: "M\xF3dulos, t\xEDtulos\u2026",
    value: q,
    onChange: e => setQ(e.target.value)
  }), /*#__PURE__*/React.createElement("div", {
    className: "cmd"
  }, items.map(i => /*#__PURE__*/React.createElement("button", {
    key: i.k,
    className: "cmd__i",
    onClick: () => go(i.go)
  }, i.tipo ? /*#__PURE__*/React.createElement(TypeTag, {
    tipo: i.tipo,
    showLabel: false
  }) : /*#__PURE__*/React.createElement(Icon, {
    name: i.icon,
    size: 15
  }), /*#__PURE__*/React.createElement("span", null, i.label), /*#__PURE__*/React.createElement("em", null, i.hint))), !items.length && /*#__PURE__*/React.createElement("div", {
    className: "cmd__empty"
  }, "Nada con \xAB", q, "\xBB.")));
}
function Maqueta() {
  const {
    Badge
  } = window.PolarisDesignSystem_b0ab94;
  return /*#__PURE__*/React.createElement("span", {
    title: "Datos inventados: el m\xF3dulo a\xFAn no tiene backend"
  }, /*#__PURE__*/React.createElement(Badge, {
    variant: "outline",
    color: "var(--text-3)"
  }, "Maqueta"));
}
const eur = (n, d = 2) => n.toLocaleString('es-ES', {
  minimumFractionDigits: d,
  maximumFractionDigits: d
}) + ' €';
Object.assign(window, {
  AppShell,
  PageHeader,
  CommandPalette,
  Maqueta,
  NAV,
  eur
});
})(); } catch (e) { __ds_ns.__errors.push({ path: "ui_kits/polaris-web/Shell.jsx", error: String((e && e.message) || e) }); }

// ui_kits/polaris-web/StarTrails.jsx
try { (() => {
// Star trails rotating around the celestial pole (Polaris). Canvas, fading-trail technique.
function StarTrails({
  pole = [0.62, 0.24],
  anchor,
  speed = 1,
  density = 1,
  bg = '18,17,14'
}) {
  const anchorRef = React.useRef(anchor);
  anchorRef.current = anchor;
  const ref = React.useRef(null);
  const speedRef = React.useRef(speed);
  React.useEffect(() => {
    speedRef.current = speed;
  }, [speed]);
  React.useEffect(() => {
    const cv = ref.current,
      ctx = cv.getContext('2d');
    const reduce = window.matchMedia('(prefers-reduced-motion: reduce)').matches;
    let W,
      H,
      dpr,
      stars = [],
      raf,
      cur = speedRef.current,
      P = pole;
    const palette = ['255,244,220', '255,244,220', '255,244,220', '243,238,228', '157,214,201', '245,213,140'];
    const init = () => {
      dpr = Math.min(window.devicePixelRatio || 1, 1.5);
      W = cv.clientWidth;
      H = cv.clientHeight;
      const a = anchorRef.current && anchorRef.current();
      P = a ? [a[0] / W, a[1] / H] : pole;
      cv.width = W * dpr;
      cv.height = H * dpr;
      ctx.setTransform(dpr, 0, 0, dpr, 0, 0);
      const R = Math.hypot(Math.max(P[0], 1 - P[0]) * W, Math.max(P[1], 1 - P[1]) * H);
      const n = Math.round(W * H / 1400 * density);
      stars = Array.from({
        length: n
      }, () => ({
        r: Math.pow(Math.random(), 0.7) * R + 6,
        a: Math.random() * Math.PI * 2,
        s: Math.random() < 0.08 ? 1.5 + Math.random() : 0.5 + Math.random() * 0.8,
        c: palette[Math.random() * palette.length | 0],
        o: 0.25 + Math.random() * 0.75,
        tw: Math.random() * 6
      }));
      ctx.fillStyle = 'rgb(' + bg + ')';
      ctx.fillRect(0, 0, W, H);
    };
    let t = 0;
    const frame = () => {
      cur += (speedRef.current - cur) * 0.04;
      t += 0.016;
      ctx.fillStyle = 'rgba(' + bg + ',' + (0.045 + Math.min(0.04, cur * 0.004)) + ')';
      ctx.fillRect(0, 0, W, H);
      const cx = P[0] * W,
        cy = P[1] * H,
        w = 0.0011 * cur;
      for (const st of stars) {
        const a0 = st.a;
        st.a += w * (0.6 + 40 / (st.r + 40));
        const o = st.o * (0.75 + 0.25 * Math.sin(t * 2 + st.tw));
        ctx.strokeStyle = 'rgba(' + st.c + ',' + o + ')';
        ctx.lineWidth = st.s;
        ctx.lineCap = 'round';
        ctx.beginPath();
        ctx.arc(cx, cy, st.r, a0, st.a);
        ctx.stroke();
      }
      // Polaris itself
      const g = ctx.createRadialGradient(cx, cy, 0, cx, cy, 28);
      g.addColorStop(0, 'rgba(255,244,220,.9)');
      g.addColorStop(0.15, 'rgba(157,214,201,.35)');
      g.addColorStop(1, 'rgba(91,179,160,0)');
      ctx.fillStyle = g;
      ctx.beginPath();
      ctx.arc(cx, cy, 28, 0, Math.PI * 2);
      ctx.fill();
      ctx.fillStyle = '#fff4dc';
      ctx.beginPath();
      ctx.arc(cx, cy, 2.2 + Math.sin(t * 1.6) * 0.4, 0, Math.PI * 2);
      ctx.fill();
      raf = requestAnimationFrame(frame);
    };
    init();
    if (reduce) {
      for (let i = 0; i < 160; i++) {
        cur = 1;
      }
      frame();
      cancelAnimationFrame(raf);
    } else raf = requestAnimationFrame(frame);
    const onR = () => init();
    setTimeout(init, 60);
    document.fonts && document.fonts.ready.then(init);
    window.addEventListener('resize', onR);
    return () => {
      cancelAnimationFrame(raf);
      window.removeEventListener('resize', onR);
    };
  }, []);
  return /*#__PURE__*/React.createElement("canvas", {
    ref: ref,
    style: {
      position: 'absolute',
      inset: 0,
      width: '100%',
      height: '100%',
      display: 'block'
    }
  });
}
window.StarTrails = StarTrails;
})(); } catch (e) { __ds_ns.__errors.push({ path: "ui_kits/polaris-web/StarTrails.jsx", error: String((e && e.message) || e) }); }

// ui_kits/polaris-web/data.js
try { (() => {
// Datos de muestra. Odisea: datos reales del briefing. Kuiper/Fusión/Atlas/Núcleo: MAQUETA (inventados).
window.PD = {
  user: {
    id: 11,
    username: 'dani',
    email: 'danielserrano1702@gmail.com',
    nombre: 'Daniel',
    avatarUrl: null,
    creadoEn: '2026-08-30T16:41:44Z',
    emailVerificado: true,
    tieneGoogle: true,
    tienePassword: true
  },
  entradas: [{
    id: 1,
    titulo: 'Interstellar',
    tituloOriginal: 'Interstellar',
    tipo: 'PELICULA',
    anio: 2014,
    duracionMin: 169,
    generos: 'Aventura, Drama, Ciencia ficción',
    fuenteExterna: 'TMDB',
    estado: 'TERMINADO',
    valoracion: 10,
    favorito: true,
    fechaInicio: '2026-08-02',
    fechaFin: '2026-08-02',
    progreso: null,
    notas: 'Vista en versión original. La escena del muelle sigue igual de bien.',
    sinopsis: 'Un grupo de exploradores cruza un agujero de gusano buscando un nuevo hogar para la humanidad.'
  }, {
    id: 2,
    titulo: 'Breaking Bad',
    tituloOriginal: 'Breaking Bad',
    tipo: 'SERIE',
    anio: 2008,
    duracionMin: 45,
    generos: 'Drama, Crimen',
    fuenteExterna: 'TMDB',
    estado: 'EN_CURSO',
    valoracion: 9,
    favorito: true,
    fechaInicio: '2026-09-01',
    fechaFin: null,
    progreso: 34,
    notas: null,
    sinopsis: null
  }, {
    id: 3,
    titulo: 'The Legend of Zelda',
    tituloOriginal: null,
    tipo: 'JUEGO',
    anio: 1986,
    duracionMin: null,
    generos: 'Aventura',
    fuenteExterna: 'IGDB',
    estado: 'ABANDONADO',
    valoracion: 6,
    favorito: false,
    fechaInicio: '2026-07-12',
    fechaFin: null,
    progreso: null,
    notas: null,
    sinopsis: null
  }, {
    id: 4,
    titulo: 'Dune',
    tituloOriginal: 'Dune',
    tipo: 'LIBRO',
    anio: 1965,
    duracionMin: 607,
    generos: 'Ciencia ficción, Ficción',
    fuenteExterna: 'OPEN_LIBRARY',
    estado: 'EN_CURSO',
    valoracion: null,
    favorito: false,
    fechaInicio: '2026-09-10',
    fechaFin: null,
    progreso: 212,
    notas: null,
    sinopsis: 'Frank Herbert'
  }, {
    id: 5,
    titulo: 'Blade Runner 2049',
    tituloOriginal: 'Blade Runner 2049',
    tipo: 'PELICULA',
    anio: 2017,
    duracionMin: 164,
    generos: 'Ciencia ficción, Drama',
    fuenteExterna: 'TMDB',
    estado: 'PENDIENTE',
    valoracion: null,
    favorito: false,
    fechaInicio: null,
    fechaFin: null,
    progreso: null,
    notas: null,
    sinopsis: null
  }],
  catalogo: [{
    titulo: 'The Legend of Zelda: Breath of the Wild',
    tipo: 'JUEGO',
    anio: 2017,
    fuenteExterna: 'IGDB'
  }, {
    titulo: 'Dune: Part Two',
    tipo: 'PELICULA',
    anio: 2024,
    fuenteExterna: 'TMDB'
  }, {
    titulo: 'Dune Messiah',
    tipo: 'LIBRO',
    anio: 1969,
    fuenteExterna: 'OPEN_LIBRARY'
  }, {
    titulo: 'Better Call Saul',
    tipo: 'SERIE',
    anio: 2015,
    fuenteExterna: 'TMDB'
  }],
  // ---- MAQUETA ----
  kuiper: {
    presupuesto: 1800,
    gastado: 1284.5,
    ingresos: 2450,
    dias: [42, 18, 64, 30, 12, 88, 74, 22, 35, 9, 51, 60, 140, 26, 33, 47, 15, 70, 38, 29, 95, 21, 44, 57, 31],
    categorias: [{
      nombre: 'Casa',
      gastado: 620,
      limite: 650,
      icon: 'house'
    }, {
      nombre: 'Comida',
      gastado: 318.4,
      limite: 380,
      icon: 'shopping-basket'
    }, {
      nombre: 'Ocio',
      gastado: 136.6,
      limite: 120,
      icon: 'ticket'
    }, {
      nombre: 'Transporte',
      gastado: 128,
      limite: 180,
      icon: 'train-front'
    }, {
      nombre: 'Suscripciones',
      gastado: 81.5,
      limite: 90,
      icon: 'repeat'
    }],
    movimientos: [{
      fecha: '25 sep',
      concepto: 'Mercadona',
      categoria: 'Comida',
      tipo: 'GASTO',
      importe: 31.2
    }, {
      fecha: '24 sep',
      concepto: 'Abono transporte',
      categoria: 'Transporte',
      tipo: 'GASTO',
      importe: 44
    }, {
      fecha: '23 sep',
      concepto: 'Cine — Yelmo',
      categoria: 'Ocio',
      tipo: 'GASTO',
      importe: 18.5
    }, {
      fecha: '22 sep',
      concepto: 'Venta Wallapop',
      categoria: 'Otros',
      tipo: 'INGRESO',
      importe: 60
    }, {
      fecha: '21 sep',
      concepto: 'Luz',
      categoria: 'Casa',
      tipo: 'GASTO',
      importe: 57.3
    }, {
      fecha: '20 sep',
      concepto: 'Spotify',
      categoria: 'Suscripciones',
      tipo: 'GASTO',
      importe: 10.99
    }]
  },
  fusion: {
    kcal: 1640,
    objetivo: 2300,
    macros: [{
      nombre: 'Proteína',
      g: 112,
      obj: 150
    }, {
      nombre: 'Carbohidratos',
      g: 168,
      obj: 260
    }, {
      nombre: 'Grasa',
      g: 52,
      obj: 75
    }],
    comidas: [{
      momento: 'Desayuno',
      hora: '08:10',
      lineas: [['Avena', 60, 228], ['Leche semidesnatada', 250, 115], ['Plátano', 120, 107]]
    }, {
      momento: 'Comida',
      hora: '14:30',
      lineas: [['Arroz blanco', 90, 315], ['Pechuga de pollo', 180, 297], ['Aceite de oliva', 10, 88]]
    }, {
      momento: 'Merienda',
      hora: '18:00',
      lineas: [['Yogur natural', 125, 76], ['Nueces', 25, 164]]
    }, {
      momento: 'Cena',
      hora: '—',
      lineas: []
    }],
    semana: [2210, 2380, 1980, 2290, 2450, 2120, 2310, 2260, 2050, 2400, 2330, 2190, 2280, 1640]
  },
  atlas: {
    ultima: {
      rutina: 'Empuje',
      dia: 'Martes 23',
      ejercicios: 6,
      series: 22,
      volumen: 8420
    },
    siguiente: {
      rutina: 'Tirón',
      dia: 'Hoy'
    },
    progresion: [70, 72.5, 72.5, 74, 75, 75, 76.5, 78, 77.5, 80],
    semanas: ['S28', 'S29', 'S30', 'S31', 'S32', 'S33', 'S34', 'S35', 'S36', 'S37'],
    volumen: [18.2, 20.1, 17.4, 21.8, 22.5, 19.9, 23.4, 24.1, 22.0, 25.3],
    records: [{
      ej: 'Press banca',
      v: '80 kg × 3',
      fecha: '23 sep',
      nuevo: true
    }, {
      ej: 'Sentadilla',
      v: '105 kg × 5',
      fecha: '18 sep'
    }, {
      ej: 'Peso muerto',
      v: '130 kg × 3',
      fecha: '11 sep'
    }, {
      ej: 'Dominadas',
      v: '+10 kg × 6',
      fecha: '19 sep'
    }],
    sesiones: [{
      fecha: 'Mar 23',
      rutina: 'Empuje',
      ej: 6,
      series: 22,
      vol: 8420
    }, {
      fecha: 'Dom 21',
      rutina: 'Pierna',
      ej: 5,
      series: 20,
      vol: 11260
    }, {
      fecha: 'Vie 19',
      rutina: 'Tirón',
      ej: 6,
      series: 21,
      vol: 7980
    }, {
      fecha: 'Mié 17',
      rutina: 'Improvisado',
      ej: 3,
      series: 9,
      vol: 2940
    }]
  },
  semanaAtlas: [1, 0, 1, 0, 1, 0, 1],
  actividad: [{
    hora: '11:02',
    mod: 'fusion',
    icon: 'utensils',
    txt: 'Merienda registrada',
    det: '240 kcal'
  }, {
    hora: '10:15',
    mod: 'kuiper',
    icon: 'shopping-basket',
    txt: 'Mercadona',
    det: '−31,20 €'
  }, {
    hora: 'Ayer',
    mod: 'odisea',
    icon: 'tv',
    txt: 'Breaking Bad · episodio 34',
    det: 'En curso'
  }, {
    hora: 'Ayer',
    mod: 'atlas',
    icon: 'trophy',
    txt: 'Récord en press banca',
    det: '80 kg × 3'
  }, {
    hora: 'Mar 23',
    mod: 'odisea',
    icon: 'plus',
    txt: 'Blade Runner 2049 a Pendientes',
    det: 'TMDB'
  }, {
    hora: 'Mar 23',
    mod: 'nucleo',
    icon: 'scale',
    txt: 'Peso registrado',
    det: '78,2 kg'
  }],
  nucleo: {
    peso: 78.2,
    delta: -0.4,
    serie: [79.4, 79.1, 79.3, 78.9, 78.8, 78.9, 78.6, 78.6, 78.4, 78.2]
  }
};
})(); } catch (e) { __ds_ns.__errors.push({ path: "ui_kits/polaris-web/data.js", error: String((e && e.message) || e) }); }

__ds_ns.Avatar = __ds_scope.Avatar;

__ds_ns.Badge = __ds_scope.Badge;

__ds_ns.Button = __ds_scope.Button;

__ds_ns.Card = __ds_scope.Card;

__ds_ns.Eyebrow = __ds_scope.Eyebrow;

__ds_ns.Icon = __ds_scope.Icon;

__ds_ns.IconButton = __ds_scope.IconButton;

__ds_ns.Logo = __ds_scope.Logo;

__ds_ns.StateGlyph = __ds_scope.StateGlyph;

__ds_ns.StatusBadge = __ds_scope.StatusBadge;

__ds_ns.TypeTag = __ds_scope.TypeTag;

__ds_ns.BarChart = __ds_scope.BarChart;

__ds_ns.LineChart = __ds_scope.LineChart;

__ds_ns.ListRow = __ds_scope.ListRow;

__ds_ns.ListHeader = __ds_scope.ListHeader;

__ds_ns.ProgressBar = __ds_scope.ProgressBar;

__ds_ns.Rating = __ds_scope.Rating;

__ds_ns.RingChart = __ds_scope.RingChart;

__ds_ns.Stat = __ds_scope.Stat;

__ds_ns.Alert = __ds_scope.Alert;

__ds_ns.Dialog = __ds_scope.Dialog;

__ds_ns.Toast = __ds_scope.Toast;

__ds_ns.Tooltip = __ds_scope.Tooltip;

__ds_ns.Checkbox = __ds_scope.Checkbox;

__ds_ns.Input = __ds_scope.Input;

__ds_ns.Kbd = __ds_scope.Kbd;

__ds_ns.SegmentedControl = __ds_scope.SegmentedControl;

__ds_ns.Select = __ds_scope.Select;

__ds_ns.Switch = __ds_scope.Switch;

__ds_ns.NavBar = __ds_scope.NavBar;

__ds_ns.Tabs = __ds_scope.Tabs;

})();
