package org.geolatte.geom.codec;

import org.geolatte.geom.ByteBuffer;
import org.geolatte.geom.Point;
import org.geolatte.geom.Position;
import org.geolatte.geom.PositionSequence;
import org.geolatte.geom.crs.CoordinateReferenceSystem;

public class PostgisWkb2Decoder extends  PostgisWkbDecoder{

//    @Override
//    protected <P extends Position> Point<P> decodePoint(ByteBuffer byteBuffer, CoordinateReferenceSystem<P> crs) {
//        PositionSequence<P> points = readPositions(1, byteBuffer, crs);
//        //check if the coordinates are (NaN, NaN)
//        P pos = points.first();
//        if(Double.isNaN(pos.getCoordinate(0))) {
//            return new Point<P>(crs);
//        }
//        return new Point<P>(points, crs);
//    }
}
