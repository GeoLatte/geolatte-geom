package org.geolatte.geom.codec.db.oracle;

import org.geolatte.geom.*;
import org.geolatte.geom.cga.CircularArcLinearizer;
import org.geolatte.geom.codec.db.Decoder;
import org.geolatte.geom.crs.CoordinateReferenceSystem;
import org.geolatte.geom.crs.CoordinateReferenceSystems;
import org.geolatte.geom.crs.CrsRegistry;

import java.util.List;

import static org.geolatte.geom.PositionSequenceBuilders.fixedSized;

/**
 * Created by Karel Maesen, Geovise BVBA on 17/02/15.
 */
abstract public class AbstractSDODecoder implements Decoder<SDOGeometry> {

    private static final double LINEARIZER_EPSILON = 0.0001;
    protected SDOGeometry nativeGeom;

    @Override
    public Geometry<?> decode(SDOGeometry nativeGeom) {
        if (!accepts(nativeGeom)) {
            throw new IllegalArgumentException(getClass().getSimpleName() + " received object of type " + nativeGeom.getGType());
        }
        this.nativeGeom = nativeGeom;
        return internalDecode();
    }

    /**
     * Returns the SRID
     *
     * @param nativeGeom the SDOGeometry to decode
     * @return the CoordinateReferencySystem of the geometry
     */
    @Deprecated
    CoordinateReferenceSystem<?> getCoordinateReferenceSystem(SDOGeometry nativeGeom) {
        return nativeGeom.getCoordinateReferenceSystem();
    }

    //Note in these and subsequent private methods we pass in the crs so that type inferencing can occur
    @Deprecated
    protected <P extends Position> PositionSequence<P> convertOrdinateArray(Double[] oordinates, SDOGeometry sdoGeom, CoordinateReferenceSystem<P> crs) {
        final int dim = sdoGeom.getDimension();
        int numPos = oordinates.length / dim;

        PositionSequenceBuilder<P> sequenceBuilder = fixedSized(numPos, crs.getPositionClass());

        final int zDim = sdoGeom.getZDimension() - 1;
        final int lrsDim = sdoGeom.getLRSDimension() - 1;

        double[] buffer = new double[dim];
        for (int posIdx = 0; posIdx < numPos; posIdx++) {
            int componentIdx = 0; //tracks component in Position
            buffer[componentIdx] = oordinates[posIdx * dim + componentIdx]; //x
            componentIdx++;
            buffer[componentIdx] = oordinates[posIdx * dim + componentIdx]; //y
            if (zDim > 0) {
                componentIdx++;
                /*
                 * Z ordinate can be null! This is valid in ORACLE, but leads to an NPE because of autoboxing.
                 */
                Double zOrdinate = oordinates[posIdx * dim + zDim];
                buffer[componentIdx] = zOrdinate == null ? Double.NaN : zOrdinate;
            }
            if (lrsDim > 0) {
                componentIdx++;
                buffer[componentIdx] = oordinates[posIdx * dim + lrsDim];
            }

            sequenceBuilder.add(buffer);
        }
        return sequenceBuilder.toPositionSequence();
    }

    abstract Geometry<?> internalDecode();

    abstract <P extends Position> Geometry<P> decode(SDOGType gtype, List<Element> elements, CoordinateReferenceSystem<P> crs);


    @Deprecated
    static class CombiningVisitor<P extends Position> implements LLAPositionVisitor {

        final PositionSequenceBuilder<P> builder;

        CombiningVisitor(PositionSequenceBuilder<P> builder) {
            this.builder = builder;
        }

        /**
         * The visit method that is executed for each coordinate.
         *
         * @param coordinate the visited coordinate in array representation
         */
        @Override
        public void visit(double[] coordinate) {
            this.builder.add(coordinate);
        }

        PositionSequence<P> result() {
            return builder.toPositionSequence();
        }
    }


}
