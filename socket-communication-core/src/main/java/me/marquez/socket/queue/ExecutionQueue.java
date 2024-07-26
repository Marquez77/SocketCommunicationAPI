package me.marquez.socket.queue;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import java.net.SocketAddress;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ExecutionQueue {

    @AllArgsConstructor
    private static class Execution {
        Runnable runnable;
        CompletableFuture<?> future;
    }

    private final ExecutorService thread;
    private final Queue<Execution> queue = new LinkedList<>();
    private volatile boolean running = false;
    @Getter
    @Setter
    private volatile SocketAddress currentTarget;

    public ExecutionQueue(String prefix) {
        this.thread = Executors.newSingleThreadExecutor(new SocketThreadFactory(prefix));
    }

    public synchronized void add(@NotNull Runnable runnable, CompletableFuture<?> future) {
        // System.out.println("add: " + this);
        queue.add(new Execution(runnable, future));
        // System.out.println("Added: " + this + " " + queue.size() + " " + running);
        if(running)
            return;
        running = true;
        thread.execute(() -> {
            Execution task;
            while (true) {
                synchronized (this) {
                    // System.out.println("Peek: " + this + " " + queue.size() + " " + running);
                    task = queue.peek();
                    // System.out.println("Peeked: " + this + " " + queue.size() + " " + running);
                }
                try {
                    if(task != null && !(task.future.isDone() || task.future.isCancelled()))
                        task.runnable.run();
                }catch (Exception e) {
                    e.printStackTrace();
                }
                synchronized (this) {
                    // System.out.println("Poll: " + this + " " + queue.size() + " " + running);
                    queue.poll();
                    // System.out.println("Polled: " + this + " " + queue.size() + " " + running);
                    if (queue.isEmpty()) {
                        running = false;
                        setCurrentTarget(null);
                        return;
                    }
                }
            }
        });
    }

    public synchronized int size() {
        return queue.size();
    }


}
