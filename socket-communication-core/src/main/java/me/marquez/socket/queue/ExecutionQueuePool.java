package me.marquez.socket.queue;

import java.util.concurrent.CompletableFuture;

public class ExecutionQueuePool {

    private final ExecutionQueue[] queues;

    public ExecutionQueuePool(int size) {
        queues = new ExecutionQueue[size];
        for (int i = 0; i < size; i++) {
            queues[i] = new ExecutionQueue();
        }
    }

    private synchronized ExecutionQueue getBestQueue() {
        ExecutionQueue bestQueue = queues[0];
        for (ExecutionQueue queue : queues) {
            if (queue.size() < bestQueue.size()) {
                bestQueue = queue;
            }
        }
        return bestQueue;
    }

    public synchronized void submit(Runnable runnable, CompletableFuture<?> future) {
//        System.out.println("submit: " + getBestQueue().size());
        getBestQueue().add(runnable, future);
    }

    public int size() {
        return queues.length;
    }

}
