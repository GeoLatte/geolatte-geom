package org.geolatte.geom.crs;

import org.geolatte.geom.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * A compound {@code CoordinateReferenceSystem} that is constructed by adding additional CoordinateSystemAxes to a
 * base coordinate reference system (either a Geographic, Geocentric or Projected Coordinate Reference System).
 *
 * @author Karel Maesen, Geovise BVBA
 *         creation-date: 3/4/14
 */
public class CompoundCoordinateReferenceSystem<P extends Position> extends CoordinateReferenceSystem<P> {

    private final List<SingleCoordinateReferenceSystem<?>> components;


    protected CompoundCoordinateReferenceSystem(String name, SingleCoordinateReferenceSystem<?>... components) {
        //TODO this is problematic: combineCS() result needs to be cast to make CompoundCRS into a CRS<P>!
        super(components[0].getCrsId(), name, (CoordinateSystem<P>) combineCS(components));
        this.components = Arrays.asList(components);
    }

    //TODO -- can we simplify here?
    private static CoordinateSystem<?> combineCS(SingleCoordinateReferenceSystem<?>[] components) {
        if (components == null || components.length == 0)
            throw new IllegalArgumentException("Too few arguments, or null arguments");
        CoordinateSystem<?> coordinateSystem = null;
        for (SingleCoordinateReferenceSystem<?> component : components) {
            if (coordinateSystem == null) {
                coordinateSystem = component.getCoordinateSystem();
            } else {
                if ( component.getCoordinateSystem() instanceof OneDimensionCoordinateSystem) {
                    coordinateSystem = coordinateSystem.merge((OneDimensionCoordinateSystem<?>) component.getCoordinateSystem());

                } else {
                    throw new UnsupportedOperationException("Can't merge specified coordinate systems");
                }
            }
        }
        return coordinateSystem;
    }

    public List<SingleCoordinateReferenceSystem<?>> getComponents() {
        return Collections.unmodifiableList(components);
    }

    @Override
    public boolean isCompound() {
        return true;
    }


}
