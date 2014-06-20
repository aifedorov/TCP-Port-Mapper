package TCPProxy;

/**
 * TCP конфигурация прокси сервера
 */

public class ProxyConfig {

    private final int localPort;
    private final String remoteHost;
    private final int remotePort;
    private int workerCount;

    public ProxyConfig(int localPort, String remoteHost, int remotePort) {
        this.localPort = localPort;
        this.remoteHost = remoteHost;
        this.remotePort = remotePort;
    }

    public int getLocalPort() {
        return localPort;
    }

    public int getRemotePort() {
        return remotePort;
    }

    public String getRemoteHost() {
        return remoteHost;
    }

    public int getWorkerCount() {
        return workerCount;
    }

    public void setWorkerCount(int workerCount) {
        this.workerCount = workerCount;
    }

}
