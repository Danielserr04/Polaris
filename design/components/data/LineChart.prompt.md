Trend over time (weight, estimated 1RM, daily kcal). Pair a solid series with a dashed objective.
```jsx
<LineChart height={180} labels={['Jul', 'Ago', 'Sep']} showLegend
  series={[{ name: 'Press banca · 1RM', points: [72, 74, 75, 78] }, { name: 'Objetivo', points: [80, 80, 80, 80], dashed: true, color: 'var(--text-3)' }]} />
```
