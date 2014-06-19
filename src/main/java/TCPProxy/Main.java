package TCPProxy;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Запуск прокси сервера, для каждого порта на локальной машине отдельный поток
 */
public class Main {

    private final static Logger LOGGER = Logger.getAnonymousLogger();

    public static void main(String[] args){

        if(args.length != 1){
            System.err.println("Пожалуйста, укажите путь до конфигурационного файла прокси сервера.");
            System.exit(1);
        }

        final Properties propertiesProxy = new Properties();
        File configFile = new File(args[0]);

        try {
            FileReader reader = new FileReader(configFile);
            propertiesProxy.load(reader);
            reader.close();

            } catch (FileNotFoundException exception) {
                if (LOGGER.isLoggable(Level.SEVERE)){
                    LOGGER.log(Level.SEVERE, "Конфигурационный файл " + args[0] + " не найден!", exception);
                    System.exit(1);
                }
            } catch (IOException exception) {
                if (LOGGER.isLoggable(Level.SEVERE)){
                    LOGGER.log(Level.SEVERE, "Не удалось загрузить конфигурационный файл!", exception);
                    System.exit(1);
            }
        }

        final List<ProxyConfig> configs = ConfigParser.parse(propertiesProxy);
        for (final ProxyConfig config : configs){
            try {
                new Thread(new Worker(config)).start();
                LOGGER.log(Level.INFO, "Сервер успешно запущен на порте: " + config.getLocalPort());

            } catch (IOException exception) {
                if (LOGGER.isLoggable(Level.SEVERE)){
                    LOGGER.log(Level.SEVERE, "Ошибка при запуске сервера!", exception);
                    System.exit(1);
                }
            }
        }
      }
    }
