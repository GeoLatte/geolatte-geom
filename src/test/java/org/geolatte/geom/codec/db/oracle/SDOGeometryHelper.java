package org.geolatte.geom.codec.db.oracle;

import java.math.BigDecimal;

/**
 * Created by Karel Maesen, Geovise BVBA on 28/03/15.
 */
public class SDOGeometryHelper {


    public static SDOGeometry sdoGeometry(Integer gtype, int srid, SDOPoint sdoPoint, int[] elemInfo, Double[] ordinates ) {

        ElemInfo info = elemInfo == null ? null : new ElemInfo(toBigDecimalArray(elemInfo));
        Ordinates o = ordinates == null ? null : new Ordinates(ordinates);

        SDOGeometry result = new SDOGeometry(
                SDOGType.parse(gtype),
                srid,
                sdoPoint,
                info ,
                o);

        return result;
    }

    public static SDOPoint sdoPoint(double x, double y, double z) {
        return new SDOPoint(x, y, z);
    }

    public static SDOPoint sdoPoint(double x, double y) {
        return new SDOPoint(x, y);
    }

    private static BigDecimal[] toBigDecimalArray(int[] ints) {
        BigDecimal[] result = new BigDecimal[ints.length];
        for (int i = 0; i < ints.length; i++) {
            result[i] = new BigDecimal(ints[i]);
        }
        return result;
    }




}
