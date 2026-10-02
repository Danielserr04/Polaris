import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { api, ApiError } from './client';

// Cuentas y transferencias de Kuiper. Reflejan los DTO de Cuenta* y Transferencia* en
// src/main/java/com/polaris/kuiper/. Ver docs/decisiones/039-cuentas-y-transferencias.md.

export type TipoCuenta = 'CORRIENTE' | 'AHORRO' | 'TARJETA' | 'EFECTIVO';

export const TIPOS_CUENTA: { value: TipoCuenta; label: string }[] = [
  { value: 'CORRIENTE', label: 'Corriente' },
  { value: 'AHORRO', label: 'Ahorro' },
  { value: 'TARJETA', label: 'Tarjeta' },
  { value: 'EFECTIVO', label: 'Efectivo' },
];

export function etiquetaTipoCuenta(tipo: TipoCuenta): string {
  return TIPOS_CUENTA.find((t) => t.value === tipo)?.label ?? tipo;
}

export interface CuentaList {
  id: number;
  nombre: string;
  tipo: TipoCuenta;
  /** Saldo inicial + ingresos - gastos + transferencias entrantes - salientes. Lo calcula el backend. */
  saldoActual: number;
  color: string | null;
  icono: string | null;
  banco: string | null;
  archivada: boolean;
}

export interface CuentaForm extends CuentaList {
  saldoInicial: number;
}

/** Cuerpo de POST/PUT /cuenta. El PUT reemplaza todo. */
export interface CuentaRequest {
  nombre: string;
  tipo: TipoCuenta;
  saldoInicial: number;
  color: string | null;
  icono: string | null;
  banco: string | null;
  archivada: boolean;
}

export interface Patrimonio {
  /** Suma del saldo actual de todas las cuentas, archivadas incluidas. */
  total: number;
  numeroCuentas: number;
}

export interface TransferenciaList {
  id: number;
  fecha: string;
  importe: number;
  cuentaOrigenId: number;
  cuentaOrigenNombre: string;
  cuentaDestinoId: number;
  cuentaDestinoNombre: string;
  concepto: string | null;
}

export interface TransferenciaRequest {
  cuentaOrigenId: number;
  cuentaDestinoId: number;
  importe: number;
  fecha: string;
  concepto: string | null;
}

export interface FiltroTransferencias {
  desde?: string;
  hasta?: string;
  cuentaId?: number;
}

const BASE = '/api/kuiper';

export const clavesCuentas = {
  cuentas: ['kuiper', 'cuentas'] as const,
  cuenta: (id: number) => ['kuiper', 'cuenta', id] as const,
  patrimonio: ['kuiper', 'patrimonio'] as const,
  transferencias: ['kuiper', 'transferencias'] as const,
};

/** Todas las cuentas, archivadas incluidas (las pantallas filtran). Activas primero y por nombre. */
export function useCuentas() {
  return useQuery({
    queryKey: clavesCuentas.cuentas,
    queryFn: () => api<CuentaList[]>(`${BASE}/cuenta`),
  });
}

/** La ficha completa (con el saldo inicial, que el listado no trae). */
export function useCuenta(id: number | undefined) {
  return useQuery({
    queryKey: clavesCuentas.cuenta(id ?? 0),
    queryFn: () => api<CuentaForm>(`${BASE}/cuenta/${id}`),
    enabled: id !== undefined,
    gcTime: 0,
  });
}

export function usePatrimonio() {
  return useQuery({
    queryKey: clavesCuentas.patrimonio,
    queryFn: () => api<Patrimonio>(`${BASE}/cuenta/patrimonio`),
  });
}

export function useTransferencias(filtro: FiltroTransferencias) {
  return useQuery({
    queryKey: [...clavesCuentas.transferencias, filtro] as const,
    queryFn: () => api<TransferenciaList[]>(`${BASE}/transferencia`, { query: { ...filtro } }),
  });
}

/** Una cuenta o transferencia cambia saldos, patrimonio, listados de movimientos y el Inicio. */
function useInvalidar() {
  const qc = useQueryClient();
  return () =>
    Promise.all([qc.invalidateQueries({ queryKey: ['kuiper'] }), qc.invalidateQueries({ queryKey: ['inicio'] })]);
}

