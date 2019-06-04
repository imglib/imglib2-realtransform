package net.imglib2.realtransform.inverse;

import net.imglib2.RealLocalizable;
import net.imglib2.RealPositionable;
import net.imglib2.realtransform.InverseRealTransform;
import net.imglib2.realtransform.InvertibleRealTransform;
import net.imglib2.realtransform.RealTransform;

public class WrappedIterativeInvertibleRealTransform< T extends RealTransform > implements InvertibleRealTransform
{
	protected final T forwardTransform;

	protected final DifferentiableRealTransform differentiableTransform;

	protected final InverseRealTransformGradientDescent inverseTransform;

	public WrappedIterativeInvertibleRealTransform( final T xfm )
	{
		this.forwardTransform = xfm;

		if( xfm instanceof DifferentiableRealTransform )
		{
			differentiableTransform = (DifferentiableRealTransform) xfm;
		}
		else
		{
			differentiableTransform = new RealTransformFiniteDerivatives( xfm );
		}

		inverseTransform = new InverseRealTransformGradientDescent( xfm.numSourceDimensions(), differentiableTransform );
	}

	private WrappedIterativeInvertibleRealTransform( final T xfm, InverseRealTransformGradientDescent inverse )
	{
		this.forwardTransform = xfm;
		if( xfm instanceof DifferentiableRealTransform )
		{
			differentiableTransform = (DifferentiableRealTransform) xfm;
		}
		else
		{
			differentiableTransform = new RealTransformFiniteDerivatives( xfm );
		}
		this.inverseTransform = inverse;
	}

	public T getTransform()
	{
		return forwardTransform;
	}

	@Override
	public int numSourceDimensions()
	{
		return forwardTransform.numSourceDimensions();
	}

	@Override
	public int numTargetDimensions()
	{
		return forwardTransform.numTargetDimensions();
	}

	@Override
	public void apply( double[] source, double[] target )
	{
		forwardTransform.apply( source, target );
	}

	@Override
	public void apply( RealLocalizable source, RealPositionable target )
	{
		forwardTransform.apply( source, target );
	}

	@Override
	public void applyInverse( double[] source, double[] target )
	{
		inverseTransform.apply( target, source );
	}

	@Override
	public void applyInverse( RealPositionable source, RealLocalizable target )
	{
		inverseTransform.apply( target, source );
	}

	@Override
	public InvertibleRealTransform inverse()
	{
		return new InverseRealTransform( this );
	}

	public InverseRealTransformGradientDescent getOptimzer()
	{
		return inverseTransform;
	}

	@SuppressWarnings( "unchecked" )
	@Override
	public WrappedIterativeInvertibleRealTransform<T> copy()
	{
		return new WrappedIterativeInvertibleRealTransform< T >( (T)forwardTransform.copy(), (InverseRealTransformGradientDescent)inverseTransform.copy() );
	}
}
