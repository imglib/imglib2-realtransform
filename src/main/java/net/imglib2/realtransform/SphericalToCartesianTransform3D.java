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
 * Transforms 3D spherical to cartesian coordinates.
 *
 * The source coordinate <em>(r,&theta;,&phi;)</em> is converted to the cartesian
 * target coordinate <em>(x,y,z)</em>.
 *
 * For the inverse transform (cartesian to spherical), the range of the computed
 * <em>&theta;</em> and <em>&phi;</em> is <em>-pi</em> to <em>pi</em>.
 *
 * @author Tobias Pietzsch
 */
public final class SphericalToCartesianTransform3D implements InvertibleRealTransform
{
	private static final SphericalToCartesianTransform3D instance = new SphericalToCartesianTransform3D();

	private final InverseRealTransform inverse;

	public static SphericalToCartesianTransform3D getInstance()
	{
		return instance;
	}

	private SphericalToCartesianTransform3D()
	{
		inverse = new InverseRealTransform( this );
	}

	private static double x( final double r, final double inclination, final double azimuth )
	{
		return r * Math.sin( inclination ) * Math.cos( azimuth );
	}

	private static double y( final double r, final double inclination, final double azimuth )
	{
		return r * Math.sin( inclination ) * Math.sin( azimuth );
	}

	private static double z( final double r, final double inclination )
	{
		return r * Math.cos( inclination );
	}

	private static double r( final double x, final double y, final double z )
	{
		return Math.sqrt( x * x + y * y + z * z );
	}

	private static double inclination( final double x, final double y, final double z )
	{
		return Math.acos( z / r ( x, y, z ) );
	}

	private static double azimuth( final double x, final double y )
	{
		return Math.atan2( y, x );
	}

	@Override
	public int numSourceDimensions()
	{
		return 3;
	}

	@Override
	public int numTargetDimensions()
	{
		return 3;
	}

	@Override
	public void apply( final double[] source, final double[] target )
	{
		final double r = source[ 0 ];
		final double inclination = source[ 1 ];
		final double azimuth = source[ 2 ];
		target[ 0 ] = x( r, inclination, azimuth );
		target[ 1 ] = y( r, inclination, azimuth );
		target[ 2 ] = z( r, inclination );
	}

	@Override
	public void apply( final float[] source, final float[] target )
	{
		final double r = source[ 0 ];
		final double inclination = source[ 1 ];
		final double azimuth = source[ 2 ];
		target[ 0 ] = ( float ) x( r, inclination, azimuth );
		target[ 1 ] = ( float ) y( r, inclination, azimuth );
		target[ 2 ] = ( float ) z( r, inclination );
	}

	@Override
	public void apply( final RealLocalizable source, final RealPositionable target )
	{
		final double r = source.getDoublePosition( 0 );
		final double inclination = source.getDoublePosition( 1 );
		final double azimuth = source.getDoublePosition( 2 );
		target.setPosition( x( r, inclination, azimuth ), 0 );
		target.setPosition( y( r, inclination, azimuth ), 1 );
		target.setPosition( z( r, inclination ), 2 );
	}

	@Override
	public void applyInverse( final double[] source, final double[] target )
	{
		final double x = target[ 0 ];
		final double y = target[ 1 ];
		final double z = target[ 2 ];
		source[ 0 ] = r( x, y, z );
		source[ 1 ] = inclination( x, y, z );
		source[ 2 ] = azimuth( x, y );
	}

	@Override
	public void applyInverse( final float[] source, final float[] target )
	{
		final double x = target[ 0 ];
		final double y = target[ 1 ];
		final double z = target[ 2 ];
		source[ 0 ] = ( float ) r( x, y, z );
		source[ 1 ] = ( float ) inclination( x, y, z );
		source[ 2 ] = ( float ) azimuth( x, y );
	}

	@Override
	public void applyInverse( final RealPositionable source, final RealLocalizable target )
	{
		final double x = target.getDoublePosition( 0 );
		final double y = target.getDoublePosition( 1 );
		final double z = target.getDoublePosition( 2 );
		source.setPosition( r( x, y, z ), 0 );
		source.setPosition( inclination( x, y, z ), 1 );
		source.setPosition( azimuth( x, y ), 2 );
	}

	@Override
	public InvertibleRealTransform inverse()
	{
		return inverse;
	}

	@Override
	public SphericalToCartesianTransform3D copy()
	{
		return this;
	}
}
