One Odisea entry per 40px row — built to scan 40 titles without scroll. Empty fields render as a muted em dash, long titles ellipsize.
```jsx
<ListHeader />
{entradas.map((e, i) => <ListRow key={e.id} index={i} {...e} selected={e.id === sel} onClick={() => setSel(e.id)} />)}
```
- Column template via CSS var `--pl-row-cols` on a parent if you need a different layout.
