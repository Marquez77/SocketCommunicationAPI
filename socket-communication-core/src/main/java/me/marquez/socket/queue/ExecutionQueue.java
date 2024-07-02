package me.marquez.socket.queue;

import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

public class ExecutionQueue {

    private final ExecutorService thread = Executors.newSingleThreadExecutor();
    private final Queue<Runnable> queue = new LinkedList<>();
    private final AtomicBoolean running = new AtomicBoolean(false);

    public synchronized void add(@NotNull Runnable runnable) {
        queue.add(runnable);
        if(running.compareAndSet(false, true)) {
            thread.execute(() -> {
                Runnable task;
                while (true) {
                    synchronized (this) {
                        task = queue.poll();
                        if (task == null)
                            break;
                    }
                    try {
                        task.run();
                    }catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                running.set(false);
            });
        }
    }

    public int size() {
        return queue.size();
    }


}
