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
/**
 * 
 */
package net.imglib2.realtransform;

import net.imglib2.RealLocalizable;
import net.imglib2.RealPoint;
import net.imglib2.RealPositionable;
import net.imglib2.concatenate.Concatenable;
import net.imglib2.concatenate.PreConcatenable;

/**
 * An <em>n</em> transform that applies a scaling first and then shifts coordinates.
 * This transform is faster than using an {@link AffineTransform} with 
 * entries on the diagonal only.
 * 
 * @author Philipp Hanslovsky
 */
public class ScaleAndTranslation implements 
ScaleAndTranslationGet, Concatenable< ScaleAndTranslationGet >, PreConcatenable< ScaleAndTranslationGet > 
{
	
	private final ScaleAndTranslation inverse;
	private final double[] scales;
	private final double[] translations;
	private final int nDim;
	
	

	/**
	 * @param scales Array containing scales, will be cloned.
	 * @param translations Array containing translations, will be cloned.
	 */
	public ScaleAndTranslation( final double[] scales, final double[] translations ) 
	{
		super();
		
		assert translations.length == scales.length;
		
		this.scales       = scales.clone(); // clone?
		this.translations = translations.clone(); // clone?
		this.nDim         = translations.length;
		this.inverse      = this.createInverse();
	}
	
	
	/**
	 * private constructor that takes inverse to avoid object creation when calling
	 * {@link #inverse}
	 * @param inverse 
	 * @param scales Array containing scales, will be cloned.
	 * @param translations Array containing translations, will be cloned.
	 * @param nDim Number of Dimensions
	 */
	private ScaleAndTranslation(
			final ScaleAndTranslation inverse, 
			final double[] scales,
			final double[] translations, 
			final int nDim 
			) 
	{
		super();
		assert translations.length == scales.length;
		this.inverse      = inverse;
		this.scales       = scales.clone();
		this.translations = translations.clone();
		this.nDim         = nDim;
	}
	

	@Override
	public int numSourceDimensions() 
	{
		return this.numDimensions();
	}

	@Override
	public int numTargetDimensions() 
	{
		return this.numDimensions();
	}

	@Override
	public void apply(final double[] source, final double[] target) 
	{
		assert source.length == nDim && target.length == nDim;
		for (int i = 0; i < nDim; i++) 
		{
			target[i] = scales[i]*source[i] + translations[i];
		}
	}

	@Override
	public void apply(final float[] source, final float[] target) 
	{
		assert source.length == nDim && target.length == nDim;
		for (int i = 0; i < nDim; i++) 
		{
			target[i] = (float) (scales[i]*source[i] + translations[i]);
		}
	}

	@Override
	public void apply(final RealLocalizable source, final RealPositionable target) 
	{
		assert source.numDimensions() == nDim && target.numDimensions() == nDim;
		for ( int d = 0; d < nDim; ++d ) 
		{
			target.setPosition( scales[d]*source.getDoublePosition( d ) + translations[d], d);
		}
	}

	@Override
	public void applyInverse(final double[] source, final double[] target) 
	{
		// target is the source for the inverse transform, thus switch order in call of this.inverse.apply
		this.inverse.apply( target, source );
	}

	@Override
	public void applyInverse(final float[] source, final float[] target) 
	{
		// target is the source for the inverse transform, thus switch order in call of this.inverse.apply
		this.inverse.apply( target, source );
	}

	@Override
	public void applyInverse(final RealPositionable source, final RealLocalizable target) 
	{
		// target is the source for the inverse transform, thus switch order in call of this.inverse.apply
		this.inverse.apply( target, source );
	}
	
	
	@Override
	public ScaleAndTranslation inverse() 
	{
		return this.inverse;
	}
	
	public ScaleAndTranslation createInverse() 
	{
		final double[] invertedShifts = new double[ nDim ];
		final double[] invertedScales = new double[ nDim ];
		for (int i = 0; i < nDim; i++) 
		{
			invertedScales[i] = 1.0 /scales[i];
			invertedShifts[i] = -translations[i] * invertedScales[i];
		}
		return new ScaleAndTranslation( this, invertedScales, invertedShifts, nDim );
	}

	@Override
	public ScaleAndTranslation copy() 
	{
		return new ScaleAndTranslation( inverse, scales, translations, nDim );
	}


	@Override
	public double getScale( int d ) 
	{
		return this.scales[ d ];
	}


	@Override
	public double[] getScaleCopy() 
	{
		return this.scales.clone();
	}


	@Override
	public double get( final int row, final int column ) 
	{
		if ( column == row )
			return this.scales[ row ];
		else if ( column == scales.length )
			return this.translations[ row ];
		else
			return 0.0;
	}


	@Override
	public double[] getRowPackedCopy() 
	{
		int m = this.scales.length;
		int n = m + 1;
		double[] result = new double[ m*n ];
		for ( int i = 0; i < m; ++i ) {
			int firstElementInRowIndex      = i*n;
			int lastElementInRowIndex       = firstElementInRowIndex + n - 1;
			int diagonalIndex               = firstElementInRowIndex + i;
			result[ diagonalIndex ]         = this.scales[ i ];
			result[ lastElementInRowIndex ] = this.translations[ i ];
		}
		return result;
	}


	@Override
	public RealPoint d( int d ) 
	{
		RealPoint rp = new RealPoint( nDim );
		rp.setPosition( this.scales[d], d );
		return rp;
	}


	@Override
	public int numDimensions() 
	{
		return this.nDim;
	}


	@Override
	public double getTranslation( int d ) 
	{
		return this.translations[ d ];
	}


	@Override
	public double[] getTranslationCopy() 
	{
		return this.translations.clone();
	}


	@Override
	public ScaleAndTranslation preConcatenate(
			ScaleAndTranslationGet a ) 
	{
		assert a.numDimensions() == this.nDim: "Dimensions do not match.";
		for ( int d = 0; d < this.nDim; ++d ) {
			double scale            = a.getScale( d );
			double translation      = this.translations[ d ];
			this.scales[ d ]       *= scale;
			this.translations[ d ]  = a.getTranslation( d ) + scale*translation;
		}
		return this;
	}


	@Override
	public Class< ScaleAndTranslationGet > getPreConcatenableClass() 
	{
		return ScaleAndTranslationGet.class;
	}


	@Override
	public ScaleAndTranslation concatenate(
			ScaleAndTranslationGet a) 
	{
		assert a.numDimensions() == this.nDim: "Dimensions do not match.";
		for ( int d = 0; d < this.nDim; ++d ) {
			double scale            = this.scales[ d ];
			this.scales[ d ]       *= a.getScale( d );
			this.translations[ d ] += a.getTranslation( d )*scale;
		}
		return this;
	}


	@Override
	public Class< ScaleAndTranslationGet > getConcatenableClass() 
	{
		return ScaleAndTranslationGet.class;
	}

}
