package org.geolatte.geom.crs.trans;

import org.geolatte.geom.C2D;
import org.geolatte.geom.Envelope;
import org.geolatte.geom.G2D;
import org.junit.Test;

import static org.geolatte.geom.AssertHelpers.assertEquals;
import static org.geolatte.geom.builder.DSL.c;
import static org.geolatte.geom.builder.DSL.g;
import static org.geolatte.geom.crs.CoordinateReferenceSystems.WEB_MERCATOR;
import static org.geolatte.geom.crs.CoordinateReferenceSystems.WGS84;

/**
 * Created by Karel Maesen, Geovise BVBA on 2019-03-28.
 */
public class TestEnvelopeTransform {

    private EnvelopeTransform<G2D, C2D> transform = new EnvelopeTransform<>(TransformOperations.from(WGS84, WEB_MERCATOR));

    @Test
    public void testEnvelopeTransformForward(){
        Envelope<G2D> env = new Envelope<>(g(5, 50), g(6, 51), WGS84);
        Envelope<C2D> exp = new Envelope<> (c(556597.4539663679, 6446275.841017158),c(667916.9447596414, 6621293.722740163), WEB_MERCATOR);
        assertEquals(exp, transform.forward(env), 0.00001);
    }

    @Test
    public void testEnvelopeTransformReverse(){
        Envelope<C2D> env = new Envelope<> (c(556597.4539663679, 6446275.841017158),c(667916.9447596414, 6621293.722740163), WEB_MERCATOR);
        Envelope<G2D> exp = new Envelope<>(g(5, 50), g(6, 51), WGS84);
        assertEquals(exp, transform.reverse(env), 0.00001);
    }
}
