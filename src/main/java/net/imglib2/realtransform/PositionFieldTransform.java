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

import java.util.function.Supplier;

import net.imglib2.FinalInterval;
import net.imglib2.Interval;
import net.imglib2.Localizable;
import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.RealLocalizable;
import net.imglib2.RealPositionable;
import net.imglib2.RealRandomAccess;
import net.imglib2.RealRandomAccessible;
import net.imglib2.converter.Converters;
import net.imglib2.interpolation.randomaccess.NLinearInterpolatorFactory;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.util.Localizables;
import net.imglib2.view.Views;
import net.imglib2.view.composite.CompositeIntervalView;
import net.imglib2.view.composite.RealComposite;

/**
 * A {@link RealTransform} by continuous coordinate lookup.
 *
 * @author Stephan Saalfeld &lt;saalfelds@janelia.hhmi.org&gt;
 * @author Caleb Hulbert &lt;hulbertc@janelia.hhmi.org&gt;
 * @author John Bogovic &lt;bogovicj@janelia.hhmi.org&gt;
 */
public class PositionFieldTransform implements RealTransform
{
	protected final RealRandomAccess< ? extends RealLocalizable > access;

	protected final int numTargetDimensions;

	public PositionFieldTransform( final RealRandomAccess< ? extends RealLocalizable > positionAccesses )
	{
		access = positionAccesses;
		numTargetDimensions = access.get().numDimensions();
	}

	public PositionFieldTransform( final RealRandomAccessible< ? extends RealLocalizable > positions )
	{
		access = positions.realRandomAccess();
		numTargetDimensions = access.get().numDimensions();
	}

	/**
	 *
	 * @param <T>
	 *            position type
	 * @param positions
	 *            interleaved target coordinate, this means that the components
	 *            of the target coordinates are in the 0th dimension
	 */
	public < T extends RealType< T > > PositionFieldTransform( final RandomAccessibleInterval< T > positions )
	{
		this( convertToComposite( positions ) );
	}

	/**
	 *
	 * @param <T>
	 *            position type
	 * @param positions
	 *            interleaved target coordinate, this means that the components
	 *            of the target coordinates are in the 0th dimension
	 * @param pixelToPhysical
	 *            a transformation from pixel coordinates to physical
	 *            coordinates
	 */
	public < T extends RealType< T > > PositionFieldTransform( final RandomAccessibleInterval< T > positions, final AffineGet pixelToPhysical )
	{
		this( RealViews.affine( convertToComposite( positions ), pixelToPhysical ) );
	}

	/**
	 *
	 * @param <T>
	 *            position type
	 * @param positions
	 *            interleaved target coordinate, this means that the components
	 *            of the target coordinates are in the 0th dimension
	 * @param spacing
	 *            the pixel spacing
	 */
	public < T extends RealType< T > > PositionFieldTransform( final RandomAccessibleInterval< T > positions, final double... spacing )
	{
		this( RealViews.affine(
				convertToComposite( positions ),
				spacing.length == 2 ? new Scale2D( spacing ) : spacing.length == 3 ? new Scale3D( spacing ) : new Scale( spacing ) ) );
	}

	/**
	 *
	 * @param <T>
	 *            position type
	 * @param positions
	 *            interleaved target coordinate, this means that the components
	 *            of the target coordinates are in the 0th dimension
	 * @param spacing
	 *            the pixel spacing
	 * @param offset
	 *            the pixel offset
	 */
	public < T extends RealType< T > > PositionFieldTransform( final RandomAccessibleInterval< T > positions, final double[] spacing, final double[] offset )
	{
		this( RealViews.affine( convertToComposite( positions ), new ScaleAndTranslation( spacing, offset ) ) );
	}

	@Override
	public int numSourceDimensions()
	{
		return access.numDimensions();
	}

	@Override
	public int numTargetDimensions()
	{
		return numTargetDimensions;
	}

