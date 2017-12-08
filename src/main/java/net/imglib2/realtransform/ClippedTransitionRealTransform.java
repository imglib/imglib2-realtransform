/*
 * #%L
 * ImgLib2: a general-purpose, multidimensional image processing library.
 * %%
 * Copyright (C) 2009 - 2017 Tobias Pietzsch, Stephan Preibisch, Stephan Saalfeld,
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
import net.imglib2.realtransform.RealTransform;

/**
 * A {@link RealTransform} that transitions between two <em>n</em>-dimensional
 * {@link RealTransform RealTransforms}.  The resulting {@link RealTransform}
 * is (<em>n</em> + 1)-dimensional, the transition happens along an
 * interval over the <em>n</em>-th dimension that remains unchanged.
 *
 * The transition is clipped beyond the transition interval, i.e. is not
 * linearly extrapolated.
 *
 * @author Stephan Saalfeld
 */
public class ClippedTransitionRealTransform implements RealTransform
{
	private final InterpolatedRealTransform interpolant;
	private final double transitionOffset;
	private final double transitionScale;
	private final int maxN;

	private ClippedTransitionRealTransform(
			final InterpolatedRealTransform interpolant,
			final double transitionOffset,
			final double transitionScale )
	{
		this.interpolant = interpolant;
		this.transitionOffset = transitionOffset;
		this.transitionScale = transitionScale;
		maxN = interpolant.numSourceDimensions();
	}

	public ClippedTransitionRealTransform(
			final RealTransform a,
			final RealTransform b,
			final double min,
			final double max )
	{
		this( new InterpolatedRealTransform( a, b, 1.0 ), min, 1.0 / ( max - min ) );
	}

	@Override
	public int numSourceDimensions()
	{
		return interpolant.numSourceDimensions() + 1;
	}

	@Override
	public int numTargetDimensions()
	{
		return numSourceDimensions();
	}

	private final double lambda( final double position )
	{
		return Math.max( 0.0, Math.min( 1.0, 1.0 - ( position - transitionOffset ) * transitionScale ) );
	}

	@Override
	public void apply( final double[] source, final double[] target )
	{
		interpolant.setLambda( lambda( source[ maxN ] ) );
		interpolant.apply( source, target );
		target[ maxN ] = source[ maxN ];
	}

	@Override
	public void apply( final float[] source, final float[] target )
	{
		interpolant.setLambda( lambda( source[ maxN ] ) );
		interpolant.apply( source, target );
		target[ maxN ] = source[ maxN ];
	}

	@Override
	public void apply( final RealLocalizable source, final RealPositionable target )
	{
		final double z = source.getDoublePosition( maxN );
		interpolant.setLambda( lambda( z ) );
		interpolant.apply( source, target );
		target.setPosition( z, maxN );
	}

	@Override
	public ClippedTransitionRealTransform copy()
	{
		return new ClippedTransitionRealTransform( interpolant.copy(), transitionOffset, transitionScale );
	}
}
