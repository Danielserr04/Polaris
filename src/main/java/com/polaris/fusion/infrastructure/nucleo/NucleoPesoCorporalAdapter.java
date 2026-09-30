package com.polaris.fusion.infrastructure.nucleo;

import com.polaris.fusion.application.out.PesoCorporalPort;
import com.polaris.fusion.domain.model.PesoCorporal;
import com.polaris.fusion.domain.model.PesoCorporalFilter;
import com.polaris.nucleo.application.in.CreateRegistroPesoInterface;
import com.polaris.nucleo.application.in.ListRegistroPesoInterface;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * El unico sitio de Fusion que conoce clases de Nucleo. Llama a sus casos de
 * uso de entrada ({@code application/in}), que es lo que Nucleo expone: nunca a
 * su repositorio ni a su entidad. El dato vive una vez, en registro_peso.
 * Ver docs/decisiones/022-peso-corporal-desde-fusion-y-atlas.md.
 */
@Component
@RequiredArgsConstructor
public class NucleoPesoCorporalAdapter implements PesoCorporalPort {

    private final ListRegistroPesoInterface listRegistroPeso;
    private final CreateRegistroPesoInterface createRegistroPeso;
    private final PesoCorporalNucleoMapper mapper;

    @Override
    public List<PesoCorporal> findAll(Long usuarioId, PesoCorporalFilter filter) {
        return mapper.toPesoCorporalList(
                listRegistroPeso.list(usuarioId, mapper.toRegistroPesoFilter(filter)));
    }

    @Override
    public PesoCorporal registrar(Long usuarioId, PesoCorporal peso) {
        return mapper.toPesoCorporal(
                createRegistroPeso.create(usuarioId, mapper.toRegistroPeso(peso)));
    }
}
