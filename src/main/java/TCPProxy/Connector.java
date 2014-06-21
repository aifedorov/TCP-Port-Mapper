package TCPProxy;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Класс нужен для подключения воркера к удаленому хосту и обмена данными между каналами, с использованием буферов
 */
public class Connector {

    private final static Logger LOGGER = Logger.getAnonymousLogger();

    private final ProxyBuffer clientBuffer = new ProxyBuffer();
    private final ProxyBuffer serverBuffer = new ProxyBuffer();
    private boolean closeConnection = false;

    private final SocketChannel clientChannel;
    private Selector selector;
    private SocketChannel serverChannel;
    private ProxyConfig config;

    public Connector(SocketChannel clientChannel, ProxyConfig config) {
        this.clientChannel = clientChannel;
        this.config = config;
    }

    public void readFromClient() throws IOException {
        serverBuffer.writeFrom(clientChannel);
        if (serverBuffer.isReadyToRead()) register();
    }

    public void readFromServer() throws IOException {
        clientBuffer.writeFrom(serverChannel);
        if (clientBuffer.isReadyToRead()) register();
    }

    public void writeToClient() throws IOException {
        clientBuffer.writeTo(clientChannel);
        if (clientBuffer.isReadyToWrite()) register();
    }

    public void writeToServer() throws IOException {
        serverBuffer.writeTo(serverChannel);
        if (serverBuffer.isReadyToWrite()) register();
    }

    //определяем селектор на прослушку нужных событий для каждого канала
    public void register() throws ClosedChannelException {
        int clientOps = 0;
        if (serverBuffer.isReadyToWrite()) clientOps |= SelectionKey.OP_READ;
        if (clientBuffer.isReadyToRead()) clientOps |= SelectionKey.OP_WRITE;
        clientChannel.register(selector, clientOps);

        int serverOps = 0;
        if (clientBuffer.isReadyToWrite()) serverOps |= SelectionKey.OP_READ;
        if (serverBuffer.isReadyToRead()) serverOps |= SelectionKey.OP_WRITE;
        serverChannel.register(selector, serverOps);
    }

    public void register(Selector selector) {
        this.selector = selector;

        try {
            final InetSocketAddress socketAddress = new InetSocketAddress(
                    config.getRemoteHost(), config.getRemotePort());
            serverChannel = SocketChannel.open();
            serverChannel.connect(socketAddress);
            serverChannel.configureBlocking(false);

            register();
        } catch (IOException exception) {

            if (LOGGER.isLoggable(Level.WARNING))
                LOGGER.log(Level.WARNING, "Не удалось соедениться с "
                        + config.getRemoteHost() + ":" + config.getRemotePort(), exception);
        }
    }

    public void process(final SelectionKey key) {
        try {
            if (key.channel() == clientChannel) {
                if (key.isValid() && key.isReadable())
                    readFromClient();
                if (key.isValid() && key.isWritable())
                    writeToClient();
            }

            if (key.channel() == serverChannel) {
                if (key.isValid() && key.isReadable())
                    readFromServer();
                if (key.isValid() && key.isWritable())
                    writeToServer();
            }
        } catch (ClosedChannelException exception) {

            closeChannel(this.clientChannel);
            closeChannel(this.serverChannel);
            closeConnection = true;

        } catch (IOException exception) {

            closeChannel(this.clientChannel);
            closeChannel(this.serverChannel);
            closeConnection = true;

            if (LOGGER.isLoggable(Level.WARNING))
                LOGGER.log(Level.WARNING, "Ошибка при обработке ключей.", exception);
        }
    }

    private static void closeChannel(SocketChannel channel) {
        if (channel != null) {
            try {
                channel.close();
            } catch (IOException exception) {
                if (LOGGER.isLoggable(Level.WARNING))
                    LOGGER.log(Level.WARNING, "Не удалось корректно завершить работу канала.", exception);
            }
        }
    }

    public boolean isCloseConnection() { return closeConnection; }
}
