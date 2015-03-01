package org.geolatte.geom.codec.db.oracle;

import com.vividsolutions.jts.geom.Coordinate;
import org.geolatte.geom.*;
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


    PositionSequence<?> convertOrdinateArray(Double[] oordinates, SDOGeometry sdoGeom) {
        final int dim = sdoGeom.getDimension();
        int numPos = oordinates.length / dim;

        PositionSequenceBuilder<?> sequenceBuilder = fixedSized(numPos, crs.getPositionClass());

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
        if ( seq1 == null ) {
            return seq2;
        }
        if ( seq2 == null ) {
            return seq1;
        }
        int totalSize = seq1.size() + seq2.size();
        PositionSequenceBuilder<P> builder = fixedSized(totalSize, seq1.getPositionClass());
        CombiningVisitor<P> visitor = new CombiningVisitor<P>(builder);
        seq1.accept(visitor);
        seq2.accept(visitor);
        return visitor.result();
    }



    abstract protected Geometry<?> internalDecode(SDOGeometry nativeGeom);


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
    protected PositionSequence<?> getCompoundCSeq(int idxFirst, int idxLast, SDOGeometry sdoGeom) {
        PositionSequence<?> cs = null;
        for ( int i = idxFirst; i <= idxLast; i++ ) {
            // pop off the last element as it is added with the next
            // coordinate sequence
            if ( cs != null && cs.size() > 0 ) {
                final Coordinate[] coordinates = cs.toCoordinateArray();
                final Coordinate[] newCoordinates = new Coordinate[coordinates.length - 1];
                System.arraycopy( coordinates, 0, newCoordinates, 0, coordinates.length - 1 );
                cs = getGeometryFactory().getCoordinateSequenceFactory().create( newCoordinates );
            }
            cs = add( cs, getElementCSeq( i, sdoGeom, ( i < idxLast ) ) );
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
    private PositionSequence<?> getElementCSeq(int i, SDOGeometry sdoGeom, boolean hasNextSE) {
        final ElementType type = sdoGeom.getInfo().getElementType( i );
        final Double[] elemOrdinates = extractOrdinatesOfElement( i, sdoGeom, hasNextSE );
        PositionSequence<?> cs;
        if ( type.isStraightSegment() ) {
            cs = convertOrdinateArray( elemOrdinates, sdoGeom );
        }
        else if ( type.isArcSegment() || type.isCircle() ) {
            final Coordinate[] linearized = linearize(
                    elemOrdinates,
                    sdoGeom.getDimension(),
                    sdoGeom.isLRSGeometry(),
                    type.isCircle()
            );
            PositionSequenceBuilder<? extends Position> builder = fixedSized(linearized.length, crs
                    .getPositionClass());
            for ( Coordinate c : linearized) {
                builder.add(c)
            }

        }
        else if ( type.isRect() ) {
            cs = convertOrdinateArray( elemOrdinates, sdoGeom );
            final Coordinate ll = cs.getCoordinate( 0 );
            final Coordinate ur = cs.getCoordinate( 1 );
            final Coordinate lr = new Coordinate( ur.x, ll.y );
            final Coordinate ul = new Coordinate( ll.x, ur.y );
            if ( type.isExteriorRing() ) {
                cs = getGeometryFactory().getCoordinateSequenceFactory()
                        .create( new Coordinate[] { ll, lr, ur, ul, ll } );
            }
            else {
                cs = getGeometryFactory().getCoordinateSequenceFactory()
                        .create( new Coordinate[] { ll, ul, ur, lr, ll } );
            }
        }
        else {
            throw new RuntimeException(
                    "Unexpected Element type in compound: "
                            + type
            );
        }
        return cs;
    }

    /**
     * Linearizes arcs and circles.
     *
     * @param arcOrdinates arc or circle coordinates
     * @param dim coordinate dimension
     * @param lrs whether this is an lrs geometry
     * @param entireCirlce whether the whole arc should be linearized
     *
     * @return linearized interpolation of arcs or circle
     */
    private Position[] linearize(Double[] arcOrdinates, int dim, boolean lrs, boolean entireCirlce) {
        Position[] linearizedCoords = new Position[0];
        // CoordDim is the dimension that includes only non-measure (X,Y,Z)
        // ordinates in its value
        final int coordDim = lrs ? dim - 1 : dim;
        // this only works with 2-Dimensional geometries, since we use
        // JGeometry linearization;
        if ( coordDim != 2 ) {
            throw new IllegalArgumentException(
                    "Can only linearize 2D arc segments, but geometry is "
                            + dim + "D."
            );
        }
        int numOrd = dim;
        while ( numOrd < arcOrdinates.length ) {
            numOrd = numOrd - dim;
            final double x1 = arcOrdinates[numOrd++];
            final double y1 = arcOrdinates[numOrd++];
            final double m1 = lrs ? arcOrdinates[numOrd++] : Double.NaN;
            final double x2 = arcOrdinates[numOrd++];
            final double y2 = arcOrdinates[numOrd++];
            final double m2 = lrs ? arcOrdinates[numOrd++] : Double.NaN;
            final double x3 = arcOrdinates[numOrd++];
            final double y3 = arcOrdinates[numOrd++];
            final double m3 = lrs ? arcOrdinates[numOrd++] : Double.NaN;

            Coordinate[] coords;
            if ( entireCirlce ) {
                coords = Circle.linearizeCircle(x1, y1, x2, y2, x3, y3);
            }
            else {
                coords = Circle.linearizeArc( x1, y1, x2, y2, x3, y3 );
            }

            // if this is an LRS geometry, fill the measure values into
            // the linearized array
            if ( lrs ) {
                throw new UnsupportedOperationException();
//				MCoordinate[] mcoord = new MCoordinate[coords.length];
//				int lastIndex = coords.length - 1;
//				mcoord[0] = MCoordinate.create2dWithMeasure( x1, y1, m1 );
//				mcoord[lastIndex] = MCoordinate.create2dWithMeasure( x3, y3, m3 );
//				// convert the middle coordinates to MCoordinate
//				for ( int i = 1; i < lastIndex; i++ ) {
//					mcoord[i] = MCoordinate.convertCoordinate( coords[i] );
//					// if we happen to split on the middle measure, then
//					// assign it
//					if ( Double.compare( mcoord[i].x, x2 ) == 0
//							&& Double.compare( mcoord[i].y, y2 ) == 0 ) {
//						mcoord[i].m = m2;
//					}
//				}
//				coords = mcoord;
            }

            // if this is not the first arcsegment, the first linearized
            // point is already in linearizedArc, so disregard this.
            int resultBegin = 1;
            if ( linearizedCoords.length == 0 ) {
                resultBegin = 0;
            }

            final int destPos = linearizedCoords.length;
            final Coordinate[] tmpCoords = new Coordinate[linearizedCoords.length + coords.length - resultBegin];
            System.arraycopy( linearizedCoords, 0, tmpCoords, 0, linearizedCoords.length );
            System.arraycopy( coords, resultBegin, tmpCoords, destPos, coords.length - resultBegin );
            linearizedCoords = tmpCoords;
        }
        return linearizedCoords;
    }

    private Geometry convert2JTS(SDOGeometry sdoGeom) {
        final int dim = sdoGeom.getGType().getDimension();
        final int lrsDim = sdoGeom.getGType().getLRSDimension();
        Geometry result = null;
        switch ( sdoGeom.getGType().getTypeGeometry() ) {
            case POINT:
                result = convertSDOPoint( sdoGeom );
                break;
            case LINE:
                result = convertSDOLine( dim, lrsDim, sdoGeom );
                break;
            case POLYGON:
                result = convertSDOPolygon( dim, lrsDim, sdoGeom );
                break;
            case MULTIPOINT:
                result = convertSDOMultiPoint( dim, lrsDim, sdoGeom );
                break;
            case MULTILINE:
                result = convertSDOMultiLine( dim, lrsDim, sdoGeom );
                break;
            case MULTIPOLYGON:
                result = convertSDOMultiPolygon( dim, lrsDim, sdoGeom );
                break;
            case COLLECTION:
                result = convertSDOCollection( dim, lrsDim, sdoGeom );
                break;
            default:
                throw new IllegalArgumentException(
                        "Type not supported: "
                                + sdoGeom.getGType().getTypeGeometry()
                );
        }
        result.setSRID( sdoGeom.getSRID() );
        return result;

    }

    private Geometry convertSDOCollection(int dim, int lrsDim, SDOGeometry sdoGeom) {
        final List<Geometry> geometries = new ArrayList<Geometry>();
        for (SDOGeometry elemGeom : sdoGeom.getElementGeometries()) {
            geometries.add(convert2JTS(elemGeom));
        }
        final Geometry[] geomArray = new Geometry[geometries.size()];
        return getGeometryFactory().createGeometryCollection(geometries.toArray(geomArray));
    }

    private Point convertSDOPoint(SDOGeometry sdoGeom) {
        Double[] ordinates = sdoGeom.getOrdinates().getOrdinateArray();
        if ( ordinates.length == 0 ) {
            if ( sdoGeom.getDimension() == 2 ) {
                ordinates = new Double[] {
                        sdoGeom.getPoint().x,
                        sdoGeom.getPoint().y
                };
            }
            else {
                ordinates = new Double[] {
                        sdoGeom.getPoint().x,
                        sdoGeom.getPoint().y, sdoGeom.getPoint().z
                };
            }
        }
        final CoordinateSequence cs = convertOrdinateArray( ordinates, sdoGeom );
        return getGeometryFactory().createPoint( cs );
    }

    private MultiPoint convertSDOMultiPoint(int dim, int lrsDim, SDOGeometry sdoGeom) {
        final Double[] ordinates = sdoGeom.getOrdinates().getOrdinateArray();
        final CoordinateSequence cs = convertOrdinateArray( ordinates, sdoGeom );
        final MultiPoint multipoint = getGeometryFactory().createMultiPoint( cs );
        return multipoint;
    }

    private LineString convertSDOLine(int dim, int lrsDim, SDOGeometry sdoGeom) {
        final boolean lrs = sdoGeom.isLRSGeometry();
        final ElemInfo info = sdoGeom.getInfo();
        CoordinateSequence cs = null;

        int i = 0;
        while ( i < info.getSize() ) {
            if ( info.getElementType( i ).isCompound() ) {
                final int numCompounds = info.getNumCompounds( i );
                cs = add( cs, getCompoundCSeq( i + 1, i + numCompounds, sdoGeom ) );
                i += 1 + numCompounds;
            }
            else {
                cs = add( cs, getElementCSeq( i, sdoGeom, false ) );
                i++;
            }
        }


        if ( lrs ) {
            throw new UnsupportedOperationException();
        }
        else {
            return getGeometryFactory().createLineString( cs );
        }

    }

    private MultiLineString convertSDOMultiLine(int dim, int lrsDim, SDOGeometry sdoGeom) {
        final boolean lrs = sdoGeom.isLRSGeometry();
        if ( lrs ) {
            throw new UnsupportedOperationException();
        }
        final ElemInfo info = sdoGeom.getInfo();
        final LineString[] lines = new LineString[sdoGeom.getInfo().getSize()];
        int i = 0;
        while ( i < info.getSize() ) {
            CoordinateSequence cs = null;
            if ( info.getElementType( i ).isCompound() ) {
                final int numCompounds = info.getNumCompounds( i );
                cs = add( cs, getCompoundCSeq( i + 1, i + numCompounds, sdoGeom ) );
                final LineString line = getGeometryFactory().createLineString( cs );
                lines[i] = line;
                i += 1 + numCompounds;
            }
            else {
                cs = add( cs, getElementCSeq( i, sdoGeom, false ) );
                final LineString line = getGeometryFactory().createLineString( cs );
                lines[i] = line;
                i++;
            }
        }

        return getGeometryFactory().createMultiLineString( lines );
    }

    private Geometry convertSDOPolygon(int dim, int lrsDim, SDOGeometry sdoGeom) {
        LinearRing shell = null;
        final LinearRing[] holes = new LinearRing[sdoGeom.getNumElements() - 1];
        final ElemInfo info = sdoGeom.getInfo();
        int i = 0;
        int idxInteriorRings = 0;
        while ( i < info.getSize() ) {
            CoordinateSequence cs = null;
            int numCompounds = 0;
            if ( info.getElementType( i ).isCompound() ) {
                numCompounds = info.getNumCompounds( i );
                cs = add( cs, getCompoundCSeq( i + 1, i + numCompounds, sdoGeom ) );
            }
            else {
                cs = add( cs, getElementCSeq( i, sdoGeom, false ) );
            }
            if ( info.getElementType( i ).isInteriorRing() ) {
                holes[idxInteriorRings] = getGeometryFactory()
                        .createLinearRing( cs );
                idxInteriorRings++;
            }
            else {
                shell = getGeometryFactory().createLinearRing( cs );
            }
            i += 1 + numCompounds;
        }
        return getGeometryFactory().createPolygon( shell, holes );
    }

    private MultiPolygon convertSDOMultiPolygon(int dim, int lrsDim, SDOGeometry sdoGeom) {
        List<LinearRing> holes = new ArrayList<LinearRing>();
        final List<Polygon> polygons = new ArrayList<Polygon>();
        final ElemInfo info = sdoGeom.getInfo();
        LinearRing shell = null;
        int i = 0;
        while ( i < info.getSize() ) {
            CoordinateSequence cs = null;
            int numCompounds = 0;
            if ( info.getElementType( i ).isCompound() ) {
                numCompounds = info.getNumCompounds( i );
                cs = add( cs, getCompoundCSeq( i + 1, i + numCompounds, sdoGeom ) );
            }
            else {
                cs = add( cs, getElementCSeq( i, sdoGeom, false ) );
            }
            if ( info.getElementType( i ).isInteriorRing() ) {
                final LinearRing lr = getGeometryFactory().createLinearRing( cs );
                holes.add( lr );
            }
            else {
                if ( shell != null ) {
                    final Polygon polygon = getGeometryFactory().createPolygon(
                            shell,
                            holes.toArray( new LinearRing[holes.size()] )
                    );
                    polygons.add( polygon );
                    shell = null;
                }
                shell = getGeometryFactory().createLinearRing( cs );
                holes = new ArrayList<LinearRing>();
            }
            i += 1 + numCompounds;
        }
        if ( shell != null ) {
            final Polygon polygon = getGeometryFactory().createPolygon(
                    shell,
                    holes.toArray( new LinearRing[holes.size()] )
            );
            polygons.add( polygon );
        }
        return getGeometryFactory().createMultiPolygon( polygons.toArray( new Polygon[polygons.size()] ) );
    }



    private CoordinateSequence add(CoordinateSequence seq1, CoordinateSequence seq2) {
        if ( seq1 == null ) {
            return seq2;
        }
        if ( seq2 == null ) {
            return seq1;
        }
        final Coordinate[] c1 = seq1.toCoordinateArray();
        final Coordinate[] c2 = seq2.toCoordinateArray();
        final Coordinate[] c3 = new Coordinate[c1.length + c2.length];
        System.arraycopy( c1, 0, c3, 0, c1.length );
        System.arraycopy( c2, 0, c3, c1.length, c2.length );
        return getGeometryFactory().getCoordinateSequenceFactory().create( c3 );
    }

    private Double[] extractOrdinatesOfElement(int element, SDOGeometry sdoGeom, boolean hasNextSE) {
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





    public GeometryFactory getGeometryFactory() {
        return geometryFactory;
    }

    public void setGeometryFactory(GeometryFactory geometryFactory) {
        this.geometryFactory = geometryFactory;
    }


}
