/*-
 * #%L
 * ImgLib2: a general-purpose, multidimensional image processing library.
 * %%
 * Copyright (C) 2009 - 2020 Tobias Pietzsch, Stephan Preibisch, Stephan Saalfeld,
 * John Bogovic, Albert Cardona, Barry DeZonia, Christian Dietz, Jan Funke,
 * Aivar Grislis, Jonathan Hale, Grant Harris, Stefan Helfrich, Mark Hiner,
 * Martin Horn, Steffen Jaensch, Lee Kamentsky, Larry Lindsey, Melissa Linkert,
 * Mark Longair, Brian Northan, Nick Perry, Curtis Rueden, Johannes Schindelin,
 * Jean-Yves Tinevez and Michael Zinsmaier.
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */
package net.imglib2.realtransform;

import static org.junit.Assert.assertArrayEquals;

import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;

import net.imglib2.RealInterval;
import net.imglib2.RealPoint;
import net.imglib2.iterator.LocalizingRealIntervalIterator;
import net.imglib2.position.FunctionRealRandomAccessible;
import net.imglib2.realtransform.inverse.DifferentiableRealTransform;
import net.imglib2.realtransform.inverse.InverseRealTransformGradientDescent;
import net.imglib2.realtransform.inverse.RealTransformFiniteDerivatives;
import net.imglib2.realtransform.inverse.WrappedIterativeInvertibleRealTransform;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.Intervals;
import net.imglib2.view.composite.RealComposite;

public class IterableInverseTests
{
	public final double EPS = 0.0001;

	public final double MIDEPS = 0.001;

	@Test
	public void testTpsInverseSimple()
	{
		/*
		 * Simple TPS
		 */
		final double[][] srcPts = new double[][] { { -1.0, 0.0, 1.0, 0.0 }, // x
				{ 0.0, -1.0, 0.0, 1.0 } }; // y

		final double[][] tgtPts = new double[][] { { -2.0, 0.0, 2.0, 0.0 }, // x
				{ 0.0, -2.0, 0.0, 2.0 } }; // y

		final ThinplateSplineTransform tps = new ThinplateSplineTransform( srcPts, tgtPts );
		final WrappedIterativeInvertibleRealTransform< ThinplateSplineTransform > tpsInv = new WrappedIterativeInvertibleRealTransform<>( tps );

		final double[] x = new double[] { 0.5, 0.5 };
		final double[] y = new double[ 2 ];
		final double[] yest = new double[ 2 ];
		final double[] yestinv = new double[ 2 ];

		tpsInv.getOptimzer().setTolerance( EPS / 2 );

		tps.apply( x, y );
		tpsInv.applyInverse( yest, y );
		tps.apply( yest, yestinv );

		Assert.assertArrayEquals( "tps", x, yest, EPS );
	}

	public static boolean almostEqual( final RealPoint p, final RealPoint q, final double eps )
	{
		assert p.numDimensions() == q.numDimensions();

		for ( int i = 0; i < p.numDimensions(); i++ )
		{
			if ( Math.abs( p.getDoublePosition( i ) - q.getDoublePosition( i ) ) > eps )
			{ return false; }
		}

		return true;
	}

