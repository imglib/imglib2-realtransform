/*
 * #%L
 * ImgLib2: a general-purpose, multidimensional image processing library.
 * %%
 * Copyright (C) 2009 - 2016 Tobias Pietzsch, Stephan Preibisch, Barry DeZonia,
 * Stephan Saalfeld, Curtis Rueden, Albert Cardona, Christian Dietz, Jean-Yves
 * Tinevez, Johannes Schindelin, Jonathan Hale, Lee Kamentsky, Larry Lindsey, Mark
 * Hiner, Michael Zinsmaier, Martin Horn, Grant Harris, Aivar Grislis, John
 * Bogovic, Steffen Jaensch, Stefan Helfrich, Jan Funke, Nick Perry, Mark Longair,
 * Melissa Linkert and Dimiter Prodanov.
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

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import net.imglib2.RandomAccessible;
import net.imglib2.RealRandomAccessible;
import net.imglib2.concatenate.ConcatenateUtils;
import net.imglib2.util.Pair;
import net.imglib2.util.ValuePair;
import net.imglib2.view.RandomAccessibleOnRealRandomAccessible;

/**
 * @author Christian Dietz, University of Konstanz
 */
public class RealViewsSimplifyUtils
{
	/**
	 * @param affineGet {@link AffineGet} to be checked
	 * @return true, if the given {@link AffineGet} only scales and translates.
	 */
	public static boolean isExclusiveScaleAndTranslation( final AffineGet affineGet )
	{
		if ( affineGet instanceof ScaleAndTranslationGet ) { return true; }

		final int n = affineGet.numDimensions();

		for ( int r = 0; r < n; ++r )
		{
			for ( int c = 0; c < n + 1; ++c )
			{
				if ( affineGet.get( r, c ) != 0.0 && r != c && c != n ) { return false; }
			}
		}

		return true;
	}

	/**
	 * @param affineGet {@link AffineGet} to be checked
	 * @return true, if the given {@link AffineGet} affineGet only scales.
	 */
	public static boolean isExclusiveScale( final AffineGet affineGet )
	{
		if ( affineGet instanceof ScaleGet ) { return true; }

		final int n = affineGet.numDimensions();

		for ( int r = 0; r < n; ++r )
		{
			for ( int c = 0; c < n + 1; ++c )
			{
				if ( affineGet.get( r, c ) != 0.0 && r != c ) { return false; }
			}
		}

		return true;
	}

	/**
	 * @param affineGet {@link AffineGet} to be checked
	 * @return true, if the given {@link AffineGet} only translates.
	 */
	public static boolean isExlusiveTranslation( final AffineGet affineGet )
	{
		if ( affineGet instanceof TranslationGet ) { return true; }

		final int n = affineGet.numDimensions();

		for ( int r = 0; r < n; ++r )
		{
			for ( int c = 0; c < n + 1; ++c )
			{
				final double val = affineGet.get( r, c );
				if ( val != 0.0 && ( ( r == c && val != 1.0 ) || ( c != n && val != 1.0 ) ) ) { return false; }
			}
		}

		return true;
	}

	/**
	 * @param affineGet
	 *            to be checked
	 * @return true, if the given {@link AffineGet} is identity
	 */
	public static boolean isIdentity( final AffineGet affineGet )
	{
		final int n = affineGet.numDimensions();

		for ( int r = 0; r < n; ++r )
		{
			for ( int c = 0; c < n + 1; ++c )
			{
				final double val = affineGet.get( r, c );
				if ( ( r == c && val != 1.0 ) || ( r != c && val != 0.0 ) )
					return false;
			}
		}

		return true;
	}

	/**
	 * See {@link RealViews}{@link #simplifyReal(RealRandomAccessible)}
	 * 
	 * @param source
	 *            to be simplified.
	 * @param <T> the type
	 * @return a (potentially) simplified version of the source.
	 * 
	 */
	public static < T > RealRandomAccessible< T > simplifyReal( final RealRandomAccessible< T > source )
	{
		final Pair< RealRandomAccessible< T >, RealTransform > tmp = findSourceAndSimplifyTransforms( source );

		if ( tmp.getB() == null ) { return tmp.getA(); }

		return createRealRandomAccessible( tmp.getA(), tmp.getB() );
	}

	/**
	 * See {@link RealViews}{@link #simplify(RealRandomAccessible)}
	 * 
	 * @param source
	 *            to be simplified.
	 * @param <T> the type
	 * @return a (potentially) simplified version of the source.
	 * 
	 */
	@SuppressWarnings( "unchecked" )
	public static < T > RandomAccessible< T > simplify( final RealRandomAccessible< T > source )
	{
		final Pair< RealRandomAccessible< T >, RealTransform > tmp = findSourceAndSimplifyTransforms( source );

		if ( tmp.getB() == null )
		{
			if ( tmp.getA() instanceof RandomAccessible )
			{
				return ( RandomAccessible< T > ) tmp.getA();
			}
			return new RandomAccessibleOnRealRandomAccessible<>( source );
		}

		return createRandomAccessible( tmp.getA(), tmp.getB() );
	}

