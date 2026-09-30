package com.polaris.fusion.domain.service;

import com.polaris.fusion.application.in.BuscarCatalogoAlimentoInterface;
import com.polaris.fusion.application.in.ImportarAlimentoInterface;
import com.polaris.fusion.application.out.AlimentoRepositoryPort;
import com.polaris.fusion.application.out.CatalogoAlimentoExternoPort;
import com.polaris.fusion.domain.model.Alimento;
import com.polaris.fusion.domain.model.ImportacionAlimento;
import com.polaris.fusion.domain.model.ResultadoCatalogoAlimento;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Busca en la API externa de alimentos e importa fichas al catalogo.
 *
 * <p>Catalogo compartido: no hay usuarioId. Importar dos veces la misma ficha
 * (o que la importen dos usuarios) devuelve el mismo Alimento.
 */
@Service
@RequiredArgsConstructor
public class CatalogoAlimentoService implements BuscarCatalogoAlimentoInterface, ImportarAlimentoInterface {

    private final CatalogoAlimentoExternoPort fuente;
    private final AlimentoRepositoryPort repository;

    /** Marca los resultados ya importados con el id del Alimento ("anadir" vs "ya lo tienes"). */
    @Override
    public List<ResultadoCatalogoAlimento> buscar(String texto) {
        List<ResultadoCatalogoAlimento> resultados = fuente.buscar(texto);

        resultados.forEach(resultado -> repository
                .findByFuenteExternaAndIdExterno(resultado.getFuenteExterna(), resultado.getIdExterno())
                .ifPresent(alimento -> resultado.setAlimentoId(alimento.getId())));

        return resultados;
    }

    /** Si ya existe, ni siquiera se llama a la API externa. */
    @Override
    public ImportacionAlimento importar(String idExterno) {
        Optional<Alimento> existente = repository.findByFuenteExternaAndIdExterno(fuente.fuente(), idExterno);
        if (existente.isPresent()) {
            return new ImportacionAlimento(existente.get(), false);
        }

        Alimento ficha = fuente.obtener(idExterno);
        ficha.setId(null);
        ficha.setFuenteExterna(fuente.fuente());
        ficha.setIdExterno(idExterno);
        return new ImportacionAlimento(repository.save(ficha), true);
    }
}
