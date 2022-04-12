/*-
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
package net.imglib2.util;

import java.util.Arrays;

import net.imglib2.FinalRealInterval;
import net.imglib2.RealInterval;
import net.imglib2.RealPoint;
import net.imglib2.iterator.IntervalIterator;
import net.imglib2.iterator.LocalizingRealIntervalIterator;
import net.imglib2.realtransform.RealTransform;

/**
 * Convenience methods for manipulating {@link RealInterval RealIntervals}.
 *
 * @author John Bogovic
 *
 */
public class RealIntervals
{
	/**
	 * Estimate the {@link RealInterval} that bounds the given RealInterval
	 * after being transformed by a {@link RealTransform}.
	 * <p>
	 * This implementation estimates the bounding interval by transforming points 
	 * on the corners of the given real interval.
	 *
	 * @param interval
	 *            the real interval
	 * @param transform
	 *            the transformation
	 * @return the bounding interval
	 */
	public static RealInterval boundingIntervalCorners( final RealInterval interval, final RealTransform transform )
	{
		final int nd = interval.numDimensions();
		final double[] pt = new double[ nd ];

		final double[] min = new double[ nd ];
		final double[] max = new double[ nd ];
		Arrays.fill( min, Double.MAX_VALUE );
		Arrays.fill( max, Double.MIN_VALUE );

		// iterate over the corners of an nd-hypercube
		final long[] unitInterval = new long[ nd ];
		Arrays.fill( unitInterval, 2 );
		IntervalIterator it = new IntervalIterator( unitInterval );
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

		return new FinalRealInterval( min, max );
	}


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
	 * @param spacing
	 *            spacing between samples along each dimension
	 * @return the bounding interval
	 */
	public static RealInterval boundingIntervalFaces( final RealInterval interval, final RealTransform transform, double... spacing )
	{
		final int nd = interval.numDimensions();
		final double[] itSpacing = fillSpacing( nd, spacing ) ;

		final double[] min = new double[ nd ];
		final double[] max = new double[ nd ];
		Arrays.fill( min, Double.MAX_VALUE );
		Arrays.fill( max, Double.MIN_VALUE );

		final double[] itvlMin = new double[ nd ];
		final double[] itvlMax = new double[ nd ];
		for( int i = 0; i < nd; i++ )
		{
			interval.realMin( itvlMin );
			interval.realMax( itvlMax );
			itvlMin[ i ]  = interval.realMin( i );
			itvlMax[ i ]  = interval.realMin( i );
			transformedCoordinateBounds( transform, new LocalizingRealIntervalIterator( itvlMin, itvlMax, itSpacing ), min, max );

			itvlMin[ i ]  = interval.realMax( i );
			itvlMax[ i ]  = interval.realMax( i );
			transformedCoordinateBounds( transform, new LocalizingRealIntervalIterator( itvlMin, itvlMax, itSpacing ), min, max );
		}

		return new FinalRealInterval( min, max );
	}

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
	 * @param numSamples
	 *            number of samples per dimension
	 * @return the bounding interval
	 */
	public static RealInterval boundingIntervalFacesSamples( final RealInterval interval, final RealTransform transform, int... numSamples )
	{
		return boundingIntervalFaces( interval, transform, stepsFromSamples( interval, numSamples ));
	}

	/**
	 * Estimate the {@link RealInterval} that bounds the given RealInterval
	 * after being transformed by a {@link RealTransform}.
	 * <p>
	 * This implementation estimates the bounding interval by transforming
	 * points in the volume of the given real interval.
	 *
	 * @param interval
	 *            the real interval
	 * @param transform
	 *            the transformation
	 * @param spacing
	 *            spacing between samples along each dimension
	 * @return the bounding interval
	 */
	public static RealInterval boundingIntervalVolume(
			final RealInterval interval, final RealTransform transform, double... spacing )
	{
		int nd = interval.numDimensions();
		final double[] itSpacing = fillSpacing( nd, spacing ) ;

		double[] min = new double[ nd ];
		double[] max = new double[ nd ];
		Arrays.fill( min, Long.MAX_VALUE );
		Arrays.fill( max, Long.MIN_VALUE );
		final LocalizingRealIntervalIterator it = new LocalizingRealIntervalIterator( interval, itSpacing );
		transformedCoordinateBounds( transform, it, min, max );
		return new FinalRealInterval( min, max );
	}

	/**
	 * Estimate the {@link RealInterval} that bounds the given RealInterval
	 * after being transformed by a {@link RealTransform}.
	 * <p>
	 * This implementation estimates the bounding interval by transforming
	 * points in the volume of the given real interval.
	 *
	 * @param interval
	 *            the real interval
	 * @param transform
	 *            the transformation
	 * @param numSamples
	 *            the number of samples per dimension
	 * @return the bounding interval
	 */
	public static RealInterval boundingIntervalVolumeSamples(
			final RealInterval interval, final RealTransform transform, int... numSamples )
	{
		return boundingIntervalVolume( interval, transform, stepsFromSamples( interval, numSamples ));

	}

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
	protected static void transformedCoordinateBounds( 
			final RealTransform transform, final LocalizingRealIntervalIterator it,
			final double[] min, final double[] max )
	{
		final int nd = it.numDimensions();
		final RealPoint ptxfm = new RealPoint( nd );
		while( it.hasNext() )
		{
			it.fwd();
			transform.apply( it, ptxfm );
			for( int d = 0; d < nd; d++ )
			{
				final double p = ptxfm.getDoublePosition(d);
				if( p < min[ d ])
					min[ d ] = p;

				if( p > max[ d ])
					max[ d ] = p;
			}
		}
	}

	private static double[] fillSpacing( int nd, double... spacing )
	{
		final double[] out;
		if( spacing.length >= nd )
			out = spacing;
		else
		{
			out = new double[ nd ];
			for( int i = 0; i < nd; i++ )
				if( i < spacing.length )
					out[i] = spacing[ i ];
				else
					out[i] = spacing[ spacing.length - 1 ];
		}
		return out;
	}

	private static double[] stepsFromSamples( final RealInterval interval , int... numSamples )
	{
		final double[] out = new double[ interval.numDimensions() ];
		for( int i = 0; i < out.length; i++ )
		{
			final double w = interval.realMax( i ) - interval.realMin( i );
			if( i < numSamples.length )
				out[i] = w / numSamples[ i ];
			else
				out[i] = w / numSamples[ numSamples.length - 1 ];
		}
		return out;
	}

}