	protected static RealTransform simplifyRealTransform( final RealTransform transform )
	{
		final RealTransform tmp;
		if ( transform instanceof InverseRealTransform )
		{
			tmp = ( ( InverseRealTransform ) transform ).inverse().inverse();
		}
		else
		{
			tmp = transform;
		}

		if ( transform instanceof AffineGet ) { return simplifyAffineGet( ( AffineGet ) tmp ); }
		return tmp;
	}

	/*
	 * Simplifies an AffineGet to it's most specific class (e.g. Translation2D)
	 */
	private static AffineGet simplifyAffineGet( final AffineGet affineGet )
	{
		final int n = affineGet.numDimensions();

		if ( isExlusiveTranslation( affineGet ) )
		{
			final double[] translations = new double[ n ];

			for ( int d = 0; d < n; d++ )
			{
				translations[ d ] = affineGet.get( d, n );
			}

			if ( n == 2 )
			{
				return new Translation2D( translations );
			}
			else if ( n == 3 )
			{
				return new Translation3D( translations );
			}
			else
			{
				return new Translation( translations );
			}
		}
		else if ( isExclusiveScale( affineGet ) )
		{

			final double[] scalings = new double[ n ];

			for ( int d = 0; d < n; d++ )
			{
				scalings[ d ] = affineGet.get( d, d );
			}

			if ( n == 2 )
			{
				return new Scale2D( scalings );
			}
			else if ( n == 3 )
			{
				return new Scale3D( scalings );
			}
			else
			{
				return new Scale( scalings );
			}
		}
		else if ( isExclusiveScaleAndTranslation( affineGet ) )
		{
			final double[] s = new double[ n ];
			final double[] t = new double[ n ];
			for ( int d = 0; d < n; d++ )
			{
				t[ d ] = affineGet.get( d, n );
				s[ d ] = affineGet.get( d, d );
			}

			return new ScaleAndTranslation( t, s );

		}
		return ( AffineGet ) affineGet.copy();
	}

	private static < T > RandomAccessible< T > createRandomAccessible( final RealRandomAccessible< T > rra, final RealTransform t )
	{
		if ( t instanceof AffineGet )
		{
			return new AffineRandomAccessible<>( rra, ( AffineGet ) t );
		}
		return new RealTransformRandomAccessible<>( rra, t );
	}

	private static < T > RealRandomAccessible< T > createRealRandomAccessible( final RealRandomAccessible< T > rra, final RealTransform t )
	{
		if ( !t.isIdentity() )
		{
			if ( t instanceof AffineGet )
			{
				return new AffineRandomAccessible<>( rra, ( AffineGet ) t );
			}
			return new RealTransformRandomAccessible<>( rra, t );
		}

		return rra;
	}

	@SuppressWarnings( "unchecked" )
	private static < T > Pair< RealRandomAccessible< T >, RealTransform > findSourceAndSimplifyTransforms( final RealRandomAccessible< T > source )
	{
		final List< RealTransform > transforms = new LinkedList<>();

		RealRandomAccessible< T > tmp = source;
		if ( tmp instanceof RealTransformRealRandomAccessible )
		{
			transforms.add( ( ( RealTransformRealRandomAccessible< T, ? > ) tmp ).getTransformToSource() );
			tmp = ( ( RealTransformRealRandomAccessible< T, ? > ) tmp ).getSource();

			while ( true )
			{
				if ( tmp instanceof RealTransformRealRandomAccessible )
				{
					transforms.add( ( ( RealTransformRealRandomAccessible< T, ? > ) tmp ).getTransformToSource().copy() );
					tmp = ( ( RealTransformRealRandomAccessible< T, ? > ) tmp ).getSource();
				}
				else
				{
					break;
				}
			}

			simplifyRealTransforms( transforms );

			for ( int i = 0; i < transforms.size() - 1; i++ )
			{
				tmp = createRealRandomAccessible( tmp, transforms.get( i ) );
			}
			if ( transforms.size() > 0 )
				return new ValuePair<>( tmp, transforms.get( transforms.size() - 1 ) );
		}
		return new ValuePair<>( tmp, null );
	}

	private static void simplifyRealTransforms( final List< RealTransform > transforms )
	{
		int oldSize = transforms.size() + 1;

		Collections.reverse( transforms );
		while ( transforms.size() < oldSize && transforms.size() > 0 )
		{
			oldSize = transforms.size();
			final Iterator< RealTransform > it = transforms.iterator();

			int i = 0;
			while ( it.hasNext() )
			{
				final RealTransform simplified = simplifyRealTransform( it.next() );

				if ( !simplified.isIdentity() )
				{
					transforms.set( i, simplified );
				}
				else
				{
					it.remove();
					--i;
				}

				i++;
			}

			ConcatenateUtils.join( transforms );
		}
	}

}
