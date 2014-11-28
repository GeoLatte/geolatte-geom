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

/**
 * Common coordinate reference systems.
 *
 * @author Karel Maesen, Geovise BVBA
 *         creation-date: 3/31/14
 */
public class CoordinateReferenceSystems {

    /**
     * Creates a generic projected coordinate reference system using the specified units of length for coordinates.
     * <p/>
     * A generic system is one without a precisely defined Coordinate Reference System
     *
     * @param unit the unit to use for the planar coordinates.
     * @return a {@code CoordinateReferenceSystem} with the specified length units
     */
    public static ProjectedCoordinateReferenceSystem mkProjected(LinearUnit unit) {
        return new ProjectedCoordinateReferenceSystem(CrsId.UNDEFINED, "Generic 2D Projected",
                mkGeographic(Unit.DEGREE), Projection.UNKNOWN, new ArrayList<CrsParameter>(),
                new CartesianCoordinateSystem2D(new StraightLineAxis("X", CoordinateSystemAxisDirection.EAST, unit), new
                StraightLineAxis("Y", CoordinateSystemAxisDirection.NORTH, unit)));
    }

    /**
     * Creates a generic geographic coordinate reference system using the specified units of length for coordinates.
     * <p/>
     * A generic system is one without a precisely defined datum or ellipsoid.
     *
     * @param unit the unit to use for the planar coordinates.
     * @return a {@code CoordinateReferenceSystem}
     */
    public static Geographic2DCoordinateReferenceSystem mkGeographic(AngularUnit unit) {
        return new Geographic2DCoordinateReferenceSystem(CrsId.UNDEFINED, "Generic 2D Projected", new
                EllipsoidalCoordinateSystem2D(new GeodeticLatitudeCSAxis("Lat", unit), new GeodeticLongitudeCSAxis
                ("Lon", unit)));
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
        return (CompoundCoordinateReferenceSystem<R>)combine(base,ods);
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
            List<SingleCoordinateReferenceSystem<?>> nc = new ArrayList<SingleCoordinateReferenceSystem<?>>();
            nc.addAll(components);
            nc.add(ods);
            return new CompoundCoordinateReferenceSystem(base.getName() + "+" + ods.getName(), nc.toArray(new
                    SingleCoordinateReferenceSystem[nc.size()]));
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
    final public static SingleCoordinateReferenceSystem<P2D> PROJECTED_2D_METER = CoordinateReferenceSystems.mkProjected
            (Unit.METER);

    /**
     * A generic projected 2DM {@code CoordinateReferenceSystem} with meter coordinates
     */
    final public static CompoundCoordinateReferenceSystem<P2DM> PROJECTED_2DM_METER = addLinearSystem(PROJECTED_2D_METER,
            P2DM.class, Unit.METER);

    /**
     * A generic projected 3D {@code CoordinateReferenceSystem} with meter coordinates
     */
    final public static CompoundCoordinateReferenceSystem<P3D> PROJECTED_3D_METER = addVerticalSystem(PROJECTED_2D_METER, P3D
            .class, Unit.METER);

    /**
     * A generic projected 3DM {@code CoordinateReferenceSystem} with meter coordinates
     */
    final public static CompoundCoordinateReferenceSystem<P3DM> PROJECTED_3DM_METER = addLinearSystem(PROJECTED_3D_METER,
            P3DM.class, Unit.METER);

            /**
             * The WGS 84 {@code GeographicCoordinateReferenceSystem}
             */
    public static Geographic2DCoordinateReferenceSystem WGS84 = CrsRegistry
            .getGeographicCoordinateReferenceSystemForEPSG(4326);

    public static <P extends Position> boolean hasAxisOrder(CoordinateReferenceSystem crs, int order){
        CoordinateSystemAxis[] axes = crs.getCoordinateSystem().getAxes();
        for (CoordinateSystemAxis axis : axes) {
            if (axis.getNormalOrder() == order) {
                return true;
            }
        }
        return false;
    }

    public static <P extends Position> boolean hasVerticalAxis(CoordinateReferenceSystem crs) {
        return hasAxisOrder(crs, 2);
    }

    public static <P extends Position> boolean hasMeasureAxis(CoordinateReferenceSystem crs) {
        return hasAxisOrder(crs, 3);
    }

}
