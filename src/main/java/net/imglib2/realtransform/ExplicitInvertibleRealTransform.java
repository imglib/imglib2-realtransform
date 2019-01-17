package net.imglib2.realtransform;

import net.imglib2.RealLocalizable;
import net.imglib2.RealPositionable;

/**
 * An {@link InvertibleRealTransform} for which the forward and inverse
 * transforms are explicitly given as {@link RealTransform}s. Consistency is not
 * internally enforced.
 * 
 * @author John Bogovic
 * @author Stephan Saalfeld
 *
 */
public class ExplicitInvertibleRealTransform implements InvertibleRealTransform
{
	private final RealTransform forwardTransform;

	private final RealTransform inverseTransform;

	private final ExplicitInvertibleRealTransform inverse;

	/**
	 * Creates a new ExplicitInvertibleRealTransform.
	 *  
	 * @param forwardTransform the transform defining the forward direction
	 * @param inverseTransform the trasnform defining the inverse direction
	 */
	public ExplicitInvertibleRealTransform( final RealTransform forwardTransform, final RealTransform inverseTransform )
	{
		this.forwardTransform = forwardTransform;
		this.inverseTransform = inverseTransform;
		this.inverse = new ExplicitInvertibleRealTransform( inverseTransform, forwardTransform, this );
		// TODO explicitly check that source and target dimensions for forward
		// and inverse transforms are compatible?
	}

	private ExplicitInvertibleRealTransform( final RealTransform forwardTransform, final RealTransform inverseTransform, final ExplicitInvertibleRealTransform inverse )
	{
		this.forwardTransform = forwardTransform;
		this.inverseTransform = inverseTransform;
		this.inverse = inverse;
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
	public ExplicitInvertibleRealTransform inverse()
	{
		return inverse;
	}

	@Override
	public InvertibleRealTransform copy()
	{
		return new ExplicitInvertibleRealTransform( forwardTransform, inverseTransform );
	}

}
