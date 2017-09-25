package org.geolatte.geom.cga;

import org.geolatte.geom.C2D;
import org.junit.Test;
import static java.lang.Math.*;
import static org.junit.Assert.assertEquals;

/**
 * Created by Karel Maesen, Geovise BVBA on 01/03/15.
 */
public class CircleTest {

    double radius = 8;
    double centerX = 3;
    double centerY = 4;

    double startAngle = Math.PI / 4;
    C2D p0 = new C2D(centerX + radius * cos(startAngle), centerY + radius*sin(startAngle));
    C2D p1 = new C2D(centerX + radius * cos(startAngle+0.3), centerY + radius*sin(startAngle+0.3));
    C2D p2 = new C2D(centerX + radius * cos(startAngle+1.0), centerY + radius*sin(startAngle+1.0));


    @Test
    public void testValidCircle() {
        Circle c = new Circle(p0, p1, p2);
        double errBound = Math.ulp(1.0d)*100;
        assertEquals(radius, c.radius, errBound);
        assertEquals(centerX, c.x, errBound);
        assertEquals(centerY, c.y, errBound);
    }

    @Test
    public void testOrderOfPointsDoesNotMatter() {
        Circle c = new Circle(p0, p2, p1);
        double errBound = Math.ulp(1.0d)*100;
        assertEquals(c.x, centerX, errBound);
        assertEquals(c.y, centerY, errBound);
        assertEquals(c.radius, radius, errBound);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testCollinearPointsThrowException() {
        C2D p0 = new C2D(0, 0);
        C2D p1 = new C2D(1, 1);
        C2D p2 = new C2D(2, 2);
        Circle c = new Circle(p0, p1, p2);
    }

    @Test
    public void testRobustness() {
        C2D p0 = new C2D(0, 0);
        C2D p1 = new C2D(1, 1 + 2*Math.ulp(1d)); //nearly collinear points
        C2D p2 = new C2D(2, 2);
        new Circle(p0, p1, p2);
    }

}
