/*
 * #%L
 * ImgLib2: a general-purpose, multidimensional image processing library.
 * %%
 * Copyright (C) 2009 - 2024 Tobias Pietzsch, Stephan Preibisch, Stephan Saalfeld,
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
import static org.junit.Assert.assertEquals;

import java.util.function.BiConsumer;
import java.util.stream.DoubleStream;
import java.util.stream.Stream;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import net.imglib2.Cursor;
import net.imglib2.FinalInterval;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.RealPoint;
import net.imglib2.RealRandomAccessible;
import net.imglib2.img.array.ArrayCursor;
import net.imglib2.img.array.ArrayImg;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.img.basictypeaccess.array.DoubleArray;
import net.imglib2.interpolation.randomaccess.NLinearInterpolatorFactory;
import net.imglib2.iterator.IntervalIterator;
import net.imglib2.loops.LoopBuilder;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.ConstantUtils;
import net.imglib2.util.Intervals;
import net.imglib2.view.IntervalView;
import net.imglib2.view.Views;
import net.imglib2.view.composite.RealComposite;

public class DisplacementFieldTest
{
	DisplacementFieldTransform defgrad;

	final double EPS = 1e-5;

	@Test
	public void testNumSourceTargetDimensions()
	{
		// a deformation field representing a 2d transform
		final IntervalView< FloatType > defRai = Views.interval( ConstantUtils.constantRandomAccessible( new FloatType( 0.0f ), 3 ), new FinalInterval( 10, 10, 2 ) );

		final DisplacementFieldTransform def2d = new DisplacementFieldTransform( convertToDefFieldInput( defRai ) );

		Assert.assertEquals( 2, def2d.numSourceDimensions() );
		Assert.assertEquals( 2, def2d.numTargetDimensions() );

		// a deformation field representing a 3d transform
		final IntervalView< FloatType > defRai3d = Views.interval( ConstantUtils.constantRandomAccessible( new FloatType( 0.0f ), 4 ), new FinalInterval( 10, 10, 10, 3 ) );

		final DisplacementFieldTransform def3d = new DisplacementFieldTransform( convertToDefFieldInput( defRai3d ) );

		Assert.assertEquals( 3, def3d.numSourceDimensions() );
		Assert.assertEquals( 3, def3d.numTargetDimensions() );
	}

	@Test
	public void testTranslation()
	{
		final IntervalView< FloatType > defRai = Views.interval( ConstantUtils.constantRandomAccessible( new FloatType( 1.0f ), 3 ), new FinalInterval( 10, 10, 2 ) );

		final DisplacementFieldTransform def2d = new DisplacementFieldTransform( convertToDefFieldInput( defRai ) );

		final double[] p = new double[] { 5.0, 4.0 };
		final double[] pxfm = new double[] { 6.0, 5.0 };
		final double[] q = new double[ 2 ];

		def2d.apply( p, q );
		Assert.assertArrayEquals( pxfm, q, EPS );

		def2d.apply( p, p );
		Assert.assertArrayEquals( "double apply in place", pxfm, p, EPS );

		final RealPoint src = new RealPoint( 5.0, 4.0 );
		final RealPoint tgt = new RealPoint( 2 );
		def2d.apply( src, tgt );
		Assert.assertEquals( tgt.getDoublePosition( 0 ), 6.0, EPS );
		Assert.assertEquals( tgt.getDoublePosition( 1 ), 5.0, EPS );

		// in place
		def2d.apply( src, src );
		Assert.assertEquals( "positionable in place x", src.getDoublePosition( 0 ), 6.0, EPS );
		Assert.assertEquals( "positionable in place y", src.getDoublePosition( 1 ), 5.0, EPS );
	}

	@Test
	public void testGradient()
	{
		final double[] p = new double[] { 5.0, 4.0 };
		final double[] pxfm = new double[] { 10.0, 4.0 };
		final double[] q = new double[ 2 ];

		defgrad.apply( p, q );
		Assert.assertArrayEquals( pxfm, q, EPS );

//		float[] pf = new float[]{ 5.0f, 4.0f };
//		float[] pxfmf = new float[]{ 10.0f, 4.0f };
//		float[] qf = new float[ 2 ];
//
//		defgrad.apply( pf, qf );
//		Assert.assertArrayEquals( pxfmf, qf, (float)EPS );

		final RealPoint src = new RealPoint( p );
		final RealPoint tgt = new RealPoint( 2 );
		defgrad.apply( src, tgt );
		Assert.assertEquals( tgt.getDoublePosition( 0 ), pxfm[ 0 ], EPS );
		Assert.assertEquals( tgt.getDoublePosition( 1 ), pxfm[ 1 ], EPS );
	}

	@Test
	public void testRender()
	{
		final FinalInterval interval4d = new FinalInterval( 4, 4, 4, 3 );
		final FinalInterval interval = new FinalInterval( 4, 4, 4 );

		// make a deformation field with a vector [ 1, 1, 1 ] everywhere
		final IntervalView< FloatType > defRai = Views.interval( ConstantUtils.constantRandomAccessible( new FloatType( 1.0f ), 4 ), interval4d );

		final DisplacementFieldTransform def3d = new DisplacementFieldTransform( convertToDefFieldInput( defRai ) );

		// make a dummy image
		final ArrayImg< DoubleType, DoubleArray > im = ArrayImgs.doubles( 4, 4, 4 );
		final ArrayCursor< DoubleType > c = im.cursor();
		double x = 0;
		/* Set the x-displacemnt equal to the x-position */
		while ( c.hasNext() )
			c.next().set( x++ );

		final RealTransformRandomAccessible< DoubleType, RealTransform > imXfmReal = new RealTransformRandomAccessible<>( Views.interpolate( Views.extendZero( im ), new NLinearInterpolatorFactory<>() ), def3d );

		final IntervalView< DoubleType > imXfm = Views.interval( Views.raster( imXfmReal ), interval );

		final RandomAccess< DoubleType > imraO = im.randomAccess();
		imraO.setPosition( new int[] { 2, 2, 2 } );

		final RandomAccess< DoubleType > imra = imXfm.randomAccess();
		imra.setPosition( new int[] { 1, 1, 1 } );
	}

	@Test
	public void testInverse()
	{
		final InvertibleDisplacementFieldTransform dfieldGradInv = new InvertibleDisplacementFieldTransform( defgrad );
		dfieldGradInv.getOptimzer().setTolerance( EPS / 2 );

		final double[] p = new double[] { 5.0, 4.0 };
		final double[] pxfm = new double[] { 10.0, 4.0 };
		final double[] q = new double[ 2 ];

		dfieldGradInv.apply( p, q );

		// apply inverse to destination and ensure it goes to the source point
		dfieldGradInv.applyInverse( q, pxfm );
		Assert.assertArrayEquals( p, q, EPS );
	}

	@Test
	public void testDifferentDimensions()
	{
		final double EPS = 1e-9;
		for ( int nt = 1; nt < 5; nt++ )
			for ( int ns = nt; ns < 5; ns++ )
			{
				final RealPoint p = new RealPoint( ns );
				final RealPoint q = new RealPoint( nt );
				final RealComposite< DoubleType > vec = DoubleType.createVector( nt );
				vec.setOne();

				final DisplacementFieldTransform dfield = new DisplacementFieldTransform(
						ConstantUtils.constantRealRandomAccessible( vec, 1 ) );

				dfield.apply( p, p );
				assertEquals( p.getDoublePosition( 0 ), 1, EPS );
				p.setPosition( 0, 0 );

				dfield.apply( p, q );
				assertEquals( q.getDoublePosition( 0 ), 1, EPS );
			}
	}

	@Test
	public void testRasterize()
	{
		final double EPS = 1e-9;
		final AffineTransform2D xfm = new AffineTransform2D();
		xfm.translate( 3, 4 );
		xfm.scale( 0.3, 1.3 );

		final FinalInterval interval = Intervals.createMinMax( 0, 0, 3, 3 );
		final double[] spacing = new double[] { 0.5, 2 };
		final double[] offset = new double[] { -1, 2 };
		final ScaleAndTranslation toPhysical = new ScaleAndTranslation( spacing, offset );

		final RandomAccessibleInterval< DoubleType > dfieldImg = DisplacementFieldTransform.createDisplacementField( xfm, interval, spacing, offset, () -> { return DoubleType.createVector(2); } );
		final DisplacementFieldTransform dfield = new DisplacementFieldTransform( dfieldImg, spacing, offset );

		final RealPoint p = new RealPoint(2);
		final RealPoint qTrue = new RealPoint(2);
		final RealPoint qDfield = new RealPoint(2);
		final IntervalIterator it = new IntervalIterator( interval );
		while( it.hasNext())
		{
			it.fwd();
			toPhysical.apply( it, p );

			xfm.apply( p, qTrue );
			dfield.apply( p, qDfield );
			assertArrayEquals( qTrue.positionAsDoubleArray(), qDfield.positionAsDoubleArray(), EPS );
		}
	}

	@Before
	public void setUp()
	{
		final RandomAccessibleInterval< FloatType > defRai = ArrayImgs.floats( 11, 11, 2 );
		final Cursor< FloatType > c = Views.flatIterable( defRai ).cursor();
		while ( c.hasNext() )
		{
			c.fwd();
			if ( c.getIntPosition( 2 ) == 0 )
				c.get().set( c.getFloatPosition( 0 ) );
		}
		final RandomAccessibleInterval< FloatType > moved = Views.moveAxis( defRai, 2, 0 );

		defgrad = new DisplacementFieldTransform( moved );
	}

	private RealRandomAccessible< RealComposite< FloatType > > convertToDefFieldInput( final RandomAccessibleInterval< FloatType > rai )
	{

		return Views.interpolate( Views.extendBorder( Views.collapseReal( rai ) ), new NLinearInterpolatorFactory<>() );
	}

	@After
	public void tearDown()
	{
		defgrad = null;
	}

}
