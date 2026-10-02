import { useMemo, useState } from 'react';
import { mensajeError, usePesos } from '../../api/atlas';
import { CAMPOS_MEDIDA, type CampoMedida, categoriaImc, imc, useMedidas, usePerfilCorporal } from '../../api/cuerpo';
import { Alert, Button, Card, LineChart, Select, Stat } from '../../design-system';
import { iso, num, relativa, sumarDias } from '../../lib/fechas';

interface Props {
  onEditar: (id: number) => void;
}

const cm = (n: number | null | undefined) => (n == null ? '—' : `${num(n, n % 1 ? 1 : 0)}`);

/**
 * Peso, IMC y medidas corporales. El peso y las medidas viven en Nucleo; el IMC se
 * calcula aqui con la altura del perfil (Nucleo guarda, no interpreta).
 */
export function CuerpoTab({ onEditar }: Props) {
  const hoy = useMemo(() => new Date(), []);
  const medidas = useMedidas();
  const perfil = usePerfilCorporal();
  const pesos = usePesos(iso(sumarDias(hoy, -365)), iso(hoy));
  const [campo, setCampo] = useState<CampoMedida>('cinturaCm');

  const lista = medidas.data ?? [];
  const ordenPesos = [...(pesos.data ?? [])].sort((a, b) => a.fecha.localeCompare(b.fecha));
  const peso = ordenPesos[ordenPesos.length - 1];
  const altura = perfil.data?.alturaCm;
  const valorImc = imc(peso?.pesoKg, altura);
  const categoria = valorImc !== null ? categoriaImc(valorImc) : null;

  // Para la grafica, de mas antigua a mas reciente y solo los dias que tienen esa medida.
  const serie = [...lista].reverse().filter((m) => m[campo] != null);
  const ultimaCintura = lista.find((m) => m.cinturaCm != null);
  const previaCintura = lista.filter((m) => m.cinturaCm != null)[1];
  const ratio = ultimaCintura?.cinturaCm && altura ? ultimaCintura.cinturaCm / altura : null;
  const nombreCampo = CAMPOS_MEDIDA.find((c) => c.campo === campo)?.nombre ?? '';

  if (medidas.isError) {
    return (
      <Alert
        tone="danger"
        title="No se han podido cargar las medidas"
        action={
          <Button size="sm" variant="secondary" onClick={() => void medidas.refetch()}>
            Reintentar
          </Button>
        }
      >
        {mensajeError(medidas.error)}
      </Alert>
    );
  }

  return (
    <div className="grid">
      <div className="span-12 stats pl-rise">
        <Stat label="Peso corporal" value={peso?.pesoKg ?? 0} decimals={1} unit="kg" size={34} caption={peso ? relativa(peso.fecha, hoy) : 'sin registros'} />
        <Stat
          label="IMC"
          value={valorImc ?? 0}
          decimals={1}
          size={34}
          delta={categoria ?? undefined}
          deltaTone="flat"
          caption={altura ? `con ${altura} cm de altura` : 'pon tu altura en el perfil'}
        />
        <Stat
          label="Cintura"
          value={ultimaCintura?.cinturaCm ?? 0}
          decimals={1}
          unit="cm"
          size={34}
          delta={ultimaCintura?.cinturaCm && previaCintura?.cinturaCm ? `${ultimaCintura.cinturaCm >= previaCintura.cinturaCm ? '+' : '−'}${num(Math.abs(ultimaCintura.cinturaCm - previaCintura.cinturaCm), 1)} cm` : undefined}
          deltaTone="flat"
          caption={ultimaCintura ? relativa(ultimaCintura.fecha, hoy) : 'sin medidas'}
        />
        <Stat
          label="Cintura / altura"
          value={ratio ?? 0}
          decimals={2}
          size={34}
          caption={ratio === null ? 'falta cintura o altura' : ratio < 0.5 ? 'por debajo de 0,5: bien' : 'por encima de 0,5'}
        />
      </div>

      <div className="span-7">
        <Card
          delay={100}
          eyebrow="Evolución"
          title={nombreCampo}
          action={
            <div style={{ width: 150 }}>
              <Select size="sm" aria-label="Medida" value={campo} onChange={(e) => setCampo(e.target.value as CampoMedida)} options={CAMPOS_MEDIDA.map((c) => ({ value: c.campo, label: c.nombre }))} />
            </div>
          }
        >
          {medidas.isPending ? (
            <p className="muted" style={{ margin: 0 }}>Cargando…</p>
          ) : serie.length < 2 ? (
            <p className="muted" style={{ margin: 0 }}>Hacen falta al menos dos mediciones de {nombreCampo.toLowerCase()} para ver la evolución.</p>
          ) : (
            <LineChart
              height={220}
              labels={[serie[0], serie[serie.length - 1]].map((m) => relativa(m.fecha, hoy))}
              format={(v) => num(v, 1)}
              series={[{ name: `${nombreCampo} (cm)`, points: serie.map((m) => m[campo] as number) }]}
            />
          )}
        </Card>
      </div>

      <div className="span-5">
        <Card delay={160} eyebrow="Historial" title="Mediciones" padding="8px 0 0">
          {medidas.isPending ? (
            <p className="muted" style={{ margin: '6px 18px 14px' }}>Cargando…</p>
          ) : lista.length === 0 ? (
            <div className="atl-vacio">Aún no hay medidas. Apunta las primeras con el botón Medidas.</div>
          ) : (
            <div className="table">
              <div className="table__r table__r--med atl-med__cab" aria-hidden="true">
                <span>Fecha</span>
                <span>Pecho</span>
                <span>Cintura</span>
                <span>Cadera</span>
              </div>
              {lista.slice(0, 12).map((m) => (
                <button key={m.id} type="button" className="table__r table__r--med atl-ses" onClick={() => onEditar(m.id)} aria-label={`Editar las medidas del ${relativa(m.fecha, hoy)}`}>
                  <span className="pl-row__num">{relativa(m.fecha, hoy)}</span>
                  <span className="money">{cm(m.pechoCm)}</span>
                  <span className="money">{cm(m.cinturaCm)}</span>
                  <span className="money">{cm(m.caderaCm)}</span>
                </button>
              ))}
            </div>
          )}
        </Card>
      </div>
    </div>
  );
}
