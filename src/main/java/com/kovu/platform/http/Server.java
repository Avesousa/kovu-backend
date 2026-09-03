package com.kovu.platform.http;

import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.lang.System.Logger.Level;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

/**
 * Envoltorio delgado sobre {@code com.sun.net.httpserver.HttpServer}.
 *
 * El executor es lo interesante: cada request se atiende en un hilo virtual
 * ({@link Executors#newVirtualThreadPerTaskExecutor()}), no en un hilo de
 * plataforma del pool. Como el código de la capa JDBC es bloqueante (una
 * query bloquea el hilo esperando la red/el disco de MySQL), sin hilos
 * virtuales cada conexión lenta a la base consumiría un hilo de plataforma
 * caro; con hilos virtuales, bloquear es barato y miles de requests
 * concurrentes no agotan el pool de hilos del sistema operativo.
 */
public final class Server {

    private static final System.Logger LOG = System.getLogger(Server.class.getName());

    private final HttpServer httpServer;
    private final int port;

    public Server(int port, Router router) throws IOException {
        this.port = port;
        this.httpServer = HttpServer.create(new InetSocketAddress(port), 0);
        this.httpServer.createContext("/", router);
        this.httpServer.setExecutor(Executors.newVirtualThreadPerTaskExecutor());
    }

    public void start() {
        httpServer.start();
        LOG.log(Level.INFO, "Servidor escuchando en el puerto " + port);
    }

    public void stop(int delaySeconds) {
        httpServer.stop(delaySeconds);
    }

    public int port() {
        return port;
    }
}
