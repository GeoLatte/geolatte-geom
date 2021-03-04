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
 * Copyright (C) 2010 - 2011 and Ownership of code is shared by:
 * Qmino bvba - Romeinsestraat 18 - 3001 Heverlee  (http://www.qmino.com)
 * Geovise bvba - Generaal Eisenhowerlei 9 - 2140 Antwerpen (http://www.geovise.com)
 */

package org.geolatte.geom.codec;

import org.geolatte.geom.Geometry;
import org.geolatte.geom.Position;
import org.geolatte.geom.codec.support.Holder;
import org.geolatte.geom.codec.support.LinearPositionsHolder;
import org.geolatte.geom.crs.CoordinateReferenceSystem;
import org.geolatte.geom.crs.CoordinateReferenceSystems;
import org.geolatte.geom.crs.CrsRegistry;

import java.util.Optional;
import java.util.regex.Pattern;

/**
 * A decoder for the Postgis WKT/EWKT representations as used in Postgis (at least 1.0 to 1.5+).
 *
 * <p>This class is not thread-safe</p>
 *
 * @author Karel Maesen, Geovise BVBA, 2011
 */
public class PostgisWktDecoder implements WktDecoder {

    @Override
    public <P extends Position> Geometry<P> decode(String wkt, CoordinateReferenceSystem<P> crs) {
        return new PostgisWktParser<P>(wkt, crs).parse();
    }
}

class PostgisWktParser<P extends Position> extends BaseWktParser<P> {

    private final static PostgisWktDialect dialect = new PostgisWktDialect();
    private final static Pattern SRID_RE = Pattern.compile("^SRID=(.*);", Pattern.CASE_INSENSITIVE);

    public PostgisWktParser(String wkt, CoordinateReferenceSystem<P> crs) {
        super(dialect, wkt, crs);
    }

    public PostgisWktParser(PostgisWktDialect dialect, String wkt, CoordinateReferenceSystem<P> crs) {
        super(dialect, wkt, crs);
    }

    @Override
    protected void matchesOptionalZMMarkers() {
        tokenizer.skipWhitespace();
        if (tokenizer.matchesOneOf('Z', 'z').isPresent()) {
            this.hasZMark = true;
        }
        if (tokenizer.matchesOneOf('M', 'm').isPresent()) {
            this.hasMMark = true;
        }
    }

    @Override
    protected void matchesOptionalSrid() {
        Optional<String> sridOpt = tokenizer.extractGroupFromPattern(SRID_RE, 1);
        if (sridOpt.isPresent()) {
            try {
                int sridCode = Integer.parseInt(sridOpt.get());
                setMatchedSrid(CrsRegistry.getCoordinateReferenceSystemForEPSG(sridCode, CoordinateReferenceSystems.PROJECTED_2D_METER));
            } catch (NumberFormatException e) {
                throw new WktDecodeException("Not a valid SRID code in WKT " + sridOpt.get());
            }
        }
    }


}