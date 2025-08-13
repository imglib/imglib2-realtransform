package net.imglib2.realtransform.distortion;

import net.imglib2.RealLocalizable;
import net.imglib2.RealPositionable;
import net.imglib2.realtransform.InvertibleRealTransform;

/**
 * Applies spherical field curvature distortion along the axial dimension.
 * The axial offset follows the formula: z' = z + (R - √(R² - r²)), where R is the radius of curvature
 * and r is the distance from the optical axis.
 * <p>
 * Note: This transform is only invertible for points within radius R from the optical axis.
 * Points at radius r > R will produce NaN values.
 */
public class SphericalCurvatureZDistortion implements InvertibleRealTransform
{
	private final int numDimensions;

	private final int axialDimension;

	private final double R;

	private final double sign;

	/**
	 * Creates a spherical curvature distortion transform.
	 * 
	 * @param numDimensions number of dimensions
	 * @param axialDimension the dimension index of the optical axis where curvature is applied
	 * @param R radius of curvature
	 */
	public SphericalCurvatureZDistortion( final int numDimensions, final int axialDimension, double R )
	{
		this(numDimensions, axialDimension, R, 1.0);
	}

	private SphericalCurvatureZDistortion( final int numDimensions, final int axialDimension, double R, double sign)
	{
		this.numDimensions = numDimensions;
		this.axialDimension = axialDimension;
		this.R = R;
		this.sign = sign;
	}

	@Override
	public int numSourceDimensions()
	{
		return numDimensions;
	}

	@Override
	public int numTargetDimensions()
	{
		return numDimensions;
	}

	@Override
	public void apply( double[] source, double[] target )
	{
		apply(1, source, target);
	}
	
	private void apply( double s, double[] source, double[] target )
	{
		for ( int i = 0; i < numDimensions; i++ )
		{
			if ( i == axialDimension )
				target[ i ] = source[ i ] + s * sign * axialOffset( squaredRadius( source ) );
			else
				target[ i ] = source[ i ];
		}
	}

	@Override
	public void apply( RealLocalizable source, RealPositionable target )
	{

		for ( int i = 0; i < numDimensions; i++ )
		{
			if ( i == axialDimension )
				target.setPosition( source.getDoublePosition( i ) + sign * axialOffset( squaredRadius( source ) ), i );
			else
				target.setPosition( source.getDoublePosition( i ), i );
		}
	}
	
	private void apply( double s, RealLocalizable source, RealPositionable target )
	{

		for ( int i = 0; i < numDimensions; i++ )
		{
			if ( i == axialDimension )
				target.setPosition( source.getDoublePosition( i ) + s * sign * axialOffset( squaredRadius( source ) ), i );
			else
				target.setPosition( source.getDoublePosition( i ), i );
		}
	}
	
	@Override
	public void applyInverse( double[] source, double[] target )
	{
		apply( -1, target, source );
	}

	@Override
	public void applyInverse( RealPositionable source, RealLocalizable target )
	{
		apply( -1, target, source );
	}

	@Override
	public SphericalCurvatureZDistortion copy()
	{
		return new SphericalCurvatureZDistortion( numDimensions, axialDimension, R, sign );
	}

	@Override
	public InvertibleRealTransform inverse()
	{
		return new SphericalCurvatureZDistortion( numDimensions, axialDimension, R, -1 * sign );
	}

	private double squaredRadius( double[] position )
	{
		double r = 0;
		for ( int i = 0; i < position.length; i++ )
			if ( i != axialDimension )
				r += position[ i ] * position[ i ];

		return r;
	}

	private double squaredRadius( RealLocalizable position )
	{
		double r = 0;
		for ( int i = 0; i < position.numDimensions(); i++ )
			if ( i != axialDimension )
				r += position.getDoublePosition( i ) * position.getDoublePosition( i );

		return r;
	}

	private double axialOffset( double r2 )
	{
		return R - Math.sqrt( ( R * R ) - r2 );
	}

}
