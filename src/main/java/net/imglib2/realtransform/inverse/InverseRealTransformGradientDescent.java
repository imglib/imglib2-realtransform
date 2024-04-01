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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

public class InverseRealTransformGradientDescent implements RealTransform
{
	int ndims;

	AffineTransform jacobian;

	double[] directionalDeriv; // derivative in direction of dir (the
								// descent direction )

	double descentDirectionMag; // computes dir^T directionalDeriv
								// (where dir^T is often
								// -directionalDeriv)

	double[] dir; // descent direction

	double[] errorV; // error vector ( errorV = target - estimateXfm )

	double[] estimate; // current estimate

	double[] estimateXfm; // current estimateXfm

	double[] target;

	boolean fixZ = false;

	double error = 9999.0;

	double stepSz = 1.0;

	double beta = 0.7;

	double tolerance = 0.5;

	double c = 0.0001;

	int maxIters = 100;

	double jacobianEstimateStep = 1.0;

	double jacobianRegularizationEps = 0.1;

	int stepSizeMaxTries = 10;

	double maxStepSize = Double.MAX_VALUE;

	double minStepSize = 1e-9;

	private DifferentiableRealTransform xfm;

	private double[] guess; // initialization for iterative inverse

	protected static Logger logger = LoggerFactory.getLogger( InverseRealTransformGradientDescent.class );

	public InverseRealTransformGradientDescent( int ndims, DifferentiableRealTransform xfm )
	{
		this.ndims = ndims;
		this.xfm = xfm;
		dir = new double[ ndims ];
		errorV = new double[ ndims ];
		directionalDeriv = new double[ ndims ];
		descentDirectionMag = 0.0;
		jacobian = new AffineTransform( ndims );

		target = new double[ ndims ];
		estimate = new double[ ndims ];
		estimateXfm = new double[ ndims ];
	}

	public void setBeta( final double beta )
	{
		this.beta = beta;
	}

	public void setC( final double c )
	{
		this.c = c;
	}

	public void setTolerance( final double tol )
	{
		this.tolerance = tol;
	}

	public void setMaxIters( final int maxIters )
	{
		this.maxIters = maxIters;
	}

	public void setFixZ( final boolean fixZ )
	{
		this.fixZ = fixZ;
	}

	public void setStepSize( final double stepSize )
	{
		stepSz = stepSize;
	}

	public void setMinStep( final double minStep )
	{
		this.minStepSize = minStep;
	}

	public void setMaxStep( final double maxStep )
	{
		this.maxStepSize = maxStep;
	}

	public void setJacobianEstimateStep( final double jacStep )
	{
		this.jacobianEstimateStep = jacStep;
	}

	public void setJacobianRegularizationEps( final double e )
	{
		this.jacobianRegularizationEps = e;
	}

	public void setStepSizeMaxTries( final int stepSizeMaxTries )
	{
		this.stepSizeMaxTries = stepSizeMaxTries;
	}

	public void setTarget( final double[] tgt )
	{
		System.arraycopy( tgt, 0, target, 0, ndims );
	}

	public double[] getErrorVector()
	{
		return errorV;
	}

	public double[] getDirection()
	{
		return dir;
	}

	public void setEstimate( final double[] est )
	{
		System.arraycopy( est, 0, estimate, 0, ndims );
	}

	public void setEstimateXfm( final double[] est )
	{
		System.arraycopy( est, 0, estimateXfm, 0, ndims );
	}

	public double[] getEstimate()
	{
		return estimate;
	}

	public double getError()
	{
		return error;
	}

	public int numSourceDimensions()
	{
		return ndims;
	}

	@Override
	public int numTargetDimensions()
	{
		return ndims;
	}

	@Override
	public RealTransform copy()
	{
		InverseRealTransformGradientDescent copy = new InverseRealTransformGradientDescent( ndims, xfm.copy() );
		copy.setBeta( this.beta );
		copy.setC( this.c );
		copy.setTolerance( this.tolerance );
		copy.setMaxIters( this.maxIters );
		return copy;
	}

	public void setGuess( final double[] guess )
	{
		this.guess = guess;
	}

	public void apply( final double[] s, final double[] t )
	{
		// invTol sets the error
		inverseTol( s, s, tolerance, maxIters );

		// copy estimate into t
		System.arraycopy( estimate, 0, t, 0, t.length );
	}

	@Deprecated
	public void apply( final float[] src, final float[] tgt )
	{
		double[] srcd = new double[ src.length ];
		double[] tgtd = new double[ tgt.length ];
		for ( int i = 0; i < src.length; i++ )
			srcd[ i ] = src[ i ];

		apply( srcd, tgtd );

		for ( int i = 0; i < tgt.length; i++ )
			tgt[ i ] = ( float ) tgtd[ i ];
	}

	public void apply( final RealLocalizable src, final RealPositionable tgt )
	{
		double[] srcd = new double[ src.numDimensions() ];
		double[] tgtd = new double[ tgt.numDimensions() ];
		src.localize( srcd );
		apply( srcd, tgtd );
		tgt.setPosition( tgtd );
	}

