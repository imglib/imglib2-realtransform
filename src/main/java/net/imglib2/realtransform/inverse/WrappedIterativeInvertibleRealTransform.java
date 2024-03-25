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
package net.imglib2.realtransform.inverse;

import net.imglib2.RealLocalizable;
import net.imglib2.RealPositionable;
import net.imglib2.realtransform.InverseRealTransform;
import net.imglib2.realtransform.InvertibleRealTransform;
import net.imglib2.realtransform.RealTransform;

public class WrappedIterativeInvertibleRealTransform< T extends RealTransform > implements InvertibleRealTransform
{
	protected final T forwardTransform;

	protected final DifferentiableRealTransform differentiableTransform;

	protected final InverseRealTransformGradientDescent inverseTransform;

	public WrappedIterativeInvertibleRealTransform( final T xfm )
	{
		this.forwardTransform = xfm;

		if( xfm instanceof DifferentiableRealTransform )
		{
			differentiableTransform = (DifferentiableRealTransform) xfm;
		}
		else
		{
			differentiableTransform = new RealTransformFiniteDerivatives( xfm );
		}

		inverseTransform = new InverseRealTransformGradientDescent( xfm.numSourceDimensions(), differentiableTransform );
	}

	private WrappedIterativeInvertibleRealTransform( final T xfm, InverseRealTransformGradientDescent inverse )
	{
		this.forwardTransform = xfm;
		if( xfm instanceof DifferentiableRealTransform )
		{
			differentiableTransform = (DifferentiableRealTransform) xfm;
		}
		else
		{
			differentiableTransform = new RealTransformFiniteDerivatives( xfm );
		}
		this.inverseTransform = inverse;
	}

	public T getTransform()
	{
		return forwardTransform;
	}

	@Override
	public int numSourceDimensions()
	{
		return forwardTransform.numSourceDimensions();
	}

	@Override
	public int numTargetDimensions()
	{
		return forwardTransform.numTargetDimensions();
	}

	@Override
	public void apply( double[] source, double[] target )
	{
		forwardTransform.apply( source, target );
	}

	@Override
	public void apply( RealLocalizable source, RealPositionable target )
	{
		forwardTransform.apply( source, target );
	}

	@Override
	public void applyInverse( double[] source, double[] target )
	{
		inverseTransform.apply( target, source );
	}

	@Override
	public void applyInverse( RealPositionable source, RealLocalizable target )
	{
		inverseTransform.apply( target, source );
	}

	@Override
	public InvertibleRealTransform inverse()
	{
		return new InverseRealTransform( this );
	}

	public InverseRealTransformGradientDescent getOptimzer()
	{
		return inverseTransform;
	}

	@SuppressWarnings( "unchecked" )
	@Override
	public WrappedIterativeInvertibleRealTransform<T> copy()
	{
		return new WrappedIterativeInvertibleRealTransform< T >( (T)forwardTransform.copy(), (InverseRealTransformGradientDescent)inverseTransform.copy() );
	}
}
