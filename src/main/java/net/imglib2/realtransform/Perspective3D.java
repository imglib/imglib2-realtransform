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

import net.imglib2.EuclideanSpace;
import net.imglib2.RealLocalizable;
import net.imglib2.RealPositionable;

/**
 * 3d perspective transformation. Implemented as singleton as it has no
 * properties.
 * 
 * @author Stephan Saalfeld
 */
public class Perspective3D implements InvertibleRealTransform, EuclideanSpace
{
	final static protected Perspective3D instance = new Perspective3D();

	protected Perspective3D()
	{}

	static public Perspective3D getInstance()
	{
		return instance;
	}

	@Override
	public int numDimensions()
	{
		return 3;
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
		assert source.length >= 3 && target.length >= 3: "Input dimensions too small.";

		target[ 0 ] = source[ 0 ] / source[ 2 ];
		target[ 1 ] = source[ 1 ] / source[ 2 ];
		target[ 2 ] = source[ 2 ];
	}

	@Override
	public void apply( final float[] source, final float[] target )
	{
		assert source.length >= 3 && target.length >= 3: "Input dimensions too small.";

		target[ 0 ] = source[ 0 ] / source[ 2 ];
		target[ 1 ] = source[ 1 ] / source[ 2 ];
		target[ 2 ] = source[ 2 ];
	}

	@Override
	public void apply( final RealLocalizable source, final RealPositionable target )
	{
		assert source.numDimensions() >= 3 && target.numDimensions() >= 3: "Input dimensions too small.";

		final double z = source.getDoublePosition( 2 );
		target.setPosition( source.getDoublePosition( 0 ) / z, 0 );
		target.setPosition( source.getDoublePosition( 1 ) / z, 1 );
		target.setPosition( source.getDoublePosition( 2 ), 2 );
	}

	@Override
	public void applyInverse( final double[] source, final double[] target )
	{
		assert source.length >= 3 && target.length >= 3: "Input dimensions too small.";

		source[ 0 ] = target[ 0 ] * target[ 2 ];
		source[ 1 ] = target[ 1 ] * target[ 2 ];
		source[ 2 ] = target[ 2 ];
	}

	@Override
	public void applyInverse( final float[] source, final float[] target )
	{
		assert source.length >= 3 && target.length >= 3: "Input dimensions too small.";

		source[ 0 ] = target[ 0 ] * target[ 2 ];
		source[ 1 ] = target[ 1 ] * target[ 2 ];
		source[ 2 ] = target[ 2 ];
	}

	@Override
	public void applyInverse( final RealPositionable source, final RealLocalizable target )
	{
		assert source.numDimensions() >= 3 && target.numDimensions() >= 3: "Input dimensions too small.";

		final double z = target.getDoublePosition( 2 );
		source.setPosition( target.getDoublePosition( 0 ) * z, 0 );
		source.setPosition( target.getDoublePosition( 1 ) * z, 1 );
		source.setPosition( target.getDoublePosition( 2 ), 2 );
	}

	@Override
	public InvertibleRealTransform inverse()
	{
		return InversePerspective3D.getInstance();
	}

	@Override
	public Perspective3D copy()
	{
		return this;
	}
}
