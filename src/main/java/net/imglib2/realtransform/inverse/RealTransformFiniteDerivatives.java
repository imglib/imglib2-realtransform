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

	public RealTransform copy()
	{
		return new RealTransformFiniteDerivatives( transform );
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
