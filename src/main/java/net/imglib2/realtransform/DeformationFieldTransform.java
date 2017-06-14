/*
 * #%L
 * ImgLib2: a general-purpose, multidimensional image processing library.
 * %%
 * Copyright (C) 2009 - 2016 Tobias Pietzsch, Stephan Preibisch, Stephan Saalfeld,
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
import net.imglib2.interpolation.randomaccess.NLinearInterpolatorFactory;
import net.imglib2.realtransform.inverse.DifferentiableRealTransform;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.Views;

/**
 * An <em>n</em>-dimensional deformation field.
 * <p>
 * Wraps a {@link RandomAccessibleInterval} of dimensionality D+1 ("def") and
 * interprets it as a D-dimensional {@link RealTransform}. The last dimension of
 * of the {@link RandomAccessibleInterval} must have at least D components.
 * <p>
 * The deformation field should be interpreted as a d-dimensional vector field.
 * A source point is displaced by adding the vector at that point the the source
 * point's position.
 *
 * @author John Bogovic &lt;bogovicj@janelia.hhmi.org&gt;
 *
 */
public class DeformationFieldTransform< T extends RealType< T > > implements RealTransform
//implements DifferentiableRealTransform
{

	private final RealRandomAccessible< T > defFieldReal;

	private final RealRandomAccess< T > defFieldAccess;

	private final int numDim;

	public DeformationFieldTransform( final RandomAccessibleInterval< T > def )
	{
		this( Views.interpolate( def, new NLinearInterpolatorFactory< T >() ) );
	}

	public DeformationFieldTransform( final RealRandomAccessible< T > defFieldReal )
	{
		this.defFieldReal = defFieldReal;
		this.numDim = defFieldReal.numDimensions() - 1;
		defFieldAccess = defFieldReal.realRandomAccess();
	}

	@Override
	public int numSourceDimensions()
	{
		return numDim;
	}

	@Override
	public int numTargetDimensions()
	{
		return numDim;
	}

	public RealRandomAccess< T > getDefFieldAcess()
	{
		return defFieldAccess;
	}

	@Override
	public void apply( final double[] source, final double[] target )
	{
		for ( int d = 0; d < numDim; d++ )
			defFieldAccess.setPosition( source[ d ], d );

		defFieldAccess.setPosition( 0.0, numDim );

		System.arraycopy( source, 0, target, 0, numDim );
		for ( int d = 0; d < numDim; d++ )
		{
			target[ d ] += defFieldAccess.get().getRealDouble();
			defFieldAccess.fwd( numDim );
		}
	}

	@Override
	public void apply( final RealLocalizable source, final RealPositionable target )
	{
		for ( int d = 0; d < numDim; d++ )
			defFieldAccess.setPosition( source.getDoublePosition( d ), d );

		defFieldAccess.setPosition( 0.0, numDim );

		double newpos = 0;
		for ( int d = 0; d < numDim; d++ )
		{
			newpos = source.getDoublePosition( d ) + defFieldAccess.get().getRealDouble();
			target.setPosition( newpos, d );

			defFieldAccess.fwd( numDim );
		}
	}

	@Override
	public RealTransform copy()
	{
		return new DeformationFieldTransform<>( this.defFieldReal );
	}
}
