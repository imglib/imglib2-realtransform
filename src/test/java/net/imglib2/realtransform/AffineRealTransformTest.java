package net.imglib2.realtransform;

import static org.junit.Assert.assertArrayEquals;

import org.junit.Test;

import net.imglib2.RealPoint;

public class AffineRealTransformTest
{
	private static final double EPS = 1e-9;

	// 3D -> 2D: x' = x + 2z + 1, y' = 3y - 1
	private static AffineRealTransform projection()
	{
		return new AffineRealTransform( 3, 2, new double[] {
				1, 0, 2, 1,
				0, 3, 0, -1 } );
	}

	// 2D -> 3D: x' = x + 1, y' = 2y, z' = x + y + 5
	private static AffineRealTransform embedding()
	{
		return new AffineRealTransform( 2, 3, new double[] {
				1, 0, 1,
				0, 2, 0,
				1, 1, 5 } );
	}

	@Test
	public void testProjection()
	{
		final double[] dst = new double[ 2 ];
		projection().apply( new double[] { 1, 2, 3 }, dst );
		assertArrayEquals( new double[] { 8, 5 }, dst, EPS );

		final RealPoint p = new RealPoint( 2 );
		projection().apply( new RealPoint( 1.0, 2.0, 3.0 ), p );
		assertArrayEquals( new double[] { 8, 5 }, p.positionAsDoubleArray(), EPS );
	}

	@Test
	public void testEmbedding()
	{
		final double[] dst = new double[ 3 ];
		embedding().apply( new double[] { 1, 2 }, dst );
		assertArrayEquals( new double[] { 2, 4, 8 }, dst, EPS );

		final RealPoint p = new RealPoint( 3 );
		embedding().apply( new RealPoint( 1.0, 2.0 ), p );
		assertArrayEquals( new double[] { 2, 4, 8 }, p.positionAsDoubleArray(), EPS );
	}

	@Test
	public void testMatchesAffineTransform3D()
	{
		final AffineTransform3D affine = new AffineTransform3D();
		affine.set(
				1.1, 0.2, -0.3, 4,
				0.5, 0.9, 0.1, -2,
				-0.2, 0.3, 1.4, 7 );
		final AffineRealTransform art = new AffineRealTransform( 3, 3, affine.getRowPackedCopy() );

		final double[] src = { 1.5, -2.5, 3.25 };
		final double[] expected = new double[ 3 ];
		final double[] actual = new double[ 3 ];
		affine.apply( src, expected );
		art.apply( src, actual );
		assertArrayEquals( expected, actual, EPS );
	}

	@Test
	public void testInPlace()
	{
		final double[] p = { 1, 2, 3 };
		projection().apply( p, p );
		assertArrayEquals( new double[] { 8, 5 }, new double[] { p[ 0 ], p[ 1 ] }, EPS );

		final double[] q = { 1, 2, 0 };
		embedding().apply( q, q );
		assertArrayEquals( new double[] { 2, 4, 8 }, q, EPS );
	}

	@Test
	public void testInPlaceRealLocalizable()
	{
		final RealPoint p = new RealPoint( 1.0, 2.0, 3.0 );
		projection().apply( p, p );
		assertArrayEquals( new double[] { 8, 5 }, new double[] { p.getDoublePosition( 0 ), p.getDoublePosition( 1 ) }, EPS );

		final RealPoint q = new RealPoint( 1.0, 2.0, 0.0 );
		embedding().apply( q, q );
		assertArrayEquals( new double[] { 2, 4, 8 }, q.positionAsDoubleArray(), EPS );
	}

	@Test
	public void testInSequence()
	{
		// 3D -> 2D -> 3D
		final RealTransformSequence seq = new RealTransformSequence();
		seq.add( projection() );
		seq.add( embedding() );

		final double[] src = new double[] { 1, 2, 3 };
		RealPoint srcPt = RealPoint.wrap( src );
		
		// projection: (1, 2, 3) -> (x + 2z + 1, 3y - 1) = (1 + 6 + 1, 6 - 1) = (8, 5)
		// embedding: (8, 5) -> (x + 1, 2y, x + y + 5) = (9, 10, 18)
		final double[] expected = { 9, 10, 18 };

		final double[] dst = new double[ 3 ];
		seq.apply( src, dst );
		assertArrayEquals( expected, dst, EPS );

		final RealPoint p = new RealPoint( 3 );
		seq.apply( srcPt, p );
		assertArrayEquals( expected, p.positionAsDoubleArray(), EPS );
	}
}
