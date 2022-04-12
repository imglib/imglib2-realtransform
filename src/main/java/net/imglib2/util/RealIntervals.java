package net.imglib2.util;

import java.util.Arrays;
import java.util.stream.IntStream;

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
	 * @param steps
	 *            step between samples
	 * @return the bounding interval
	 */
	public static RealInterval boundingIntervalFaces( final RealInterval interval, final RealTransform xfm, double... steps )
	{
		final int nd = interval.numDimensions();
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
			transformedCoordinateBounds( xfm, new LocalizingRealIntervalIterator( itvlMin, itvlMax, steps ), min, max );

			itvlMin[ i ]  = interval.realMax( i );
			itvlMax[ i ]  = interval.realMax( i );
			transformedCoordinateBounds( xfm, new LocalizingRealIntervalIterator( itvlMin, itvlMax, steps ), min, max );
		}

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
	 * @param steps
	 *            step between samples
	 * @return the bounding interval
	 */
	public static RealInterval boundingIntervalVolume(
			final RealInterval interval, final RealTransform transform, double... steps )
	{
		int nd = interval.numDimensions();
		double[] min = new double[ nd ];
		double[] max = new double[ nd ];
		Arrays.fill( min, Long.MAX_VALUE );
		Arrays.fill( max, Long.MIN_VALUE );
		final LocalizingRealIntervalIterator it = new LocalizingRealIntervalIterator( interval, steps );
		transformedCoordinateBounds( transform, it, min, max );
		return new FinalRealInterval( min, max );
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

}
