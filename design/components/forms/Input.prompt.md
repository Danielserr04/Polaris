Labelled text input; focus lifts it with a hard accent shadow. Also the search field (icon="search").
```jsx
<Input label="Nombre" defaultValue="Daniel" />
<Input icon="search" placeholder="Buscar en Odisea" trailing={<Kbd>⌘K</Kbd>} />
<Input label="Usuario" value="dani" locked hint="Forma parte de tus credenciales, no se puede cambiar." />
<Input label="Contraseña actual" type="password" error="La contraseña actual no es correcta" />
```
