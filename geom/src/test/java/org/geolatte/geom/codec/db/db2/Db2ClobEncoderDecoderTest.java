package org.geolatte.geom.codec.db.db2;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.sql.Clob;
import java.sql.SQLException;

import org.geolatte.geom.G2D;
import org.geolatte.geom.Point;

import org.junit.Test;

import static org.geolatte.geom.builder.DSL.g;
import static org.geolatte.geom.builder.DSL.point;
import static org.geolatte.geom.crs.CoordinateReferenceSystems.WGS84;
import static org.junit.Assert.assertEquals;

/**
 * Created by Karel Maesen, Geovise BVBA on 14/02/2018.
 */
public class Db2ClobEncoderDecoderTest {


	@Test
	public void testEncoding() {
		Db2ClobEncoder encoder = new Db2ClobEncoder();
		Point<G2D> g2d = point( WGS84, g( 20.345, 10.234 ) );

		assertEquals( "SRID=4326;POINT(20.345 10.234)", encoder.encode( g2d ) );

	}

	@Test
	public void testDecoding() {
		Db2ClobDecoder decoder = new Db2ClobDecoder( 4326 );
		Point<G2D> g2d = point( WGS84, g( 20.345, 10.234 ) );

		assertEquals( g2d, decoder.decode( new TestClob( "SRID=4326;POINT(20.345 10.234)" ) ) );

	}
}

class TestClob implements Clob {

	final private String value;

	TestClob(String obj) {
		this.value = obj;
	}

	@Override
	public long length() throws SQLException {
		return value.length();
	}

	@Override
	public String getSubString(long pos, int length) throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public Reader getCharacterStream() throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public InputStream getAsciiStream() throws SQLException {
		try {
			return new ByteArrayInputStream( value.getBytes( "UTF-8" ) );
		}
		catch (UnsupportedEncodingException e) {
			throw new SQLException( e );
		}
	}

	@Override
	public long position(String searchstr, long start) throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public long position(Clob searchstr, long start) throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public int setString(long pos, String str) throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public int setString(long pos, String str, int offset, int len) throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public OutputStream setAsciiStream(long pos) throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public Writer setCharacterStream(long pos) throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void truncate(long len) throws SQLException {

	}

	@Override
	public void free() throws SQLException {

	}

	@Override
	public Reader getCharacterStream(long pos, long length) throws SQLException {
		return null;
	}
}
