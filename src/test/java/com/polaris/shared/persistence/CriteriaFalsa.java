package com.polaris.shared.persistence;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.mockito.invocation.InvocationOnMock;
import org.springframework.data.jpa.domain.Specification;

import java.util.Arrays;
import java.util.stream.Collectors;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.withSettings;

/**
 * Apoyo para probar las Specifications sin base de datos: ejecuta la
 * Specification contra un Root y un CriteriaBuilder falsos que, en vez de
 * construir SQL, devuelven una descripcion textual del predicado. Asi el test
 * lee lo que la Specification pide, por ejemplo
 * {@code and(equal(usuarioId, 1), equal(tipo, GASTO))}, y cubre los campos, los
 * valores, el orden y el escape del LIKE.
 *
 * <p>Las rutas anidadas salen con punto: {@code root.get("categoria").get("id")}
 * se describe como {@code categoria.id}. Un filtro ausente no aporta nada; si
 * ninguna condicion aplica, el resultado es null.
 */
public final class CriteriaFalsa {

    private CriteriaFalsa() {
    }

    /** Descripcion del predicado que construye la Specification, o null si no filtra nada. */
    @SuppressWarnings("unchecked")
    public static <E> String describir(Specification<E> especificacion) {
        Root<E> root = mock(Root.class, withSettings().defaultAnswer(inv -> respuestaDePath("", inv)));
        CriteriaQuery<?> query = mock(CriteriaQuery.class);
        CriteriaBuilder cb = mock(CriteriaBuilder.class, withSettings().defaultAnswer(CriteriaFalsa::respuestaDeBuilder));

        Predicate predicado = especificacion.toPredicate(root, query, cb);
        return predicado == null ? null : predicado.toString();
    }

    private static Object respuestaDePath(String ruta, InvocationOnMock inv) {
        String metodo = inv.getMethod().getName();
        if (metodo.equals("toString")) {
            return ruta;
        }
        if (metodo.equals("get") && inv.getArguments().length == 1 && inv.getArgument(0) instanceof String campo) {
            return path(ruta.isEmpty() ? campo : ruta + "." + campo);
        }
        return null;
    }

    @SuppressWarnings("rawtypes")
    private static Path path(String ruta) {
        return mock(Path.class, withSettings().defaultAnswer(inv -> respuestaDePath(ruta, inv)));
    }

    private static Object respuestaDeBuilder(InvocationOnMock inv) {
        String metodo = inv.getMethod().getName();
        if (metodo.equals("toString")) {
            return "CriteriaBuilder";
        }
        String descripcion = metodo + "(" + Arrays.stream(inv.getArguments())
                .map(String::valueOf)
                .collect(Collectors.joining(", ")) + ")";
        Class<?> devuelve = inv.getMethod().getReturnType();
        if (Predicate.class.isAssignableFrom(devuelve)) {
            return mock(Predicate.class, withSettings().defaultAnswer(i -> descripcionComoToString(descripcion, i)));
        }
        if (Expression.class.isAssignableFrom(devuelve)) {
            return mock(Expression.class, withSettings().defaultAnswer(i -> descripcionComoToString(descripcion, i)));
        }
        return null;
    }

    private static Object descripcionComoToString(String descripcion, InvocationOnMock inv) {
        return inv.getMethod().getName().equals("toString") ? descripcion : null;
    }
}
