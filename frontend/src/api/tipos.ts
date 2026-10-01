// Contratos de la API. Reflejan los DTO del backend; si cambian alli, cambian aqui.

/** Cuerpo de todo error de la API (shared/error/ErrorResponse). */
export interface ErrorResponse {
  timestamp: string;
  status: number;
  error: string;
  path: string;
}

/** POST /api/auth/login */
export interface TokenDto {
  token: string;
  tipo: 'Bearer';
  expiraEnSegundos: number;
}

/** GET /api/auth/usuario */
export interface UsuarioDto {
  id: number;
  username: string;
  email: string;
  nombre?: string;
  avatarUrl?: string;
  creadoEn: string;
  emailVerificado: boolean;
  tieneGoogle: boolean;
  tienePassword: boolean;
}
