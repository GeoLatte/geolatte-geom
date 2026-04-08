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
 */

package org.geolatte.geom.crs;

import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.geolatte.geom.crs.CoordinateReferenceSystems.addLinearSystem;
import static org.geolatte.geom.crs.CoordinateReferenceSystems.addVerticalSystem;
import static org.geolatte.geom.crs.CoordinateReferenceSystems.adjustTo;
import static org.geolatte.geom.crs.CoordinateReferenceSystems.mkProjected;
import static org.geolatte.geom.crs.Unit.METER;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

/**
 * Specification tests for {@link CoordinateReferenceSystems#adjustTo} and its overloads.
 *
 * <p>These tests encode the contract documented on {@code adjustTo}: the canonical form is
 * bidirectional (it both adds and strips Z/M axes) and the convenience overload that takes a
 * coordinate-count derives the {@code (hasZ, hasM)} target shape from {@code (dim, hasM)} per
 * the documented mapping.
 *
 * <p>Several of these tests intentionally fail against the current implementation, which is
 * one-directional (widen-only). They lock in the spec the implementation needs to satisfy.
 *
 * <h4>Test isolation</h4>
 * {@code adjustTo} caches results in {@link CrsRegistry}, which is global state. Tests that
 * shared a base CRS would contaminate each other's cache slots, hiding spec violations behind
 * results that an earlier test happened to populate. To eliminate this, every test method gets
 * a fresh synthetic projected base CRS with a unique {@code (TEST, n)} {@link CrsId}, so its
 * cache slots are guaranteed virgin at the start of the test.
 *
 * <p>Each test starts from four fixtures sharing one synthetic horizontal base:
 * <ul>
 *   <li>{@code base2D}  — XY    (the synthetic projected CRS itself)</li>
 *   <li>{@code base3D}  — XYZ   (compound, built via {@code addVerticalSystem})</li>
 *   <li>{@code base2DM} — XYM   (compound, built via {@code addLinearSystem})</li>
 *   <li>{@code base3DM} — XYZM  (compound, both axes added)</li>
 * </ul>
 */
public class AdjustToSpecTest {

    private static final AtomicInteger TEST_BASE_COUNTER = new AtomicInteger(900_000);

    private CrsId baseId;
    private CoordinateReferenceSystem<?> base2D;
    private CoordinateReferenceSystem<?> base3D;
    private CoordinateReferenceSystem<?> base2DM;
    private CoordinateReferenceSystem<?> base3DM;

    @Before
    public void setUp() {
        baseId = new CrsId("TEST", TEST_BASE_COUNTER.incrementAndGet());
        base2D = mkProjected(baseId, METER);
        // Register so canonical "strip → 2D base" returns this very instance.
        CrsRegistry.registerCoordinateReferenceSystem(base2D);
        base3D = addVerticalSystem(base2D, METER);
        base2DM = addLinearSystem(base2D, METER);
        base3DM = addLinearSystem(addVerticalSystem(base2D, METER), METER);
    }

    private void assertShape(String label,
                             CoordinateReferenceSystem<?> result,
                             boolean expectedHasZ,
                             boolean expectedHasM) {
        assertEquals(label + ": horizontal base CrsId", baseId, result.getCrsId());
        assertEquals(label + ": hasZ", expectedHasZ, result.hasZ());
        assertEquals(label + ": hasM", expectedHasM, result.hasM());
    }

    // ---------------------------------------------------------------------
    // Canonical form: adjustTo(crs, hasZ, hasM)
    // ---------------------------------------------------------------------

    // ----- already-correct shape: shape preserved, base preserved -----
    //
    // For base2D the input itself is the canonical instance we registered, so we can
    // additionally assert same-instance. For the compound fixtures (base3D, base2DM, base3DM)
    // we built them ad-hoc via addVerticalSystem/addLinearSystem, so they may or may not be
    // the canonical instance for their shape — adjustTo is allowed (and expected) to return
    // the canonical equivalent from CrsRegistry instead. We therefore only assert shape
    // preservation here, and let canonicalisation_* tests pin down the identity rules.

    @Test
    public void canonical_2D_to_2D_isSameInstance() {
        assertSame(base2D, adjustTo(base2D, false, false));
    }

