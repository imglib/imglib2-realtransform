/*
 * #%L
 * ImgLib2: a general-purpose, multidimensional image processing library.
 * %%
 * Copyright (C) 2009 - 2016 Tobias Pietzsch, Stephan Preibisch, Stephan Saalfeld,
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

import org.junit.Assert;
import org.junit.Test;

import net.imglib2.Cursor;
import net.imglib2.FinalInterval;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.RealPoint;
import net.imglib2.img.array.ArrayCursor;
import net.imglib2.img.array.ArrayImg;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.img.basictypeaccess.array.DoubleArray;
import net.imglib2.interpolation.randomaccess.NLinearInterpolatorFactory;
import net.imglib2.interpolation.randomaccess.NearestNeighborInterpolatorFactory;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.ConstantUtils;
import net.imglib2.view.IntervalView;
import net.imglib2.view.Views;

public class DeformationFieldTest
{

	@Test
	public void testNumSourceTargetDimensions()
	{
		// a deformation field representing a 2d transform
		IntervalView< FloatType > defRai = Views.interval(
				ConstantUtils.constantRandomAccessible( new FloatType( 0.0f ), 3 ),
				new FinalInterval( 10, 10, 2 ) );

		DeformationFieldTransform< FloatType > def2d = new DeformationFieldTransform< >(
				defRai );

		Assert.assertEquals( 2, def2d.numSourceDimensions() );
		Assert.assertEquals( 2, def2d.numTargetDimensions() );

		// a deformation field representing a 3d transform
		IntervalView<FloatType> defRai3d = Views.interval(
				ConstantUtils.constantRandomAccessible( new FloatType( 0.0f ), 4 ),
				new FinalInterval( 10, 10, 10, 3 ) );

		DeformationFieldTransform< FloatType > def3d = new DeformationFieldTransform< >(
				defRai3d );

		Assert.assertEquals( 3, def3d.numSourceDimensions() );
		Assert.assertEquals( 3, def3d.numTargetDimensions() );
	}

	@Test
	public void testTranslation()
	{
		final double EPS = 1e-5;

		IntervalView< FloatType > defRai = Views.interval(
				ConstantUtils.constantRandomAccessible( new FloatType( 1.0f ), 3 ),
				new FinalInterval( 10, 10, 2 ) );

		DeformationFieldTransform< FloatType > def2d = new DeformationFieldTransform< >(
				defRai );

		double[] p = new double[]{ 5.0, 4.0 };
		double[] pxfm = new double[]{ 6.0, 5.0 };
		double[] q = new double[ 2 ];

		def2d.apply( p, q );
		Assert.assertArrayEquals( pxfm, q, EPS );

		float[] pf = new float[]{ 5.0f, 4.0f };
		float[] pxfmf = new float[]{ 6.0f, 5.0f };
		float[] qf = new float[ 2 ];

		def2d.apply( pf, qf );
		Assert.assertArrayEquals( pxfmf, qf, (float)EPS );

		RealPoint src = new RealPoint( 5.0, 4.0 );
		RealPoint tgt = new RealPoint( 2 );
		def2d.apply( src, tgt );
		Assert.assertEquals( tgt.getDoublePosition( 0 ), 6.0, EPS );
		Assert.assertEquals( tgt.getDoublePosition( 1 ), 5.0, EPS );
	}

	@Test
	public void testGradient()
	{
		final double EPS = 1e-5;

		RandomAccessibleInterval<FloatType> defRai = ArrayImgs.floats( 10, 10, 2 );
		Cursor<FloatType> c = Views.flatIterable(defRai).cursor();
		while( c.hasNext() )
		{
			c.fwd();
			if( c.getIntPosition( 2 ) == 0 )
				c.get().set( c.getFloatPosition(0) );
		}

		DeformationFieldTransform< FloatType > def2d = new DeformationFieldTransform< >(
				Views.interpolate( defRai, new NearestNeighborInterpolatorFactory< FloatType >() ));

		double[] p = new double[]{ 5.0, 4.0 };
		double[] pxfm = new double[]{ 10.0, 4.0 };
		double[] q = new double[ 2 ];

		def2d.apply( p, q );
		Assert.assertArrayEquals( pxfm, q, EPS );

		float[] pf = new float[]{ 5.0f, 4.0f };
		float[] pxfmf = new float[]{ 10.0f, 4.0f };
		float[] qf = new float[ 2 ];

		def2d.apply( pf, qf );
		Assert.assertArrayEquals( pxfmf, qf, (float)EPS );

		RealPoint src = new RealPoint( 10.0, 4.0 );
		RealPoint tgt = new RealPoint( 2 );
		def2d.apply( src, tgt );
		Assert.assertEquals( tgt.getDoublePosition( 0 ), 10.0, EPS );
		Assert.assertEquals( tgt.getDoublePosition( 1 ), 4.0, EPS );
	}

	@Test
	public void testRender()
	{
		FinalInterval interval4d = new FinalInterval( 4, 4, 4, 3 );
		FinalInterval interval = new FinalInterval( 4, 4, 4 );

		// make a deformation field with a vector [ 1, 1, 1 ] everywhere
		IntervalView< FloatType > defRai = Views.interval(
				ConstantUtils.constantRandomAccessible( new FloatType( 1.0f ), 4 ),
				interval4d );

		DeformationFieldTransform< FloatType > def3d = new DeformationFieldTransform< >(
				defRai );

		// make a dummy image
		ArrayImg< DoubleType, DoubleArray > im = ArrayImgs.doubles( 4, 4, 4 );
		ArrayCursor< DoubleType > c = im.cursor();
		double x = 0;
		while( c.hasNext())
			c.next().set( x++ );

		RealTransformRandomAccessible< DoubleType, RealTransform > imXfmReal =
				new RealTransformRandomAccessible< >(
					Views.interpolate( Views.extendZero( im ), 
							new NLinearInterpolatorFactory< DoubleType >() ),
					def3d);

		IntervalView< DoubleType > imXfm = Views.interval( Views.raster( imXfmReal ), interval);

		RandomAccess< DoubleType > imraO = im.randomAccess();
		imraO.setPosition( new int[]{2,2,2} );

		RandomAccess< DoubleType > imra = imXfm.randomAccess();
		imra.setPosition( new int[]{1,1,1} );
	}
}
