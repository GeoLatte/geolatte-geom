package org.geolatte.geom.cga;

import org.geolatte.geom.Position;
import org.geolatte.geom.PositionSequence;
import org.geolatte.geom.PositionSequenceBuilder;

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

    public PositionSequence<P> linearize() {
        double x0 = p0.getCoordinate(0);
        double y0 = p0.getCoordinate(1);
        double x1 = p1.getCoordinate(0);
        double y1 = p1.getCoordinate(1);
        double x2 = p2.getCoordinate(0);
        double y2 = p2.getCoordinate(1);

        //TODO -- add quick check to see if positions need to linearized (e.g. when very close to each other)


        //translate coordinate system to that origin is at the center of circle c
        double xd0 = (x0 - c.x);
        double yd0 = (y0 - c.y);

        double xd1 = (x1 - c.x);
        double yd1 = (y1 - c.y);

        double xd2 = (x2 - c.x);
        double yd2 = (y2 - c.y);


        double theta0 = angleInDirection(xd0, yd0);
        double theta1 = angleInDirection(xd1, yd1);
        double theta2 = angleInDirection(xd2, yd2);

        //we linearize by incrementing start angle theta by and increment.
        // the following will always hold:
        // radius = radius*cos(increment) + error
        // we want error to be < threshold, so we can calculate increment
        // notice that argument to acos is very close to 1 (if threshold is small, as we assume here)
        // so angleIncrement is garuanteed to be positive and small
        double angleIncr = acos((c.radius - threshold) / c.radius);


        //now we "walk" from theta, over theta1 to theta2 (or inversely)
        PositionSequenceBuilder<P> builder = variableSized((Class<P>) p0.getClass());
        builder.add(p0);
        AddPointsBetweenPolarCoordinates(theta0, theta1, angleIncr, builder);
        builder.add(p1);
        AddPointsBetweenPolarCoordinates(theta1, theta2, angleIncr, builder);
        builder.add(p2);

        return builder.toPositionSequence();

    }

    // Adds points strictly between theta and theta1, using the specified angle-increment
    private void AddPointsBetweenPolarCoordinates(double theta, double theta1, double angleIncr,
                                                  PositionSequenceBuilder<P> builder) {
        //first find direction:
        double sign = theta < theta1 ? 1d : -1d;
        double a = theta + sign*angleIncr;
        while (sign * a < sign * theta1) {
            builder.add(c.x + c.radius * cos(a), c.y + c.radius * sin(a));
            a = a + sign * angleIncr;
        }
    }

    //atan2 give the angular coordinate theta of the polar coordinates (r, theta)
    //the angular coordinate ranges between -PI and PI, but to define the circular segment, we
    //should normalize to [0 ,2*PI] if counterclockwise, and [0, -2*PI] if clockwise
    private double angleInDirection(double x, double y) {
        double theta = atan2(y, x);
        if (isCounterClockwise) {
            return (theta >= 0) ? theta : 2*Math.PI + theta;
        } else {
            return (theta <= 0) ? theta : theta - 2*Math.PI;
        }
    }

}
