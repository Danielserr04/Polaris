package com.polaris.odisea.domain.service;

import com.polaris.odisea.application.in.BuscarCatalogoInterface;
import com.polaris.odisea.application.in.ImportarEntradaInterface;
import com.polaris.odisea.application.out.CatalogoExternoPort;
import com.polaris.odisea.application.out.EntradaRepositoryPort;
import com.polaris.odisea.application.out.TituloRepositoryPort;
import com.polaris.odisea.domain.model.Entrada;
import com.polaris.odisea.domain.model.EstadoEntrada;
import com.polaris.odisea.domain.model.ResultadoCatalogo;
import com.polaris.odisea.domain.model.TipoContenido;
import com.polaris.odisea.domain.model.Titulo;
import com.polaris.shared.error.DuplicateResourceException;
import com.polaris.shared.error.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Busca en las fuentes externas e importa a tu lista.
 *
 * <p>Recibe la lista de adaptadores y elige por tipo de contenido: no sabe si
 * detras hay TMDB o cualquier otra cosa. Spring inyecta todos los beans de
 * CatalogoExternoPort que existan, asi que anadir una fuente es escribir un
 * adaptador y nada mas.
 */
@Service
@RequiredArgsConstructor
public class CatalogoService implements BuscarCatalogoInterface, ImportarEntradaInterface {

    private final List<CatalogoExternoPort> fuentes;
    private final TituloRepositoryPort tituloRepository;
    private final EntradaRepositoryPort entradaRepository;

    /**
     * Marca los resultados que ya estan importados con el id del Titulo, para
     * que el frontend pueda distinguir "anadir" de "ya lo tienes".
     */
    @Override
    public List<ResultadoCatalogo> buscar(String texto, TipoContenido tipo) {
        CatalogoExternoPort fuente = fuentePara(tipo);

        List<ResultadoCatalogo> resultados = fuente.buscar(texto, tipo);

        resultados.forEach(resultado -> tituloRepository
                .findByIdExternoAndFuenteExterna(resultado.getIdExterno(), resultado.getFuenteExterna())
                .ifPresent(titulo -> resultado.setTituloId(titulo.getId())));

        return resultados;
    }

    /**
     * El Titulo es catalogo compartido: si otro usuario ya importo esa ficha,
     * se reutiliza. Lo que se crea siempre es la Entrada, que es lo tuyo.
     */
    @Override
    public Entrada importar(Long usuarioId, String idExterno, TipoContenido tipo) {
        CatalogoExternoPort fuente = fuentePara(tipo);

        Titulo titulo = tituloRepository
                .findByIdExternoAndFuenteExterna(idExterno, fuente.fuente())
                .orElseGet(() -> tituloRepository.save(fuente.obtener(idExterno, tipo)));

        if (entradaRepository.existsByUsuarioIdAndTituloId(usuarioId, titulo.getId())) {
            throw new DuplicateResourceException("Ya tienes ese titulo en tu lista");
        }

        Entrada entrada = Entrada.builder()
                .usuarioId(usuarioId)
                .tituloId(titulo.getId())
                .estado(EstadoEntrada.PENDIENTE)
                .favorito(false)
                .build();

        return entradaRepository.save(entrada);
    }

    /**
     * 400 y no 404: el tipo existe, lo que no hay es fuente para el. Juegos y
     * libros caen aqui hasta que se decidan, ver docs/modulos/odisea.md.
     */
    private CatalogoExternoPort fuentePara(TipoContenido tipo) {
        return fuentes.stream()
                .filter(candidata -> candidata.soporta(tipo))
                .findFirst()
                .orElseThrow(() -> new ValidationException(
                        "Todavia no hay catalogo externo para el tipo " + tipo));
    }
}
