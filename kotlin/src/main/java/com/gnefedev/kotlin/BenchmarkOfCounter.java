package com.gnefedev.kotlin;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Threads;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

//Benchmark                          Mode  Cnt       Score        Error  Units
//BenchmarkOfCounter.atomicCounter  thrpt   20   93921.868 ±   1381.567  ops/s
//BenchmarkOfCounter.withCapacity   thrpt   20  508740.751 ± 269215.343  ops/s
@Threads(Threads.MAX)
public class BenchmarkOfCounter {
    @Benchmark
    public void atomicCounter() {
        AtomicCounter.INSTANCE.increment();
    }

//    @Benchmark
//    public void oneThreadCoroutineCounter() {
//        OneThreadCoroutineCounter.INSTANCE.increment();
//    }

//    @Benchmark
//    public void channelCoroutineCounter() {
//        new ChannelCoroutineCounter(0);
//    }

    @Benchmark
    public void withCapacity() {
        new ChannelCoroutineCounter(100);
    }

    public static void main(String[] args) throws RunnerException {
        Options options = new OptionsBuilder()
                .include(BenchmarkOfCounter.class.getSimpleName())
                .forks(1)
                .build();
        new Runner(options).run();
    }

}
