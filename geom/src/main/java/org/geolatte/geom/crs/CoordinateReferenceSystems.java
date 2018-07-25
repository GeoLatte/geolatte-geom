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
 * Copyright (C) 2010 - 2014 and Ownership of code is shared by:
 * Qmino bvba - Romeinsestraat 18 - 3001 Heverlee  (http://www.qmino.com)
 * Geovise bvba - Generaal Eisenhowerlei 9 - 2140 Antwerpen (http://www.geovise.com)
 */

package org.geolatte.geom.crs;

import org.geolatte.geom.*;

import java.util.ArrayList;
import java.util.List;

import static java.lang.String.format;

/**
 * Common coordinate reference systems.
 *
 * @author Karel Maesen, Geovise BVBA
 * creation-date: 3/31/14
 */
public class CoordinateReferenceSystems {

    public static ProjectedCoordinateReferenceSystem mkProjected(int srid, LinearUnit unit) {
        return new ProjectedCoordinateReferenceSystem(CrsId.valueOf(srid), "Generic 2D Projected",
                mkGeographic(Unit.DEGREE), Projection.UNKNOWN, new ArrayList<>(),
                new CartesianCoordinateSystem2D(new StraightLineAxis("X", CoordinateSystemAxisDirection.EAST, unit), new
                        StraightLineAxis("Y", CoordinateSystemAxisDirection.NORTH, unit)));
    }

    /**
     * Creates a generic projected coordinate reference system using the specified units of length for coordinates.
     * <p/>
     * A generic system is one without a precisely defined Coordinate Reference System
     *
     * @param unit the unit to use for the planar coordinates.
     * @return a {@code CoordinateReferenceSystem} with the specified length units
     */
    public static ProjectedCoordinateReferenceSystem mkProjected(LinearUnit unit) {
        return mkProjected(CrsId.UNDEFINED.getCode(), unit);
    }

    /**
     * Creates a generic geographic coordinate reference system using the specified units of length for coordinates.
     * <p/>
     * A generic system is one without a precisely defined datum or ellipsoid.
     *
     * @param
     * @param unit the unit to use for the planar coordinates.
     * @return a {@code CoordinateReferenceSystem}
     */
    public static GeographicCoordinateReferenceSystem mkGeographic(int srid, AngularUnit unit) {
        return new Geographic2DCoordinateReferenceSystem(CrsId.valueOf(srid), "Generic 2D Geographic", new
                EllipsoidalCoordinateSystem2D(new GeodeticLatitudeCSAxis("Lat", unit), new GeodeticLongitudeCSAxis
                ("Lon", unit)));
    }

    public static GeographicCoordinateReferenceSystem mkGeographic(AngularUnit unit) {
        return mkGeographic(CrsId.UNDEFINED.getCode(), unit);
    }

    /**
     * Returns a {@code CoordinateReferenceSystem} derived from the specified @{code {@link CoordinateReferenceSystem}}
     * but extended with the specified axis
     *
     * @param baseCrs      the base Coordinate Reference System
     * @param verticalUnit the Unit for the Vertical axis (or null if not required)
     * @param measureUnit  the Unit for measures (or null if not required)
     * @return a {@code CoordinateReferenceSystem} with at least the specified dimension, and using the specified
     * crs as base
     */
    public static CoordinateReferenceSystem<?> mkCoordinateReferenceSystem(
            CoordinateReferenceSystem<?> baseCrs, LinearUnit verticalUnit, LinearUnit measureUnit) {

        CoordinateReferenceSystem<?> result = baseCrs;
        if (verticalUnit != null &&
                !hasVerticalAxis(baseCrs)) {
            result = addVerticalSystem(result, verticalUnit);
        }
        if (measureUnit != null && !hasMeasureAxis(baseCrs)) {
            result = addLinearSystem(result, measureUnit);
        }
        return result;
    }

    public static CoordinateReferenceSystem<?> mkCoordinateReferenceSystem(
            int epsgCode, LinearUnit verticalUnit, LinearUnit measureUnit) {

        return mkCoordinateReferenceSystem(
                CrsRegistry.getCoordinateReferenceSystemForEPSG(epsgCode, PROJECTED_2D_METER),
                verticalUnit,
                measureUnit
        );

    }


    @SuppressWarnings("unchecked")
    public static <P extends Position> CoordinateReferenceSystem<P> mkCoordinateReferenceSystem(
            CoordinateReferenceSystem<?> baseCrs, LinearUnit verticalUnit, LinearUnit measureUnit, Class<P> positionType) {

        CoordinateReferenceSystem<?> crs = mkCoordinateReferenceSystem(baseCrs, verticalUnit, measureUnit);

        if (crs.getPositionClass().equals(positionType)) {
            return (CoordinateReferenceSystem<P>)crs;
        }

        throw new IllegalArgumentException(format("Invalid positionClass: %s not equals %s",
                crs.getPositionClass().getName(), positionType.getName()));
    }

    /**
     * Creates a 1-Dimensional {@code LinearCoordinateReferenceSystem}
     *
     * @param unit the linear unit tot use
     * @return a  {@code LinearCoordinateReferenceSystem} with an M-axis
     */
    public static LinearCoordinateReferenceSystem mkLinear(LinearUnit unit) {
        return new LinearCoordinateReferenceSystem("measure", new MeasureStraightLineAxis("M", unit));
    }

    /**
     * Creates a 1-Dimensional {@code VerticalCoordinateReferenceSystem}
     *
     * @param unit the linear unit to use
     * @return a  {@code LinearCoordinateReferenceSystem} with an Z-axis
     */
    public static VerticalCoordinateReferenceSystem mkVertical(LinearUnit unit) {
        return new VerticalCoordinateReferenceSystem(CrsId.UNDEFINED, "vertical", VerticalDatum
                .UNKNOWN_VERTICAL_DATUM, new VerticalStraightLineAxis("Z", unit));
    }

    /**
     * @param base
     * @param ods
     * @param resultCSPtype the Position class for the result of the combined coordinate reference system
     * @param <P>
     * @param <R>
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <P extends Position, R extends P> CompoundCoordinateReferenceSystem<R> combine
    (CoordinateReferenceSystem<P> base, SingleCoordinateReferenceSystem ods, Class<R> resultCSPtype) {
        return (CompoundCoordinateReferenceSystem<R>) combine(base, ods);
    }

    public static <P extends Position, R extends P> CompoundCoordinateReferenceSystem<R> addLinearSystem
            (CoordinateReferenceSystem<P> base, Class<R> resultCSPtype, LinearUnit unit) {
        return combine(base, mkLinear(unit), resultCSPtype);
    }

    public static <P extends Position, R extends P> CompoundCoordinateReferenceSystem<R> addVerticalSystem
            (CoordinateReferenceSystem<P> base, Class<R> resultCSPtype, LinearUnit unit) {
        return combine(base, mkVertical(unit), resultCSPtype);
    }


    @SuppressWarnings("unchecked")
    public static <P extends Position> CompoundCoordinateReferenceSystem<?> combine
            (CoordinateReferenceSystem<P> base, SingleCoordinateReferenceSystem ods) {
        if (base instanceof CompoundCoordinateReferenceSystem) {
            List<SingleCoordinateReferenceSystem<?>> components = ((CompoundCoordinateReferenceSystem<P>) base)
                    .getComponents();
            List<SingleCoordinateReferenceSystem<?>> nc = new ArrayList<SingleCoordinateReferenceSystem<?>>(components);
            nc.add(ods);
            return new CompoundCoordinateReferenceSystem(base.getName() + "+" + ods.getName(), nc.toArray(
                    new SingleCoordinateReferenceSystem[0]));
        } else if (base instanceof SingleCoordinateReferenceSystem) {
            SingleCoordinateReferenceSystem<P> single = (SingleCoordinateReferenceSystem<P>) base;
            return new CompoundCoordinateReferenceSystem(single.getName() + "+" + ods.getName(), single, ods);
        }

        throw new UnsupportedOperationException("Unsupported type of coordinate reference system");
    }

    public static <P extends Position> CompoundCoordinateReferenceSystem<?> addLinearSystem
            (CoordinateReferenceSystem<P> base, LinearUnit unit) {
        return combine(base, mkLinear(unit));
    }

    public static <P extends Position> CompoundCoordinateReferenceSystem<?> addVerticalSystem
            (CoordinateReferenceSystem<P> base, LinearUnit unit) {
        return combine(base, mkVertical(unit));
    }


    /**
     * A generic projected 2D {@code CoordinateReferenceSystem} with meter coordinates
     */
    final public static SingleCoordinateReferenceSystem<C2D> PROJECTED_2D_METER = CoordinateReferenceSystems.mkProjected
            (Unit.METER);

    /**
     * A generic projected 2DM {@code CoordinateReferenceSystem} with meter coordinates
     */
    final public static CompoundCoordinateReferenceSystem<C2DM> PROJECTED_2DM_METER = addLinearSystem(PROJECTED_2D_METER,
            C2DM.class, Unit.METER);

    /**
     * A generic projected 3D {@code CoordinateReferenceSystem} with meter coordinates
     */
    final public static CompoundCoordinateReferenceSystem<C3D> PROJECTED_3D_METER = addVerticalSystem(PROJECTED_2D_METER, C3D
            .class, Unit.METER);

    /**
     * A generic projected 3DM {@code CoordinateReferenceSystem} with meter coordinates
     */
    final public static CompoundCoordinateReferenceSystem<C3DM> PROJECTED_3DM_METER = addLinearSystem(PROJECTED_3D_METER,
            C3DM.class, Unit.METER);

    /**
     * The WGS 84 {@code GeographicCoordinateReferenceSystem}
     */
    public static Geographic2DCoordinateReferenceSystem WGS84 = CrsRegistry
            .getGeographicCoordinateReferenceSystemForEPSG(4326);

    /**
     * The WGS 84/Pseudo-Mercator {@code ProjectedCoordinateReferenceSystem}
     *
     * This is de de facto standard for Web mapping applications. See <a href="https://en.wikipedia.org/wiki/Web_Mercator#Identifiers">this Wikipedia article</a>
     * for more information, and some warnings of its use.
     */
    public static ProjectedCoordinateReferenceSystem WEB_MERCATOR = CrsRegistry
            .getProjectedCoordinateReferenceSystemForEPSG(3857);

    /**
     * The European ETRS89 geographic reference system.
     *
     * This system can be used for all of Europe.
     */
    public static GeographicCoordinateReferenceSystem ETRS89 = CrsRegistry
            .getGeographicCoordinateReferenceSystemForEPSG(4258);


    public static <P extends Position> boolean hasAxisOrder(CoordinateReferenceSystem<P> crs, int order) {
        CoordinateSystemAxis[] axes = crs.getCoordinateSystem().getAxes();
        for (CoordinateSystemAxis axis : axes) {
            if (axis.getNormalOrder() == order) {
                return true;
            }
        }
        return false;
    }

    public static <P extends Position> boolean hasVerticalAxis(CoordinateReferenceSystem<P> crs) {
        return hasAxisOrder(crs, 2);
    }

    public static <P extends Position> boolean hasMeasureAxis(CoordinateReferenceSystem<P> crs) {
        return hasAxisOrder(crs, 3);
    }

}
