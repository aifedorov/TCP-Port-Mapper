package TCPProxy;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Задача синхронизации клиента и сервера
 */
public class SyncTask implements Runnable {

    private final static Logger LOGGER = Logger.getAnonymousLogger();
    private Connector connector;

    public SyncTask(Connector connector) throws IOException {
        this.connector = connector;
    }

    public void run() {
        Selector selector = null;
        try {

            selector = Selector.open();
            connector.register(selector);

            //завершим синхронизацию, когда клиент закроет соединение
            while (!connector.isCloseConnection()) {

                selector.select();
                Iterator it = selector.selectedKeys().iterator();
                while (it.hasNext()) {
                    SelectionKey key = (SelectionKey) it.next();
                    it.remove();

                    if (key.isValid() && key.isReadable() || key.isValid() && key.isWritable())
                        connector.process(key);
                }
            }
        }
        catch (IOException exception) {
            if (LOGGER.isLoggable(Level.SEVERE))
                LOGGER.log(Level.SEVERE, "Ошибка в селекторе, работа потока была остановлена!", exception);

        }finally {
            if (selector != null) {
                try {
                    selector.close();
                } catch (IOException exception) {
                    if (LOGGER.isLoggable(Level.WARNING))
                        LOGGER.log(Level.WARNING, "Не удалось корректно завершить работу селектора.", exception);
                }
            }
        }
    }
}


