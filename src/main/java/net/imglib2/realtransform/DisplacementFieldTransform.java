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
import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.RealLocalizable;
import net.imglib2.RealPositionable;
import net.imglib2.RealRandomAccess;
import net.imglib2.RealRandomAccessible;
import net.imglib2.converter.Converters;
import net.imglib2.position.FunctionRandomAccessible;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.view.Views;
import net.imglib2.view.composite.RealComposite;

/**
 * A {@link RealTransform} by continuous offset lookup.
 *
 * @author Stephan Saalfeld &lt;saalfelds@janelia.hhmi.org&gt;
 * @author Caleb Hulbert &lt;hulbertc@janelia.hhmi.org&gt;
 * @author John Bogovic &lt;bogovicj@janelia.hhmi.org&gt;
 */
public class DisplacementFieldTransform extends PositionFieldTransform
{
	protected final int numTargetDimensions;

	public DisplacementFieldTransform( final RealRandomAccess< ? extends RealLocalizable > displacementsAccess )
	{
		super( displacementsAccess );
		this.numTargetDimensions = access.get().numDimensions() < access.numDimensions() ? access.get().numDimensions() : access.numDimensions();
	}

	public DisplacementFieldTransform( final RealRandomAccessible< ? extends RealLocalizable > displacements )
	{
		super( displacements );
		this.numTargetDimensions = access.get().numDimensions() < access.numDimensions() ? access.get().numDimensions() : access.numDimensions();
	}

	/**
	 *
	 * @param <T>
	 *            type of the displacements
	 * @param displacements
	 *            interleaved displacement vectors, this means that the
	 *            components of the displacement vectors are in the 0th
	 *            dimension
	 */
	public < T extends RealType< T > > DisplacementFieldTransform( final RandomAccessibleInterval< T > displacements )
	{
		super( displacements );
		this.numTargetDimensions = access.get().numDimensions() < access.numDimensions() ? access.get().numDimensions() : access.numDimensions();
	}

	/**
	 *
	 * @param <T>
	 *            type of the displacements
	 * @param displacements
	 *            interleaved displacement vectors, this means that the
	 *            components of the displacement vectors are in the 0th
	 *            dimension
	 * @param pixelToPhysical
	 *            a transformation from pixel coordinates to physical
	 *            coordinates
	 */
	public < T extends RealType< T > > DisplacementFieldTransform( final RandomAccessibleInterval< T > displacements, final AffineGet pixelToPhysical )
	{
		super( displacements, pixelToPhysical );
		this.numTargetDimensions = access.get().numDimensions() < access.numDimensions() ? access.get().numDimensions() : access.numDimensions();
	}

	/**
	 *
	 * @param <T>
	 *            type of the displacements
	 * @param displacements
	 *            interleaved displacement vectors, this means that the
	 *            components of the displacement vectors are in the 0th
	 *            dimension
	 * @param spacing
	 *            the pixel spacing
	 */
	public < T extends RealType< T > > DisplacementFieldTransform( final RandomAccessibleInterval< T > displacements, final double... spacing )
	{
		super( displacements, spacing );
		this.numTargetDimensions = access.get().numDimensions() < access.numDimensions() ? access.get().numDimensions() : access.numDimensions();
	}

	/**
	 *
	 * @param <T>
	 *            type of the displacements
	 * @param displacements
	 *            interleaved displacement vectors, this means that the
	 *            components of the displacement vectors are in the 0th
	 *            dimension
	 * @param spacing
	 *            the pixel spacing
	 * @param offset
	 *            the pixel offset
	 */
	public < T extends RealType< T > > DisplacementFieldTransform( final RandomAccessibleInterval< T > displacements, final double[] spacing, final double[] offset )
	{
		super( displacements, spacing, offset );
		this.numTargetDimensions = access.get().numDimensions() < access.numDimensions() ? access.get().numDimensions() : access.numDimensions();
	}

	@Override
	public int numTargetDimensions()
	{
		return numTargetDimensions;
	}

