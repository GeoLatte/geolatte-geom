package org.geolatte.geom.crs.trans;

import java.util.Arrays;

/**
 * Converts between a Geographic 3D and a 2D system
 *
 * EPSG Method 9659
 * Created by Karel Maesen, Geovise BVBA on 20/07/17.
 */
public class Geographic3DTo2D implements Transformation   {

    final private double height;

    public Geographic3DTo2D(double height) {
        this.height = height;
    }


    public Geographic3DTo2D() {
        this(0.0d);
    }

    @Override
    public boolean isReversible() {
        return true;
    }

    @Override
    public void forward(double[] inCoordinate, double[] outCoordinate) {
        System.arraycopy(inCoordinate, 0, outCoordinate, 0, 2);
    }

    @Override
    public void reverse(double[] inCoordinate, double[] outCoordinate) {
        System.arraycopy(inCoordinate, 0, outCoordinate, 0, 2);
        outCoordinate[2] = height;
    }

}

