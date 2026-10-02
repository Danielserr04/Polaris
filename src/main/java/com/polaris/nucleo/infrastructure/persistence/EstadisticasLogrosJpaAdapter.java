package com.polaris.nucleo.infrastructure.persistence;

import com.polaris.nucleo.application.out.EstadisticasLogrosPort;
import com.polaris.nucleo.domain.model.EstadisticasLogros;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.stream.Stream;

/** Las fechas de pesos y medidas, y lo relleno del perfil, para los logros de Nucleo. */
@Component("nucleoEstadisticasLogrosJpaAdapter")
@RequiredArgsConstructor
public class EstadisticasLogrosJpaAdapter implements EstadisticasLogrosPort {

    private final RegistroPesoRepository registroPesoRepository;
    private final MedidaCorporalRepository medidaCorporalRepository;
    private final PerfilRepository perfilRepository;

    @Override
    public EstadisticasLogros find(Long usuarioId) {
        int campos = perfilRepository.findByUsuarioId(usuarioId)
                .map(p -> (int) Stream.of(p.getAlturaCm(), p.getFechaNacimiento(), p.getSexo(), p.getNivelActividad())
                        .filter(Objects::nonNull).count())
                .orElse(0);
        return EstadisticasLogros.builder()
                .fechasPeso(registroPesoRepository.findFechas(usuarioId))
                .fechasMedida(medidaCorporalRepository.findFechas(usuarioId))
                .camposPerfil(campos)
                .build();
    }
}
