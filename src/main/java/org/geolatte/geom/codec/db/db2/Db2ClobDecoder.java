package org.geolatte.geom.codec.db.db2;

import org.geolatte.geom.Geometry;
import org.geolatte.geom.codec.Wkt;
import org.geolatte.geom.codec.db.Decoder;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.sql.Clob;
import java.sql.SQLException;

/**
 * Created by Adtech Geospatial, 2014
 */
public class Db2ClobDecoder implements Decoder<Clob> {

    @Override
    public Geometry<?> decode(Clob clob) {
        String wkt = clobToString(clob);
        return Wkt.fromWkt(wkt);
    }

    @Override
    public boolean accepts(Clob clob) {
        return true;
    }

    /**
     * @param clob - spatial value represented as WKT comes in as Clob type
     * @return The spatial value as WKT String type
     */
    private String clobToString(Clob clob) {
        InputStream in = null;
        final char[] buffer = new char[0x10000];
        StringBuilder out = new StringBuilder();
        try {
            in = clob.getAsciiStream();
            Reader in2 = new InputStreamReader(in);
            int read;
            do {
                read = in2.read(buffer, 0, buffer.length);
                if (read > 0) {
                    out.append(buffer, 0, read);
                }
            } while (read >= 0);

        } catch (IOException | SQLException ex) {
            throw new DB2DecodeException(ex);
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (IOException e) {
                // nothing to do
            }
        }
        String result = out.toString();
        return result;
    }

}