	public double inverseTol( final double[] target, final double[] guess, final double tolerance, final int maxIters )
	{
		// TODO - have a flag in the apply method to also return the derivative
		// if requested
		// to prevent duplicated effort

		// TODO should this be
		this.target = target;

		/*
		 * initialize the error to a big enough number This shouldn't matter
		 * since the error is updated below after the estimate updated.
		 */
		error = 999 * tolerance;

		setEstimate( guess );

		xfm.apply( estimate, estimateXfm );
		updateError();

		double t = 1.0;
		int k = 0;
		while ( error >= tolerance && k < maxIters )
		{

			/*
			 * xfm.jacobian( estimate );
			 * 
			 * if( jacobianRegularizationEps > 0 ) regularizeJacobian();
			 */

			// TODO the above lines may be important
			// if we want to regularize the jacobian
			xfm.directionToward( dir, estimateXfm, target );

			/* the two below lines should give identical results */
			// t = backtrackingLineSearch( c, beta, stepSizeMaxTries, t0 );
			t = backtrackingLineSearch( error );

			if ( t == 0.0 )
				break;

			updateEstimate( t ); // go in negative direction to reduce cost
			xfm.apply( estimate, estimateXfm );
			updateError();

			error = getError();

			k++;
		}

		return error;
	}

	/**
	 *  Changes jacobian (J) to be:
	 *  ( 1-eps ) * J + ( eps ) * I
	 *  note jacRegMatrix = eps * I
	 */
	public void regularizeJacobian()
	{
		for ( int i = 0; i < ndims; i++ )
		{
			jacobian.set( jacobianRegularizationEps + jacobian.get( i, i ), i, i );
		}
	}

	/**
	 * Uses Backtracking Line search to determine a step size.
	 * 
	 * @param t0
	 *            initial step size
	 * @return the step size
	 */
	public double backtrackingLineSearch( double t0 )
	{
		double t = t0; // step size

		int k = 0;
		// boolean success = false;
		while ( k < stepSizeMaxTries )
		{
			if ( armijoCondition( c, t ) )
			{
				// success = true;
				break;
			}
			else
				t *= beta;

			k++;
		}

		if ( t < minStepSize )
			return minStepSize;

		if ( t > maxStepSize )
			return maxStepSize;

		return t;
	}

	/**
	 * Uses Backtracking Line search to determine a step size.
	 * 
	 * @param c
	 *            the armijoCondition parameter
	 * @param beta
	 *            the fraction to multiply the step size at each iteration (
	 *            less than 1 )
	 * @param maxtries
	 *            max number of tries
	 * @param t0
	 *            initial step size
	 * @return the step size
	 */
	public double backtrackingLineSearch( double c, double beta, int maxtries, double t0 )
	{
		double t = t0; // step size

		int k = 0;
		// boolean success = false;
		while ( k < maxtries )
		{
			if ( armijoCondition( c, t ) )
			{
				// success = true;
				break;
			}
			else
				t *= beta;

			k++;
		}

		return t;
	}

	/**
	 * Returns true if the armijo condition is satisfied.
	 * 
	 * @param c
	 *            the c parameter
	 * @param t
	 *            the step size
	 * @return true if the step size satisfies the condition
	 */
	public boolean armijoCondition( final double c, final double t )
	{
		double[] d = dir;
		double[] x = estimate; // give a convenient name

		double[] x_ap = new double[ ndims ];
		for ( int i = 0; i < ndims; i++ )
			x_ap[ i ] = x[ i ] + t * d[ i ];

		// don't have to do this in here - this should be reused
		// double[] phix = xfm.apply( x );
		// TODO make sure estimateXfm is updated at the correct time
		double[] phix = estimateXfm;
		double[] phix_ap = new double[ this.ndims ];
		xfm.apply( x_ap, phix_ap );

		double fx = squaredError( phix );
		double fx_ap = squaredError( phix_ap );

		double m = sumSquaredErrorsDeriv( this.target, phix ) * descentDirectionMag;

		if ( fx_ap < fx + c * t * m )
			return true;
		else
			return false;
	}

	public double squaredError( final double[] x )
	{
		double error = 0;
		for ( int i = 0; i < ndims; i++ )
			error += ( x[ i ] - this.target[ i ] ) * ( x[ i ] - this.target[ i ] );

		return error;
	}

	public void updateEstimate( final double stepSize )
	{
		for ( int i = 0; i < ndims; i++ )
			estimate[ i ] += stepSize * dir[ i ];
	}

	public void updateError()
	{
		if ( estimate == null || target == null )
		{
			System.err.println( "WARNING: Call to updateError with null target or estimate" );
			return;
		}

		for ( int i = 0; i < ndims; i++ )
			errorV[ i ] = target[ i ] - estimateXfm[ i ];

		// set scalar error to magnitude of error gradient
		error = 0.0;
		for ( int i = 0; i < ndims; i++ )
		{
			error += errorV[ i ] * errorV[ i ];
		}

		error = Math.sqrt( error );
	}

	/**
	 * This function returns \nabla f ^T \nabla f where f = || y - x ||^2 and
	 * the gradient is taken with respect to x
	 * 
	 * @param y
	 * @param x
	 * @return
	 */
	private double sumSquaredErrorsDeriv( double[] y, double[] x )
	{
		double errDeriv = 0.0;
		for ( int i = 0; i < ndims; i++ )
			errDeriv += ( y[ i ] - x[ i ] ) * ( y[ i ] - x[ i ] );

		return 2 * errDeriv;
	}

	public static double sumSquaredErrors( double[] y, double[] x )
	{
		int ndims = y.length;

		double err = 0.0;
		for ( int i = 0; i < ndims; i++ )
			err += ( y[ i ] - x[ i ] ) * ( y[ i ] - x[ i ] );

		return err;
	}

}
