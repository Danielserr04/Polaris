import { PageHeader } from '../components/PageHeader';

interface Props {
  eyebrow: string;
  coord?: string;
  title: string;
}

// Pantalla vacia mientras cada modulo no esta conectado a su API. Se sustituye una a una.
export function Pendiente({ eyebrow, coord, title }: Props) {
  return (
    <>
      <PageHeader eyebrow={eyebrow} coord={coord} title={title} />
      <p className="muted">Sin conectar todavía.</p>
    </>
  );
}