    @Test
    public void canonical_3D_to_3D_preservesShape() {
        assertShape("3D→3D", adjustTo(base3D, true, false), true, false);
    }

    @Test
    public void canonical_2DM_to_2DM_preservesShape() {
        assertShape("2DM→2DM", adjustTo(base2DM, false, true), false, true);
    }

    @Test
    public void canonical_3DM_to_3DM_preservesShape() {
        assertShape("3DM→3DM", adjustTo(base3DM, true, true), true, true);
    }

    // ----- widen direction: add the missing axes -----

    @Test
    public void canonical_widen_2D_to_3D() {
        assertShape("2D→3D", adjustTo(base2D, true, false), true, false);
    }

    @Test
    public void canonical_widen_2D_to_2DM() {
        assertShape("2D→2DM", adjustTo(base2D, false, true), false, true);
    }

    @Test
    public void canonical_widen_2D_to_3DM() {
        assertShape("2D→3DM", adjustTo(base2D, true, true), true, true);
    }

    @Test
    public void canonical_widen_3D_to_3DM() {
        assertShape("3D→3DM", adjustTo(base3D, true, true), true, true);
    }

    @Test
    public void canonical_widen_2DM_to_3DM() {
        assertShape("2DM→3DM", adjustTo(base2DM, true, true), true, true);
    }

    // ----- strip direction: remove unwanted axes (the bug fix) -----

    @Test
    public void canonical_strip_3D_to_2D() {
        assertShape("3D→2D", adjustTo(base3D, false, false), false, false);
    }

    @Test
    public void canonical_strip_2DM_to_2D() {
        assertShape("2DM→2D", adjustTo(base2DM, false, false), false, false);
    }

    @Test
    public void canonical_strip_3DM_to_2D() {
        assertShape("3DM→2D", adjustTo(base3DM, false, false), false, false);
    }

    @Test
    public void canonical_strip_3DM_to_3D() {
        assertShape("3DM→3D", adjustTo(base3DM, true, false), true, false);
    }

    @Test
    public void canonical_strip_3DM_to_2DM() {
        assertShape("3DM→2DM", adjustTo(base3DM, false, true), false, true);
    }

    // ----- swap direction: simultaneously strip one axis and add the other -----

    @Test
    public void canonical_swap_3D_to_2DM() {
        assertShape("3D→2DM", adjustTo(base3D, false, true), false, true);
    }

    @Test
    public void canonical_swap_2DM_to_3D() {
        assertShape("2DM→3D", adjustTo(base2DM, true, false), true, false);
    }

    // ----- (false, false) returns the registered horizontal base instance -----

    @Test
    public void canonical_strip_to_2D_returnsRegisteredBase() {
        // For an EPSG-registered horizontal base, stripping Z/M must yield THE base instance
        // already in CrsRegistry — not a freshly-built equivalent.
        assertSame(base2D, adjustTo(base3D, false, false));
        assertSame(base2D, adjustTo(base2DM, false, false));
        assertSame(base2D, adjustTo(base3DM, false, false));
    }

    // ---------------------------------------------------------------------
    // Canonicalisation through CrsRegistry
    // ---------------------------------------------------------------------

    @Test
    public void canonicalisation_repeatedCall_returnsSameInstance() {
        CoordinateReferenceSystem<?> first = adjustTo(base2D, true, false);
        CoordinateReferenceSystem<?> second = adjustTo(base2D, true, false);
        assertSame(first, second);
    }

    @Test
    public void canonicalisation_widenAndStrip_convergeToSameInstance() {
        // adjustTo(2D,  true, false) → 3D widened from the base
        // adjustTo(3DM, true, false) → 3D stripped from 3DM
        // Both must resolve to the same canonical 3D instance for this base.
        CoordinateReferenceSystem<?> fromWiden = adjustTo(base2D, true, false);
        CoordinateReferenceSystem<?> fromStrip = adjustTo(base3DM, true, false);
        assertSame(fromWiden, fromStrip);
    }

    @Test
    public void canonicalisation_2DM_widenAndStrip_convergeToSameInstance() {
        CoordinateReferenceSystem<?> fromWiden = adjustTo(base2D, false, true);
        CoordinateReferenceSystem<?> fromStrip = adjustTo(base3DM, false, true);
        assertSame(fromWiden, fromStrip);
    }

