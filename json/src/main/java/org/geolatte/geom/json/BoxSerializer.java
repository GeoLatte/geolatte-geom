package org.geolatte.geom.json;

import org.geolatte.geom.Box;
import org.geolatte.geom.Position;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ValueSerializer;

import java.util.Arrays;

public class BoxSerializer<P extends Position> extends ValueSerializer<Box<P>> {

    @Override
    public void serialize(Box<P> box, JsonGenerator gen, SerializationContext serializers) {
        final double[] bbox = concat(box.lowerLeft().toArray(null), box.upperRight().toArray(null));
        gen.writeArray(bbox, 0, bbox.length);
    }

    @Override
    public boolean isEmpty(SerializationContext provider, Box<P> box) {
        return box == null || box.isEmpty();
    }

    private double[] concat(double[] pos1, double[] pos2) {
        double[] result = Arrays.copyOf(pos1, pos1.length + pos2.length);
        System.arraycopy(pos2, 0, result, pos1.length, pos2.length);
        return result;
    }
}
