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

import Jama.Matrix;
import net.imglib2.EuclideanSpace;
import net.imglib2.Localizable;
import net.imglib2.Point;
import net.imglib2.Positionable;
import net.imglib2.RealLocalizable;
import net.imglib2.RealPositionable;
import net.imglib2.concatenate.Concatenable;
import net.imglib2.concatenate.PreConcatenable;
import net.imglib2.img.array.ArrayImg;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.img.array.ArrayRandomAccess;
import net.imglib2.img.basictypeaccess.array.LongArray;
import net.imglib2.transform.Transform;
import net.imglib2.type.numeric.integer.LongType;
import net.imglib2.util.Pair;
import net.imglib2.util.ValuePair;

/**
 * An <em>n</em>-dimensional long precision affine transformation. This
 * transformation supports all transformations that can be expressed by long
 * integers such as:
 * <ul>
 * <li>discrete translation</li>
 * <li>90 degrees rotations</li>
 * <li>axis permutations</li>
 * <li>discrete subsampling</li>
 * <li>discrete shearing</li>
 * </ul>
 *
 * The inverse of this transform is typically not a {@link LongAffineTransform}
 * but an {@link AffineTransform}.
 *
 * @author Stephan Saalfeld
 */
public class LongAffineTransform implements EuclideanSpace, Transform, RealTransform, Concatenable< LongAffineTransform >, PreConcatenable< LongAffineTransform >
{
	final protected int n;

	protected final long[] atArray;

	protected final ArrayImg< LongType, LongArray > at;

	protected final ArrayRandomAccess< LongType > atAccess;

	protected final long[] tmpLong;

	protected final double[] tmp;

	protected final Point[] ds;

	protected void identity()
	{

		Arrays.fill( atArray, 0 );
		Arrays.fill( tmpLong, 0 );
		for ( int d = 0; d < n; ++d )
		{
			atAccess.setPosition( d, 0 );
			atAccess.setPosition( d, 1 );
			atAccess.get().set( 1 );
		}
		atAccess.setPosition( n, 0 );
		for ( int d = 0; d < n; ++d )
		{
			atAccess.setPosition( d, 1 );
			atAccess.get().set( 0 );
			ds[ d ].setPosition( tmpLong );
			ds[ d ].setPosition( 1, d );
		}
	}

	protected void updateD( final int d )
	{

		atAccess.setPosition( d, 0 );
		for ( int l = 0; l < n; ++l )
		{
			atAccess.setPosition( l, 1 );
			ds[ d ].setPosition( atAccess.get().get(), l );
		}
	}

	protected void updateDs()
	{

		for ( int d = 0; d < n; ++d )
		{
			updateD( d );
		}
	}

	public LongAffineTransform( final int n )
	{
		atArray = new long[ ( n + 1 ) * n ];
		this.n = n;
		at = ArrayImgs.longs( atArray, n + 1, n );
		atAccess = at.randomAccess();
		tmp = new double[ n ];
		tmpLong = new long[ n ];
		ds = new Point[ n ];
		for ( int d = 0; d < n; ++d )
			ds[ d ] = new Point( n );
		identity();
	}

	public LongAffineTransform( final long... atArray )
	{
		this.atArray = atArray;
		n = ( int ) Math.sqrt( atArray.length );
		at = ArrayImgs.longs( atArray, n + 1, n );
		atAccess = at.randomAccess();
		tmp = new double[ n ];
		tmpLong = new long[ n ];
		ds = new Point[ n ];
		for ( int d = 0; d < n; ++d )
			ds[ d ] = new Point( n );
		updateDs();
	}

	public LongAffineTransform( final LongAffineTransform template )
	{
		this.atArray = template.atArray.clone();
		n = template.n;
		at = ArrayImgs.longs( atArray, n + 1, n );
		atAccess = at.randomAccess();
		tmp = template.tmp.clone();
		tmpLong = template.tmpLong.clone();
		ds = new Point[ n ];
		for ( int d = 0; d < n; ++d )
		{
			ds[ d ] = new Point( n );
			ds[ d ].setPosition( template.ds[ d ] );
		}
	}

	public AffineTransform toAffineTransform()
	{
		final double[] doubleParameters = new double[ atArray.length ];
		for ( int i = 0; i < atArray.length; ++i )
			doubleParameters[ i ] = atArray[ i ];

		return new AffineTransform( doubleParameters );
	}

	private static void column(
			final Matrix matrix,
			final int c,
			final double[] a )
	{

		for ( int r = 0; r < matrix.getRowDimension(); ++r )
			a[ r ] = matrix.get( r, c );
	}

	private static boolean zero(
			final double[] a )
	{

		for ( int r = 0; r < a.length; ++r )
			if ( a[ r ] != 0 )
				return false;

		return true;
	}