	@Test
	public void testTpsInverse()
	{
		/*
		 * Warp
		 */
		final double[][] src_simple = new double[][] { { 0, 0, 0, 1, 1, 1, 2, 2, 2 }, // x
				{ 0, 1, 2, 0, 1, 2, 0, 1, 2 }, // y
		};
		// target points
		final double[][] tgt = new double[][] { { -0.5, -0.5, -0.5, 1.5, 1.5, 1.5, 2.0, 2.0, 2.0 }, // x
				{ -0.5, 1.5, 2.0, -0.5, 1.5, 2.0, -0.5, 1.5, 2.0 } // y
		};

		final ThinplateSplineTransform tps = new ThinplateSplineTransform( src_simple, tgt );
		final WrappedIterativeInvertibleRealTransform< ThinplateSplineTransform > tpsInv = new WrappedIterativeInvertibleRealTransform<>( tps );
		tpsInv.getOptimzer().setTolerance( EPS / 5 );

		/* **** PT 1 **** */
		final double[] x = new double[] { 0.0f, 0.0f };
		final double[] y = new double[ 2 ];
		final double[] yi = new double[ 2 ];

		final RealPoint xp = new RealPoint( 2 );
		final RealPoint yp = new RealPoint( 2 );
		final RealPoint yip = new RealPoint( 2 );

		tps.apply( x, y );
		tpsInv.applyInverse( yi, y );
		Assert.assertArrayEquals( "tps warp inv 1", x, yi, EPS );

		xp.setPosition( x );
		tpsInv.apply( xp, yp );
		tpsInv.applyInverse( yip, yp );
		Assert.assertTrue( "tps warp inv 1 pts", almostEqual( xp, yip, EPS ) );

		xp.setPosition( x );
		tpsInv.apply( xp, yp );
		tpsInv.applyInverse( yip, yp );
		Assert.assertTrue( "tps warp inv 1 pts", almostEqual( xp, yip, EPS ) );

		/* **** PT 2 **** */
		x[ 0 ] = 0.5;
		x[ 1 ] = 0.5;

		tps.apply( x, y );
		tpsInv.applyInverse( yi, y );
		Assert.assertArrayEquals( "tps warp inv 2", x, yi, EPS );

		xp.setPosition( x );
		tpsInv.apply( xp, yp );
		tpsInv.applyInverse( yip, yp );
		Assert.assertTrue( "tps warp inv 2 pts", almostEqual( xp, yip, EPS ) );

		/* **** PT 3 **** */
		x[ 0 ] = 1;
		x[ 1 ] = 1;

		tps.apply( x, y );
		tpsInv.applyInverse( yi, y );
		Assert.assertArrayEquals( "tps warp inv 3", x, yi, EPS );

		xp.setPosition( x );
		tpsInv.apply( xp, yp );
		tpsInv.applyInverse( yip, yp );
		Assert.assertTrue( "tps warp inv 3 pts", almostEqual( xp, yip, EPS ) );

		/* **** RANDOM PT SMALL **** */
		x[ 0 ] = 0.6198672937136046;
		x[ 1 ] = 0.3758563293461874;

		tps.apply( x, y );
		tpsInv.applyInverse( yi, y );
		Assert.assertArrayEquals( "tps warp inv 4", x, yi, EPS );

		xp.setPosition( x );
		tpsInv.apply( xp, yp );
		tpsInv.applyInverse( yip, yp );
		Assert.assertTrue( "tps warp inv 4 pts", almostEqual( xp, yip, EPS ) );

		/* **** RANDOM PT NEG **** */
		x[ 0 ] = -0.24032;
		x[ 1 ] = -0.65288;

		tps.apply( x, y );
		tpsInv.applyInverse( yi, y );
		Assert.assertArrayEquals( "tps warp inv 5", x, yi, EPS );

		xp.setPosition( x );
		tpsInv.apply( xp, yp );
		tpsInv.applyInverse( yip, yp );
		Assert.assertTrue( "tps warp inv 5 pts", almostEqual( xp, yip, EPS ) );

		/* **** RANDOM PT BIG **** */
		x[ 0 ] = 4.983245;
		x[ 1 ] = 3.124307;

		tpsInv.apply( x, y );
		tpsInv.applyInverse( yi, y );
		Assert.assertArrayEquals( "tps warp inv 6", x, yi, EPS );

		xp.setPosition( x );
		tpsInv.apply( xp, yp );
		tpsInv.applyInverse( yip, yp );
		Assert.assertTrue( "tps warp inv 6 pts", almostEqual( xp, yip, EPS ) );
	}

	private void assertInvertibleTransform( final double[] initialPos, final InvertibleRealTransform invertibleTransform, final String message )
	{

		final double[] tmpPos = new double[ initialPos.length ];
		final double[] resultPos = new double[ initialPos.length ];
		invertibleTransform.apply( initialPos, tmpPos );
		invertibleTransform.applyInverse( resultPos, tmpPos );

		Assert.assertArrayEquals( message, initialPos, resultPos, MIDEPS );
	}

	private void assertInvertibleTransform( final RealPoint initialPoint, final InvertibleRealTransform invertibleTransform, final String message )
	{

		final RealPoint tmpPoint = new RealPoint( initialPoint.numDimensions() );
		final RealPoint resultPoint = new RealPoint( initialPoint.numDimensions() );
		invertibleTransform.apply( initialPoint, tmpPoint );
		invertibleTransform.applyInverse( resultPoint, tmpPoint );

		Assert.assertTrue( message, almostEqual( initialPoint, resultPoint, MIDEPS ) );
	}

