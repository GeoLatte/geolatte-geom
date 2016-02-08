package org.geolatte.geom;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by Karel Maesen, Geovise BVBA on 06/02/16.
 */
public class NestedPositionSequenceTest {

    @Test
    public void testReverse() {
        PositionSequenceBuilder<C2D> builder = PositionSequenceBuilders.fixedSized(2, C2D.class);
        PositionSequence<C2D> p = builder.add(1, 0).add(2, 3).toPositionSequence();
        PositionSequenceBuilder<C2D> builder2 = PositionSequenceBuilders.fixedSized(3, C2D.class);
        PositionSequence<C2D> p2 = builder2.add(5,6).add(7,8).add(10,11).toPositionSequence();

        NestedPositionSequence nps = new NestedPositionSequence(new PositionSequence[]{p, p2});

        PositionSequenceBuilder<C2D> builder3 = PositionSequenceBuilders.fixedSized(2, C2D.class);
        PositionSequence<C2D> rp = builder3.add(2, 3).add(1, 0).toPositionSequence();
        PositionSequenceBuilder<C2D> builder4 = PositionSequenceBuilders.fixedSized(3, C2D.class);
        PositionSequence<C2D> rp2 = builder4.add(10,11).add(7,8).add(5,6).toPositionSequence();

        NestedPositionSequence rnps = new NestedPositionSequence(new PositionSequence[]{rp2, rp});

        assertEquals(rnps, nps.reverse());
    }
}
