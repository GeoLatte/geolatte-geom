package org.geolatte.geom.cga;

import org.geolatte.geom.Position;

import static org.geolatte.geom.cga.NumericalMethods.*;

/**
 * A Simple Circle value class
 *
 * Created by Karel Maesen, Geovise BVBA on 01/03/15.
 */
public class Circle {

    final double x;
    final double y;
    final double radius;

    public Circle(double x, double y, double radius){
        this.x = x;
        this.y = y;
        this.radius = radius;
    }

    public Circle(Position p0, Position p1, Position p2) {
        this(p0, p1, p2, true);
    }

    /**
     * Creates a Circle through three {@code Position}s
     * @param p0 first {@code Position}
     * @param p1 second {@code Position}
     * @param p2 third {@code Position}
     * @param doCollinearityCheck if true, then first verify that points are not collinear
     * @throws IllegalArgumentException if {@code Position}s are collinear, and collinearity check if enabled
     */
    public Circle(Position p0, Position p1, Position p2, boolean doCollinearityCheck){
        //the collinear check is the most expensive part in this calculation.
        if (doCollinearityCheck && collinear(p0, p1, p2)){
            throw new IllegalArgumentException("Positions are collinear in 2D");
        }
        //note we don't check for counterclockwise direction,
        //because sign inversions in numerator and denominator cancel out
        double X1 = p1.getCoordinate(0) - p0.getCoordinate(0);
        double Y1 = p1.getCoordinate(1) - p0.getCoordinate(1);
        double X2 = p2.getCoordinate(0) - p0.getCoordinate(0);
        double Y2 = p2.getCoordinate(1) - p0.getCoordinate(1);

        double areaM = 2 * determinant(X1, Y1, X2, Y2); // 4 times area
        double sqrL10 = Math.pow(X1, 2) + Math.pow(Y1, 2);
        double sqrL20 = Math.pow(X2, 2) + Math.pow(Y2, 2);

        this.x = p0.getCoordinate(0) + (Y2 * sqrL10 - Y1 * sqrL20) / areaM;
        this.y = p0.getCoordinate(1) + (X1*sqrL20 - X2*sqrL10)  / areaM;

        this.radius = Math.hypot(x - p0.getCoordinate(0), y - p0.getCoordinate(1));

    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Circle circle = (Circle) o;

        if (Double.compare(circle.radius, radius) != 0) return false;
        if (Double.compare(circle.x, x) != 0) return false;
        if (Double.compare(circle.y, y) != 0) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        temp = Double.doubleToLongBits(x);
        result = (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(y);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(radius);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }

    @Override
    public String toString() {
        return "Circle{" +
                "x=" + x +
                ", y=" + y +
                ", radius=" + radius +
                '}';
    }
}
