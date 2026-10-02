package com.polaris.atlas.infrastructure.nucleo;

import com.polaris.atlas.application.in.ListSesionInterface;
import com.polaris.atlas.domain.model.SesionFilter;
import com.polaris.nucleo.application.out.ComprobarRecordatorioPort;
import com.polaris.nucleo.domain.model.AvisoRecordatorio;
import com.polaris.nucleo.domain.model.TipoRecordatorio;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Optional;

/**
 * Recordatorio de entreno: pendiente mientras no haya ninguna sesion ese
 * dia. Atlas responde a Nucleo con sus propios casos de uso. Ver
 * docs/decisiones/044-recordatorios.md.
 */
@Component
@RequiredArgsConstructor
public class EntrenoRecordatorioAdapter implements ComprobarRecordatorioPort {

    private final ListSesionInterface listSesion;

    @Override
    public TipoRecordatorio tipo() {
        return TipoRecordatorio.ENTRENO;
    }

    @Override
    public Optional<AvisoRecordatorio> pendiente(Long usuarioId, LocalDate fecha) {
        SesionFilter filtro = SesionFilter.builder().desde(fecha).hasta(fecha).build();
        if (!listSesion.list(usuarioId, filtro).isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(new AvisoRecordatorio(TipoRecordatorio.ENTRENO, "Hoy toca entrenar",
                "Aún no has registrado la sesión de hoy en Atlas.", "/atlas"));
    }
}
