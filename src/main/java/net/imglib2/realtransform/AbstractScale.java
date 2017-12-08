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
import net.imglib2.RealPoint;
import net.imglib2.RealPositionable;

/**
 * <em>n</em>-d arbitrary scaling.  Abstract base implementation.
 *
 * @author Stephan Saalfeld
 */
abstract public class AbstractScale implements ScaleGet
{
	final protected double[] s;

	final protected RealPoint[] ds;

	protected AbstractScale( final double[] s, final RealPoint[] ds )
	{
		this.s = s;
		this.ds = ds;
	}

	public AbstractScale( final double... s )
	{
		this.s = s.clone();
		ds = new RealPoint[ s.length ];
	}

	/**
	 * Set the scale vector.
	 * 
	 * @param s
	 *            s.length &lt;= the number of dimensions of this
	 *            {@link AbstractScale}
	 */
	abstract public void set( final double... s );

	@Override
	public void applyInverse( final double[] source, final double[] target )
	{
		assert source.length >= s.length && target.length >= s.length : "Input dimensions too small.";
		
		for ( int d = 0; d < s.length; ++d )
			source[ d ] = target[ d ] / s[ d ];
	}

	@Override
	public void applyInverse( final float[] source, final float[] target )
	{
		assert source.length >= s.length && target.length >= s.length : "Input dimensions too small.";
		
		for ( int d = 0; d < s.length; ++d )
			source[ d ] = ( float )( target[ d ] / s[ d ] );
		
	}

	@Override
	public void applyInverse( final RealPositionable source, final RealLocalizable target )
	{
		assert source.numDimensions() >= s.length && target.numDimensions() >= s.length : "Input dimensions too small.";
		
		for ( int d = 0; d < s.length; ++d )
			source.setPosition( target.getDoublePosition( d ) / s[ d ], d );
	}

	@Override
	abstract public AbstractScale inverse();

	@Override
	public int numDimensions()
	{
		return s.length;
	}

	@Override
	public int numSourceDimensions()
	{
		return s.length;
	}

	@Override
	public int numTargetDimensions()
	{
		return s.length;
	}

	@Override
	public void apply( final double[] source, final double[] target )
	{
		assert source.length >= s.length && target.length >= s.length : "Input dimensions too small.";
		
		for ( int d = 0; d < s.length; ++d )
			target[ d ] = source[ d ] * s[ d ];
	}

	@Override
	public void apply( final float[] source, final float[] target )
	{
		assert source.length >= s.length && target.length >= s.length : "Input dimensions too small.";
		
		for ( int d = 0; d < s.length; ++d )
			target[ d ] = ( float )( source[ d ] * s[ d ] );
	}

	@Override
	public void apply( final RealLocalizable source, final RealPositionable target )
	{
		assert source.numDimensions() >= s.length && target.numDimensions() >= s.length : "Input dimensions too small.";
		
		for ( int d = 0; d < s.length; ++d )
			target.setPosition( source.getDoublePosition( d ) * s[ d ], d );
	}

	@Override
	public double get( final int row, final int column )
	{
		assert row >= 0 && row < numDimensions() : "Dimension index out of bounds.";
		
		return row == column ? s[ row ] : 0;
	}

	@Override
	public double[] getRowPackedCopy()
	{
		final int step = s.length + 2;
		final double[] matrix = new double[ s.length * s.length + s.length ];
		for ( int d = 0; d < s.length; ++d )
			matrix[ d * step ] = s[ d ];
		return matrix;
	}

	@Override
	public RealLocalizable d( final int d )
	{
		assert d >= 0 && d < numDimensions() : "Dimension index out of bounds.";
		
		return ds[ d ];
	}

	@Override
	abstract public AbstractScale copy();

	@Override
	public double getScale( final int d )
	{
		return s[ d ];
	}

	@Override
	public double[] getScaleCopy()
	{
		return s.clone();
	}
	
	@Override
	public double getTranslation( final int d ) {
		return 0.0;
	}

	@Override
	public double[] getTranslationCopy() {
		return new double[ s.length ];
	}
}
