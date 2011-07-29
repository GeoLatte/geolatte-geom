/*
 * This file is part of the GeoLatte project.
 *
 *     GeoLatte is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Lesser General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     GeoLatte is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Lesser General Public License for more details.
 *
 *     You should have received a copy of the GNU Lesser General Public License
 *     along with GeoLatte.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright (C) 2010 - 2011 and Ownership of code is shared by:
 * Qmino bvba - Romeinsestraat 18 - 3001 Heverlee  (http://www.qmino.com)
 * Geovise bvba - Generaal Eisenhowerlei 9 - 2140 Antwerpen (http://www.geovise.com)
 */

package org.geolatte.geom;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateSequence;
import com.vividsolutions.jts.geom.Envelope;
import org.geolatte.geom.crs.CoordinateSystem;

import java.util.Iterator;

/**
 * @author Karel Maesen, Geovise BVBA, 2011
 */
abstract class AbstractPointSequence implements PointSequence, CoordinateSequence {

    private final CoordinateSystem coordinateSystem;

    AbstractPointSequence(CoordinateSystem coordinateSystem) {
        this.coordinateSystem = coordinateSystem;
    }

    public static DimensionalCoordinate[] toCoordinateArray(AbstractPointSequence sequence) {
        DimensionalCoordinate[] coordinates = new DimensionalCoordinate[sequence.size()];
        for (int i = 0; i < sequence.size(); i++){
            coordinates[i] = new DimensionalCoordinate(sequence.getCoordinateSystem());
            sequence.getCoordinate(i, coordinates[i]);
        }
        return coordinates;
    }

    public static CoordinateSequence fromCoordinateArray(Coordinate[] coordinates, CoordinateSystem dim) {
        FixedSizePointSequenceBuilder builder = new FixedSizePointSequenceBuilder(coordinates.length, dim);
        double[] ordinates = new double[dim.getCoordinateDimension()];
        for (Coordinate co : coordinates) {
            copy(co,ordinates, dim);
            builder.add(ordinates);
        }
        return (CoordinateSequence)builder.toPointSequence();
    }

    private static void copy(Coordinate co, double[] ordinates, CoordinateSystem coordinateSystem) {
        ordinates[coordinateSystem.getIndex(CoordinateAccessor.X)] = co.x;
        ordinates[coordinateSystem.getIndex(CoordinateAccessor.Y)] = co.y;
        if (coordinateSystem.is3D()) ordinates[coordinateSystem.getIndex(CoordinateAccessor.Z)] = co.z;
        if (coordinateSystem.isMeasured()) {
            ordinates[coordinateSystem.getIndex(CoordinateAccessor.M)] = (co instanceof DimensionalCoordinate) ?
                   ((DimensionalCoordinate)co).m :
                    Double.NaN;
        }
    }

    @Override
    public CoordinateSystem getCoordinateSystem() {
        return this.coordinateSystem;
    }

    @Override
    public boolean is3D(){
        return this.coordinateSystem.is3D();
    }

    @Override
    public boolean isMeasured(){
        return this.coordinateSystem.isMeasured();
    }

    /*
   This overrides the CoordinateSequence notion of dimension
    */
    @Override
    public int getDimension() {
        return getCoordinateDimension();
    }

    public int getCoordinateDimension() {
        return this.coordinateSystem.getCoordinateDimension();
    }

    /**
     * Clones a  PointSequence.
     *
     * <p>This method is defined in the JTS CoordinateSequence interface.
     * @return
     */
    @Override
    public abstract PointSequence clone() ;

    //TODO -- this can now be implemented much simpler now by a simple array copy
    @Override
    public void getCoordinates(double[] coordinates, int i) {
        if (coordinates.length < this.coordinateSystem.getCoordinateDimension())
            throw new IllegalArgumentException(String.format("Coordinate array must be at least of getLength %d", this.coordinateSystem.getCoordinateDimension()));
        coordinates[coordinateSystem.getIndex(CoordinateAccessor.X)] = getX(i);
        coordinates[coordinateSystem.getIndex(CoordinateAccessor.Y)] = getY(i);
        int ci = 2;
        if (is3D() ) {
            coordinates[coordinateSystem.getIndex(CoordinateAccessor.Z)]  = getZ(i);
        }
        if (isMeasured()){
            coordinates[coordinateSystem.getIndex(CoordinateAccessor.M)] = getM(i);
        }
    }

    @Override
    public double getX(int i) {
        return getCoordinate(i, CoordinateAccessor.X);
    }

    @Override
    public double getY(int i) {
        return getCoordinate(i, CoordinateAccessor.Y);
    }

    @Override
    public double getZ(int i) {
        return getCoordinate(i, CoordinateAccessor.Z);
    }

    @Override
    public double getM(int i) {
        return getCoordinate(i, CoordinateAccessor.M);
    }

    @Override
    public Coordinate getCoordinate(int i) {
        DimensionalCoordinate co = new DimensionalCoordinate(getCoordinateSystem());
        co.x = getX(i);
        co.y = getY(i);
        if (is3D())
            co.z = getZ(i);
        if (isMeasured())
            co.m = getM(i);
        return co;
    }

    @Override
    public Coordinate getCoordinateCopy(int i) {
        return getCoordinate(i);
    }

    @Override
    public void getCoordinate(int i, Coordinate coordinate) {
        coordinate.x = getX(i);
        coordinate.y = getY(i);
        coordinate.z = getZ(i);
        if (coordinate instanceof DimensionalCoordinate &&
                ((DimensionalCoordinate)coordinate).isMeasured()){
            ((DimensionalCoordinate)coordinate).m = getM(i);
        }
    }

    @Override
    public double getOrdinate(int i, int ordinateIndex){
        switch(ordinateIndex) {
            case CoordinateSequence.X:
                return getCoordinate(i, CoordinateAccessor.X);
            case CoordinateSequence.Y:
                return getCoordinate(i, CoordinateAccessor.Y);
            case CoordinateSequence.Z:
                return getCoordinate(i, CoordinateAccessor.Z);
            case CoordinateSequence.M:
                return getCoordinate(i, CoordinateAccessor.M);
        }
        throw new IllegalArgumentException("Ordinate index " + ordinateIndex + " is not supported.");
    }

    @Override
    public abstract double getCoordinate(int pointIndex, CoordinateAccessor accessor);

    @Override
    public void setOrdinate(int i, int ordinateIndex, double value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Coordinate[] toCoordinateArray() {
        return toCoordinateArray(this);
    }

    @Override
    public Envelope expandEnvelope(Envelope envelope) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Iterator<Point> iterator() {
        return new PointSequenceIterator(this);
    }

    @Override
    public void accept(PointVisitor visitor) {
        visitor.setCoordinateSystem(getCoordinateSystem());
        double[] coordinates = new double[getCoordinateDimension()];
        for (int i = 0; i < size(); i++) {
            getCoordinates(coordinates,i);
            visitor.visit(coordinates);
        }
    }

}
