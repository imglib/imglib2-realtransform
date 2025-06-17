package net.imglib2.realtransform.distortion;

import net.imglib2.RealLocalizable;
import net.imglib2.RealPositionable;
import net.imglib2.realtransform.InvertibleRealTransform;

public class RadialMagnificationDistortion implements InvertibleRealTransform
{
	private final int numDimensions;

	private final int axialDimension;

	private final double M0;

	private final double Mr;

	private final Inverse inverse;

	public RadialMagnificationDistortion(
			final int numDimensions,
			final int axialDimension,
			final double m0, final double mr)
	{
		this.numDimensions = numDimensions;
		this.axialDimension = axialDimension;

		this.M0 = m0;
		this.Mr = mr;

		this.inverse = new Inverse();
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
		final double r2 = squaredRadius( axialDimension, source );
		final double m = M0 + Mr * r2;
		for( int i = 0; i < numDimensions; i++ ) {
			if ( i == axialDimension )
				target[ i ] = source[ i ];
			else
				target[ i ] = m * source[ i ];
		}
	}

	@Override
	public void apply( RealLocalizable source, RealPositionable target )
	{
		final double r2 = squaredRadius( axialDimension, source );
		final double m = M0 + Mr * r2;
		
		for( int i = 0; i < numDimensions; i++ ) {
			
			if( i == axialDimension)
				target.setPosition( source.getDoublePosition( i ), i );
			else
				target.setPosition( m * source.getDoublePosition( i ), i);
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
	public InvertibleRealTransform copy()
	{
		return new RadialMagnificationDistortion( numDimensions, axialDimension, M0, Mr );
	}

	@Override
	public InvertibleRealTransform inverse()
	{
		return inverse;
	}
	
	private static double squaredRadius(int axialDirection, double[] position)
	{
		double r = 0;
		for( int i = 0; i < position.length; i++ )
			if( i != axialDirection)
				r += position[i] * position[i];
		
		return r;
	}

	private static double squaredRadius( int axialDirection, RealLocalizable position )
	{
		double r = 0;
		for( int i = 0; i < position.numDimensions(); i++ )
			if( i != axialDirection)
				r += position.getDoublePosition( i ) * position.getDoublePosition( i );

		return r;
	}

	/*
	 * out = (M0 + MR*r2) * in
	 * so for the inverse:
	 * in = out / (M0 + MR*r2)
	 */
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
			final double r2 = squaredRadius( axialDimension, source );
			final double m = M0 + Mr * r2;
			for( int i = 0; i < numDimensions; i++ ) {
				if ( i == axialDimension )
					target[ i ] = source[ i ];
				else
					target[ i ] =  source[ i ] / m;
			}
		}

		@Override
		public void apply( RealLocalizable source, RealPositionable target )
		{
			final double r2 = squaredRadius( axialDimension, source );
			final double m = M0 + Mr * r2;
			for( int i = 0; i < numDimensions; i++ ) {
				
				if( i == axialDimension)
					target.setPosition( source.getDoublePosition( i ), i );
				else
					target.setPosition( source.getDoublePosition( i ) / m, i);
			}
		}

		@Override
		public void applyInverse( double[] source, double[] target )
		{
			RadialMagnificationDistortion.this.apply( target, source );
			
		}

		@Override
		public void applyInverse( RealPositionable source, RealLocalizable target )
		{
			RadialMagnificationDistortion.this.apply( target, source );
		}

		@Override
		public InvertibleRealTransform inverse()
		{
			return RadialMagnificationDistortion.this;
		}

		@Override
		public InvertibleRealTransform copy()
		{
			return RadialMagnificationDistortion.this.copy().inverse();
		}
		
	}

}
