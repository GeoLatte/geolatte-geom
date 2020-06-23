package org.geolatte.geom.generator;

import java.util.Collection;
import java.util.List;
import java.util.Random;

/**
 * Created by Karel Maesen, Geovise BVBA on 2019-08-16.
 */
public class Choice<T> implements Generator<T> {

    private final List<T> choices;
    private final Random rnd;

    public  static <T> Choice<T> of(List<T> choices) {
        return new Choice<T>(choices);
    }

    public Choice(List<T> choices, Random rnd) {
        this.choices = choices;
        this.rnd = rnd;
    }

    public Choice(List<T> choices) {
        this(choices, new Random());
    }

    @Override
    public T generate() {
        int index = rnd.nextInt(choices.size());
        return choices.get(index);
    }
}
