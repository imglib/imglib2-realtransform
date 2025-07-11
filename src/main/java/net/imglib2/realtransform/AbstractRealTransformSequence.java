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

import java.util.ArrayList;

import net.imglib2.RealLocalizable;
import net.imglib2.RealPoint;
import net.imglib2.RealPositionable;

/**
 * Shared properties of {@link RealTransformSequence} and
 * {@link InvertibleRealTransformSequence}, sequences of something that extends
 * {@link RealTransform RealTransforms}. If empty, will behave as the identity transformation.
 *
 * @author Stephan Saalfeld
 *
 * @param <R> transformation
 */
public class AbstractRealTransformSequence< R extends RealTransform > implements RealTransform
{
	final protected ArrayList< R > transforms = new ArrayList<>();

	protected double[] tmp = new double[ 0 ];

	protected RealPoint ptmp = RealPoint.wrap( tmp );

	protected int nSource = 0;

	protected int nTarget = 0;

	/**
	 * Append a {@link RealTransform} to the sequence.
	 *
	 * @param transform the RealTransform
	 */
	public void add( final R transform )
	{
		transforms.add( transform );

		if ( transforms.size() == 1 )
		{
			nSource = transform.numSourceDimensions();

			/**
			 * tmp has to be initialized at source size to enable
			 * #apply(float[], float[]) later which requires initial copy of
			 * source into tmp.
			 */
			tmp = new double[ nSource ];
			ptmp = RealPoint.wrap( tmp );
		}

		nTarget = transform.numTargetDimensions();

		if ( tmp.length < nTarget )
		{
			tmp = new double[ nTarget ];
			ptmp = RealPoint.wrap( tmp );
		}
	}

	@Override
	public int numSourceDimensions()
	{
		return nSource;
	}

	@Override
	public int numTargetDimensions()
	{
		return nTarget;
	}

	/**
	 * Returns true if either the sequence is empty, or if
	 * every transform in the sequence returns true for {@link isIdentity}.  This
	 * sequence could behave as the identity even if this method returns false,
	 * for example, if it contains only a transform and its inverse.
	 *
	 * @return true if empty or contains only identity transforms.
	 */
	@Override
	public boolean isIdentity()
	{
		if ( transforms.size() == 0 )
		{
			return true;
		}
		else
		{
			// if any transform in the sequence is not identity, this sequence
			// is not the identity
			for ( final R t : transforms )
			{
				if ( !t.isIdentity() )
					return false;
			}
			return true;
		}
	}

	@Override
	public void apply( final double[] source, final double[] target )
	{
		assert source.length >= nSource && target.length >= nTarget: "Input dimensions too small.";

		final int s = transforms.size() - 1;
		if ( s > -1 )
		{
			if ( s > 0 )
			{
				transforms.get( 0 ).apply( source, tmp );

				for ( int i = 1; i < s; ++i )
					transforms.get( i ).apply( tmp, tmp );

				transforms.get( s ).apply( tmp, target );
			}
			else
				transforms.get( 0 ).apply( source, target );
		}
		else
		{
			System.arraycopy( source, 0, target, 0, target.length );
		}
	}

	@Override
	public void apply( final float[] source, final float[] target )
	{
		assert source.length >= nSource && target.length >= nTarget: "Input dimensions too small.";

		final int s = transforms.size() - 1;
		if ( s > -1 )
		{
			for ( int d = 0; d < nSource; ++d )
				tmp[ d ] = source[ d ];

			for ( final RealTransform t : transforms )
				t.apply( tmp, tmp );

			for ( int d = 0; d < nTarget; ++d )
				target[ d ] = ( float )tmp[ d ];
		}
		else
		{
			System.arraycopy( source, 0, target, 0, target.length );
		}
	}

	@Override
	public void apply( final RealLocalizable source, final RealPositionable target )
	{
		assert source.numDimensions() >= nSource && target.numDimensions() >= nTarget: "Input dimensions too small.";

		final int s = transforms.size() - 1;
		if ( s > -1 )
		{
			if ( s > 0 )
			{
				transforms.get( 0 ).apply( source, ptmp );
				for ( int i = 1; i < s; ++i )
					transforms.get( i ).apply( tmp, tmp );

				transforms.get( s ).apply( ptmp, target );
			}
			else
				transforms.get( 0 ).apply( source, target );
		}
		else
		{
			target.setPosition( source );
		}
	}

	@SuppressWarnings( "unchecked" )
	@Override
	public AbstractRealTransformSequence< R > copy()
	{
		final AbstractRealTransformSequence< R > copy = new AbstractRealTransformSequence<>();
		for ( final R t : transforms )
			copy.add( ( R ) t.copy() );
		return copy;
	}
}
