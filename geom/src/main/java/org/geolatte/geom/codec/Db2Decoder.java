package org.geolatte.geom.codec;

/**
 * An EWKT decoder for DB2 as used in Hibernate Spatial
 * <p>
 * <p>
 * Note:this Decoder is to be used in combination with the DB2 EWKT transform group created by David Adler.See the
 * Hibernate documentation for the DB2 Dialect.
 * <p>
 * Created by Karel Maesen,Geovise BVBA on 05/06/17.
 */

class Db2Decoder extends HANAWktDecoder {

	//DB2 and HANA use the same (E)WKT format, so we just inherit (so later we can substitute a different implementation should the need arise
}
