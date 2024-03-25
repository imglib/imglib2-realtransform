/*-
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
package net.imglib2.realtransform.inverse;

import net.imglib2.RealLocalizable;
import net.imglib2.RealPositionable;
import net.imglib2.realtransform.AffineTransform;
import net.imglib2.realtransform.RealTransform;

/**
 * Use finite differences to estimate the jacobian of an arbitrary
 * RealTransform.
 * 
 * @author John Bogovic
 *
 */
public class RealTransformFiniteDerivatives extends AbstractDifferentiableRealTransform
{
	protected final RealTransform transform;

	protected final AffineTransform jacobian;

	protected double step;

	public RealTransformFiniteDerivatives( final RealTransform transform )
	{
		this.transform = transform;
		int srcD = transform.numSourceDimensions();
		int tgtD = transform.numTargetDimensions();
		jacobian = new AffineTransform( srcD > tgtD ? srcD : tgtD );
		step = 0.01;
	}

	public void setStep( double step )
	{
		this.step = step;
	}

	public int numSourceDimensions()
	{
		return transform.numSourceDimensions();
	}

	public int numTargetDimensions()
	{
		return transform.numTargetDimensions();
	}

	public void apply( double[] source, double[] target )
	{
		transform.apply( source, target );
	}

	public void apply( RealLocalizable source, RealPositionable target )
	{
		transform.apply( source, target );
	}

	public RealTransformFiniteDerivatives copy()
	{
		return new RealTransformFiniteDerivatives( transform.copy() );
	}

	/**
	 * Estimates the jacobian matrix at x of the wrapped RealTransform. Returns
	 * an {@link AffineTransform} so that matrix operations are convenient.
	 * 
	 * @param x
	 *            the point at which to estimate the jacobian
	 * @return the jacobian
	 */
	public AffineTransform jacobian( double[] x )
	{
		int ndims = numSourceDimensions();
		double[] p = new double[ ndims ];
		double[] q = new double[ ndims ];
		double[] qc = new double[ ndims ];

		double[][] newjac = new double[ ndims ][ ndims+1 ];

		transform.apply( x, qc );

		for ( int i = 0; i < ndims; i++ )
		{
			for ( int j = 0; j < ndims; j++ )
				if ( j == i )
					p[ j ] = x[ j ] + step;
				else
					p[ j ] = x[ j ];

			transform.apply( p, q );

			for ( int j = 0; j < ndims; j++ )
			{
				newjac[ j ][ i ] = ( q[ j ] - qc[ j ] ) / step;
			}
		}
		jacobian.set( newjac );

		return jacobian;
	}
	
}
