package org.geolatte.geom.cga;

import org.geolatte.geom.*;
import org.geolatte.geom.jts.JTS;
import org.junit.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.triangulate.VoronoiDiagramBuilder;

import java.util.*;

import static org.geolatte.geom.crs.CoordinateReferenceSystems.PROJECTED_2D_METER;
import static org.junit.Assert.*;

/**
 * Tests {@link VoronoiDiagram}.
 */
public class VoronoiDiagramTest {

    private final Random random = new Random(59L);

    @Test
    public void gridCellsAreSquares() {
        // a 5 x 5 grid: all interior cells are unit squares around the sites, with corners at half integers
        List<C2D> sites = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) sites.add(new C2D(i, j));
        }
        VoronoiDiagram<C2D> diagram = voronoi(sites, new double[]{-0.5, -0.5, 4.5, 4.5});
        for (int s = 0; s < sites.size(); s++) {
            Polygon<C2D> cell = diagram.getCells().get(s);
            C2D site = sites.get(s);
            assertEquals(5, cell.getNumPositions());   // 4 distinct corners: cocircular centers coincide
            for (C2D p : cell.getExteriorRing().getPositions()) {
                assertEquals(0.5, Math.abs(p.getX() - site.getX()), 0);
                assertEquals(0.5, Math.abs(p.getY() - site.getY()), 0);
            }
            assertEquals(1.0, JTS.to(cell).getArea(), 0);
        }
    }

    @Test
    public void singleAndCollinearSites() {
        VoronoiDiagram<C2D> one = voronoi(Collections.singletonList(new C2D(3, 4)), null);
        assertEquals(1, one.getCells().size());
        assertEquals(new org.locationtech.jts.geom.Envelope(2, 4, 3, 5),
                JTS.to(one.getCells().get(0)).getEnvelopeInternal());

        List<C2D> line = Arrays.asList(new C2D(2, 2), new C2D(0, 0), new C2D(1, 1));
        VoronoiDiagram<C2D> strips = voronoi(line, new double[]{-5, -5, 5, 5});
        assertPartition(strips, new double[]{-5, -5, 5, 5});
        assertNearestSite(strips, new double[]{-5, -5, 5, 5});

        List<C2D> horizontal = Arrays.asList(new C2D(0, 0), new C2D(2, 0), new C2D(3, 0));
        VoronoiDiagram<C2D> vertical = voronoi(horizontal, new double[]{-1, -1, 4, 1});
        assertEquals(2.0 * 2, JTS.to(vertical.getCells().get(0)).getArea(), 0);   // x in [-1, 1]
        assertEquals(1.5 * 2, JTS.to(vertical.getCells().get(1)).getArea(), 0);   // x in [1, 2.5]
        assertEquals(1.5 * 2, JTS.to(vertical.getCells().get(2)).getArea(), 0);   // x in [2.5, 4]

        assertTrue(voronoi(Collections.emptyList(), null).getDiagram().isEmpty());
    }

    @Test
    public void cellsOutsideTheClipEnvelopeAreEmpty() {
        List<C2D> sites = Arrays.asList(new C2D(0, 0), new C2D(10, 0), new C2D(0, 10), new C2D(100, 100));
        VoronoiDiagram<C2D> diagram = voronoi(sites, new double[]{-1, -1, 1, 1});
        assertEquals(4, diagram.getCells().size());
        assertFalse(diagram.getCells().get(0).isEmpty());
        assertTrue(diagram.getCells().get(3).isEmpty());
        assertEquals(1, diagram.getDiagram().getNumGeometries());
    }

    @Test
    public void randomSites() {
        for (int trial = 0; trial < 20; trial++) {
            int n = 2 + random.nextInt(300);
            double offset = trial % 2 == 0 ? 0 : 150000;
            List<C2D> sites = new ArrayList<>();
            for (int i = 0; i < n; i++) {
                sites.add(new C2D(offset + 100 * random.nextDouble(), offset + 100 * random.nextDouble()));
            }
            double[] box = {offset - 20, offset - 20, offset + 120, offset + 120};
            VoronoiDiagram<C2D> diagram = voronoi(sites, box);
            assertPartition(diagram, box);
            assertNearestSite(diagram, box);
        }
    }

    @Test
    public void nearlyCocircularSites() {
        // sites on a circle: all cells meet near the center, in a cluster of nearly coincident vertices
        for (double noise : new double[]{0, 1e-12, 1e-6}) {
            List<C2D> sites = new ArrayList<>();
            for (int i = 0; i < 64; i++) {
                double a = 2 * Math.PI * i / 64;
                sites.add(new C2D(Math.cos(a) + noise * random.nextGaussian(), Math.sin(a) + noise * random.nextGaussian()));
            }
            double[] box = {-3, -3, 3, 3};
            VoronoiDiagram<C2D> diagram = voronoi(sites, box);
            double area = 0;
            for (Polygon<C2D> cell : diagram.getCells()) area += JTS.to(cell).getArea();
            assertEquals(36, area, 1e-9);
            assertNearestSite(diagram, box);
        }
    }

    @Test
    public void sameCellsAsJts() {
        List<C2D> sites = new ArrayList<>();
        List<Coordinate> coordinates = new ArrayList<>();
        for (int i = 0; i < 500; i++) {
            double x = 1000 * random.nextDouble(), y = 1000 * random.nextDouble();
            sites.add(new C2D(x, y));
            coordinates.add(new Coordinate(x, y));
        }
        double[] box = {-100, -100, 1100, 1100};
        VoronoiDiagram<C2D> diagram = voronoi(sites, box);
        VoronoiDiagramBuilder builder = new VoronoiDiagramBuilder();
        builder.setSites(coordinates);
        builder.setClipEnvelope(new org.locationtech.jts.geom.Envelope(box[0], box[2], box[1], box[3]));
        org.locationtech.jts.geom.Geometry jts = builder.getDiagram(new GeometryFactory());
        assertEquals(500, jts.getNumGeometries());
        for (int i = 0; i < jts.getNumGeometries(); i++) {
            org.locationtech.jts.geom.Geometry cell = jts.getGeometryN(i);
            Coordinate site = (Coordinate) cell.getUserData();
            int s = coordinates.indexOf(site);
            assertEquals(cell.getArea(), JTS.to(diagram.getCells().get(s)).getArea(), 1e-6 * cell.getArea());
        }
    }

    // ---------------------------------------------------------------------------------------------------------

    // the cells are valid, and together cover the box without overlaps
    private static void assertPartition(VoronoiDiagram<C2D> diagram, double[] box) {
        double total = 0;
        List<org.locationtech.jts.geom.Geometry> cells = new ArrayList<>();
        for (Polygon<C2D> cell : diagram.getCells()) {
            org.locationtech.jts.geom.Geometry g = JTS.to(cell);
            assertTrue(g.isValid());
            total += g.getArea();
            cells.add(g);
        }
        double area = (box[2] - box[0]) * (box[3] - box[1]);
        assertEquals(area, total, 1e-9 * area);
        org.locationtech.jts.geom.Geometry union = new GeometryFactory().buildGeometry(cells).union();
        assertEquals(area, union.getArea(), 1e-9 * area);

        // adjacent cells share exactly the same vertices: a vertex inside the box is shared by at least 3 cells, a
        // vertex on a side of the box by at least 2 (a corner of the box may belong to a single cell)
        Map<List<Double>, Integer> occurrences = new HashMap<>();
        for (Polygon<C2D> cell : diagram.getCells()) {
            if (cell.isEmpty()) continue;
            PositionSequence<C2D> ring = cell.getExteriorRing().getPositions();
            for (int i = 0; i + 1 < ring.size(); i++) {
                occurrences.merge(Arrays.asList(ring.getPositionN(i).getX(), ring.getPositionN(i).getY()), 1, Integer::sum);
            }
        }
        for (Map.Entry<List<Double>, Integer> e : occurrences.entrySet()) {
            double x = e.getKey().get(0), y = e.getKey().get(1);
            int sides = (x == box[0] || x == box[2] ? 1 : 0) + (y == box[1] || y == box[3] ? 1 : 0);
            int expected = sides == 2 ? 1 : sides == 1 ? 2 : 3;
            assertTrue("vertex " + e.getKey() + " in " + e.getValue() + " cells", e.getValue() >= expected);
        }
    }

    // random points lie in the cell of their nearest site (unless they are nearly equidistant)
    private void assertNearestSite(VoronoiDiagram<C2D> diagram, double[] box) {
        List<C2D> sites = diagram.getSites();
        GeometryFactory factory = new GeometryFactory();
        for (int q = 0; q < 200; q++) {
            double x = box[0] + (box[2] - box[0]) * random.nextDouble(), y = box[1] + (box[3] - box[1]) * random.nextDouble();
            int best = -1;
            double d1 = Double.POSITIVE_INFINITY, d2 = Double.POSITIVE_INFINITY;
            for (int s = 0; s < sites.size(); s++) {
                double d = Math.hypot(sites.get(s).getX() - x, sites.get(s).getY() - y);
                if (d < d1) {
                    d2 = d1;
                    d1 = d;
                    best = s;
                } else if (d < d2) {
                    d2 = d;
                }
            }
            if (d2 - d1 < 1e-6 * (1 + d1)) continue;
            assertTrue(JTS.to(diagram.getCells().get(best)).covers(factory.createPoint(new Coordinate(x, y))));
        }
    }

    private static VoronoiDiagram<C2D> voronoi(List<C2D> sites, double[] box) {
        DelaunayTriangulation<C2D> t = DelaunayTriangulation.of(sites, PROJECTED_2D_METER);
        Envelope<C2D> clip = box == null ? null : new Envelope<>(box[0], box[1], box[2], box[3], PROJECTED_2D_METER);
        return VoronoiDiagram.of(t, clip);
    }
}
