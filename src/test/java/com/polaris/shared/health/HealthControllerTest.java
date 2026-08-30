package com.polaris.shared.health;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientAutoConfiguration;
import org.springframework.boot.autoconfigure.security.oauth2.client.servlet.OAuth2ClientWebSecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Sin filtros de seguridad: aqui se prueba la logica del controller (200 contra
 * 503 segun conteste MySQL), no quien puede llamarlo. Que /health sea publico lo
 * decide SecurityConfig y se comprueba en JwtAuthenticationFilterTest.
 *
 * <p>Sin addFilters = false, al entrar Spring Security en B1 este slice aplicaria
 * la cadena por defecto y devolveria 401 en ambos casos.
 */
@WebMvcTest(controllers = HealthController.class,
        excludeAutoConfiguration = {
                OAuth2ClientAutoConfiguration.class,
                OAuth2ClientWebSecurityAutoConfiguration.class
        })
@AutoConfigureMockMvc(addFilters = false)
class HealthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DataSource dataSource;

    @Test
    void devuelve200CuandoLaBaseDeDatosResponde() throws Exception {
        Connection conexion = mock(Connection.class);
        when(conexion.isValid(anyInt())).thenReturn(true);
        when(dataSource.getConnection()).thenReturn(conexion);

        mockMvc.perform(get("/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.app").value("polaris"))
                .andExpect(jsonPath("$.database").value("UP"));
    }

    @Test
    void devuelve503CuandoMySqlNoContesta() throws Exception {
        when(dataSource.getConnection()).thenThrow(new SQLException("conexion rechazada"));

        mockMvc.perform(get("/health"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.status").value("DOWN"))
                .andExpect(jsonPath("$.database").value("DOWN"));
    }
}
