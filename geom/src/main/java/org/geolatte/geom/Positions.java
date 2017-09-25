/*
 * This file is part of the GeoLatte project.
 *
 *     GeoLatte is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Lesser General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     GeoLatte is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Lesser General Public License for more details.
 *
 *     You should have received a copy of the GNU Lesser General Public License
 *     along with GeoLatte.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright (C) 2010 - 2014 and Ownership of code is shared by:
 * Qmino bvba - Romeinsestraat 18 - 3001 Heverlee  (http://www.qmino.com)
 * Geovise bvba - Generaal Eisenhowerlei 9 - 2140 Antwerpen (http://www.geovise.com)
 */

package org.geolatte.geom;

import org.geolatte.geom.crs.CoordinateReferenceSystem;

import java.util.Arrays;
import java.util.List;

/**
 * Factories for creating Positions.
 *
 * @author Karel Maesen, Geovise BVBA
 *         creation-date: 4/3/14
 */
public class Positions {

	public static List<PositionFactory<?>> registeredFactories;

	static {
		registeredFactories = Arrays.asList(
				new CanMakeG2D(),
				new CanMakeG2DM(),
				new CanMakeG3D(),
				new CanMakeG3DM(),
				new CanMakeP2D(),
				new CanMakeP2DM(),
				new CanMakeP3D(),
				new CanMakeP3DM()
		);
	}

	/**
	 * Factory method for {@code Position}s in the reference system.
	 * <p/>
	 * The coordinates array should be in normalized order. See
	 * {@link Position}
	 *
	 * @param coordinates the coordinates of the position
	 *
	 * @return the {@code Position} with the specified coordinates.
	 */
	@SuppressWarnings("unchecked")
	public static <P extends Position> P mkPosition(Class<P> pClass, double... coordinates) {
		PositionFactory<P> factory = getFactoryFor( pClass );
		return factory.mkPosition( coordinates );
	}

	@SuppressWarnings("unchecked")
	public static <P extends Position> P mkPosition(CoordinateReferenceSystem<P> crs, double... coordinates) {
		return mkPosition( crs.getPositionClass(), coordinates );
	}

	public static <P extends Position> PositionSequence<P> collect(Class<P> positionType, P... positions) {
		PositionSequenceBuilder<P> builder = PositionSequenceBuilders.fixedSized(positions.length, positionType);
		for(P p : positions) {
			builder.add(p);
		}
		return builder.toPositionSequence();
	}

	@SuppressWarnings("unchecked")
	public static <P extends Position> PositionFactory<P> getFactoryFor(Class<P> pClass) {
		for ( PositionFactory<?> pFact : registeredFactories ) {
			if ( pFact.forClass().equals( pClass ) ) {
				return (PositionFactory<P>) pFact;
			}
		}
		throw new UnsupportedOperationException(
				String.format(
						"Position type %s unsupported",
						pClass.getSimpleName()
				)
		);
	}

	/**
	 * Copies the source positions to a new PositionSequence.
	 * <p/>
	 * <P>The coordinates are taken as-is. If the target coordinate reference systems has a larger coordinate
	 * dimensions then the source, NaN coordinate values are created.</p>
	 *
	 * @param source
	 * @param targetPosClass target type of {@code Position}
	 * @param <P>
	 *
	 * @return a copy of the source positions
	 */
	public static <Q extends Position, P extends Position> PositionSequence<P> copy(
			final PositionSequence<Q> source,
			final Class<P> targetPosClass) {
		PositionFactory<P> factory = Positions.getFactoryFor( targetPosClass );
		final PositionSequenceBuilder<P> builder = PositionSequenceBuilders.fixedSized( source.size(), targetPosClass );
		if ( source.isEmpty() ) {
			return builder.toPositionSequence();
		}

		final double[] coords = new double[Math.max(
				source.getCoordinateDimension(),
				factory.getCoordinateDimension()
		)];
		Arrays.fill( coords, Double.NaN );
		PositionVisitor<Q> visitor = new PositionVisitor<Q>() {
			public void visit(Q position) {
				position.toArray( coords );
				builder.add( coords );
			}
		};
		source.accept( visitor );
		return builder.toPositionSequence();
	}

	//Factories
	public static class CanMakeP2D implements PositionFactory<C2D> {

		@Override
		public Class<C2D> forClass() {
			return C2D.class;
		}

		@Override
		public int getCoordinateDimension() {
			return 2;
		}

		@Override
		public C2D mkPosition(double... coordinates) {
			return (coordinates.length == 0 ? new C2D() : new C2D( coordinates[0], coordinates[1] ));
		}

		@Override
		public boolean hasZComponent() {
			return false;
		}

		@Override
		public boolean hasMComponent() {
			return false;
		}

