package org.geolatte.geom.crs;

/**
 * Created by Karel Maesen, Geovise BVBA on 29/11/14.
 */
public class VerticalDatum extends CrsIdentifiable {


    public static final VerticalDatum UNKNOWN_VERTICAL_DATUM = new VerticalDatum(CrsId.UNDEFINED, "Undefined " +
            "Vertical", 0);

    private final int datumType;

    /**
     * Constructs an instance.
     *
     * @param crsId
     * @param name
     */
    protected VerticalDatum(CrsId crsId, String name, int datumtype) {
        super(crsId, name);
        this.datumType = datumtype;
    }

    public int getDatumType() {
        return this.datumType;
    }

}
