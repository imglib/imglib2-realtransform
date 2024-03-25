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

import net.imglib2.RealLocalizable;
import net.imglib2.RealPositionable;
import net.imglib2.util.LinAlgHelpers;

/**
 * 2D-homography {@link RealTransform} to be applied to points in 2D-space.
 *
 * Copied from https://github.com/axtimwalde/mpicbg/blob/master/mpicbg/src/main/java/mpicbg/models/HomographyModel2D.java
 *
 * @author Stephan Saalfeld
 */
public class HomographyTransform2D implements InvertibleRealTransform
{
	protected double
			m00 = 1, m01 = 0, m02 = 0,
			m10 = 0, m11 = 1, m12 = 0,
			m20 = 0, m21 = 0, m22 = 1;

	public void set(
			final double m00, final double m01, final double m02,
			final double m10, final double m11, final double m12,
			final double m20, final double m21, final double m22 )
	{
		this.m00 = m00;
		this.m01 = m01;
		this.m02 = m02;

		this.m10 = m10;
		this.m11 = m11;
		this.m12 = m12;

		this.m20 = m20;
		this.m21 = m21;
		this.m22 = m22;

		invert();
	}

	protected double
			i00 = 1, i01 = 0, i02 = 0,
			i10 = 0, i11 = 1, i12 = 0,
			i20 = 0, i21 = 0, i22 = 1;

	final private void invert()
	{
		final double det = LinAlgHelpers.det3x3(
				m00, m01, m02,
				m10, m11, m12,
				m20, m21, m22 );

		i00 = ( m11 * m22 - m12 * m21 ) / det;
		i01 = ( m02 * m21 - m01 * m22 ) / det;
		i02 = ( m01 * m12 - m02 * m11 ) / det;

		i10 = ( m12 * m20 - m10 * m22 ) / det;
		i11 = ( m00 * m22 - m02 * m20 ) / det;
		i12 = ( m02 * m10 - m00 * m12 ) / det;

		i20 = ( m10 * m21 - m11 * m20 ) / det;
		i21 = ( m01 * m20 - m00 * m21 ) / det;
		i22 = ( m00 * m11 - m01 * m10 ) / det;
	}

	@Override
	final public void apply( final double[] source, final double[] target )
	{
		assert source.length >= 2 && source.length >= 2 : "2d homographies can be applied to 2d points only.";

		final double s = m20 * source[ 0 ] + m21 * source[ 1 ] + m22;
		final double t0 = m00 * source[ 0 ] + m01 * source[ 1 ] + m02;
		final double t1 = m10 * source[ 0 ] + m11 * source[ 1 ] + m12;

		target[ 0 ] = t0 / s;
		target[ 1 ] = t1 / s;
	}

	@Override
	final public void apply( final float[] source, final float[] target )
	{
		assert source.length >= 2 && source.length >= 2 : "2d homographies can be applied to 2d points only.";

		final double s = m20 * source[ 0 ] + m21 * source[ 1 ] + m22;
		final double t0 = m00 * source[ 0 ] + m01 * source[ 1 ] + m02;
		final double t1 = m10 * source[ 0 ] + m11 * source[ 1 ] + m12;

		target[ 0 ] = ( float )( t0 / s );
		target[ 1 ] = ( float )( t1 / s );
	}

	@Override
	final public void apply( final RealLocalizable source, final RealPositionable target )
	{
		assert source.numDimensions() >= 2 && source.numDimensions() >= 2 : "2d homographies can be applied to 2d points only.";

		final double s = m20 * source.getDoublePosition( 0 ) + m21 * source.getDoublePosition( 1 ) + m22;
		final double t0 = m00 * source.getDoublePosition( 0 ) + m01 * source.getDoublePosition( 1 ) + m02;
		final double t1 = m10 * source.getDoublePosition( 0 ) + m11 * source.getDoublePosition( 1 ) + m12;

		target.setPosition( t0 / s, 0 );
		target.setPosition( t1 / s, 1 );
	}

