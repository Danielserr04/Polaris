Main action button; one primary per view, secondary for alternatives, ghost for tertiary, danger for destructive-but-reversible.
```jsx
<Button icon="plus">Añadir título</Button>
<Button variant="secondary">Cancelar</Button>
<Button variant="ghost" size="sm" iconRight="arrow-right">Ver todo</Button>
```
- Inherits the current module accent (`data-module`), so a primary in Kuiper is gold.
- `loading` swaps the icon for a spinner and disables. Sizes: sm 32px · md 40px · lg 48px.
