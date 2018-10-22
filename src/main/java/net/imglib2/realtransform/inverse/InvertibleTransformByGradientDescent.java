package net.imglib2.realtransform.inverse;

import net.imglib2.RealLocalizable;
import net.imglib2.RealPositionable;
import net.imglib2.realtransform.InvertibleRealTransform;

/**
 * Use gradient descent to iteratively estimate the inverse of a differentiable
 * forward transformation.
 * 
 * @author John Bogovic
 *
 */
public class InvertibleTransformByGradientDescent implements InvertibleRealTransform
{
	final boolean isInverse;

	final DifferentiableRealTransform forwardTransform;

	final BacktrackingLineSearch inverseTransform;

	public InvertibleTransformByGradientDescent( final DifferentiableRealTransform forwardTransform )
	{
		this( forwardTransform, false );
	}

	public InvertibleTransformByGradientDescent( final DifferentiableRealTransform forwardTransform, final boolean isInverse )
	{
		this( forwardTransform, new BacktrackingLineSearch( forwardTransform ), isInverse );
	}

	public InvertibleTransformByGradientDescent( final DifferentiableRealTransform forwardTransform, final BacktrackingLineSearch inverseTransform, final boolean isInverse )
	{
		this.forwardTransform = forwardTransform;
		this.inverseTransform = inverseTransform;

		this.isInverse = isInverse;
	}

	@Override
	public void apply( double[] p, double[] q )
	{
		if ( isInverse )
			inverseTransform.iterativeInverse( p, q );
		else
			forwardTransform.apply( p, q );
	}

	@Override
	public void apply( RealLocalizable p, RealPositionable q )
	{
		if ( isInverse )
		{
			double[] pd = new double[ p.numDimensions() ];
			double[] qd = new double[ p.numDimensions() ];

			p.localize( pd );
			inverseTransform.iterativeInverse( pd, qd );
			q.setPosition( qd );
		}
		else
			forwardTransform.apply( p, q );
	}

	@Override
	public void applyInverse( double[] p, double[] q )
	{
		if ( isInverse )
			forwardTransform.apply( p, q );
		else
			inverseTransform.iterativeInverse( p, q );
	}

	@Override
	public void applyInverse( RealPositionable p, RealLocalizable q )
	{
		if ( isInverse )
			forwardTransform.apply( q, p );
		else
		{
			double[] pd = new double[ p.numDimensions() ];
			double[] qd = new double[ p.numDimensions() ];

			q.localize( qd );
			inverseTransform.iterativeInverse( qd, pd );
			p.setPosition( pd );
		}
	}

	@Override
	public InvertibleTransformByGradientDescent copy()
	{
		return new InvertibleTransformByGradientDescent( forwardTransform, isInverse );
	}

	@Override
	public InvertibleTransformByGradientDescent inverse()
	{
		return new InvertibleTransformByGradientDescent( forwardTransform, !isInverse );
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

}
