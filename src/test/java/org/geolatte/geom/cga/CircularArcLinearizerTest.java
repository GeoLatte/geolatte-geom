package org.geolatte.geom.cga;

import org.geolatte.geom.C2D;
import org.geolatte.geom.Position;
import org.geolatte.geom.PositionSequence;
import org.geolatte.geom.support.generator.CircleGenerator;
import org.geolatte.geom.support.generator.Generator;
import org.geolatte.geom.support.generator.StdGenerators;
import org.junit.Test;

import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static org.junit.Assert.assertEquals;

/**
 * Created by Karel Maesen, Geovise BVBA on 03/03/15.
 */
public class CircularArcLinearizerTest {

    private static final int SAMPLE_SIZE = 1000;
    private static final double EPSILON = 0.0001d;

    CircleGenerator cgen = new CircleGenerator(-10, 10, 10);

    Circle c;
    C2D[] p;
    private StringBuilder errormsg;

    @Test
    public void check(){
        for(int i = 0; i < SAMPLE_SIZE; i++) {
            errormsg = new StringBuilder();
            c = cgen.sample();
            p = new ThreePositionsOnCircleGenerator(c).sample();
            CircularArcLinearizer<C2D> linearizer = new CircularArcLinearizer<C2D>(p[0], p[1], p[2], 0.0001);
            PositionSequence<C2D> sequence = linearizer.linearize();
            verify(sequence);
            sequence = linearizer.linearizeCircle();
            verifyCircle(sequence);
        }
    }

    private void verifyCircle(PositionSequence<C2D> seq){
        C2D prev = null;
        double angle = 0;
        for(C2D co : seq){

            if (prev == null) {
                assertEquals(co, p[0]); // first position in sequence exactly equals p0
                prev = co;
                continue;
            }

            //check that co is on the circle
            verifyOnCircle(co);
            angle += getAngle(prev, co);
            prev = co;

        }
        assertEquals(prev, p[0]); //is same as first
        assertEquals(String.format("Difference is: %f (on Circle %s)", 2*Math.PI - angle, c.toString())
                + errormsg.toString(), 2*Math.PI, angle, 0.0001);
    }

    private void verify(PositionSequence<C2D> seq){

        C2D prev = null;
        int dir = 0; // negative for clockwise, positive for counterclockwise, zero for not-known
        for(C2D co : seq){

            if (prev == null) {
                assertEquals(co, p[0]); // first position in sequence exactly equals p0
                prev = co;
                continue;
            }

            //check that co is on the circle
            verifyOnCircle(co);

            if( dir == 0 ) {
                dir = getDirection(prev, co);
            } else {
                assertEquals(dir, getDirection(prev, co));
            }

            prev = co;

        }
        assertEquals(prev, p[2]); // last position exactly equals p2;
    }

    private void verifyOnCircle(C2D co) {
        double d = Math.hypot(co.getX() - c.x, co.getY() - c.y);
        assertEquals(c.radius, d, c.radius*EPSILON);
    }

    private int getDirection(C2D prev, C2D co) {
        return (int) Math.signum(NumericalMethods.crossProduct(prev.getX() -c.x, co.getX() -c.x, prev.getY() -c.y, co.getY() -c.y));
    }

    private double getAngle(C2D prev, C2D co) {
        double cp =  NumericalMethods.crossProduct(prev.getX() -c.x, co.getX() -c.x, prev.getY() -c.y, co.getY() -c.y);
        double theta = Math.asin(cp / Math.pow(c.radius, 2));
        errormsg.append("\n" + "Theta: " + theta + " for points:" + co.getX() + "," + co.getY());
        return theta;
    }

}


class ThreePositionsOnCircleGenerator implements Generator<C2D[]> {

    final private Circle circle;
    final private Generator<Boolean> isCounterClockwiseGenerator = StdGenerators.bernouilly(0.5);

    ThreePositionsOnCircleGenerator(Circle c){
        circle = c;
    }

    @Override
    public C2D[] sample() {
        C2D[] positions = new C2D[3];
        double[] theta = new double[3];
        boolean isCCW = isCounterClockwiseGenerator.sample();
        if (isCCW) {
            //counterclockwise
            theta[0] = StdGenerators.between(0, 2 * Math.PI).sample();
            theta[1] = StdGenerators.between(theta[0], 2*Math.PI).sample();
            theta[2] = StdGenerators.between(theta[1], 2*Math.PI).sample();
        } else {
            theta[0] = StdGenerators.between(0, - 2 * Math.PI).sample();
            theta[1] = StdGenerators.between(theta[0], - 2*Math.PI).sample();
            theta[2] = StdGenerators.between(theta[1], - 2*Math.PI).sample();
        }
        for( int i = 0 ; i < positions.length; i++) {
            positions[i] = toCartesian(theta[i]);
        }

        // verify that we actually do return as expected.
        assert ((isCCW && NumericalMethods.isCounterClockwise(positions[0], positions[1], positions[2])) ||
                (!isCCW && !NumericalMethods.isCounterClockwise(positions[0], positions[1], positions[2])) );

        return positions;

    }

    private C2D toCartesian(double theta0) {
        return new C2D(circle.x + circle.radius * cos(theta0), circle.y + circle.radius * sin(theta0));
    }
}