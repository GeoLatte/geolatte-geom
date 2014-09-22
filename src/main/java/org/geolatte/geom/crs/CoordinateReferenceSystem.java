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

import org.geolatte.geom.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * A Coordinate Reference System.
 *
 * @author Karel Maesen, Geovise BVBA
 *         creation-date: 8/2/11
 */
public class CoordinateReferenceSystem<P extends Position> extends CrsIdentifiable {

    private final static Map<Class<? extends Position>, Class<? extends Measured>> measuredVariants =
            new HashMap<Class<? extends Position>, Class<? extends Measured>>();

    private final static Map<Class<? extends Position>, Class<? extends Vertical>> verticalVariants =
            new HashMap<Class<? extends Position>, Class<? extends Vertical>>();


    static {
        measuredVariants.put(P2D.class, P2DM.class);
        measuredVariants.put(P3D.class, P3DM.class);
        measuredVariants.put(G2D.class, G2DM.class);
        measuredVariants.put(G3D.class, G3DM.class);

        verticalVariants.put(P2D.class, P3D.class);
        verticalVariants.put(P2DM.class, P3DM.class);
        verticalVariants.put(G2D.class, G3D.class);
        verticalVariants.put(G2DM.class, G3DM.class);

    }

    @SuppressWarnings("unchecked")
    public static <T extends Position, M extends Position & Measured> Class<M> getMeasuredVariant(Class<T> positionTypeClass) {
        if (Measured.class.isAssignableFrom(positionTypeClass)) return (Class<M>) positionTypeClass;
        return (Class<M>) measuredVariants.get(positionTypeClass);
    }

    @SuppressWarnings("unchecked")
    public static <T extends Position, V extends Position & Vertical> Class<V> getVerticalVariant(Class<T> positionTypeClass) {
        if (Vertical.class.isAssignableFrom(positionTypeClass)) return (Class<V>) positionTypeClass;
        return (Class<V>) verticalVariants.get(positionTypeClass);
    }

    @SuppressWarnings("unchecked")
    public static <T extends Position, M extends Position & Measured> CoordinateReferenceSystem<M> getMeasuredVariant(
            CoordinateReferenceSystem<T> crs, CoordinateSystemAxis measureAxis) {

        if (crs.hasMeasureAxis()) return (CoordinateReferenceSystem<M>) crs;
        if (!measureAxis.isMeasureAxis()) throw new IllegalArgumentException("Require a MeasureAxis");
        Class<?> measuredClass = measuredVariants.get(crs.getPositionClass());
        return (CoordinateReferenceSystem<M>) crs.addAxes(crs.getName() + "_M", (Class<M>) measuredClass, measureAxis);
    }

    public static <T extends Position, M extends Position & Measured> CoordinateReferenceSystem<M> getMeasuredVariant(
            CoordinateReferenceSystem<T> crs) {
        return getMeasuredVariant(crs, new CoordinateSystemAxis("M", CoordinateSystemAxisDirection.OTHER, LengthUnit.METER));
    }

    @SuppressWarnings("unchecked")
    public static <T extends Position, V extends Position & Vertical> CoordinateReferenceSystem<V> getVerticalVariant(
            CoordinateReferenceSystem<T> crs, CoordinateSystemAxis verticalAxis) {

        if (crs.hasVerticalAxis()) return (CoordinateReferenceSystem<V>) crs;
        if (!verticalAxis.isVerticalAxis()) {
            throw new IllegalArgumentException("Require a VerticalAxis");
        }
        Class<?> verticalClass = verticalVariants.get(crs.getPositionClass());

        return (CoordinateReferenceSystem<V>) crs.addAxes(crs.getName() + "_Z", (Class<V>) verticalClass, verticalAxis);
    }

    public static <T extends Position, V extends Position & Vertical> CoordinateReferenceSystem<V> getVerticalVariant(
            CoordinateReferenceSystem<T> crs) {
        return getVerticalVariant(crs, new CoordinateSystemAxis("V", CoordinateSystemAxisDirection.OTHER, LengthUnit.METER));
    }

