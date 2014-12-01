/*
 * This file is part of the GeoLatte project.
 *
 *     GeoLatte is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Lesser General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     GeoLatte is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Lesser General Public License for more details.
 *
 *     You should have received a copy of the GNU Lesser General Public License
 *     along with GeoLatte.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright (C) 2010 - 2013 and Ownership of code is shared by:
 * Qmino bvba - Romeinsestraat 18 - 3001 Heverlee  (http://www.qmino.com)
 * Geovise bvba - Generaal Eisenhowerlei 9 - 2140 Antwerpen (http://www.geovise.com)
 */

package org.geolatte.geom.curve;

import org.geolatte.geom.Envelope;
import org.geolatte.geom.Geometry;
import org.geolatte.geom.C2D;
import org.geolatte.geom.Position;
import org.geolatte.geom.crs.CoordinateReferenceSystem;

import java.util.regex.Pattern;

/**
 * Calculates the Morton code (Morton-order or Z-order) of Geometries
 *
 * <p>Morton codes are labels for the nodes of a QuadTree. A QuadTree is a partition of a spatial extent by recursively
 * decomposing it into four equal quadrants. A QuadTree is determined by a spatial extent and depth of the tree (the
 * number of recursive subdivisions of the extent). Both are specified by the {@code MortonContext} passed during
 * construction of instances of this class.</p>
 *
 * <p>The Morton code of a {@code Geometry} can be viewed as a path to the quadrant containing the envelope of that
 * {@code Geometry}. The left-most character of the code contains the label of the quadrant at depth 1, the second at
 * depth 2, etc. If the Morton code is the empty string, then the envelope fits in no single quandrant of the QuadTree.
 * </p>
 *
 * <p>At each level the four quadrants are labeled:</p>
 * <ul>
 * <li>O : lower-left quadrant</li>
 * <li>1 : upper-left quadrant</li>
 * <li>2 : lower-right quadrant</li>
 * <li>3 : upper-right quadrant</li>
 * <p/>
 * </ul>
 *
 * @author Karel Maesen, Geovise BVBA
 *         creation-date: 2/19/13
 */
public class MortonCode<P extends C2D> {

    private final Pattern VALID_MORTONCODE_PATTERN = Pattern.compile("[0,1,2,3]*");
    private final MortonContext<P> mortonContext;
    /**
     * The width of the leaves of the quadtree implied by the MortonContext
     */
    private final double gridWidth;
    /**
     * The height of the leaves of the quadtree implied by the MortonContext
     */
    private final double gridHeight;
    private final int maxGridCellCoordinate;

    /**
     * Constructs an instance with the given {@code Mortoncontext}
     * <p/>
     * <p>The specified {@code MortonCode} determines a QuadTree for which
     * this instance calculates labels.
     *
     * @param mortonContext the context to  use when calculating morton codes.
     */
    public MortonCode(MortonContext<P> mortonContext) {
        this.mortonContext = mortonContext;
        gridWidth = mortonContext.getLeafWidth();
        gridHeight = mortonContext.getLeafHeight();
        this.maxGridCellCoordinate = mortonContext.getNumberOfDivisionsAlongAxis() - 1;
    }

    /**
     * Returns the Morton code for the specified {@code Geometry}.
     * <p/>
     * <p>This method is equivalent to {@code ofEnvelope(geometry.getEnvelope())}.
     *
     * @param geometry a {code Geometry} value.
     * @return the morton code for the envelope of the specified {@code Geometry}.
     * @throws IllegalArgumentException if the geometry is null, or has an envelope which is not contained in
     *                                  the spatial extent of this instance's {@code MortonContext}
     */
    public String ofGeometry(Geometry<P> geometry) {
        checkForNull(geometry);
        return ofEnvelope(geometry.getEnvelope());
    }

