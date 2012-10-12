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

import java.util.Iterator;

/**
 * Represents an empty <code>PointSequence</code>.
 *
 * @author Karel Maesen, Geovise BVBA, 2011
 */
public class EmptyPointSequence implements PointSequence, CoordinateSequence {

    public static final EmptyPointSequence INSTANCE = new EmptyPointSequence();

    private EmptyPointSequence(){}

    @Override
    public boolean is3D() {
        return false;
    }

    @Override
    public boolean isMeasured() {
        return false;
    }

    @Override
    public DimensionalFlag getDimensionalFlag() {
        return DimensionalFlag.XY;
    }

    @Override
    public boolean isEmpty() {
        return true;
    }

    @Override
    public int getCoordinateDimension() {
        return 2;
    }

    @Override
    public void getCoordinates(double[] coordinates, int index) {
        throw new IndexOutOfBoundsException("Empty PointSequence");
    }

    @Override
    public int getDimension() {
        return getCoordinateDimension();
    }

    @Override
    public Coordinate getCoordinate(int i) {
        throw new IndexOutOfBoundsException("Empty PointSequence");
    }

    @Override
    public Coordinate getCoordinateCopy(int i) {
        throw new IndexOutOfBoundsException("Empty PointSequence");
    }

    @Override
    public void getCoordinate(int index, Coordinate coord) {
        throw new IndexOutOfBoundsException("Empty PointSequence");
    }

    @Override
    public double getX(int position) {
        throw new IndexOutOfBoundsException("Empty PointSequence");
    }

    @Override
    public double getY(int position) {
        throw new IndexOutOfBoundsException("Empty PointSequence");
    }

    @Override
    public double getOrdinate(int index, int ordinateIndex) {
        throw new IndexOutOfBoundsException("Empty PointSequence");
    }

    @Override
    public double getCoordinate(int position, CoordinateComponent component) {
        throw new IndexOutOfBoundsException("Empty PointSequence");
    }

    @Override
    public double getZ(int position) {
        throw new IndexOutOfBoundsException("Empty PointSequence");
    }

    @Override
    public double getM(int position) {
        throw new IndexOutOfBoundsException("Empty PointSequence");
    }

    @Override
    public int size() {
        return 0;
    }

    @Override
    public void setOrdinate(int index, int ordinateIndex, double value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Coordinate[] toCoordinateArray() {
        return new Coordinate[0];
    }

    @Override
    public Envelope expandEnvelope(Envelope env) {
        return env;
    }

    @Override
    public Iterator<Point> iterator() {
        return new PointSequenceIterator(this);
    }

    @Override
    public EmptyPointSequence clone(){
        return this;
    }

    @Override
    public void accept(PointVisitor visitor) {
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || !(o instanceof PointSequence)) return false;

        PointSequence that = (PointSequence) o;
        return that.isEmpty();
    }

    @Override
    public String toString(){
        return " EMPTY";
    }

}
