package com.gnefedev.coroutine;

import kotlin.jvm.functions.Function0;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Random;

@Threads(1)
@Warmup(iterations = 10)
@Measurement(iterations = 10)
public class BenchmarkOfServer {
//    @Benchmark
//    public void callbackSimpleDoInFuture(JobHolder jobHolder) {
//        AtomicInteger counter = new AtomicInteger(0);
//        for (int i = 0; i < 1000; i++) {
//            DoInFuture.INSTANCE.withCallback(counter::incrementAndGet, jobHolder.simpleWork);
//        }
//        while (counter.get() != 1000) {
//            Thread.yield();
//        }
//    }
//
//    @Benchmark
//    public void callbackSimpleDoInCoroutine(JobHolder jobHolder) {
//        AtomicInteger counter = new AtomicInteger(0);
//        for (int i = 0; i < 1000; i++) {
//            DoInCoroutine.INSTANCE.withCallback(counter::incrementAndGet, jobHolder.simpleWork);
//        }
//        while (counter.get() != 1000) {
//            Thread.yield();
//        }
//    }
//
//
//    @Benchmark
//    public void callbackGCLoadDoInFuture(JobHolder jobHolder) {
//        AtomicInteger counter = new AtomicInteger(0);
//        for (int i = 0; i < 1000; i++) {
//            DoInFuture.INSTANCE.withCallback(counter::incrementAndGet, jobHolder.gcLoadWork);
//        }
//        while (counter.get() != 1000) {
//            Thread.yield();
//        }
//    }
//
//    @Benchmark
//    public void callbackGCLoadDoInCoroutine(JobHolder jobHolder) {
//        AtomicInteger counter = new AtomicInteger(0);
//        for (int i = 0; i < 1000; i++) {
//            DoInCoroutine.INSTANCE.withCallback(counter::incrementAndGet, jobHolder.gcLoadWork);
//        }
//        while (counter.get() != 1000) {
//            Thread.yield();
//        }
//    }
//
//
//
//
//    @Benchmark
//    public String bulkSimpleDoInFuture(JobHolder jobHolder) {
//        return DoInFuture.INSTANCE.collect(100, "", String::concat, jobHolder.simpleWork);
//    }
//
//    @Benchmark
//    public String bulkSimpleDoInCoroutine(JobHolder jobHolder) {
//        return DoInCoroutine.INSTANCE.collect(100, "", String::concat, jobHolder.simpleWork);
//    }
//
//
//
//    @Benchmark
//    public String bulkGCLoadDoInFuture(JobHolder jobHolder) {
//        return DoInFuture.INSTANCE.collect(100, "", String::concat, jobHolder.gcLoadWork);
//    }
//
//    @Benchmark
//    public String bulkGCLoadDoInCoroutine(JobHolder jobHolder) {
//        return DoInCoroutine.INSTANCE.collect(100, "", String::concat, jobHolder.gcLoadWork);
//    }



//    @Benchmark
//    public String bulkPingSyncDoInFuture(JobHolder jobHolder) {
//        return DoInFuture.INSTANCE.collect(100, "", String::concat, jobHolder.pingYandexSync);
//    }
//
//    @Benchmark
//    public String bulkPingAsyncInCoroutine(JobHolder jobHolder) {
//        return DoInCoroutine.INSTANCE.collect(100, "", String::concat, jobHolder.pingYandexSync);
//    }


    @Benchmark
    public String file1SyncDoInFuture(JobHolder jobHolder) {
        return DoInFuture.INSTANCE.readSync(1);
    }

    @Benchmark
    public String file1SyncInCoroutine(JobHolder jobHolder) {
        return DoInCoroutine.INSTANCE.readSync(1);
    }

    @Benchmark
    public String file1AsyncDoInFuture(JobHolder jobHolder) {
        return DoInFuture.INSTANCE.readAsync(1);
    }

    @Benchmark
    public String file1AsyncInCoroutine(JobHolder jobHolder) {
        return DoInCoroutine.INSTANCE.readAsync(1);
    }


    @Benchmark
    public String file5SyncDoInFuture(JobHolder jobHolder) {
        return DoInFuture.INSTANCE.readSync(5);
    }

    @Benchmark
    public String file5SyncInCoroutine(JobHolder jobHolder) {
        return DoInCoroutine.INSTANCE.readSync(5);
    }

