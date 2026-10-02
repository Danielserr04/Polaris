package com.polaris.fusion.infrastructure.nucleo;

import com.polaris.fusion.application.out.DatosCorporalesPort;
import com.polaris.fusion.domain.model.DatosCorporales;
import com.polaris.nucleo.application.in.GetPerfilInterface;
import com.polaris.nucleo.domain.model.PerfilNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Lee el perfil de Nucleo por su caso de uso de entrada, nunca por su
 * repositorio. Como NucleoPesoCorporalAdapter. Ver
 * docs/decisiones/043-calculo-del-objetivo-desde-el-perfil.md.
 */
@Component
@RequiredArgsConstructor
public class NucleoDatosCorporalesAdapter implements DatosCorporalesPort {

    private final GetPerfilInterface getPerfil;
    private final DatosCorporalesNucleoMapper mapper;

    @Override
    public Optional<DatosCorporales> find(Long usuarioId) {
        try {
            return Optional.of(mapper.toDatosCorporales(getPerfil.get(usuarioId)));
        } catch (PerfilNotFoundException e) {
            return Optional.empty();
        }
    }
}
