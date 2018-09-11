package org.geolatte.geom.crs;

import java.util.Objects;

/**
 * Created by Karel Maesen, Geovise BVBA on 11/09/2018.
 */
public class CrsExtendedId extends CrsId {

    final private Unit verticalUnit;
    final private Unit measureUnit;

    /**
     * Creates an instance having the specified authority and code.
     *
     * <p>If authority EPSG and 0 or -1 is passed for the code parameter, a value equal to <code>CrsId.UNDEFINED</code> is returned.
     *
     * param base base CrsId
     * @return a <code>CrsId</code> for the specified authority and code.
     */
    CrsExtendedId(CrsId base, Unit verticalUnit, Unit measureUnit) {
        super(base.getAuthority(), base.getCode());
        this.verticalUnit = verticalUnit;
        this.measureUnit = measureUnit;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        CrsExtendedId that = (CrsExtendedId) o;
        return Objects.equals(verticalUnit, that.verticalUnit) &&
                Objects.equals(measureUnit, that.measureUnit);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), verticalUnit, measureUnit);
    }
}
