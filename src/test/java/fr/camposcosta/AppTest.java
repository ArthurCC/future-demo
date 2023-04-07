package fr.camposcosta;

import org.junit.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Unit test for simple App.
 */
public class AppTest {
    /**
     * Rigorous Test :-)
     */
    @Test
    public void shouldAnswerWithTrue() {
        assertTrue(true);
    }

    /**
     * CompletableFuture implements future interface so we can use it as simple future
     *
     * @throws ExecutionException
     * @throws InterruptedException
     */
    @Test
    public void simpleFuture() throws ExecutionException, InterruptedException {
        Future<String> future = calculateAsync();

        System.out.println("Blocking for future result");
        String result = future.get();
        System.out.println("Result : " + result);

        assertEquals("Hello", result);
    }

    private Future<String> calculateAsync() {
        CompletableFuture<String> completableFuture = new CompletableFuture<>();

        Executors.newCachedThreadPool()
                .submit(() -> {
                    sleep(2000);
                    System.out.println("completing future");
                    completableFuture.complete("Hello");
                });

        System.out.println("Returning future");
        return completableFuture;
    }

    /**
     * Execute some code asynchronously using runAsync (Runnable) or supplyAsync (Supplier)
     */
    @Test
    public void encapsulatedLogic() throws ExecutionException, InterruptedException {
        CompletableFuture<String> futureSupply = CompletableFuture.supplyAsync(() -> "Hello from Supplier");
        CompletableFuture.runAsync(() -> System.out.println("Hello from runnable"));

        assertEquals("Hello from Supplier", futureSupply.get());
    }

    /**
     * thenApply method takes a function that process the result of a future
     * result is first arg and returns a new future that holds the result of the process
     */
    @Test
    public void processAsyncComputationResultThenApply() throws ExecutionException, InterruptedException {
        CompletableFuture<String> completableFuture = CompletableFuture.supplyAsync(() -> {
            System.out.println("Waiting");
            sleep(2000);
            System.out.println("completing future");
            return "Hello";
        });

        // Create new Future that waits for result and process
        CompletableFuture<String> processFuture = completableFuture.thenApply(res -> {
            System.out.println("processing result");
            sleep(2000);
            return res + " World";
        });

        System.out.println("Waiting for processed result");
        assertEquals("Hello World", processFuture.get());
    }

    /**
     * thenAccept uses a consumer for processing result and does not return a result
     */
    @Test
    public void processAsyncComputationResultThenAccept() {
        CompletableFuture<String> completableFuture = CompletableFuture.supplyAsync(() -> "Hello");

        completableFuture.thenAccept(res -> System.out.println(res));
    }

    /**
     * thenRun takes a Runnable and ignore the result
     * used for running some logging for example
     */
    @Test
    public void processAsyncComputationResultThenRun() {
        CompletableFuture<String> completableFuture = CompletableFuture.supplyAsync(() -> "Hello");

        completableFuture.thenRun(() -> System.out.println("Computation finished"));
    }

    /**
     * thenCompose accept a function with result of previous future as arg and return a new Future
     * Basically when first future returns, we compute another async operation using the result of that first Future
     * Sequential execution of async operation basically
     */
    @Test
    public void combiningFuturesThenCompose() throws ExecutionException, InterruptedException {
        CompletableFuture<String> completableFuture = CompletableFuture.supplyAsync(() -> {
            System.out.println("Computing 1...");
            sleep(2000);
            System.out.println("Completing 1");
            return "Hello";
        }).thenCompose(res -> {
            CompletableFuture composedFuture = CompletableFuture.supplyAsync(() -> {
                System.out.println("Computing 2...");
                sleep(2000);
                System.out.println("Completing 2");
                return res + " World";
            });

            System.out.println("Doing some other stuff in thenCompose");
            sleep(500);

            return composedFuture;
        });

        System.out.println("Waiting for result");
        assertEquals("Hello World", completableFuture.get());
    }

