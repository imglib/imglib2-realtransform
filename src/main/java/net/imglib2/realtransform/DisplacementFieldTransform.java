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

import java.util.Arrays;
import java.util.function.Supplier;
import java.util.stream.LongStream;

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
import net.imglib2.type.numeric.RealType;
import net.imglib2.util.Localizables;
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
	 * @param displacements
	 * 			interleaved displacement vectors, this means that the
	 * 			components of the displacement vectors are in the 0th dimension
	 */
	public < T extends RealType< T > > DisplacementFieldTransform( final RandomAccessibleInterval< T > displacements )
	{
		super( displacements );
		this.numTargetDimensions = access.get().numDimensions() < access.numDimensions() ? access.get().numDimensions() : access.numDimensions();
	}

	/**
	 *
	 * @param displacements
	 * 			interleaved displacement vectors, this means that the
	 * 			components of the displacement vectors are in the 0th dimension
	 * @param pixelToPhysical
	 * 			a transformation from pixel coordinates to physical coordinates
	 */
	public < T extends RealType< T > > DisplacementFieldTransform( final RandomAccessibleInterval< T > displacements, final AffineGet pixelToPhysical )
	{
		super( displacements, pixelToPhysical );
		this.numTargetDimensions = access.get().numDimensions() < access.numDimensions() ? access.get().numDimensions() : access.numDimensions();
	}

	/**
	 *
	 * @param displacements
	 * 			interleaved displacement vectors, this means that the
	 * 			components of the displacement vectors are in the 0th dimension
	 * @param spacing
	 * 			the pixel spacing
	 */
	public < T extends RealType< T > > DisplacementFieldTransform( final RandomAccessibleInterval< T > displacements, final double... spacing )
	{
		super( displacements, spacing );
		this.numTargetDimensions = access.get().numDimensions() < access.numDimensions() ? access.get().numDimensions() : access.numDimensions();
	}

	/**
	 *
	 * @param displacements
	 * 			interleaved displacement vectors, this means that the
	 * 			components of the displacement vectors are in the 0th dimension
	 * @param spacing
	 * 			the pixel spacing
	 * @param offset
	 * 			the pixel offse
	 */
	public < T extends RealType< T > > DisplacementFieldTransform( final RandomAccessibleInterval< T > displacements, final double[] spacing, final double[] offset )
	{
		super( displacements, spacing, offset);
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

	public static < T extends RealType< T > > RandomAccessibleInterval<T> createDisplacementField(
			final RealTransform transform,
			final Interval interval,
			final double[] spacing,
			final double[] offset,
			Supplier<RealComposite<T>> sup )
	{
		final int nd = transform.numTargetDimensions();	
		final ScaleAndTranslation scaleOffset = new ScaleAndTranslation(spacing, offset);
		final RandomAccessibleInterval<Localizable> pixelCoordinates = Localizables.randomAccessibleInterval(interval);

		final RandomAccessible< RealComposite < T > > physical = Converters.convert2(
				pixelCoordinates,
				(x, y) -> { scaleOffset.apply( x, y ); },
				sup );

		final RandomAccessible< RealComposite < T > > displacements = Converters.convert2(
				physical,
				(x, y) -> {
					transform.apply(x, y);
					for (int d = 0; d < nd; d++)
						y.move(-x.getDoublePosition(d), d);
				},
				sup );

		final long[] dfieldDims = LongStream.concat(
			LongStream.of( interval.numDimensions() ),
			Arrays.stream( interval.dimensionsAsLongArray() )).toArray();

		return Views.interval( Views.interleave( displacements ), new FinalInterval( dfieldDims ) );
	}
}