	private static boolean zeroExcept(
			final Matrix a,
			final int c,
			final int except )
	{
		for ( int r = 0; r < a.getRowDimension(); ++r )
			if ( !( r == except || a.get( r, c ) == 0 ) )
				return false;

		return true;
	}

	private static double ratio( final double a, final double b )
	{
		if ( b == 0 )
			return a == 0 ? Double.NaN : Double.POSITIVE_INFINITY;
		else
			return a / b;
	}

	public static boolean collinear(
			final double[] a,
			final double[] b )
	{
		final int n = a.length;

		double ratio = ratio( a[ 0 ], b[ 0 ] );

		for ( int r = 1; r < n; ++r )
		{
			if ( Double.isNaN( ratio ) )
				ratio = ratio( a[ r ], b[ r ] );
			else if ( ratio != ratio( a[ r ], b[ r ] ) )
				return false;
		}

		return true;
	}

	private static boolean collinear(
			final Matrix matrix,
			final double[] a,
			final double[] b,
			final int ca,
			final int cb )
	{
		column( matrix, ca, a );
		column( matrix, cb, b );

		return collinear( a, b );
	}

	private static int needWork(
			final Matrix affineMatrix,
			final Matrix roundMatrix,
			final double[] a,
			final double[] b,
			final boolean[] needWork )
	{
		final int n = needWork.length;

		for ( int c = 0; c < n; ++c )
		{
			if ( needWork[ c ] )
			{
				column( roundMatrix, c, a );
				if ( zero( a ) )
				{
					column( affineMatrix, c, b );
					if ( zero( b ) )
						needWork[ c ] = false;
				}
			}
		}

		for ( int c1 = 0; c1 < n; ++c1 )
		{
			if ( needWork[ c1 ] )
			{
//				needWork[ c1 ] = false;
				column( roundMatrix, c1, a );
				for ( int c2 = c1 + 1; c2 < n; ++c2 )
				{
					if ( needWork[ c2 ] )
					{
						column( roundMatrix, c2, b );
						if ( collinear( a, b ) )
						{
//							needWork[ c1 ] = true;
//							needWork[ c2 ] = true;
							return c1;
						}
					}
				}
			}
		}

		return -1;

//		for ( int r = 0; r < n; ++r )
//		{
//			if ( needWork[ r ] )
//				return true;
//		}
//
//		return false;
	}

	private static void move(
			final Matrix affineMatrix,
			final Matrix roundMatrix,
			final boolean[][] hasMoved,
			final int c )
	{
		final int n = affineMatrix.getRowDimension();

		double minDistance = Double.MAX_VALUE;
		int minR = 0;
		double value = 0;

		for ( int r = 0; r < n; ++r )
		{
			if ( !hasMoved[ c ][ r ] )
			{
				final double real = affineMatrix.get( r, c );
				final double round = roundMatrix.get( r, c );

				final double distance = Math.abs( real - round );
				if ( distance < minDistance )
				{
					final double test = real < round ? round - 1 : round + 1;
					if ( test == 0 && zeroExcept( roundMatrix, c, r ) )
					{
						minDistance = distance;
						minR = r;
						value = test;
					}
				}
			}
		}

		roundMatrix.set( minR, c, value );
	}

	public static void spread(
			final Matrix affineMatrix,
			final Matrix roundMatrix )
	{
		final int n = affineMatrix.getColumnDimension();
		final double[] a = new double[ n ];
		final double[] b = new double[ n ];
		final boolean[] needWork = new boolean[ n ];
		Arrays.fill( needWork, true );
		int moveThis = needWork( affineMatrix, roundMatrix, a, b, needWork );
		if ( moveThis >= 0 )
		{
			final boolean[][] hasMoved = new boolean[ n ][ n ];

			do
			{
				move( affineMatrix, roundMatrix, hasMoved, moveThis );
				moveThis = needWork( affineMatrix, roundMatrix, a, b, needWork );
				System.out.println( moveThis );
			}
			while ( moveThis >= 0 );
		}
	}

