package carely.utils;

import javafx.concurrent.Task;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.function.Consumer;

public final class BackgroundTasks {
    private static final ExecutorService EXECUTOR = Executors.newCachedThreadPool(new BackgroundThreadFactory());

    private BackgroundTasks() {
    }

    public static <T> void run(CheckedSupplier<T> action, Consumer<T> onSuccess, Consumer<Throwable> onFailure, Runnable onFinished) {
        Task<T> task = new Task<>() {
            @Override
            protected T call() throws Exception {
                return action.get();
            }
        };

        task.setOnSucceeded(event -> {
            try {
                onSuccess.accept(task.getValue());
            } finally {
                onFinished.run();
            }
        });
        task.setOnFailed(event -> {
            try {
                onFailure.accept(task.getException());
            } finally {
                onFinished.run();
            }
        });

        EXECUTOR.submit(task);
    }

    public static void shutdown() {
        EXECUTOR.shutdownNow();
    }

    @FunctionalInterface
    public interface CheckedSupplier<T> {
        T get() throws Exception;
    }

    private static final class BackgroundThreadFactory implements ThreadFactory {
        @Override
        public Thread newThread(Runnable runnable) {
            Thread thread = new Thread(runnable, "carely-background");
            thread.setDaemon(true);
            return thread;
        }
    }
}
