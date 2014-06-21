package TCPProxy;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Александр on 21.06.14.
 */
public class Worker extends Thread {

    private final static Logger LOGGER = Logger.getAnonymousLogger();

    private String workerId;
    private Runnable task;
    // Необходима ссылка на пул нитей в котором существует нить, чтобы
    // нить могла добавить себя в пул нитей по завершению работы.
    private ThreadPool threadpool;

    public Worker(String id, ThreadPool pool) {
        this.workerId = id;
        this.threadpool = pool;
        start();
    }

    // ThreadPool, когда ставит в расписание задачу, использует этот метод
    // для делегирования задачи Worker-нити. Кроме того для установки
    // задачи (типа Runnable) он также переключает ожидающий метод
    // run() на начало выполнения задачи.
    public void setTask(Runnable task) {
        this.task = task;
        synchronized (this) {
            notify();
        }
    }

    public void run() {
        try {
            while (!threadpool.isStopped()) {
                synchronized (this) {
                    if (task != null) {
                        try {
                            // Запускаем задачу
                            task.run();
                        }
                        catch (Exception exception) {
                            if (LOGGER.isLoggable(Level.SEVERE))
                                LOGGER.log(Level.SEVERE, "Ошибка в потоке " + workerId + "при выполнении задачи!", exception);
                        }
                        // Возвращает себя в пул нитей
                        threadpool.putWorker(this);
                    }
                    wait();
                }
            }
            LOGGER.info(this + " остановлен");
        }
        catch (InterruptedException exception) {
            throw new RuntimeException(exception);
        }
    }

    public String toString() {
        return "Поток : " + workerId;
    }
}
