package org.geolatte.geom.crs;

/**
 * @author Karel Maesen, Geovise BVBA, 2011
 */
public class CrsId {

    final static public CrsId UNDEFINED = new CrsId("EPSG", -1);

    final String authority;
    final int code;

    static public CrsId parse(String srsString) {
          if (srsString == null || srsString.isEmpty())
              throw new IllegalArgumentException("Require input of form '<authority>:<code>");
          String[] tokens = srsString.split(":");
          String authority;
          String codeStr = tokens[tokens.length - 1];
          codeStr = codeStr.trim();
          int code = Integer.valueOf(codeStr);
          authority = "EPSG";
          if (tokens.length == 2) {
              authority = tokens[0];
          }
          return new CrsId(authority, code);
      }




    public CrsId(String authority, int code) {
        this.authority = authority;
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
