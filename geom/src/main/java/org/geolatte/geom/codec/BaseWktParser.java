package org.geolatte.geom.codec;

import org.geolatte.geom.C2D;
import org.geolatte.geom.Geometry;
import org.geolatte.geom.GeometryType;
import org.geolatte.geom.Position;
import org.geolatte.geom.codec.support.*;
import org.geolatte.geom.crs.CoordinateReferenceSystem;
import org.geolatte.geom.crs.CoordinateReferenceSystems;

import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

import static org.geolatte.geom.codec.SimpleTokenizer.*;

class BaseWktParser<P extends Position> {

    private final static CoordinateReferenceSystem<C2D> DEFAULT_CRS = CoordinateReferenceSystems.PROJECTED_2D_METER;
    private final static Pattern EMPTY_PATTERN = Pattern.compile("empty", Pattern.CASE_INSENSITIVE);

    private final WktDialect dialect;
    private final CoordinateReferenceSystem<P> overrideCrs;

    private CoordinateReferenceSystem<?> matchedSrid;

    SimpleTokenizer tokenizer;

    GeometryType type;
    GeometryBuilder builder;
    boolean hasMMark = false;

    enum Delimiter {CLOSE, OPEN, SEP, NO_DELIM}

    /**
     * The constructor of this AbstractWktDecoder.
     * <p>
     * The specified CRS (if not null) will be used for the resulting Geometry, even
     * is the WKT contains a SRID code.
     *
     * @param wktDialect The <code>WktVariant</code> to be used by this decoder.
     * @param wkt        The WKT string to parser
     * @param crs        the CoordinateReferenceSystem for the parse result
     */
    BaseWktParser(WktDialect wktDialect, String wkt, CoordinateReferenceSystem<P> crs) {
        dialect = wktDialect;
        tokenizer = new SimpleTokenizer(wkt);
        this.overrideCrs = crs;
    }

    Geometry<P> parse() {
        matchesOptionalSrid();
        builder = matchesGeometryTaggedText();
        CoordinateReferenceSystem<P> geomCrs = selectCoordinateReferenceSystem();
        try {
            return builder.createGeometry(geomCrs);
        } catch (DecodeException e) {
            throw new WktDecodeException("Failure in decoding Wkt", e);
        }
    }

    protected GeometryBuilder matchesGeometryTaggedText() {
        matchesGeometryKeyword();
        GeometryBuilder builder = GeometryBuilder.create(type);
        matchesOptionalZMMarkers();
        matchesTaggedText(builder);
        return builder;
    }

    @SuppressWarnings("unchecked")
    protected CoordinateReferenceSystem<P> selectCoordinateReferenceSystem() {
        if (overrideCrs != null) {
            return overrideCrs;
        } else {
            CoordinateReferenceSystem<?> baseCrs = this.matchedSrid != null ? matchedSrid : DEFAULT_CRS;
            return (CoordinateReferenceSystem<P>) widenCrsToCoordinateDimension(baseCrs);
        }
    }

    protected CoordinateReferenceSystem<?> widenCrsToCoordinateDimension(CoordinateReferenceSystem<?> crs) {
        return CoordinateReferenceSystems.adjustTo(crs, builder.getCoordinateDimension(), hasMMark);
    }

    protected void matchesOptionalSrid() {
        //do nothing
    }

    protected void setMatchedSrid(CoordinateReferenceSystem<?> crs) {
        this.matchedSrid = crs;
    }

    protected void matchesGeometryKeyword() {
        for (Map.Entry<GeometryType, Pattern> entry : dialect.geometryTypePatternMap().entrySet()) {
            if (tokenizer.matchPattern(entry.getValue())) {
                type = entry.getKey();
                return;
            }
        }
        throw new WkbDecodeException("Expected geometryKeyword starting at position: " + tokenizer.currentPos());
    }

    protected void matchesOptionalZMMarkers() {
        //do nothing in base case
    }

    protected void matchesTaggedText(GeometryBuilder builder) {
        if (tokenizer.matchPattern(EMPTY_PATTERN)) {
            return;
        }
        matchesPositionText(builder);
    }

