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
import static org.geolatte.geom.builder.DSL.g;
import static org.geolatte.geom.crs.CoordinateReferenceSystems.WGS84;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by Karel Maesen, Geovise BVBA on 2019-07-29.
 */
public class GeneratorsTest {

    private Box<G2D> box = new Box<>(g(10, 10), g(80, 80), WGS84);

    @Test
    public void testPointGenerator(){
        Generator<Point<G2D>> gen = GeometryGenerators.point(box);
        Point<G2D> pnt = gen.generate();
        assertTrue(box.contains(pnt.getPosition()));
    }

    @Test
    public void testLineStringGenerator() {
        Generator<LineString<G2D>> generator = GeometryGenerators.lineString(3, box);
        LineString<G2D> ls = generator.generate();
        assertEquals(3, ls.getNumPositions());
        assertTrue(ls.getBoundingBox().within(box));

    }

    @Test
    public void testPolygonGenerator() {
        Generator<Polygon<G2D>> generator = GeometryGenerators.polygon(24, box);
        Polygon<G2D> polygon = generator.generate();
        assertEquals(24, polygon.getNumPositions());
        assertTrue(polygon.getBoundingBox().within(box));
    }

    @Test
    public void testMultiPointGenerator() {
        Generator<MultiPoint<G2D>> generator = GeometryGenerators.multiPoint(24, box);
        MultiPoint<G2D> mp = generator.generate();
        assertEquals(24, mp.getNumPositions());
        assertTrue(mp.getBoundingBox().within(box));
    }

    @Test
    public void testGeometryCollectionGenerator() {
        Generator<GeometryCollection<G2D>> generator = GeometryGenerators.geometryCollection (
                3,
                GeometryGenerators.lineString(3, box),
                GeometryGenerators.point(box)
        );
        GeometryCollection<G2D> gc = generator.generate();
        assertEquals(3, gc.getNumGeometries());
        assertTrue(gc.getBoundingBox().within(box));
    }

    @Test
    public void testStringGenerator(){
        Generator<String> strng = new ValueGeneratorFactory().stringGenerator(5, 20);
        for(int i = 0; i < 100; i++){
            String s = strng.generate();
            assert (s.length() <= 20 && s.length() >= 5);
        }
    }

    @Test
    public void testIntGenerator(){
        Generator<Integer> ints = new ValueGeneratorFactory().integerGenerator(5, 20);
        for(int i = 0; i < 100; i++){
            Integer g = ints.generate();
            assert (g <= 20 && g >= 5);
        }
    }

    @Test
    public void testInstantGenerator(){
        LocalDateTime start = LocalDateTime.of(2015, 1, 1, 0, 0);
        LocalDateTime end = LocalDateTime.of(2016, 1, 1, 0, 0);
        Generator<Instant> instants = new ValueGeneratorFactory().instantGenerator(start.toInstant(ZoneOffset.UTC), end.toInstant(ZoneOffset.UTC));
        for(int i = 0; i < 100; i++){
            LocalDateTime dt = LocalDateTime.ofInstant(instants.generate(), ZoneOffset.UTC);
            assert ( dt.isAfter(start) && dt.isBefore(end) );
        }
    }

    @Test
    public void testPropertyMapGenerator(){
        ValueGeneratorFactory generator = new ValueGeneratorFactory();
        List<Generator<?>> generators = asList(generator.integerGenerator(0, 100), generator.stringGenerator(1, 100));
        PropertyMapGenerator props = new PropertyMapGenerator(5,
                generator.stringGenerator(3,5),
                Choice.of(generators)
        );
        Map<String, Object> map = props.generate();
        assert (map.keySet().size() == 5);
    }
}
