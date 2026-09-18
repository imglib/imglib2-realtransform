/*
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
 * A {@link RealTransform} representing an affine map x &rarr; Ax + b from an
 * N-dimensional source space to an M-dimensional target space.
 * <p>
 * Unlike {@link AffineTransform} it need not be invertible. This class can also
 * represent non-invertible affine transformations between coordinate systems of
 * like dimension.
 * <p>
 * The matrix is stored row-major as M rows of N+1 values, with the translation
 * b in the last column of each row.
 */
public class AffineRealTransform implements RealTransform
{

	protected final int numSourceDimensions;

	protected final int numTargetDimensions;

	protected final double[] matrix; // stored row major

	/**
	 * Creates the identity transform. If the source and target dimensions
	 * differ, extra source dimensions are dropped and extra target dimensions
	 * are set to zero.
	 */
	public AffineRealTransform( int numSourceDimensions, int numTargetDimensions )
	{
		this.numSourceDimensions = numSourceDimensions;
		this.numTargetDimensions = numTargetDimensions;

		matrix = new double[ ( numSourceDimensions + 1 ) * numTargetDimensions ];
		final int n = Math.min( numSourceDimensions, numTargetDimensions );
		for ( int d = 0; d < n; d++ )
			matrix[ d * ( numSourceDimensions + 1 ) + d ] = 1;
	}

	public AffineRealTransform( int numSourceDimensions, int numTargetDimensions, double[] matrix )
	{
		int expectedLength = ( numSourceDimensions + 1 ) * numTargetDimensions;
		if ( matrix.length != expectedLength )
			throw new IllegalArgumentException( "matrix length " + matrix.length + " != (numSourceDimensions + 1) * numTargetDimensions (" + expectedLength + ")" );

		this.numSourceDimensions = numSourceDimensions;
		this.numTargetDimensions = numTargetDimensions;
		this.matrix = matrix;
	}

	@Override
	public int numSourceDimensions()
	{
		return numSourceDimensions;
	}

	@Override
	public int numTargetDimensions()
	{
		return numTargetDimensions;
	}

	@Override
	public void apply( double[] src, double[] dst )
	{
		double[] tgt;
		if ( src == dst )
			tgt = new double[ numTargetDimensions ];
		else
			tgt = dst;

		int k = 0;
		for ( int j = 0; j < numTargetDimensions; j++ )
		{
			tgt[ j ] = 0;
			for ( int i = 0; i < numSourceDimensions; i++ )
				tgt[ j ] += matrix[ k++ ] * src[ i ];

			tgt[ j ] += matrix[ k++ ]; // translation component
		}

		if ( tgt != dst )
			System.arraycopy( tgt, 0, dst, 0, tgt.length );
	}

	@Override
	public void apply( RealLocalizable src, RealPositionable dst )
	{
		// This dedicated loop with a tmp variable
		// gets a 10-30% speedup in benchmarks over
		// calling the array apply method at the cost
		// of a duplicated implementation.
		if ( src == dst )
		{
			final double[] tmp = new double[ numTargetDimensions ];
			int k = 0;
			for ( int j = 0; j < numTargetDimensions; j++ )
			{
				double val = 0;
				for ( int i = 0; i < numSourceDimensions; i++ )
					val += matrix[ k++ ] * src.getDoublePosition( i );

				tmp[ j ] = val + matrix[ k++ ]; // translation component
			}

			// copy back only the target dimensions; dst may have more
			for ( int j = 0; j < numTargetDimensions; j++ )
				dst.setPosition( tmp[ j ], j );
			return;
		}

		int k = 0;
		for ( int j = 0; j < numTargetDimensions; j++ )
		{
			double val = 0;
			for ( int i = 0; i < numSourceDimensions; i++ )
				val += matrix[ k++ ] * src.getDoublePosition( i );

			dst.setPosition( val + matrix[ k++ ], j ); // translation component
		}
	}

	@Override
	public RealTransform copy()
	{
		return new AffineRealTransform( numSourceDimensions, numTargetDimensions, matrix.clone() );
	}

}