    /**
     * thenCombine computes two futures independently and process both their results
     * 1 arg -> second future
     * 2 arg -> BiFunction that processes both results
     */
    @Test
    public void combingFuturesThenCombine() throws ExecutionException, InterruptedException {
        CompletableFuture<String> completableFuture = CompletableFuture.supplyAsync(() ->
                {
                    System.out.println("Computing 1...");
                    sleep(2000);
                    return "Hello";
                })
                .thenCombine(
                        CompletableFuture.supplyAsync(() -> {
                            System.out.println("Computing 2...");
                            return " World";
                        }),
                        (res1, res2) -> {
                            System.out.println("Processing results...");
                            sleep(1000);
                            return res1 + res2;
                        }
                );

        System.out.println("Waiting for result of combining future...");
        assertEquals("Hello World", completableFuture.get());
    }

    /**
     * Use thenAcceptBoth when we don't want to return anything from the results
     */
    @Test
    public void combiningFuturesThenAcceptBoth() {
        CompletableFuture.supplyAsync(() -> "Hello")
                .thenAcceptBoth(
                        CompletableFuture.supplyAsync(() -> " World"),
                        (res1, res2) -> System.out.println(res1 + res2)
                );
    }

    /**
     * use allOf to group multiple futures execution into one
     * This method doesn't return anything and we have to get the result of all futures manually
     */
    @Test
    public void runningMultipleFuturesParallelAllOf() throws ExecutionException, InterruptedException {
        CompletableFuture<String> future1 = CompletableFuture.supplyAsync(() -> {
            System.out.println("Computing 1...");
            sleep(1500);
            System.out.println("Completing 1");
            return "Hello";
        });
        CompletableFuture<String> future2 = CompletableFuture.supplyAsync(() -> {
            System.out.println("Computing 2...");
            sleep(500);
            System.out.println("Completing 2");
            return " Beautiful";
        });
        CompletableFuture<String> future3 = CompletableFuture.supplyAsync(() -> {
            System.out.println("Computing 3...");
            sleep(1000);
            System.out.println("Completing 3");
            return " World";
        });

        CompletableFuture<Void> combinedFuture = CompletableFuture.allOf(future1, future2, future3);

        System.out.println("Waiting for all futures to complete...");
        combinedFuture.get();

        System.out.println("All futures completed");
        assertTrue(future1.isDone());
        assertTrue(future2.isDone());
        assertTrue(future3.isDone());
    }

    /**
     * We can use join to combine the result of multiple futures into a new result
     * join() is similar to get() but throws a runtime exception if Future doesn't complete
     */
    @Test
    public void runningMultipleFuturesParallelJoin() {
        CompletableFuture<String> future1 = CompletableFuture.supplyAsync(() -> {
            System.out.println("Computing 1...");
            sleep(1500);
            System.out.println("Completing 1");
            return "Hello";
        });
        CompletableFuture<String> future2 = CompletableFuture.supplyAsync(() -> {
            System.out.println("Computing 2...");
            sleep(500);
            System.out.println("Completing 2");
            return " Beautiful";
        });
        CompletableFuture<String> future3 = CompletableFuture.supplyAsync(() -> {
            System.out.println("Computing 3...");
            sleep(1000);
            System.out.println("Completing 3");
            return " World";
        });


        String combined = Stream.of(future1, future2, future3)
                .map((future) -> {
                    System.out.println("Waiting for future");
                    return future.join();
                })
                .collect(Collectors.joining());

        assertEquals("Hello Beautiful World", combined);
    }

    /**
     * Use handle method that takes a function with 2 arguments
     * 1st : result of computation if completed successfully
     * 2nd : Exception thrown if an error occurred during computation
     */
    @Test
    public void errorHandlingHandle() throws ExecutionException, InterruptedException {
        String name = null;
        CompletableFuture<String> completableFuture = CompletableFuture.supplyAsync(() -> {
                    System.out.println("Computing...");
                    sleep(2000);
                    if (name == null) throw new RuntimeException("Computation error !");
                    return "Hello " + name;
                })
                .handle((res, ex) -> res != null ? res : "Hello stranger");

        System.out.println("Waiting for results");
        assertEquals("Hello stranger", completableFuture.get());
    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
