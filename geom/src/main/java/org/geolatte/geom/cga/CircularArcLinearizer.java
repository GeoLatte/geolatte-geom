package org.geolatte.geom.cga;

import org.geolatte.geom.Position;
import org.geolatte.geom.PositionSequence;
import org.geolatte.geom.PositionSequenceBuilder;

import java.util.Arrays;

import static java.lang.Math.*;
import static org.geolatte.geom.PositionSequenceBuilders.variableSized;

/**
 * Linearizes arc segments defined by three consecutive {@code Positions}
 * <p/>
 * <p>The implementation guarantees that the specified Positions are among the returned, linearized Positions</p>
 * <p/>
 * Created by Karel Maesen, Geovise BVBA on 02/03/15.
 */
public class CircularArcLinearizer<P extends Position> {

    final private double threshold;
    final private P p0;
    final private P p1;
    final private P p2;
    final private Circle c;
    final private boolean isCounterClockwise;
    final private PositionSequenceBuilder<P> builder;

    //TODO threshold must become parameter of linearize/linearizeCircle!!
    // to that tolerance can be calculated as proportional to radius.
    public CircularArcLinearizer(P p0, P p1, P p2, double threshold) {
        if (p0 == null || p1 == null | p2 == null) {
            throw new IllegalArgumentException();
        }
        this.threshold = abs(threshold);
        this.p0 = p0;
        this.p1 = p1;
        this.p2 = p2;
        this.c = new Circle(p0, p1, p2, false);
        this.isCounterClockwise = NumericalMethods.isCounterClockwise(p0, p1, p2);
        this.builder = variableSized((Class<P>) p0.getClass());
    }

    public Circle getCircle() {
        return this.c;
    }

    public double getRadius() {
        return this.c.radius;
    }

    public PositionSequence<P> linearizeCircle() {
        double angleIncr = acos((c.radius - threshold) / c.radius);
        PositionSequenceBuilder<P> builder = variableSized((Class<P>) p0.getClass());
        double theta0 = angleInDirection(p0);
        double theta1 = angleInDirection(p1);
        builder.add(p0);
        AddPointsBetweenPolarCoordinates(theta0, theta1, p0, p1, angleIncr, builder);
        builder.add(p1);
        //TODO -- I'm not sure whether this actually works if the circle defined by the 3 positions is in clockwise direction
//        AddPointsBetweenPolarCoordinates(theta1, theta0 + 2 * Math.PI, p1, p0, angleIncr, builder, true);
        AddPointsBetweenPolarCoordinates(theta1, theta0, p1, p0, angleIncr, builder);
        builder.add(p0);
        return builder.toPositionSequence();
    }

    /**
     * Linearizes the arc segment defined by the three {@code Position}s specified in this instance's constructor.<br>
     * Compared to {@link #linearizeCircle()} this is not using normalized direction angels, as direction of arc 
     * will be lost in this case (Does not matter if working with a circle).
     *
     * @return a PositionSequence that approximates the arc segment
     */
    public PositionSequence<P> linearize() {
        double theta0 = angleInDirection(p0);
        double theta1 = angleInDirection(p1);
        double theta2 = angleInDirection(p2);

        //we linearize by incrementing start angle theta by and increment.
        // the following will always hold:
        // radius = radius*cos(increment) + error
        // we want error to be < threshold, so we can calculate increment
        // notice that argument to acos is very close to 1 (if threshold is small, as we assume here)
        // so angleIncrement is garuanteed to be positive and small
        double angleIncr = acos((c.radius - threshold) / c.radius);

        //TODO -- quick check if angles theta are closer together than angleIncr, then we don't need to
        // linearize

        //now we "walk" from theta, over theta1 to theta2 (or inversely)
        PositionSequenceBuilder<P> builder = variableSized((Class<P>) p0.getClass());
        if (isCounterClockwise) {
	        builder.add(p0);
	        AddPointsBetweenPolarCoordinates(theta0, theta1, p0, p1, angleIncr, builder);
	        builder.add(p1);
	        AddPointsBetweenPolarCoordinates(theta1, theta2, p1, p2, angleIncr, builder);
	        builder.add(p2);
	        return builder.toPositionSequence();
        } else {
        	// If clockwise, we have to do it reverse
        	builder.add(p2);
        	AddPointsBetweenPolarCoordinates(theta2, theta1, p2, p1, angleIncr, builder);
        	builder.add(p1);
        	AddPointsBetweenPolarCoordinates(theta1, theta0, p1, p0, angleIncr, builder);
        	builder.add(p0);
        	return builder.toPositionSequence().reverse();
        }

    }

    // Adds points strictly between theta and theta1, using the specified angle-increment
    // and interpolates the "higher dimensions" on the run
    private void AddPointsBetweenPolarCoordinates(double theta, double theta1, P p, P p1, double maxAngleIncr,
                                                  PositionSequenceBuilder<P> builder) {
        int dim = p.getCoordinateDimension();

        
        // If working with arcs, we have to normalize theta1 that it always bigger than theta
        // This is needed for computing steps only and only if arcs have not been normalized yet.
        double normalizedTheta1 = theta1;
	    while(normalizedTheta1 < theta) {
	       	normalizedTheta1 += 2.0 * Math.PI;
	    }
        //first a number of steps and angleIncrement such that we go in equal increment steps from theta to theta1
        int steps = (int) Math.ceil(Math.abs(normalizedTheta1 - theta) / maxAngleIncr);
        double angleIncr = Math.abs(normalizedTheta1 - theta) / (double) steps;
        
        //determine increments for Z and M dimensions:
        double[] incr = new double[dim - 2];
        for (int i = 0; i < incr.length; i++) {
            incr[i] = (p1.getCoordinate(2 + i) - p.getCoordinate(2 + i)) / steps;
        }

        //now find direction (but only if working with normalized angles).
        double sign = 1.0;
        
        // we initialize the higher dimensions
        double[] buf = new double[dim];
        for (int i = 0; i < incr.length; i++) {
            buf[2 + i] = p.getCoordinate(2 + i);
        }
        double a = theta;
        for (int idx =0 ; idx < steps-1 ; idx++) {
            a = a + sign * angleIncr;
            //calculate x,y positions
            buf[0] = c.x + c.radius * cos(a);
            buf[1] = c.y + c.radius * sin(a);

            //and interpolate
            for (int i = 0; i < incr.length; i++) {
                buf[2 + i] = buf[2 + i] + incr[i];
            }
            builder.add(buf);
        }
    }

    //atan2 give the angular coordinate theta of the polar coordinates (r, theta)
    //the angular coordinate ranges between -PI and PI.
    private double angleInDirection(Position p) {
        double x = (p.getCoordinate(0) - c.x);
        double y = (p.getCoordinate(1) - c.y);
        return atan2(y, x);
    }


    
}
