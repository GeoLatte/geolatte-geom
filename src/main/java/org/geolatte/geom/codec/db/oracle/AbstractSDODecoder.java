package org.geolatte.geom.codec.db.oracle;

import com.vividsolutions.jts.geom.Coordinate;
import org.geolatte.geom.*;
import org.geolatte.geom.cga.CircularArcLinearizer;
import org.geolatte.geom.codec.db.Decoder;
import org.geolatte.geom.crs.CoordinateReferenceSystem;
import org.geolatte.geom.crs.CoordinateReferenceSystems;
import org.geolatte.geom.crs.CrsRegistry;
import org.geolatte.geom.crs.LinearUnit;

import java.util.ArrayList;
import java.util.List;

import static org.geolatte.geom.PositionSequenceBuilders.fixedSized;

/**
* Created by Karel Maesen, Geovise BVBA on 17/02/15.
*/
abstract public class AbstractSDODecoder implements Decoder<SDOGeometry> {


    //TODO -- this should be parameterized.
    private static double LINEARIZER_EPSILON = 0.0001;
    private CoordinateReferenceSystem<?> crs;


    @Override
    public Geometry<?> decode(SDOGeometry nativeGeom) {
        if(!accepts(nativeGeom)) {
            throw new IllegalArgumentException( getClass().getSimpleName() + " received object of type " + nativeGeom.getGType() );
        }
        this.crs = determineCRS(nativeGeom);
        return internalDecode(nativeGeom);
    }

    /**
     * Determine the SRID, by considering the srid value to be an EPSG code.
     * @param nativeGeom
     * @return
     */
    CoordinateReferenceSystem<?> getCoordinateReferenceSystem(SDOGeometry nativeGeom) {
        return crs;
    }

     private CoordinateReferenceSystem<?> determineCRS(SDOGeometry nativeGeom){

        final int srid = nativeGeom.getSRID();

        //TODO -- can we detect that fallback is used?
        CoordinateReferenceSystem<?> crs = CrsRegistry
                .getCoordinateReferenceSystemForEPSG(srid, CoordinateReferenceSystems.PROJECTED_2D_METER);

        if (getVerticalDimension(nativeGeom) > 0) {
                crs = CoordinateReferenceSystems.addVerticalSystem(crs, LinearUnit.METER);
        }

        if ( getLinearReferenceDimension(nativeGeom) > 0) {
            crs = CoordinateReferenceSystems.addLinearSystem(crs, LinearUnit.METER);
        }
        return crs;
    }

    int getCoordinateDimension(SDOGeometry nativeGeom){
        return nativeGeom.getGType().getDimension();
    }

    int getLinearReferenceDimension(SDOGeometry nativeGeom) {
        return nativeGeom.getGType().getLRSDimension();
    }

    int getVerticalDimension(SDOGeometry nativeGeom) {
        return nativeGeom.getGType().getZDimension();
    }

    //Note in these and subsequent private methods we pass in the crs so that type inferencing can occur
    protected <P extends Position> PositionSequence<P> convertOrdinateArray(Double[] oordinates, SDOGeometry sdoGeom, CoordinateReferenceSystem<P>  crs) {
        final int dim = sdoGeom.getDimension();
        int numPos = oordinates.length / dim;

        PositionSequenceBuilder<P> sequenceBuilder = fixedSized(numPos, crs.getPositionClass());

        final int zDim = sdoGeom.getZDimension() - 1;
		final int lrsDim = sdoGeom.getLRSDimension() - 1;

        double[] buffer = new double[dim];
        for (int posIdx = 0; posIdx < numPos; posIdx++) {
            int componentIdx = 0; //tracks component in Position
            buffer[componentIdx++] = oordinates[posIdx * dim]; //x
            buffer[componentIdx++] = oordinates[posIdx * dim]; //y
            if (zDim > 0) {
                buffer[componentIdx++] = oordinates[posIdx * dim + zDim];
            }
            if (lrsDim > 0) {
                buffer[componentIdx++] = oordinates[posIdx * dim + lrsDim];
            }

            sequenceBuilder.add(buffer);
        }
        return sequenceBuilder.toPositionSequence();
    }

    protected <P extends Position> PositionSequence<P> add(PositionSequence<P> seq1, PositionSequence<P> seq2) {
        return add(seq1, 0, seq2, 0);
    }