	@Override
	public void apply( final double[] source, final double[] target )
	{
		// TODO replace with setPositionAndGet after new imglib2 release
		// TODO N = min (target, numTargetDimsensions) ?
		access.setPosition( source );
		final RealLocalizable comp = access.get();
		for ( int d = 0; d < numTargetDimensions(); d++ )
			target[ d ] = comp.getDoublePosition( d ) + source[ d ];
	}

	@Override
	public void apply( final float[] source, final float[] target )
	{
		// TODO replace with setPositionAndGet after new imglib2 release
		access.setPosition( source );
		final RealLocalizable comp = access.get();
		for ( int d = 0; d < numTargetDimensions(); d++ )
			target[ d ] = comp.getFloatPosition( d ) + source[ d ];
	}

	@Override
	public void apply( final RealLocalizable source, final RealPositionable target )
	{
		// TODO replace with setPositionAndGet after new imglib2 release
		access.setPosition( source );
		final RealLocalizable comp = access.get();
		for ( int d = 0; d < numTargetDimensions(); d++ )
			target.setPosition( comp.getDoublePosition( d ) + source.getDoublePosition( d ), d );
	}

	@Override
	public RealTransform copy()
	{
		return new DisplacementFieldTransform( access.copy() );
	}

	/**
	 * Creates a {@link RandomAccessibleInterval} of {@link DoubleType}
	 * containing the displacements of a {@link DisplacementFieldTransform} for
	 * a given {@link RealTransform}. This can be useful for saving a
	 * transformation as a displacement field, but generally should be not used
	 * to create a {@link DisplacementFieldTransform}.
	 * <p>
	 * Components of the displacements are in the 0th dimension, the extents of
	 * the field are given by the given {@link Interval}. The output interval
	 * will therefore be of size: <br>
	 * [ transform.numTargetDimensions(), interval.dimension(0), ...,
	 * interval.dimension( N-1 )]
	 * <p>
	 * The spacing parameter specifies how the discrete coordinates of the
	 * output displacement field map to the input source coordinates of the
	 * transform.
	 *
	 * @param transform
	 *            the transform to be converted
	 * @param interval
	 *            interval
	 * @param spacing
	 *            the spacing of the grid
	 * @return the displacement field
	 */
	public static RandomAccessibleInterval< DoubleType > createDisplacementField(
			final RealTransform transform,
			final Interval interval,
			final double[] spacing )
	{
		return createDisplacementField( transform, interval, new Scale( spacing ), () -> DoubleType.createVector( transform.numTargetDimensions() ) );
	}

	/**
	 * Creates a {@link RandomAccessibleInterval} of {@link DoubleType}
	 * containing the displacements of a {@link DisplacementFieldTransform} for
	 * a given {@link RealTransform}. This can be useful for saving a
	 * transformation as a displacement field, but generally should be not used
	 * to create a {@link DisplacementFieldTransform}.
	 * <p>
	 * Components of the displacements are in the 0th dimension, the extents of
	 * the field are given by the given {@link Interval}. The output interval
	 * will therefore be of size: <br>
	 * [ transform.numTargetDimensions(), interval.dimension(0), ...,
	 * interval.dimension( N-1 )]
	 * <p>
	 * The spacing and offset parameters specify how the discrete coordinates of
	 * the output displacement field map to the input source coordinates of the
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
	 * @return the displacement field
	 */
	public static RandomAccessibleInterval< DoubleType > createDisplacementField(
			final RealTransform transform,
			final Interval interval,
			final double[] spacing,
			final double[] offset )
	{
		return createDisplacementField( transform, interval, new ScaleAndTranslation( spacing, offset ), () -> DoubleType.createVector( transform.numTargetDimensions() ) );
	}

