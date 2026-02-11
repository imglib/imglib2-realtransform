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
package net.imglib2.realtransform.inverse;

import net.imglib2.RealLocalizable;
import net.imglib2.RealPositionable;
import net.imglib2.realtransform.InvertibleRealTransform;

/**
 * Use gradient descent to iteratively estimate the inverse of a differentiable
 * forward transformation.
 * 
 * @author John Bogovic
 *
 */
public class InvertibleTransformByGradientDescent implements InvertibleRealTransform
{
	final boolean isInverse;

	final DifferentiableRealTransform forwardTransform;

	final BacktrackingLineSearch inverseTransform;

	public InvertibleTransformByGradientDescent( final DifferentiableRealTransform forwardTransform )
	{
		this( forwardTransform, false );
	}

	public InvertibleTransformByGradientDescent( final DifferentiableRealTransform forwardTransform, final boolean isInverse )
	{
		this( forwardTransform, new BacktrackingLineSearch( forwardTransform ), isInverse );
	}

	public InvertibleTransformByGradientDescent( final DifferentiableRealTransform forwardTransform, final BacktrackingLineSearch inverseTransform, final boolean isInverse )
	{
		this.forwardTransform = forwardTransform;
		this.inverseTransform = inverseTransform;

		this.isInverse = isInverse;
	}

	@Override
	public void apply( double[] p, double[] q )
	{
		if ( isInverse )
			inverseTransform.iterativeInverse( p, q );
		else
			forwardTransform.apply( p, q );
	}

	@Override
	public void apply( RealLocalizable p, RealPositionable q )
	{
		if ( isInverse )
		{
			double[] pd = new double[ p.numDimensions() ];
			double[] qd = new double[ p.numDimensions() ];

			p.localize( pd );
			inverseTransform.iterativeInverse( pd, qd );
			q.setPosition( qd );
		}
		else
			forwardTransform.apply( p, q );
	}

	@Override
	public void applyInverse( double[] p, double[] q )
	{
		if ( isInverse )
			forwardTransform.apply( p, q );
		else
			inverseTransform.iterativeInverse( p, q );
	}

	@Override
	public void applyInverse( RealPositionable p, RealLocalizable q )
	{
		if ( isInverse )
			forwardTransform.apply( q, p );
		else
		{
			double[] pd = new double[ p.numDimensions() ];
			double[] qd = new double[ p.numDimensions() ];

			q.localize( qd );
			inverseTransform.iterativeInverse( qd, pd );
			p.setPosition( pd );
		}
	}

	@Override
	public InvertibleTransformByGradientDescent copy()
	{
		return new InvertibleTransformByGradientDescent( forwardTransform, isInverse );
	}

	@Override
	public InvertibleTransformByGradientDescent inverse()
	{
		return new InvertibleTransformByGradientDescent( forwardTransform, !isInverse );
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

}
