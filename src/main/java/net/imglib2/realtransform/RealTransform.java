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
 * Transformation from R<sup><em>n</em></sup> to R<sup><em>m</em></sup>.
 *
 * <p>
 * Applying the transformation to an <em>n</em>-dimensional <em>source</em>
 * vector yields an <em>m</em>-dimensional <em>target</em> vector.
 * </p>
 * <p>
 * Source and target vectors are required to be at least <em>n</em>- or
 * <em>m</em>-dimensional respectively. The {@link RealTransform} is expected to
 * leave all dimensions beyond <em>n</em>-1 in the source vector and <em>m</em>
 * -1 in the target vector unchanged.
 * </p>
 * <p>
 * Implementations must support an in-place apply (i.e., passing the same
 * object as both <em>source</em> and <em>target</em>).
 * </p>
 *
 * @author Tobias Pietzsch
 * @author Stephan Saalfeld
 */
public interface RealTransform
{
	/**
	 * Returns <em>n</em>, the minimal number of dimension of the source vector.
	 *
	 * @return the dimension of the source vector.
	 */
	public int numSourceDimensions();

	/**
	 * Returns <em>m</em>, the minimal dimension of the target vector.
	 *
	 * @return the dimension of the target vector.
	 */
	public int numTargetDimensions();

	/**
	 * Apply the {@link RealTransform} to a source vector to obtain a target
	 * vector.
	 *
	 * @param source
	 *            source coordinates, length must be {@code >=}
	 *            {@link #numSourceDimensions()}
	 * @param target
	 *            set this to the target coordinates, length must be {@code >=}
	 *            {@link #numTargetDimensions()}
	 */
	public void apply( final double[] source, final double[] target );

	/**
	 * Apply the {@link RealTransform} to a source vector to obtain a target
	 * vector.
	 *
	 * @param source
	 *            source coordinates, length must be {@code >=}
	 *            {@link #numSourceDimensions()}
	 * @param target
	 *            set this to the target coordinates, length must be {@code >=}
	 *            {@link #numTargetDimensions()}
	 *
	 * @deprecated use double precision instead
	 */
	@Deprecated
	public default void apply( final float[] source, final float[] target )
	{
		assert source.length >= numSourceDimensions() && target.length >= numTargetDimensions() : "Input dimensions too small.";

		final double[] doubleSource = new double[ source.length ];
		final double[] doubleTarget = new double[ target.length ];

		for ( int d = 0; d < source.length; ++d )
			doubleSource[ d ] = source[ d ];

		apply( doubleSource, doubleTarget );

		for ( int d = 0; d < target.length; ++d )
			target[ d ] = ( float )doubleTarget[ d ];
	}

	/**
	 * Apply the {@link RealTransform} to a source {@link RealLocalizable} to
	 * obtain a target {@link RealPositionable}.
	 *
	 * @param source
	 *            source coordinates, {@link RealLocalizable#numDimensions()
	 *            numDimensions()} must be {@code >=}
	 *            {@link #numSourceDimensions()}
	 * @param target
	 *            set this to the target coordinates,
	 *            {@link RealLocalizable#numDimensions() numDimensions()} must
	 *            be {@code >=} {@link #numTargetDimensions()}
	 */
	public void apply( final RealLocalizable source, final RealPositionable target );

	/**
	 * Create a deep copy of this {@link RealTransform}.
	 *
	 * <p>
	 * Deep copying is required to make sure that stateful {@link RealTransform
	 * RealTransforms} can be duplicated for concurrent code.
	 * </p>
	 *
	 * @return deep copy
	 */
	public RealTransform copy();

	/**
	 * Check if the {@link RealTransform} is identity.
	 * 
	 * @return true if identity
	 */
	default boolean isIdentity()
	{
		return false;
	}
}
