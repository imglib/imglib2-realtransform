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

import java.util.PrimitiveIterator;
import java.util.Random;

import org.junit.Before;
import org.junit.Test;

import net.imglib2.FinalRealInterval;
import net.imglib2.test.ImgLib2Assert;

/**
 * @author Stephan Saalfeld
 *
 */
public class AffineTransform2DTest {

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
		final AffineTransform2D affine = new AffineTransform2D();
		affine.set(
				rnd.nextDouble(), rnd.nextDouble(), rnd.nextDouble(),
				rnd.nextDouble(), rnd.nextDouble(), rnd.nextDouble() );

		final AffineTransform2D dR = new AffineTransform2D();

		for ( int i = 0; i < 100; ++i )
		{
			final double d = rnd.nextDouble() * 8 * Math.PI - 4 * Math.PI;
			final double dcos = Math.cos( d );
			final double dsin = Math.sin( d );

			dR.set(
					dcos, -dsin, 0.0,
					dsin, dcos, 0.0 );

			dR.concatenate( affine );
			affine.rotate( d );

			assertArrayEquals( dR.getRowPackedCopy(), affine.getRowPackedCopy(), 0.001 );
		}
	}

	@Test
	public void testEstimateBounds()
	{
		final int numIterations = 1000;
		final PrimitiveIterator.OfDouble random = rnd.doubles( -100, 100 ).iterator();

		for ( int i = 0; i < numIterations; i++ )
		{
			final double[] min = new double[ 2 ];
			final double[] max = new double[ 2 ];
			for ( int d = 0; d < 2; d++ )
			{
				final double a = random.nextDouble();
				final double b = random.nextDouble();
				min[ d ] = Math.min( a, b );
				max[ d ] = Math.max( a, b );
			}
			final FinalRealInterval interval = new FinalRealInterval( min, max );

			final AffineTransform2D affine = new AffineTransform2D();
			for ( int r = 0; r < 2; r++ )
				for ( int c = 0; c < 3; c++ )
					affine.set( random.nextDouble(), r, c );

			ImgLib2Assert.assertIntervalEquals( affine.estimateBounds( interval ), AffineTransform3DTest.estimateBoundsFromCorners( affine, interval ), 0.0000001 );
		}
	}
}
