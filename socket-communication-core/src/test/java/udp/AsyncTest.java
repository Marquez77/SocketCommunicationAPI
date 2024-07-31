package udp;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class AsyncTest {

    public static void main(String[] args) {
        System.out.println(Runtime.getRuntime().availableProcessors());

        // 사용자 정의 쓰레드 풀을 생성
        ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

        long start = System.currentTimeMillis();
        // 200개의 비동기 작업 생성
        for (int i = 0; i < 2000; i++) {
//            CompletableFuture.runAsync(() -> {
//                try {
//                    Thread.sleep(1000);
//                } catch (InterruptedException e) {
//                    Thread.currentThread().interrupt();
//                }
//                System.out.println("Task completed by " + Thread.currentThread().getName());
//            }, executor).exceptionally(ex -> {
//                System.out.println("Task failed: " + ex.getMessage());
//                return null;
//            });
            executor.submit(() -> {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
//                System.out.println("Task completed by " + Thread.currentThread().getName());
            });
        }

        // 메인 쓰레드는 계속해서 실행
        System.out.println("Main thread is running " + (System.currentTimeMillis() - start) + "ms");

        // Executor 종료를 기다리지 않고 즉시 종료
        executor.shutdown();
    }

}
