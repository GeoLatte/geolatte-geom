package org.geolatte.geom.cga;

import java.util.Arrays;

/**
 * A Dimensionally Extended Nine-Intersection Model (DE-9IM) matrix, as defined in the OGC Simple Feature Access
 * specification (Part 1, Sec. 6.1.15).
 *
 * <p>Entry (L<sub>A</sub>, L<sub>B</sub>) is the dimension of the intersection of the part of geometry A at
 * location L<sub>A</sub> with the part of geometry B at location L<sub>B</sub>: {@link #FALSE} (-1) if the
 * intersection is empty, 0, 1 or 2 otherwise. The matrix also records the dimensions of A and B, which are needed
 * for the {@code crosses}, {@code overlaps} and {@code touches} predicates.</p>
 *
 * <p>Instances are immutable, and are computed by {@link Relate#relate}.</p>
 */
public final class IntersectionMatrix {

    /**
     * The value of an entry for an empty intersection
     */
    public static final int FALSE = -1;

    private final int[] entries;   // row-major, rows: location in A, columns: location in B
    private final int dimensionA, dimensionB;

    /**
     * Creates a matrix
     *
     * @param entries    the 9 entries in row-major order (II, IB, IE, BI, BB, BE, EI, EB, EE), each -1, 0, 1 or 2
     * @param dimensionA the dimension of geometry A (-1 if A is empty)
     * @param dimensionB the dimension of geometry B (-1 if B is empty)
     */
    public IntersectionMatrix(int[] entries, int dimensionA, int dimensionB) {
        if (entries.length != 9) throw new IllegalArgumentException("A matrix has 9 entries");
        for (int e : entries) {
            if (e < FALSE || e > 2) throw new IllegalArgumentException("Invalid entry: " + e);
        }
        if (dimensionA < -1 || dimensionA > 2 || dimensionB < -1 || dimensionB > 2) {
            throw new IllegalArgumentException("Invalid dimension");
        }
        this.entries = entries.clone();
        this.dimensionA = dimensionA;
        this.dimensionB = dimensionB;
    }

    /**
     * @return the dimension of the intersection of the part of A at location a, and the part of B at location b;
     * {@link #FALSE} if it is empty
     */
    public int get(Location a, Location b) {
        return entries[3 * a.ordinal() + b.ordinal()];
    }

    /**
     * @return the dimension of geometry A, or -1 if A is empty
     */
    public int getDimensionA() {
        return dimensionA;
    }

    /**
     * @return the dimension of geometry B, or -1 if B is empty
     */
    public int getDimensionB() {
        return dimensionB;
    }

    /**
     * Matches this matrix against a pattern of 9 characters in row-major order. Each character is {@code T} (the
     * entry is not {@link #FALSE}), {@code F} (the entry is {@link #FALSE}), {@code *} (any entry), or {@code 0},
     * {@code 1} or {@code 2} (the entry has this dimension).
     *
     * @param pattern the pattern, e.g. {@code "T*F**F***"}
     * @return true if every entry matches its pattern character
     * @throws IllegalArgumentException if the pattern is not valid
     */
    public boolean matches(String pattern) {
        if (pattern == null || pattern.length() != 9) {
            throw new IllegalArgumentException("A pattern has 9 characters: " + pattern);
        }
        boolean result = true;
        for (int i = 0; i < 9; i++) {
            result &= matches(entries[i], pattern.charAt(i));
        }
        return result;
    }

    private static boolean matches(int entry, char c) {
        switch (Character.toUpperCase(c)) {
            case 'T':
                return entry != FALSE;
            case 'F':
                return entry == FALSE;
            case '*':
                return true;
            case '0':
            case '1':
            case '2':
                return entry == c - '0';
            default:
                throw new IllegalArgumentException("Invalid pattern character: " + c);
        }
    }

    // ---------------------------------------------------------------------------------------------------------
    // named predicates
    // ---------------------------------------------------------------------------------------------------------

