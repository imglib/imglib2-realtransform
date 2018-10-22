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

	public RealTransform copy()
	{
		return dxfm.copy();
	}

}
