package com.polaris.shared.web;

import jakarta.servlet.DispatcherType;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RequestIdFilterTest {

    private final RequestIdFilter filtro = new RequestIdFilter();

    @AfterEach
    void limpiarMdc() {
        MDC.clear();
    }

    /** Ejecuta el filtro y devuelve el id que el MDC tenia DENTRO de la cadena. */
    private String ejecutar(MockHttpServletRequest peticion, MockHttpServletResponse respuesta) throws Exception {
        List<String> vistoEnLaCadena = new ArrayList<>();
        FilterChain cadena = (req, res) -> vistoEnLaCadena.add(MDC.get(RequestIdFilter.CLAVE_MDC));
        filtro.doFilter(peticion, respuesta, cadena);
        return vistoEnLaCadena.get(0);
    }

    private static MockHttpServletRequest conCabecera(String valor) {
        MockHttpServletRequest peticion = new MockHttpServletRequest("GET", "/api/algo");
        peticion.addHeader(RequestIdFilter.CABECERA, valor);
        return peticion;
    }

    @Test
    @DisplayName("sin cabecera genera un UUID, lo pone en el MDC y lo devuelve en la respuesta")
    void generaUnUuidSiNoViene() throws Exception {
        MockHttpServletResponse respuesta = new MockHttpServletResponse();

        String enElMdc = ejecutar(new MockHttpServletRequest("GET", "/api/algo"), respuesta);

        String enLaRespuesta = respuesta.getHeader(RequestIdFilter.CABECERA);
        assertThat(enLaRespuesta).isEqualTo(enElMdc);
        assertThat(UUID.fromString(enLaRespuesta)).isNotNull();
    }

    @Test
    @DisplayName("respeta un id valido del cliente")
    void respetaUnIdValido() throws Exception {
        MockHttpServletResponse respuesta = new MockHttpServletResponse();

        String enElMdc = ejecutar(conCabecera("front-7f3a_91"), respuesta);

        assertThat(enElMdc).isEqualTo("front-7f3a_91");
        assertThat(respuesta.getHeader(RequestIdFilter.CABECERA)).isEqualTo("front-7f3a_91");
    }

    @Test
    @DisplayName("acepta un id de exactamente 64 caracteres y rechaza uno de 65")
    void limiteDeLongitud() throws Exception {
        String sesentaYCuatro = "a".repeat(64);
        MockHttpServletResponse ok = new MockHttpServletResponse();
        ejecutar(conCabecera(sesentaYCuatro), ok);
        assertThat(ok.getHeader(RequestIdFilter.CABECERA)).isEqualTo(sesentaYCuatro);

        MockHttpServletResponse largo = new MockHttpServletResponse();
        ejecutar(conCabecera(sesentaYCuatro + "a"), largo);
        assertThat(largo.getHeader(RequestIdFilter.CABECERA)).isNotEqualTo(sesentaYCuatro + "a");
        assertThat(UUID.fromString(largo.getHeader(RequestIdFilter.CABECERA))).isNotNull();
    }

    @ParameterizedTest(name = "[{index}] se descarta {0}")
    @ValueSource(strings = {
            "abc\r\n2026-10-01 ERROR falso: login de admin",
            "abc\n",
            "\nabc",
            "abc\rdef",
            "con espacio",
            "a;b",
            "<script>",
            "../../etc/passwd",
            "x'; DROP TABLE usuario;--",
            "${jndi:ldap://evil/x}",
            "%0d%0aSet-Cookie:x=y",
            "ñandú",
            "abc\u0000def"
    })
    void descartaUnIdMalicioso(String malicioso) throws Exception {
        MockHttpServletResponse respuesta = new MockHttpServletResponse();

        String enElMdc = ejecutar(conCabecera(malicioso), respuesta);

        assertThat(enElMdc).isNotEqualTo(malicioso);
        assertThat(respuesta.getHeader(RequestIdFilter.CABECERA)).isEqualTo(enElMdc);
        assertThat(UUID.fromString(enElMdc)).isNotNull();
    }

    @Test
    @DisplayName("una cabecera vacia se descarta")
    void descartaUnIdVacio() throws Exception {
        MockHttpServletResponse respuesta = new MockHttpServletResponse();

        ejecutar(conCabecera(""), respuesta);

        assertThat(UUID.fromString(respuesta.getHeader(RequestIdFilter.CABECERA))).isNotNull();
    }

    @Test
    @DisplayName("dos peticiones sin cabecera reciben ids distintos")
    void idsDistintos() throws Exception {
        MockHttpServletResponse una = new MockHttpServletResponse();
        MockHttpServletResponse otra = new MockHttpServletResponse();

        ejecutar(new MockHttpServletRequest("GET", "/a"), una);
        ejecutar(new MockHttpServletRequest("GET", "/a"), otra);

        assertThat(una.getHeader(RequestIdFilter.CABECERA)).isNotEqualTo(otra.getHeader(RequestIdFilter.CABECERA));
    }

    @Test
    @DisplayName("limpia el MDC al terminar")
    void limpiaElMdc() throws Exception {
        ejecutar(new MockHttpServletRequest("GET", "/api/algo"), new MockHttpServletResponse());

        assertThat(MDC.get(RequestIdFilter.CLAVE_MDC)).isNull();
    }

    @Test
    @DisplayName("limpia el MDC aunque la cadena lance una excepcion, y la cabecera ya iba puesta")
    void limpiaElMdcSiLaCadenaFalla() {
        MockHttpServletResponse respuesta = new MockHttpServletResponse();
        FilterChain cadenaRota = (req, res) -> {
            throw new IllegalStateException("fallo");
        };

        assertThatThrownBy(() -> filtro.doFilter(new MockHttpServletRequest("GET", "/x"), respuesta, cadenaRota))
                .isInstanceOf(IllegalStateException.class);

        assertThat(MDC.get(RequestIdFilter.CLAVE_MDC)).isNull();
        assertThat(respuesta.getHeader(RequestIdFilter.CABECERA)).isNotNull();
    }

    @Test
    @DisplayName("el redespacho de error (/error) reutiliza el mismo id en vez de generar otro")
    void elRedespachoDeErrorReutilizaElId() throws Exception {
        MockHttpServletRequest peticion = new MockHttpServletRequest("GET", "/api/algo");
        String primero = ejecutar(peticion, new MockHttpServletResponse());

        peticion.setDispatcherType(DispatcherType.ERROR);
        MockHttpServletResponse respuestaDelError = new MockHttpServletResponse();
        String segundo = ejecutar(peticion, respuestaDelError);

        assertThat(segundo).isEqualTo(primero);
        assertThat(respuestaDelError.getHeader(RequestIdFilter.CABECERA)).isEqualTo(primero);
    }

    @Test
    @DisplayName("el filtro tiene la maxima prioridad: corre antes que la cadena de Spring Security (-100)")
    void ordenDeMaximaPrioridad() {
        Order orden = RequestIdFilter.class.getAnnotation(Order.class);

        assertThat(orden).isNotNull();
        assertThat(orden.value()).isEqualTo(Ordered.HIGHEST_PRECEDENCE);
        assertThat(orden.value()).isLessThan(-100);
    }
}
