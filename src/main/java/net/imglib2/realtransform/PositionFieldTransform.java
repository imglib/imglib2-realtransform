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

import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.RealLocalizable;
import net.imglib2.RealPositionable;
import net.imglib2.RealRandomAccess;
import net.imglib2.RealRandomAccessible;
import net.imglib2.interpolation.InterpolatorFactory;
import net.imglib2.interpolation.randomaccess.NLinearInterpolatorFactory;
import net.imglib2.outofbounds.OutOfBoundsBorderFactory;
import net.imglib2.outofbounds.OutOfBoundsFactory;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.Views;

/**
 * A {@link RealTransform} by continuous coordinate lookup.
 *
 * @author Stephan Saalfeld &lt;saalfelds@janelia.hhmi.org&gt;
 */
public class PositionFieldTransform< T extends RealType< T > > implements RealTransform
{
	/* one for each dimension */
	protected final RealRandomAccess< T >[] positionAccesses;

	@SuppressWarnings("unchecked")
	protected static < T extends RealType< T > > RealRandomAccessible< T >[] extendAndInterpolate(
			final RandomAccessibleInterval< T >[] positions,
			final OutOfBoundsFactory< T, RandomAccessibleInterval< T > > outOfBoundsFactory,
			final InterpolatorFactory< T, RandomAccessible< T > > interpolatorFactory)
	{
		final RealRandomAccessible<T>[] realPositions = new RealRandomAccessible[positions.length];
		Arrays.setAll( realPositions, i -> Views.interpolate(
				Views.extend(positions[i], outOfBoundsFactory),
				interpolatorFactory));
		return realPositions;
	}

	protected RealRandomAccess< T >[] copyAccesses()
	{
		@SuppressWarnings("unchecked")
		final RealRandomAccess< T >[] accessCopies = new RealRandomAccess[ positionAccesses.length ];
		Arrays.setAll( accessCopies, i -> positionAccesses[ i ].copyRealRandomAccess() );
		return accessCopies;
	}

	@SuppressWarnings( "unchecked" )
	public PositionFieldTransform( final RealRandomAccess< T >... positionAccesses )
	{
		this.positionAccesses = positionAccesses;
	}

	@SuppressWarnings( "unchecked" )
	@SafeVarargs
	public PositionFieldTransform( final RealRandomAccessible< T >... positions )
	{
		assert( Arrays.stream( positions ).allMatch( p -> p.numDimensions() == positions.length ) ) : "Dimensions do not match.";

		positionAccesses = new RealRandomAccess[ positions.length ];
		Arrays.setAll( positionAccesses, i -> positions[ i ].realRandomAccess() );
	}

	@SafeVarargs
	public PositionFieldTransform( final RandomAccessibleInterval< T >... positions )
	{
		this(extendAndInterpolate(positions, new OutOfBoundsBorderFactory<>(), new NLinearInterpolatorFactory<>()));
	}

	@SafeVarargs
	public PositionFieldTransform(
			final OutOfBoundsFactory< T, RandomAccessibleInterval< T > > outOfBoundsFactory,
			final InterpolatorFactory< T, RandomAccessible< T > > interpolatorFactory,
			final RandomAccessibleInterval< T >... positions )
	{
		this(extendAndInterpolate( positions, outOfBoundsFactory, interpolatorFactory ) );
	}

	@Override
	public int numSourceDimensions()
	{
		return positionAccesses.length;
	}

	@Override
	public int numTargetDimensions()
	{
		return positionAccesses.length;
	}

	@Override
	public void apply( final double[] source, final double[] target )
	{
		for ( int d = 0; d < positionAccesses.length; d++ )
			positionAccesses[ d ].setPosition( source );

		for ( int d = 0; d < positionAccesses.length; d++ )
			target[ d ] = positionAccesses[ d ].get().getRealDouble();
	}

	@Override
	public void apply( final float[] source, final float[] target )
	{
		for ( int d = 0; d < positionAccesses.length; d++ )
			positionAccesses[ d ].setPosition( source );

		for ( int d = 0; d < positionAccesses.length; d++ )
			target[ d ] = ( float )positionAccesses[ d ].get().getRealDouble();
	}

	@Override
	public void apply( final RealLocalizable source, final RealPositionable target )
	{
		for ( int d = 0; d < positionAccesses.length; d++ )
			positionAccesses[ d ].setPosition( source );

		for ( int d = 0; d < positionAccesses.length; d++ )
			target.setPosition( positionAccesses[ d ].get().getRealDouble(), d );
	}

	@Override
	public RealTransform copy()
	{
		return new PositionFieldTransform<>( copyAccesses() );
	}
}
