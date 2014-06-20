package TCPProxy;

import java.io.IOException;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Воркер создает сокет на локальной машине по указанному порту и ожидает подключение клиента
 * Еще в нем находиться селектор, управляющий каналами сервера и клиента
 */

public class Worker extends Thread {

    private final static Logger LOGGER = Logger.getAnonymousLogger();

    private ProxyConfig config;
    private Connector connector;
    private Acceptor acceptor;


    public Worker(ProxyConfig config, Acceptor acceptor){
        this.config = config;
        this.acceptor = acceptor;
    }

    @Override
    public void run() {

        Selector selector = null;

            try {

                selector = Selector.open();

                /**
                 * Регистрируем селектор на прослушку событий подключения к локальной машине
                 * После передаем управление воркерам - каждый клинт работает со своим воркером
                */
               this.acceptor.register(selector);

               while (!Thread.interrupted()) {

                   selector.select();
                   Iterator selectedKeys = selector.selectedKeys().iterator();

                   while (selectedKeys.hasNext()) {
                       SelectionKey key = (SelectionKey) selectedKeys.next();
                       selectedKeys.remove();

                        if (key.isValid() && key.isAcceptable()) {

                            //при подключении клиента создаем коннектор для обмена данными
                            SocketChannel clientChannel = this.acceptor.process(key);

                            //создаем конектор на удаленный хост
                            this.connector = new Connector(clientChannel, config);

                            //определяем для него селектор
                            this.connector.register(selector);
                        }

                        if (key.isValid() && key.isReadable() || key.isValid() && key.isWritable())
                            this.connector.process(key);
                        }

                }

            } catch (IOException exception) {
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
