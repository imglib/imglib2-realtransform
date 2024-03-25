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

import java.util.Random;

import net.imglib2.RealPoint;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class InvertibleRealTransformSequenceTest
{
	static private AffineTransform3D a1, a2, a3, a123;

	static private double[] x = new double[ 3 ];

	static private double[] y = new double[ 3 ];

	static private RealPoint px = RealPoint.wrap( x );

	static private RealPoint py = RealPoint.wrap( y );

	final static Random rnd = new Random( 0 );

	final static private double r()
	{
		return 10 * 2 * ( rnd.nextDouble() - 0.5 );
	}

	final static void add( final InvertibleRealTransformSequence l )
	{
		l.add( a1 );
		l.add( a2 );
		l.add( a3 );
	}

	@BeforeClass
	public static void setUpBeforeClass() throws Exception
	{
		a1 = new AffineTransform3D();
		a2 = new AffineTransform3D();
		a3 = new AffineTransform3D();

		a1.set(
				r(), r(), r(), r(),
				r(), r(), r(), r(),
				r(), r(), r(), r() );
		a2.set(
				r(), r(), r(), r(),
				r(), r(), r(), r(),
				r(), r(), r(), r() );
		a3.set(
				r(), r(), r(), r(),
				r(), r(), r(), r(),
				r(), r(), r(), r() );

		a123 = new AffineTransform3D();
		a123.preConcatenate( a1 );
		a123.preConcatenate( a2 );
		a123.preConcatenate( a3 );

		x[ 0 ] = r();
		x[ 1 ] = r();
		x[ 2 ] = r();

		a123.apply( x, y );
	}

	@Test
	public void testEmptySequence()
	{
		final InvertibleRealTransformSequence l = new InvertibleRealTransformSequence();
		Assert.assertTrue( "isIdentity is true for empty sequence", l.isIdentity() );

		final double[] x1 = new double[ 3 ];
		final double[] x2 = new double[ 3 ];

		// must behave as identity
		l.apply( y, x1 );
		Assert.assertArrayEquals( "empty sequence behaves as identity", y, x1, 0.001 );

		// must behave as identity
		l.applyInverse( x2, y );
		Assert.assertArrayEquals( "empty sequence inverse behaves as identity", x2, y, 0.001 );

		// ensure a sequence with containing non-identity does not return true
		// for isIdentity
		l.add( a1 );
		Assert.assertFalse( "isIdentity is false for non-identity sequence", l.isIdentity() );
	}

	@Test
	public void testNumSourceDimensions()
	{
		final InvertibleRealTransformSequence l = new InvertibleRealTransformSequence();
		Assert.assertEquals( 0, l.numSourceDimensions() );
		add( l );
		Assert.assertEquals( 3, l.numSourceDimensions() );
	}

	@Test
	public void testNumTargetDimensions()
	{
		final InvertibleRealTransformSequence l = new InvertibleRealTransformSequence();
		Assert.assertEquals( 0, l.numTargetDimensions() );
		add( l );
		Assert.assertEquals( 3, l.numTargetDimensions() );
	}

	@Test
	public void testApplyDoubleArrayDoubleArray()
	{
		final InvertibleRealTransformSequence l = new InvertibleRealTransformSequence();
		add( l );
		final double[] y1 = new double[ 3 ];

		l.apply( x, y1 );
		Assert.assertArrayEquals( y, y1, 0.001 );
	}

	@Test
	public void testApplyFloatArrayFloatArray()
	{
		final InvertibleRealTransformSequence l = new InvertibleRealTransformSequence();
		add( l );
		final float[] x1 = new float[ 3 ];
		x1[ 0 ] = ( float ) x[ 0 ];
		x1[ 1 ] = ( float ) x[ 1 ];
		x1[ 2 ] = ( float ) x[ 2 ];
		final float[] y1 = new float[ 3 ];

		l.apply( x1, y1 );

		final double[] y1d = new double[ 3 ];
		y1d[ 0 ] = y1[ 0 ];
		y1d[ 1 ] = y1[ 1 ];
		y1d[ 2 ] = y1[ 2 ];

		Assert.assertArrayEquals( y, y1d, 0.001 );
	}

	@Test
	public void testApplyRealLocalizableRealPositionable()
	{
		final InvertibleRealTransformSequence l = new InvertibleRealTransformSequence();
		add( l );
		final double[] y1 = new double[ 3 ];
		final RealPoint py1 = RealPoint.wrap( y1 );

		l.apply( px, py1 );
		Assert.assertArrayEquals( y, y1, 0.001 );
	}

	@Test
	public void testApplyInverseDoubleArrayDoubleArray()
	{
		final InvertibleRealTransformSequence l = new InvertibleRealTransformSequence();
		add( l );
		final double[] x1 = new double[ 3 ];

		l.applyInverse( x1, y );
		Assert.assertArrayEquals( x, x1, 0.001 );
	}

	@Test
	public void testApplyInverseFloatArrayFloatArray()
	{
		final InvertibleRealTransformSequence l = new InvertibleRealTransformSequence();
		add( l );
		final float[] x1 = new float[ 3 ];
		final float[] y1 = new float[ 3 ];
		y1[ 0 ] = ( float ) y[ 0 ];
		y1[ 1 ] = ( float ) y[ 1 ];
		y1[ 2 ] = ( float ) y[ 2 ];

		l.applyInverse( x1, y1 );

		final double[] x1d = new double[ 3 ];
		x1d[ 0 ] = x1[ 0 ];
		x1d[ 1 ] = x1[ 1 ];
		x1d[ 2 ] = x1[ 2 ];

		Assert.assertArrayEquals( x, x1d, 0.001 );
	}

	@Test
	public void testApplyInverseRealPositionableRealLocalizable()
	{
		final InvertibleRealTransformSequence l = new InvertibleRealTransformSequence();
		add( l );
		final double[] x1 = new double[ 3 ];
		final RealPoint px1 = RealPoint.wrap( x1 );

		l.applyInverse( px1, py );
		Assert.assertArrayEquals( x, x1, 0.001 );
	}

	@Test
	public void testInverse()
	{
		final InvertibleRealTransformSequence l = new InvertibleRealTransformSequence();
		add( l );
		final InvertibleRealTransform inverse = l.inverse();
		final double[] x1 = new double[ 3 ];

		inverse.apply( y, x1 );
		Assert.assertArrayEquals( x, x1, 0.001 );
	}

	@Test
	public void testCopy()
	{
		final InvertibleRealTransformSequence l = new InvertibleRealTransformSequence();
		add( l );

		final InvertibleRealTransformSequence lCopy = l.copy();

		/* copy is a deep copy */
		Assert.assertNotSame( l, lCopy );
		for ( int i = 0; i < l.transforms.size(); ++i )
			Assert.assertNotSame( l.transforms.get( i ), lCopy.transforms.get( i ) );

		/* copy performs correctly */
		final double[] x1 = new double[ 3 ];
		final double[] y1 = new double[ 3 ];
		lCopy.apply( x, y1 );
		Assert.assertArrayEquals( y, y1, 0.001 );
		lCopy.applyInverse( x1, y );
		Assert.assertArrayEquals( x, x1, 0.001 );
	}
}