    protected void matchesPositionText(GeometryBuilder builder) {
        switch (type) {
            case POINT:
                builder.setPositions(matchesSinglePosition());
                break;
            case LINESTRING:
                builder.setPositions(matchesPositionList());
                break;
            case POLYGON:
            case MULTILINESTRING:
                builder.setPositions(matchesListOfPositionList());
                break;
            case MULTIPOLYGON:
                builder.setPositions(matchesPolygonList());
                break;
            case MULTIPOINT:
                builder.setPositions(matchesMultiPointList());
                break;
            case GEOMETRYCOLLECTION:
                matchGeometries((CollectionGeometryBuilder) builder);
                break;
            default:
                throw new WktDecodeException("Unknown geometry type");
        }

    }

    protected Holder matchesMultiPointList() {
        return matchesPositionList();
    }

    protected void matchGeometries(CollectionGeometryBuilder builder) {
        if (!tokenizer.matchesOpenList()) {
            throw new WktDecodeException("Expected '(' near position " + tokenizer.currentPos());
        }
        Delimiter d;
        do {
            builder.push(matchesGeometryTaggedText());
            d = matchesDelimiter();
            if (d == Delimiter.NO_DELIM) {
                throw new WktDecodeException(String.format("Expected ')' or ',' near %d", tokenizer.currentPos()));
            }
        } while (d != Delimiter.CLOSE);
    }

    protected PointHolder matchesPosition() {
        PointHolder pnt = new PointHolder();
        Delimiter d;
        do {
            pnt.push(tokenizer.fastReadNumber());
            d = matchesDelimiter();
        } while (!(d == Delimiter.CLOSE || d == Delimiter.SEP));
        tokenizer.back(1);
        return pnt;
    }

    protected PointHolder matchesSinglePosition() {
        if (!tokenizer.matchesOpenList()) {
            throw new WktDecodeException("Expected '(' near position " + tokenizer.currentPos());
        }
        PointHolder pnt = matchesPosition();
        if (!tokenizer.matchesCloseList()) {
            throw new WktDecodeException("Expected ')' near position " + tokenizer.currentPos());
        }
        return pnt;
    }

    protected LinearPositionsHolder matchesPositionList() {
        if (!tokenizer.matchesOpenList()) {
            throw new WktDecodeException("Expected '(' near position " + tokenizer.currentPos());
        }
        LinearPositionsHolder lph = new LinearPositionsHolder();
        Delimiter d;
        do {
            lph.push(matchesPosition());
            d = matchesDelimiter();
            if (d == Delimiter.NO_DELIM) {
                throw new WktDecodeException(String.format("Expected ')' or ',' near %d", tokenizer.currentPos()));
            }
        } while (d != Delimiter.CLOSE);
        return lph;
    }

    protected LinearPositionsListHolder matchesListOfPositionList() {
        if (!tokenizer.matchesOpenList()) {
            throw new WktDecodeException("Expected '(' near position " + tokenizer.currentPos());
        }
        LinearPositionsListHolder lplh = new LinearPositionsListHolder();
        Delimiter d;
        do {
            lplh.push(matchesPositionList());
            d = matchesDelimiter();
            if (d == Delimiter.NO_DELIM) {
                throw new WktDecodeException(String.format("Expected ')' or ',' near %d", tokenizer.currentPos()));
            }
        } while (d != Delimiter.CLOSE);
        return lplh;
    }

    protected PolygonListHolder matchesPolygonList() {
        if (!tokenizer.matchesOpenList()) {
            throw new WktDecodeException("Expected '(' near position " + tokenizer.currentPos());
        }
        PolygonListHolder plh = new PolygonListHolder();
        Delimiter d;
        do {
            plh.push(matchesListOfPositionList());
            d = matchesDelimiter();
            if (d == Delimiter.NO_DELIM) {
                throw new WktDecodeException(String.format("Expected ')' or ',' near %d", tokenizer.currentPos()));
            }
        } while (d != Delimiter.CLOSE);
        return plh;
    }

    protected Delimiter matchesDelimiter() {
        Optional<Character> match = tokenizer.matchesOneOf(openListChar, closeListChar, elementSeparator);
        if (match.isPresent()) {
            switch (match.get()) {
                case ',':
                    return Delimiter.SEP;
                case '(':
                    return Delimiter.OPEN;
                case ')':
                    return Delimiter.CLOSE;
                default:
                    return Delimiter.NO_DELIM;
            }
        } else {
            return Delimiter.NO_DELIM;
        }
    }

}


