package org.geolatte.geom.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.geolatte.geom.Box;
import org.geolatte.geom.Position;

import java.io.IOException;
import java.util.Arrays;

public class BoxSerializer<P extends Position> extends JsonSerializer<Box<P>> {

    @Override
    public void serialize(Box<P> box, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        final double[] bbox = concat(box.lowerLeft().toArray(null), box.upperRight().toArray(null));
        gen.writeArray(bbox, 0, bbox.length);
    }

    @Override
    public boolean isEmpty(SerializerProvider provider, Box<P> box) {
        return box == null || box.isEmpty();
    }

    private double[] concat(double[] pos1, double[] pos2) {
        double[] result = Arrays.copyOf(pos1, pos1.length + pos2.length);
        System.arraycopy(pos2, 0, result, pos1.length, pos2.length);
        return result;
    }
}
