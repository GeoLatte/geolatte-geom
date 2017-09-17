package org.geolatte.geom.crs;

/**
 * An identifier for a <code>CoordinateReferenceSystem</code> or other object referenced in a
 * <code>CoordinateReferenceSystem</code> definition.
 *
 * <p>A commonly used alternative name for <code>CrsId</code> is "SRID".</p>
 *
 * <p>A <code>CrsId</code> consists of the name of an authority and a numeric identifier. The authority is the
 * organization that is responsible for managing the definition of the identified object. </p>
 *
 * <p>In practice, <code>CoordinateReferenceSystem</code>s are usually identified by their EPSG code, with <a href="http://www.epsg.org/">EPSG</a>
 * the authority for the identifier. In this implementation, EPSG is the default authority.</p>
 *
 * @author Karel Maesen, Geovise BVBA, 2011
 */
public class CrsId {

    final static public String DEFAULT_AUTHORITY = "EPSG";

    /**
     * Identifies an undefined (or unknown) <code>CrsId</code>.
     */
    final static public CrsId UNDEFINED = new CrsId("EPSG", -1);

    final private String authority;
    final private int code;

    /**
     * Creates an instance from a <code>String</code> of the form "[&lt;authority&gt;:]&lt;code&gt;".
     *
     * <p>If the "authority" prefix is missing, then the authority will be assumed to be EPSG.</p>
     *
     * @param srsString the string to parse into a <code>CrsId</code>.
     * @return
     * @throws IllegalArgumentException when the string can nog be parsed as
     */
    static public CrsId parse(String srsString) {
        if (srsString == null || srsString.isEmpty())
            throw new IllegalArgumentException("Require input of form '<authority>:<code>");
        String[] tokens = srsString.split(":");
        String authority;
        String codeStr = tokens[tokens.length - 1];
        int code = toNumericIdentifier(codeStr);
        authority = DEFAULT_AUTHORITY;
        if (tokens.length == 2) {
            authority = tokens[0];
        } else if( tokens.length == 7) {
            authority = tokens[4];
        }
        return valueOf(authority, code);
    }

    private static int toNumericIdentifier(String codeStr) {
        codeStr = codeStr.trim();
        try {
            return Integer.valueOf(codeStr);
        }catch(NumberFormatException e){
            throw new IllegalArgumentException(String.format("Can't parse value %s into an integer.", codeStr));
        }
    }

    /**
     * Returns an instance having the specified authority and code.
     *
     * @param authority the authority that assigned the code
     * @param code the code for the <code>CoordinateReferenceSystem</code>
     * @return a <code>CrsId</code> for the specified authority and code.
     */
    public static CrsId valueOf(String authority, int code) {
        if (DEFAULT_AUTHORITY.equalsIgnoreCase(authority) && (code <= 0 )) {
            return CrsId.UNDEFINED;
        }
        CrsId result = null;
        if (DEFAULT_AUTHORITY.equalsIgnoreCase(authority)) {
            result = CrsRegistry.getCrsIdForEPSG(code);
        }
        return result == null ? new CrsId(authority, code) : result;
    }

    /**
     * Returns a <code>CrsId</code> with the specified code and the
     * EPSG as authority.
     *
     * @param code the code for the <code>CrsId</code>.
     * @return a <code>CrsId</code> for the specified code,and EPSG as authority.
     */
    public static CrsId valueOf(int code) {
        return valueOf(CrsId.DEFAULT_AUTHORITY, code);
    }

    /**
     * Creates an instance having the specified authority and code.
     *
     * <p>If authority EPSG and 0 or -1 is passed for the code parameter, a value equal to <code>CrsId.UNDEFINED</code> is returned.
     *
     * @param authority the authority that assigned the code
     * @param code the code for the <code>CoordinateReferenceSystem</code>
     * @return a <code>CrsId</code> for the specified authority and code.
     */
    public CrsId(String authority, int code) {
        if (authority == null || authority.isEmpty()) {
            throw new IllegalArgumentException("Null or empty authority parameter.");
        }
        this.authority = authority.toUpperCase();
        this.code = (code < 1 && DEFAULT_AUTHORITY.equals(this.authority)) ? -1 : code;
    }

    /**
     * Returns the authority for this <code>CrsId</code>.
     *
     * @return a String representing the authority for this <code>CrsId</code>
     */
    public String getAuthority() {
        return authority;
    }

    /**
     * Returns the code for this <code>CrsId</code>.
     *
     * @return the numeric code for this <code>CrsId</code>
     */
    public int getCode() {
        return code;
    }

    @Override
    public String toString() {
        return String.format("%S:%d", authority, code);
    }

    public String toUrn() {
        return String.format("urn:ogc:def:crs:%s::%s", authority, code);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CrsId crsId = (CrsId) o;

        if (code != crsId.code) return false;
        if (!authority.equals(crsId.authority)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = authority.hashCode();
        result = 31 * result + code;
        return result;
    }
}