    protected <P extends Position> PositionSequence<P> add(PositionSequence<P> seq1, int seq1Offset, PositionSequence<P> seq2, int seq2Offset) {
        if ( seq1 == null ) {
            return seq2;
        }
        if ( seq2 == null ) {
            return seq1;
        }
        int totalSize = seq1.size() + seq2.size() - seq2Offset;
        PositionSequenceBuilder<P> builder = fixedSized(totalSize, seq1.getPositionClass());
        CombiningVisitor<P> visitor = new CombiningVisitor<P>(builder);

        addToBuilder(seq1, seq1Offset, builder, visitor);
        addToBuilder(seq2, seq2Offset, builder, visitor);
        return builder.toPositionSequence();

    }

    private <P extends Position> void addToBuilder(PositionSequence<P> seq, int skip, PositionSequenceBuilder<P> builder, CombiningVisitor<P> visitor) {
        if (skip == 0) {
            seq.accept(visitor);
        } else {
            for(P pos : seq) {
                if (skip-- <= 0) {
                    builder.add(pos);
                }
            }
        }
    }


    abstract Geometry<?> internalDecode(SDOGeometry nativeGeom);


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

    /**
     * Gets the CoordinateSequence corresponding to a compound element.
     *
     * @param idxFirst the first sub-element of the compound element
     * @param idxLast the last sub-element of the compound element
     * @param sdoGeom the SDOGeometry that holds the compound element.
     *
     * @return
     */
    protected <P extends Position> PositionSequence<P> getCompoundCSeq(int idxFirst, int idxLast, SDOGeometry sdoGeom) {
        PositionSequence<P> cs = null;
        for ( int i = idxFirst; i <= idxLast; i++ ) {
            // pop off the last element as it is added with the next
            // coordinate sequence
            if ( cs != null && cs.size() > 0 ) {
                add( cs, 1, getElementCSeq( i, sdoGeom, ( i < idxLast ) , (CoordinateReferenceSystem<P>) crs) , 0);
            } else {
                cs = add( cs, getElementCSeq( i, sdoGeom, ( i < idxLast ),  (CoordinateReferenceSystem<P>) crs ) );
            }
        }
        return cs;
    }

    /**
     * Gets the CoordinateSequence corresponding to an element.
     *
     * @param i
     * @param sdoGeom
     *
     * @return
     */
    protected <P extends Position> PositionSequence<P> getElementCSeq(int i, SDOGeometry sdoGeom, boolean hasNextSE,  CoordinateReferenceSystem<P> crs) {
        final ElementType type = sdoGeom.getInfo().getElementType( i );
        final Double[] elemOrdinates = extractOrdinatesOfElement( i, sdoGeom, hasNextSE );
        PositionSequence<P> cs;
        if ( type.isStraightSegment() ) {
            cs = convertOrdinateArray( elemOrdinates, sdoGeom, crs);
        } else if ( type.isArcSegment() || type.isCircle() ) {
            cs = linearize(convertOrdinateArray(elemOrdinates, sdoGeom, crs),
                    type.isCircle());
        } else if ( type.isRect() ) {
            cs = convertOrdinateArray( elemOrdinates, sdoGeom, crs );
            cs = env2Seq(cs.getPositionN(0), cs.getPositionN(1), type.isExteriorRing());
        }
        else {
            throw new RuntimeException(
                    "Unexpected Element type in compound: "
                            + type
            );
        }
        return cs;
    }

    private <P extends Position> PositionSequence<P> env2Seq(P p0, P p1, boolean asExteriorRing) {
        Envelope<P> env = new Envelope<P>(p0, p1, (CoordinateReferenceSystem<P>)crs);
        if (asExteriorRing) {
            return fixedSized(5, (Class<P>)crs.getPositionClass())
                    .add(env.lowerLeft())
                    .add(env.lowerRight())
                    .add(env.upperRight())
                    .add(env.upperLeft())
                    .add(env.lowerLeft()).toPositionSequence();
        } else {
            return fixedSized(5, (Class<P>)crs.getPositionClass())
                    .add(env.lowerLeft())
                    .add(env.upperLeft())
                    .add(env.upperRight())
                    .add(env.lowerRight())
                    .add(env.lowerLeft()).toPositionSequence();
        }
    }

    /**
     * Linearizes arcs and circles.
     *
     *
     * @return linearized interpolation of arcs or circle
     */
    protected <P extends Position> PositionSequence<P> linearize(PositionSequence<P> positions, boolean entireCirlce) {
        PositionSequence<P> result = null;
        int idx = 0;

        while ( idx < positions.size()) {
            P p0 = positions.getPositionN(idx++);
            P p1 = positions.getPositionN(idx++);
            P p2 = positions.getPositionN(idx); //dont' increment, we repeat next iteration from this index

            CircularArcLinearizer<P> linearizer = new CircularArcLinearizer<P>(p0, p1, p2,
                    LINEARIZER_EPSILON);


            PositionSequence<P> ps;
            if (entireCirlce) {
                ps = linearizer.linearizeCircle();
            } else {
                ps = linearizer.linearize();
            }


            // if this is not the first arcsegment, the first linearized
            // point is already in linearizedArc, so disregard this.
            if (result == null) { // this is the first iteration
                result = ps;
            } else {
                result = add(result, 0, ps, 1);
            }
        }
        return result;
    }

