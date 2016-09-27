package org.geolatte.geom.crs;

/**
 * Created by Karel Maesen, Geovise BVBA on 29/11/14.
 */
public class VerticalDatum extends CrsIdentifiable {


    public static final VerticalDatum UNKNOWN_VERTICAL_DATUM = new VerticalDatum(CrsId.UNDEFINED, "Undefined " +
            "Vertical", 0);

    private final int datumType;
    private final Extension extension;

    /**
     * Constructs an instance.
     *
     * @param crsId
     * @param name
     */
    public VerticalDatum(CrsId crsId, String name, int datumtype) {
        this(crsId, name, datumtype, null);
    }

    /**
     * Constructs an instance
     * @param crsId
     * @param name
     * @param datumtype
     */
    public VerticalDatum(CrsId crsId, String name, int datumtype, Extension extension) {
        super(crsId, name);
        this.datumType = datumtype;
        this.extension = extension;
    }

    public int getDatumType() {
        return this.datumType;
    }

    public Extension getExtension() {
        return this.extension;
    }

}