    /**
     * @return true if A and B have no point in common ({@code FF*FF****})
     */
    public boolean isDisjoint() {
        return !t(II) && !t(IB) && !t(BI) && !t(BB);
    }

    /**
     * @return true if A and B have at least one point in common
     */
    public boolean isIntersects() {
        return !isDisjoint();
    }

    /**
     * @return true if A and B have a point in common, but their interiors do not intersect ({@code FT*******},
     * {@code F**T*****} or {@code F***T****}); false if both are points
     */
    public boolean isTouches() {
        if (dimensionA == 0 && dimensionB == 0) return false;
        return !t(II) && (t(IB) || t(BI) || t(BB));
    }

    /**
     * @return true if A crosses B: {@code T*T******} if dim(A) &lt; dim(B), {@code T*****T**} if dim(A) &gt;
     * dim(B), {@code 0********} if both are lines; false otherwise
     */
    public boolean isCrosses() {
        if (dimensionA < 0 || dimensionB < 0) return false;
        if (dimensionA == 1 && dimensionB == 1) return entries[II] == 0;
        if (dimensionA < dimensionB) return t(II) && t(IE);
        if (dimensionA > dimensionB) return t(II) && t(EI);
        return false;
    }

    /**
     * @return true if A lies within B ({@code T*F**F***})
     */
    public boolean isWithin() {
        return t(II) && !t(IE) && !t(BE);
    }

    /**
     * @return true if A contains B ({@code T*****FF*})
     */
    public boolean isContains() {
        return t(II) && !t(EI) && !t(EB);
    }

    /**
     * @return true if every point of B is a point of A ({@code T*****FF*}, {@code *T****FF*},
     * {@code ***T**FF*} or {@code ****T*FF*})
     */
    public boolean isCovers() {
        return isIntersects() && !t(EI) && !t(EB);
    }

    /**
     * @return true if every point of A is a point of B ({@code T*F**F***}, {@code *TF**F***},
     * {@code **FT*F***} or {@code **F*TF***})
     */
    public boolean isCoveredBy() {
        return isIntersects() && !t(IE) && !t(BE);
    }

    /**
     * @return true if A and B are topologically equal: they have the same dimension and {@code T*F**FFF*}; two
     * empty geometries are also equal
     */
    public boolean isEquals() {
        if (dimensionA != dimensionB) return false;
        if (dimensionA < 0) return true;
        return t(II) && !t(IE) && !t(BE) && !t(EI) && !t(EB);
    }

    /**
     * @return true if A and B overlap: they have the same dimension and {@code T*T***T**} (points and areas), or
     * {@code 1*T***T**} (lines)
     */
    public boolean isOverlaps() {
        if (dimensionA != dimensionB || dimensionA < 0) return false;
        boolean interiors = dimensionA == 1 ? entries[II] == 1 : t(II);
        return interiors && t(IE) && t(EI);
    }

    /**
     * @return the transposed matrix: the matrix of B relative to A
     */
    public IntersectionMatrix transpose() {
        int[] t = new int[9];
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) t[3 * j + i] = entries[3 * i + j];
        }
        return new IntersectionMatrix(t, dimensionB, dimensionA);
    }

    private static final int II = 0, IB = 1, IE = 2, BI = 3, BB = 4, BE = 5, EI = 6, EB = 7;

    private boolean t(int index) {
        return entries[index] != FALSE;
    }

    /**
     * @return the entries as a string of 9 characters in row-major order, e.g. {@code "212101212"}
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(9);
        for (int e : entries) sb.append(e == FALSE ? 'F' : (char) ('0' + e));
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof IntersectionMatrix)) return false;
        IntersectionMatrix other = (IntersectionMatrix) o;
        return dimensionA == other.dimensionA && dimensionB == other.dimensionB
                && Arrays.equals(entries, other.entries);
    }

    @Override
    public int hashCode() {
        return 31 * (31 * Arrays.hashCode(entries) + dimensionA) + dimensionB;
    }
}
