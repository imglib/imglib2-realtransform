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

import java.util.Arrays;

import net.imglib2.RealLocalizable;
import net.imglib2.RealPositionable;

/**
 * A 2D polynomial transform of n-th order.
 *
 * Copied from https://github.com/axtimwalde/mpicbg/blob/master/mpicbg/src/main/java/mpicbg/models/PolynomialTransform2D.java
 *
 * @author Stephan Saalfeld
 */
public class PolynomialTransform2D implements RealTransform
{
	/**
	 * order of the polynomial transform
	 */
	protected int order = 0;

	/**
	 * holds two coefficients for each polynomial coefficient, including 1
	 * initialized at 0 order, i.e. translation, the order follows that
	 * specified at
	 *
	 * http://bishopw.loni.ucla.edu/AIR5/2Dnonlinear.html#polylist
	 *
	 * two times
	 */
	protected double[] a = new double[ 2 ];

	/**
	 * register to hold all polynomial terms during applyInPlace following the
	 * order specified at
	 *
	 * http://bishopw.loni.ucla.edu/AIR5/2Dnonlinear.html#polylist
	 *
	 * excluding 1 because we want to avoid repeated multiplication with 1
	 */
	protected double[] polTerms = new double[ 0 ];

	/**
	 * Calculate the maximum order of a polynom whose number of polyynomial
	 * terms is smaller or equal a given number.
	 *
	 * @param numPolTerms the number of terms
	 * @return result of the calculation
	 */
	final static public int orderOf( final int numPolTerms )
	{
		return ( int )Math.nextUp( ( Math.sqrt( 2 * numPolTerms + 0.25 ) - 1.5 ) );
	}

	/**
	 * Calculate the number of polynomial terms for a 2d polynomial transform
	 * of given order.
	 *
	 * @param order of the polynomial
	 * @return result of the calculation
	 */
	final static public int numPolTerms( final int order )
	{
		return ( int )Math.round( ( order + 2 ) * ( order + 1 ) * 0.5 );
	}

	/**
	 * Set the coefficients.  The number of coefficients implicitly specifies
	 * the order of the {@link PolynomialTransform2D} which is set to the
	 * highest order that is fully specified by the provided coefficients.
	 * The coefficients are interpreted in the order specified at
	 *
	 * http://bishopw.loni.ucla.edu/AIR5/2Dnonlinear.html#polylist
	 *
	 * , first for x', then for y'.  It is thus not possible to omit higher
	 * order coefficients assuming that they would become 0.  The passed vararg
	 * array is used directly without copy which enables direct access to the
	 * coefficients from calling code.  Use this option wisely.
	 *
	 * @param a coefficients
	 */
	public void set( final double... a )
	{
		order = orderOf( a.length / 2 );
		final int numPolTerms = numPolTerms( order );

		this.a = a;
		/* this would certainly be safer but means that we do not have access to the coefficients later */
//		this.a =  new double[ numPolTerms * 2 ];
//		System.arraycopy( a, 0, this.a, 0, this.a.length );

		polTerms = new double[ numPolTerms - 1 ];
	}

	protected void populateTerms( final double x, final double y )
	{
		if ( order == 0 ) return;
		polTerms[ 0 ] = x;
		polTerms[ 1 ] = y;
		for ( int o = 2, i = 2; o <= order; ++o, i += o )
		{
			for ( int p = 0; p < o; ++p)
			{
				polTerms[ i + p ] = polTerms[ i + p - o ] * x;
			}
			polTerms[ i + o ] = polTerms[ i - 1 ] * y;
		}
	}

	protected void printTerms()
	{
		final String[] polTermString = new String[ polTerms.length ];
		if ( order == 0 )
			System.out.println( "No polynomial terms." );
		polTermString[ 0 ] = "x";
		polTermString[ 1 ] = "y";
		for ( int o = 2, i = 2; o <= order; ++o, i += o )
		{
			for ( int p = 0; p < o; ++p)
			{
				polTermString[ i + p ] = polTermString[ i + p - o ] + "x";
			}
			polTermString[ i + o ] = polTermString[ i - 1 ] + "y";
		}
		System.out.println( Arrays.toString( polTermString ) );
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
		populateTerms( source[ 0 ], source[ 1 ] );
		target[ 0 ] = a[ 0 ];
		for ( int i = 0; i < polTerms.length;)
			target[ 0 ] += polTerms[ i ] * a[ ++i ];
		final int numPolTerms = polTerms.length + 1;
		target[ 1 ] = a[ numPolTerms ];
		for ( int i = 0; i < polTerms.length;)
			target[ 1 ] += polTerms[ i ] * a[ ++i + numPolTerms ];
	}

	@Override
	public void apply( final float[] source, final float[] target )
	{
		populateTerms( source[ 0 ], source[ 1 ] );
		double x = a[ 0 ];
		for ( int i = 0; i < polTerms.length;)
			x += polTerms[ i ] * a[ ++i ];
		final int numPolTerms = polTerms.length + 1;
		double y = a[ numPolTerms ];
		for ( int i = 0; i < polTerms.length;)
			y += polTerms[ i ] * a[ ++i + numPolTerms ];
		target[ 0 ] = ( float )x;
		target[ 1 ] = ( float )y;
	}

	@Override
	public void apply( RealLocalizable source, RealPositionable target )
	{
		populateTerms( source.getDoublePosition( 0 ), source.getDoublePosition( 1 ) );
		double x = a[ 0 ];
		for ( int i = 0; i < polTerms.length;)
			x += polTerms[ i ] * a[ ++i ];
		final int numPolTerms = polTerms.length + 1;
		double y = a[ numPolTerms ];
		for ( int i = 0; i < polTerms.length;)
			y += polTerms[ i ] * a[ ++i + numPolTerms ];
		target.setPosition( x, 0 );
		target.setPosition( y, 1 );
	}

	@Override
	public RealTransform copy()
	{
		final PolynomialTransform2D copy = new PolynomialTransform2D();
		copy.set( a.clone() );
		return copy;
	}
}
