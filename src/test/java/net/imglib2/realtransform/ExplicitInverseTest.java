package net.imglib2.realtransform;

import static org.junit.Assert.*;

import org.junit.Test;

public class ExplicitInverseTest
{

	private static final double EPS = 1e-6;

	@Test
	public void testConstructionAndApply()
	{
		AffineTransform3D xfm = new AffineTransform3D();
		xfm.scale( 0.3 );
		xfm.setTranslation( 5, -5, 0 );
		xfm.rotate( 0, 0.1 );
		xfm.rotate( 1, 0.2 );
		xfm.rotate( 2, 0.3 );

		AffineTransform3D xfmi = xfm.inverse;

		ExplicitInvertibleRealTransform eit = new ExplicitInvertibleRealTransform( xfm, xfmi );

		assertTrue( "inverse-inverse is equal(==)  ", eit == ( eit.inverse().inverse() ) );

		double[] p = new double[] { 1, 1, 1 };
		double[] q = new double[ 3 ];
		double[] qtrue = new double[ 3 ];

		xfm.apply( p, qtrue );
		eit.apply( p, q );

		xfm.inverse().apply( p, qtrue );
		eit.apply( p, q );

		xfmi.apply( p, qtrue );
		eit.applyInverse( q, p );

		// not especially useful at the moment, but it's something
		assertArrayEquals( "foward", qtrue, q, EPS );
		assertArrayEquals( "inverse", qtrue, q, EPS );

	}
}
