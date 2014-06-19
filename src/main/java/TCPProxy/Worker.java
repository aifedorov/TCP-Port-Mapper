package TCPProxy;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Воркер создает сокет на локальной машине по указанному порту и ожидает подключение клиента
 * Еще в нем находиться селектор, управляющий каналами сервера и клиента
 */

public class Worker implements Runnable {

    private final static Logger LOGGER = Logger.getAnonymousLogger();

    private int port;
    private SocketChannel clientChannel;
    private ServerSocketChannel serverChannel;
    private Selector selector;
    private ProxyConfig config;
    private Connector connector;

    public Worker(ProxyConfig config) throws IOException {
        this.config = config;
        this.port = config.getLocalPort();
        this.selector = this.initSelector();
    }

    private Selector initSelector() throws IOException {

        try {

            Selector selector = Selector.open();

            this.serverChannel = ServerSocketChannel.open();
            InetSocketAddress inetSocketAddress = new InetSocketAddress( this.port );
            serverChannel.socket().bind(inetSocketAddress);
            serverChannel.configureBlocking(false);
            serverChannel.register(selector, SelectionKey.OP_ACCEPT);
            return selector;

        }catch (IOException exception){
            if (LOGGER.isLoggable(Level.SEVERE))
                LOGGER.log(Level.SEVERE, "Ошибка во время инициализации сервера!", exception);
        }
       return null;
    }

    @Override
    public void run() {
            try {
                while (true) {

                    this.selector.select();
                    Iterator selectedKeys = this.selector.selectedKeys().iterator();

                    while (selectedKeys.hasNext()) {
                        SelectionKey key = (SelectionKey) selectedKeys.next();
                        selectedKeys.remove();

                        if (key.isValid() && key.isAcceptable()) {
                            ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();
                            this.clientChannel = serverSocketChannel.accept();
                            this.clientChannel.configureBlocking(false);

                            //создаем конектор на удаленный хост
                            this.connector = new Connector(this.clientChannel, config);

                            //определяем для него селектор
                            this.connector.register( this.selector);
                        }

                    if (key.isValid() && key.isReadable() || key.isValid() && key.isWritable()){
                          this.connector.process(key);
                        }
                    }
                }
            } catch (IOException exception) {
                if (LOGGER.isLoggable(Level.SEVERE))
                    LOGGER.log(Level.SEVERE, "Ошибка в селекторе, работа потока была остановлена!", exception);

            }finally {
                if (this.selector != null) {
                    try {
                        this.selector.close();
                    } catch (IOException exception) {
                        if (LOGGER.isLoggable(Level.WARNING))
                            LOGGER.log(Level.WARNING, "Не удалось корректно завершить работу селектора.", exception);
                    }
                }
            }
          }

}
