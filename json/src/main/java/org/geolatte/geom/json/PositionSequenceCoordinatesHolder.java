package org.geolatte.geom.json;

import java.util.*;
import java.util.function.Consumer;

/**
 * Created by Karel Maesen, Geovise BVBA on 09/09/17.
 */
class PositionSequenceCoordinatesHolder extends CoordinatesHolder implements Iterable<SinglePositionCoordinatesHolder> {

    final List<SinglePositionCoordinatesHolder> spcs = new ArrayList<SinglePositionCoordinatesHolder>();

    public PositionSequenceCoordinatesHolder(double[][] coordinates) {
        for (double[] co : coordinates) {
            spcs.add(new SinglePositionCoordinatesHolder(co));
        }
    }

    public List<SinglePositionCoordinatesHolder> getSpcs() {
        return Collections.unmodifiableList(spcs);
    }

    @Override
    public Iterator<SinglePositionCoordinatesHolder> iterator() {
        return spcs.iterator();
    }

    @Override
    public void forEach(Consumer<? super SinglePositionCoordinatesHolder> action) {
        spcs.forEach(action);
    }

    @Override
    public Spliterator<SinglePositionCoordinatesHolder> spliterator() {
        return spcs.spliterator();
    }

    @Override
    boolean isEmpty() {
        return spcs.isEmpty();
    }

    @Override
    int getCoordinateDimension() {
        return spcs.stream().mapToInt(CoordinatesHolder::getCoordinateDimension).max().orElse(0);
    }
}