		@Override
		public int getMComponentIndex() {
			return -1;
		}
	}

	public static class CanMakeP2DM implements PositionFactory<C2DM> {

		@Override
		public Class<C2DM> forClass() {
			return C2DM.class;
		}

		@Override
		public int getCoordinateDimension() {
			return 3;
		}

		@Override
		public C2DM mkPosition(double... coordinates) {
			return coordinates.length == 0 ? new C2DM() : new C2DM( coordinates[0], coordinates[1], coordinates[2] );
		}

		@Override
		public boolean hasZComponent() {
			return false;
		}

		@Override
		public boolean hasMComponent() {
			return true;
		}

		@Override
		public int getMComponentIndex() {
			return 2;
		}
	}

	public static class CanMakeP3D implements PositionFactory<C3D> {

		@Override
		public Class<C3D> forClass() {
			return C3D.class;
		}

		@Override
		public int getCoordinateDimension() {
			return 3;
		}

		@Override
		public C3D mkPosition(double... coordinates) {
            /*
             In some cases we do not have 3 coordinates here but
			 * only two. in this case we are using 0.0 as z coordinate.
			 */
			return coordinates.length == 0 ? new C3D() : new C3D(
					coordinates[0], coordinates[1],
					coordinates.length > 2 ? coordinates[2] : 0.0
			);
		}

		@Override
		public boolean hasZComponent() {
			return true;
		}

		@Override
		public boolean hasMComponent() {
			return false;
		}

		@Override
		public int getMComponentIndex() {
			return -1;
		}
	}

	public static class CanMakeP3DM implements PositionFactory<C3DM> {

		@Override
		public Class<C3DM> forClass() {
			return C3DM.class;
		}

		@Override
		public int getCoordinateDimension() {
			return 4;
		}

		@Override
		public C3DM mkPosition(double... coordinates) {
			return coordinates.length == 0 ? new C3DM() : new C3DM(
					coordinates[0], coordinates[1], coordinates[2],
					coordinates[3]
			);
		}

		@Override
		public boolean hasZComponent() {
			return true;
		}

		@Override
		public boolean hasMComponent() {
			return true;
		}

		@Override
		public int getMComponentIndex() {
			return 3;
		}
	}

	//Factories
	public static class CanMakeG2D implements PositionFactory<G2D> {

		@Override
		public Class<G2D> forClass() {
			return G2D.class;
		}

		@Override
		public int getCoordinateDimension() {
			return 2;
		}

		@Override
		public G2D mkPosition(double... coordinates) {
			return (coordinates.length == 0 ? new G2D() : new G2D( coordinates[0], coordinates[1] ));
		}

		@Override
		public boolean hasZComponent() {
			return false;
		}

		@Override
		public boolean hasMComponent() {
			return false;
		}

		@Override
		public int getMComponentIndex() {
			return -1;
		}
	}

	public static class CanMakeG2DM implements PositionFactory<G2DM> {

		@Override
		public Class<G2DM> forClass() {
			return G2DM.class;
		}

		@Override
		public int getCoordinateDimension() {
			return 3;
		}

		@Override
		public G2DM mkPosition(double... coordinates) {
			return coordinates.length == 0 ? new G2DM() : new G2DM( coordinates[0], coordinates[1], coordinates[2] );
		}

		@Override
		public boolean hasZComponent() {
			return false;
		}

		@Override
		public boolean hasMComponent() {
			return true;
		}

		@Override
		public int getMComponentIndex() {
			return 2;
		}
	}

	public static class CanMakeG3D implements PositionFactory<G3D> {

		@Override
		public Class<G3D> forClass() {
			return G3D.class;
		}

		@Override
		public int getCoordinateDimension() {
			return 3;
		}

		@Override
		public G3D mkPosition(double... coordinates) {
			return coordinates.length == 0 ? new G3D() : new G3D( coordinates[0], coordinates[1], coordinates[2] );
		}

		@Override
		public boolean hasZComponent() {
			return true;
		}

		@Override
		public boolean hasMComponent() {
			return false;
		}

		@Override
		public int getMComponentIndex() {
			return -1;
		}
	}

	public static class CanMakeG3DM implements PositionFactory<G3DM> {

		@Override
		public Class<G3DM> forClass() {
			return G3DM.class;
		}

		@Override
		public int getCoordinateDimension() {
			return 4;
		}

		@Override
		public G3DM mkPosition(double... coordinates) {
			return coordinates.length == 0 ? new G3DM() : new G3DM(
					coordinates[0], coordinates[1], coordinates[2],
					coordinates[3]
			);
		}

		@Override
		public boolean hasZComponent() {
			return true;
		}

		@Override
		public boolean hasMComponent() {
			return true;
		}

		@Override
		public int getMComponentIndex() {
			return 3;
		}
	}

}
