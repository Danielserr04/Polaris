package com.polaris.kuiper.domain.service;

import com.polaris.kuiper.application.in.GetComparativaCategoriasInterface;
import com.polaris.kuiper.application.in.GetEvolucionInterface;
import com.polaris.kuiper.application.in.GetProyeccionInterface;
import com.polaris.kuiper.application.in.ListComerciosFrecuentesInterface;
import com.polaris.kuiper.application.in.ListInsightsInterface;
import com.polaris.kuiper.application.out.MovimientoRepositoryPort;
import com.polaris.kuiper.application.out.PresupuestoRepositoryPort;
import com.polaris.kuiper.application.out.RecurrenteRepositoryPort;
import com.polaris.kuiper.domain.model.Categoria;
import com.polaris.kuiper.domain.model.CategoriaComparada;
import com.polaris.kuiper.domain.model.ComercioFrecuente;
import com.polaris.kuiper.domain.model.ComparativaCategorias;
import com.polaris.kuiper.domain.model.EstadoProyeccion;
import com.polaris.kuiper.domain.model.EvolucionMensual;
import com.polaris.kuiper.domain.model.Insight;
import com.polaris.kuiper.domain.model.Movimiento;
import com.polaris.kuiper.domain.model.MovimientoFilter;
import com.polaris.kuiper.domain.model.PeriodoPresupuesto;
import com.polaris.kuiper.domain.model.Presupuesto;
import com.polaris.kuiper.domain.model.PresupuestoFilter;
import com.polaris.kuiper.domain.model.ProyeccionMensual;
import com.polaris.kuiper.domain.model.Recurrente;
import com.polaris.kuiper.domain.model.RecurrenteFilter;
import com.polaris.kuiper.domain.model.SeveridadInsight;
import com.polaris.kuiper.domain.model.TipoInsight;
import com.polaris.kuiper.domain.model.TipoMovimiento;
import com.polaris.shared.error.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.Normalizer;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Analisis de solo lectura sobre los movimientos, presupuestos y recurrentes
 * que ya existen. Como el resumen, agrega en Java: cada consulta es un rango
 * de fechas de un usuario que el indice (usuario_id, fecha) ya acota. No hay
 * tabla ni migracion. Ver docs/decisiones/037-analisis-calculado-en-servicio.md.
 */
