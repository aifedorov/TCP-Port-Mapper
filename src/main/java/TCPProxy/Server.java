package TCPProxy;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Сервер ожидает подключения клиентов, при их появлении добавляет новую задачу синхронизации в пул потоков
 */
public class Server extends Thread {

    private final static Logger LOGGER = Logger.getAnonymousLogger();
    private final static int COUNT_THREADS = 25;
    private static ThreadPool threadPool = new ThreadPool(COUNT_THREADS);

    private final ProxyConfig config;
    private Acceptor acceptor;
    private Connector connector;

    public Server(final ProxyConfig config){
        this.config = config;
    }

    public void run(){

        try {

            //инициализация сервера на локальной машине
            this.acceptor = new Acceptor(config);
            this.acceptor.init();


            if (LOGGER.isLoggable(Level.INFO))
                LOGGER.info("Сервер успешно запущен на порте " + this.config.getLocalPort());

            ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
            Selector selector = Selector.open();
            this.acceptor.register(selector);

            try {

                while (true) {

                    selector.select();

                    Iterator it = selector.selectedKeys().iterator();
                    while (it.hasNext()) {
                        SelectionKey key = (SelectionKey) it.next();
                        it.remove();

                        if (key.isValid() && key.isAcceptable()) {

                            //при подключении клиента создаем коннектор для обмена данными
                            SocketChannel clientChannel = this.acceptor.process(key);

                            //создаем конектор на удаленный хост
                            this.connector = new Connector(clientChannel, config);

                            System.out.println("Успешное подключение клиента " + clientChannel.toString());

                            // Добавляем новую задачу для пула потоков
                            threadPool.addTask(new SyncTask(connector));
                        }
                    }
                }
            }
            finally {
                serverSocketChannel.close();
                selector.close();
                LOGGER.info("Работа сервера на порту " + this.config.getLocalPort() + " остановлена!");
            }

        } catch (IOException exception){
            if (LOGGER.isLoggable(Level.SEVERE))
                LOGGER.log(Level.SEVERE, "Ошибка при инициализации сервера на " + this.config.getLocalPort() + " порту!", exception);
        }
    }
}
