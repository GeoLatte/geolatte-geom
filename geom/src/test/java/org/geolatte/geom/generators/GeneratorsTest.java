package org.geolatte.geom.generators;

import org.geolatte.geom.*;
import org.geolatte.geom.generator.*;
import org.junit.Test;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;
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

    @Test
    public void testStringGenerator(){
        Generator<String> strng = new ValueGenerator().stringGenerator(5, 20);
        for(int i = 0; i < 100; i++){
            String s = strng.generate();
            assert (s.length() <= 20 && s.length() >= 5);
        }
    }

    @Test
    public void testIntGenerator(){
        Generator<Integer> ints = new ValueGenerator().integerGenerator(5, 20);
        for(int i = 0; i < 100; i++){
            Integer g = ints.generate();
            System.out.println(g);
            assert (g <= 20 && g >= 5);
        }
    }

    @Test
    public void testInstantGenerator(){
        LocalDateTime start = LocalDateTime.of(2015, 1, 1, 0, 0);
        LocalDateTime end = LocalDateTime.of(2016, 1, 1, 0, 0);
        Generator<Instant> instants = new ValueGenerator().instantGenerator(start.toInstant(ZoneOffset.UTC), end.toInstant(ZoneOffset.UTC));
        for(int i = 0; i < 100; i++){
            LocalDateTime dt = LocalDateTime.ofInstant(instants.generate(), ZoneOffset.UTC);
            System.out.println(dt);
            assert ( dt.isAfter(start) && dt.isBefore(end) );
        }
    }

    @Test
    public void testPropertyMapGenerator(){
        ValueGenerator generator = new ValueGenerator();
        List<Generator<?>> generators = asList(generator.integerGenerator(0, 100), generator.stringGenerator(1, 100));
        PropertyMapGenerator props = new PropertyMapGenerator(5,
                generator.stringGenerator(3,5),
                Choice.of(generators)
        );
        for(int i = 0; i < 100; i++){
            Map<String, Object> map = props.generate();
            assert ( map.keySet().size() == 5 );
        }
    }
}
