The app shell's only navigation: a floating, blurred island centred at the top (replaces a sidebar). Each module item carries its own colour.
```jsx
<div style={{ position: 'fixed', top: 16, left: 0, right: 0, display: 'flex', justifyContent: 'center', zIndex: 10 }}>
  <NavBar value={mod} onChange={setMod} user={{ name: 'Daniel' }} items={[
    { id: 'inicio', label: 'Inicio', icon: 'compass', color: 'var(--mod-polaris)' },
    { id: 'odisea', label: 'Odisea', icon: 'clapperboard', color: 'var(--mod-odisea)' }]} />
</div>
```