    @Benchmark
    public String file5AsyncDoInFuture(JobHolder jobHolder) {
        return DoInFuture.INSTANCE.readAsync(5);
    }

    @Benchmark
    public String file5AsyncInCoroutine(JobHolder jobHolder) {
        return DoInCoroutine.INSTANCE.readAsync(5);
    }


    @Benchmark
    public String file100SyncDoInFuture(JobHolder jobHolder) {
        return DoInFuture.INSTANCE.readSync(100);
    }

    @Benchmark
    public String file100SyncInCoroutine(JobHolder jobHolder) {
        return DoInCoroutine.INSTANCE.readSync(100);
    }

    @Benchmark
    public String file100AsyncDoInFuture(JobHolder jobHolder) {
        return DoInFuture.INSTANCE.readAsync(100);
    }

    @Benchmark
    public String file100AsyncInCoroutine(JobHolder jobHolder) {
        return DoInCoroutine.INSTANCE.readAsync(100);
    }



//    @Benchmark
//    public String simpleDoInFuture(JobHolder jobHolder) {
//        return DoInFuture.INSTANCE.doWork(jobHolder.simpleWork);
//    }
//
//    @Benchmark
//    public String simpleDoInCoroutine(JobHolder jobHolder) {
//        return DoInCoroutine.INSTANCE.doWork(jobHolder.simpleWork);
//    }
//


//    @Benchmark
//    public String gcLoadDoInFuture(JobHolder jobHolder) {
//        return DoInFuture.INSTANCE.doWork(jobHolder.gcLoadWork);
//    }
//
//    @Benchmark
//    public String gcLoadDoInCoroutine(JobHolder jobHolder) {
//        return DoInCoroutine.INSTANCE.doWork(jobHolder.gcLoadWork);
//    }

    public static void main(String[] args) throws RunnerException {
            Options options = new OptionsBuilder()
                    .include(BenchmarkOfServer.class.getSimpleName())
                    .forks(1)
                    .jvmArgs("-Xmx2g")
                    .build();
            new Runner(options).run();
    }

    @State(Scope.Thread)
    public static class JobHolder {
        private Function0<String> simpleWork;
        private Function0<String> gcLoadWork;
        private Function0<String> pingYandexSync;
        private Function0<String> pingYandexAsync;

        @Setup(Level.Invocation)
        public void init() {
            Random random = new Random();
            simpleWork = () -> {
                double result = 0.0;
                for (int i = 0; i < 100_000; i++) {
                    result *= random.nextDouble();
                }
                return String.valueOf(result);
            };
            gcLoadWork = () -> {
                double result = 0.0;
                for (int i = 0; i < 3_000; i++) {
                    result *= new BigDecimal(random.nextDouble()).hashCode();
                }
                return String.valueOf(result);
            };
            pingYandexSync = () -> {
                try {
                    URL url = new URL("https://ya.ru/");
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();

                    connection.setRequestMethod("GET");

                    BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    String inputLine;
                    StringBuilder response = new StringBuilder();
                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }
                    in.close();

                    return response.toString();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            };
//            pingYandexAsync = () -> {
//                try {
//                    URL url = new URL("https://ya.ru/");
//                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
//
//                    connection.setRequestMethod("GET");
//
//                    BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
//                    String inputLine;
//                    StringBuilder response = new StringBuilder();
//                    while ((inputLine = in.readLine()) != null) {
//                        response.append(inputLine);
//                    }
//                    in.close();
//
//                    return response.toString();
//                } catch (Exception e) {
//                    throw new RuntimeException(e);
//                }
//            };

        }
    }

//    @State(Scope.Benchmark)
//    public static class ServersHolder {
//
//        @Setup(Level.Iteration)
//        public void initIterator() {
//        }
//
//        @TearDown(Level.Iteration)
//        public void printSystemStats() {
//            Runtime runtime = Runtime.getRuntime();
//
//            NumberFormat format = NumberFormat.getInstance();
//
//            long maxMemory = runtime.maxMemory();
//            long allocatedMemory = runtime.totalMemory();
//            long freeMemory = runtime.freeMemory();
//
//            System.out.println("free memory: " + format.format(freeMemory / 1024));
//            System.out.println("allocated memory: " + format.format(allocatedMemory / 1024));
//            System.out.println("max memory: " + format.format(maxMemory / 1024));
//            System.out.println("total free memory: " + format.format((freeMemory + (maxMemory - allocatedMemory)) / 1024));
//        }
//    }


}
