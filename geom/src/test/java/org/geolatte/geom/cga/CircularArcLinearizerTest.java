package org.geolatte.geom.cga;

import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

import org.geolatte.geom.C2D;
import org.geolatte.geom.LineString;
import org.geolatte.geom.PositionSequence;
import org.geolatte.geom.crs.CoordinateReferenceSystem;
import org.geolatte.geom.crs.CoordinateReferenceSystems;
import org.geolatte.geom.crs.LinearUnit;
import org.geolatte.geom.jts.JTS;
import org.geolatte.geom.support.generator.CircleGenerator;
import org.geolatte.geom.support.generator.Generator;
import org.geolatte.geom.support.generator.StdGenerators;
import org.junit.Test;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;

/**
 * Created by Karel Maesen, Geovise BVBA on 03/03/15.
 */
public class CircularArcLinearizerTest {

	private static GeometryFactory geometryFactory = new GeometryFactory();
	/* Line from middle to right. Used to detect if linearized arcs intersection. */
	private static Geometry LINE_RIGHT = geometryFactory.createLineString(new Coordinate[] {new Coordinate(20.0, 20.0), new Coordinate(40.0, 20.0)});
	/* Line from middle to left. Used to detect if linearized arcs intersection. */
	private static Geometry LINE_LEFT = geometryFactory.createLineString(new Coordinate[] {new Coordinate(20.0, 20.0), new Coordinate(0.0, 20.0)});
	/* Line from middle to up. Used to detect if linearized arcs intersection. */
	private static Geometry LINE_UP = geometryFactory.createLineString(new Coordinate[] {new Coordinate(20.0, 20.0), new Coordinate(20.0, 40.0)});
	/* Line from middle to down. Used to detect if linearized arcs intersection. */
	private static Geometry LINE_DOWN = geometryFactory.createLineString(new Coordinate[] {new Coordinate(20.0, 20.0), new Coordinate(20.0, 0.0)});
	
	/* Point on arc in first quadrant seen counter-clockwise. */
	private static C2D POINT_QUADRANT_1 = new C2D(27.0711, 27.0711);
	/* Point on arc in second quadrant seen counter-clockwise. */
	private static C2D POINT_QUADRANT_2 = new C2D(12.9289, 27.0711);
	/* Point on arc in third quadrant seen counter-clockwise. */
	private static C2D POINT_QUADRANT_3 = new C2D(12.9289, 12.9289);
	/* Point on arc in fourth quadrant seen counter-clockwise. */
	private static C2D POINT_QUADRANT_4 = new C2D(27.0711, 12.9289);
	
	private static final CoordinateReferenceSystem<C2D> coordinateRefSystem = CoordinateReferenceSystems.addVerticalSystem(CoordinateReferenceSystems.PROJECTED_2D_METER, C2D.class,
	            LinearUnit.METER);
	
    private static final int SAMPLE_SIZE = 100;
    private static final double EPSILON = 0.0001d;

    CircleGenerator cgen = new CircleGenerator(-100, 100, 100);

    Circle c;
    ThreePointSample p;
    private StringBuilder errormsg;

    @Test
    public void check(){
        for(int i = 0; i < SAMPLE_SIZE; i++) {
            errormsg = new StringBuilder();
            c = cgen.sample();
            p = new ThreePositionsOnCircleGenerator(c).sample();
            CircularArcLinearizer<C2D> linearizer = new CircularArcLinearizer<C2D>(p.positions[0], p.positions[1], p.positions[2], 0.0001);
            assertTrue(equals(c, linearizer.getCircle()));
            PositionSequence<C2D> sequence = linearizer.linearize();
			verify(sequence,p.positions[0],p.positions[2]);
            sequence = linearizer.linearizeCircle();
            verifyCircle(sequence, p.positions[0]);
            verify(sequence,p.positions[0],p.positions[0]);
            // Do opposite direction
            linearizer = new CircularArcLinearizer<C2D>(p.positions[2], p.positions[1], p.positions[0], 0.0001);
            assertTrue(equals(c, linearizer.getCircle()));
            sequence = linearizer.linearize();
			verify(sequence,p.positions[2],p.positions[0]);
            sequence = linearizer.linearizeCircle();
            verifyCircle(sequence, p.positions[2]);
            verify(sequence,p.positions[2],p.positions[2]);
        }
    }

    private boolean equals(Circle c, Circle circle) {
        return (Math.abs(c.x - circle.x) < EPSILON) && (Math.abs(c.y - circle.y) < EPSILON) && (Math.abs(c.radius -
                circle.radius) < EPSILON);
    }


    //TODO -- add tests for higher dimensions.
    

    private void verifyCircle(PositionSequence<C2D> seq, C2D start){
        C2D prev = null;
        double angle = 0;
        for(C2D co : seq){

            if (prev == null) {
                assertEquals(co, start); // first position in sequence exactly equals p0
                prev = co;
                continue;
            }

            //check that co is on the circle
            verifyOnCircle(co);
            angle += getAngle(prev, co);
            prev = co;
        }
        assertEquals(prev, start); //is same as first
        assertEquals(String.format("Difference is: %f (on Circle %s)", 2*Math.PI - angle, c.toString())
                + errormsg.toString(), 2*Math.PI, angle, 0.0001);
    }