export function useCrearCuenta() {
  const invalidar = useInvalidar();
  return useMutation({
    mutationFn: (cuerpo: CuentaRequest) => api<CuentaForm>(`${BASE}/cuenta`, { metodo: 'POST', cuerpo }),
    onSuccess: invalidar,
  });
}

export function useActualizarCuenta() {
  const invalidar = useInvalidar();
  return useMutation({
    mutationFn: ({ id, cuerpo }: { id: number; cuerpo: CuentaRequest }) =>
      api<CuentaForm>(`${BASE}/cuenta/${id}`, { metodo: 'PUT', cuerpo }),
    onSuccess: invalidar,
  });
}

/**
 * Archiva o reactiva. El PUT reemplaza todo y el listado no trae el saldo inicial, asi que se
 * lee la ficha antes de mandarla con `archivada` cambiado.
 */
export function useArchivarCuenta() {
  const invalidar = useInvalidar();
  return useMutation({
    mutationFn: async ({ id, archivada }: { id: number; archivada: boolean }) => {
      const c = await api<CuentaForm>(`${BASE}/cuenta/${id}`);
      const cuerpo: CuentaRequest = {
        nombre: c.nombre,
        tipo: c.tipo,
        saldoInicial: c.saldoInicial,
        color: c.color,
        icono: c.icono,
        banco: c.banco,
        archivada,
      };
      return api<CuentaForm>(`${BASE}/cuenta/${id}`, { metodo: 'PUT', cuerpo });
    },
    onSuccess: invalidar,
  });
}

/** `alBorrar` cierra el dialogo antes de invalidar, como en useBorrarMovimiento. */
export function useBorrarCuenta(alBorrar?: () => void) {
  const qc = useQueryClient();
  const invalidar = useInvalidar();
  return useMutation({
    mutationFn: (id: number) => api<void>(`${BASE}/cuenta/${id}`, { metodo: 'DELETE' }),
    onSuccess: (_, id) => {
      alBorrar?.();
      qc.removeQueries({ queryKey: clavesCuentas.cuenta(id) });
      return invalidar();
    },
  });
}

export function useCrearTransferencia() {
  const invalidar = useInvalidar();
  return useMutation({
    mutationFn: (cuerpo: TransferenciaRequest) => api<TransferenciaList>(`${BASE}/transferencia`, { metodo: 'POST', cuerpo }),
    onSuccess: invalidar,
  });
}

export function useActualizarTransferencia(id: number) {
  const invalidar = useInvalidar();
  return useMutation({
    mutationFn: (cuerpo: TransferenciaRequest) =>
      api<TransferenciaList>(`${BASE}/transferencia/${id}`, { metodo: 'PUT', cuerpo }),
    onSuccess: invalidar,
  });
}

export function useBorrarTransferencia(alBorrar?: () => void) {
  const invalidar = useInvalidar();
  return useMutation({
    mutationFn: (id: number) => api<void>(`${BASE}/transferencia/${id}`, { metodo: 'DELETE' }),
    onSuccess: () => {
      alBorrar?.();
      return invalidar();
    },
  });
}

/** Mensaje legible de un fallo de la API de cuentas o transferencias (el backend escribe sin tildes). */
export function mensajeErrorCuentas(e: unknown): string {
  if (e instanceof ApiError) {
    if (e.status === 409) return 'Ya tienes una cuenta con ese nombre.';
    if (e.status === 400 && /archiva/i.test(e.message)) {
      return 'Tiene movimientos, recurrentes o transferencias: archívala en vez de borrarla.';
    }
    if (e.status === 400 && /distintas/i.test(e.message)) return 'El origen y el destino tienen que ser cuentas distintas.';
    if (e.status === 404 && /transferencia/i.test(e.message)) return 'Esa transferencia ya no existe.';
    if (e.status === 404) return 'Esa cuenta ya no existe.';
    return e.message;
  }
  return 'No se ha podido conectar con el servidor.';
}
