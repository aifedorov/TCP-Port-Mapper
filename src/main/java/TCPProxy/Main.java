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
 * Created by Александр on 21.06.14.
 */
public class Main {

    private final static Logger LOGGER = Logger.getAnonymousLogger();

    public static void main(String[] args) throws IOException {

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

            //запускаем сервера на локальных портах, указанных в конфигурационном файле
            //сервер ожидает подключения клиента
            new Server(config).start();
        }
    }
}
