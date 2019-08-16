package org.geolatte.geom.generator;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Created by Karel Maesen, Geovise BVBA on 2019-08-16.
 */
public class ValueGenerator {

    private final Random rnd;

    public ValueGenerator(Random rnd) {
        this.rnd = rnd;
    }

    public ValueGenerator() {
        this(new Random());
    }

    public Generator<String> stringGenerator(int minLength, int maxLength) {
        return new Generator<String>() {
            private final Choice<Character> ascii = new Choice<>(asciiRange());

            public String generate() {
                int length = minLength + rnd.nextInt(maxLength - minLength + 1);
                char[] chars = new char[length];
                IntStream.range(0, length).forEach(i -> chars[i] = ascii.generate());
                return String.valueOf(chars);
            }
        };
    }

    public Generator<Integer> integerGenerator(int min, int max) {
        return () -> min + rnd.nextInt(max - min + 1);
    }

    public Generator<Double> doubleGenerator(double min, double max) {
        return () -> min + rnd.nextDouble() * (max - min);
    }

    public Generator<Instant> instantGenerator(Instant start, Instant end) {
        return () -> {
            long seconds = Duration.between(start, end).getSeconds();
            return Instant.ofEpochSecond(start.getEpochSecond() + (long) (rnd.nextDouble() * seconds));
        };
    }

    private static List<Character> asciiRange() {
        return IntStream.rangeClosed('a', 'z').mapToObj(i -> (char) i).collect(Collectors.toList());
    }

}