	@Override
	public void apply( final double[] source, final double[] target )
	{
		access.setPosition( source );
		access.get().localize( target );
	}

	@Override
	public void apply( final float[] source, final float[] target )
	{
		access.setPosition( source );
		access.get().localize( target );
	}

	@Override
	public void apply( final RealLocalizable source, final RealPositionable target )
	{
		access.setPosition( source );
		access.get().localize( target );
	}

	@Override
	public RealTransform copy()
	{
		return new PositionFieldTransform( access.copy() );
	}

	private static < T extends RealType< T > > RealRandomAccessible< ? extends RealLocalizable > convertToComposite(
			final RandomAccessibleInterval< T > position )
	{
		final CompositeIntervalView< T, RealComposite< T > > collapsedFirst =
				Views.collapseReal(
						Views.moveAxis( position, 0, position.numDimensions() - 1 ) );

		return Views.interpolate(
				Views.extendBorder( collapsedFirst ),
				new NLinearInterpolatorFactory<>() );
	}

	/**
	 * Creates a {@link RandomAccessibleInterval} of {@link DoubleType}
	 * containing the positions of a {@link PositionFieldTransform} for a given
	 * {@link RealTransform}. This can be useful for saving a transformation as
	 * a displacement field, but generally should be not used to create a
	 * {@link PositionFieldTransform}.
	 * <p>
	 * Components of the positions are in the 0th dimension, the extents of the
	 * field are given by the given {@link Interval}. The output interval will
	 * therefore be of size: <br>
	 * [ transform.numTargetDimensions(), interval.dimension(0), ...,
	 * interval.dimension( N-1 )]
	 * <p>
	 * The spacing parameter specifies how the discrete coordinates of the
	 * output position field map to the input source coordinates of the
	 * transform.
	 *
	 * @param transform
	 *            the transform to be converted
	 * @param interval
	 *            interval
	 * @param spacing
	 *            the spacing of the grid
	 * @return the position field
	 */
	public static RandomAccessibleInterval< DoubleType > createPositionField(
			final RealTransform transform,
			final Interval interval,
			final double[] spacing )
	{
		return createPositionField( transform, interval, new Scale( spacing ), () -> DoubleType.createVector( transform.numTargetDimensions() ) );
	}

	/**
	 * Creates a {@link RandomAccessibleInterval} of {@link DoubleType}
	 * containing the positions of a {@link PositionFieldTransform} for a given
	 * {@link RealTransform}. This can be useful for saving a transformation as
	 * a displacement field, but generally should be not used to create a
	 * {@link PositionFieldTransform}.
	 * <p>
	 * Components of the positions are in the 0th dimension, the extents of the
	 * field are given by the given {@link Interval}. The output interval will
	 * therefore be of size: <br>
	 * [ transform.numTargetDimensions(), interval.dimension(0), ...,
	 * interval.dimension( N-1 )]
	 * <p>
	 * The spacing and offset parameters specify how the discrete coordinates of
	 * the output position field map to the input source coordinates of the
	 * transform.
	 *
	 * @param transform
	 *            the transform to be converted
	 * @param interval
	 *            interval
	 * @param spacing
	 *            the spacing of the grid
	 * @param offset
	 *            the offset of the output in physical units
	 * @return the position field
	 */
	public static RandomAccessibleInterval< DoubleType > createPositionField(
			final RealTransform transform,
			final Interval interval,
			final double[] spacing,
			final double[] offset )
	{
		return createPositionField( transform, interval, spacing, offset, () -> DoubleType.createVector( transform.numTargetDimensions() ) );
	}

