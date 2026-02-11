/*-
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
package net.imglib2.realtransform;

import net.imglib2.RealLocalizable;
import net.imglib2.RealPositionable;

/**
 * An {@link InvertibleRealTransform} for which the forward and inverse
 * transforms are explicitly given as {@link RealTransform}s. Consistency is not
 * internally enforced.
 *
 * @author John Bogovic
 * @author Stephan Saalfeld
 *
 */
public class ExplicitInvertibleRealTransform implements InvertibleRealTransform
{
	private final RealTransform forwardTransform;

	private final RealTransform inverseTransform;

	private final ExplicitInvertibleRealTransform inverse;

	/**
	 * Creates a new ExplicitInvertibleRealTransform.
	 *
	 * @param forwardTransform the transform defining the forward direction
	 * @param inverseTransform the transform defining the inverse direction
	 */
	public ExplicitInvertibleRealTransform( final RealTransform forwardTransform, final RealTransform inverseTransform )
	{
		assert
			forwardTransform.numTargetDimensions() == inverseTransform.numSourceDimensions() &&
			forwardTransform.numSourceDimensions() == inverseTransform.numTargetDimensions() : "number of target and source dimensions not compatible";

		this.forwardTransform = forwardTransform;
		this.inverseTransform = inverseTransform;
		this.inverse = new ExplicitInvertibleRealTransform( this );
	}

	private ExplicitInvertibleRealTransform( final ExplicitInvertibleRealTransform inverse )
	{
		this.forwardTransform = inverse.inverseTransform;
		this.inverseTransform = inverse.forwardTransform;
		this.inverse = inverse;
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
	public void apply( final double[] source, final double[] target )
	{
		forwardTransform.apply( source, target );
	}

	@Override
	public void apply( final RealLocalizable source, final RealPositionable target )
	{
		forwardTransform.apply( source, target );
	}

	@Override
	public void applyInverse( final double[] source, final double[] target )
	{
		inverseTransform.apply( target, source );
	}

	@Override
	public void applyInverse( final RealPositionable source, final RealLocalizable target )
	{
		inverseTransform.apply( target, source );
	}

	@Override
	public ExplicitInvertibleRealTransform inverse()
	{
		return inverse;
	}

	@Override
	public InvertibleRealTransform copy()
	{
		return new ExplicitInvertibleRealTransform( forwardTransform.copy(), inverseTransform.copy() );
	}
}
