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

public class ThinPlateSplineTest
{
	final double EPS = 1e-5;

	@Test
	public void testTps()
	{
		int ndims = 2;
		double[][] srcPts = new double[][] { 
			{ -1.0, 0.0, 1.0, 0.0, -1.0, 1.0 }, // x
			{ 0.0, -1.0, 0.0, 1.0, -1.0, 1.0 } }; // y

		double[][] tgtPts = new double[][] { 
			{ -2.0, -1.0, 0.0, -1.0, -2.0, 0.0 }, // x
			{ -2.0, -3.0, -2.0, -1.0, -3.0, -1.0 } }; // y

		ThinplateSplineTransform tps = new ThinplateSplineTransform( srcPts, tgtPts );

		double[] p = new double[ ndims ];
		double[] q = new double[ ndims ];
		double[] qtrue = new double[ ndims ];

		for ( int i = 0; i < srcPts[ 0 ].length; i++ )
		{
			p[ 0 ] = srcPts[ 0 ][ i ];
			p[ 1 ] = srcPts[ 1 ][ i ];

			qtrue[ 0 ] = tgtPts[ 0 ][ i ];
			qtrue[ 1 ] = tgtPts[ 1 ][ i ];

			tps.apply( p, q );
			Assert.assertArrayEquals( "double apply " + i, q, qtrue, EPS );

			tps.apply( p, p );
			Assert.assertArrayEquals( "double apply in place" + i, p, qtrue, EPS );
		}

	}
}
