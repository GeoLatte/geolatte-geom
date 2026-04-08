package org.geolatte.geom.json;

import org.geolatte.geom.Box;
import org.geolatte.geom.Position;
import org.geolatte.geom.json.spi.GeoJsonWriter;

import java.util.Arrays;

/**
 * Jackson-free writer for the GeoJSON {@code bbox} representation of a {@link Box}.
 *
 * <p>A bbox is encoded as a flat JSON array of {@code [lowerLeft..., upperRight...]}
 * coordinates.</p>
 */
public final class GeoJsonBoxWriter {

    public <P extends Position> void write(GeoJsonWriter out, Box<P> box) {
        final double[] bbox = concat(box.lowerLeft().toArray(null), box.upperRight().toArray(null));
        out.writeDoubleArray(bbox, 0, bbox.length);
    }

    private static double[] concat(double[] pos1, double[] pos2) {
        double[] result = Arrays.copyOf(pos1, pos1.length + pos2.length);
        System.arraycopy(pos2, 0, result, pos1.length, pos2.length);
        return result;
    }
}
