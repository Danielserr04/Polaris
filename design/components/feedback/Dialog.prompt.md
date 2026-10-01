Modal with a blurred scrim and springy pop-in; the panel carries an accent block shadow.
```jsx
<Dialog open={o} onClose={() => setO(false)} title="Desvincular Google"
  footer={<><Button variant="ghost" onClick={close}>Cancelar</Button><Button variant="danger">Desvincular</Button></>}>
  Podrás seguir entrando con tu usuario y contraseña.
</Dialog>
```
