package org.geolatte.geom;

import junit.framework.Assert;
import org.geolatte.geom.codec.Wkt;
import org.junit.Test;

public class LocateAlongTest {

    @Test
    public void locateAlongRightBeforeEnd() {
        Geometry geom = Wkt.newDecoder().decode("SRID=31370;MULTILINESTRINGM((0 0 0,4000 0 4),(6000 0 6,10000 0 10))");

        Geometry located = geom.locateAlong(3.999);
        Assert.assertNotNull(located);
    }

    @Test
    public void locateAlongAtTheEnd() {
        // be careful, this one only fails if jvm level assertions are enabled (-ea flag)
        Geometry geom = Wkt.newDecoder().decode("SRID=31370;MULTILINESTRINGM((0 0 0,4000 0 4),(6000 0 6,10000 0 10))");

        Geometry located = geom.locateAlong(4.0);
        Assert.assertNotNull(located);
    }

    @Test
    public void locateAlongAtTheEndOnlyOneSegment() {
        Geometry geom = Wkt.newDecoder().decode("SRID=31370;MULTILINESTRINGM((0 0 0,4000 0 4))");

        Geometry located = geom.locateAlong(4.0);
        Assert.assertNotNull(located);
    }

}