	/**
	 * A = A_{rest} * A_{round}
	 *
	 * A * A_{round}^{-1} = A_{rest}
	 *
	 *
	 * @param affine
	 * @return
	 */
	public static Pair< LongAffineTransform, AffineTransform > decomposeRealLong(
			final AffineGet affine )
	{
		final int n = affine.numDimensions();

		final Matrix affineMatrix = new Matrix( n, n );

		final Matrix roundMatrix = new Matrix( n, n );

		for ( int r = 0, i = 0; r < n; ++r )
		{
			for ( int c = 0; c < n; ++c, ++i )
			{
				final double value = affine.get( r, c );
				affineMatrix.set( r, c, value );
				roundMatrix.set( r, c, Math.round( value ) );
			}
		}

		spread( affineMatrix, roundMatrix );

		final long[] atLong = new long[ n * n + n ];
		final double[] atRound = new double[ atLong.length ];
		for ( int r = 0, i = 0; r < n; ++r, ++i )
		{
			for ( int c = 0; c < n; ++c, ++i )
			{
				atRound[ i ] = atLong[ i ] = ( long ) roundMatrix.get( r, c );
			}
			atRound[ i ] = atLong[ i ] = Math.round( affine.get( r, n ) );
		}

		final LongAffineTransform longAffine = new LongAffineTransform( atLong );
		final AffineTransform rest = new AffineTransform( atRound ).inverse();
		rest.preConcatenate( affine );

		return new ValuePair<>(
				longAffine,
				rest );
	}

	/**
	 * A = A_{round} * A_{rest}
	 *
	 * A_{round}^{-1} * A = A_{rest}
	 *
	 *
	 * @param affine
	 * @return
	 */
	public static Pair< AffineTransform, LongAffineTransform > decomposeLongReal(
			final AffineGet affine )
	{
		final int n = affine.numDimensions();

		final Matrix affineMatrix = new Matrix( n, n );

		final Matrix roundMatrix = new Matrix( n, n );

		for ( int r = 0, i = 0; r < n; ++r )
		{
			for ( int c = 0; c < n; ++c, ++i )
			{
				final double value = affine.get( r, c );
				affineMatrix.set( r, c, value );
				roundMatrix.set( r, c, Math.round( value ) );
			}
		}

		spread( affineMatrix, roundMatrix );

		final long[] atLong = new long[ n * n + n ];
		final double[] atRound = new double[ atLong.length ];
		for ( int r = 0, i = 0; r < n; ++r, ++i )
		{
			for ( int c = 0; c < n; ++c, ++i )
			{
				atRound[ i ] = atLong[ i ] = ( long ) roundMatrix.get( r, c );
			}
			atRound[ i ] = atLong[ i ] = Math.round( affine.get( r, n ) );
		}

		final LongAffineTransform longAffine = new LongAffineTransform( atLong );
		final AffineTransform rest = new AffineTransform( atRound ).inverse();
		rest.concatenate( affine );

		return new ValuePair<>(
				rest,
				longAffine );
	}

	public void set( final LongAffineTransform template )
	{
		assert n == template.numDimensions(): "Dimensions do not match.";

		System.arraycopy( template.atArray.clone(), 0, atArray, 0, n );
		System.arraycopy( template.tmp, 0, tmp, 0, n );
		System.arraycopy( template.tmpLong, 0, tmpLong, 0, n );
		for ( int i = 0; i < n; ++i )
		{
			ds[ i ].setPosition( template.ds[ i ] );
		}
	}

	@Override
	public LongAffineTransform concatenate( final LongAffineTransform other )
	{
		assert other.numDimensions() == n: "Dimensions do not match.";

		final ArrayRandomAccess< LongType > atAccessOther = other.atAccess;

		final long[] result = new long[ atArray.length ];

		for ( int i = 0, r = 0; r < n; ++r, ++i )
		{
			atAccess.setPosition( r, 1 );
			for ( int c = 0; c < n; ++c, ++i )
			{
				atAccessOther.setPosition( c, 0 );
				long value = 0;
				for ( int l = 0; l < n; ++l )
				{
					atAccess.setPosition( l, 0 );
					atAccessOther.setPosition( l, 1 );
					value += atAccess.get().get() * atAccessOther.get().get();
				}
				result[ i ] = value;
			}
			atAccess.setPosition( n, 0 );
			atAccessOther.setPosition( n, 0 );
			long value = atAccess.get().get();
			for ( int l = 0; l < n; ++l )
			{
				atAccess.setPosition( l, 0 );
				atAccessOther.setPosition( l, 1 );
				value += atAccess.get().get() * atAccessOther.get().get();
			}
			result[ i ] = value;
		}

		System.arraycopy( result, 0, atArray, 0, atArray.length );

		updateDs();

		return this;
	}

	@Override
	public Class< LongAffineTransform > getConcatenableClass()
	{
		return LongAffineTransform.class;
	}

