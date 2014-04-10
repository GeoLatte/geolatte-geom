package org.geolatte.geom.jts;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateSequence;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.impl.CoordinateArraySequence;
import junit.framework.Assert;
import org.geolatte.geom.LineString;
import org.geolatte.geom.Point;
import org.junit.Test;

/**
 *
 */
public class JtsConversionCanHandleMValuesTest {

    @Test
    public void testHandlesMValues() {

        CoordinateSequence sequence = new MCoordinateSequence(new Coordinate[]{new MCoordinate(1.0,2.0,3.0,4.0)});

        Point p = (Point)JTS.from(new GeometryFactory().createPoint(sequence));

        Assert.assertEquals(1.0, p.getX(), 0.0001);
        Assert.assertEquals(2.0, p.getY(), 0.0001);
        Assert.assertEquals(3.0, p.getZ(), 0.0001);
        Assert.assertEquals(4.0, p.getM(), 0.0001);
    }

    private static class MCoordinateSequence extends CoordinateArraySequence {

        public MCoordinateSequence(Coordinate[] coordinates) {
            super(coordinates);
        }

        @Override
        public double getOrdinate(int index, int ordinateIndex) {
            return getCoordinate(index).getOrdinate(ordinateIndex);
        }
    }


    private static class MCoordinate extends Coordinate {

        private final double m;

        private MCoordinate(double x, double y, double z, double m) {
            super(x, y, z);
            this.m = m;
        }

        @Override
        public double getOrdinate(int ordinateIndex) {
            if(ordinateIndex == CoordinateSequence.M) {
                return m;
            } else {
                return super.getOrdinate(ordinateIndex);
            }
        }
    }
}
