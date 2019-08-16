package org.geolatte.geom.generators;

import org.geolatte.geom.*;
import org.geolatte.geom.crs.CoordinateReferenceSystem;
import org.geolatte.geom.generator.Generator;
import org.geolatte.geom.generator.GeometryGenerators;
import org.junit.Test;

import static org.geolatte.geom.crs.CoordinateReferenceSystems.WGS84;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by Karel Maesen, Geovise BVBA on 2019-07-29.
 */
public class GeneratorsTest {

    private Envelope<G2D> env = new Envelope<>(10, 10, 80, 80, WGS84);

    @Test
    public void testPointGenerator(){
        Generator<Point<G2D>> gen = GeometryGenerators.point(env);
        Point<G2D> pnt = gen.generate();
        assertTrue(pnt.getEnvelope().within(env));
    }

    @Test
    public void testLineStringGenerator() {
        Generator<LineString<G2D>> generator = GeometryGenerators.lineString(3, env);
        LineString<G2D> ls = generator.generate();
        assertEquals(3, ls.getNumPositions());
        assertTrue(ls.getEnvelope().within(env));

    }

    @Test
    public void testPolygonGenerator() {
        Generator<Polygon<G2D>> generator = GeometryGenerators.polygon(24, env);
        Polygon<G2D> ls = generator.generate();
        assertEquals(24, ls.getNumPositions());
        assertTrue(ls.getEnvelope().within(env));
    }

    @Test
    public void testMultiPointGenerator() {
        Generator<MultiPoint<G2D>> generator = GeometryGenerators.multiPoint(24, env);
        MultiPoint<G2D> ls = generator.generate();
        assertEquals(24, ls.getNumPositions());
        assertTrue(ls.getEnvelope().within(env));
    }

    @Test
    public void testGeometryCollectionGenerator() {
        Generator<GeometryCollection<G2D>> generator = GeometryGenerators.geometryCollection (
                3,
                GeometryGenerators.lineString(3, env),
                GeometryGenerators.point(env)
        );
        GeometryCollection<G2D> gc = generator.generate();
        assertEquals(3, gc.getNumGeometries());
        assertTrue(gc.getEnvelope().within(env));
    }
    
}
