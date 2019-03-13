package net.imglib2.realtransform;

import java.util.function.BiConsumer;

import org.junit.Assert;
import org.junit.Test;

import net.imglib2.RealLocalizable;
import net.imglib2.RealPoint;
import net.imglib2.position.FunctionRealRandomAccessible;
import net.imglib2.realtransform.inverse.DifferentiableRealTransform;
import net.imglib2.realtransform.inverse.InverseRealTransformGradientDescent;
import net.imglib2.realtransform.inverse.RealTransformFiniteDerivatives;
import net.imglib2.realtransform.inverse.WrappedIterativeInvertibleRealTransform;
import net.imglib2.type.numeric.real.FloatType;

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
		double[][] srcPts = new double[][]{
				{ -1.0, 0.0, 1.0, 0.0 }, // x
				{ 0.0, -1.0, 0.0, 1.0 } }; // y

		double[][] tgtPts = new double[][]{
				{ -2.0, 0.0, 2.0, 0.0 }, // x
				{ 0.0, -2.0, 0.0, 2.0 } }; // y


		ThinplateSplineTransform tps = new ThinplateSplineTransform( srcPts, tgtPts );
		WrappedIterativeInvertibleRealTransform<ThinplateSplineTransform> tpsInv = new WrappedIterativeInvertibleRealTransform<>( tps );

		double[] x = new double[]{ 0.5, 0.5 };
		double[] y  = new double[ 2 ];
		double[] yest = new double[ 2 ];
		double[] yestinv = new double[ 2 ];
		
		tpsInv.getOptimzer().setTolerance( EPS / 2 );
		
		tps.apply( x, y );
		tpsInv.applyInverse( yest, y );
		tps.apply( yest, yestinv );
		
		Assert.assertArrayEquals("tps", x, yest, EPS );
	}

	public static boolean almostEqual( RealPoint p, RealPoint q, double eps )
	{
		assert p.numDimensions() == q.numDimensions();

		for( int i = 0; i< p.numDimensions(); i++ )
		{
			if( Math.abs( p.getDoublePosition( i ) - q.getDoublePosition( i )) > eps )
			{
				//System.err.println( "p: "  + p );
				//System.err.println( "q: "  + q );
				return false;
			}
		}

		return true;
	}

	@Test
	public void testTpsInverse()
	{
		/*
		 * Warp
		 */
		final double[][] src_simple = new double[][]
		{
				{ 0, 0, 0, 1, 1, 1, 2, 2, 2 }, // x
				{ 0, 1, 2, 0, 1, 2, 0, 1, 2 }, // y
		};
		// target points
		final double[][] tgt = new double[][]
		{
				{ -0.5, -0.5, -0.5, 1.5, 1.5, 1.5, 2.0, 2.0, 2.0 }, // x
				{ -0.5, 1.5, 2.0, -0.5, 1.5, 2.0, -0.5, 1.5, 2.0 } // y
		};

		final ThinplateSplineTransform tps = new ThinplateSplineTransform( src_simple, tgt );
		WrappedIterativeInvertibleRealTransform<ThinplateSplineTransform> tpsInv = new WrappedIterativeInvertibleRealTransform<>( tps );

		/* **** PT 1 **** */
		double[] x = new double[]{ 0.0f, 0.0f };
		double[] y = new double[ 2 ];
		double[] yi = new double[ 2 ];

		RealPoint xp = new RealPoint( 2 );
		RealPoint yp = new RealPoint( 2 );
		RealPoint yip = new RealPoint( 2 );

		tps.apply( x, y );
		tpsInv.applyInverse( yi, y );
		Assert.assertArrayEquals("tps warp inv 1", x, yi, EPS );

		xp.setPosition( x );
		tpsInv.apply( xp, yp );
		tpsInv.applyInverse( yip, yp );
		Assert.assertTrue( "tps warp inv 1 pts", almostEqual( xp, yip, EPS ));



		/* **** PT 2 **** */
		x[ 0 ] = 0.5;
		x[ 1 ] = 0.5;

		tps.apply( x, y );
		tpsInv.applyInverse( yi, y );
		Assert.assertArrayEquals("tps warp inv 2", x, yi, EPS );

		xp.setPosition( x );
		tpsInv.apply( xp, yp );
		tpsInv.applyInverse( yip, yp );
		Assert.assertTrue( "tps warp inv 2 pts", almostEqual( xp, yip, EPS ));


		/* **** PT 3 **** */
		x[ 0 ] = 1;
		x[ 1 ] = 1;

		tps.apply( x, y );
		tpsInv.applyInverse( yi, y );
		Assert.assertArrayEquals("tps warp inv 3", x, yi, EPS );

		xp.setPosition( x );
		tpsInv.apply( xp, yp );
		tpsInv.applyInverse( yip, yp );
		Assert.assertTrue( "tps warp inv 3 pts", almostEqual( xp, yip, EPS ));


		/* **** RANDOM PT SMALL **** */
		x[ 0 ] = 0.6198672937136046;
		x[ 1 ] = 0.3758563293461874;

		tps.apply( x, y );
		tpsInv.applyInverse( yi, y );
		Assert.assertArrayEquals("tps warp inv 4", x, yi, EPS );

		xp.setPosition( x );
		tpsInv.apply( xp, yp );
		tpsInv.applyInverse( yip, yp );
		Assert.assertTrue( "tps warp inv 4 pts", almostEqual( xp, yip, EPS ));


		/* **** RANDOM PT NEG **** */
		x[ 0 ] = -0.24032;
		x[ 1 ] = -0.65288;

		tps.apply( x, y );
		tpsInv.applyInverse( yi, y );
		Assert.assertArrayEquals("tps warp inv 5", x, yi, EPS );

		xp.setPosition( x );
		tpsInv.apply( xp, yp );
		tpsInv.applyInverse( yip, yp );
		Assert.assertTrue( "tps warp inv 5 pts", almostEqual( xp, yip, EPS ));

		/* **** RANDOM PT BIG **** */
		x[ 0 ] = 4.983245;
		x[ 1 ] = 3.124307;

		tpsInv.apply( x, y );
		tpsInv.applyInverse( yi, y );
		Assert.assertArrayEquals("tps warp inv 6", x, yi, EPS );

		xp.setPosition( x );
		tpsInv.apply( xp, yp );
		tpsInv.applyInverse( yip, yp );
		Assert.assertTrue( "tps warp inv 6 pts", almostEqual( xp, yip, EPS ));
	}

	@Test
	public void testDeformationFieldInverse()
	{
		FunctionRealRandomAccessible< FloatType > wigglyDefField = new FunctionRealRandomAccessible<>( 3, new BiConsumer< RealLocalizable, FloatType >()
		{
			@Override
			public void accept( RealLocalizable t, FloatType u )
			{
				double r = Math.sqrt( t.getDoublePosition( 0 ) * t.getDoublePosition( 0 ) + t.getDoublePosition( 1 ) * t.getDoublePosition( 1 ) );

				if ( t.getDoublePosition( 2 ) < 0.5 )
				{
					// x-coordinate field
					u.setReal( 0.05 * Math.sin( r / 10 ) * t.getDoublePosition( 1 ) );
				}
				else
				{
					// y-coordinate field
					u.setReal( -0.05 * Math.sin( r / 10 ) * t.getDoublePosition( 0 ) );
				}
			}
		}, FloatType::new );

		DeformationFieldTransform< FloatType > defTransform = new DeformationFieldTransform<>( wigglyDefField );
		InvertibleDeformationFieldTransform< FloatType > invDef = new InvertibleDeformationFieldTransform<>( defTransform );
		invDef.getOptimzer().setMaxIters( 1000 );
		invDef.getOptimzer().setTolerance( MIDEPS / 5 );

		double[] x = new double[] { 0.0f, 0.0f };
		double[] y = new double[ 2 ];
		double[] yi = new double[ 2 ];

		RealPoint xp = new RealPoint( 2 );
		RealPoint yp = new RealPoint( 2 );
		RealPoint yip = new RealPoint( 2 );

		// THE ORIGIN
		x[ 0 ] = 0;
		x[ 1 ] = 0;

		int i = 0;
		invDef.apply( x, y );
		invDef.applyInverse( yi, y );
		Assert.assertArrayEquals( "tps warp inv " + i, x, yi, MIDEPS );

		xp.setPosition( x );
		invDef.apply( xp, yp );
		invDef.applyInverse( yip, yp );
		Assert.assertTrue( "tps warp inv pts " + i, almostEqual( xp, yip, MIDEPS ) );

		// A SECOND POINT
		i++;
		x[ 0 ] = 130;
		x[ 1 ] = -190;

		invDef.apply( x, y );
		invDef.applyInverse( yi, y );
		Assert.assertArrayEquals( "tps warp inv " + i, x, yi, MIDEPS );

		xp.setPosition( x );
		invDef.apply( xp, yp );
		invDef.applyInverse( yip, yp );
		Assert.assertTrue( "tps warp inv pts " + i, almostEqual( xp, yip, MIDEPS ) );

		// ANOTHER POINT
		i++;
		x[ 0 ] = -300;
		x[ 1 ] = 220;

		invDef.apply( x, y );
		invDef.applyInverse( yi, y );
		Assert.assertArrayEquals( "tps warp inv " + i, x, yi, MIDEPS );

		xp.setPosition( x );
		invDef.apply( xp, yp );
		invDef.applyInverse( yip, yp );
		Assert.assertTrue( "tps warp inv pts " + i, almostEqual( xp, yip, MIDEPS ) );
	}

	@Test
	public void testAffineInverse()
	{
		double[] p = new double[]{ 3, 0.5, 30 };
		double[] pxfm = new double[ 3 ];
		double[] q = new double[ 3 ];

		IterativeAffineInverse I3 = new IterativeAffineInverse( 3 );
		
		
		I3.apply( p, pxfm );
		I3.applyInverse( q, p );
		Assert.assertArrayEquals( "identity matrix inverse", pxfm, q, EPS );

		IterativeAffineInverse rot = new IterativeAffineInverse( 3 );

		rot.set( 0.0, 1.0, 0.0, 0.1,
				 -1.0, 0.0, 0.0, 8,
				 0.0, 0.0, 1.0, 50 );
		
		
		// a difficult case in which
		// the optimizer must 
		InverseRealTransformGradientDescent rotinverter = new InverseRealTransformGradientDescent( 3, rot );
		rotinverter.setTolerance( EPS / 20 );

		rot.apply( p, pxfm );
		rotinverter.apply( pxfm, q );

		Assert.assertArrayEquals( "rotation matrix inverse", p, q, EPS );
	}
	
	private class IterativeAffineInverse extends AffineTransform implements DifferentiableRealTransform
	{
		final AffineTransform jacobian;

		public IterativeAffineInverse( int n )
		{
			super( n );
			jacobian = new AffineTransform( n );
		}

		@Override
		public void directionToward( double[] displacement, double[] x, double[] y )
		{
			RealTransformFiniteDerivatives.directionToward( jacobian(x), displacement, x, y );
		}

		@Override
		public AffineTransform jacobian( double[] x )
		{
			jacobian.set( this.getRowPackedCopy() );
			for( int d = 0; d < n; d++ )
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
