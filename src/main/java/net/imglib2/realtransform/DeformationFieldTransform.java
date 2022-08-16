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

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.RealLocalizable;
import net.imglib2.RealPositionable;
import net.imglib2.RealRandomAccess;
import net.imglib2.RealRandomAccessible;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.composite.Composite;
import net.imglib2.view.composite.RealComposite;

import java.util.Arrays;
import java.util.function.IntFunction;
import java.util.function.IntUnaryOperator;

/**
 * A {@link RealTransform} by continuous offset lookup.
 *
 * @author Stephan Saalfeld &lt;saalfelds@janelia.hhmi.org&gt;
 */
public class DeformationFieldTransform< T extends RealType< T > > extends PositionFieldTransform< T >
{

	public DeformationFieldTransform( RealRandomAccessible<RealComposite< T >> positions )
	{

		super( positions );
	}

	public DeformationFieldTransform( RealRandomAccess<RealComposite< T >> positionAccesses )
	{

		super( positionAccesses );
	}

	public DeformationFieldTransform( RandomAccessibleInterval< T > positions )
	{

		super( positions );
	}

	@Override
	public void apply( final double[] source, final double[] target )
	{

		Composite< T > comp = positionAccesses.setPositionAndGet(source);
		for ( int d = 0; d < target.length; d++ )
			target[ d ] = comp.get( d ).getRealDouble() + source[ d ];
	}

	@Override
	public void apply( final float[] source, final float[] target )
	{

		Composite< T > comp = positionAccesses.setPositionAndGet(source);
		for ( int d = 0; d < target.length; d++ )
			target[ d ] = ( float ) (comp.get( d ).getRealDouble() + source[ d ]);
	}

	@Override
	public void apply( final RealLocalizable source, final RealPositionable target )
	{

		Composite< T > comp = positionAccesses.setPositionAndGet(source);
		for ( int d = 0; d < target.numDimensions(); d++ )
			target.setPosition( comp.get( d ).getRealDouble() + source.getDoublePosition( d ), d );
	}

	@Override
	public RealTransform copy()
	{
		return new DeformationFieldTransform<>( positionAccesses.copy() );
	}
}
