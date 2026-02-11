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

import net.imglib2.RealInterval;
import net.imglib2.RealPoint;
import net.imglib2.iterator.LocalizingRealIntervalIterator;
import net.imglib2.realtransform.RealTransform;

/**
 *
 * @author John Bogovic
 * @author Stephan Saalfeld
 *
 */
public interface IntervalSamplingMethod
{
	/**
	 * Singleton instance of stateless {@link Corners} method.  Use instead of
	 * creating new instances with {@code new Corners()}.
	 */
	static Corners CORNERS = new Corners();

	/**
	 * Transforms all points produced by the {@link LocalizingRealIntervalIterator}
	 * with the provided {@link RealTransform} and stores the min and max coordinates
	 * for each dimension in the provided arrays.
	 * <p>
	 * Min and max arrays are not reset before iteration, meaning this method
	 * may be called repeatedly with different iterators to find bounds of
	 * the union of iterated points.
	 *
	 * @param transform a transformation
	 * @param it the real interval iterator
	 * @param min the min coordinate array to modify
	 * @param max the max coordinate array to modify
	 */
	static void transformedCoordinateBounds(
			final RealTransform transform,
			final LocalizingRealIntervalIterator it,
			final double[] min,
			final double[] max )
	{
		assert
				transform.numTargetDimensions() <= min.length &&
				transform.numTargetDimensions() <= max.length :
				"Transformation target dimensionality too large for min and max vectors.";

		assert
				transform.numSourceDimensions() <= it.numDimensions() :
				"Transformation target dimensionality too large for min and max vectors.";

		final int nTarget = transform.numTargetDimensions();
		final RealPoint targetPoint = new RealPoint( nTarget );
		while( it.hasNext() )
		{
			it.fwd();
			transform.apply( it, targetPoint );
			for( int d = 0; d < nTarget; d++ )
			{
				final double p = targetPoint.getDoublePosition( d );
				if( p < min[ d ] )
					min[ d ] = p;

				if( p > max[ d ] )
					max[ d ] = p;
			}
		}
	}

	/**
	 *
	 * @param interval
	 *            the real interval
	 * @param transform
	 *            the transformation
	 * @return the bounding interval
	 */
	RealInterval bounds( final RealInterval interval, final RealTransform transform );
}
