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
	 * @param inverseTransform the transform defining the inverse direction
	 */
	public ExplicitInvertibleRealTransform( final RealTransform forwardTransform, final RealTransform inverseTransform )
	{
		assert
			forwardTransform.numTargetDimensions() == inverseTransform.numSourceDimensions() &&
			forwardTransform.numSourceDimensions() == inverseTransform.numTargetDimensions() : "number of target and source dimensions not compatible";

		this.forwardTransform = forwardTransform;
		this.inverseTransform = inverseTransform;
		this.inverse = new ExplicitInvertibleRealTransform( this );
	}

	private ExplicitInvertibleRealTransform( final ExplicitInvertibleRealTransform inverse )
	{
		this.forwardTransform = inverse.inverseTransform;
		this.inverseTransform = inverse.forwardTransform;
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
	public void apply( final double[] source, final double[] target )
	{
		forwardTransform.apply( source, target );
	}

	@Override
	public void apply( final RealLocalizable source, final RealPositionable target )
	{
		forwardTransform.apply( source, target );
	}

	@Override
	public void applyInverse( final double[] source, final double[] target )
	{
		inverseTransform.apply( target, source );
	}

	@Override
	public void applyInverse( final RealPositionable source, final RealLocalizable target )
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