    protected Double[] extractOrdinatesOfElement(int element, SDOGeometry sdoGeom, boolean hasNextSE) {
        final int start = sdoGeom.getInfo().getOrdinatesOffset( element );
        if ( element < sdoGeom.getInfo().getSize() - 1 ) {
            int end = sdoGeom.getInfo().getOrdinatesOffset( element + 1 );
            // if this is a subelement of a compound geometry,
            // the last point is the first point of
            // the next subelement.
            if ( hasNextSE ) {
                end += sdoGeom.getDimension();
            }
            return sdoGeom.getOrdinates().getOrdinatesArray( start, end );
        }
        else {
            return sdoGeom.getOrdinates().getOrdinatesArray( start );
        }
    }


//    private Geometry convert2JTS(SDOGeometry sdoGeom) {
//        final int dim = sdoGeom.getGType().getDimension();
//        final int lrsDim = sdoGeom.getGType().getLRSDimension();
//        Geometry result = null;
//        switch ( sdoGeom.getGType().getTypeGeometry() ) {
//            case POINT:
//                result = convertSDOPoint( sdoGeom );
//                break;
//            case LINE:
//                result = convertSDOLine( dim, lrsDim, sdoGeom );
//                break;
//            case POLYGON:
//                result = convertSDOPolygon( dim, lrsDim, sdoGeom );
//                break;
//            case MULTIPOINT:
//                result = convertSDOMultiPoint( dim, lrsDim, sdoGeom );
//                break;
//            case MULTILINE:
//                result = convertSDOMultiLine( dim, lrsDim, sdoGeom );
//                break;
//            case MULTIPOLYGON:
//                result = convertSDOMultiPolygon( dim, lrsDim, sdoGeom );
//                break;
//            case COLLECTION:
//                result = convertSDOCollection( dim, lrsDim, sdoGeom );
//                break;
//            default:
//                throw new IllegalArgumentException(
//                        "Type not supported: "
//                                + sdoGeom.getGType().getTypeGeometry()
//                );
//        }
//        result.setSRID( sdoGeom.getSRID() );
//        return result;
//
//    }
//
//    private Geometry convertSDOCollection(int dim, int lrsDim, SDOGeometry sdoGeom) {
//        final List<Geometry> geometries = new ArrayList<Geometry>();
//        for (SDOGeometry elemGeom : sdoGeom.getElementGeometries()) {
//            geometries.add(convert2JTS(elemGeom));
//        }
//        final Geometry[] geomArray = new Geometry[geometries.size()];
//        return getGeometryFactory().createGeometryCollection(geometries.toArray(geomArray));
//    }
//
//    private Point convertSDOPoint(SDOGeometry sdoGeom) {
//        Double[] ordinates = sdoGeom.getOrdinates().getOrdinateArray();
//        if ( ordinates.length == 0 ) {
//            if ( sdoGeom.getDimension() == 2 ) {
//                ordinates = new Double[] {
//                        sdoGeom.getPoint().x,
//                        sdoGeom.getPoint().y
//                };
//            }
//            else {
//                ordinates = new Double[] {
//                        sdoGeom.getPoint().x,
//                        sdoGeom.getPoint().y, sdoGeom.getPoint().z
//                };
//            }
//        }
//        final CoordinateSequence cs = convertOrdinateArray( ordinates, sdoGeom );
//        return getGeometryFactory().createPoint( cs );
//    }
//
//    private MultiPoint convertSDOMultiPoint(int dim, int lrsDim, SDOGeometry sdoGeom) {
//        final Double[] ordinates = sdoGeom.getOrdinates().getOrdinateArray();
//        final CoordinateSequence cs = convertOrdinateArray( ordinates, sdoGeom );
//        final MultiPoint multipoint = getGeometryFactory().createMultiPoint( cs );
//        return multipoint;
//    }
//
//    private LineString convertSDOLine(int dim, int lrsDim, SDOGeometry sdoGeom) {
//        final boolean lrs = sdoGeom.isLRSGeometry();
//        final ElemInfo info = sdoGeom.getInfo();
//        CoordinateSequence cs = null;
//
//        int i = 0;
//        while ( i < info.getSize() ) {
//            if ( info.getElementType( i ).isCompound() ) {
//                final int numCompounds = info.getNumCompounds( i );
//                cs = add( cs, getCompoundCSeq( i + 1, i + numCompounds, sdoGeom ) );
//                i += 1 + numCompounds;
//            }
//            else {
//                cs = add( cs, getElementCSeq( i, sdoGeom, false ) );
//                i++;
//            }
//        }
//
//
//        if ( lrs ) {
//            throw new UnsupportedOperationException();
//        }
//        else {
//            return getGeometryFactory().createLineString( cs );
//        }
//
//    }
//
//    private MultiLineString convertSDOMultiLine(int dim, int lrsDim, SDOGeometry sdoGeom) {
//        final boolean lrs = sdoGeom.isLRSGeometry();
//        if ( lrs ) {
//            throw new UnsupportedOperationException();
//        }
//        final ElemInfo info = sdoGeom.getInfo();
//        final LineString[] lines = new LineString[sdoGeom.getInfo().getSize()];
//        int i = 0;
//        while ( i < info.getSize() ) {
//            CoordinateSequence cs = null;
//            if ( info.getElementType( i ).isCompound() ) {
//                final int numCompounds = info.getNumCompounds( i );
//                cs = add( cs, getCompoundCSeq( i + 1, i + numCompounds, sdoGeom ) );
//                final LineString line = getGeometryFactory().createLineString( cs );
//                lines[i] = line;
//                i += 1 + numCompounds;
//            }
//            else {
//                cs = add( cs, getElementCSeq( i, sdoGeom, false ) );
//                final LineString line = getGeometryFactory().createLineString( cs );
//                lines[i] = line;
//                i++;
//            }
//        }
//
//        return getGeometryFactory().createMultiLineString( lines );
//    }
//
//    private Geometry convertSDOPolygon(int dim, int lrsDim, SDOGeometry sdoGeom) {
//        LinearRing shell = null;
//        final LinearRing[] holes = new LinearRing[sdoGeom.getNumElements() - 1];
//        final ElemInfo info = sdoGeom.getInfo();
//        int i = 0;
//        int idxInteriorRings = 0;
//        while ( i < info.getSize() ) {
//            CoordinateSequence cs = null;
//            int numCompounds = 0;
//            if ( info.getElementType( i ).isCompound() ) {
//                numCompounds = info.getNumCompounds( i );
//                cs = add( cs, getCompoundCSeq( i + 1, i + numCompounds, sdoGeom ) );
//            }
//            else {
//                cs = add( cs, getElementCSeq( i, sdoGeom, false ) );
//            }
//            if ( info.getElementType( i ).isInteriorRing() ) {
//                holes[idxInteriorRings] = getGeometryFactory()
//                        .createLinearRing( cs );
//                idxInteriorRings++;
//            }
//            else {
//                shell = getGeometryFactory().createLinearRing( cs );
//            }
//            i += 1 + numCompounds;
//        }
//        return getGeometryFactory().createPolygon( shell, holes );
//    }
//
//    private MultiPolygon convertSDOMultiPolygon(int dim, int lrsDim, SDOGeometry sdoGeom) {
//        List<LinearRing> holes = new ArrayList<LinearRing>();
//        final List<Polygon> polygons = new ArrayList<Polygon>();
//        final ElemInfo info = sdoGeom.getInfo();
//        LinearRing shell = null;
//        int i = 0;
//        while ( i < info.getSize() ) {
//            CoordinateSequence cs = null;
//            int numCompounds = 0;
//            if ( info.getElementType( i ).isCompound() ) {
//                numCompounds = info.getNumCompounds( i );
//                cs = add( cs, getCompoundCSeq( i + 1, i + numCompounds, sdoGeom ) );
//            }
//            else {
//                cs = add( cs, getElementCSeq( i, sdoGeom, false ) );
//            }
//            if ( info.getElementType( i ).isInteriorRing() ) {
//                final LinearRing lr = getGeometryFactory().createLinearRing( cs );
//                holes.add( lr );
//            }
//            else {
//                if ( shell != null ) {
//                    final Polygon polygon = getGeometryFactory().createPolygon(
//                            shell,
//                            holes.toArray( new LinearRing[holes.size()] )
//                    );
//                    polygons.add( polygon );
//                    shell = null;
//                }
//                shell = getGeometryFactory().createLinearRing( cs );
//                holes = new ArrayList<LinearRing>();
//            }
//            i += 1 + numCompounds;
//        }
//        if ( shell != null ) {
//            final Polygon polygon = getGeometryFactory().createPolygon(
//                    shell,
//                    holes.toArray( new LinearRing[holes.size()] )
//            );
//            polygons.add( polygon );
//        }
//        return getGeometryFactory().createMultiPolygon( polygons.toArray( new Polygon[polygons.size()] ) );
//    }
//
//






}