    /**
     * Returns the Morton code for the specified {@code Envelope}.
     *
     * @param envelope an {@code Envelope} value.
     * @return the morton code for the specified {@code Envelope} value.
     * @throws IllegalArgumentException if the value of the envelope parameter is null, or is not contained in
     *                                  the spatial extent of this instance's {@code MortonContext}
     */
    public String ofEnvelope(Envelope<P> envelope) {
        checkForNull(envelope);
        checkWithinExtent(envelope);

        // recalculate the X,Y coordinates to grid-cell coordinates. These are
        // the row,column-indices of grid formed by the (lowest-level) leaves
        // of the Quadtree
        int colMin = getCol(envelope.lowerLeft().getCoordinate(0));
        int rowMin = getRow(envelope.lowerLeft().getCoordinate(1));
        int colMax = getCol(envelope.upperRight().getCoordinate(0));
        int rowMax = getRow(envelope.upperRight().getCoordinate(1));
        int[] cols = {colMin, colMax};
        int[] rows = {rowMin, rowMax};

        // interleave the binary representation of the grid-cell coordinates
        long[] interLeaved = {0L, 0L};
        for (int i = 0; i < 2; i++) {
            interLeaved[i] = interleave(cols[i], rows[i]);
        }
        //return the common prefix
        return commonMortonCodePrefixAsString(interLeaved[0], interLeaved[1]);
    }

    /**
     * Returns the Morton code for the specified {@code Point}.
     *
     * @param pos an {@code Point} value.
     * @return the morton code for the specified {@code Point} value.
     * @throws IllegalArgumentException if the value of the point parameter is null, or is not contained in
     *                                  the spatial extent of this instance's {@code MortonContext}
     */
    public String ofPosition(P pos) {
        //check inputs
        checkForNull(pos);
        checkWithinExtent(pos);
        int col = getCol(pos.getX());
        int row = getRow(pos.getY());
        long interleaved = interleave(col, row);
        return pointMortonCodeAsString(interleaved);
    }

    /**
     * Returns the maximum length of a morton code generated by this instance.
     * @return the maximum length of a morton code generated by this instance.
     */
    public int getMaxLength() {
        return this.mortonContext.getDepth();
    }

    /**
     * Returns the extent that corresponds to the specified morton code
     * @param mortoncode a morton code String
     * @return the {@code} envelope representing the specified morton code
     */
    public Envelope<P> envelopeOf(String mortoncode) {
        if (mortoncode == null || ! isValidMortonCode(mortoncode)) {
            throw new IllegalArgumentException(String.format(
                    "Parameter %s is not a valid mortoncode with max. depth %d.", mortoncode, mortonContext.getDepth()));
        }
        return envelopeOf(mortoncode, 0, mortonContext.getExtent());
    }

    /*
    A recursive procedure for calculating the envelope of a mortoncode.
    We recurse on index into the morton code (in this way we do not have to take apart the mortoncode String)
     */
    private Envelope<P> envelopeOf(String mortoncode, int index, Envelope<P> extent) {
        assert (extent != null);
        if (index >= mortoncode.length()) {
            return extent;
        }
        char c = mortoncode.charAt(index);
        double minX = extent.lowerLeft().getCoordinate(0);
        double minY = extent.lowerLeft().getCoordinate(1);
        double w = (extent.extentAlongDimension(0)) / 2.0;
        double h = (extent.extentAlongDimension(1)) / 2.0;
        CoordinateReferenceSystem<P> crs = extent.getCoordinateReferenceSystem();
        switch (c) {
            case '0':
                return envelopeOf(mortoncode, ++index, new Envelope<P>(minX, minY, minX + w, minY + h, crs));
            case '1':
                return envelopeOf(mortoncode, ++index, new Envelope<P>(minX, minY + h, minX + w, minY + 2 * h, crs));
            case '2':
                return envelopeOf(mortoncode, ++index, new Envelope<P>(minX + w, minY, minX + 2 * w, minY + h, crs));
            case '3':
                return envelopeOf(mortoncode, ++index, new Envelope<P>(minX + w, minY + h, minX + 2 * w, minY + 2 * h, crs));
            default:
                //this can only be a programming error!
                throw new IllegalStateException("Received a mortoncode element that is not 0, 1, 2 or 3.");
        }
    }

