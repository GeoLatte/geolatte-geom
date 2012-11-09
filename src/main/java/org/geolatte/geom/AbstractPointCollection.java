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
import org.geolatte.geom.crs.CrsId;
import org.geolatte.geom.jts.DimensionalCoordinate;

/**
 * @author Karel Maesen, Geovise BVBA, 2011
 */
abstract class AbstractPointCollection implements PointCollection, CoordinateSequence {

    private final DimensionalFlag dimensionalFlag;
    private final CrsId crsId;


    public AbstractPointCollection(DimensionalFlag dimensionalFlag, CrsId crsId) {
        this.dimensionalFlag = dimensionalFlag;
        this.crsId = crsId;
    }


    private static Coordinate[] toCoordinateArray(AbstractPointCollection set) {
        Coordinate[] coordinates = new Coordinate[set.size()];
        for (int i = 0; i < set.size(); i++){
            coordinates[i] = new DimensionalCoordinate(set.getDimensionalFlag());
            set.getCoordinate(i, coordinates[i]);
        }
        return coordinates;
    }

    public DimensionalFlag getDimensionalFlag() {
        return this.dimensionalFlag;
    }

    public CrsId getCrsId() {
        return this.crsId;
    }

    public boolean is3D(){
        return this.dimensionalFlag.is3D();
    }

    public boolean isMeasured(){
        return this.dimensionalFlag.isMeasured();
    }

    /*
   This overrides the CoordinateSequence notion of dimension
    */
    public int getDimension() {
        return getCoordinateDimension();
    }

    public int getCoordinateDimension() {
        return this.dimensionalFlag.getCoordinateDimension();
    }

    /**
     * Clones a  PointCollection
     *
     * <p>This method is defined in the JTS CoordinateSequence interface.
     * @return
     */
    @Override
    public abstract PointCollection clone() ;

    @Override
    public void getCoordinates(double[] coordinates, int i) {
        if (coordinates.length < this.dimensionalFlag.getCoordinateDimension())
            throw new IllegalArgumentException(String.format("Coordinate array must be at least of getLength %d", this.dimensionalFlag.getCoordinateDimension()));
        coordinates[dimensionalFlag.X] = getX(i);
        coordinates[dimensionalFlag.Y] = getY(i);
        if (is3D() ) {
            coordinates[dimensionalFlag.Z]  = getZ(i);
        }
        if (isMeasured()){
            coordinates[dimensionalFlag.M] = getM(i);
        }
    }

    public double getX(int position) {
        return getCoordinate(position, CoordinateComponent.X);
    }

    public double getY(int position) {
        return getCoordinate(position, CoordinateComponent.Y);
    }

    public double getZ(int position) {
        return getCoordinate(position, CoordinateComponent.Z);
    }

    public double getM(int position) {
        return getCoordinate(position, CoordinateComponent.M);
    }

    public Coordinate getCoordinate(int i) {
        DimensionalCoordinate co = new DimensionalCoordinate(getDimensionalFlag());
        co.x = getX(i);
        co.y = getY(i);
        if (is3D())
            co.z = getZ(i);
        if (isMeasured())
            co.m = getM(i);
        return co;
    }

    public Coordinate getCoordinateCopy(int i) {
        return getCoordinate(i);
    }

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
                return getCoordinate(i, CoordinateComponent.X);
            case CoordinateSequence.Y:
                return getCoordinate(i, CoordinateComponent.Y);
            case CoordinateSequence.Z:
                return getCoordinate(i, CoordinateComponent.Z);
            case CoordinateSequence.M:
                return getCoordinate(i, CoordinateComponent.M);
        }
        throw new IllegalArgumentException("Ordinate index " + ordinateIndex + " is not supported.");
    }

    @Override
    public abstract double getCoordinate(int position, CoordinateComponent accessor);

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
        EnvelopeExpander expander = new EnvelopeExpander(envelope);
        this.accept(expander);
        return expander.result();
    }


    @Override
    public void accept(PointVisitor visitor) {
        double[] coordinates = new double[getCoordinateDimension()];
        for (int i = 0; i < size(); i++) {
            getCoordinates(coordinates,i);
            visitor.visit(coordinates);
        }
    }

    private static class EnvelopeExpander implements PointVisitor{

        final private Envelope env;
        EnvelopeExpander(Envelope env) {
            this.env = env;
        }

        @Override
        public void visit(double[] coordinates) {
            this.env.expandToInclude(coordinates[0], coordinates[1]);
        }

        public Envelope result(){
            return this.env;
        }

    }

}
