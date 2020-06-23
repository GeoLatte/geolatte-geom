package org.geolatte.geom.generator;

import org.geolatte.geom.*;

import java.util.List;
import java.util.Random;

import static java.util.Arrays.asList;

/**
 * Created by Karel Maesen, Geovise BVBA on 03/08/2018.
 */
public class GeometryGenerators {

    public static <P extends Position> Generator<Geometry<P>> combine
            (final Random rnd, final List<Generator<? extends Geometry<P>>> generators) {
        return new CombinedGeometryGenerator<>(rnd, generators);
    }

    public static <P extends Position> Generator<Geometry<P>> combine
            (final List<Generator<? extends Geometry<P>>> generators) {
        return new CombinedGeometryGenerator<>(new Random(), generators);
    }

    public static <P extends Position> Generator<Point<P>> point(Box<P> bbox) {
        return new DefaultPointGenerator<>(bbox, new Random());
    }

    public static <P extends Position> Generator<Point<P>> point(Box<P> bbox, Random rnd) {
        return new DefaultPointGenerator<>(bbox, rnd);
    }

    public static <P extends Position> Generator<LineString<P>> lineString(int numPnts, Box<P> bbox,
                                                                                      Random rnd) {
        return new DefaultLineStringGenerator<>(numPnts, bbox, rnd);
    }

    public static <P extends Position> Generator<LineString<P>> lineString(int numPnts, Box<P> bbox) {
        return new DefaultLineStringGenerator<>(numPnts, bbox, new Random());
    }

    public static <P extends Position> Generator<Polygon<P>> polygon(int numPnts, Box<P> bbox,
                                                                                Random rnd) {
        return new DefaultPolygonGenerator<>(numPnts, bbox, rnd);
    }

    public static <P extends Position> Generator<Polygon<P>> polygon(int numPnts, Box<P> bbox) {
        return new DefaultPolygonGenerator<>(numPnts, bbox, new Random());
    }

    public static <P extends Position> Generator<MultiPoint<P>> multiPoint(int numPnts, Box<P> bbox,
                                                                                      Random rnd) {
        return new DefaultMultiPointGenerator<>(numPnts, bbox, rnd);
    }

    public static <P extends Position> Generator<MultiPoint<P>> multiPoint(int numPnts, Box<P> bbox) {
        return new DefaultMultiPointGenerator<>(numPnts, bbox, new Random());
    }

    public static <P extends Position> Generator<MultiLineString<P>> multiLineString(int numLines,
                                                                                                int numPoints, Box<P> bbox, Random rnd) {
        return new DefaultMultiLineStringGenerator<>(numLines, numPoints, bbox, rnd);
    }

    public static <P extends Position> Generator<MultiLineString<P>> multiLineString(int numLines,
                                                                                                int numPoints, Box<P> bbox) {
        return new DefaultMultiLineStringGenerator<>(numLines, numPoints, bbox, new Random());
    }

    public static <P extends Position> Generator<MultiPolygon<P>> multiPolygon
            (int numLines, int numPoints, Box<P> bbox, Random rnd) {
        return new DefaultMultiPolygonGenerator<>(numLines, numPoints, bbox, rnd);
    }

    public static <P extends Position> Generator<MultiPolygon<P>> multiPolygon
            (int numLines, int numPoints, Box<P> bbox) {
        return new DefaultMultiPolygonGenerator<>(numLines, numPoints, bbox, new Random());
    }

    @SafeVarargs
    public static <P extends Position> Generator<GeometryCollection<P>> geometryCollection
            (int numGeoms, Random rnd, Generator<? extends Geometry<P>>... generators) {

        Generator<Geometry<P>> combine = combine(asList(generators));
        return new DefaultGeometryCollectionGenerator<>(numGeoms, combine, rnd);
    }

    @SafeVarargs
    public static <P extends Position> Generator<GeometryCollection<P>> geometryCollection
            (int numGeoms, Generator<? extends Geometry<P>>... generators) {
        return geometryCollection(numGeoms, new Random(), generators);
    }

}
