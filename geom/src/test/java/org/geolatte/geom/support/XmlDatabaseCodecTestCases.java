package org.geolatte.geom.support;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.geolatte.geom.ByteBuffer;
import org.geolatte.geom.Geometry;
import org.geolatte.geom.codec.Wkt;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * A Set of Test cases for testing encoding/decoding between Geolatte Geometry and native
 * database formats.
 *
 * @author Karel Maesen, Geovise BVBA
 *         creation-date: 4/9/13
 */
public class XmlDatabaseCodecTestCases {

    private final Map<Integer, CodecTestCase> testCases = new HashMap<Integer, CodecTestCase>();

    public void addCase(Integer key, String ewkt, int srid, Geometry geometry, Object dbRepresentation) {
        CodecTestCase testCase = new CodecTestCase();
        testCase.ewkt = ewkt;
        testCase.srid = srid;
        testCase.geometry = geometry;
        testCase.dbRepresentation = dbRepresentation;
        testCases.put(key, testCase);
    }

    /**
     * Returns the expected geometry in EWKT for the specified test case
     *
     * @param key the test case identifier
     * @return
     */
    public String getEWKT(Integer key) {
        return this.testCases.get(key).ewkt;
    }

    /**
     * Returns the SRID of the expected geometry for the specified test case
     *
     * @param key the test case identifier
     * @return
     */
    public int getSrid(Integer key) {
        return this.testCases.get(key).srid;
    }

    /**
     * Returns the expected geometry for the specified test case
     *
     * @param key the test case identifier
     * @return
     */
    public Geometry getGeometry(Integer key) {
        return this.testCases.get(key).geometry;
    }


    /**
     * Returns the internal database representation of the expected geometry for the specified test case
     *
     * @param key the test case identifier
     * @return
     */
    public Object getDbRepresentation(Integer key) {
        return this.testCases.get(key).dbRepresentation;
    }

    public Set<Integer> all() {
        return this.testCases.keySet();
    }

    public static XmlDatabaseCodecTestCases fromResource(String name) {
        InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(name);
        if (inputStream == null) {
            throw new IllegalArgumentException(String.format("XML resource %s not found on classpath.", name));
        }
        SAXReader reader = new SAXReader();
        try {

            Document document = reader.read( inputStream );
            return processTestCasesDocument(document);

        } catch (DocumentException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                inputStream.close();
            } catch (IOException e) {
                // ignore
            }
        }
    }

    private static XmlDatabaseCodecTestCases processTestCasesDocument(Document document) {
        XmlDatabaseCodecTestCases xmlCodecTestCases = new XmlDatabaseCodecTestCases();
        Element root = document.getRootElement();
        for ( Iterator it = root.elementIterator(); it.hasNext(); ) {
            Element element = (Element)it.next();
            addElement(element, xmlCodecTestCases);
        }
        return xmlCodecTestCases;
    }

    private static void addElement(Element element, XmlDatabaseCodecTestCases testCases) {
        int id = Integer.valueOf(element.selectSingleNode("id").getText().trim());
        String ewkt = element.selectSingleNode("ewkt").getText();
        int srid = Integer.valueOf(element.selectSingleNode("srid").getText().trim());
        String hexString = element.selectSingleNode("db_representation").getText().trim();
        Geometry geom = Wkt.fromWkt(String.format("SRID=%d;%s", srid, ewkt));
        ByteBuffer bytes = ByteBuffer.from(hexString);
        testCases.addCase(id, ewkt, srid, geom, bytes);
    }

    static private class CodecTestCase {
        //specification of expected Geolatte geometry
        public String ewkt;
        //SRID of expected Geolatte Geometry
        public int srid;
        //expected Geolatte Geometry
        public Geometry geometry;
        // db binary native Representation
        public Object dbRepresentation;

    }

}
