package org.geolatte.geom.generator;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.stream.IntStream;

/**
 * Created by Karel Maesen, Geovise BVBA on 2019-08-16.
 */
public class PropertyMapGenerator implements Generator<Map<String, Object>> {

    private final int size;
    private final Choice<Generator<?>> valueGenerator;
    private final Generator<String> keyGenerator;

    public PropertyMapGenerator(int size, Generator<String> keyGenerator, Choice<Generator<?>> valueGeneratorChoice) {
        this.size = size;
        this.keyGenerator = keyGenerator;
        valueGenerator = valueGeneratorChoice;
    }

    @Override
    public Map<String, Object> generate() {
        Map<String, Object> map = new HashMap<>();
        IntStream.range(0, size).forEach(i -> {
            map.put(keyGenerator.generate(), valueGenerator.generate().generate());
        });
        return map;
    }
}
