/*-
 * #%L
 * ImgLib2: a general-purpose, multidimensional image processing library.
 * %%
 * Copyright (C) 2009 - 2020 Tobias Pietzsch, Stephan Preibisch, Stephan Saalfeld,
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
 * Wraps and regularizes a {@link DifferentiableRealTransform}.
 * <p>
 * Returns a convex combination of the identity matrix and the jacobian of the
 * wrapped DifferentiableReal Transform. Specifically: 
 * epsilon * I + ( 1 - * epsilon ) * J
 * 
 * @author John Bogovic &lt;bogovicj@janelia.hhmi.org&gt;
 *
 */
public class RegularizedDifferentiableRealTransform extends AbstractDifferentiableRealTransform
{

	protected final DifferentiableRealTransform dxfm;

	protected final double epsilon;

	public RegularizedDifferentiableRealTransform( final DifferentiableRealTransform dxfm, final double epsilon )
	{
		this.dxfm = dxfm;
		this.epsilon = epsilon;
	}

	/**
	 * Returns the jacobian matrix of this transform at the point x.
	 * 
	 * @param x
	 *            the point
	 * @return the jacobian
	 */
	public AffineTransform jacobian( double[] x )
	{
		AffineTransform jac = dxfm.jacobian( x );
		for ( int i = 0; i < jac.numSourceDimensions(); i++ )
			jac.set( epsilon + ( 1 - epsilon ) * jac.get( i, i ), i, i );

		return jac;
	}

	public int numSourceDimensions()
	{
		return dxfm.numSourceDimensions();
	}

	public int numTargetDimensions()
	{
		return dxfm.numTargetDimensions();
	}

	public void apply( double[] source, double[] target )
	{
		dxfm.apply( source, target );
	}

	public void apply( RealLocalizable source, RealPositionable target )
	{
		dxfm.apply( source, target );
	}

	public DifferentiableRealTransform copy()
	{
		return dxfm.copy();
	}

}
