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
 * Transforms 2D polar to cartesian coordinates.
 * 
 * The source coordinate <em>(r,&theta;)</em> is converted to the cartesian
 * target coordinate <em>(x,y)</em>.
 * 
 * For the inverse transform (cartesian to polar), the range of the computed
 * <em>&theta;</em> is <em>-pi</em> to <em>pi</em>.
 * 
 * TODO This can be a singleton
 * 
 * @author Tobias Pietzsch
 */
public class PolarToCartesianTransform2D implements InvertibleRealTransform
{
	private static double x( final double r, final double t )
	{
		return r * Math.cos( t );
	}

	private static double y( final double r, final double t )
	{
		return r * Math.sin( t );
	}

	private static double r( final double x, final double y )
	{
		return Math.sqrt( x * x + y * y );
	}

	private static double t( final double x, final double y )
	{
		return Math.atan2( y, x );
	}

	private final InverseRealTransform inverse;

	public PolarToCartesianTransform2D()
	{
		inverse = new InverseRealTransform( this );
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

	@Override
	public void apply( final double[] source, final double[] target )
	{
		final double r = source[ 0 ];
		final double t = source[ 1 ];
		target[ 0 ] = x( r, t );
		target[ 1 ] = y( r, t );
	}

	@Override
	public void apply( final float[] source, final float[] target )
	{
		final double r = source[ 0 ];
		final double t = source[ 1 ];
		target[ 0 ] = ( float ) x( r, t );
		target[ 1 ] = ( float ) y( r, t );
	}

	@Override
	public void apply( final RealLocalizable source, final RealPositionable target )
	{
		final double r = source.getDoublePosition( 0 );
		final double t = source.getDoublePosition( 1 );
		target.setPosition( x( r, t ), 0 );
		target.setPosition( y( r, t ), 1 );
	}

	@Override
	public void applyInverse( final double[] source, final double[] target )
	{
		final double x = target[ 0 ];
		final double y = target[ 1 ];
		source[ 0 ] = r( x, y );
		source[ 1 ] = t( x, y );
	}

	@Override
	public void applyInverse( final float[] source, final float[] target )
	{
		final double x = target[ 0 ];
		final double y = target[ 1 ];
		source[ 0 ] = ( float ) r( x, y );
		source[ 1 ] = ( float ) t( x, y );
	}

	@Override
	public void applyInverse( final RealPositionable source, final RealLocalizable target )
	{
		final double x = target.getDoublePosition( 0 );
		final double y = target.getDoublePosition( 1 );
		source.setPosition( r( x, y ), 0 );
		source.setPosition( t( x, y ), 1 );
	}

	@Override
	public InvertibleRealTransform inverse()
	{
		return inverse;
	}

	@Override
	public PolarToCartesianTransform2D copy()
	{
		return this;
	}
}
