package com.polaris.nucleo.domain.service;

import com.polaris.nucleo.application.in.DescartarRecordatorioInterface;
import com.polaris.nucleo.application.in.EnviarPushPruebaInterface;
import com.polaris.nucleo.application.in.EnviarRecordatoriosInterface;
import com.polaris.nucleo.application.in.GetRecordatorioInterface;
import com.polaris.nucleo.application.in.ListRecordatorioInterface;
import com.polaris.nucleo.application.in.ListRecordatorioPendienteInterface;
import com.polaris.nucleo.application.in.UpdateRecordatorioInterface;
import com.polaris.nucleo.application.out.ComprobarRecordatorioPort;
import com.polaris.nucleo.application.out.EnviarPushPort;
import com.polaris.nucleo.application.out.RecordatorioRepositoryPort;
import com.polaris.nucleo.application.out.SuscripcionPushRepositoryPort;
import com.polaris.nucleo.domain.model.AvisoRecordatorio;
import com.polaris.nucleo.domain.model.Recordatorio;
import com.polaris.nucleo.domain.model.TipoRecordatorio;
import com.polaris.shared.error.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Recordatorios del usuario del JWT: uno por tipo. Los que no ha guardado
 * existen igual, con sus valores por defecto, para que el frontend no tenga
 * que conocerlos.
 *
 * <p>Si un recordatorio sigue pendiente lo dice el modulo dueno del dato
 * (ComprobarRecordatorioPort); un tipo sin comprobador no avisa nunca. Ver
 * docs/decisiones/044-recordatorios.md.
 */
@Slf4j
@Service
public class RecordatorioService implements
        GetRecordatorioInterface,
        ListRecordatorioInterface,
        UpdateRecordatorioInterface,
        ListRecordatorioPendienteInterface,
        DescartarRecordatorioInterface,
        EnviarRecordatoriosInterface,
        EnviarPushPruebaInterface {

    private final RecordatorioRepositoryPort repository;
    private final SuscripcionPushRepositoryPort suscripciones;
    private final EnviarPushPort push;
    private final Map<TipoRecordatorio, ComprobarRecordatorioPort> comprobadores;

    public RecordatorioService(RecordatorioRepositoryPort repository,
                               SuscripcionPushRepositoryPort suscripciones,
                               EnviarPushPort push,
                               List<ComprobarRecordatorioPort> comprobadores) {
        this.repository = repository;
        this.suscripciones = suscripciones;
        this.push = push;
        this.comprobadores = new EnumMap<>(TipoRecordatorio.class);
        comprobadores.forEach(c -> this.comprobadores.put(c.tipo(), c));
    }

    @Override
    public Recordatorio get(Long usuarioId, TipoRecordatorio tipo) {
        return repository.findByUsuarioIdAndTipo(usuarioId, tipo)
                .orElseGet(() -> tipo.porDefecto(usuarioId));
    }

    @Override
    public List<Recordatorio> list(Long usuarioId) {
        Map<TipoRecordatorio, Recordatorio> guardados = repository.findAllByUsuarioId(usuarioId).stream()
                .collect(Collectors.toMap(Recordatorio::getTipo, Function.identity()));
        return Arrays.stream(TipoRecordatorio.values())
                .map(tipo -> guardados.getOrDefault(tipo, tipo.porDefecto(usuarioId)))
                .toList();
    }

    /**
     * Si ya existe, conserva su id (para que el save actualice y no inserte
     * otra fila, que el unique de usuario y tipo rechazaria) y sus marcas de
     * dia: cambiar la hora no vuelve a mandar el aviso de hoy.
     */
    @Override
    public Recordatorio update(Long usuarioId, Recordatorio recordatorio) {
        if (recordatorio.getTipo() == null) {
            throw new ValidationException("Falta el tipo de recordatorio");
        }
        if (recordatorio.getHora() == null) {
            throw new ValidationException("Falta la hora del recordatorio");
        }
        if (recordatorio.getDias() == null || recordatorio.getDias().isEmpty()) {
            throw new ValidationException("Elige al menos un día de la semana");
        }
        Optional<Recordatorio> existente = repository.findByUsuarioIdAndTipo(usuarioId, recordatorio.getTipo());
        recordatorio.setId(existente.map(Recordatorio::getId).orElse(null));
        recordatorio.setAvisadoEn(existente.map(Recordatorio::getAvisadoEn).orElse(null));
        recordatorio.setDescartadoEn(existente.map(Recordatorio::getDescartadoEn).orElse(null));
        recordatorio.setUsuarioId(usuarioId);
        recordatorio.setHora(recordatorio.getHora().withSecond(0).withNano(0));
        return repository.save(recordatorio);
    }

    @Override
    public List<AvisoRecordatorio> pendientes(Long usuarioId, LocalDateTime ahora) {
        return pendientesConRecordatorio(usuarioId, ahora).stream()
                .map(Pendiente::aviso)
                .toList();
    }

    @Override
    public void descartar(Long usuarioId, TipoRecordatorio tipo, LocalDate fecha) {
        Recordatorio recordatorio = get(usuarioId, tipo);
        recordatorio.setDescartadoEn(fecha);
        repository.save(recordatorio);
    }

    /**
     * Cada usuario va en su propio try: un fallo con uno no deja sin aviso a
     * los demas. avisadoEn se guarda aunque el push no llegue a ningun movil,
     * para no reintentarlo cada minuto el resto del dia.
     */
    @Override
    public int enviar(LocalDateTime ahora) {
        int enviados = 0;
        for (Long usuarioId : suscripciones.findUsuarioIds()) {
            try {
                for (Pendiente p : pendientesConRecordatorio(usuarioId, ahora)) {
                    if (ahora.toLocalDate().equals(p.recordatorio().getAvisadoEn())) {
                        continue;
                    }
                    push.enviar(usuarioId, p.aviso());
                    p.recordatorio().setAvisadoEn(ahora.toLocalDate());
                    repository.save(p.recordatorio());
                    enviados++;
                }
            } catch (RuntimeException e) {
                log.warn("Recordatorios: fallo con el usuario {}: {}", usuarioId, e.getMessage());
            }
        }
        return enviados;
    }

    @Override
    public int probar(Long usuarioId) {
        return push.enviar(usuarioId, new AvisoRecordatorio(null, "Polaris",
                "Así te llegarán los recordatorios a este dispositivo.", "/perfil"));
    }

    /** Un comprobador que falla cuenta como "nada pendiente": mejor callar que avisar mal. */
    private List<Pendiente> pendientesConRecordatorio(Long usuarioId, LocalDateTime ahora) {
        LocalDate fecha = ahora.toLocalDate();
        List<Pendiente> resultado = new ArrayList<>();
        for (Recordatorio r : list(usuarioId)) {
            ComprobarRecordatorioPort comprobador = comprobadores.get(r.getTipo());
            if (comprobador == null || !r.tocaEn(fecha, ahora.toLocalTime())) {
                continue;
            }
            try {
                comprobador.pendiente(usuarioId, fecha).ifPresent(a -> resultado.add(new Pendiente(r, a)));
            } catch (RuntimeException e) {
                log.warn("Recordatorios: no se ha podido comprobar {} del usuario {}: {}",
                        r.getTipo(), usuarioId, e.getMessage());
            }
        }
        return resultado;
    }

    private record Pendiente(Recordatorio recordatorio, AvisoRecordatorio aviso) {
    }
}
