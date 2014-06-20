package TCPProxy;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Прокси сервер
 */
public class Server {

    private final static Logger LOGGER = Logger.getAnonymousLogger();

    private final ProxyConfig config;
    private Thread[] workers;
    private Acceptor acceptor;

    public Server(final ProxyConfig config){
        this.config = config;
    }

    public void start(){

        try {

            //инициализация сервера на локальной машине
            this.acceptor = new Acceptor(config);
            this.acceptor.initClient();

        } catch (IOException exception){
            if (LOGGER.isLoggable(Level.SEVERE))
                LOGGER.log(Level.SEVERE, "Ошибка при инициализации сервера на локальном порту!", exception);
        }

        workers = new Thread[config.getWorkerCount()];
        for (int i = 0; i < workers.length; i++) {
            workers[i] = new Worker(this.config, this.acceptor);
        }

        for (final Thread worker : workers)
            worker.start();

        if (LOGGER.isLoggable(Level.INFO))
            LOGGER.info("Сервер, слушающий порт " + this.config.getLocalPort() + " c " + config.getWorkerCount() + " потоками успешно запущен.");
    }
}
