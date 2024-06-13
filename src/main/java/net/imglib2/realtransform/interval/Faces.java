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

package net.imglib2.realtransform.interval;

import java.util.Arrays;

import net.imglib2.FinalRealInterval;
import net.imglib2.RealInterval;
import net.imglib2.iterator.LocalizingRealIntervalIterator;
import net.imglib2.realtransform.RealTransform;

/**
 * @author John Bogovic
 * @author Stephan Saalfeld
 */
public abstract class Faces implements IntervalSamplingMethod
{
	protected abstract double[] spacing( final RealInterval interval );

	/**
	 * Estimate the {@link RealInterval} that bounds the given RealInterval
	 * after being transformed by a {@link RealTransform}.
	 * <p>
	 * This implementation estimates the bounding interval by transforming points
	 * on the faces of the given real interval.
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
		assert interval.numDimensions() >= transform.numSourceDimensions() : "Interval dimensions too small for transformation.";

		final int nSource = transform.numSourceDimensions();
		final int nTarget = transform.numTargetDimensions();

		final double[] itSpacing = spacing( interval );

		final double[] min = new double[ nTarget ];
		final double[] max = new double[ nTarget ];

		Arrays.fill( min, Double.MAX_VALUE );
		Arrays.fill( max, Double.MIN_VALUE );

		final double[] itMin = new double[ nTarget ];
		final double[] itMax = new double[ nTarget ];
		for( int i = 0; i < nSource; i++ )
		{
			interval.realMin( itMin );
			interval.realMax( itMax );
			itMin[ i ]  = interval.realMin( i );
			itMax[ i ]  = interval.realMin( i );
			IntervalSamplingMethod.transformedCoordinateBounds(
					transform,
					new LocalizingRealIntervalIterator( itMin, itMax, itSpacing ),
					min,
					max );

			itMin[ i ]  = interval.realMax( i );
			itMax[ i ]  = interval.realMax( i );
			IntervalSamplingMethod.transformedCoordinateBounds(
					transform,
					new LocalizingRealIntervalIterator( itMin, itMax, itSpacing ),
					min,
					max );
		}

		return new FinalRealInterval( min, max, false );
	}
}