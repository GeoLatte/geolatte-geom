package org.geolatte.geom.cga;

import org.geolatte.geom.C2D;
import org.geolatte.geom.PositionSequenceBuilder;
import org.geolatte.geom.PositionSequenceBuilders;
import org.geolatte.geom.cga.TopologyPreservingSimplification.Component;
import org.geolatte.geom.cga.TopologyPreservingSimplification.Segment;
import org.geolatte.geom.cga.TopologyPreservingSimplification.SegmentIndex;
import org.junit.Test;
import org.locationtech.jts.geom.Envelope;

import java.util.*;

import static org.junit.Assert.assertEquals;

/**
 * Tests the spatial index of {@link TopologyPreservingSimplification} against a brute force search.
 */
public class SegmentIndexTest {

    private final Random random = new Random(3L);

    @Test
    public void queriesMatchBruteForce() {
        for (double scale : new double[]{1e-300, 1e-3, 1, 1e5, 1e300}) {
            for (double offset : new double[]{0, 1e6 * scale}) {
                check(scale, offset);
            }
        }
    }

    private void check(double scale, double offset) {
        // short segments, long segments, and points
        int n = 2000;
        PositionSequenceBuilder<C2D> builder = PositionSequenceBuilders.fixedSized(n, C2D.class);
        for (int i = 0; i < n; i++) {
            builder.add(new C2D(offset + random.nextDouble() * 100 * scale, offset + random.nextDouble() * 100 * scale));
        }
        Component c = new Component(builder.toPositionSequence(), false, false);
        List<Segment> segments = new ArrayList<>();
        for (int i = 0; i + 1 < n; i++) {
            int j = random.nextInt(10) == 0 ? random.nextInt(n) : i + 1;   // some long segments
            if (random.nextInt(20) == 0) j = i;                             // and some points
            segments.add(new Segment(c, Math.min(i, j), Math.max(i, j)));
        }
        Envelope extent = new Envelope();
        for (Segment s : segments) extent.expandToInclude(s.envelope);
        SegmentIndex index = new SegmentIndex(extent, segments.size() / 3);
        for (Segment s : segments) index.insert(s);
        // remove some, and insert new ones
        Collections.shuffle(segments, random);
        List<Segment> present = new ArrayList<>(segments.subList(0, segments.size() / 2));
        for (Segment s : segments.subList(segments.size() / 2, segments.size())) index.remove(s);
        for (int i = 0; i < 100; i++) {
            Segment s = new Segment(c, random.nextInt(n / 2), n / 2 + random.nextInt(n / 2));
            index.insert(s);
            present.add(s);
        }

        for (int q = 0; q < 300; q++) {
            double x = offset + random.nextDouble() * 120 * scale - 10 * scale;
            double y = offset + random.nextDouble() * 120 * scale - 10 * scale;
            double size = random.nextInt(4) == 0 ? 0 : Math.pow(10, random.nextDouble() * 3 - 1) * scale;
            Envelope envelope = new Envelope(x, x + size, y, y + size * random.nextDouble());
            Set<Segment> expected = Collections.newSetFromMap(new IdentityHashMap<>());
            for (Segment s : present) {
                if (s.envelope.intersects(envelope)) expected.add(s);
            }
            List<Segment> found = new ArrayList<>();
            index.query(envelope, found::add);
            assertEquals(expected.size(), found.size());   // no duplicates
            Set<Segment> foundSet = Collections.newSetFromMap(new IdentityHashMap<>());
            foundSet.addAll(found);
            assertEquals(expected, foundSet);
        }
    }
}
