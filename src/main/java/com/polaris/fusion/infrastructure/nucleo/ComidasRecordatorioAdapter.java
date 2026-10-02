package com.polaris.fusion.infrastructure.nucleo;

import com.polaris.fusion.application.in.ListComidaInterface;
import com.polaris.fusion.domain.model.ComidaFilter;
import com.polaris.nucleo.application.out.ComprobarRecordatorioPort;
import com.polaris.nucleo.domain.model.AvisoRecordatorio;
import com.polaris.nucleo.domain.model.TipoRecordatorio;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Optional;

/**
 * Recordatorio de comidas: pendiente mientras no haya ninguna comida
 * apuntada ese dia. Fusion responde a Nucleo con sus propios casos de uso.
 * Ver docs/decisiones/044-recordatorios.md.
 */
@Component
@RequiredArgsConstructor
public class ComidasRecordatorioAdapter implements ComprobarRecordatorioPort {

    private final ListComidaInterface listComida;

    @Override
    public TipoRecordatorio tipo() {
        return TipoRecordatorio.COMIDAS;
    }

    @Override
    public Optional<AvisoRecordatorio> pendiente(Long usuarioId, LocalDate fecha) {
        ComidaFilter filtro = ComidaFilter.builder().fecha(fecha).build();
        if (!listComida.list(usuarioId, filtro).isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(new AvisoRecordatorio(TipoRecordatorio.COMIDAS, "Apunta tus comidas",
                "Hoy aún no has registrado ninguna comida en Fusión.", "/fusion"));
    }
}
