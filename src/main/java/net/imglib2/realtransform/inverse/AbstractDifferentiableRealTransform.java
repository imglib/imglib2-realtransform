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

import org.ejml.data.DMatrix;
import org.ejml.data.DMatrixRMaj;
import org.ejml.dense.row.NormOps_DDRM;
import org.ejml.dense.row.mult.MatrixVectorMult_DDRM;

import net.imglib2.realtransform.AffineTransform;

public abstract class AbstractDifferentiableRealTransform implements DifferentiableRealTransform
{
	protected final int n;

	protected final DMatrixRMaj err;

	protected final DMatrixRMaj dir;

	public AbstractDifferentiableRealTransform( int n )
	{
		this.n = n;
		err = new DMatrixRMaj(n, 1);
		dir = new DMatrixRMaj(n, 1);
	}

	/**
	 * Returns the jacobian matrix of this transform at the point x.
	 * 
	 * @param x
	 *            the point
	 * @return the jacobian
	 */
	@Deprecated
	public AffineTransform jacobian( double[] x )
	{
		 return toAffine(jacobianMatrix(x));
	}

	private AffineTransform toAffine( final DMatrix mtx )
	{
		final AffineTransform out = new AffineTransform(numSourceDimensions());

		final DMatrixRMaj mtxRMaj;
		if( mtx instanceof DMatrixRMaj )
		{
			mtxRMaj = ((DMatrixRMaj)mtx);
		}
		else {
			mtxRMaj = new DMatrixRMaj(mtx);
		}

		out.set(mtxRMaj.data);
		return out;
	}

	public abstract DMatrixRMaj jacobianMatrix( final double[] x );

	/**
	 * Writes the direction <em>displacement</em> in which to move the input
	 * source point <em>x</em> in order that F( x + d ) is closer to the
	 * destination point <em>y</em> than F( x ).
	 * <p>
	 * The output is a normalized vector.
	 * 
	 * @param displacement
	 *            the displacement to write into
	 * @param x
	 *            the source point
	 * @param y
	 *            the destination point
	 */
	public void directionToward( final double[] displacement, final double[] x, final double[] y )
	{
		directionToward( jacobianMatrix( x ), displacement, x, y );
	}

	@Deprecated
	public static void directionToward( final AffineTransform jacobian, final double[] displacement, final double[] x, final double[] y )
	{
		double[] err = new double[ x.length ];
		for ( int i = 0; i < x.length; i++ )
			err[ i ] = y[ i ] - x[ i ];

		double[] dir = new double[ x.length ];
		matrixTranspose( jacobian ).apply( err, dir );

		double norm = 0.0;
		for ( int i = 0; i < dir.length; i++ )
			norm += ( dir[ i ] * dir[ i ] );

		norm = Math.sqrt( norm );
		for ( int i = 0; i < dir.length; i++ )
			dir[ i ] /= norm;

		System.arraycopy( dir, 0, displacement, 0, dir.length );
	}

	public void directionToward( final DMatrixRMaj jacobian, final double[] displacement, final double[] x, final double[] y )
	{
		for ( int i = 0; i < x.length; i++ )
			err.set(i, y[ i ] - x[ i ]);

		MatrixVectorMult_DDRM.multTransA_small(jacobian, err, dir);
		NormOps_DDRM.normalizeF(dir);
		System.arraycopy(dir.data, 0, displacement, 0, n);
	}

	@Deprecated
	public static AffineTransform matrixTranspose( final AffineTransform a )
	{
		int nd = a.numDimensions();
		final AffineTransform aT = new AffineTransform( nd );
		double[][] mtx = new double[ nd ][ nd + 1 ];
		for ( int i = 0; i < nd; i++ )
			for ( int j = 0; j < nd; j++ )
				mtx[ j ][ i ] = a.get( i, j );

		aT.set( mtx );
		return aT;
	}

}
