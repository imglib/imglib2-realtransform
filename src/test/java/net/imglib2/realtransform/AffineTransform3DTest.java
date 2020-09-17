/*
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

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.PrimitiveIterator;
import java.util.Random;
import net.imglib2.FinalRealInterval;
import net.imglib2.RealInterval;
import net.imglib2.test.ImgLib2Assert;
import net.imglib2.util.Util;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Stephan Saalfeld
 *
 */
public class AffineTransform3DTest
{

	protected Random rnd = new Random( 0 );
	
	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception
	{
		 rnd.setSeed( 0 );
	}

	@Test
	public void testRotate()
	{
		final AffineTransform3D affine = new AffineTransform3D();
		affine.set(
				rnd.nextDouble(), rnd.nextDouble(), rnd.nextDouble(), rnd.nextDouble(),
				rnd.nextDouble(), rnd.nextDouble(), rnd.nextDouble(), rnd.nextDouble(),
				rnd.nextDouble(), rnd.nextDouble(), rnd.nextDouble(), rnd.nextDouble() );
		
		final AffineTransform3D dR = new AffineTransform3D();
		
		for ( int i = 0; i < 100; ++i )
		{
			final double d = rnd.nextDouble() * 8 * Math.PI - 4 * Math.PI;
			final double dcos = Math.cos( d );
			final double dsin = Math.sin( d );
			
			final int axis = rnd.nextInt( 3 );
		
			switch ( axis )
			{
			case 0:
				dR.set(
						1.0f, 0.0f, 0.0f, 0.0f,
						0.0f, dcos, -dsin, 0.0f,
						0.0f, dsin, dcos, 0.0f );
				break;
			case 1:
				dR.set(
						dcos, 0.0f, dsin, 0.0f,
						0.0f, 1.0f, 0.0f, 0.0f,
						-dsin, 0.0f, dcos, 0.0f );
				break;
			default:
				dR.set(
						dcos, -dsin, 0.0f, 0.0f,
						dsin, dcos, 0.0f, 0.0f,
						0.0f, 0.0f, 1.0f, 0.0f );
				break;
			}

			dR.concatenate( affine );
			affine.rotate( axis, d );

			assertArrayEquals( dR.getRowPackedCopy(), affine.getRowPackedCopy(), 0.001 );
			assertArrayEquals( dR.inverse().getRowPackedCopy(), affine.inverse().getRowPackedCopy(), 0.001 );
		}
	}
	
	@Test
	public void testScale()
	{
		final AffineTransform3D affine = new AffineTransform3D();
		affine.set(
				rnd.nextDouble(), rnd.nextDouble(), rnd.nextDouble(), rnd.nextDouble(),
				rnd.nextDouble(), rnd.nextDouble(), rnd.nextDouble(), rnd.nextDouble(),
				rnd.nextDouble(), rnd.nextDouble(), rnd.nextDouble(), rnd.nextDouble() );
		
		final AffineTransform3D dR = new AffineTransform3D();
		
		for ( int i = 0; i < 100; ++i )
		{
			final double s = rnd.nextDouble() * 20 - 10;
			
			dR.set(
					s, 0.0, 0.0, 0.0,
					0.0, s, 0.0, 0.0,
					0.0, 0.0, s, 0.0 );
			
			dR.concatenate( affine );
			affine.scale( s );

			assertArrayEquals( dR.getRowPackedCopy(), affine.getRowPackedCopy(), 0.001 );
			assertArrayEquals( dR.inverse().getRowPackedCopy(), affine.inverse().getRowPackedCopy(), 0.001 );
		}
	}
	
	@Test
	public void testTranslation()
	{
		double[] translation = new double[]{rnd.nextDouble(), rnd.nextDouble(), rnd.nextDouble()};
		double[] inverseTranslation = new double[]{-translation[0], -translation[1], -translation[2]};
		
		final AffineTransform3D affine = new AffineTransform3D();
		affine.set(
				rnd.nextDouble(), rnd.nextDouble(), rnd.nextDouble(), translation[0],
				rnd.nextDouble(), rnd.nextDouble(), rnd.nextDouble(), translation[1],
				rnd.nextDouble(), rnd.nextDouble(), rnd.nextDouble(), translation[2] );
		
		AffineTransform3D toBeOrigin = affine.copy();
		double[] translationFromOrigin = affine.getTranslation();
		
		//Move to origin and test
		toBeOrigin.translate(inverseTranslation);
		assertArrayEquals( toBeOrigin.getTranslation(), new double[]{0, 0, 0}, 0.001 );
		assertArrayEquals( toBeOrigin.inverse().getTranslation(), new double[]{ 0, 0, 0 }, 0.001 );

		//Move to origin the easy way
		toBeOrigin = affine.copy();
		toBeOrigin.setTranslation(0, 0, 0);
		assertArrayEquals( toBeOrigin.getTranslation(), new double[]{0, 0, 0}, 0.001 );
		assertArrayEquals( toBeOrigin.inverse().getTranslation(), new double[]{ 0, 0, 0 }, 0.001 );

		//Move back to initial position
		final AffineTransform3D backToOriginal = toBeOrigin.copy();
		backToOriginal.translate(translationFromOrigin);

		assertArrayEquals( backToOriginal.getRowPackedCopy(), affine.getRowPackedCopy(), 0.001 );
		assertArrayEquals( backToOriginal.inverse().getRowPackedCopy(), affine.inverse().getRowPackedCopy(), 0.001 );
	}

	@Test
	public void testEstimateBounds()
	{
		final int numIterations = 1000;
		final PrimitiveIterator.OfDouble random = rnd.doubles( -100, 100 ).iterator();

		for ( int i = 0; i < numIterations; i++ )
		{
			final double[] min = new double[ 3 ];
			final double[] max = new double[ 3 ];
			for ( int d = 0; d < 3; d++ )
			{
				final double a = random.nextDouble();
				final double b = random.nextDouble();
				min[ d ] = Math.min( a, b );
				max[ d ] = Math.max( a, b );
			}
			final FinalRealInterval interval = new FinalRealInterval( min, max );

			final AffineTransform3D affine = new AffineTransform3D();
			for ( int r = 0; r < 3; r++ )
				for ( int c = 0; c < 4; c++ )
					affine.set( random.nextDouble(), r, c );

			ImgLib2Assert.assertIntervalEquals( affine.estimateBounds( interval ), estimateBoundsFromCorners( affine, interval ), 0.0000001 );
		}
	}

	public static FinalRealInterval estimateBoundsFromCorners( final RealTransform transform, final RealInterval interval )
	{
		assert interval.numDimensions() == transform.numSourceDimensions();

		final int n = transform.numSourceDimensions();
		final double[] corner = new double[ n ];

		final int m = transform.numTargetDimensions();
		final double[] tcorner = new double[ m ];

		final double[] rMin = new double[ m ];
		final double[] rMax = new double[ m ];
		Arrays.fill( rMin, Double.POSITIVE_INFINITY );
		Arrays.fill( rMax, Double.NEGATIVE_INFINITY );

		final int nCorners = 1 << n;
		for ( int i = 0; i < nCorners; i++ )
		{
			for ( int  d = 0, mask = 1; d < n; ++d, mask = mask << 1 )
				corner[ d ] = ( i & mask ) == 0 ? interval.realMin( d ) : interval.realMax( d );
			transform.apply( corner, tcorner );
			Util.min( rMin, tcorner );
			Util.max( rMax, tcorner );
		}

		return new FinalRealInterval( rMin, rMax );
	}
}