    private final CoordinateSystem coordinateSystem;
    private final Class<P> positionType;
    private final CoordinateSystemAxis[] measureAxes;
    private final NormalizedOrder normalizedOrder;

    //TODO -- check that there are no more than there are no more than one LAT/Northing or EAST/Lon etc. are.

    /**
     * Constructs a <code>CoordinateReferenceSystem</code>.
     *
     * @param crsId the {@link CrsId} that identifies this <code>CoordinateReferenceSystem</code> uniquely
     * @param name  the commonly used name for this <code>CoordinateReferenceSystem</code>
     * @param axes  the {@link CoordinateSystemAxis CoordinateSystemAxes} for this <code>CoordinateReferenceSystem</code>
     * @throws IllegalArgumentException if less than two {@link CoordinateSystemAxis CoordinateSystemAxes} are passed.
     */
    public CoordinateReferenceSystem(CrsId crsId, String name, Class<P> positionType, CoordinateSystemAxis... axes) {
        super(crsId, name);
        CoordinateSystem cs = new CoordinateSystem(axes);
        this.coordinateSystem = cs;
        this.positionType = positionType;
        this.measureAxes = getCoordinateSystem().getMeasureAxes();
        normalizedOrder = new NormalizedOrder(cs);
    }


    /**
     * Returns the type token for the type of <code>Position</code>s referenced in this system.
     *
     * @return a Class object
     */
    public Class<P> getPositionClass() {
        return this.positionType;
    }


    /**
     * Returns the <code>CoordinateSystem</code> associated with this <code>CoordinateReferenceSystem</code>.
     *
     * @return Returns the <code>CoordinateSystem</code> associated with this <code>CoordinateReferenceSystem</code>.
     */
    public CoordinateSystem getCoordinateSystem() {
        return coordinateSystem;
    }

    /**
     * Returns the coordinate dimension, i.e. the number of axes in this coordinate reference system.
     *
     * @return the coordinate dimension
     */
    public int getCoordinateDimension() {
        return getCoordinateSystem().getCoordinateDimension();
    }

    /**
     * Return the {@link CoordinateSystemAxis CoordinateSystemAxes} associated with this <code>CoordinateReferenceSystem</code>.
     *
     * @return an array of {@link CoordinateSystemAxis CoordinateSystemAxes}.
     */
    public CoordinateSystemAxis getAxis(int idx) {
        return coordinateSystem.getAxis(idx);
    }

    //TODO -- Add methods to inspect the coordinate system in the

    /**
     * Returns the index of the specified axis in this {@code CoordinateReferenceSystem}, or
     * -1 if it is not an axis of this system.
     *
     * @param axis the axis to look up
     * @return the index of the specified axis in this {@code CoordinateReferenceSystem}
     */
    public int getAxisIndex(CoordinateSystemAxis axis) {
        return getCoordinateSystem().getAxisIndex(axis);
    }

    /**
     * Returns the vertical axis of this instance, or null if there is none
     * @return the vertical axis of this instance
     */
    public CoordinateSystemAxis getVerticalAxis() {
        return getCoordinateSystem().getVerticalAxis();
    }

    /**
     * Returns the index of this system's vertical axis, or -1 if there is none.
     * <p/>
     * An axis is vertical if its {@code AxisDirection} is UP or DOWN.
     *
     * @return true if this instance contains a vertical axis.
     */
    public int getIndexVerticalAxis() {
        CoordinateSystemAxis axis = getCoordinateSystem().getVerticalAxis();
        return axis == null ? -1 : getAxisIndex(axis);
    }

    /**
     * Returns the index of this system's vertical axis, or -1 if there is none.
     * <p/>
     * An axis is vertical if its {@code AxisDirection} is UP or DOWN.
     *
     * @return true if this instance contains a vertical axis.
     */
    public boolean hasVerticalAxis() {
        return getVerticalAxis() != null;
    }

    /**
     * Returns the indices of this system's measure axes, if any.
     * <p/>
     * An axis is a measure axis if its {@code AxisDirection} is UNKNOWN or OTHER.
     *
     * @return true if this instance contains a vertical axis.
     */
    public CoordinateSystemAxis[] getMeasureAxes() {
        return this.measureAxes;
    }


