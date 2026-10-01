The standard Polaris surface: 1px warm border, 6px radius, no resting shadow; interactive cards lift with a hard accent shadow.
```jsx
<div data-module="kuiper">
  <Card eyebrow="Kuiper" title="Septiembre" interactive delay={80} action={<Badge variant="outline">Maqueta</Badge>}>
    …
  </Card>
</div>
```
- Wrap in `data-module` to tint the hover shadow with that module's colour.
