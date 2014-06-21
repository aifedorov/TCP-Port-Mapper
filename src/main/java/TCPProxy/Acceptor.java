package TCPProxy;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Слушает порт, указанный в кофигурационном файле
 */
public class Acceptor {

    private final static Logger LOGGER = Logger.getAnonymousLogger();

    private ServerSocketChannel serverChannel;
    private ProxyConfig config;

    public Acceptor(ProxyConfig config) throws IOException {
        this.config = config;
    }

    public void init() {

        try {

            serverChannel = ServerSocketChannel.open();
            InetSocketAddress inetSocketAddress = new InetSocketAddress( config.getLocalPort() );
            serverChannel.socket().bind(inetSocketAddress);
            serverChannel.configureBlocking(false);

        }catch (IOException exception){
            if (LOGGER.isLoggable(Level.SEVERE))
                LOGGER.log(Level.SEVERE, "Ошибка во время инициализации сервера!", exception);
        }
    }

    public void register(Selector selector){
        try {

            serverChannel.register(selector, SelectionKey.OP_ACCEPT);

        }catch (ClosedChannelException exception){
            if (LOGGER.isLoggable(Level.SEVERE))
                LOGGER.log(Level.SEVERE, "Ошибка при регистрации селектора на клиенте!", exception);
        }

    }

   public SocketChannel process(SelectionKey key) throws IOException{

       if (key.isValid() && key.isAcceptable()) {
           ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();
           SocketChannel clientChannel = serverSocketChannel.accept();
           clientChannel.configureBlocking(false);
           return clientChannel;
       }
       return null;
   }
}
