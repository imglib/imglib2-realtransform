package net.imglib2.realtransform;

import net.imglib2.RealLocalizable;
import net.imglib2.RealPositionable;

public class SphericalCurvatureDistortion implements InvertibleRealTransform
{
	private final int numDimensions;

	private final int radialDimension;

	private final Inverse inverse;

	public SphericalCurvatureDistortion( final int numDimensions, final int radialDimension )
	{
		this.numDimensions = numDimensions;
		this.radialDimension = radialDimension;
		inverse = new Inverse();
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
		final double r = r( source );
		for ( int i = 0; i < numDimensions; i++ )
		{
			if ( i == radialDimension )
				target[ i ] = r;
			else
			{
				final double t = t( source[ radialDimension ], source[ i ] );
				target[ i ] = r * Math.sin( t );
			}
		}
	}

	@Override
	public void apply( RealLocalizable source, RealPositionable target )
	{

		final double r = r( source );
		for ( int i = 0; i < numDimensions; i++ )
		{
			if ( i == radialDimension )
				target.setPosition( r, i );
			else
			{
				final double t = t( source.getDoublePosition( radialDimension ), source.getDoublePosition( i ) );
				target.setPosition( r * Math.sin( t ), i );
			}
		}
	}

	@Override
	public void applyInverse( double[] source, double[] target )
	{
		inverse.apply( target, source );
	}

	@Override
	public void applyInverse( RealPositionable source, RealLocalizable target )
	{
		inverse.apply( target, source );
	}

	@Override
	public SphericalCurvatureDistortion copy()
	{
		return new SphericalCurvatureDistortion(numDimensions, radialDimension);
	}

	@Override
	public InvertibleRealTransform inverse()
	{
		return inverse;
	}


	private static double t( final double x, final double y )
	{
		return Math.atan2( y, x );
	}

	private double r( final double[] p )
	{
		return Math.sqrt( squaredRadius( p ) );
	}

	private double r( final RealLocalizable p )
	{
		return Math.sqrt( squaredRadius( p ) );
	}
	
	private double squaredRadius( double[] position )
	{
		double r = 0;
		for( int i = 0; i < position.length; i++ )
			r += position[i] * position[i];

		return r;
	}

	private double squaredRadius( RealLocalizable position )
	{
		double r = 0;
		for( int i = 0; i < position.numDimensions(); i++ )
			r += position.getDoublePosition( i ) * position.getDoublePosition( i );

		return r;
	}

	private class Inverse implements InvertibleRealTransform
	{

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
			/**
			 *  v = r*sin(t)
			 *  v / r
			 */
			final double r = source[radialDimension];

			for ( int i = 0; i < numDimensions; i++ )
			{
				if ( i == radialDimension )
					target[ i ] = r;
				else
				{
					final double t = Math.atan2( source[ i ], r );
					target[ i ] = r * Math.sin( t );
				}
			}

		}

		@Override
		public void apply( RealLocalizable source, RealPositionable target )
		{
			final double r = source.getDoublePosition( radialDimension );
			double partialR = 0;
			for ( int i = 0; i < numDimensions; i++ )
			{
				if ( i != radialDimension )
				{
					final double t = Math.atan2( source.getDoublePosition( i ), r );
					double v = r * Math.sin( t );
					partialR += v * v;
					target.setPosition( v, i );
				}
			}

			// r^2 = partialR^2 + z^2
			// z^2 = r^2 - partialR^2
			// z = sqrt(r^2 - partialR^2)
			final double remR = (r*r) - partialR;
			target.setPosition( Math.sqrt( remR ), radialDimension );
		}

		@Override
		public void applyInverse( double[] source, double[] target )
		{
			SphericalCurvatureDistortion.this.apply( target, source );
		}

		@Override
		public void applyInverse( RealPositionable source, RealLocalizable target )
		{
			SphericalCurvatureDistortion.this.apply( target, source );
		}

		@Override
		public InvertibleRealTransform copy()
		{
			return SphericalCurvatureDistortion.this.copy().inverse();
		}

		@Override
		public InvertibleRealTransform inverse()
		{
			return SphericalCurvatureDistortion.this;
		}
	}

}