	/**
	 * Creates a {@link RandomAccessibleInterval} containing the positions of a
	 * {@link PositionFieldTransform} for a given {@link RealTransform}. This
	 * can be useful for saving a transformation as a displacement field, but
	 * generally should be not used to create a {@link PositionFieldTransform}.
	 * <p>
	 * Components of the positions are in the 0th dimension, the extents of the
	 * field are given by the given {@link Interval}. The output interval will
	 * therefore be of size: <br>
	 * [ transform.numTargetDimensions(), interval.dimension(0), ...,
	 * interval.dimension( N-1 )]
	 * <p>
	 * The spacing and offset parameters specify how the discrete coordinates of
	 * the output position field map to the input source coordinates of the
	 * transform.
	 * <p>
	 * The given supplier determines the output type and must provide
	 * {@link RealComposite}s of size greater than or equal to the transforms
	 * target dimension. For example,
	 *
	 * <pre>
	 * {@code
	 * () -> DoubleType.createVector( transform.numTargetDimensions() )
	 * }
	 * </pre>
	 *
	 * @param <T>
	 *            output type
	 * @param transform
	 *            the transform to be converted
	 * @param interval
	 *            interval
	 * @param spacing
	 *            the spacing of the grid
	 * @param offset
	 *            the offset of the output in physical units
	 * @param supplier
	 *            supplier for intermediate {@link RealComposite} type
	 * @return the position field
	 */
	public static < T extends RealType< T > > RandomAccessibleInterval< T > createPositionField(
			final RealTransform transform,
			final Interval interval,
			final double[] spacing,
			final double[] offset,
			final Supplier< RealComposite< T > > supplier )
	{
		return createPositionField( transform, interval, new ScaleAndTranslation( spacing, offset ), supplier );
	}

	/**
	 * Creates a {@link RandomAccessibleInterval} containing the positions of a
	 * {@link PositionFieldTransform} for a given {@link RealTransform}. This
	 * can be useful for saving a transformation as a displacement field, but
	 * generally should be not used to create a {@link PositionFieldTransform}.
	 * <p>
	 * Components of the positions are in the 0th dimension, the extents of the
	 * field are given by the given {@link Interval}. The output interval will
	 * therefore be of size: <br>
	 * [ transform.numTargetDimensions(), interval.dimension(0), ...,
	 * interval.dimension( N-1 )]
	 * <p>
	 * The {@link RealTransform} specifies how the discrete coordinates of the
	 * output position field map to the input source coordinates of the
	 * transform, i.e. it enables setting the spacing and offset of the
	 * displacement field grid.
	 * <p>
	 * The given supplier determines the output type and must provide
	 * {@link RealComposite}s of size greater than or equal to the transforms
	 * target dimension. For example,
	 *
	 * <pre>
	 * {@code
	 * () -> DoubleType.createVector( transform.numTargetDimensions() )
	 * }
	 * </pre>
	 *
	 * @param <T>
	 *            output type
	 * @param transform
	 *            the transform to be converted
	 * @param interval
	 *            interval
	 * @param gridTransform
	 *            transformation from the discrete grid to the transform's
	 *            source coordinates
	 * @param supplier
	 *            supplier for intermediate {@link RealComposite} type
	 * @return the position field
	 */
	public static < T extends RealType< T > > RandomAccessibleInterval< T > createPositionField(
			final RealTransform transform,
			final Interval interval,
			final RealTransform gridTransform,
			final Supplier< RealComposite< T > > supplier )
	{
		final RandomAccessibleInterval< Localizable > pixelCoordinates = Localizables.randomAccessibleInterval( interval );
		final RandomAccessible< RealComposite< T > > positions = Converters.convert2(
				pixelCoordinates,
				() -> {
					final RealTransform gridCopy = gridTransform.copy();
					final RealTransform copy = transform.copy();
					return ( x, y ) -> {
						gridCopy.apply( x, y );
						copy.apply( y, y );
					};
				},
				supplier );

		final long[] pfieldDims = new long[ interval.numDimensions() + 1 ];
		pfieldDims[ 0 ] = interval.numDimensions();
		for ( int i = 0; i < interval.numDimensions(); ++i )
			pfieldDims[ i + 1 ] = interval.dimension( i );

		return Views.interval( Views.interleave( positions ), new FinalInterval( pfieldDims ) );
	}
}
