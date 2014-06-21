package TCPProxy;

import java.util.Iterator;
import java.util.LinkedList;

/**
 * Created by Александр on 20.06.14.
 */
public class ThreadPool extends Thread {

    private static final int DEFAULT_NUMBER_WORKERS = 5;

    private LinkedList workerPool = new LinkedList();
    private LinkedList taskList = new LinkedList();
    private boolean stopped = false;
    private int countWorkers;

    public ThreadPool(){
        this(DEFAULT_NUMBER_WORKERS);
    }

    public ThreadPool(int countWorkers) {
        this.countWorkers = countWorkers;
        init();
    }

    private void init(){
        for (int i = 0; i < countWorkers; i++)
            workerPool.add(new Worker("" + i, this));
        start();
    }

    public void run() {
        try {
            while (!stopped) {
                if (taskList.isEmpty()) {
                    synchronized (taskList) {
                        // Если очередь пустая, подождать, пока будет добавлена новая задача
                        taskList.wait();
                    }
                }
                else if (workerPool.isEmpty()) {
                    synchronized (workerPool) {
                        // Если нет рабочих потоков, подождать, пока не появится
                        workerPool.wait();
                    }
                } else {
                    // Запускаем следующую задачу из писка задач
                    getWorker().setTask((Runnable) taskList.removeLast());
                }
            }
        }
        catch (InterruptedException exception) {
            throw new RuntimeException(exception);
        }
    }

    public void addTask(Runnable task) {
        taskList.addFirst(task);
        synchronized (taskList) {
            // Если добавлена новая задача, уведомляем
            taskList.notify();
        }
    }

    public void putWorker(Worker worker) {
        workerPool.addFirst(worker);
        // Когда нет свободных потоков в пуле, то блокируем пул, до тех пор пока они не появятся
        synchronized (workerPool) {
            workerPool.notify();
        }
    }

    private Worker getWorker() {
        return (Worker) workerPool.removeLast();
    }

    public boolean isStopped() {
        return stopped;
    }

    public void stopThreads() {
        stopped = true;
        Iterator it = workerPool.iterator();
        while (it.hasNext()) {
            Worker worker = (Worker) it.next();
            synchronized (worker) {
                worker.notify();
            }
        }
    }

    public void testThreadPool() {
        ThreadPool threadPool = new ThreadPool();
        for (int i = 0; i < 10; i++) {
            threadPool.addTask(new Runnable() {
                public void run() {
                    System.out.println("Hello!");
                }
            });
        }
    }
}