    /**
     * Returns the first measure axis, or null if there are no measure axes.
     *
     * @return
     */
    public CoordinateSystemAxis getMeasureAxis() {
        CoordinateSystemAxis[] measureAxes = getMeasureAxes();
        return measureAxes.length == 0 ? null : measureAxes[0];
    }

    /**
     * Returns true if this system has at least one measure axis.
     * <p/>
     * An axis is a measure axis if its {@code AxisDirection} is UNKNOWN or OTHER.
     *
     * @return true if this instance contains a vertical axis.
     */
    public boolean hasMeasureAxis() {
        return getMeasureAxes().length > 0;
    }

    public boolean isCompound() {
        return false;
    }

    /**
     * Derive a compound CRS from this instance by adding the specified axes.
     *
     * @param name
     * @param axes
     * @param <Q>  the expected type of Position for the resulting compound CRS.
     * @return
     */
    //TODO -- remove this with addMeasure, addVertical axes + use reflection to derive appropriate targetPositionType
    public <Q extends Position> CompoundCoordinateReferenceSystem<Q, P> addAxes(String name, Class<Q> targetPositionType, CoordinateSystemAxis... axes) {
        boolean hasMeasure = false;
        boolean hasVert = false;

        for (CoordinateSystemAxis axis : getCoordinateSystem().getAxes()) {
            if (axis.isMeasureAxis()) {
                hasMeasure = true;
            } else if (axis.isVerticalAxis()) {
                hasVert = true;
            }
        }

        for (CoordinateSystemAxis axis : axes) {
            if (axis.isMeasureAxis()) {
                hasMeasure = true;
            } else if (axis.isVerticalAxis()) {
                hasVert = true;
            }
        }

        if (Vertical.class.isAssignableFrom(targetPositionType) && !hasVert) {
            throw new IllegalStateException("Attempt to create a CRS without Vertical axis, but for Position with Vertical component.");
        }

        if (!Vertical.class.isAssignableFrom(targetPositionType) && hasVert) {
            throw new IllegalStateException("Attempt to create a CRS with a Vertical axis, but for Position without Vertical component.");
        }

        if (Measured.class.isAssignableFrom(targetPositionType) && !hasMeasure) {
            throw new IllegalStateException("Attempt to create a CRS without Measure axis, but for Position with Measure component.");
        }

        if (!Measured.class.isAssignableFrom(targetPositionType) && hasMeasure) {
            throw new IllegalStateException("Attempt to create a CRS with a Measure axis, but for Position without Measure component.");
        }

        return new CompoundCoordinateReferenceSystem<Q, P>(name, this, targetPositionType, axes);
    }

    public <M extends Position & Measured> CompoundCoordinateReferenceSystem<M, P> addMeasureAxis(Unit unit) {
        Class<M> targetPType = CoordinateReferenceSystem.getMeasuredVariant(getPositionClass());
        return addAxes(getName() + " [+M]", targetPType, new CoordinateSystemAxis("M", CoordinateSystemAxisDirection.OTHER, unit));
    }

    public <V extends Position & Vertical> CompoundCoordinateReferenceSystem<V, P> addVerticalAxis(Unit unit) {
        Class<V> targetPType = CoordinateReferenceSystem.getVerticalVariant(getPositionClass());
        return addAxes(getName() + " [+Z]", targetPType, new CoordinateSystemAxis("Z", CoordinateSystemAxisDirection.UP, unit));
    }

    public CoordinateReferenceSystem<?> getBaseCoordinateReferenceSystem() {
        return this;
    }

    /**
     * Provides the normalized order of {@code CoordinateSystemAxis}es to the axis order
     * in this reference system.
     *
     * @return the normalized order mapping
     */
    public NormalizedOrder getNormalizedOrder() {
        return this.normalizedOrder;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        CoordinateReferenceSystem that = (CoordinateReferenceSystem) o;

        if (!coordinateSystem.equals(that.coordinateSystem)) return false;
        if (!Arrays.equals(measureAxes, that.measureAxes)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + coordinateSystem.hashCode();
        result = 31 * result + Arrays.hashCode(measureAxes);
        return result;
    }
}
