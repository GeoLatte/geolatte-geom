package org.geolatte.geom.crs;

import org.geolatte.geom.*;

import java.util.Arrays;

/**
 * A compound {@code CoordinateReferenceSystem} that is constructed by adding additional CoordinateSystemAxes
 * to
 *
 * @author Karel Maesen, Geovise BVBA
 *         creation-date: 3/4/14
 */
public class CompoundCoordinateReferenceSystem<T extends Position, B extends Position> extends CoordinateReferenceSystem<T> {

    private final CoordinateReferenceSystem<B> base;

    protected CompoundCoordinateReferenceSystem(String name, CoordinateReferenceSystem<B> base, Class<T> positionType, CoordinateSystemAxis... additional) {
        super(base.getCrsId(), name, positionType, combineAxes(base.getCoordinateSystem().getAxes(), additional));
        this.base = base;

    }

    private static CoordinateSystemAxis[] combineAxes(CoordinateSystemAxis[] base, CoordinateSystemAxis[] additional) {
        CoordinateSystemAxis[] result = new CoordinateSystemAxis[base.length + additional.length];
        System.arraycopy(base, 0, result, 0, base.length);
        System.arraycopy(additional, 0, result, base.length, additional.length);
        return result;
    }

    @Override
    public boolean isCompound() {
        return true;
    }

    @Override
    public CoordinateReferenceSystem<B> getBaseCoordinateReferenceSystem() {
        return this.base;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        CompoundCoordinateReferenceSystem that = (CompoundCoordinateReferenceSystem) o;

        if (getIndexVerticalAxis() != that.getIndexVerticalAxis()) return false;
        if (!base.equals(that.base)) return false;
        if (!Arrays.equals(getMeasureAxes(), that.getMeasureAxes())) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + base.hashCode();
        return result;
    }
}
