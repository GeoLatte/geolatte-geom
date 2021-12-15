package org.geolatte.geom.codec.db.oracle;

import org.geolatte.geom.*;
import org.geolatte.geom.cga.NumericalMethods;
import org.geolatte.geom.codec.db.Encoder;

import java.util.Stack;

import static org.geolatte.geom.PositionSequenceBuilders.fixedSized;

/**
 * Created by Karel Maesen, Geovise BVBA on 01/04/15.
 */
abstract public class AbstractSDOEncoder implements Encoder<SDOGeometry> {


    /**
     * Returns the dimension for the Measured coordinate
     * @param geom  geometry to inspect
     * @param <P> Position type of the geometry
     * @return the dimension for the Measured coordinate
     * @deprecated  Use SDOGtype.gtypeFor(Geometry)
     *
     */
    @Deprecated
    protected <P extends Position> int getLRSDim(Geometry<P> geom) {
        if (Measured.class.isAssignableFrom(geom.getPositionClass())) {
            if (geom.getCoordinateDimension() == 3) {
                return 3;
            } else {
                return 4;
            }
        }
        return 0;
    }

    protected SDOGeometry addPolygon(SDOGeometry sdoGeom, Polygon polygon) {
        final int numInteriorRings = polygon.getNumInteriorRing();
        final ElemInfo info = new ElemInfo(numInteriorRings + 1);
        int ordinatesPreviousOffset = 0;
        if (sdoGeom.getOrdinates() != null) {
            ordinatesPreviousOffset = sdoGeom.getOrdinates().getOrdinateArray().length;
        }
        int ordinatesOffset = ordinatesPreviousOffset + 1;
        Double[] ordinates = new Double[]{};
        for (int i = 0; i < info.getNumTriplets(); i++) {
            ElementType et;
            PositionSequence<?> positionSequence;
            if (i == 0) {
                et = ElementType.EXTERIOR_RING_STRAIGHT_SEGMENTS;
                LinearRing<?> exteriorRing = polygon.getExteriorRing();
                positionSequence = exteriorRing.getPositions();
                if (!isCounterClockwise(exteriorRing)) {
                    positionSequence = reverse(positionSequence);
                }
            } else {
                et = ElementType.INTERIOR_RING_STRAIGHT_SEGMENTS;
                LinearRing<?> interiorRing = polygon.getInteriorRingN(i - 1);
                positionSequence = interiorRing.getPositions();
                if (isCounterClockwise(interiorRing)) {
                    positionSequence = reverse(positionSequence);
                }
            }
            info.setElement(i, ordinatesOffset, et);
            ordinates = addOrdinates(ordinates, convertPositionSequence(positionSequence));
            ordinatesOffset = ordinatesPreviousOffset + ordinates.length + 1;
        }
        ElemInfo newInfo = addElementInfo(sdoGeom.getInfo(), info);
        Ordinates newOrdiantes = addOrdinates(sdoGeom.getOrdinates(), ordinates);
        return new SDOGeometry(sdoGeom.getGType(), sdoGeom.getSRID(), null, newInfo, newOrdiantes);
    }

    protected ElemInfo addElementInfo(ElemInfo oldInfo, ElemInfo added) {
        if (oldInfo == null) return added;
        oldInfo.addElement(added);
        return oldInfo;
    }

    protected Ordinates addOrdinates(Ordinates oldOrdinates, Double[] newOrdinates) {
        if (oldOrdinates == null) {
            return new Ordinates(newOrdinates);
        } else {
            oldOrdinates.addOrdinates(newOrdinates);
            return oldOrdinates;
        }
    }

    protected Double[] convertPositionSequence(PositionSequence<?> coordinates) {
        int dim = coordinates.getCoordinateDimension();
        if (dim > 4) {
            throw new IllegalArgumentException("Dim parameter value cannot be greater than 4");
        }
        final Double[] converted = new Double[coordinates.size() * dim];
        LLAPositionVisitor visitor = new ToArrayVisitor(converted);
        coordinates.accept(visitor);
        return converted;
    }

    protected <P extends Position> PositionSequence<P> reverse(PositionSequence<P> positions) {
        PositionSequenceBuilder<P> builder = fixedSized(positions.size(), positions.getPositionClass());
        Stack<P> stack = new Stack<P>();
        for (P pos : positions) {
            stack.push(pos);
        }
        while (!stack.empty()) {
            builder.add(stack.pop());
        }
        return builder.toPositionSequence();
    }

    protected Double[] addOrdinates(Double[] oldOrdinates, Double[] newOrdinates) {
        final Double[] combined = new Double[oldOrdinates.length + newOrdinates.length];
        System.arraycopy(oldOrdinates, 0, combined, 0, oldOrdinates.length);
        System.arraycopy(newOrdinates, 0, combined, oldOrdinates.length, newOrdinates.length);
        return combined;
    }

    static class ToArrayVisitor implements LLAPositionVisitor {
        private final Double[] result;
        int idx = 0;

        ToArrayVisitor(Double[] result) {
            this.result = result;
        }

        @Override
        public void visit(double[] coordinate) {
            for (int i = 0; i < coordinate.length; i++) {
                result[idx++] = toDouble(coordinate[i]);
            }
        }
    }

    private boolean isCounterClockwise(LinearRing<?> ring) {
        try {
            return NumericalMethods.isCounterClockwise(ring);
        } catch (IllegalArgumentException e) {
            //assume true when points are (nearly) collinear
            // this can happen with very small rings or invalid rings.
            return true;
        }
    }

    /**
     * This method converts a double primitive to a Double wrapper instance, but
     * treats a Double.NaN value as null.
     *
     * @param d the value to be converted
     * @return A Double instance of d, Null if the parameter is Double.NaN
     */
    private static Double toDouble(double d) {
        return Double.isNaN(d) ? null : d;
    }

}
