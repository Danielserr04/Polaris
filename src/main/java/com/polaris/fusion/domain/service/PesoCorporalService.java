package com.polaris.fusion.domain.service;

import com.polaris.fusion.application.in.CreatePesoCorporalInterface;
import com.polaris.fusion.application.in.ListPesoCorporalInterface;
import com.polaris.fusion.application.out.PesoCorporalPort;
import com.polaris.fusion.domain.model.PesoCorporal;
import com.polaris.fusion.domain.model.PesoCorporalFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Fusion ve y apunta el peso pero no lo guarda ni lo valida a su manera: las
 * reglas (un peso por dia, unicidad, aislamiento por usuario) son de Nucleo y
 * se aplican al otro lado del puerto. Aqui no hay logica que duplicar.
 * Ver docs/decisiones/022-peso-corporal-desde-fusion-y-atlas.md.
 */
@Service
@RequiredArgsConstructor
public class PesoCorporalService implements
        ListPesoCorporalInterface,
        CreatePesoCorporalInterface {

    private final PesoCorporalPort pesoCorporalPort;

    @Override
    public List<PesoCorporal> list(Long usuarioId, PesoCorporalFilter filter) {
        return pesoCorporalPort.findAll(usuarioId, filter);
    }

    @Override
    public PesoCorporal create(Long usuarioId, PesoCorporal peso) {
        return pesoCorporalPort.registrar(usuarioId, peso);
    }
}
