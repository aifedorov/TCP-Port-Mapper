package TCPProxy;

import java.util.*;

/**
 * Парсер конфигурационного файла
 */

public class ConfigParser {

    public static List<ProxyConfig> parse(Properties properties){
        final Set<String> proxyNames = new HashSet<String>();
        final List<String> propertySet = (List<String>) Collections.list(properties.propertyNames());
        for (final String propertyName : propertySet) {
            final int dotIndex = propertyName.lastIndexOf('.');
            if (dotIndex == -1) throw new IllegalArgumentException(
                    "Не верно указан параметр " + propertyName + " используйте <имя прокси>.localPort|remotePort|remoteHost");

            proxyNames.add(propertyName.substring(0, dotIndex));
        }
        if (proxyNames.isEmpty()) throw new IllegalArgumentException("Пожалуйста, укажите хотя бы один прокси сервер.");

        final List<ProxyConfig> proxyConfigs = new ArrayList<ProxyConfig>();
        for (final String proxyName : proxyNames) {
            final int localPort = findIntegerProperty(properties, proxyName + ".localPort");
            final int remotePort = findIntegerProperty(properties, proxyName + ".remotePort");
            final String remoteHost = findProperty(properties, proxyName + ".remoteHost");

            proxyConfigs.add(new ProxyConfig(localPort, remoteHost, remotePort));
        }
        return proxyConfigs;
    }

    private static int findIntegerProperty(Properties properties, String key) {
        final String value = findProperty(properties, key);
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException(
                    "Не верное значение для " + key + " = " + value, exception);
        }
    }

    private static String findProperty(Properties properties, String key) {
        final String value = properties.getProperty(key);
        if (value == null) throw new IllegalArgumentException("Пожалуйста, укажите значение для " + key);
        return value;
    }
}
