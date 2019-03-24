package org.geolatte.geom.crs.trans;

import org.geolatte.geom.crs.*;
import org.geolatte.geom.crs.trans.projections.Projections;

import static org.geolatte.geom.crs.CoordinateReferenceSystems.WGS84;

/**
 * Factory for common ConcatenatedOperations
 * Created by Karel Maesen, Geovise BVBA on 02/04/2018.
 */
public class CoordinateOperations {

    /**
     * Returns the Position Vector transformation for the geographic 2D domain.
     * <p>
     * It first transforms from the source CRS to WGS84, and then from WGS84 to the target CRS.
     * <p>
     * This transformation uses EPSG method 9606
     *
     * @return a Transformation conforming to EPSG method 9606
     */
    static public CoordinateOperation positionVectorTransformation2D(GeographicCoordinateReferenceSystem source, GeographicCoordinateReferenceSystem target) {
        CoordinateOperation datumTransformation = positionVectorTransformation(source.getDatum(), target.getDatum());
            return new ConcatenatedOperation.Builder()
                    .reverse(new Geographic3DTo2DConversion())
                    .forward(new GeographicToGeocentricConversion(source))
                    .forward(datumTransformation)
                    .reverse(new GeographicToGeocentricConversion(target))
                    .forward(new Geographic3DTo2DConversion())
                    .build();

    }

//    /**
//     * Returns the Position Vector transformation for the geographic 3D domain.
//     * <p>
//     * It first transforms from the source CRS to WGS84, and then from WGS84 to the target CRS.
//     * <p>
//     * This transformation uses EPSG method 1037
//     *
//     * @return a Transformation conforming to EPSG method 1037
//     */
//    static public CoordinateOperation positionVectorTransformation3D(GeographicCoordinateReferenceSystem source, GeographicCoordinateReferenceSystem target) {
//        CoordinateOperation datumTransformation = positionVectorTransformation(source.getDatum(), target.getDatum());
//        return new ConcatenatedOperation.Builder()
//                .forward(new GeographicToGeocentricConversion(source))
//                .forward(datumTransformation)
//                .reverse(new GeographicToGeocentricConversion(target))
//                .build();
//    }

    static public CoordinateOperation positionVectorTransformation(Datum source, Datum target) {
        if (source == target) {
            return new IdentityOp(3);
        } else if (WGS84.getDatum().equals(source)) {
            if(target.getToWGS84().length == 0) throw new IllegalArgumentException("Source and target datums must have a ToWGS84 parameters ");
            return PositionVectorTransformation.fromTOWGS84(target.getToWGS84()).reverse();
        } else if (WGS84.getDatum().equals(target)) {
            if(source.getToWGS84().length == 0) throw new IllegalArgumentException("Source and target datums must have a ToWGS84 parameters ");
            return PositionVectorTransformation.fromTOWGS84(source.getToWGS84());
        } else {
            if(source.getToWGS84().length == 0 || target.getToWGS84().length == 0)
                throw new IllegalArgumentException("Source and target datums must have a ToWGS84 parameters ");
                return PositionVectorTransformation.fromTOWGS84(source.getToWGS84())
                        .appendReverse(PositionVectorTransformation.fromTOWGS84(target.getToWGS84()));
        }
    }

    /**
     * Creates a transformation from source to target CRS's
     * @param sourceId the source CRS
     * @param targetId the target CRS
     * @return a CoordinateOperation whose forward operation transforms source to target
     * @throws UnsupportedTransformException when no transform could be determined
     */
    static public CoordinateOperation transform(CrsId sourceId, CrsId targetId) {

        //TODO -- simplify this method and clean up
        CoordinateReferenceSystem<?> source = CrsRegistry.getCoordinateReferenceSystem(sourceId, null);
        CoordinateReferenceSystem<?> target = CrsRegistry.getCoordinateReferenceSystem(targetId, null);
        if (source == null) throw new UnsupportedCoordinateReferenceSystem(sourceId);
        if (target == null) throw new UnsupportedCoordinateReferenceSystem(targetId);
        ConcatenatedOperation.Builder builder = new ConcatenatedOperation.Builder();
        GeographicCoordinateReferenceSystem sourceGeodetic;

        if (source instanceof ProjectedCoordinateReferenceSystem) {
            ProjectedCoordinateReferenceSystem projectedSource = (ProjectedCoordinateReferenceSystem)source;
            builder.reverse( Projections.buildFrom(projectedSource) ); // "unproject" to datum
            sourceGeodetic = projectedSource.getGeographicCoordinateSystem();
        } else if (source instanceof GeographicCoordinateReferenceSystem) {
            sourceGeodetic = ((GeographicCoordinateReferenceSystem)source);
        } else {
            throw new UnsupportedTransformException(sourceId, targetId);
        }

        GeographicCoordinateReferenceSystem targetGeodetic;
        if (target instanceof GeographicCoordinateReferenceSystem) {
            targetGeodetic = ((GeographicCoordinateReferenceSystem) target);
            if (!sourceGeodetic.equals(targetGeodetic)) {
                builder.forward(positionVectorTransformation2D(sourceGeodetic, targetGeodetic));
            }
        } else if (target instanceof ProjectedCoordinateReferenceSystem) {
            ProjectedCoordinateReferenceSystem projectedTarget = (ProjectedCoordinateReferenceSystem)target;
            targetGeodetic = projectedTarget.getGeographicCoordinateSystem();
            CoordinateOperation forwardOp = Projections.buildFrom(projectedTarget);// "project" from datum
            if (!sourceGeodetic.equals(targetGeodetic)) {
                builder.forward(positionVectorTransformation2D(sourceGeodetic, targetGeodetic));
            }
            builder.forward(forwardOp);
        } else {
            throw new UnsupportedTransformException(sourceId, targetId);
        }
        return builder.build();
    }



    static public CoordinateOperation identity(int dim){
        return new IdentityOp(dim);
    }

    private static class IdentityOp implements CoordinateOperation{
        final private int dimension;
        IdentityOp(int dimension){
            this.dimension = dimension;
        }

        @Override
        public boolean isReversible() {
            return true;
        }

        @Override
        public int inCoordinateDimension() {
            return dimension;
        }

        @Override
        public int outCoordinateDimension() {
            return dimension;
        }

        @Override
        public void forward(double[] inCoordinate, double[] outCoordinate) {
            System.arraycopy(inCoordinate, 0, outCoordinate, 0, dimension);
        }

        @Override
        public void reverse(double[] inCoordinate, double[] outCoordinate) {
            forward(inCoordinate, outCoordinate);
        }
    };
}
