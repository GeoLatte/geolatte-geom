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

package org.geolatte.geom.crs;

import org.geolatte.geom.Position;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A coordinate system.
 * <p/>
 * <p>A coordinate system is characterized by its {@link CoordinateSystemAxis CoordinateSystemAxes} (in order).</p>
 *
 * @param <P> the Position type for the system
 *
 * @author Karel Maesen, Geovise BVBA
 *         creation-date: 4/29/11
 */
abstract public class CoordinateSystem<P extends Position> {

    private final CoordinateSystemAxis[] axes;

    /**
     * Constructs a <code>CoordinateSystem</code>.
     * <p/>
     * <p><code>CoordinateSystem</code>s are characterized by their {@link CoordinateSystemAxis CoordinateSystemAxes}. </p>
     *
     * @param axes the sequence (at least two) of its <code>CoordinateSystem</code>s.
     * @throws IllegalArgumentException when less than two axes are specified, or when an argument is null.
     */
    public CoordinateSystem(CoordinateSystemAxis... axes) {
        if (axes == null || axes.length == 0) {
            throw new IllegalArgumentException("Requires at least 1 axis");
        }
        for (CoordinateSystemAxis axis : axes) {
            if (axis == null) throw new IllegalArgumentException("Null axes are not allowed");
        }
        this.axes = axes;
    }

    abstract public Class<P> getPositionClass();

    /**
     * Returns the {@link CoordinateSystemAxis CoordinateSystemAxes} of this <code>CoordinateSystem</code> (in order).
     *
     * @return
     */
    public CoordinateSystemAxis[] getAxes() {
        return Arrays.copyOf(axes, axes.length);
    }

    /**
     * Returns a list of all the directions of this systems
     *
     * @return a List of Coordinate system axis directions.
     */
    public List<CoordinateSystemAxisDirection> getAxisDirections(){
        List<CoordinateSystemAxisDirection> directions = new ArrayList<CoordinateSystemAxisDirection>(axes.length);
        for (CoordinateSystemAxis a: axes){
            directions.add(a.getAxisDirection());
        }
        return directions;
    }

    /**
     * Returns the normal order value for the axes of this systems in the order that the axes have been defined for this
     * system.
     *
     * @return a List of Coordinate system axis directions.
     */
    public List<Integer> getAxisNormalOrder(){
        List<Integer> order = new ArrayList<Integer>(axes.length);
        for (CoordinateSystemAxis a: axes){
            order.add(a.getNormalOrder());
        }
        return order;
    }

    /**
     * Returns the coordinate dimension, i.e. the number of axes in this coordinate system.
     *
     * @return
     */
    public int getCoordinateDimension() {
        return this.axes.length;
    }

    /**
     * Returns the position of the specified {@link CoordinateSystemAxis} in this <code>CoordinateSystem</code>,
     * or -1 if it is not an axis of this instance.
     *
     * @param axis
     * @return
     */
    public int getAxisIndex(CoordinateSystemAxis axis) {
        int i = 0;
        for (CoordinateSystemAxis a : axes) {
            if (a.equals(axis)) return i;
            i++;
        }
        return -1;
    }

    /**
     * Return the axis that corresponds to the i-th element in the coordinates for a {@code Position}
     * in this {@code CoordinateSystem}.
     *
     * @param i the 0-base index for a coordinate
     * @return the axis
     * @throws java.lang.IndexOutOfBoundsException if i > getCoordinateDimension() - 1
     */
    public CoordinateSystemAxis getAxisForComponentIndex(int i) {
        List<Integer> order = getAxisNormalOrder();
        int oIdx = order.get(i);
        for (CoordinateSystemAxis a : axes) {
            if(a.getAxisDirection().defaultNormalOrder == oIdx) return a;
        }
        throw new IllegalStateException(); //if we get this, there is a programming error
    }

    /**
     * Returns the {@link CoordinateSystemAxis} at the specified position.
     *
     * @param index
     * @return
     */
    public CoordinateSystemAxis getAxis(int index) {
        return this.axes[index];
    }

    /**
     * Returns the <code>Unit</code> of the axis at the specified position.
     *
     * @param index
     * @return
     */
    public Unit getAxisUnit(int index) {
        return this.axes[index].getUnit();
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CoordinateSystem that = (CoordinateSystem) o;

        if (!Arrays.equals(axes, that.axes)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(axes);
    }

    /**
     * Create a coordinate system that merges this instance with the specified system
     * @param coordinateSystem the system to merge with
     * @return a new {@code CoordinateSystem}
     * @throws java.lang.UnsupportedOperationException if no supported coordinate system can represent the merge
     */
    public abstract CoordinateSystem<?> merge(OneDimensionCoordinateSystem<?> coordinateSystem);

    /**
     * Create a new coordinate system with the axes of this system plus the specified axis
     * @param axis the additional axis
     * @return a new {@code CoordinateSystem}
     * @throws java.lang.UnsupportedOperationException if no supported coordinate system can contain the axes.
     */
    public abstract CoordinateSystem<?> extend(CoordinateSystemAxis axis);

}
