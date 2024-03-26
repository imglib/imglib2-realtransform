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

import net.imglib2.RandomAccess;
import net.imglib2.RealInterval;
import net.imglib2.RealPoint;
import net.imglib2.RealRandomAccess;
import net.imglib2.RealRandomAccessible;

/**
 * A {@link RealRandomAccessible} whose samples are generated from a
 * {@link RealRandomAccessible} transformed by an {@link RealTransform}.
 * Changing the {@link RealTransform} will affect the
 * {@link RealTransformRealRandomAccessible} but not any existing
 * {@link RealRandomAccess} on it because each {@link RealRandomAccess}
 * internally works with a copy of the transform.  Make sure that you request
 * a new {@link RandomAccess} after modifying the transformation.
 *
 * @author Stephan Saalfeld
 */
public class RealTransformRealRandomAccessible< T, R extends RealTransform > implements RealRandomAccessible< T >
{
	final protected RealRandomAccessible< T > source;

	final protected R transformToSource;

	/**
	 * {@link RealRandomAccess} that generates its samples from a source
	 * {@link RealRandomAccessible} at coordinates transformed by a
	 * {@link RealTransform}.
	 *
	 */
	public class RealTransformRealRandomAccess extends RealPoint implements RealRandomAccess< T >
	{
		final protected RealRandomAccess< T > sourceAccess;

		final protected R transformCopy;

		@SuppressWarnings( "unchecked" )
		protected RealTransformRealRandomAccess()
		{
			super( transformToSource.numSourceDimensions() );
			sourceAccess = source.realRandomAccess();
			transformCopy = ( R )transformToSource.copy();
		}

		@SuppressWarnings( "unchecked" )
		private RealTransformRealRandomAccess( final RealTransformRealRandomAccess a )
		{
			super( a );
			this.sourceAccess = a.sourceAccess.copy();
			transformCopy = ( R )a.transformCopy.copy();
		}

		final protected void apply()
		{
			transformCopy.apply( this, sourceAccess );
		}

		@Override
		public T get()
		{
			apply();
			return sourceAccess.get();
		}

		@Override
		public RealTransformRealRandomAccess copy()
		{
			return new RealTransformRealRandomAccess( this );
		}
	}

	public RealTransformRealRandomAccessible( final RealRandomAccessible< T > source, final R transformToSource )
	{
		assert source.numDimensions() == transformToSource.numTargetDimensions();

		this.source = source;
		this.transformToSource = transformToSource;
	}

	@Override
	public int numDimensions()
	{
		return transformToSource.numSourceDimensions();
	}

	@Override
	public RealTransformRealRandomAccess realRandomAccess()
	{
		return new RealTransformRealRandomAccess();
	}

	/**
	 * To be overridden for {@link RealTransform} that can estimate the
	 * boundaries of a transferred {@link RealInterval}.
	 */
	@Override
	public RealTransformRealRandomAccess realRandomAccess( final RealInterval interval )
	{
		return realRandomAccess();
	}

	/**
	 * @return source {@link RealRandomAccessible}
	 */
	public RealRandomAccessible< T > getSource()
	{
		return source;
	}

	/**
	 * @return transform applied to source
	 */
	public R getTransformToSource()
	{
		return transformToSource;
	}

	@Override
	public T getType()
	{
		return source.getType();
	}
}
