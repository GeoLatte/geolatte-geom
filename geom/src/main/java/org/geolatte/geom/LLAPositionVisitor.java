package org.geolatte.geom;

/**
 * A low-level access Position Visitor.
 *
 * <p>This visitor is for high performance applications. The PositionSequence that accepts the visitor passes each
 * coordinate as an array.</p>
 *
 * @see org.geolatte.geom.PositionSequence#accept(LLAPositionVisitor)
 *
 * @author Karel Maesen, Geovise BVBA
 *         creation-date: 3/4/14
 */
public interface LLAPositionVisitor {


    /**
     * The visit method that is executed for each coordinate.
     *
     * @param coordinate the visited coordinate in array representation
     *
     */
    public void visit(double[] coordinate);

}
