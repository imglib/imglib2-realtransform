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

import net.imglib2.RealLocalizable;
import net.imglib2.RealPositionable;

/**
 * An {@link InvertibleRealTransform} that is a sequence of
 * {@link InvertibleRealTransform InvertibleRealTransforms}.
 * If empty, will behave as the identity transformation.
 * 
 * {@link isIdentity} will return true if either the sequence is empty, or if
 * every transform in the sequence returns true for {@link isIdentity}.  This 
 * sequence could behave as the identity even if {@link isIdentity} returns false,
 * for example, if it contains only a transform and its inverse.
 * 
 * @author Stephan Saalfeld
 */
public class InvertibleRealTransformSequence extends AbstractRealTransformSequence< InvertibleRealTransform > implements InvertibleRealTransform
{
	@Override
	public void applyInverse( final double[] source, final double[] target )
	{
		assert source.length >= nSource && target.length >= nTarget: "Input dimensions too small.";

		final int s = transforms.size() - 1;
		if ( s > -1 )
		{
			if ( s > 0 )
			{
				transforms.get( s ).applyInverse( tmp, target );
				
				for ( int i = s - 1; i > 0; --i )
					transforms.get( i ).applyInverse( tmp, tmp );

				transforms.get( 0 ).applyInverse( source, tmp );
			}
			else
				transforms.get( 0 ).applyInverse( source, target );
		}
		else
		{
			System.arraycopy( target, 0, source, 0, source.length );
		}
	}

	@Override
	public void applyInverse( final float[] source, final float[] target )
	{
		assert source.length >= nSource && target.length >= nTarget: "Input dimensions too small.";

		final int s = transforms.size() - 1;
		if ( s > -1 )
		{
			for ( int d = 0; d < nTarget; ++d )
				tmp[ d ] = target[ d ];

			for ( int i = s; i > -1; --i )
				transforms.get( i ).applyInverse( tmp, tmp );

			for ( int d = 0; d < nSource; ++d )
				source[ d ] = ( float )tmp[ d ];
		}
		else
		{
			System.arraycopy( target, 0, source, 0, source.length );
		}
	}

	@Override
	public void applyInverse( final RealPositionable source, final RealLocalizable target )
	{
		assert source.numDimensions() >= nSource && target.numDimensions() >= nTarget: "Input dimensions too small.";

		final int s = transforms.size() - 1;
		if ( s > -1 )
		{
			if ( s > 0 )
			{
				transforms.get( s ).applyInverse( ptmp, target );
				
				for ( int i = s - 1; i > 0; --i )
					transforms.get( i ).applyInverse( tmp, tmp );

				transforms.get( 0 ).applyInverse( source, ptmp );
			}
			else
				transforms.get( 0 ).applyInverse( source, target );
		}
		else
		{
			source.setPosition( target );
		}
	}

	@Override
	public InvertibleRealTransform inverse()
	{
		return new InverseRealTransform( this );
	}

	@Override
	public InvertibleRealTransformSequence copy()
	{
		final InvertibleRealTransformSequence copy = new InvertibleRealTransformSequence();
		for ( final InvertibleRealTransform t : transforms )
			copy.add( t.copy() );
		return copy;
	}
}