	@Test
	public void testDeformationFieldInverse()
	{

		final FunctionRealRandomAccessible< RealComposite< FloatType > > wiggleDefField = new FunctionRealRandomAccessible<>( 2, ( pos, target ) -> {
			final double r = Math.sqrt( pos.getDoublePosition( 0 ) * pos.getDoublePosition( 0 ) + pos.getDoublePosition( 1 ) * pos.getDoublePosition( 1 ) );

			target.get( 0 ).setReal( 0.05 * Math.sin( r / 10 ) * pos.getDoublePosition( 1 ) );
			target.get( 1 ).setReal( -0.05 * Math.sin( r / 10 ) * pos.getDoublePosition( 0 ) );

		}, () -> FloatType.createVector( 2 ) );

		final DisplacementFieldTransform wiggleTransform = new DisplacementFieldTransform( wiggleDefField );
		final InvertibleDisplacementFieldTransform inverseTransform = new InvertibleDisplacementFieldTransform( wiggleTransform );
		inverseTransform.getOptimzer().setMaxIters( 1000 );
		inverseTransform.getOptimzer().setTolerance( MIDEPS / 5 );

		final double[] x = new double[] { 0.0f, 0.0f };
		final RealPoint xPoint = new RealPoint( 2 );

		// THE ORIGIN
		int inverseTestNum = 0;

		x[ 0 ] = 0;
		x[ 1 ] = 0;
		xPoint.setPosition( x );
		assertInvertibleTransform( x, inverseTransform, "def field inv " + inverseTestNum );
		assertInvertibleTransform( xPoint, inverseTransform, "def field inv pts" + inverseTestNum++ );

		// A SECOND POINT
		x[ 0 ] = 130;
		x[ 1 ] = -190;
		xPoint.setPosition( x );
		assertInvertibleTransform( x, inverseTransform, "def field inv " + inverseTestNum );
		assertInvertibleTransform( xPoint, inverseTransform, "def field inv pts" + inverseTestNum++ );

		// ANOTHER POINT
		x[ 0 ] = -300;
		x[ 1 ] = 220;
		xPoint.setPosition( x );
		assertInvertibleTransform( x, inverseTransform, "def field inv " + inverseTestNum );
		assertInvertibleTransform( xPoint, inverseTransform, "def field inv pts" + inverseTestNum );
	}

	@Test
	public void testAffineInverse()
	{
		final double[] p = new double[] { 3, 0.5, 30 };
		final double[] pxfm = new double[ 3 ];
		final double[] q = new double[ 3 ];

		final IterativeAffineInverse I3 = new IterativeAffineInverse( 3 );

		I3.apply( p, pxfm );
		I3.applyInverse( q, p );
		Assert.assertArrayEquals( "identity matrix inverse", pxfm, q, EPS );

		final IterativeAffineInverse rot = new IterativeAffineInverse( 3 );

		rot.set( 0.0, 1.0, 0.0, 0.1, -1.0, 0.0, 0.0, 8, 0.0, 0.0, 1.0, 50 );

		// a difficult case in which
		// the optimizer must
		final InverseRealTransformGradientDescent rotinverter = new InverseRealTransformGradientDescent( 3, rot );
		rotinverter.setTolerance( EPS / 20 );

		rot.apply( p, pxfm );
		rotinverter.apply( pxfm, q );

		Assert.assertArrayEquals( "rotation matrix inverse", p, q, EPS );
	}

	/**
	 * Tests that the provided {@lin InvertibleRealTransform} is within EPSILON
	 * of being invertible over the given interval.
	 * 
	 * @param tform
	 * 		the transformation to tests
	 * @param interval 
	 * 		over which to test
	 * @param step
	 * 		the step  
	 * @param EPSILON
	 * 		the allowable error
	 */
	static void inverseTransformTestHelper( InvertibleRealTransform tform, final RealInterval interval, double[] step, final double EPSILON )
	{
		int nd = interval.numDimensions();
		final LocalizingRealIntervalIterator it = new LocalizingRealIntervalIterator( interval, step );
		final RealPoint p = new RealPoint( nd );
		final RealPoint q = new RealPoint( nd );
		while ( it.hasNext() )
		{
			it.fwd();
			tform.apply( it, p );
			tform.applyInverse( q, p );
			assertArrayEquals( 
					String.format( "not invertible at point %s", Arrays.toString( it.positionAsDoubleArray() ) ),
					it.positionAsDoubleArray(),
					q.positionAsDoubleArray(),
					EPSILON );
		}

	}

	private class IterativeAffineInverse extends AffineTransform implements DifferentiableRealTransform
	{
		final AffineTransform jacobian;

		public IterativeAffineInverse( final int n )
		{
			super( n );
			jacobian = new AffineTransform( n );
		}

		@Override
		public void directionToward( final double[] displacement, final double[] x, final double[] y )
		{
			RealTransformFiniteDerivatives.directionToward( jacobian( x ), displacement, x, y );
		}

		@Override
		public AffineTransform jacobian( final double[] x )
		{
			jacobian.set( this.getRowPackedCopy() );
			for ( int d = 0; d < n; d++ )
				jacobian.set( 0.0, d, n );

			return jacobian;
		}

		@Override
		public IterativeAffineInverse copy()
		{
			return new IterativeAffineInverse( jacobian.numDimensions() );
		}

	}
}
