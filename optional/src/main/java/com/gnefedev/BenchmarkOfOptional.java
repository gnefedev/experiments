package com.gnefedev;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.*;
import java.util.function.Function;


/**
 Benchmark                       Mode  Cnt         Score        Error  Units
 BenchmarkOfOptional.functions  thrpt  100  17416928.181 ± 343359.074  ops/s
 BenchmarkOfOptional.optional   thrpt  100  11779311.114 ± 209356.914  ops/s
 */
@Threads(1)
@Warmup(iterations = 100)
@Measurement(iterations = 100)
public class BenchmarkOfOptional {
    @Benchmark
    public String functions(HumanHolder humanHolder) {
        return getOrNull(humanHolder.human, Human::getName, Human.Name::getFirstName);
    }

    @Benchmark
    public String optional(HumanHolder humanHolder) {
        return Optional.ofNullable(humanHolder.human)
                .map(Human::getName)
                .map(Human.Name::getFirstName)
                .orElse(null);
    }

    public static <R, S, S2> R getOrNull(S from, Function<S, S2> getFirst, Function<S2, R> thenGet) {
        if (from == null) {
            return null;
        }
        S2 temp = getFirst.apply(from);
        if (temp == null) {
            return null;
        }
        return thenGet.apply(temp);
    }

    public static void main(String[] args) throws RunnerException {
        Options options = new OptionsBuilder()
                .include(BenchmarkOfOptional.class.getSimpleName())
                .forks(1)
                .build();
        new Runner(options).run();
    }

    @State(Scope.Thread)
    public static class HumanHolder {
        private Human human;

        @Setup(Level.Invocation)
        public void setup(HumansListHolder humansListHolder) {
            if (!humansListHolder.humanIterator.hasNext()) {
                humansListHolder.humanIterator = humansListHolder.humans.iterator();
            }
            human = humansListHolder.humanIterator.next();
        }
    }

    @State(Scope.Benchmark)
    public static class HumansListHolder {
        private List<Human> humans;
        private Iterator<Human> humanIterator;

        @Setup(Level.Trial)
        public void setup() {
            Random random = new Random(new Date().getTime());
            humans = new ArrayList<>();
            for (int i = 0; i < 1_000_000; i++) {
                Human human;
                if (random.nextBoolean() && random.nextBoolean() && random.nextBoolean()) {
                    human = null;
                } else {
                    human = new Human();
                    Human.Name name;
                    if (random.nextBoolean() && random.nextBoolean() ) {
                        name = null;
                    } else {
                        name = new Human.Name();
                        if (random.nextBoolean()) {
                            name.setFirstName(String.valueOf(random.nextLong()));
                        }
                    }
                    human.setName(name);
                }
                humans.add(human);
            }
        }

        @Setup(Level.Iteration)
        public void initIterator() {
            humanIterator = humans.iterator();
        }
    }

    public static class Human {
        private Name name;

        public Name getName() {
            return name;
        }

        public void setName(Name name) {
            this.name = name;
        }

        public static class Name {
            private String firstName;

            public String getFirstName() {
                return firstName;
            }

            public void setFirstName(String firstName) {
                this.firstName = firstName;
            }
        }
    }
}
