package org.geolatte.geom.codec;

import org.geolatte.geom.AbstractGeometryCollection;
import org.geolatte.geom.Geometry;
import org.geolatte.geom.Position;

class MySqlWkbDialect extends WkbDialect {
    final public static WkbDialect INSTANCE = new MySqlWkbDialect();

    private MySqlWkbDialect() {
    }

    @Override
    protected <P extends Position> int extraHeaderSize(Geometry<P> geom) {
        return 4;
    }

    @Override
    protected <P extends Position> int sizeEmptyGeometry(Geometry<P> geometry) {
        return 0;
    }

    private <P extends Position> boolean hasEmpty(Geometry<P> geometry) {
        if (geometry.isEmpty()) {
            return true;
        }
        if (geometry instanceof AbstractGeometryCollection) {
            for (Geometry<P> part : (AbstractGeometryCollection<P, ?>) geometry) {
                if (hasEmpty(part)) return true;
            }
        }
        return false;
    }
}
