package com.polaris.shared.testing;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.core.read.ListAppender;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Captura en memoria lo que escribe el logger de una clase, para comprobar en un
 * test QUE se loguea (nivel, mensaje, si lleva excepcion). Usar con
 * try-with-resources: al cerrar se desengancha y se restaura el nivel.
 */
public final class CapturaLogs implements AutoCloseable {

    private final Logger logger;
    private final Level nivelPrevio;
    private final ListAppender<ILoggingEvent> appender = new ListAppender<>();

    private CapturaLogs(Class<?> clase) {
        this.logger = (Logger) LoggerFactory.getLogger(clase);
        this.nivelPrevio = logger.getLevel();
        logger.setLevel(Level.DEBUG);
        appender.start();
        logger.addAppender(appender);
    }

    public static CapturaLogs de(Class<?> clase) {
        return new CapturaLogs(clase);
    }

    public List<ILoggingEvent> eventos() {
        return appender.list;
    }

    public List<ILoggingEvent> eventos(Level nivel) {
        return appender.list.stream().filter(e -> e.getLevel() == nivel).toList();
    }

    /** Todo lo escrito: mensajes ya formateados y, de cada excepcion, clase, mensaje y causas. */
    public String texto() {
        return appender.list.stream().map(e -> e.getFormattedMessage() + volcar(e.getThrowableProxy()))
                .collect(Collectors.joining("\n"));
    }

    private static String volcar(IThrowableProxy error) {
        StringBuilder sb = new StringBuilder();
        for (IThrowableProxy actual = error; actual != null; actual = actual.getCause()) {
            sb.append("\n").append(actual.getClassName()).append(": ").append(actual.getMessage());
        }
        return sb.toString();
    }

    @Override
    public void close() {
        logger.detachAppender(appender);
        logger.setLevel(nivelPrevio);
    }
}