    private void verify(PositionSequence<C2D> seq, C2D startPosition,C2D endPosition){

        C2D prev = null;
        int dir = 0; // negative for clockwise, positive for counterclockwise, zero for not-known
        for(C2D co : seq){

            if (prev == null) {
                assertEquals(co, startPosition); // first position in sequence exactly equals p0
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
        assertEquals(prev, endPosition); // last position exactly equals p2;
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

    
    /*
     * Tests to verify arc resolution is using a valid direction.
     * 
     * Decided to do this by intersection of linearized arcs. Separated it into four quadrants.
     * 
     *          |
     *      Q2  |  Q1
     *          |
     *   -------+-------
     *          |
     *      Q3  |  Q4
     *          |
     * 
     */
    public static Geometry linearize(C2D start,C2D pointOnArc,C2D end) {
    	PositionSequence<C2D> result = new CircularArcLinearizer<>(start, pointOnArc, end, 0.0001).linearize();
    	LineString<C2D> lineString = new LineString<>(result,coordinateRefSystem);
    	return JTS.to(lineString);
    }
    
    @Test
    public void testArcQ1ToQ4Clockwise() throws Exception {
    	Geometry result = linearize(POINT_QUADRANT_1, new C2D(30.0,20.0), POINT_QUADRANT_4);
    	assertTrue(result.intersects(LINE_RIGHT));
    	assertFalse(result.intersects(LINE_LEFT));
    	assertFalse(result.intersects(LINE_UP));
    	assertFalse(result.intersects(LINE_DOWN));
    }
    
    @Test
    public void testArcQ1ToQ4CounterClockwise() throws Exception {
    	Geometry result = linearize(POINT_QUADRANT_4, new C2D(30.0,20.0), POINT_QUADRANT_1);
    	assertTrue(result.intersects(LINE_RIGHT));
    	assertFalse(result.intersects(LINE_LEFT));
    	assertFalse(result.intersects(LINE_UP));
    	assertFalse(result.intersects(LINE_DOWN));
    }
    
    @Test
    public void testArcQ2ToQ4Clockwise() throws Exception {
    	Geometry result = linearize(POINT_QUADRANT_2, POINT_QUADRANT_1, POINT_QUADRANT_4);
    	assertTrue(result.intersects(LINE_RIGHT));
    	assertFalse(result.intersects(LINE_LEFT));
    	assertTrue(result.intersects(LINE_UP));
    	assertFalse(result.intersects(LINE_DOWN));
    }
    
    @Test
    public void testArcQ2ToQ4CounterClockwise() throws Exception {
    	Geometry result = linearize(POINT_QUADRANT_4, POINT_QUADRANT_1, POINT_QUADRANT_2);
    	assertTrue(result.intersects(LINE_RIGHT));
    	assertFalse(result.intersects(LINE_LEFT));
    	assertTrue(result.intersects(LINE_UP));
    	assertFalse(result.intersects(LINE_DOWN));
    	
    }
    
    @Test
    public void testArcQ3ToQ4Clockwise() throws Exception {
    	Geometry result = linearize(POINT_QUADRANT_3, POINT_QUADRANT_1, POINT_QUADRANT_4);
    	
    	assertTrue(result.intersects(LINE_RIGHT));
    	assertTrue(result.intersects(LINE_LEFT));
    	assertTrue(result.intersects(LINE_UP));
    	assertFalse(result.intersects(LINE_DOWN));
    }
    
    @Test
    public void testArcClockwise() throws Exception {
    	C2D p0 = new C2D(29.8128,21.9259 );
    	C2D p1 = new C2D(28.6305,14.9489 );
    	C2D p2 = new C2D(23.6725,10.6988 );
    	
    	PositionSequence<C2D> result = new CircularArcLinearizer<>(p0, p1, p2, 0.0001).linearize();
    	LineString<C2D> lineString = new LineString<>(result,coordinateRefSystem);
    	Geometry jtsGeometry = JTS.to(lineString);
    	
    	org.locationtech.jts.geom.LineString intersectGeometry = new GeometryFactory().createLineString(new Coordinate[] {new Coordinate(20.0, 20.0), new Coordinate(40.0, 20.0)});
    	assertTrue(jtsGeometry.intersects(intersectGeometry));
    }
    
    @Test
    public void testArcCounterClockwise() throws Exception {
    	C2D p0 = new C2D(29.8128,21.9259 );
    	C2D p1 = new C2D(28.6305,14.9489 );
    	C2D p2 = new C2D(23.6725,10.6988 );
    	
    	PositionSequence<C2D> result = new CircularArcLinearizer<>(p2, p1, p0, 0.0001).linearize();
    	LineString<C2D> lineString = new LineString<>(result,coordinateRefSystem);
    	Geometry jtsGeometry = JTS.to(lineString);
    	
    	org.locationtech.jts.geom.LineString intersectGeometry = new GeometryFactory().createLineString(new Coordinate[] {new Coordinate(20.0, 20.0), new Coordinate(40.0, 20.0)});
    	assertTrue(jtsGeometry.intersects(intersectGeometry));
    }
}


class ThreePositionsOnCircleGenerator implements Generator<ThreePointSample> {

    final private Circle circle;
    final private Generator<Boolean> isCounterClockwiseGenerator = StdGenerators.bernouilly(0.5);

    ThreePositionsOnCircleGenerator(Circle c){
        circle = c;
    }

    @Override
    public ThreePointSample sample() {
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

        return new ThreePointSample(isCCW, theta, positions);

    }

    private C2D toCartesian(double theta0) {
        return new C2D(circle.x + circle.radius * cos(theta0), circle.y + circle.radius * sin(theta0));
    }


}

class ThreePointSample{
    boolean isCCW;
    double[] thetas;
    C2D[] positions;
    ThreePointSample(boolean isCCW, double[] thetas, C2D[] positions){
        this.isCCW = isCCW;
        this.thetas = thetas;
        this.positions = positions;
    }
}