    private boolean isValidMortonCode(String mortoncode) {
        return (mortoncode.length() <= mortonContext.getDepth()) &&
                VALID_MORTONCODE_PATTERN.matcher(mortoncode).matches();
    }

    private int getRow(double y) {
        int col = (int) Math.floor((y - mortonContext.getMinY()) / gridHeight);
        //if col > mortonContext.getMaxGridNum(), then it should fall in the last column
        // this happens only for coordinates that are exactly equal mortonContext.getMaxY()
        // since we test for containment in the extent.
        return col > maxGridCellCoordinate ? col - 1 : col;
    }

    private int getCol(double x) {
        int row = (int) Math.floor((x - mortonContext.getMinX()) / gridWidth);
        //if col > mortonContext.getMaxGridNum(), then it should fall in the last row
        // this happens only for coordinates that are exactly equal mortonContext.getMaxX()
        // since we test for containment in the extent.
        return row > maxGridCellCoordinate ? row - 1 : row;
    }

    private void checkWithinExtent(P pos) {
        if (!mortonContext.extentContains(pos)) {
            throw new IllegalArgumentException("Point not in extent of this MortonCodeContext.");
        }
    }

    private void checkWithinExtent(Envelope<P> envelope) {
        if (!mortonContext.extentContains(envelope)) {
            throw new IllegalArgumentException("Geometry envelope not in extent of this MortonCodeContext.");
        }
    }

    //interleaves the bits of col and row integer coordinates.
    //this is also a mortoncode
    private long interleave(int col, int row) {
        long interleaved = 0L;
        //we only need to interleave up to depth of the tree
        for (int i = 0; i < mortonContext.getDepth(); i++) {
            interleaved |= ((row & (1 << i)) << i) | ((col & (1 << i)) << (i + 1));
        }
        return interleaved;
    }

    private String pointMortonCodeAsString(long interleaved) {
        //level is in this case the depth of the QuadTree.
        return toRadix4String(interleaved, mortonContext.getDepth());
    }

    /**
     * Returns the common prefix of two morton codes.
     * <p/>
     * <p>The common prefix is the morton code for the quadtree node that
     * is the common ancestor node for the nodes specified by the argument morton codes.</p>
     *
     * @param mc1 first mortonCode (as an interleaved long)
     * @param mc2 second mortonCode
     * @return the common prefix of the specified morton codes
     */
    private String commonMortonCodePrefixAsString(long mc1, long mc2) {
        int commonPrefixLength = 0;
        int firstSignificantBit = mortonContext.getDepth() * 2;
        //the bit-mask for the label (in base-4) of the node at a certain tree-depth
        // we start at the max depth, so first shift the mask to the firstSignificantBit
        long mask = 3 << firstSignificantBit - 2;
        for (int level = 1; level <= mortonContext.getDepth(); level++) {
            // if the base-4 bits when XOR-ed equal to 0 they are the same
            if (((mc1 & mask) ^ (mc2 & mask)) != 0) {
                break;
            }
            commonPrefixLength = level;
            mask >>= 2; //right-shift the mask;
        }
        //drop all bits to the left of commonPrefixLength * 2
        mc1 >>= firstSignificantBit - (commonPrefixLength * 2);
        return toRadix4String(mc1, commonPrefixLength);
    }

    /**
     * Transforms the morton code long value into a string such that each character is a label for the quadrant.
     * <p/>
     * (note: {@code Long.toString(interleaved, 4)} was not used since this turns morton code '001' into '1')
     *
     * @param interleaved the morton code as a long
     * @param level       the level of the morton code
     * @return  a String representation of the MortonCode
     */
    private String toRadix4String(long interleaved, int level) {
        char[] cbuf = new char[level];
        for (int pos = level - 1; pos >= 0; pos--) {
            int label = (int) interleaved & 3; // the label at tree-depth of pos;
            cbuf[pos] = (char) ('0' + label);
            interleaved >>= 2;
        }
        return String.valueOf(cbuf);
    }

    private void checkForNull(Object object) {
        if (object == null) {
            throw new IllegalArgumentException("Null geometry is not allowed.");
        }
    }

}
