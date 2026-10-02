package com.polaris.nucleo.infrastructure.push;

import com.polaris.nucleo.application.out.ClavePushPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Las claves VAPID con las que se firman los avisos. Si llegan por variables
 * de entorno se usan esas; si no, se leen de la tabla push_vapid y, la
 * primera vez, se generan y se guardan ahi. Tienen que ser siempre las
 * mismas: si cambian, los dispositivos suscritos dejan de recibir avisos
 * hasta que se vuelvan a suscribir. Ver docs/decisiones/045-recordatorios.md.
 */
@Slf4j
@Component
public class VapidClavesAdapter implements ClavePushPort {

    private final JdbcTemplate jdbc;
    private final String publicaEntorno;
    private final String privadaEntorno;
    private volatile String[] claves;

    public VapidClavesAdapter(JdbcTemplate jdbc,
                              @Value("${polaris.push.vapid-publica:}") String publicaEntorno,
                              @Value("${polaris.push.vapid-privada:}") String privadaEntorno) {
        this.jdbc = jdbc;
        this.publicaEntorno = publicaEntorno;
        this.privadaEntorno = privadaEntorno;
    }

    @Override
    public String clavePublica() {
        return claves()[0];
    }

    public String clavePrivada() {
        return claves()[1];
    }

    private String[] claves() {
        String[] c = claves;
        if (c == null) {
            synchronized (this) {
                if (claves == null) {
                    claves = cargar();
                }
                c = claves;
            }
        }
        return c;
    }

    private String[] cargar() {
        if (!publicaEntorno.isBlank() && !privadaEntorno.isBlank()) {
            return new String[]{publicaEntorno.trim(), privadaEntorno.trim()};
        }
        List<String[]> guardadas = jdbc.query("select publica, privada from push_vapid where id = 1",
                (rs, i) -> new String[]{rs.getString(1), rs.getString(2)});
        if (!guardadas.isEmpty()) {
            return guardadas.get(0);
        }
        String[] nuevas = WebPushCifrado.generarClaves();
        // insert ignore: si otra instancia gano la carrera, se usan las suyas.
        jdbc.update("insert ignore into push_vapid (id, publica, privada) values (1, ?, ?)", nuevas[0], nuevas[1]);
        log.info("Push: generadas claves VAPID nuevas y guardadas en push_vapid");
        return jdbc.queryForObject("select publica, privada from push_vapid where id = 1",
                (rs, i) -> new String[]{rs.getString(1), rs.getString(2)});
    }
}