    // ---------------------------------------------------------------------
    // Convenience overload: adjustTo(crs, dim, hasM)
    // ---------------------------------------------------------------------

    // ----- mapping table: dim ↦ derived (hasZ, hasM) -----

    @Test
    public void overload_dim2_yields_XY() {
        assertShape("dim=2 from 2D",  adjustTo(base2D,  2, false), false, false);
        assertShape("dim=2 from 3D",  adjustTo(base3D,  2, false), false, false);
        assertShape("dim=2 from 2DM", adjustTo(base2DM, 2, false), false, false);
        assertShape("dim=2 from 3DM", adjustTo(base3DM, 2, false), false, false);
    }

    @Test
    public void overload_dim3_hasMfalse_yields_XYZ() {
        assertShape("dim=3 from 2D",  adjustTo(base2D,  3, false), true, false);
        assertShape("dim=3 from 2DM", adjustTo(base2DM, 3, false), true, false);
        assertShape("dim=3 from 3DM", adjustTo(base3DM, 3, false), true, false);
    }

    @Test
    public void overload_dim3_hasMtrue_yields_XYM() {
        assertShape("dim=3,hasM from 2D",  adjustTo(base2D,  3, true), false, true);
        assertShape("dim=3,hasM from 3D",  adjustTo(base3D,  3, true), false, true);
        assertShape("dim=3,hasM from 3DM", adjustTo(base3DM, 3, true), false, true);
    }

    @Test
    public void overload_dim4_yields_XYZM() {
        assertShape("dim=4 from 2D",  adjustTo(base2D,  4, false), true, true);
        assertShape("dim=4 from 3D",  adjustTo(base3D,  4, false), true, true);
        assertShape("dim=4 from 2DM", adjustTo(base2DM, 4, false), true, true);
    }

    // ----- the GeoJSON regression case: 3D default + 2-coord input -----

    @Test
    public void overload_dim2_strips_3D_to_2D() {
        // This is exactly the failure mode of PointDeserializationTest.testDeserializePointTextWithNoLimitToCrs.
        assertShape("GeoJSON 3D-default + 2-coord input",
                adjustTo(base3D, 2, false),
                false, false);
    }

    // ----- single-arg sugar: adjustTo(crs, dim) ≡ adjustTo(crs, dim, false) -----

    @Test
    public void overload_singleArg_treats_dim3_as_XYZ() {
        assertShape("dim=3 (sugar)", adjustTo(base2D, 3), true, false);
    }

    @Test
    public void overload_singleArg_dim2_strips_3D_to_2D() {
        assertShape("dim=2 (sugar) from 3D", adjustTo(base3D, 2), false, false);
    }

    @Test
    public void overload_singleArg_dim4_yields_XYZM() {
        assertShape("dim=4 (sugar) from 2D", adjustTo(base2D, 4), true, true);
    }

    // ---------------------------------------------------------------------
    // Empty / no-constraint case
    // ---------------------------------------------------------------------

    @Test
    public void overload_dim0_returnsCrsUnchanged() {
        // Empty GeoJSON/WKT geometries report coordinateDimension=0; the parser's default CRS
        // must be passed through unchanged in that case (there is nothing to constrain).
        assertSame(base3D, adjustTo(base3D, 0, false));
        assertSame(base2D, adjustTo(base2D, 0, false));
        assertSame(base2DM, adjustTo(base2DM, 0, false));
    }

    @Test
    public void overload_dim1_returnsCrsUnchanged() {
        // dim < 2 is uniformly treated as "no constraint".
        assertSame(base3D, adjustTo(base3D, 1, false));
    }

    // ---------------------------------------------------------------------
    // Error cases
    // ---------------------------------------------------------------------

    @Test(expected = IllegalArgumentException.class)
    public void overload_dim_greaterThan4_throws() {
        adjustTo(base2D, 5, false);
    }

    @Test(expected = IllegalArgumentException.class)
    public void overload_dim2_withHasM_throws() {
        // dim=2 has no room for an M axis — this is a contradictory request.
        adjustTo(base2D, 2, true);
    }
}