	/**
	 * Creates a {@link RandomAccessibleInterval} containing the displacements
	 * of a {@link DisplacementFieldTransform} for a given
	 * {@link RealTransform}. This can be useful for saving a transformation as
	 * a displacement field, but generally should be not used to create a
	 * {@link DisplacementFieldTransform}.
	 * <p>
	 * Components of the displacements are in the 0th dimension, the extents of
	 * the field are given by the given {@link Interval}. The output interval
	 * will therefore be of size: <br>
	 * [ transform.numTargetDimensions(), interval.dimension(0), ...,
	 * interval.dimension( N-1 )]
	 * <p>
	 * The spacing and offset parameters specify how the discrete coordinates of
	 * the output displacement field map to the input source coordinates of the
	 * transform.
	 * <p>
	 * The given supplier determines the output type and must provide
	 * {@link RealComposite}s of size greater than or equal to the transforms
	 * target dimension. For example,
	 *
	 * <pre>
	 * {@code () -> DoubleType.createVector(transform.numTargetDimensions())}
	 * </pre>
	 *
	 * @param <T>
	 *            the type of the output
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
	 * @return the displacement field
	 */
	public static < T extends RealType< T > > RandomAccessibleInterval< T > createDisplacementField(
			final RealTransform transform,
			final Interval interval,
			final double[] spacing,
			final double[] offset,
			final Supplier< RealComposite< T > > supplier )
	{
		return createDisplacementField( transform, interval, new ScaleAndTranslation( spacing, offset ), supplier );
	}

	/**
	 * Creates a {@link RandomAccessibleInterval} containing the displacements
	 * of a {@link DisplacementFieldTransform} for a given
	 * {@link RealTransform}. This can be useful for saving a transformation as
	 * a displacement field, but generally should be not used to create a
	 * {@link DisplacementFieldTransform}.
	 * <p>
	 * Components of the displacements are in the 0th dimension, the extents of
	 * the field are given by the given {@link Interval}. The output interval
	 * will therefore be of size: <br>
	 * [ transform.numTargetDimensions(), interval.dimension(0), ...,
	 * interval.dimension( N-1 )]
	 * <p>
	 * The {@link RealTransform} specifies how the discrete coordinates of the
	 * output displacement field map to the input source coordinates of the
	 * transform, i.e. it enables setting the spacing and offset of the
	 * displacement field grid.
	 * <p>
	 * The given supplier determines the output type and must provide
	 * {@link RealComposite}s of size greater than or equal to the transforms
	 * target dimension. For example,
	 *
	 * <pre>
	 * {@code () -> DoubleType.createVector(transform.numTargetDimensions())}
	 * </pre>
	 *
	 * @param <T>
	 *            the type of the output
	 * @param transform
	 *            the transform to be converted
	 * @param interval
	 *            interval
	 * @param gridTransform
	 *            transformation from the discrete grid to the transform's
	 *            source coordinates
	 * @param supplier
	 *            supplier for intermediate {@link RealComposite} type
	 * @return the displacement field
	 */
	public static < T extends RealType< T > > RandomAccessibleInterval< T > createDisplacementField(
			final RealTransform transform,
			final Interval interval,
			final RealTransform gridTransform,
			final Supplier< RealComposite< T > > supplier )
	{
		final int nd = transform.numTargetDimensions();
		final RandomAccessible< RealComposite< T > > transformedGrid = new FunctionRandomAccessible<>(
				nd,
				() -> {
					final RealTransform copy = gridTransform.copy();
					return ( x, y ) -> {
						copy.apply( x, y );
					};
				},
				supplier );

		final RandomAccessible< RealComposite< T > > displacements = Converters.convert2(
				transformedGrid,
				() -> {
					final RealTransform copy = transform.copy();
					return ( x, y ) -> {
						copy.apply( x, y );
						for ( int d = 0; d < nd; d++ )
							y.move( -x.getDoublePosition( d ), d );
					};
				},
				supplier );

		final long[] dfieldDims = new long[ interval.numDimensions() + 1 ];
		dfieldDims[ 0 ] = interval.numDimensions();
		for ( int i = 0; i < interval.numDimensions(); ++i )
			dfieldDims[ i + 1 ] = interval.dimension( i );

		return Views.interval( Views.interleave( displacements ), new FinalInterval( dfieldDims ) );
	}

}
