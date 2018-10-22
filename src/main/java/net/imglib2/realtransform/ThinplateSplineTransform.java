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

import org.ejml.data.DenseMatrix64F;
import org.ejml.ops.CommonOps;
import org.ejml.ops.NormOps;

import jitk.spline.ThinPlateR2LogRSplineKernelTransform;
import net.imglib2.RealLocalizable;
import net.imglib2.RealPoint;
import net.imglib2.RealPositionable;
import net.imglib2.realtransform.inverse.AbstractDifferentiableRealTransform;
import net.imglib2.realtransform.inverse.DifferentiableRealTransform;

/**
 * An <em>n</em>-dimensional thin plate spline transform backed by John
 * Bogovic's <a href="https://github.com/saalfeldlab/jitk-tps">jitk-tps</a>
 * library.
 *
 * @author Stephan Saalfeld
 * @author John Bogovic
 */
public class ThinplateSplineTransform extends AbstractDifferentiableRealTransform implements RealTransform
{
	final private ThinPlateR2LogRSplineKernelTransform tps;

	final private double[] a;

	final private double[] b;

	final private RealPoint rpa;

	double[] estimateXfm;

	/*
	 * The jacobian matrix
	 */
	private AffineTransform jacobian;

	/*
	 * derivative in direction of dir (the descent direction )
	 */
	private DenseMatrix64F directionalDeriv;

	/*
	 * The descent direction
	 */
	private DenseMatrix64F dir;

	/*
	 * computes dir^T directionalDeriv (where dir^T is often -directionalDeriv)
	 */
	private DenseMatrix64F descentDirectionMag;

	/*
	 * error vector ( errorV = target - estimateXfm )
	 */
	private DenseMatrix64F errorV;

	final static private ThinPlateR2LogRSplineKernelTransform init( final double[][] p, final double[][] q )
	{
		assert p.length == q.length;

		final ThinPlateR2LogRSplineKernelTransform tps = new ThinPlateR2LogRSplineKernelTransform( p.length, p, q );

		return tps;
	}

	public ThinplateSplineTransform( final ThinPlateR2LogRSplineKernelTransform tps )
	{
		this.tps = tps;
		a = new double[ tps.getNumDims() ];
		b = new double[ a.length ];
		rpa = RealPoint.wrap( a );
		estimateXfm = new double[ tps.getNumDims() ];
	}

	public ThinplateSplineTransform( final double[][] p, final double[][] q )
	{
		this( init( p, q ) );
	}

	@Override
	public void apply( final double[] source, final double[] target )
	{
		tps.apply( source, target );
	}

	@Override
	public void apply( final RealLocalizable source, final RealPositionable target )
	{
		rpa.setPosition( source );
		tps.apply( a, b );
		for ( int d = 0; d < a.length; ++d )
			target.setPosition( b[ d ], d );
	}

	@Override
	public ThinplateSplineTransform copy()
	{
		/* tps is stateless and constant and can therefore be reused */
		return new ThinplateSplineTransform( tps );
	}

	@Override
	public int numSourceDimensions()
	{
		return tps.getNumDims();
	}

	@Override
	public int numTargetDimensions()
	{
		return tps.getNumDims();
	}

	private void initializeInverse()
	{
		int ndims = tps.getNumDims();
		dir = new DenseMatrix64F( ndims, 1 );
		errorV = new DenseMatrix64F( ndims, 1 );
		directionalDeriv = new DenseMatrix64F( ndims, 1 );
		descentDirectionMag = new DenseMatrix64F( 1, 1 );
	}

	public AffineTransform jacobian( final double[] x )
	{
		double[][] jac = tps.jacobian( x );
		double[] jflat = new double[ x.length * ( x.length + 1 ) ];

		int k = 0;
		for( int i = 0; i< x.length; i++ )
			for( int j = 0; j< (x.length +1); j++ )
				if( j < x.length )
					jflat[k++] = jac[i][j];
				else
					k++;

		jacobian = new AffineTransform( x.length );
		jacobian.set( jflat );

		return jacobian;
	}

}
