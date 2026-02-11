/*
 * #%L
 * ImgLib2: a general-purpose, multidimensional image processing library.
 * %%
 * Copyright (C) 2009 - 2026 Tobias Pietzsch, Stephan Preibisch, Stephan Saalfeld,
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

package net.imglib2.realtransform.interval;

import java.util.Arrays;

import net.imglib2.FinalRealInterval;
import net.imglib2.RealInterval;
import net.imglib2.iterator.IntervalIterator;
import net.imglib2.realtransform.RealTransform;

/**
 *
 * @author John Bogovic
 * @author Stephan Saalfeld
 */
public class Corners implements IntervalSamplingMethod
{
	/**
	 *
	 * @param interval
	 *            the real interval
	 * @param transform
	 *            the transformation
	 * @return the bounding interval
	 */
	@Override
	public RealInterval bounds( final RealInterval interval, final RealTransform transform )
	{
		final int nd = interval.numDimensions();
		final double[] pt = new double[ nd ];

		final double[] min = new double[ nd ];
		final double[] max = new double[ nd ];
		Arrays.fill( min, Double.POSITIVE_INFINITY );
		Arrays.fill( max, Double.NEGATIVE_INFINITY );

		// iterate over the corners of an nd-hypercube
		final long[] unitInterval = new long[ nd ];
		Arrays.fill( unitInterval, 2 );
		final IntervalIterator it = new IntervalIterator( unitInterval );
		while ( it.hasNext() )
		{
			it.fwd();
			for ( int d = 0; d < nd; d++ )
			{
				if ( it.getLongPosition( d ) == 0 )
					pt[ d ] = interval.realMin( d );
				else
					pt[ d ] = interval.realMax( d );
			}

			transform.apply( pt, pt );
			for ( int d = 0; d < nd; d++ )
			{
				if ( pt[ d ] < min[ d ] )
					min[ d ] = pt[ d ];

				if ( pt[ d ] > max[ d ] )
					max[ d ] = pt[ d ];
			}
		}

		return new FinalRealInterval( min, max, false );
	}
}
