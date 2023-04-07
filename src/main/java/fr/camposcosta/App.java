package fr.camposcosta;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Hello world!
 */
public class App {

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        Future<String> future = calculateAsync();

        System.out.println("Blocking for future result");
        String result = future.get();
        System.out.println("Result : " + result);
    }

    public static Future<String> calculateAsync() {
        CompletableFuture<String> completableFuture = new CompletableFuture<>();

        Executors.newCachedThreadPool()
                .submit(() -> {
                    try {
                        Thread.sleep(2000);
                        System.out.println("completing future");
                        completableFuture.complete("Hello");
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                });

        System.out.println("Returning future");
        return completableFuture;
    }
}
