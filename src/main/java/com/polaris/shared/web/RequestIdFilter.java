package com.polaris.shared.web;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Da a cada peticion un identificador de correlacion: lo pone en el MDC de SLF4J
 * (clave {@code requestId}, que el patron de logging imprime en cada linea) y lo
 * devuelve en la cabecera de respuesta {@code X-Request-Id}. Con el id que ve el
 * cliente se encuentran en el log todas las lineas de esa peticion.
 *
 * <p>Si el cliente manda {@code X-Request-Id} y es valido ({@link #VALIDO}) se
 * respeta, para poder seguir una peticion desde el front; si no (vacio, largo,
 * con saltos de linea u otros caracteres) se ignora y se genera un UUID. Un valor
 * del cliente nunca llega al log sin pasar por esa validacion: sin ella, un
 * {@code \r\n} en la cabecera permitiria falsificar lineas de log.
 *
 * <p>Orden de maxima prioridad: corre antes que la cadena de Spring Security
 * (orden -100), asi que tambien llevan id los 401 y 403 que genera Security sin
 * llegar a ningun controller. Es un Filter de servlet, no parte de la cadena de
 * Security, y por eso es un {@code @Component} (al contrario que
 * JwtAuthenticationFilter). Ver docs/decisiones/029-logs-y-requestid.md.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RequestIdFilter extends OncePerRequestFilter {

    public static final String CABECERA = "X-Request-Id";
    public static final String CLAVE_MDC = "requestId";

    /** Atributo de la peticion: el redespacho de error reutiliza el mismo id. */
    static final String ATRIBUTO = RequestIdFilter.class.getName() + ".REQUEST_ID";

    /** Formato admitido de un id enviado por el cliente. */
    static final Pattern VALIDO = Pattern.compile("[A-Za-z0-9_-]{1,64}");

    /** Por defecto no filtra el redespacho de error (/error); asi tambien lleva id. */
    @Override
    protected boolean shouldNotFilterErrorDispatch() {
        return false;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain chain) throws ServletException, IOException {
        String id = resolverId(request);
        request.setAttribute(ATRIBUTO, id);
        MDC.put(CLAVE_MDC, id);
        response.setHeader(CABECERA, id);
        try {
            chain.doFilter(request, response);
        } finally {
            MDC.remove(CLAVE_MDC);
        }
    }

    private String resolverId(HttpServletRequest request) {
        if (request.getAttribute(ATRIBUTO) instanceof String existente) {
            return existente;
        }
        String entrante = request.getHeader(CABECERA);
        if (entrante != null && VALIDO.matcher(entrante).matches()) {
            return entrante;
        }
        return UUID.randomUUID().toString();
    }
}