@Service
@RequiredArgsConstructor
public class AnalisisService implements
        GetEvolucionInterface,
        GetComparativaCategoriasInterface,
        ListComerciosFrecuentesInterface,
        ListInsightsInterface,
        GetProyeccionInterface {

    public static final int MAX_MESES = 24;
    public static final int MAX_LIMITE = 50;

    /** Con escala 2 para que los totales salgan siempre como 0.00 y no como 0. */
    private static final BigDecimal CERO = BigDecimal.ZERO.setScale(2);
    private static final BigDecimal CIEN = BigDecimal.valueOf(100);
    /** Por encima o por debajo de este porcentaje frente a la media, el insight deja de ser neutro. */
    private static final BigDecimal UMBRAL_MEDIA = BigDecimal.TEN;
    private static final BigDecimal AHORRO_BUENO = BigDecimal.valueOf(20);
    private static final BigDecimal PRESUPUESTO_AJUSTADO = BigDecimal.valueOf(80);
    private static final int MESES_MEDIA = 3;
    private static final int DIAS_PROXIMOS = 7;
    private static final int MIN_GASTOS_DIA_SEMANA = 5;
    private static final int MAX_CARGOS_EN_TEXTO = 4;
    /** Freno para no iterar sin fin sobre un recurrente semanal muy atrasado. */
    private static final int MAX_CARGOS = 1000;

    private static final Locale ES = Locale.forLanguageTag("es-ES");

    private final MovimientoRepositoryPort movimientoRepository;
    private final PresupuestoRepositoryPort presupuestoRepository;
    private final RecurrenteRepositoryPort recurrenteRepository;

    // ------------------------------------------------------------ evolucion

    @Override
    public List<EvolucionMensual> get(Long usuarioId, YearMonth hasta, int meses) {
        if (meses < 1 || meses > MAX_MESES) {
            throw new ValidationException("meses debe estar entre 1 y " + MAX_MESES);
        }
        YearMonth desde = hasta.minusMonths(meses - 1L);
        Map<YearMonth, List<Movimiento>> porMes = porMes(
                movimientos(usuarioId, desde.atDay(1), hasta.atEndOfMonth()));

        List<EvolucionMensual> serie = new ArrayList<>();
        for (YearMonth mes = desde; !mes.isAfter(hasta); mes = mes.plusMonths(1)) {
            List<Movimiento> delMes = porMes.getOrDefault(mes, List.of());
            BigDecimal ingresos = total(delMes, TipoMovimiento.INGRESO);
            BigDecimal gastos = total(delMes, TipoMovimiento.GASTO);
            BigDecimal balance = ingresos.subtract(gastos);
            serie.add(EvolucionMensual.builder()
                    .periodo(mes)
                    .ingresos(ingresos)
                    .gastos(gastos)
                    .balance(balance)
                    .tasaAhorro(porcentaje(balance, ingresos))
                    .build());
        }
        return serie;
    }

    // ----------------------------------------------------------- categorias

    /**
     * El rango anterior tiene la misma duracion y acaba el dia antes de
     * {@code desde}. Si el rango son meses enteros, el anterior son los mismos
     * meses justo antes: septiembre se compara con agosto entero, no con 30 dias.
     */
    @Override
    public ComparativaCategorias get(Long usuarioId, LocalDate desde, LocalDate hasta) {
        validarRango(desde, hasta);
        LocalDate anteriorHasta = desde.minusDays(1);
        LocalDate anteriorDesde;
        if (sonMesesEnteros(desde, hasta)) {
            long meses = ChronoUnit.MONTHS.between(YearMonth.from(desde), YearMonth.from(hasta)) + 1;
            anteriorDesde = YearMonth.from(desde).minusMonths(meses).atDay(1);
        } else {
            anteriorDesde = desde.minusDays(ChronoUnit.DAYS.between(desde, hasta) + 1);
        }

        Map<Long, Categoria> categorias = new LinkedHashMap<>();
        Map<Long, BigDecimal> actual = new LinkedHashMap<>();
        Map<Long, BigDecimal> anterior = new LinkedHashMap<>();
        for (Movimiento m : gastos(movimientos(usuarioId, anteriorDesde, hasta))) {
            categorias.putIfAbsent(m.getCategoriaId(), m.getCategoria());
            Map<Long, BigDecimal> destino = m.getFecha().isBefore(desde) ? anterior : actual;
            destino.merge(m.getCategoriaId(), m.getImporte(), BigDecimal::add);
        }

        BigDecimal total = suma(actual.values());
        BigDecimal totalAnterior = suma(anterior.values());

        List<CategoriaComparada> filas = categorias.entrySet().stream()
                .map(e -> {
                    BigDecimal gastado = actual.getOrDefault(e.getKey(), CERO);
                    BigDecimal antes = anterior.getOrDefault(e.getKey(), CERO);
                    BigDecimal diferencia = gastado.subtract(antes);
                    return CategoriaComparada.builder()
                            .categoria(e.getValue())
                            .gastado(gastado)
                            .porcentaje(Optional.ofNullable(porcentaje(gastado, total)).orElse(CERO))
                            .gastadoAnterior(antes)
                            .diferencia(diferencia)
                            .variacion(porcentaje(diferencia, antes))
                            .build();
                })
                .sorted(Comparator.comparing(CategoriaComparada::getGastado).reversed()
                        .thenComparing(Comparator.comparing(CategoriaComparada::getGastadoAnterior).reversed())
                        .thenComparing(c -> c.getCategoria().getNombre(), String.CASE_INSENSITIVE_ORDER))
                .toList();

        return ComparativaCategorias.builder()
                .desde(desde)
                .hasta(hasta)
                .anteriorDesde(anteriorDesde)
                .anteriorHasta(anteriorHasta)
                .total(total)
                .totalAnterior(totalAnterior)
                .categorias(filas)
                .build();
    }

    // ------------------------------------------------------------ comercios

    /**
     * Solo gastos con concepto. "Mercadona", " mercadona " y "MERCADONA" son el
     * mismo comercio; se muestra la forma escrita que mas se repite.
     */
    @Override
    public List<ComercioFrecuente> list(Long usuarioId, LocalDate desde, LocalDate hasta, int limite) {
        validarRango(desde, hasta);
        if (limite < 1 || limite > MAX_LIMITE) {
            throw new ValidationException("limite debe estar entre 1 y " + MAX_LIMITE);
        }

        Map<String, Acumulado> porClave = new LinkedHashMap<>();
        for (Movimiento m : gastos(movimientos(usuarioId, desde, hasta))) {
            String escrito = limpiar(m.getConcepto());
            if (escrito.isEmpty()) {
                continue;
            }
            porClave.computeIfAbsent(clave(escrito), k -> new Acumulado()).sumar(escrito, m.getImporte());
        }

        return porClave.values().stream()
                .map(Acumulado::toComercio)
                .sorted(Comparator.comparing(ComercioFrecuente::getTotal).reversed()
                        .thenComparing(Comparator.comparingInt(ComercioFrecuente::getVeces).reversed())
                        .thenComparing(ComercioFrecuente::getNombre, String.CASE_INSENSITIVE_ORDER))
                .limit(limite)
                .toList();
    }

    /** Espacios de los extremos fuera y los de dentro colapsados a uno. */
    static String limpiar(String concepto) {
        return concepto == null ? "" : concepto.trim().replaceAll("\\s+", " ");
    }

    /** Clave de agrupacion: sin tildes ni mayusculas. */
    static String clave(String limpio) {
        return Normalizer.normalize(limpio, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT);
    }

    private static final class Acumulado {
        private final Map<String, Integer> variantes = new LinkedHashMap<>();
        private BigDecimal total = CERO;
        private int veces;

        void sumar(String escrito, BigDecimal importe) {
            variantes.merge(escrito, 1, Integer::sum);
            total = total.add(importe);
            veces++;
        }

        /** En un empate de variantes gana la primera vista: el listado llega del mas reciente al mas antiguo. */
        ComercioFrecuente toComercio() {
            String nombre = null;
            int max = 0;
            for (Map.Entry<String, Integer> v : variantes.entrySet()) {
                if (v.getValue() > max) {
                    nombre = v.getKey();
                    max = v.getValue();
                }
            }
            return ComercioFrecuente.builder()
                    .nombre(nombre)
                    .total(total)
                    .veces(veces)
                    .ticketMedio(total.divide(BigDecimal.valueOf(veces), 2, RoundingMode.HALF_UP))
                    .build();
        }
    }

    // ------------------------------------------------------------- insights

    /**
     * Cada insight se genera solo si hay datos para decir algo: un mes vacio
     * devuelve una lista vacia, no textos con ceros. Se ordenan AVISO, BIEN, INFO.
     */
    @Override
    public List<Insight> list(Long usuarioId, YearMonth periodo, LocalDate hoy) {
        boolean enCurso = periodo.equals(YearMonth.from(hoy));
        Map<YearMonth, List<Movimiento>> porMes = porMes(
                movimientos(usuarioId, periodo.minusMonths(MESES_MEDIA).atDay(1), periodo.atEndOfMonth()));
        List<Movimiento> delMes = porMes.getOrDefault(periodo, List.of());

        List<Insight> insights = new ArrayList<>();
        gastoFrenteAMedia(periodo, hoy, enCurso, delMes, porMes).ifPresent(insights::add);
        categoriaQueMasSube(periodo, delMes, porMes).ifPresent(insights::add);
        presupuestoAjustado(usuarioId, delMes).ifPresent(insights::add);
        tasaDeAhorro(periodo, enCurso, delMes).ifPresent(insights::add);
        mayorGasto(delMes).ifPresent(insights::add);
        if (enCurso) {
            recurrentesProximos(usuarioId, hoy).ifPresent(insights::add);
        }
        diaConMasGasto(periodo, enCurso, delMes).ifPresent(insights::add);

        insights.sort(Comparator.comparing(Insight::getSeveridad));
        return insights;
    }

    /**
     * Frente a la media de los meses anteriores que tienen algun movimiento
     * (hasta 3): asi un usuario nuevo no se compara con meses vacios. En el mes
     * en curso la media se prorratea a los dias que han pasado.
     */
    private Optional<Insight> gastoFrenteAMedia(YearMonth periodo, LocalDate hoy, boolean enCurso,
                                                List<Movimiento> delMes,
                                                Map<YearMonth, List<Movimiento>> porMes) {
        List<BigDecimal> anteriores = new ArrayList<>();
        for (int i = 1; i <= MESES_MEDIA; i++) {
            List<Movimiento> mes = porMes.get(periodo.minusMonths(i));
            if (mes != null && !mes.isEmpty()) {
                anteriores.add(total(mes, TipoMovimiento.GASTO));
            }
        }
        if (anteriores.isEmpty()) {
            return Optional.empty();
        }
        BigDecimal media = suma(anteriores).divide(BigDecimal.valueOf(anteriores.size()), 2, RoundingMode.HALF_UP);
        BigDecimal referencia = enCurso
                ? media.multiply(BigDecimal.valueOf(hoy.getDayOfMonth()))
                        .divide(BigDecimal.valueOf(periodo.lengthOfMonth()), 2, RoundingMode.HALF_UP)
                : media;
        if (referencia.signum() == 0) {
            return Optional.empty();
        }

        BigDecimal gasto = total(delMes, TipoMovimiento.GASTO);
        BigDecimal variacion = porcentaje(gasto.subtract(referencia), referencia);
        String contraQue = anteriores.size() == 1
                ? "lo que gastaste el mes anterior"
                : "la media de los " + anteriores.size() + " meses anteriores";
        String frase = enCurso
                ? "Llevas " + euros(gasto) + " este mes"
                : "Gastaste " + euros(gasto) + " en " + nombreMes(periodo);
        String alturas = enCurso ? " a estas alturas" : "";

        SeveridadInsight severidad;
        String titulo;
        String texto;
        if (variacion.compareTo(UMBRAL_MEDIA) > 0) {
            severidad = SeveridadInsight.AVISO;
            titulo = "Gastas más que de costumbre";
            texto = frase + ", un " + pct(variacion) + " más que " + contraQue + alturas
                    + " (" + euros(referencia) + ").";
        } else if (variacion.compareTo(UMBRAL_MEDIA.negate()) < 0) {
            severidad = SeveridadInsight.BIEN;
            titulo = "Gastas menos que de costumbre";
            texto = frase + ", un " + pct(variacion) + " menos que " + contraQue + alturas
                    + " (" + euros(referencia) + ").";
        } else {
            severidad = SeveridadInsight.INFO;
            titulo = "Gasto en línea con lo habitual";
            texto = frase + ", parecido a " + contraQue + alturas + " (" + euros(referencia) + ").";
        }
        return Optional.of(insight(TipoInsight.GASTO_VS_MEDIA, severidad, titulo, texto));
    }

    /** Solo si el mes anterior tiene movimientos: en el primer mes todo "sube". */
    private Optional<Insight> categoriaQueMasSube(YearMonth periodo, List<Movimiento> delMes,
                                                  Map<YearMonth, List<Movimiento>> porMes) {
        YearMonth mesAnterior = periodo.minusMonths(1);
        List<Movimiento> anterior = porMes.get(mesAnterior);
        if (anterior == null || anterior.isEmpty()) {
            return Optional.empty();
        }
        Map<Long, BigDecimal> antes = porCategoria(gastos(anterior));
        Map<Long, BigDecimal> ahora = porCategoria(gastos(delMes));
        Map<Long, Categoria> categorias = new LinkedHashMap<>();
        gastos(delMes).forEach(m -> categorias.putIfAbsent(m.getCategoriaId(), m.getCategoria()));

        Long masSube = null;
        BigDecimal maxSubida = BigDecimal.ZERO;
        for (Map.Entry<Long, BigDecimal> e : ahora.entrySet()) {
            BigDecimal subida = e.getValue().subtract(antes.getOrDefault(e.getKey(), CERO));
            if (subida.compareTo(maxSubida) > 0) {
                masSube = e.getKey();
                maxSubida = subida;
            }
        }
        if (masSube == null) {
            return Optional.empty();
        }

        String nombre = nombreCategoria(categorias.get(masSube));
        BigDecimal previo = antes.getOrDefault(masSube, CERO);
        BigDecimal actual = ahora.get(masSube);
        String texto = previo.signum() == 0
                ? euros(actual) + " en " + nombreMes(periodo) + "; en " + nombreMes(mesAnterior) + " no tuvo gastos."
                : "+" + euros(maxSubida) + " respecto a " + nombreMes(mesAnterior) + " (de " + euros(previo)
                        + " a " + euros(actual) + ", un " + pct(porcentaje(maxSubida, previo)) + " más).";
        return Optional.of(insight(TipoInsight.CATEGORIA_SUBE, SeveridadInsight.AVISO,
                nombre + " es la categoría que más sube", texto));
    }

    /** Los presupuestos MENSUAL superados o, si no hay ninguno, el mas ajustado por encima del 80 %. */
    private Optional<Insight> presupuestoAjustado(Long usuarioId, List<Movimiento> delMes) {
        List<Presupuesto> presupuestos = presupuestoRepository.findAll(usuarioId,
                PresupuestoFilter.builder().periodo(PeriodoPresupuesto.MENSUAL).build());
        if (presupuestos.isEmpty()) {
            return Optional.empty();
        }
        Map<Long, BigDecimal> gastado = porCategoria(gastos(delMes));

        record Uso(Presupuesto presupuesto, BigDecimal gastado, BigDecimal porcentaje) {
        }
        List<Uso> usos = presupuestos.stream()
                .filter(p -> p.getImporteLimite() != null && p.getImporteLimite().signum() > 0)
                .map(p -> {
                    BigDecimal g = gastado.getOrDefault(p.getCategoriaId(), CERO);
                    return new Uso(p, g, porcentaje(g, p.getImporteLimite()));
                })
                .sorted(Comparator.comparing(Uso::porcentaje).reversed())
                .toList();

        List<Uso> superados = usos.stream().filter(u -> u.porcentaje().compareTo(CIEN) > 0).toList();
        if (superados.size() == 1) {
            Uso u = superados.get(0);
            return Optional.of(insight(TipoInsight.PRESUPUESTO, SeveridadInsight.AVISO,
                    "Has superado el presupuesto de " + nombreCategoria(u.presupuesto().getCategoria()),
                    "Llevas " + euros(u.gastado()) + " de " + euros(u.presupuesto().getImporteLimite())
                            + " (" + pct(u.porcentaje()) + ")."));
        }
        if (superados.size() > 1) {
            List<String> partes = superados.stream()
                    .map(u -> nombreCategoria(u.presupuesto().getCategoria()) + " (" + pct(u.porcentaje()) + ")")
                    .toList();
            return Optional.of(insight(TipoInsight.PRESUPUESTO, SeveridadInsight.AVISO,
                    "Has superado " + superados.size() + " presupuestos",
                    enumerar(partes) + "."));
        }
        return usos.stream()
                .filter(u -> u.porcentaje().compareTo(PRESUPUESTO_AJUSTADO) >= 0)
                .findFirst()
                .map(u -> insight(TipoInsight.PRESUPUESTO, SeveridadInsight.AVISO,
                        nombreCategoria(u.presupuesto().getCategoria()) + ", al " + pct(u.porcentaje())
                                + " de su presupuesto",
                        "Llevas " + euros(u.gastado()) + " de " + euros(u.presupuesto().getImporteLimite())
                                + "; te quedan " + euros(u.presupuesto().getImporteLimite().subtract(u.gastado()))
                                + "."));
    }

    private Optional<Insight> tasaDeAhorro(YearMonth periodo, boolean enCurso, List<Movimiento> delMes) {
        BigDecimal ingresos = total(delMes, TipoMovimiento.INGRESO);
        if (ingresos.signum() == 0) {
            return Optional.empty();
        }
        BigDecimal gastos = total(delMes, TipoMovimiento.GASTO);
        BigDecimal balance = ingresos.subtract(gastos);
        BigDecimal tasa = porcentaje(balance, ingresos);
        String cuando = enCurso ? "Este mes" : "En " + nombreMes(periodo);

        if (balance.signum() < 0) {
            return Optional.of(insight(TipoInsight.TASA_AHORRO, SeveridadInsight.AVISO,
                    "Gastas más de lo que ingresas",
                    cuando + " los gastos (" + euros(gastos) + ") superan a los ingresos ("
                            + euros(ingresos) + ") en " + euros(balance.negate()) + "."));
        }
        SeveridadInsight severidad = tasa.compareTo(AHORRO_BUENO) >= 0 ? SeveridadInsight.BIEN : SeveridadInsight.INFO;
        return Optional.of(insight(TipoInsight.TASA_AHORRO, severidad,
                "Ahorras el " + pct(tasa) + " de tus ingresos",
                cuando + " has ingresado " + euros(ingresos) + " y gastado " + euros(gastos)
                        + ": te quedan " + euros(balance) + "."));
    }

    /** El mas caro; en un empate, el primero del mes. */
    private Optional<Insight> mayorGasto(List<Movimiento> delMes) {
        return gastos(delMes).stream()
                .max(Comparator.comparing(Movimiento::getImporte)
                        .thenComparing(Movimiento::getFecha, Comparator.reverseOrder()))
                .map(m -> {
                    String concepto = limpiar(m.getConcepto());
                    String donde = "el " + fechaLarga(m.getFecha()) + ", en " + nombreCategoria(m.getCategoria());
                    String texto = concepto.isEmpty()
                            ? mayuscula(donde) + "."
                            : concepto + ", " + donde + ".";
                    return insight(TipoInsight.MAYOR_GASTO, SeveridadInsight.INFO,
                            "Tu mayor gasto: " + euros(m.getImporte()), texto);
                });
    }

    /**
     * Los cargos de gasto que todavia no son movimiento y caen antes de
     * hoy + 7 dias. Incluye los atrasados que el job aun no ha generado.
     */
    private Optional<Insight> recurrentesProximos(Long usuarioId, LocalDate hoy) {
        LocalDate limite = hoy.plusDays(DIAS_PROXIMOS);
        record Cargo(Recurrente recurrente, LocalDate fecha) {
        }
        List<Cargo> cargos = new ArrayList<>();
        for (Recurrente r : recurrentesDeGasto(usuarioId)) {
            for (LocalDate fecha : cargosPendientes(r, r.getProximaFecha(), limite)) {
                cargos.add(new Cargo(r, fecha));
            }
        }
        if (cargos.isEmpty()) {
            return Optional.empty();
        }
        cargos.sort(Comparator.comparing(Cargo::fecha)
                .thenComparing(c -> c.recurrente().getConcepto(), String.CASE_INSENSITIVE_ORDER));

        BigDecimal total = suma(cargos.stream().map(c -> c.recurrente().getImporte()).toList());
        List<String> partes = new ArrayList<>(cargos.stream()
                .limit(MAX_CARGOS_EN_TEXTO)
                .map(c -> c.recurrente().getConcepto() + " (" + euros(c.recurrente().getImporte()) + ") "
                        + cuandoCargo(c.fecha(), hoy))
                .toList());
        if (cargos.size() > MAX_CARGOS_EN_TEXTO) {
            int resto = cargos.size() - MAX_CARGOS_EN_TEXTO;
            partes.add(resto == 1 ? "otro más" : resto + " más");
        }
        String titulo = cargos.size() == 1
                ? "1 cargo en los próximos 7 días"
                : cargos.size() + " cargos en los próximos 7 días";
        return Optional.of(insight(TipoInsight.RECURRENTES_PROXIMOS, SeveridadInsight.INFO, titulo,
                enumerar(partes) + ". Total: " + euros(total) + "."));
    }

    /** Con al menos 5 gastos en el mes: con menos, el dia "favorito" es casualidad. */
    private Optional<Insight> diaConMasGasto(YearMonth periodo, boolean enCurso, List<Movimiento> delMes) {
        List<Movimiento> gastos = gastos(delMes);
        if (gastos.size() < MIN_GASTOS_DIA_SEMANA) {
            return Optional.empty();
        }
        Map<DayOfWeek, BigDecimal> porDia = new EnumMap<>(DayOfWeek.class);
        gastos.forEach(m -> porDia.merge(m.getFecha().getDayOfWeek(), m.getImporte(), BigDecimal::add));
        if (porDia.size() < 2) {
            return Optional.empty();
        }
        Map.Entry<DayOfWeek, BigDecimal> max = porDia.entrySet().stream()
                .max(Map.Entry.<DayOfWeek, BigDecimal>comparingByValue()
                        .thenComparing(Map.Entry.comparingByKey(Comparator.reverseOrder())))
                .orElseThrow();
        BigDecimal total = suma(porDia.values());
        String cuando = enCurso ? "este mes" : "en " + nombreMes(periodo);
        return Optional.of(insight(TipoInsight.DIA_SEMANA, SeveridadInsight.INFO,
                mayuscula(diaPlural(max.getKey())) + " es cuando más gastas",
                "Concentran el " + pct(porcentaje(max.getValue(), total)) + " de tu gasto " + cuando
                        + " (" + euros(max.getValue()) + ")."));
    }

    // ----------------------------------------------------------- proyeccion

    @Override
    public ProyeccionMensual get(Long usuarioId, YearMonth periodo, LocalDate hoy) {
        YearMonth actual = YearMonth.from(hoy);
        EstadoProyeccion estado = periodo.isBefore(actual) ? EstadoProyeccion.CERRADO
                : periodo.equals(actual) ? EstadoProyeccion.EN_CURSO
                : EstadoProyeccion.FUTURO;
        int diasMes = periodo.lengthOfMonth();

        List<Movimiento> gastos = gastos(movimientos(usuarioId, periodo.atDay(1), periodo.atEndOfMonth()));
        BigDecimal gastoActual = suma(gastos.stream().map(Movimiento::getImporte).toList());
        BigDecimal gastoVariable = suma(gastos.stream()
                .filter(m -> !m.isRecurrente())
                .map(Movimiento::getImporte)
                .toList());

        int transcurridos = switch (estado) {
            case CERRADO -> diasMes;
            case EN_CURSO -> hoy.getDayOfMonth();
            case FUTURO -> 0;
        };
        BigDecimal ritmo = transcurridos == 0 ? CERO
                : gastoVariable.divide(BigDecimal.valueOf(transcurridos), 2, RoundingMode.HALF_UP);
        BigDecimal proyeccionVariable = estado != EstadoProyeccion.EN_CURSO ? CERO
                : gastoVariable.multiply(BigDecimal.valueOf(diasMes - transcurridos))
                        .divide(BigDecimal.valueOf(transcurridos), 2, RoundingMode.HALF_UP);

        BigDecimal pendientes = CERO;
        int cargos = 0;
        if (estado != EstadoProyeccion.CERRADO) {
            for (Recurrente r : recurrentesDeGasto(usuarioId)) {
                List<LocalDate> fechas = cargosPendientes(r, periodo.atDay(1), periodo.atEndOfMonth());
                cargos += fechas.size();
                pendientes = pendientes.add(r.getImporte().multiply(BigDecimal.valueOf(fechas.size())));
            }
        }

        return ProyeccionMensual.builder()
                .periodo(periodo)
                .estado(estado)
                .diasMes(diasMes)
                .diasTranscurridos(transcurridos)
                .gastoActual(gastoActual)
                .gastoVariable(gastoVariable)
                .ritmoDiario(ritmo)
                .proyeccionVariable(proyeccionVariable)
                .recurrentesPendientes(pendientes.setScale(2, RoundingMode.HALF_UP))
                .cargosPendientes(cargos)
                .gastoProyectado(gastoActual.add(proyeccionVariable).add(pendientes).setScale(2, RoundingMode.HALF_UP))
                .presupuestoMensual(presupuestoMensual(usuarioId))
                .build();
    }

    private BigDecimal presupuestoMensual(Long usuarioId) {
        List<Presupuesto> presupuestos = presupuestoRepository.findAll(usuarioId,
                PresupuestoFilter.builder().periodo(PeriodoPresupuesto.MENSUAL).build());
        return presupuestos.isEmpty() ? null
                : suma(presupuestos.stream().map(Presupuesto::getImporteLimite).filter(Objects::nonNull).toList());
    }

    // -------------------------------------------------------------- comunes

    private List<Movimiento> movimientos(Long usuarioId, LocalDate desde, LocalDate hasta) {
        return movimientoRepository.findAll(usuarioId, MovimientoFilter.builder().desde(desde).hasta(hasta).build());
    }

    private List<Recurrente> recurrentesDeGasto(Long usuarioId) {
        return recurrenteRepository.findAll(usuarioId,
                        RecurrenteFilter.builder().activo(true).tipo(TipoMovimiento.GASTO).build())
                .stream()
                .filter(r -> r.isActivo() && r.getTipo() == TipoMovimiento.GASTO && r.getProximaFecha() != null)
                .toList();
    }

    /**
     * Los cargos que aun no son movimiento (desde proximaFecha) que caen en
     * [desde, hasta], sin pasarse de las cuotas que le quedan.
     */
    static List<LocalDate> cargosPendientes(Recurrente r, LocalDate desde, LocalDate hasta) {
        List<LocalDate> fechas = new ArrayList<>();
        long restantes = r.getCuotasTotal() == null ? Long.MAX_VALUE
                : (long) r.getCuotasTotal() - r.getCuotasPagadas();
        LocalDate fecha = r.getProximaFecha();
        for (int i = 0; i < MAX_CARGOS && restantes > 0 && !fecha.isAfter(hasta); i++) {
            if (!fecha.isBefore(desde)) {
                fechas.add(fecha);
            }
            restantes--;
            fecha = r.siguienteDespuesDe(fecha);
        }
        return fechas;
    }

    private static void validarRango(LocalDate desde, LocalDate hasta) {
        if (desde == null || hasta == null) {
            throw new ValidationException("desde y hasta son obligatorios");
        }
        if (desde.isAfter(hasta)) {
            throw new ValidationException("desde no puede ser posterior a hasta");
        }
    }

    private static boolean sonMesesEnteros(LocalDate desde, LocalDate hasta) {
        return desde.getDayOfMonth() == 1 && hasta.equals(YearMonth.from(hasta).atEndOfMonth());
    }

    private static Map<YearMonth, List<Movimiento>> porMes(List<Movimiento> movimientos) {
        Map<YearMonth, List<Movimiento>> porMes = new LinkedHashMap<>();
        for (Movimiento m : movimientos) {
            porMes.computeIfAbsent(YearMonth.from(m.getFecha()), k -> new ArrayList<>()).add(m);
        }
        return porMes;
    }

    private static Map<Long, BigDecimal> porCategoria(List<Movimiento> movimientos) {
        Map<Long, BigDecimal> total = new LinkedHashMap<>();
        movimientos.forEach(m -> total.merge(m.getCategoriaId(), m.getImporte(), BigDecimal::add));
        return total;
    }

    private static List<Movimiento> gastos(List<Movimiento> movimientos) {
        return movimientos.stream().filter(m -> m.getTipo() == TipoMovimiento.GASTO).toList();
    }

    private static BigDecimal total(List<Movimiento> movimientos, TipoMovimiento tipo) {
        return movimientos.stream()
                .filter(m -> m.getTipo() == tipo)
                .map(Movimiento::getImporte)
                .reduce(CERO, BigDecimal::add);
    }

    private static BigDecimal suma(java.util.Collection<BigDecimal> importes) {
        return importes.stream().reduce(CERO, BigDecimal::add);
    }

    /** parte entre total en porcentaje, con dos decimales; nulo si total es cero. */
    static BigDecimal porcentaje(BigDecimal parte, BigDecimal total) {
        if (total == null || total.signum() == 0) {
            return null;
        }
        return parte.multiply(CIEN).divide(total, 2, RoundingMode.HALF_UP);
    }

    private static Insight insight(TipoInsight tipo, SeveridadInsight severidad, String titulo, String texto) {
        return Insight.builder().tipo(tipo).severidad(severidad).titulo(titulo).texto(texto).build();
    }

    // ----------------------------------------------------- textos en espanol

    /** 1234.5 -> "1.234,50 €". */
    static String euros(BigDecimal importe) {
        return String.format(ES, "%,.2f €", importe);
    }

    /** Sin decimales y sin signo: el signo lo dice la frase. */
    static String pct(BigDecimal porcentaje) {
        return porcentaje.abs().setScale(0, RoundingMode.HALF_UP).toPlainString() + " %";
    }

    private static String nombreMes(YearMonth mes) {
        return mes.getMonth().getDisplayName(TextStyle.FULL_STANDALONE, ES);
    }

    private static String fechaLarga(LocalDate fecha) {
        return fecha.getDayOfMonth() + " de " + fecha.getMonth().getDisplayName(TextStyle.FULL_STANDALONE, ES);
    }

    private static String cuandoCargo(LocalDate fecha, LocalDate hoy) {
        if (!fecha.isAfter(hoy)) {
            return "hoy";
        }
        if (fecha.equals(hoy.plusDays(1))) {
            return "mañana";
        }
        return "el " + fecha.getDayOfWeek().getDisplayName(TextStyle.FULL, ES) + " " + fecha.getDayOfMonth();
    }

    private static String diaPlural(DayOfWeek dia) {
        String nombre = dia.getDisplayName(TextStyle.FULL, ES);
        String plural = dia == DayOfWeek.SATURDAY || dia == DayOfWeek.SUNDAY ? nombre + "s" : nombre;
        return "los " + plural;
    }

    private static String nombreCategoria(Categoria categoria) {
        return categoria == null || categoria.getNombre() == null ? "Sin categoría" : categoria.getNombre();
    }

    private static String mayuscula(String texto) {
        return texto.isEmpty() ? texto : Character.toUpperCase(texto.charAt(0)) + texto.substring(1);
    }

    /** ["a", "b", "c"] -> "a, b y c". */
    private static String enumerar(List<String> partes) {
        if (partes.size() == 1) {
            return partes.get(0);
        }
        return String.join(", ", partes.subList(0, partes.size() - 1)) + " y " + partes.get(partes.size() - 1);
    }
}
