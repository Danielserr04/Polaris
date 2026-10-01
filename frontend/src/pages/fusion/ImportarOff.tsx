import { useState, type FormEvent } from 'react';
import { mensajeError, useBuscarCatalogo, useImportarAlimento, type ResultadoCatalogo } from '../../api/fusion';
import { useRestaurarFoco } from '../../components/useRestaurarFoco';
import { Alert, Badge, Button, Dialog, Input } from '../../design-system';
import { num } from '../../lib/fechas';

interface Props {
  onClose: () => void;
}

/** Busca en Open Food Facts y copia la ficha a tu catalogo. Sin clave: la fuente es abierta. */
export function ImportarOff({ onClose }: Props) {
  useRestaurarFoco();
  const [q, setQ] = useState('');
  const [enviado, setEnviado] = useState<string | null>(null);
  const [importado, setImportado] = useState<string | null>(null);
  const [importando, setImportando] = useState<string | null>(null);
  const [errorImportar, setErrorImportar] = useState<string | null>(null);
  const busqueda = useBuscarCatalogo(enviado);
  const importar = useImportarAlimento();

  const buscar = (ev: FormEvent) => {
    ev.preventDefault();
    const t = q.trim();
    if (t.length < 2) return;
    setImportado(null);
    setErrorImportar(null);
    setEnviado(t);
  };

  const importarUno = (r: ResultadoCatalogo) => {
    setImportando(r.idExterno);
    setErrorImportar(null);
    setImportado(null);
    importar.mutate(r.idExterno, {
      onSuccess: (a) => setImportado(a.nombre),
      onError: (e) => setErrorImportar(mensajeError(e)),
      onSettled: () => setImportando(null),
    });
  };

  return (
    <Dialog open onClose={onClose} width={620} title="Importar de Open Food Facts" footer={<Button variant="ghost" onClick={onClose}>Cerrar</Button>}>
      <div className="fus-form">
        <form onSubmit={buscar} style={{ display: 'flex', gap: 8, alignItems: 'flex-end' }}>
          <Input
            label="Buscar producto"
            icon="search"
            autoFocus
            placeholder="Nombre, marca…"
            value={q}
            onChange={(e) => setQ(e.target.value)}
            style={{ flex: 1 }}
          />
          <Button type="submit" variant="secondary" loading={busqueda.isFetching} disabled={q.trim().length < 2}>
            Buscar
          </Button>
        </form>

        {importado && <Alert tone="success">«{importado}» ya está en tu catálogo.</Alert>}
        {errorImportar && <Alert tone="danger">{errorImportar}</Alert>}

        {busqueda.isError ? (
          <Alert tone="danger" title="No se ha podido buscar">
            {mensajeError(busqueda.error)}
          </Alert>
        ) : busqueda.isSuccess ? (
          busqueda.data.length === 0 ? (
            <p className="muted" style={{ margin: 0, fontSize: 13 }}>Nada con «{enviado}».</p>
          ) : (
            <div className="fus-off" role="list">
              {busqueda.data.map((r) => (
                <div key={r.idExterno} className="fus-off__r" role="listitem">
                  <div className="fus-off__n">
                    <b>{r.nombre}</b>
                    <span>
                      {[r.marca, `${num(r.kcal100g)} kcal · P ${num(r.proteinas100g, 1)} · C ${num(r.carbohidratos100g, 1)} · G ${num(r.grasas100g, 1)}`].filter(Boolean).join(' · ')}
                    </span>
                  </div>
                  {r.alimentoId != null ? (
                    <Badge variant="outline" color="var(--text-3)">En tu catálogo</Badge>
                  ) : (
                    <Button size="sm" variant="secondary" loading={importando === r.idExterno} disabled={importando !== null} onClick={() => importarUno(r)}>
                      Importar
                    </Button>
                  )}
                </div>
              ))}
            </div>
          )
        ) : (
          <p className="muted" style={{ margin: 0, fontSize: 13 }}>
            {busqueda.isFetching ? 'Buscando…' : 'Escribe qué buscas y pulsa Buscar. Los valores son por 100 g.'}
          </p>
        )}
      </div>
    </Dialog>
  );
}
