package com.polaris.auth.infrastructure.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Lee el "Authorization: Bearer ..." y, si el token es valido, deja el usuarioId
 * como principal en el contexto de seguridad. De ahi lo saca UsuarioActual.
 *
 * <p>Si no hay cabecera o el token no vale, no lanza: simplemente no autentica y
 * deja que la cadena decida. Un endpoint publico seguira funcionando.
 *
 * <p>Sin @Component a proposito: lo instancia SecurityConfig. Un Filter declarado
 * como bean lo registraria ademas el contenedor de servlets por su cuenta, fuera
 * de la cadena de Spring Security y aplicandose a rutas que no le tocan.
 */
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String CABECERA = "Authorization";
    private static final String PREFIJO = "Bearer ";

    private final JwtService jwtService;

    public JwtAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain chain) throws ServletException, IOException {

        String cabecera = request.getHeader(CABECERA);

        if (cabecera != null && cabecera.startsWith(PREFIJO)) {
            String token = cabecera.substring(PREFIJO.length());

            jwtService.validarYExtraerUsuarioId(token).ifPresent(usuarioId -> {
                var auth = new UsernamePasswordAuthenticationToken(usuarioId, null, List.of());
                auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(auth);
            });
        }

        chain.doFilter(request, response);
    }
}
