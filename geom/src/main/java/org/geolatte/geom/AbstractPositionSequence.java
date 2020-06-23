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

import org.locationtech.jts.geom.*;
import org.locationtech.jts.geom.Envelope;

import java.io.Serializable;

/**
 * @author Karel Maesen, Geovise BVBA, 2011
 */
abstract class AbstractPositionSequence<P extends Position> implements PositionSequence<P>, CoordinateSequence, Serializable {

    private final PositionFactory<P> factory;

    public AbstractPositionSequence(PositionFactory<P> factory) {
        this.factory = factory;
    }

    private static org.locationtech.jts.geom.Coordinate[] toCoordinateArray(AbstractPositionSequence cseq) {
        org.locationtech.jts.geom.Coordinate[] coordinates = new org.locationtech.jts.geom.Coordinate[cseq.size()];
        for (int i = 0; i < cseq.size(); i++) {
            coordinates[i] = cseq.getCoordinate(i);

        }
        return coordinates;
    }


    @SuppressWarnings("unchecked")
    public Class<P> getPositionClass() {
        return factory.forClass();
    }

    @Override
    public PositionFactory<P> getPositionFactory() {
        return factory;
    }

    @Override
    public P getPositionN(int index) {
        double[] co = new double[getCoordinateDimension()];
        getCoordinates(index, co);
        return factory.mkPosition(co);
    }

    /*
   This overrides the CoordinateSequence notion of dimension
    */
    public int getDimension() {
        return getCoordinateDimension();
    }

    @Override
    public int getCoordinateDimension() {
        return this.factory.getCoordinateDimension();
    }

    /**
     * Clones a  PointCollection
     *
     * <p>This method is defined in the JTS CoordinateSequence interface.
     *
     * @return
     */
    @Override
    public abstract PositionSequence<P> clone();


    public org.locationtech.jts.geom.Coordinate getCoordinate(int i) {
        double[] c = new double[getCoordinateDimension()];
        getCoordinates(i, c);
        if (getCoordinateDimension() == 2) {
            return toCoordinateXY(c);
        } else if (factory.hasMComponent() && factory.hasZComponent()) {
            return toCoordinateXYZM(c);
        } else if (factory.hasZComponent()) {
            return toCoordinateXYZ(c);
        } else {
            return toCoordinateXYM(c);
        }
    }

    private CoordinateXY toCoordinateXY(double[] c) {
        CoordinateXY co = new CoordinateXY();
        co.setX(c[0]);
        co.setY(c[1]);
        return co;
    }

    private CoordinateXYM toCoordinateXYM(double[] c) {
        CoordinateXYM co = new CoordinateXYM();
        co.setX(c[0]);
        co.setY(c[1]);
        co.setM(c[2]);
        return co;
    }

    private Coordinate toCoordinateXYZ(double[] c) {
        Coordinate co = new Coordinate();
        co.setX(c[0]);
        co.setY(c[1]);
        co.setZ(c[2]);
        return co;
    }

    private CoordinateXYZM toCoordinateXYZM(double[] c) {
        CoordinateXYZM co = new CoordinateXYZM();
        co.setX(c[0]);
        co.setY(c[1]);
        co.setZ(c[2]);
        co.setM(c[3]);
        return co;
    }

    public org.locationtech.jts.geom.Coordinate getCoordinateCopy(int i) {
        return getCoordinate(i);
    }

    @Override
    public void getCoordinate(int index, org.locationtech.jts.geom.Coordinate coord) {
        double[] c = new double[getCoordinateDimension()];
        getCoordinates(index, c);
        coord.x = c[0];
        coord.y = c[1];
    }

    @Override
    public double getX(int index) {
        double[] c = new double[getCoordinateDimension()];
        getCoordinates(index, c);
        return c[0];
    }

    @Override
    public double getY(int index) {
        double[] c = new double[getCoordinateDimension()];
        getCoordinates(index, c);
        return c[1];
    }

    @Override
    public double getOrdinate(int i, int ordinateIndex) {
        double[] c = new double[getCoordinateDimension()];
        getCoordinates(i, c);
        int idx = 0;
        switch (ordinateIndex) {
            case CoordinateSequence.X:
                return c[0];
            case CoordinateSequence.Y:
                return c[1];
            case CoordinateSequence.Z:
                return factory.hasZComponent() ?
                        c[2] : Double.NaN;
            case CoordinateSequence.M:
                return factory.hasMComponent()?
                        c[factory.getMComponentIndex()] : Double.NaN;
        }
        throw new IllegalArgumentException("Ordinate index " + ordinateIndex + " is not supported.");
    }

    @Override
    public abstract void setOrdinate(int i, int ordinateIndex, double value);

    @Override
    public org.locationtech.jts.geom.Coordinate[] toCoordinateArray() {
        return toCoordinateArray(this);
    }

    @Override
    public Envelope expandEnvelope(Envelope envelope) {
        EnvelopeExpander<P> expander = new EnvelopeExpander<P>(envelope);
        this.accept(expander);
        return expander.result();
    }


    @Override
    abstract public void accept(PositionVisitor<P> visitor);

    private static class EnvelopeExpander<P extends Position> implements PositionVisitor<P> {

        final private Envelope env;
        final double[] buffer = new double[2];

        EnvelopeExpander(Envelope env) {
            this.env = env;
        }

        @Override
        public void visit(P position) {

            double ar[] = position.toArray(buffer);
            this.env.expandToInclude(ar[0], ar[1]);
        }

        public Envelope result() {
            return this.env;
        }

    }

}
