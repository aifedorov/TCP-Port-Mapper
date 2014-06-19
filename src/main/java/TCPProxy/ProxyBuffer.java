package TCPProxy;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SocketChannel;

/**
 * Буффер для обменна даанными между прокси и клиентом, прокси и удаленным сервером
 */

public class ProxyBuffer {

    private static enum BufferState{

        READY_TO_WRITE, READY_TO_READ
    }

    private final static int BUFFER_SIZE = 8192;

    private final ByteBuffer buffer = ByteBuffer.allocateDirect(BUFFER_SIZE);
    private BufferState state = BufferState.READY_TO_WRITE;

    public boolean isReadyToRead() {
        return state == BufferState.READY_TO_READ;
    }

    public boolean isReadyToWrite() {
        return state == BufferState.READY_TO_WRITE;
    }

    public void writeFrom(SocketChannel channel) throws IOException {
        int read = channel.read(buffer);
        if (read == -1) throw new ClosedChannelException();

        if (read > 0) {
            buffer.flip();
            state = BufferState.READY_TO_READ;
        }
    }

    public void writeTo(SocketChannel channel) throws IOException {
        channel.write(buffer);

        if (buffer.remaining() == 0) {
            buffer.clear();
            state = BufferState.READY_TO_WRITE;
        }
    }
}
