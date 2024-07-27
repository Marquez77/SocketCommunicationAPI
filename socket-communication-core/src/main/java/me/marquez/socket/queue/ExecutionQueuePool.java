package me.marquez.socket.queue;

import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class ExecutionQueuePool {

    private final ExecutionQueue[] queues;
    private final int maximumQueuePerTarget;

    public ExecutionQueuePool(String prefix, int size, int maximumQueuePerTarget) {
        this.queues = new ExecutionQueue[size];
        for (int i = 0; i < size; i++) {
            this.queues[i] = new ExecutionQueue(prefix + "-queue-" + i);
        }
        this.maximumQueuePerTarget = maximumQueuePerTarget;
    }

    private synchronized ExecutionQueue getBestQueue(SocketAddress target) {
        List<ExecutionQueue> targetQueues = new ArrayList<>();
        for (ExecutionQueue queue : queues) {
            if(target.equals(queue.getCurrentTarget())) {
                targetQueues.add(queue);
            }
        }
        ExecutionQueue bestQueue = queues[0];
        for (ExecutionQueue queue : (targetQueues.size() >= maximumQueuePerTarget ? targetQueues.toArray(ExecutionQueue[]::new) : queues)) {
            if (queue.size() < bestQueue.size()) {
                bestQueue = queue;
            }
        }
        bestQueue.setCurrentTarget(target);
        return bestQueue;
    }

    public synchronized int getEmptyQueues() {
        return (int)Arrays.stream(queues).filter(queue -> queue.size() == 0).count();
    }

    public synchronized String[] getQueueSizes() {
        return Arrays.stream(queues).map(queue -> {
            StringBuilder sb = new StringBuilder();
            sb.append(queue.size());
            var target = queue.getCurrentTarget();
            if(target != null)
                sb.append("(").append(target).append(")");
            long id = queue.getCurrentTaskId();
            if(id != -1)
                sb.append("[").append(id).append("]");
            return sb.toString();
        }).toArray(String[]::new);
    }

    public synchronized void submit(long id, SocketAddress target, Runnable runnable, CompletableFuture<?> future) {
//        System.out.println("submit: " + getBestQueue().size());
        getBestQueue(target).add(id, runnable, future);

    }

    public int size() {
        return queues.length;
    }

    public void shutdownNow() {
        for (ExecutionQueue queue : queues) {
            if(queue.getThread() != null)
                queue.getThread().shutdownNow();
        }
    }

}
