package org.geolatte.geom.generator;

import org.geolatte.geom.*;
import org.geolatte.geom.builder.DSL;

import java.util.Random;

/**
 * Created by Karel Maesen, Geovise BVBA on 28/09/2018.
 */
class DefaultMultiLineStringGenerator<P extends Position> extends AbstractGeometryGenerator<P, MultiLineString<P>> {

    private final int numLineString;
    private final DefaultLineStringGenerator<P> lineStrGen;

    DefaultMultiLineStringGenerator(int numLineString, int numPoints, Envelope<P> bbox, Random rnd) {
        super(bbox, rnd);
        this.numLineString = numLineString;
        this.lineStrGen = new DefaultLineStringGenerator<>(numPoints, false, bbox, rnd);
    }

    @Override
    public MultiLineString<P> generate() {
        LineString<P>[] linestrings = (LineString<P>[]) new LineString[numLineString - 1];
        lineStrGen.generateArray(linestrings);
        return DSL.multilinestring(lineStrGen.generate(), linestrings);
    }
}