	@Override
	public LongAffineTransform preConcatenate( final LongAffineTransform other )
	{
		assert other.numDimensions() == n: "Dimensions do not match.";

		final ArrayRandomAccess< LongType > atAccessOther = other.atAccess;

		final long[] result = new long[ atArray.length ];

		for ( int i = 0, r = 0; r < n; ++r, ++i )
		{
			atAccessOther.setPosition( r, 1 );
			for ( int c = 0; c < n; ++c, ++i )
			{
				atAccess.setPosition( c, 0 );
				long value = 0;
				for ( int l = 0; l < n; ++l )
				{
					atAccessOther.setPosition( l, 0 );
					atAccess.setPosition( l, 1 );
					value += atAccessOther.get().get() * atAccess.get().get();
				}
				result[ i ] = value;
			}
			atAccessOther.setPosition( n, 0 );
			atAccess.setPosition( n, 0 );
			long value = atAccessOther.get().get();
			for ( int l = 0; l < n; ++l )
			{
				atAccessOther.setPosition( l, 0 );
				atAccess.setPosition( l, 1 );
				value += atAccessOther.get().get() * atAccess.get().get();
			}
			result[ i ] = value;
		}

		System.arraycopy( result, 0, atArray, 0, atArray.length );

		updateDs();

		return this;
	}

	@Override
	public Class< LongAffineTransform > getPreConcatenableClass()
	{
		return LongAffineTransform.class;
	}

	public void set( final long value, final int row, final int column )
	{
		atAccess.setPosition( column, 0 );
		atAccess.setPosition( row, 1 );
		atAccess.get().set( value );

		if ( column < n )
			updateD( column );
	}

	public void set( final long... values )
	{
		System.arraycopy( values, 0, atArray, 0, Math.min( atArray.length, values.length ) );

		updateDs();
	}

	@Override
	public LongAffineTransform copy()
	{
		return new LongAffineTransform( this );
	}

	@Override
	public boolean isIdentity()
	{
		for ( int r = 0; r < n; ++r )
		{
			atAccess.setPosition( r, 1 );
			for ( int c = 0; c <= n; ++c )
			{
				atAccess.setPosition( c, 0 );
				final long value = atAccess.get().get();
				if ( ( r == c && value != 1 ) || value != 0 )
					return false;
			}
		}
		return true;
	}

	@Override
	public void apply( final double[] source, final double[] target )
	{
		for ( int r = 0; r < n; ++r )
		{
			atAccess.setPosition( r, 1 );
			double value = 0;
			for ( int c = 0; c < n; ++c )
			{
				atAccess.setPosition( c, 0 );
				value += atAccess.get().get() * source[ c ];
			}
			atAccess.setPosition( n, 0 );
			value += atAccess.get().get();
			tmp[ r ] = value;
		}

		System.arraycopy( tmp, 0, target, 0, tmp.length );
	}

	@Override
	public void apply( final RealLocalizable source, final RealPositionable target )
	{
		for ( int r = 0; r < n; ++r )
		{
			atAccess.setPosition( r, 1 );
			long value = 0;
			for ( int c = 0; c < n; ++c )
			{
				atAccess.setPosition( c, 0 );
				value += atAccess.get().get() * source.getDoublePosition( c );
			}
			atAccess.setPosition( n, 0 );
			value += atAccess.get().get();
			tmp[ r ] = value;
		}

		target.setPosition( tmp );
	}

	@Override
	public int numSourceDimensions()
	{
		return numDimensions();
	}

	@Override
	public int numTargetDimensions()
	{
		return numDimensions();
	}

	@Override
	public void apply( final long[] source, final long[] target )
	{
		for ( int r = 0; r < n; ++r )
		{
			atAccess.setPosition( r, 1 );
			long value = 0;
			for ( int c = 0; c < n; ++c )
			{
				atAccess.setPosition( c, 0 );
				value += atAccess.get().get() * source[ c ];
			}
			atAccess.setPosition( n, 0 );
			value += atAccess.get().get();
			tmpLong[ r ] = value;
		}

		System.arraycopy( tmpLong, 0, target, 0, tmpLong.length );
	}

	@Override
	public void apply( final int[] source, final int[] target )
	{
		for ( int r = 0; r < n; ++r )
		{
			atAccess.setPosition( r, 1 );
			long value = 0;
			for ( int c = 0; c < n; ++c )
			{
				atAccess.setPosition( c, 0 );
				value += atAccess.get().get() * source[ c ];
			}
			atAccess.setPosition( n, 0 );
			value += atAccess.get().get();
			tmpLong[ r ] = value;
		}

		for ( int d = 0; d < tmpLong.length; ++d )
			target[ d ] = ( int ) tmpLong[ d ];
	}

	@Override
	public void apply( final Localizable source, final Positionable target )
	{
		for ( int r = 0; r < n; ++r )
		{
			atAccess.setPosition( r, 1 );
			long value = 0;
			for ( int c = 0; c < n; ++c )
			{
				atAccess.setPosition( c, 0 );
				value += atAccess.get().get() * source.getLongPosition( c );
			}
			atAccess.setPosition( n, 0 );
			value += atAccess.get().get();
			tmpLong[ r ] = value;
		}

		target.setPosition( tmpLong );
	}

	@Override
	public int numDimensions()
	{
		return n;
	}
}
