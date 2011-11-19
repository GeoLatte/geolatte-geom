package org.geolatte.geom.crs;

/**
 * An identifier for a <code>CoordinateReferenceSystem</code>.
 * <p/>
 * <p>Another, commonly used, name for <code>CrsId</code> is SRID.</p>
 * <p/>
 * <p><code>CoordinateReferenceSystem</code>s are usually identified by their EPSG code. EPSG
 * is then the authority for the identifier. In this implementation, EPSG is therefore the default
 * authority.</p>
 *
 * @author Karel Maesen, Geovise BVBA, 2011
 */
public class CrsId {

    final static public String DEFAULT_AUTHORITY = "EPSG";

    /**
     * Identifies an undefined (or unknown) <code>CrsId</code>.
     */
    final static public CrsId UNDEFINED = new CrsId("EPSG", -1);

    final String authority;
    final int code;

    /**
     * Creates an instance from a <code>String</code> of the form "[<authority>:]<code>.
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
        }
        return new CrsId(authority, code);
    }

    private static int toNumericIdentifier(String codeStr) {
        codeStr = codeStr.trim();
        try {
            return Integer.valueOf(codeStr);
        }catch(NumberFormatException e){
            throw new IllegalArgumentException(String.format("Can't parse value %s into an integer.", codeStr));
        }
    }

    public CrsId(String authority, int code) {
        this.authority = authority;
        this.code = code;
    }

    public CrsId(int code){
        this.authority = DEFAULT_AUTHORITY;
        this.code = code;
    }

    public String getAuthority() {
        return authority;
    }

    public int getCode() {
        return code;
    }

    @Override
    public String toString() {
        return String.format("%S:%d", authority, code);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CrsId crsId = (CrsId) o;

        if (code != crsId.code) return false;
        if (authority != null ? !authority.equals(crsId.authority) : crsId.authority != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = authority != null ? authority.hashCode() : 0;
        result = 31 * result + code;
        return result;
    }
}