	@Override
	final public void applyInverse( final double[] source, final double[] target )
	{
		assert source.length >= 2 && source.length >= 2 : "2d homographies can be applied to 2d points only.";

		final double s = i20 * target[ 0 ] + i21 * target[ 1 ] + i22;
		final double t0 = i00 * target[ 0 ] + i01 * target[ 1 ] + i02;
		final double t1 = i10 * target[ 0 ] + i11 * target[ 1 ] + i12;

		source[ 0 ] = t0 / s;
		source[ 1 ] = t1 / s;
	}

	@Override
	final public void applyInverse( final float[] source, final float[] target )
	{
		assert source.length >= 2 && source.length >= 2 : "2d homographies can be applied to 2d points only.";

		final double s = i20 * target[ 0 ] + i21 * target[ 1 ] + i22;
		final double t0 = i00 * target[ 0 ] + i01 * target[ 1 ] + i02;
		final double t1 = i10 * target[ 0 ] + i11 * target[ 1 ] + i12;

		source[ 0 ] = ( float )( t0 / s );
		source[ 1 ] = ( float )( t1 / s );
	}

	@Override
	final public void applyInverse( final RealPositionable source, final RealLocalizable target )
	{
		assert source.numDimensions() >= 2 && source.numDimensions() >= 2 : "2d homographies can be applied to 2d points only.";

		final double s = i20 * target.getDoublePosition( 0 ) + i21 * target.getDoublePosition( 1 ) + i22;
		final double t0 = i00 * target.getDoublePosition( 0 ) + i01 * target.getDoublePosition( 1 ) + i02;
		final double t1 = i10 * target.getDoublePosition( 0 ) + i11 * target.getDoublePosition( 1 ) + i12;

		source.setPosition( t0 / s, 0 );
		source.setPosition( t1 / s, 1 );
	}

	final public void set( final HomographyTransform2D m )
	{
		m00 = m.m00;
		m01 = m.m01;
		m02 = m.m02;

		m10 = m.m10;
		m11 = m.m11;
		m12 = m.m12;

		m20 = m.m20;
		m21 = m.m21;
		m22 = m.m22;


		i00 = m.i00;
		i01 = m.i01;
		i02 = m.i02;

		i10 = m.i10;
		i11 = m.i11;
		i12 = m.i12;

		i20 = m.i20;
		i21 = m.i21;
		i22 = m.i22;
	}

	@Override
	public HomographyTransform2D copy()
	{
		final HomographyTransform2D m = new HomographyTransform2D();

		m.m00 = m00;
		m.m01 = m01;
		m.m02 = m02;

		m.m10 = m10;
		m.m11 = m11;
		m.m12 = m12;

		m.m20 = m20;
		m.m21 = m21;
		m.m22 = m22;


		m.i00 = i00;
		m.i01 = i01;
		m.i02 = i02;

		m.i10 = i10;
		m.i11 = i11;
		m.i12 = i12;

		m.i20 = i20;
		m.i21 = i21;
		m.i22 = i22;

		return m;
	}



	@Override
	public String toString()
	{
		return
				"[[ " + m00 + " " + m01 + " " + m02 + " ], " +
				"[ " + m10 + " " + m11 + " " + m12 + " ], " +
				"[ " + m20 + " " + m21 + " " + m22 + " ]]";
	}

	@Override
	final public HomographyTransform2D inverse()
	{
		final HomographyTransform2D m = new HomographyTransform2D();

		m.m00 = i00;
		m.m01 = i01;
		m.m02 = i02;

		m.m10 = i00;
		m.m11 = i11;
		m.m12 = i12;

		m.m20 = i20;
		m.m21 = i21;
		m.m22 = i22;


		m.i00 = m00;
		m.i01 = m01;
		m.i02 = m02;

		m.i10 = m00;
		m.i11 = m11;
		m.i12 = m12;

		m.i20 = m20;
		m.i21 = m21;
		m.i22 = m22;

		return m;
	}

	@Override
	public int numSourceDimensions()
	{
		return 2;
	}

	@Override
	public int numTargetDimensions()
	{
		return 2;
	}
}
