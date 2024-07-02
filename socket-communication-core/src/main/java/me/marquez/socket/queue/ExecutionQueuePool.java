package me.marquez.socket.queue;

public class ExecutionQueuePool {

    private final ExecutionQueue[] queues;

    public ExecutionQueuePool(int size) {
        queues = new ExecutionQueue[size];
        for (int i = 0; i < size; i++) {
            queues[i] = new ExecutionQueue();
        }
    }

    private ExecutionQueue getBestQueue() {
        ExecutionQueue bestQueue = queues[0];
        for (ExecutionQueue queue : queues) {
            if (queue.size() < bestQueue.size()) {
                bestQueue = queue;
            }
        }
        return bestQueue;
    }

    public void submit(Runnable runnable) {
        getBestQueue().add(runnable);
    }

    public int size() {
        return queues.length;
    }

}
