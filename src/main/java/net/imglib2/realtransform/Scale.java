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

import net.imglib2.RealPoint;
import net.imglib2.concatenate.Concatenable;
import net.imglib2.concatenate.PreConcatenable;

/**
 * <em>n</em>-d arbitrary scaling.
 * 
 * @author Stephan Saalfeld
 * @author Christian Dietz
 */
public class Scale extends AbstractScale implements Concatenable< ScaleGet >, PreConcatenable< ScaleGet >
{
	final protected Scale inverse;

	protected Scale( final double[] s, final Scale inverse, final RealPoint[] ds )
	{
		super( s, ds );
		this.inverse = inverse;
	}

	public Scale( final double... s )
	{
		super( s.clone(), new RealPoint[ s.length ] );
		final double[] si = new double[ s.length ];
		final RealPoint[] dis = new RealPoint[ s.length ];
		for ( int d = 0; d < s.length; ++d )
		{
			si[ d ] = 1.0 / s[ d ];
			final RealPoint dd = new RealPoint( s.length );
			dd.setPosition( s[ d ], d );
			ds[ d ] = dd;
			final RealPoint ddi = new RealPoint( s.length );
			ddi.setPosition( si[ d ], d );
			dis[ d ] = ddi;
		}
		inverse = new Scale( si, this, dis );
	}

	@Override
	public void set( final double... s )
	{
		for ( int d = 0; d < s.length; ++d )
		{
			this.s[ d ] = s[ d ];
			inverse.s[ d ] = 1.0 / s[ d ];
			ds[ d ].setPosition( s[ d ], d );
			inverse.ds[ d ].setPosition( inverse.s[ d ], d );
		}
	}

	@Override
	public Scale inverse()
	{
		return inverse;
	}

	@Override
	public Scale copy()
	{
		return new Scale( s );
	}

	@Override
	public Scale preConcatenate( final ScaleGet a )
	{
		for ( int d = 0; d < numDimensions(); ++d )
			set( s[ d ] * a.getScale( d ) );
		
		return this;
	}

	@Override
	public Class< ScaleGet > getPreConcatenableClass()
	{
		return ScaleGet.class;
	}

	@Override
	public Scale concatenate( ScaleGet a )
	{
		return preConcatenate( a );
	}

	@Override
	public Class< ScaleGet > getConcatenableClass()
	{
		return ScaleGet.class;
	}
	
	@Override
	public boolean isIdentity()
	{
		for ( int i = 0; i < s.length; i++ )
		{
			if ( s[ i ] != 1.0 ) { return false; }
		}
		return true;
	}
}
